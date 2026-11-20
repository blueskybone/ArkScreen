#!/usr/bin/env python3
"""Increment the development version once, then build an installable APK."""
import argparse
from pathlib import Path
import re
import subprocess
import sys

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--variant', choices=('r8Probe', 'debug', 'release'), default='r8Probe')
parser.add_argument('--offline', action='store_true')
args = parser.parse_args()
root = Path(__file__).resolve().parent.parent
config = root / 'app/build.gradle.kts'
source = config.read_text()
code = re.search(r'(?m)^(\s*versionCode\s*=\s*)(\d+)\s*$', source)
name = re.search(r'(?m)^(\s*versionName\s*=\s*)"(\d+\.\d+\.\d+)-dev\.?([0-9]+)"\s*$', source)
if not code or not name:
    sys.exit('Expected one development versionName and a numeric versionCode; no changes made.')
next_code = int(code.group(2)) + 1
next_name = f'{name.group(2)}-dev.{int(name.group(3)) + 1}'
source = source[:name.start()] + f'{name.group(1)}"{next_name}"' + source[name.end():]
source = re.sub(r'(?m)^(\s*versionCode\s*=\s*)\d+\s*$', lambda m: f'{m.group(1)}{next_code}', source, count=1)
config.write_text(source)
print(f'Building {next_name} (versionCode {next_code}), variant={args.variant}', flush=True)
# Keep the increment on failure: never reuse a version that might already have been installed.
task = f':app:assemble{args.variant[0].upper()}{args.variant[1:]}'
command = ['bash', 'gradlew', task]
if args.offline:
    command.append('--offline')
sys.exit(subprocess.call(command, cwd=root))
