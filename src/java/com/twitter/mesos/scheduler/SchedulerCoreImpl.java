package com.twitter.mesos.scheduler;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import com.twitter.common.util.StateMachine;
import com.twitter.mesos.Tasks;
import com.twitter.mesos.gen.AssignedTask;
import com.twitter.mesos.gen.JobConfiguration;
import com.twitter.mesos.gen.Quota;
import com.twitter.mesos.gen.ScheduleStatus;
import com.twitter.mesos.gen.ScheduledTask;
import com.twitter.mesos.gen.TaskQuery;
import com.twitter.mesos.gen.TwitterTaskInfo;
import com.twitter.mesos.gen.UpdateResult;
import com.twitter.mesos.scheduler.StateManagerImpl.StateChanger;
import com.twitter.mesos.scheduler.StateManagerImpl.StateMutation;
import com.twitter.mesos.scheduler.StateManagerImpl.UpdateException;
import com.twitter.mesos.scheduler.configuration.ConfigurationManager;
import com.twitter.mesos.scheduler.quota.QuotaManager;
import com.twitter.mesos.scheduler.quota.Quotas;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

import static com.twitter.common.base.MorePreconditions.checkNotBlank;
import static com.twitter.mesos.Tasks.SCHEDULED_TO_SHARD_ID;
import static com.twitter.mesos.Tasks.jobKey;
import static com.twitter.mesos.gen.ScheduleStatus.KILLING;
import static com.twitter.mesos.gen.ScheduleStatus.RESTARTING;
import static com.twitter.mesos.gen.ScheduleStatus.ROLLBACK;
import static com.twitter.mesos.gen.ScheduleStatus.UPDATING;
import static com.twitter.mesos.scheduler.SchedulerCoreImpl.State.CONSTRUCTED;
import static com.twitter.mesos.scheduler.SchedulerCoreImpl.State.INITIALIZED;
import static com.twitter.mesos.scheduler.SchedulerCoreImpl.State.STANDING_BY;
import static com.twitter.mesos.scheduler.SchedulerCoreImpl.State.STARTED;
import static com.twitter.mesos.scheduler.SchedulerCoreImpl.State.STOPPED;

/**
 * Implementation of the scheduler core.
 */
public class SchedulerCoreImpl implements SchedulerCore {

  private static final Logger LOG = Logger.getLogger(SchedulerCoreImpl.class.getName());

  private static final Function<TwitterTaskInfo, TwitterTaskInfo> COPY_AND_RESET_START_COMMAND =
      new Function<TwitterTaskInfo, TwitterTaskInfo>() {
        @Override public TwitterTaskInfo apply(TwitterTaskInfo task) {
          TwitterTaskInfo copy = task.deepCopy();
          ConfigurationManager.resetStartCommand(copy);
          return copy;
        }
      };

  private static final Predicate<ScheduledTask> IS_UPDATING = new Predicate<ScheduledTask>() {
    @Override public boolean apply(ScheduledTask task) {
      return task.getStatus() == UPDATING || task.getStatus() == ROLLBACK;
    }
  };

  private final CronJobManager cronScheduler;

  // Schedulers that are responsible for triggering execution of jobs.
  private final ImmutableList<JobManager> jobManagers;

  // State manager handles persistence of task modifications and state transitions.
  private final StateManagerImpl stateManager;

  private final QuotaManager quotaManager;

  /**
   * Scheduler states.
   */
  enum State {
    CONSTRUCTED,
    STANDING_BY,
    INITIALIZED,
    STARTED,
    STOPPED
  }

  private final StateMachine<State> stateMachine;

  /**
   * Creates a new core scheduler.
   *
   * @param cronScheduler Cron scheduler.
   * @param immediateScheduler Immediate scheduler.
   * @param stateManager Persistent state manager.
   * @param quotaManager Quota tracker.
   */
  @Inject
  public SchedulerCoreImpl(CronJobManager cronScheduler,
      ImmediateJobManager immediateScheduler,
      StateManagerImpl stateManager,
      QuotaManager quotaManager) {

    // The immediate scheduler will accept any job, so it's important that other schedulers are
    // placed first.
    this.jobManagers = ImmutableList.of(cronScheduler, immediateScheduler);
    this.cronScheduler = cronScheduler;
    this.stateManager = checkNotNull(stateManager);
    this.quotaManager = checkNotNull(quotaManager);

   // TODO(John Sirois): Add a method to StateMachine or write a wrapper that allows for a
   // read-locked do-in-state assertion around a block of work.  Transition would then need to grab
   // the write lock.  Another approach is to force these transitions with:
   // SchedulerCoreFactory -> SchedulerCoreRunner -> SchedulerCore which remove all state sensitive
   // methods out of schedulerCore save for stop.
   stateMachine = StateMachine.<State>builder("scheduler-core")
       .initialState(CONSTRUCTED)
       .addState(CONSTRUCTED, STANDING_BY)
       .addState(STANDING_BY, INITIALIZED)
       .addState(INITIALIZED, STARTED)
       .addState(STARTED, STOPPED)
       .build();
  }

