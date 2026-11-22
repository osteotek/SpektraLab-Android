#!/usr/bin/env python3
"""Shared scaffolding for the offline checkers in this directory.

Every checker needs the same two things: the repository root (so it can be
run from any working directory -- including the temporary fixture trees used
by test_checkers.py) and a consistent way to print findings. Keeping them here
stops the "single source of truth" scripts from drifting apart.
"""
import sys
from pathlib import Path


def repo_root():
    """Repository root, derived from this file's location."""
    return Path(__file__).resolve().parents[2]


def report(title, problems, success_line, *, prefix='  - ', stream=None, hint=None):
    """Print a checker's findings and return its exit code.

    ``stream`` is for diagnostics that belong on stderr (check-libs.py).
    ``prefix`` preserves each checker's historical problem-line formatting.
    ``hint`` is an optional trailing remediation line.
    """
    if problems:
        out = stream if stream is not None else sys.stdout
        print(title, file=out)
        for problem in problems:
            print(prefix + problem, file=out)
        if hint:
            print('  ' + hint, file=out)
        return 1
    print(success_line)
    return 0
