# Build SpektraLab Android on Windows x64

Use PowerShell 7.4+ (`pwsh`) and Git for Windows. No Android Studio, Gradle,
WSL, or administrator access is needed. Allow several GB of free disk space
for downloads and the Android toolchain.

## Java/UI development

From the repository root:

```powershell
.\scripts\setup-windows.ps1
.\scripts\build-windows.ps1 -UseReleaseNative
```

The release APK is cached in `%LOCALAPPDATA%\SpektraLabBuild`, verified against
the SHA-256 in `version.json`, and reused on later builds. To populate or refresh
that cache explicitly (once, while online):

```powershell
.\scripts\cache-release.ps1            # download + verify, or reuse a valid cache
.\scripts\cache-release.ps1 -Force     # re-download and re-verify
.\scripts\cache-release.ps1 -Offline   # fail instead of downloading
```

Review and accept the SDK package license when prompted. If you have already
reviewed the license and want unattended setup, pass `-AcceptAndroidLicenses`.
Both scripts accept `-BuildHome`; the default is
`%LOCALAPPDATA%\SpektraLabBuild`. Setup installs portable tools there without
changing the system `PATH`.
Use `-OutputDirectory` to save the APK somewhere other than `app/build`.

`-UseReleaseNative` verifies the SHA-256 of the published **1.6.2m** APK and
uses its ARM64 libraries and assets together as a matched set. It compiles the
Java, manifest, and Android `res/` files from this checkout and writes
`app/build/SpektraLab-release-native-debug.apk`. Edits to C++, JNI signatures,
shaders, and `app/src/main/assets/` are **not** included in this mode. Use it
for local development, not to publish a new release.

For a build with **no network access**, use `-UsePrebuiltNative` instead:

```powershell
.\scripts\build-windows.ps1 -UsePrebuiltNative
```

The seven ARM64 libraries extracted from that same release APK are tracked in
`app/prebuilt/arm64-v8a/` with SHA-256s in `app/prebuilt/sha256.json`, verified
by `app/build/check-prebuilt.py`. Engine assets are taken from
`app/src/main/assets/resources/` if you have them locally, otherwise from an
already cached release APK (no download is attempted). This mode produces
`SpektraLab-prebuilt-native-debug.apk`.

This is an **Android ARM64** APK (minSdk 24, targetSdk 36), not a Windows app.
To test with an ARM64 Vulkan-capable device and USB debugging enabled:

```powershell
& "$env:LOCALAPPDATA\SpektraLabBuild\sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\SpektraLabBuild\sdk\platform-tools\adb.exe" install -r .\app\build\SpektraLab-release-native-debug.apk
```

The debug key is local. Android will not update an official installation
signed with a different key. To keep an existing install and its data intact,
build with `-UseReleaseNative -SideBySide` and install
`app/build/SpektraLab-release-native-debug-side-by-side.apk` instead. The test
app is labeled **SpektraLab Test**, has its own ID (`app.spektralab.test`),
and does not have Internet permission. If reinstalling local builds with the same version code,
unpacked resources can remain cached; use a fresh test install when changing
shaders or profile assets.

## Full native build

The default build path is the full source build — it needs no external engine
checkout, prebuilt libraries or downloaded release APK:

```powershell
.\scripts\build-windows.ps1
```

It clones/uses upstream `445e314`, applies the now self-contained
`patches/engine-android.patch` (which supplies `engine/src/pipeline/tiling.hpp`
and the JNI APIs), builds the engine with the Android NDK/CMake, and writes
`app/build/SpektraLab-windows-debug.apk` (~19.8 MB). See
[`docs/source-build.md`](source-build.md) for the verification record.

A custom patched engine checkout can still be supplied:

```powershell
.\scripts\build-windows.ps1 -EngineCheckout C:\path\to\matching-engine
```

To hard-gate native changes, run
`scripts/verify.ps1 -StrictEngine -EngineRoot <patched-engine-checkout>`.

## How the repository fits together

- [`MainActivity.java`](../app/src/main/java/app/spektralab/MainActivity.java)
  is the launcher, hand-built UI, and editing workflow. It manages document
  import, frame state, preview jobs, presets, and exports. It copies packaged
  engine resources into app-private storage for native access.
- [`SpektraLabEngine.java`](../app/src/main/java/app/spektralab/engine/SpektraLabEngine.java)
  is the Java façade for the engine and image sessions. Its native methods
  cross into [`app/src/main/cpp`](../app/src/main/cpp): one JNI unit handles
  sessions, rendering and export, and a second handles RAW/LibRaw integration.
