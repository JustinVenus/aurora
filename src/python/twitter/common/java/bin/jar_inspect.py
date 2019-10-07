# ==================================================================================================
# Copyright 2011 Twitter, Inc.
# --------------------------------------------------------------------------------------------------
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this work except in compliance with the License.
# You may obtain a copy of the License in the LICENSE file, or at:
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==================================================================================================

import os
from zipfile import ZipFile

from twitter.common import app, log
from twitter.common.java.class_file import ClassFile

app.set_option('log_to_stderr', 'INFO')

def main(args):
  log.debug('main got args: %s' % args)
  for arg in args:
    if arg.endswith('.jar'):
      zp = ZipFile(arg)
      for f in zp.filelist:
        if f.filename.endswith('.class'):
          print
          print
          print '%s' % f.filename,
          foo = ClassFile(zp.read(f.filename))
          print ' => methods:%s, interfaces:%s' % (
            len(foo.methods()),
            len(foo.interfaces()))
          print foo
      zp.close()

app.main()
