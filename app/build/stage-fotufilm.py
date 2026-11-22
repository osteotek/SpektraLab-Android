#!/usr/bin/env python3
"""Stage the second engine from the published 1.7.0 APK into an APK staging dir.

The Fotufilm native libraries and its ~46 MB asset tree are not in this
repository: the host library depends on third-party Halide AOT artefacts that
are not redistributed here. The official v1.7.0 APK is therefore the binary
source for the optional "full" build. Only the entries actually needed are
copied; the caller verifies the APK SHA-256 before invoking this script.

Usage:
    python app/build/stage-fotufilm.py <source-apk> <stage-dir>
"""
import sys
import zipfile
from pathlib import Path

LIBS = {
    "libfotufilm_jni.so",
    "libfotufilmhost.so",
    "libfotufilmcodecs.so",
    # The Fotufilm host/codecs link against libc++ dynamically; the main
    # engine is statically linked and standard builds omit it.
    "libc++_shared.so",
}


def main():
    if len(sys.argv) != 3:
        print(__doc__, file=sys.stderr)
        return 2
    apk = Path(sys.argv[1])
    stage = Path(sys.argv[2])
    libdir = stage / "lib" / "arm64-v8a"
    assets = stage / "assets"
    libdir.mkdir(parents=True, exist_ok=True)
    assets.mkdir(parents=True, exist_ok=True)
    n_libs = n_assets = 0
    with zipfile.ZipFile(apk) as z:
        for info in z.infolist():
            name = info.filename
            if name.startswith("lib/arm64-v8a/") and Path(name).name in LIBS:
                target = libdir / Path(name).name
                target.write_bytes(z.read(info))
                n_libs += 1
            elif name.startswith("assets/fotufilm/"):
                target = assets / name[len("assets/"):]
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(z.read(info))
                n_assets += 1
    if n_libs != len(LIBS):
        print(f"stage-fotufilm: expected {len(LIBS)} libs, found {n_libs}",
              file=sys.stderr)
        return 1
    if n_assets == 0:
        print("stage-fotufilm: no assets/fotufilm entries", file=sys.stderr)
        return 1
    print(f"stage-fotufilm: {n_libs} libraries, {n_assets} asset files")
    return 0


if __name__ == "__main__":
    sys.exit(main())
