python_library(
  name = 'group_base',
  sources = ['group_base.py'],
  dependencies = [
    '3rdparty/python/twitter/common/concurrent',
    '3rdparty/python/twitter/common/lang',
  ]
)

python_library(
  name = 'group',
  sources = globs('*.py', exclude = ['kazoo_group.py']),
  dependencies = [
    ':group_base',
    '3rdparty/python/twitter/common/concurrent',
    '3rdparty/python/twitter/common/exceptions',
    '3rdparty/python/twitter/common/quantity',
    '3rdparty/python/twitter/common/zookeeper:zookeeper-old',
  ]
)

python_library(
  name = 'kazoo_group',
  sources = ['kazoo_group.py'],
  dependencies = [
    ':group_base',
    '3rdparty/python/twitter/common/concurrent',
    '3rdparty/python/twitter/common/zookeeper:kazoo_client',
  ]
)

python_binary(
  name = 'kazoo_cli',
  source = 'kazoo_cli.py',
  dependencies = [
    '3rdparty/python/twitter/common/app',
    ':kazoo_group',
  ]
)
