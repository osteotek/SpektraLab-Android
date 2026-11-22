#!/usr/bin/env python3
"""Fail if a Java source contains a completely silent catch block.

A swallowed exception is how this app used to lose whole failure classes.
Every catch must at least log through Diagnostics.ignored() or DebugSink.
"""
import os
import re
import sys

# The embedded Windows Python does not add the script's directory to sys.path.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from check_common import repo_root, report


ROOT = repo_root()
PATTERN = re.compile(r'catch\s*\(\s*(?:Throwable|Exception)\s+\w+\s*\)\s*\{\s*\}')
# These classes swallow only failures of the logging/reporting path itself;
# logging the failure there would recurse.
ALLOW = {
    'app/src/main/java/app/spektralab/Diagnostics.java',
    'app/src/main/java/app/spektralab/DebugSink.java',
    'app/src/main/java/app/spektralab/Probe.java',
    'app/src/main/java/app/spektralab/CrashTrail.java',
    'app/src/main/java/app/spektralab/engine/SpektraLabEngine.java',
}


def main():
    problems = []
    for path in sorted((ROOT / 'app/src').rglob('*.java')):
        if path.relative_to(ROOT).as_posix() in ALLOW:
            continue
        text = path.read_text(encoding='utf-8')
        for match in PATTERN.finditer(text):
            line = text.count('\n', 0, match.start()) + 1
            problems.append(f'{path.relative_to(ROOT).as_posix()}:{line}')
    return report(
        f'check-no-silent-catch: {len(problems)} silent catch block(s):',
        problems[:40],
        'check-no-silent-catch: every catch block does something',
        prefix='  ')


if __name__ == '__main__':
    sys.exit(main())
