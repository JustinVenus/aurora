from pants.backend.codegen.thrift.python.apache_thrift_py_gen import ApacheThriftPyGen
from pants.backend.codegen.thrift.python.python_thrift_library import PythonThriftLibrary
from pants.backend.python.targets.python_binary import PythonBinary
from pants.backend.python.targets.python_library import PythonLibrary
from pants.backend.python.targets.python_tests import PythonTests
from pants.build_graph.build_file_aliases import BuildFileAliases
from pants.goal.task_registrar import TaskRegistrar as task

from twitter.common.pants.python.commons.version import Version


class Python2Binary(PythonBinary):

  @classmethod
  def py_compatibility(cls):
    return ["CPython>=2.7,<3"]

  def __init__(self, **kwargs):
    kwargs['compatibility'] = self.__class__.py_compatibility()
    super(Python2Binary, self).__init__(**kwargs)


class Python2Tests(PythonTests):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python2Binary.py_compatibility()[0]
    super(Python2Tests, self).__init__(**kwargs)


class Python2Library(PythonLibrary):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python2Binary.py_compatibility()
    super(Python2Library, self).__init__(**kwargs)


class Python2ThriftLibrary(PythonThriftLibrary):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python2Binary.py_compatibility()
    super(Python2ThriftLibrary, self).__init__(**kwargs)


class ApacheThriftPy2Gen(ApacheThriftPyGen):
  gentarget_type = Python2ThriftLibrary


class Python3Binary(PythonBinary):

  @classmethod
  def py_compatibility(cls):
    return ["CPython>=3.6,<4"]

  def __init__(self, **kwargs):
    kwargs['compatibility'] = self.__class__.py_compatibility()
    super(Python3Binary, self).__init__(**kwargs)


class Python3Tests(PythonTests):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python3Binary.py_compatibility()[0]
    super(Python3Tests, self).__init__(**kwargs)


class Python3Library(PythonLibrary):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python3Binary.py_compatibility()
    super(Python3Library, self).__init__(**kwargs)


class Python3ThriftLibrary(PythonThriftLibrary):

  def __init__(self, **kwargs):
    kwargs['compatibility'] = Python3Binary.py_compatibility()
    super(Python3ThriftLibrary, self).__init__(**kwargs)


class ApacheThriftPy3Gen(ApacheThriftPyGen):
  gentarget_type = Python3ThriftLibrary


def build_file_aliases():
  return BuildFileAliases(
      targets={
          'python2_binary': Python2Binary,
          'python2_library': Python2Library,
          'python2_tests': Python2Tests,
          'python2_thrift_library': Python2ThriftLibrary,
          'python3_binary': Python3Binary,
          'python3_library': Python3Library,
          'python3_tests': Python3Tests,
          'python3_thrift_library': Python3ThriftLibrary,
      },
      objects={
          'aurora_version': lambda: Version('.auroraversion').version().upper(),
          'python2_compatibility': Python2Binary.py_compatibility,
          'python3_compatibility': Python3Binary.py_compatibility,
      })


def register_goals():
  task(name='thrift-py2', action=ApacheThriftPy2Gen).install('gen')
  task(name='thrift-py3', action=ApacheThriftPy3Gen).install('gen')
