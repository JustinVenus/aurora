#
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

python2_library(
  name = '_admin',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.log',
    '3rdparty/python:twitter.common.quantity',
    'api/src/main/thrift/org/apache/aurora/gen',
    'src/main/python/apache/aurora/client',
    'src/main/python/apache/aurora/common',
    'src/main/python/apache/aurora/config',
  ]
)

python2_binary(
  name = 'aurora_admin',
  entry_point = 'apache.aurora.admin.aurora_admin:proxy_main',
  dependencies = [
    ':_admin',
  ]
)

python2_library(
  name = 'admin',
  dependencies = [
    ':_admin',
  ],
  provides = setup_py(
    name = 'apache.aurora.admin',
    version = aurora_version(),
  ).with_binaries({
    'aurora_admin': ':aurora_admin',
  }),
)

python3_library(
  name = '_admin3',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.log',
    '3rdparty/python:twitter.common.quantity',
    'api/src/main/thrift/org/apache/aurora/gen:python3',
    'src/main/python/apache/aurora/client:python3',
    'src/main/python/apache/aurora/common:python3',
    'src/main/python/apache/aurora/config:python3',
  ]
)

python3_binary(
  name = 'aurora_admin3',
  entry_point = 'apache.aurora.admin.aurora_admin:proxy_main',
  dependencies = [
    ':_admin3',
  ]
)

python3_library(
  name = 'python3',
  dependencies = [
    ':_admin3',
  ],
  provides = setup_py(
    name = 'apache.aurora.admin',
    version = aurora_version(),
  ).with_binaries({
    'aurora_admin': ':aurora_admin3',
  }),
)