  @Override
  public synchronized void prepare() {
    checkLifecycleState(CONSTRUCTED);
    stateManager.prepare();
    stateMachine.transition(STANDING_BY);
  }

  @Override
  public synchronized String initialize() {
    checkLifecycleState(STANDING_BY);
    String storedFrameworkId = stateManager.initialize();
    stateMachine.transition(INITIALIZED);
    return storedFrameworkId;
  }

  @Override
  public synchronized void start() {
    checkLifecycleState(INITIALIZED);
    stateManager.start();
    stateMachine.transition(STARTED);

    for (JobManager jobManager : jobManagers) {
      jobManager.start();
    }
  }

  @Override
  public synchronized void registered(String frameworkId) {
    checkStarted();
    stateManager.setFrameworkId(frameworkId);
  }

  @Override
  public synchronized Set<ScheduledTask> getTasks(TaskQuery query) {
    checkStarted();

    return stateManager.fetchTasks(query);
  }

  @Override
  public Iterable<TwitterTaskInfo> apply(TaskQuery query) {
    return Iterables.transform(getTasks(query), Tasks.SCHEDULED_TO_INFO);
  }

  private boolean hasActiveJob(JobConfiguration job) {
    return Iterables.any(jobManagers, managerHasJob(job));
  }

  @Override
  public synchronized void tasksDeleted(Set<String> taskIds) {
    setTaskStatus(Query.byId(taskIds), ScheduleStatus.UNKNOWN, Optional.<String>absent());
  }

  @Override
  public synchronized void createJob(JobConfiguration job) throws ScheduleException,
      ConfigurationManager.TaskDescriptionException {
    checkStarted();

    final JobConfiguration populated = ConfigurationManager.validateAndPopulate(job);

    if (hasActiveJob(populated)) {
      throw new ScheduleException("Job already exists: " + jobKey(populated));
    }

    ensureHasAdditionalQuota(job.getOwner().getRole(), Quotas.fromJob(populated));

    boolean accepted = false;
    for (final JobManager manager : jobManagers) {
      if (manager.receiveJob(populated)) {
        LOG.info("Job accepted by manager: " + manager.getUniqueKey());
        accepted = true;
        break;
      }
    }

    if (!accepted) {
      LOG.severe("Job was not accepted by any of the configured schedulers, discarding.");
      LOG.severe("Discarded job: " + populated);
      throw new ScheduleException("Job not accepted, discarding.");
    }
  }

  @Override
  public synchronized void startCronJob(String role, String job) throws ScheduleException {
    checkNotBlank(role);
    checkNotBlank(job);
    checkStarted();

    String key = Tasks.jobKey(role, job);
    if (!cronScheduler.hasJob(key)) {
      throw new ScheduleException("Cron job does not exist for " + key);
    }

    cronScheduler.startJobNow(key);
  }

  @Override
  public synchronized void runJob(JobConfiguration job) {
    checkNotNull(job);
    checkNotNull(job.getTaskConfigs());
    checkStarted();

    launchTasks(job.getTaskConfigs());
  }

  /**
   * Launches tasks.
   *
   * @param tasks Tasks to launch.
   * @return The task IDs of the new tasks.
   */
  private Set<String> launchTasks(Set<TwitterTaskInfo> tasks) {
    if (tasks.isEmpty()) {
      return ImmutableSet.of();
    }

    LOG.info("Launching " + tasks.size() + " tasks.");
    return stateManager.insertTasks(tasks);
  }

  /**
   * Creates a predicate that will determine whether a job manager has a job matching a job key.
   *
   * @param job Job to match.
   * @return A new predicate matching the job owner and name given.
   */
  private static Predicate<JobManager> managerHasJob(final JobConfiguration job) {
    return new Predicate<JobManager>() {
      @Override public boolean apply(JobManager manager) {
        return manager.hasJob(jobKey(job));
      }
    };
  }

