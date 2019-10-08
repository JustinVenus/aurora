# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

MESOS_REV = '1.6.1'

python_requirement_library(
  name = 'mesos.interface',
  requirements = [
    python_requirement(
      requirement = 'mesos.interface==%s' % MESOS_REV,
    )
  ]
)

python_requirement_library(
  name = 'mesos.executor',
  requirements = [
    python_requirement(
      requirement = 'mesos.executor==%s' % MESOS_REV,
      # This native executor egg must currently be fetched out-of-band.
      # See build-support/packer/build.sh which fetches to third_party/ and uses mesos.executor to
      # enable a running cluster inside the root Vagrant machine.
      repository = '%s/third_party' % get_buildroot(),
    )
  ]
)

python_requirements()

def twitter_common(library):
  target(
    name = 'twitter.common.{}'.format(library),
    dependencies = [
      'src/python/twitter/common/{0}:{0}'.format(library)
    ]
  )

twitter_common('app')
twitter_common('collections')
twitter_common('concurrent')
twitter_common('contextutil')
twitter_common('decorators')
twitter_common('dirutil')
twitter_common('exceptions')
twitter_common('http')
twitter_common('lang')
twitter_common('log')
twitter_common('metrics')
twitter_common('options')
twitter_common('process')
twitter_common('quantity')
twitter_common('recordio')
twitter_common('string')
twitter_common('testing')
twitter_common('zookeeper')

target(
  name = 'twitter.common.recordio-thrift',
  dependencies = [
    'src/python/twitter/common/recordio:recordio-thrift',
  ]
)
