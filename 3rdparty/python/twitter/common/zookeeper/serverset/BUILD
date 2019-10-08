python_library(
  sources = globs('*.py', exclude = ['cli.py']),
  dependencies = [
    '3rdparty/python/twitter/common/lang',
    '3rdparty/python/twitter/common/zookeeper/group:group_base',
    '3rdparty/thrift/com/twitter/thrift:py-thrift',
    '3rdparty/python:thrift',
  ]
)

target(
  name = 'kazoo_serverset',
  dependencies = [
    ':serverset',
    '3rdparty/python/twitter/common/zookeeper/group:kazoo_group',
  ]
)

python_binary(
  name = 'cli',
  source = 'cli.py',
  dependencies = [
    '3rdparty/python/twitter/common/app',
    ':serverset',
    '3rdparty/python/twitter/common/zookeeper/group',
  ],
)
