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
  name = 'common',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:pystachio',
    '3rdparty/python:twitter.common.log',
    '3rdparty/python:twitter.common.recordio',
    '3rdparty/python:twitter.common.recordio-thrift',
    'api/src/main/thrift/org/apache/aurora/gen',
    'api/src/main/thrift/org/apache/thermos'
  ],
  provides = setup_py(
    name = 'apache.thermos.common',
    version = aurora_version(),
    description = 'Thermos common libraries.',
  )
)

python3_library(
  name = 'python3',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:pystachio',
    '3rdparty/python:twitter.common.log',
    '3rdparty/python:twitter.common.recordio',
    '3rdparty/python:twitter.common.recordio-thrift',
    'api/src/main/thrift/org/apache/aurora/gen:python3',
    'api/src/main/thrift/org/apache/thermos:python3'
  ],
  provides = setup_py(
    name = 'apache.thermos.common',
    version = aurora_version(),
    description = 'Thermos common libraries.',
  )
)
