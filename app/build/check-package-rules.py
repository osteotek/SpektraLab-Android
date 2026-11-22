#!/usr/bin/env python3
"""Enforce the package boundaries from docs/android-skill-adoption.md.

app → ui → domain → data is not the rule: domain is pure, data implements it,
ui consumes domain, and the root package wires everything. The checker stops
the accidental imports that turn this into one big package again.
"""
import os
import re
import sys

# The embedded Windows Python does not add the script's directory to sys.path.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from check_common import repo_root, report


JAVA = repo_root() / 'app/src/main/java/app/spektralab'
IMPORT = re.compile(r'^\s*import\s+(?:static\s+)?([\w.]+)\s*;', re.M)


def main():
    problems = []
    for path in sorted(JAVA.rglob('*.java')):
        rel = path.relative_to(JAVA).as_posix()
        package = rel.split('/')[0] if '/' in rel else 'app'
        text = path.read_text(encoding='utf-8')
        imports = IMPORT.findall(text)
        for name in imports:
            if package == 'domain':
                if name.startswith('android.') or name.endswith('MainActivity'):
                    problems.append(f'{rel}: domain must not import {name}')
                if '.data.' in name or '.ui.' in name:
                    problems.append(f'{rel}: domain must not depend on data/ui ({name})')
            elif package == 'ui':
                if '.data.' in name or name.endswith('MainActivity'):
                    problems.append(f'{rel}: ui must not import {name}')
            elif package == 'data':
                if '.ui.' in name or name.endswith('MainActivity'):
                    problems.append(f'{rel}: data must not import {name}')
    # Phase 6: every extracted worker must talk to MainActivity only through
    # its own narrow host interface, never through the concrete Activity.
    worker_hosts = {
        'ImportWorker': 'ImportWorkerHost',
        'RawImportWorker': 'RawImportWorkerHost',
        'ExportWorker': 'ExportWorkerHost',
        'RenderWorker': 'RenderWorkerHost',
        'BatchImportWorker': 'BatchImportHost',
        'BatchExportWorker': 'BatchExportHost',
    }
    for name, host_name in worker_hosts.items():
        path = JAVA / (name + '.java')
        if not path.exists():
            problems.append(f'{name}.java: missing worker')
            continue
        source = path.read_text(encoding='utf-8')
        # Strip comments and string literals so prose and diagnostic tags about
        # the old coupling do not trip the check. Frame/Look are nested types
        # still shared until Phase 5 consolidates them.
        code = re.sub(r'"(?:[^"\\]|\\.)*"', '""', source, flags=re.S)
        code = re.sub(r'/\*.*?\*/|//[^\n]*', '', code, flags=re.S)
        for nested in ('Frame', 'Look', 'LiveClip'):
            code = code.replace('MainActivity.' + nested, nested)
        if re.search(r'\bMainActivity\b', code):
            problems.append(f'{name}.java: worker must not reference MainActivity directly')
        if not re.search(r'private final ' + host_name + r'\s+host\s*;', code):
            problems.append(f'{name}.java: worker must hold a {host_name} host')
    host_worker = JAVA / 'HostWorker.java'
    if host_worker.exists():
        code = re.sub(r'/\*.*?\*/|//[^\n]*', '', host_worker.read_text(encoding='utf-8'), flags=re.S)
        if re.search(r'\bMainActivity\b', code):
            problems.append('HostWorker.java: marker base must not expose MainActivity')
    main = (JAVA / 'MainActivity.java').read_text(encoding='utf-8')
    for name, host_name in worker_hosts.items():
        if host_name not in main:
            problems.append(f'MainActivity.java: must implement {host_name}')

    return report(
        'check-package-rules: forbidden imports:',
        problems,
        'check-package-rules: app/ui/domain/data boundaries respected')


if __name__ == '__main__':
    sys.exit(main())