- The spectral pipeline lives in the separate upstream **SpektraLab**
  repository at `445e314`. [`engine-android.patch`](../patches/engine-android.patch)
  adapts its Vulkan execution for Android, including buffer and workgroup
  limits and GPU memory management. [Tiling notes](tiling.md) explain the design.
- In the Windows source-build path, the NDK cross-compiles the engine and both JNI units. Upstream
  CMake generates the imprint header and compiles patched GLSL to SPIR-V.
  LibRaw, libjpeg-turbo, and Little CMS are statically linked into the JNI
  library; the Termux build stages separate shared libraries instead.
- The source-build APK stages public upstream engine resources and product profiles,
  with a generated stock catalogue. It does not include the unused DI LUTs.
  Optional local assets under `app/src/main/assets/` can add fonts or profiles;
  the generated shaders take precedence over local copies. The release's
  exact assets and signing key are not reconstructed by this build.
- The scripts use `javac` and `d8` for Java, `aapt2` to package resources,
  `zipalign` for 16 KiB native-library alignment, and `apksigner` to sign and
  verify. The build also checks the native library's ELF dependencies before
  publishing the APK.

## Native dependency check

`app/build/check-libs.py <lib-dir>` verifies every `DT_NEEDED` of each staged
`.so` resolves inside the APK (or is provided by Android). It has two
invocation conventions:

- **Termux:** `readelf` on `PATH`; call
  `python3 app/build/check-libs.py app/build/apk/lib/arm64-v8a`.
- **Windows CMake staging:** `build-windows.ps1` sets `READELF` to the NDK's
  `llvm-readelf.exe` and passes a build-specific `--hint`:
  `python check-libs.py <abi-dir> --hint "..."`.

`READELF` always takes precedence over `readelf` on `PATH`. The failure message
is generic on purpose: the exact rename/rewrite step depends on how the codecs
were built (`build-libs.sh` on Termux, matching CMake SONAMEs on Windows).

## Manual release (scripts/announce.sh)

`scripts/announce.sh` is the **manual release tool**; it is deliberately not
wired into CI. It writes `update.json`, commits and pushes it, purges jsDelivr,
uploads the APK and announcement to the worker's KV (when `CF_TOKEN` is set),
and creates a GitHub Release with `gh` if available:

```bash
CF_TOKEN=xxxx ./scripts/announce.sh 385 1.6.2m path/to/SpektraLab-1.6.2m-arm64-v8a.apk "note" "note"
DRY_RUN=1 ./scripts/announce.sh 385 1.6.2m - "note"   # print the plan only
```

The version arguments are checked against `version.json` through
`app/build/check-version.py --identity` before anything is written, so a typo
cannot publish an `update.json` that disagrees with the built APK. The script
never stores the token in the repository.

## CI release signing

`.github/workflows/verify.yml` has a `release` job that builds the app from
source and uploads `SpektraLab-release-signed.apk` on every push (and on
same-repository PRs). It needs these repository secrets:

| Secret | Purpose |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | base64 of the release JKS (`[Convert]::ToBase64String([IO.File]::ReadAllBytes($ks))`) |
| `RELEASE_STOREPASS` | keystore / key password |
| `RELEASE_KEYALIAS` | key alias inside the keystore |
| `EXPECTED_SIGNER_SHA256` | SHA-256 digest from `apksigner verify --print-certs`; the build fails if the signer differs |

Fork PRs skip the job because GitHub does not expose secrets to them. The
offline verification job is independent and still runs on every trigger.

## Full build with Fotufilm

The default and `-UsePrebuiltNative` builds ship only the first engine; the
app therefore reports "fotufilm-native 未加载" in About. Fotufilm's native
libraries and ~46 MB asset tree are not in this repository (the host depends
on third-party Halide AOT artefacts that are not redistributed). The published
v1.7.0 APK is the binary source for the optional full variant:

```powershell
.\scripts\build-windows.ps1 -WithFotufilm -UsePrebuiltNative -SideBySide
# -> app/build/SpektraLab-prebuilt-native-debug-with-fotufilm-side-by-side.apk
```

`-WithFotufilm` can be combined with any native-source choice. It verifies the
SHA-256 in `app/build/fotufilm-source.json`, downloads the v1.7.0 APK if not
cached, and stages four libraries (`libfotufilm_jni.so`, `libfotufilmhost.so`,
`libfotufilmcodecs.so`, `libc++_shared.so`) plus `assets/fotufilm/**` via
`app/build/stage-fotufilm.py`. The APK is ~26 MB, so the size guard is 48 MB
for this variant (20 MB otherwise). Verified on the emulator: About reports
"fotufilm-native 1.0 · 47 支胶片 · 14 种媒介", the engine chooser appears for
twin films, and golden bands 0/128/512 remain `e60490499b9a94eb`.
