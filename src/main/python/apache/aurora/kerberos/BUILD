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
  name = 'kerberos_auth',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:requests-kerberos',
    'src/main/python/apache/aurora/common',
  ],
)

python2_library(
  name = '_kerberos',
  sources = rglobs('*.py'),
  dependencies = [
    ':kerberos_auth',
    'src/main/python/apache/aurora/admin',
    'src/main/python/apache/aurora/client',
  ],
)

python2_binary(
  name = 'kaurora',
  entry_point = 'apache.aurora.client.cli.client:proxy_main',
  dependencies = [
    ':_kerberos',
  ],
)

python2_binary(
  name = 'kaurora_admin',
  entry_point = 'apache.aurora.admin.aurora_admin:proxy_main',
  dependencies = [
    ':_kerberos',
  ],
)

python2_library(
  name = 'kerberos',
  dependencies = [
    ':_kerberos',
  ],
  provides = setup_py(
    name = 'apache.aurora.kerberos',
    version = aurora_version(),
  ).with_binaries({
    # TODO(ksweeney): Reconsider these as separate binaries in favor of extras_require or plugins.
    'kaurora': ':kaurora',
    'kaurora_admin': ':kaurora_admin',
  }),
)

python3_library(
  name = 'kerberos_auth3',
  sources = rglobs('*.py'),
  dependencies = [
    '3rdparty/python:requests-kerberos',
    'src/main/python/apache/aurora/common:python3',
  ],
)

python3_library(
  name = '_kerberos3',
  sources = rglobs('*.py'),
  dependencies = [
    ':kerberos_auth3',
    'src/main/python/apache/aurora/admin:python3',
    'src/main/python/apache/aurora/client:python3',
  ],
)

python3_binary(
  name = 'kaurora3',
  entry_point = 'apache.aurora.client.cli.client:proxy_main',
  dependencies = [
    ':_kerberos3',
  ],
)

python3_binary(
  name = 'kaurora_admin3',
  entry_point = 'apache.aurora.admin.aurora_admin:proxy_main',
  dependencies = [
    ':_kerberos3',
  ],
)

python3_library(
  name = 'python3',
  dependencies = [
    ':_kerberos3',
  ],
  provides = setup_py(
    name = 'apache.aurora.kerberos',
    version = aurora_version(),
  ).with_binaries({
    # TODO(ksweeney): Reconsider these as separate binaries in favor of extras_require or plugins.
    'kaurora': ':kaurora3',
    'kaurora_admin': ':kaurora_admin3',
  }),
)
