#!/usr/bin/env python3
"""One version source: check every copy of the release identity against version.json.

Checked (7 assertions across 4 files): AndroidManifest.xml, update.json,
README.md, scripts/build-windows.ps1.
Run from anywhere: `python app/build/check-version.py`.
`--identity` prints "<versionCode> <versionName>" for scripts/announce.sh.
"""

import json
import os
import re
import sys

# The embedded Windows Python does not add the script's directory to sys.path.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from check_common import repo_root, report


def read_version():
    """Return only the release identity; APK name and SHA are other checkers' job."""
    doc = json.loads((repo_root() / 'version.json').read_text(encoding='utf-8'))
    return (int(doc['versionCode']), str(doc['versionName']))


def main():
    code, name = read_version()
    if '--identity' in sys.argv[1:]:
        print(code, name)
        return 0

    root = repo_root()
    problems = []

    manifest = (root / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
    m_code = re.search(r'android:versionCode="(\d+)"', manifest)
    m_name = re.search(r'android:versionName="([^"]+)"', manifest)
    if not m_code or int(m_code.group(1)) != code:
        problems.append(f'AndroidManifest.xml versionCode should be {code}')
    if not m_name or m_name.group(1) != name:
        problems.append(f'AndroidManifest.xml versionName should be {name}')

    update = json.loads((root / 'update.json').read_text(encoding='utf-8'))
    if update.get('latest_version_code') != code:
        problems.append(f'update.json latest_version_code should be {code}')
    if update.get('latest_version_name') != name:
        problems.append(f'update.json latest_version_name should be {name}')

    readme = (root / 'README.md').read_text(encoding='utf-8')
    if not re.search(r'`' + re.escape(name) + r'\s*\(' + str(code) + r'\)`', readme):
        problems.append(f'README.md "current version" line should say `{name} ({code})`')

    script = (root / 'scripts/build-windows.ps1').read_text(encoding='utf-8')
    if 'version.json' not in script:
        problems.append('build-windows.ps1 must read version.json, not hardcode the release')
    if re.search(r'SpektraLab-\d+\.\d', script):
        problems.append('build-windows.ps1 still contains a hardcoded release APK name')

    return report(
        'check-version: version.json is the single source of truth:',
        problems,
        f'check-version: {name} ({code}) consistent in 7 checks across 4 files')


if __name__ == '__main__':
    raise SystemExit(main())
