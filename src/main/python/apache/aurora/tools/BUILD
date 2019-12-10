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

THERMOS_SOURCE = 'thermos.py'
OBSERVER_SOURCE = 'thermos_observer.py'

python2_library(
  name = '_tools',
  sources = rglobs('*.py', exclude=[[THERMOS_SOURCE], [OBSERVER_SOURCE]]),
  dependencies = [
    '3rdparty/python:CherryPy',
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.exceptions',
    '3rdparty/python:twitter.common.log',
    'src/main/python/apache/aurora/executor',
    'src/main/python/apache/thermos/cli',
    'src/main/python/apache/thermos/common',
    'src/main/python/apache/thermos/monitoring',
    'src/main/python/apache/thermos/observer',
  ],
)

python2_library(
  name = '_thermos',
  sources = [THERMOS_SOURCE],
  dependencies = [
    ':_tools',
  ],
)

python2_binary(
  name = 'thermos',
  # TODO(ksweeney): Investigate making this use entry_point instead.
  source = THERMOS_SOURCE,
  dependencies = [
    ':_tools',
  ]
)

python2_library(
  name = '_thermos_observer',
  sources = [OBSERVER_SOURCE],
  dependencies = [
    ':_tools',
  ],
)

python2_binary(
  name = 'thermos_observer',
  # TODO(ksweeney): Investigate making this use entry_point instead.
  source = OBSERVER_SOURCE,
  dependencies = [
    ':_tools',
  ]
)

python2_library(
  name = 'tools',
  dependencies = [
    ':_thermos',
    ':_thermos_observer',
    ':_tools',
  ],
  provides = setup_py(
    name = 'apache.aurora.tools',
    version = aurora_version(),
  ).with_binaries({
    'thermos': ':thermos',
    'thermos_observer': ':thermos_observer',
  }),
)

python3_library(
  name = '_tools3',
  sources = rglobs('*.py', exclude=[[THERMOS_SOURCE], [OBSERVER_SOURCE]]),
  dependencies = [
    '3rdparty/python:CherryPy',
    '3rdparty/python:twitter.common.app',
    '3rdparty/python:twitter.common.exceptions',
    '3rdparty/python:twitter.common.log',
    'src/main/python/apache/aurora/executor:python3',
    'src/main/python/apache/thermos/cli:python3',
    'src/main/python/apache/thermos/common:python3',
    'src/main/python/apache/thermos/monitoring:python3',
    'src/main/python/apache/thermos/observer:python3',
  ],
)

python3_library(
  name = '_thermos3',
  sources = [THERMOS_SOURCE],
  dependencies = [
    ':_tools3',
  ],
)

python3_binary(
  name = 'thermos3',
  # TODO(ksweeney): Investigate making this use entry_point instead.
  source = THERMOS_SOURCE,
  dependencies = [
    ':_tools3',
  ]
)

python3_library(
  name = '_thermos_observer3',
  sources = [OBSERVER_SOURCE],
  dependencies = [
    ':_tools3',
  ],
)

python3_binary(
  name = 'thermos_observer3',
  # TODO(ksweeney): Investigate making this use entry_point instead.
  source = OBSERVER_SOURCE,
  dependencies = [
    ':_tools3',
  ]
)

python3_library(
  name = 'python3',
  dependencies = [
    ':_thermos3',
    ':_thermos_observer3',
    ':_tools3',
  ],
  provides = setup_py(
    name = 'apache.aurora.tools',
    version = aurora_version(),
  ).with_binaries({
    'thermos': ':thermos3',
    'thermos_observer': ':thermos_observer3',
  }),
)
