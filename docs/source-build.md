# Source-build inputs

**Status (2026-11-22): the app builds fully from source.** No prebuilt native
payload, downloaded release APK or cached assets are required.

## Build from source

```powershell
scripts/setup-windows.ps1                       # once: JDK, NDK, CMake, LibRaw, libjpeg, glslang, upstream mirror
scripts/build-windows.ps1                        # native path (default; no flags)
```

With no native-source flag the script clones/uses upstream
`445e314e28b9b455d2457471fadaffd28c07270b`, applies
`patches/engine-android.patch`, builds `libjpeg-turbo` and
`libspektrafilm_engine.so` with the Android NDK + CMake/Ninja against
**LibRaw 0.22.2**, and stages resources, profiles and the 48 compiled Vulkan
shaders from the native build tree. The resulting
`app/build/SpektraLab-windows-debug.apk` is ~19.8 MB (under the 20 MB guardrail).

Two alternate paths remain for Java/UI work only — neither recompiles C++:

- `-UsePrebuiltNative` — the seven ARM64 libraries tracked in
  `app/prebuilt/arm64-v8a/` plus `app/prebuilt/sha256.json`.
- `-UseReleaseNative` — downloads/caches the published release APK and reuses
  its native libraries and assets.

## Verification

A source-built APK was installed on `emulator-5554` and checked with
`scripts/device-golden.ps1 -Bands 0,128,512`. All three band hashes were
**`e60490499b9a94eb`**, identical to the release and prebuilt builds. Binary
identity across toolchains is not expected; pixel equivalence is the gate.

The engine patch is self-contained now:

```
scripts/verify.ps1 -StrictEngine -EngineRoot <patched-engine-checkout>
  PASS  check-patch
  PASS  spektralab_jni.cpp compile
```

`check-patch.py` resolves every added `#include` (including
`engine/src/pipeline/tiling.hpp`) and checks every `spk_*` JNI call against the
patched public header. The compile-only shim in
`app/build/native-tests/jni-shim.hpp` declares only APIs that the supplied
engine header actually lacks (`verify.ps1` defines a macro per missing API), so
a complete tree produces no duplicate declarations.

Plain `scripts/verify.ps1` keeps the engine checks diagnostic when no
`-EngineRoot` is given; use `-StrictEngine` as the hard gate for native work.

## History

The earlier blocker — a missing `tiling.hpp`, ten undeclared `spk_*` APIs and
private `spk::io` APIs — was closed by the work recorded in
[`docs/source-build-repair-plan.md`](source-build-repair-plan.md). The public
SpektraLab engine tree at `445e314` is GPL-3.0-or-later; film profiles and LUTs
are CC BY-SA 4.0 (see `AI-NOTICE.md`). All notices remain in the APK and repo.
