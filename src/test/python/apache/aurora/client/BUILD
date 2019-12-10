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

python2_tests(
  name = 'client',
  sources = globs('*.py'),
  dependencies = [
    '3rdparty/python:futures',
    '3rdparty/python:mock',
    '3rdparty/python:mox',
    '3rdparty/python:pystachio',
    '3rdparty/python:twitter.common.contextutil',
    'src/main/python/apache/aurora/client',
    'src/main/python/apache/aurora/config',
  ],
)

python2_library(
  name = 'lib_fake_scheduler_proxy',
  sources = ['fake_scheduler_proxy.py'],
  dependencies = [
    'src/main/python/apache/aurora/client',
  ]
)

python3_library(
  name = 'lib_fake_scheduler_proxy.py3',
  sources = ['fake_scheduler_proxy.py'],
  dependencies = [
    'src/main/python/apache/aurora/client:python3',
  ]
)

python3_tests(
  name = 'python3',
  sources = globs('*.py'),
  dependencies = [
    '3rdparty/python:mox3',
    '3rdparty/python:pystachio',
    '3rdparty/python:twitter.common.contextutil',
    'src/main/python/apache/aurora/client:python3',
    'src/main/python/apache/aurora/config:python3',
  ],
)
