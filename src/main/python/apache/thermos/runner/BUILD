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
  name = '_runner',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.log',
    'src/main/python/apache/thermos/common',
    'src/main/python/apache/thermos/config',
    'src/main/python/apache/thermos/core',
  ],
)

python2_library(
  name = 'runner',
  dependencies = [
    ':_runner',
  ],
  provides = setup_py(
    name = 'apache.thermos.runner',
    version = aurora_version(),
  ).with_binaries(
    thermos_runner = ':thermos_runner',
  ),
)

python2_binary(
  name = 'thermos_runner',
  entry_point = 'apache.thermos.runner.thermos_runner',
  dependencies = [
    ':_runner',
  ],
)

python3_library(
  name = '_runner3',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.log',
    'src/main/python/apache/thermos/common:python3',
    'src/main/python/apache/thermos/config:python3',
    'src/main/python/apache/thermos/core:python3',
  ],
)

python3_library(
  name = 'python3',
  dependencies = [
    ':_runner3',
  ],
  provides = setup_py(
    name = 'apache.thermos.runner',
    version = aurora_version(),
  ).with_binaries(
    thermos_runner = ':thermos_runner3',
  ),
)

python3_binary(
  name = 'thermos_runner3',
  entry_point = 'apache.thermos.runner.thermos_runner',
  dependencies = [
    ':_runner3',
  ],
)