  @Override
  public synchronized void setTaskStatus(TaskQuery query, final ScheduleStatus status,
      Optional<String> message) {
    checkStarted();
    checkNotNull(query);
    checkNotNull(status);

    stateManager.changeState(query, status, message);
  }

  @Override
  public synchronized void killTasks(TaskQuery query, String user) throws ScheduleException {
    checkStarted();
    checkNotNull(query);
    LOG.info("Killing tasks matching " + query);

    boolean matchingScheduler = false;
    boolean updateFinished = false;

    if (Query.specifiesJobOnly(query)) {
      // If this looks like a query for all tasks in a job, instruct the scheduler modules to
      // delete the job.
      for (JobManager manager : jobManagers) {
        matchingScheduler = manager.deleteJob(Query.getJobKey(query).get()) || matchingScheduler;
      }

      String role = query.getOwner().getRole();
      String job = query.getJobName();
      if (!matchingScheduler) {
        try {
          updateFinished = stateManager.finishUpdate(
              role, job, Optional.<String>absent(), UpdateResult.TERMINATE, false);
        } catch (UpdateException e) {
          LOG.severe(String.format("Could not terminate job update for %s\n%s",
              query.getJobKey(), e.getMessage()));
        }
      }
    }

    int tasksAffected = stateManager.changeState(query, KILLING, Optional.of("Killed by " + user));
    if (!matchingScheduler && !updateFinished && (tasksAffected == 0)) {
      throw new ScheduleException("No jobs to kill");
    }
  }

  @Override
  public synchronized void restartTasks(final Set<String> taskIds) throws RestartException {
    checkStarted();
    checkNotBlank(taskIds);

    LOG.info("Restart requested for tasks " + taskIds);

    // TODO(William Farner): Change this (and the thrift interface) to query by shard ID in the
    //    context of a job instead of task ID.

    stateManager.taskOperation(Query.byId(taskIds),
        new StateMutation<RestartException>() {
          @Override public void execute(Set<ScheduledTask> tasks, StateChanger changer)
              throws RestartException {

            if (tasks.size() != taskIds.size()) {
              Set<String> unknownTasks = Sets.difference(taskIds, Tasks.ids(tasks));
              throw new RestartException("Restart requested for unknown tasks " + unknownTasks);
            } else if (Iterables.any(tasks, Predicates.not(Tasks.ACTIVE_FILTER))) {
              throw new RestartException("Restart requested for inactive tasks "
                  + Iterables.filter(tasks, Tasks.ACTIVE_FILTER));
            }

            Set<String> jobKeys = ImmutableSet.copyOf(transform(tasks, Tasks.SCHEDULED_TO_JOB_KEY));
            if (jobKeys.size() != 1) {
              throw new RestartException(
                  "Task restart request cannot span multiple jobs: " + jobKeys);
            }

            changer.changeState(Tasks.ids(tasks), RESTARTING, "Restarting by client request");
          }
        });
  }

  private void ensureHasAdditionalQuota(String role, Quota quota) throws ScheduleException {
    if (!quotaManager.hasRemaining(role, quota)) {
      throw new ScheduleException("Insufficient resource quota.");
    }
  }

  @Override
  public synchronized Optional<String> initiateJobUpdate(JobConfiguration job)
      throws ScheduleException, ConfigurationManager.TaskDescriptionException {
    checkStarted();

    JobConfiguration populated = ConfigurationManager.validateAndPopulate(job);

    if (cronScheduler.hasJob(Tasks.jobKey(job))) {
      cronScheduler.updateJob(job);
      return Optional.absent();
    }

    Set<ScheduledTask> existingTasks =
        stateManager.fetchTasks(Query.activeQuery(Tasks.jobKey(job)));

    // Reject if any existing task for the job is in UPDATING/ROLLBACK
    if (Iterables.any(existingTasks, IS_UPDATING)) {
      throw new ScheduleException("Update/Rollback already in progress for " + Tasks.jobKey(job));
    }

    if (!existingTasks.isEmpty()) {
      Quota currentJobQuota =
          Quotas.fromTasks(Iterables.transform(existingTasks, Tasks.SCHEDULED_TO_INFO));
      Quota newJobQuota = Quotas.fromJob(populated);
      Quota additionalQuota = Quotas.subtract(newJobQuota, currentJobQuota);
      ensureHasAdditionalQuota(job.getOwner().getRole(), additionalQuota);
    }

    try {
      return Optional.of(stateManager.registerUpdate(job.getOwner().getRole(), job.getName(),
          populated.getTaskConfigs()));
    } catch (StateManagerImpl.UpdateException e) {
      LOG.log(Level.INFO, "Failed to start update.", e);
      throw new ScheduleException(e.getMessage(), e);
    }
  }

