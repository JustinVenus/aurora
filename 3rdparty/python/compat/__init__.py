import contextlib

__all__ = ['contextlib']


if not hasattr(contextlib, 'nested'):
  # Reimplementation of nested in python 3.

  @contextlib.contextmanager
  def nested(*contexts):
    with contextlib.ExitStack() as stack:
      for ctx in contexts:
        stack.enter_context(ctx)
      yield contexts

  contextlib.nested = nested
