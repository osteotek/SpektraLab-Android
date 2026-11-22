#!/usr/bin/env python3
"""Stage release assets/libs or combine AAPT resources with new dex and ELF entries.

`pack` is shared by the Windows build (scripts/build-windows.ps1) and the
Termux build (build-apk.sh). The Termux path drops AAPT's placeholder dex and
locale catalog, skips the unused DI LUTs, and injects this checkout's locales;
those choices are explicit options here instead of a second ZIP implementation.
"""
import argparse
import datetime
from pathlib import Path, PurePosixPath
import shutil
import zipfile


def extract(apk, stage):
    with zipfile.ZipFile(apk) as source:
        for entry in source.infolist():
            path = PurePosixPath(entry.filename)
            if entry.is_dir() or not path.parts or path.is_absolute() or '..' in path.parts:
                continue
            if path.parts[:2] == ('assets', 'locales'):
                continue  # The catalog must match this checkout's Java, not the donor APK.
            if path.parts[0] == 'assets' or (
                len(path.parts) == 3 and path.parts[:2] == ('lib', 'arm64-v8a')
                and path.suffix == '.so'
            ):
                source.extract(entry, stage)
    for name in ('assets/resources/spektrafilm_constants.bin',
                 'lib/arm64-v8a/libspektrafilm_engine.so'):
        if not (stage / name).is_file():
            raise SystemExit(f'Release APK is missing {name}')


def extract_assets(apk, stage):
    """Copy only assets/ from a release APK (used by -UsePrebuiltNative)."""
    with zipfile.ZipFile(apk) as source:
        for entry in source.infolist():
            path = PurePosixPath(entry.filename)
            if entry.is_dir() or path.parts[0] != 'assets' or '..' in path.parts:
                continue
            if path.parts[:2] == ('assets', 'locales'):
                continue  # catalogs must match this checkout's Java
            source.extract(entry, stage)


def pack(apk, stage, *, drop_prefixes=(), inject_locales=None,
         dex_directory=None, output=None):
    dex_dir = Path(dex_directory) if dex_directory else stage / 'dex'
    if not (dex_dir / 'classes.dex').is_file():
        raise SystemExit('No classes.dex to package')
    target_path = Path(output) if output else stage / 'unsigned.apk'
    # Regenerate ZIP headers before zipalign. Appending directly to the AAPT
    # ZIP leaves local and central-directory headers that zipalign warns about.
    skipped = {}
    with zipfile.ZipFile(apk) as source, zipfile.ZipFile(target_path, 'w') as target:
        for entry in source.infolist():
            name = entry.filename
            if any(name.startswith(prefix) for prefix in drop_prefixes):
                skipped[name] = skipped.get(name, 0) + 1
                continue
            target_info = zipfile.ZipInfo(name, entry.date_time)
            target_info.compress_type = entry.compress_type
            with source.open(entry) as data, target.open(target_info, 'w') as out:
                shutil.copyfileobj(data, out)

        def add_deflated(path, name):
            # The published APK deflates its native libraries (compress_type 8).
            # zipfile.write() defaults to ZIP_STORED, which made local builds
            # 27 MB instead of 15.9 MB. Keep the original timestamp too.
            info = zipfile.ZipInfo(name, datetime.datetime.fromtimestamp(
                path.stat().st_mtime).timetuple()[:6])
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            with path.open('rb') as data, target.open(info, 'w') as out:
                shutil.copyfileobj(data, out)

        if inject_locales:
            for path in sorted(Path(inject_locales).glob('*.json')):
                add_deflated(path, 'assets/locales/' + path.name)
        for dex in sorted(dex_dir.glob('classes*.dex')):
            add_deflated(dex, dex.name)
        for lib in sorted((stage / 'lib/arm64-v8a').glob('*.so')):
            add_deflated(lib, lib.relative_to(stage).as_posix())

    for prefix in dict.fromkeys(drop_prefixes):
        count = sum(value for name, value in skipped.items()
                    if name.startswith(prefix))
        if count:
            print(f'pack: skipped {count} source entries under {prefix!r}')


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=('extract', 'assets', 'pack'))
    parser.add_argument('directory', type=Path, help='staging directory')
    parser.add_argument('apk', type=Path, help='source APK')
    parser.add_argument('--drop-prefix', action='append', default=[], metavar='PREFIX',
                        help='omit source entries starting with PREFIX (repeatable)')
    parser.add_argument('--inject-locales', type=Path, metavar='DIR',
                        help='add *.json from DIR as assets/locales/ entries')
    parser.add_argument('--dex-dir', type=Path, metavar='DIR',
                        help='classes*.dex directory (default: <stage>/dex)')
    parser.add_argument('--output', type=Path, metavar='PATH',
                        help='output APK (default: <stage>/unsigned.apk)')
    args = parser.parse_args(argv)
    if args.action == 'extract':
        extract(args.apk, args.directory)
    elif args.action == 'assets':
        extract_assets(args.apk, args.directory)
    else:
        pack(args.apk, args.directory,
             drop_prefixes=args.drop_prefix,
             inject_locales=args.inject_locales,
             dex_directory=args.dex_dir,
             output=args.output)
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
