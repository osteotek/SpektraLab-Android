#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
ROOT="$HOME/SpektraLab-apk"
SDK="$HOME/Android/Sdk"
STAGE="$ROOT/app/build/apk"
OUT="$ROOT/app/build/SpektraLab-debug.apk"
TMP="$ROOT/app/build/SpektraLab-complete.apk"
python3 "$ROOT/app/build/check-locales.py"
cp "$ROOT/app/src/main/AndroidManifest.xml" "$STAGE/AndroidManifest.xml"

# The staging tree keeps its own copies of res/ and assets/, and that has now
# cost two releases: a shader that only reached one of them, and a theme that
# stayed black under a paper-coloured app. Sync from source every time.
cp -a "$ROOT/app/src/main/res/." "$STAGE/res/"
cp -a "$ROOT/app/src/main/assets/." "$STAGE/assets/"
cd "$STAGE"
aapt package -f -m -M AndroidManifest.xml -S res -A assets -I "$SDK/platforms/android-36/android.jar" -F "$OUT" >/dev/null
# The DI LUTs are two 7.4 MB tables for a view the app has no path to (the JNI
# never calls spk_export_di / spk_render_digital_intermediate). Dropping them
# saves ~3.3 MB compressed. All ZIP choices live in apk-contents.py.
python3 "$ROOT/app/build/apk-contents.py" pack "$STAGE" "$OUT" \
  --drop-prefix classes.dex \
  --drop-prefix assets/locales/ \
  --drop-prefix assets/resources/DI/ \
  --inject-locales "$ROOT/app/src/main/locales" \
  --dex-dir "$ROOT/app/build/dex" \
  --output "$TMP"
# v1 as well as v2/v3: apksigner skips JAR signing at minSdk 24 by default,
# and a few OEM installers still refuse a v2-only package.
# Release signing is opt-in via env: RELEASE_KEYSTORE / RELEASE_STOREPASS / RELEASE_KEYALIAS.
KEY="${RELEASE_KEYSTORE:-$ROOT/debug.keystore}"
STOREPASS="${RELEASE_STOREPASS:-android}"
ALIAS="${RELEASE_KEYALIAS:-androiddebugkey}"
apksigner sign --ks "$KEY" --ks-pass "pass:$STOREPASS" --key-pass "pass:$STOREPASS" \
  --ks-key-alias "$ALIAS" --v1-signing-enabled true "$TMP"
# A library that cannot load is E-9000 on every device, and it is invisible in
# the build log: the .so is present, its DT_NEEDED just names a file the APK
# does not carry. Check the closure before the file is allowed to leave.
python3 "$ROOT/app/build/check-libs.py" "$STAGE/lib/arm64-v8a"
cp "$TMP" /sdcard/Download/SpektraLab-dev.apk
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('/sdcard/Download/SpektraLab-dev.apk') as z:
    print('APK native entries:')
    for n in z.namelist():
        if n.endswith('.so') or n=='classes.dex':
            print(n,z.getinfo(n).file_size)
PY
