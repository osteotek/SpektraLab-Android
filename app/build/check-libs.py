#!/usr/bin/env python3
"""Every DT_NEEDED of every shipped .so must resolve inside the APK.

`libjpeg.so.8` and `libz.so.1` cannot travel under their own names -- the
package installer only extracts entries under lib/ that end in `.so`, so they
are shipped as `libjpeg_8.so` / `libz_1.so` and every DT_NEEDED that names them
is rewritten in place (build-libs.sh does this in the Termux build; the
Windows CMake path builds the codecs with matching SONAMEs instead).

This runs after staging, so the mistake is a build failure instead of a
release. Exit code 1 means the APK would not have loaded.

readelf is taken from $READELF when set (build-windows.ps1 passes the NDK's
llvm-readelf), otherwise from readelf on PATH (Termux). Pass --hint to replace
the generic remediation line with build-specific guidance.
"""
import argparse
import os
import pathlib
import subprocess
import sys

# The embedded Windows Python does not add the script's directory to sys.path.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from check_common import report


# What Android itself provides. Anything else has to be in lib/.
PLATFORM = {
    "libc.so", "libm.so", "libdl.so", "liblog.so", "libandroid.so",
    "libjnigraphics.so", "libvulkan.so", "libEGL.so", "libGLESv3.so",
    "libGLESv2.so", "libz.so", "libstdc++.so",
}
DEFAULT_HINT = ("rename versioned SONAMEs and rewrite their DT_NEEDED entries "
                "so every dependency resolves inside lib/")


def needed(path):
    out = subprocess.run([os.environ.get("READELF", "readelf"), "-d", str(path)],
                         capture_output=True, text=True, check=True).stdout
    return [line.split("[")[1].split("]")[0]
            for line in out.splitlines() if "(NEEDED)" in line]


def main(directory, hint=None):
    libs = {p.name: p for p in sorted(pathlib.Path(directory).glob("*.so"))}
    if not libs:
        print(f"check-libs: no .so under {directory}", file=sys.stderr)
        return 1
    broken = {}
    for name, path in libs.items():
        missing = [n for n in needed(path)
                   if n not in libs and n not in PLATFORM]
        if missing:
            broken[name] = missing
    problems = [f"{name} needs {', '.join(missing)}"
                for name, missing in broken.items()]
    return report(
        "check-libs: these libraries cannot load as packaged:",
        problems,
        f"check-libs: {len(libs)} libraries, every DT_NEEDED resolves",
        stream=sys.stderr,
        hint=hint or DEFAULT_HINT)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', nargs='?',
                        default='app/build/apk/lib/arm64-v8a')
    parser.add_argument('--hint', help='build-specific remediation line')
    args = parser.parse_args()
    sys.exit(main(args.directory, args.hint))