  @Override
  public synchronized void updateShards(String role, String jobName, Set<Integer> shards,
      String updateToken) throws ScheduleException {
    checkStarted();

    String jobKey = Tasks.jobKey(role, jobName);
    Set<ScheduledTask> tasks = stateManager.fetchTasks(Query.liveShards(jobKey, shards));

    // Extract any shard IDs that are being added as a part of this stage in the update.
    Set<Integer> newShardIds = Sets.difference(shards,
        ImmutableSet.copyOf(Iterables.transform(tasks, SCHEDULED_TO_SHARD_ID)));

    if (!newShardIds.isEmpty()) {
      Set<TwitterTaskInfo> newTasks =
          stateManager.fetchUpdatedTaskConfigs(role, jobName, newShardIds);
      Set<Integer> unrecognizedShards = Sets.difference(newShardIds,
          ImmutableSet.copyOf(Iterables.transform(newTasks, Tasks.INFO_TO_SHARD_ID)));
      if (!unrecognizedShards.isEmpty()) {
        throw new ScheduleException("Cannot update unrecognized shards " + unrecognizedShards);
      }

      // Create new tasks, so they will be moved into the PENDING state.
      stateManager.insertTasks(newTasks);
    }

    Set<Integer> updateShardIds = Sets.difference(shards, newShardIds);
    if (!updateShardIds.isEmpty()) {
      Set<TwitterTaskInfo> oldTasks = ImmutableSet.copyOf(Iterables.transform(tasks,
          Functions.compose(COPY_AND_RESET_START_COMMAND, Tasks.SCHEDULED_TO_INFO)));
      // No need to reset the start command here since the updated tasks have not been populated.
      Set<TwitterTaskInfo> newTasks =
          stateManager.fetchUpdatedTaskConfigs(role, jobName, updateShardIds);

      Set<TwitterTaskInfo> changedTasks = Sets.difference(newTasks, oldTasks);
      Set<Integer> changedShards =
          ImmutableSet.copyOf(Iterables.transform(changedTasks, Tasks.INFO_TO_SHARD_ID));

      if (!changedShards.isEmpty()) {
        // Initiate update on the existing shards.
        stateManager.changeState(Query.liveShards(jobKey, changedShards), ScheduleStatus.UPDATING);
      }
    }
  }

  @Override
  public synchronized void rollbackShards(String role, String jobName, Set<Integer> shards,
      String updateToken) throws ScheduleException {
    checkStarted();

    stateManager.changeState(Query.liveShards(Tasks.jobKey(role, jobName), shards),
        ScheduleStatus.ROLLBACK);
  }

  @Override
  public synchronized void finishUpdate(String role, String jobName, Optional<String> updateToken,
      UpdateResult result) throws ScheduleException {
    checkStarted();

    try {
      stateManager.finishUpdate(role, jobName, updateToken, result, true);
    } catch (StateManagerImpl.UpdateException e) {
      LOG.log(Level.INFO, "Failed to finish update.", e);
      throw new ScheduleException(e.getMessage(), e);
    }
  }

  @Override
  public synchronized void preemptTask(AssignedTask task, AssignedTask preemptingTask) {
    checkNotNull(task);
    checkNotNull(preemptingTask);
    // TODO(William Farner): Throw SchedulingException if either task doesn't exist, etc.

    stateManager.changeState(Query.byId(task.getTaskId()), ScheduleStatus.PREEMPTING,
        Optional.of("Preempting in favor of " + Tasks.jobKey(preemptingTask)));
  }

  @Override
  public synchronized void stop() {
    checkStarted();
    stateManager.stop();
    stateMachine.transition(STOPPED);
  }

  private void checkStarted() {
    checkLifecycleState(STARTED);
  }

  private void checkLifecycleState(State state) {
    Preconditions.checkState(stateMachine.getState() == state);
  }
}
