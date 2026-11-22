# Plan: merge `SAINTTaiYi/SpektraLab-Android` upstream into `feat/windows-debug-builds`

**Status: Option A execution in progress** on branch `feat/adopt-upstream-170`.
Research used a throwaway worktree for a dry-run merge; it and its temporary
branch were removed afterwards.

### Gate A decisions (2026-11-22)

- **Version line:** adopt upstream `1.7.0i / 752` everywhere. `version.json` is
  updated to match; `check-version.py` must stay green.
- **Native payload:** build from source. Upstream tracks no prebuilt payload and
  the 1.6.2m `.so` set cannot serve the 1.7.0 JNI/FotuFilm surface. The new
  payload will be cached under `app/build/` only; `app/prebuilt/` is refreshed
  only after the device gate passes.
- **Feature scope:** full 1.7.0 product line, with our architecture (domain/data/
  ui boundaries, workers, verification harness) re-ported on top.

### Stage 1 outcome (2026-11-22)

- Merged `upstream/master` (`9709412`) into `feat/adopt-upstream-170`. All six
  conflicts resolved: Manifest/JNI/SpkError/README combined additively;
  `MainActivity` and `patches/engine-android.patch` taken from upstream
  (1.7.0 product baseline).
- Re-ported minimum compatibility: `DebugSink.bytes` no-op shim, `EngineApi`
  switched to upstream's pointer-based `openSession(long,…)`, 391 inherited
  silent catches converted to `Diagnostics.ignored`, version identity aligned
  to `1.7.0i / 752`.
- Our six worker classes and `HostWorker` are **temporarily set aside** (backed
  up under `%TEMP%\spk-workers-stage1`, recoverable from `9990a8a`): upstream's
  MainActivity has private nested `Look`/`Frame`/`CompareView` and a different
  API surface, so they need re-porting rather than a visibility tweak.
- Verified: `scripts/verify.ps1` all PASS; NDK/CMake source build succeeds with
  upstream's patch, LibRaw 0.22.2, `contrast_mask.cpp`, `lrglow.cpp` and
  `ultrahdr.cpp` → signed 19.8 MB APK. App installs and launches on the
  emulator (first-run feedback sheet shown, no crash).
- **Gate 5 device golden is blocked:** upstream removed our `spk_band_rows` /
  `spk_deterministic` / `spk_auto_film` intent hooks and the `SpektraLabHash`
  log line, so `device-golden.ps1` cannot read a hash. Re-porting those hooks
  into the 1.7.0 render path is Stage 2 work.
- `-UsePrebuiltNative` now ships a stale 1.6.2m payload and must not be used
  for 1.7.0 builds until the new payload is captured and verified.

### Stage 2 progress (2026-11-22)

- Re-ported the device test hooks into upstream's MainActivity: `spk_band_rows`
  / `spk_deterministic` / `spk_auto_film` intent extras, ACTION_VIEW image
  import, `SPK_BAND_ROWS` env override, auto-film selection after import, and
  a `SpektraLabHash` logcat line beside the existing preview hash.
- **New 1.7.0 golden baseline: `e60490499b9a94eb`** for bands 0/128/512 —
  banding invariance holds with the merged engine and FotuFilm build.
- `device-smoke.ps1` passes; app installs, launches and renders on the emulator.
- **Workers re-ported (compiling):** the six worker classes and `HostWorker`
  are back in the tree and build against the 1.7.0 API. The adaptation included
  widening ~160 Activity members and five nested types, adding compatibility
  members (`closeSessionSafely`, `reopeningFrame`, `exportController`,
  `exportStore`, `renderController`), adapting changed call signatures, a
  `nativeBufferAddress` JNI shim plus `allocDirect`/`freeDirect` and a
  `ByteBuffer` `openSession` overload in `SpektraLabEngine`, and localising the
  worker's pixel buffer (1.7.0 uses a native pointer rather than a ByteBuffer).
- Device golden remains `e60490499b9a94eb` for bands 0/128/512 after the
  worker re-port; smoke test passes.
- **Not yet wired:** upstream's Activity still uses its own inline
  implementations, so the workers compile but are not called. Replacing those
  inline paths is the next Stage 2 slice and must preserve the golden baseline.

## 1. Verified starting state (2026-11-22)

| Item | State |
|---|---|
| Our working tree | clean, `feat/windows-debug-builds` @ `9990a8a`, pushed and in sync with `origin` |
| New remote | `upstream` → `https://github.com/SAINTTaiYi/SpektraLab-Android` (added, fetched with `--prune`) |
| Upstream heads | only `master` @ `9709412`; tags through `v1.7.0` |
| Merge base | `b17f6d2` (our local `master`, the 1.6.2m README commit) |
| Divergence | **ours 75 commits ahead**, **upstream 11 commits ahead** |
| Product identity | ours `1.6.2m` / code `385` (with tracked `version.json`); upstream `1.7.0i` / `752` in the manifest, `1.7.0` / `743` in `update.json` |

Upstream's 11 commits are the 1.7.0 line: second engine, many new UI modes and
bug fixes. Our 75 commits are **architecture, tests and build tooling only** —
no new product features. This is a fork of two different product generations,
not a routine sync.

## 2. What upstream changed

`git diff --stat b17f6d2..upstream/master`: **94 files, +79,268 / −12,298**.

- **Second engine (FotuFilm):** ~40 new C++ files under
  `app/src/main/cpp/fotufilm/**` (`host.cpp` ~4k lines, `overscan.cpp` ~3k,
  `rebate.cpp` ~2.6k, `film_grain.hpp` ~850, plus `lrglow`, `ultrahdr`,
  `fotufilm_jni.cpp`, `fotufilm_codecs.h`) and new build scripts
  `build-fotu.sh`, `build-fotu-from-source.sh`.
- **New Java subsystems:** `LightTableCanvas`, `LightTableWeb`, `Loaders`,
  `MotionPhoto`, `NegativeDesk` (~2.6k lines), `PictureCheck`, `SelfHeal`,
  and `engine/{FotuFilm,JpegExif,TiffExif}`.
- **Native/JNI:** `spektralab_jni.cpp` +1005/−? (new `nativeRenderToBuffer`,
  `nativeGetLastRenderInfo`, engine dispatch), `spektralab_raw_jni.cpp`,
  `view_adjust.hpp` +377.
- **Resource/config:** `AndroidManifest.xml` (VIBRATE, network security config,
  `allowBackup=false`, version bump), `res/xml/network_security_config.xml`,
  `drawable-nodpi/group_qr_2.png`, `update.json`, README.
- **Docs/tools:** `AGENTS.md`, `CLAUDE.md`, `AI-NOTICE.md`, `.ai/notice.*`,
  `docs/{CODE-REVIEW-2026-11-21,REGRESSION,REPRODUCING}.md`, `scripts/audit.py`,
  `tools/regress.sh`, `tools/assets/gen_*.py`.
- **Engine patch:** `patches/engine-android.patch` +10,226 lines (1.7.0 engine).

Upstream has **no `app/prebuilt/` payload** — that is our own addition; upstream
builds natively. Our branch ships the tracked 1.6.2m `.so` set and uses it for
fast Java/UI builds.

## 3. Dry-run merge result

`git merge --no-commit --no-ff upstream/master` in a worktree at our HEAD produced
exactly **six conflicts**; all other files (including every new FotuFilm and Java
subsystem file) merged cleanly.

| File | Hunks | Lines | Nature / guidance |
|---|---:|---:|---|
| `app/src/main/java/app/spektralab/MainActivity.java` | **138** | 36,732 | Base 15,314 → ours 10,225 (refactored) → upstream 36,490. **Do not resolve textually** — see §5. |
| `patches/engine-android.patch` | **99** | 21,677 | Our 113-section 1.6.2m+engine patch vs upstream's 1.7.0 patch. **Do not resolve textually** — choose a base (§5). |
| `app/src/main/cpp/spektralab_jni.cpp` | 6 | 2,627 | Additive (new JNI entry points with empty HEAD side). Keep both. |
| `README.md` | 4 | 255 | Merge upstream product docs with our build/verification docs; align the version line. |
| `app/src/main/AndroidManifest.xml` | 2 | 73 | Additive: keep our `READ_MEDIA_IMAGES` **and** upstream's `VIBRATE`; take `networkSecurityConfig` + `allowBackup="false"`. |
| `app/src/main/java/app/spektralab/SpkError.java` | 1 | 189 | Take upstream's additions while preserving our error taxonomy. |

Non-conflict caveat: git auto-merged the manifest version to `752 / 1.7.0i`, but
`version.json`, `update.json` and README still say `385 / 1.6.2m`.
`app/build/check-version.py` enforces all four, so the merge **cannot pass
verification** until a product identity is chosen (§4).

## 4. The decision that gates everything: product identity and payload

This is not a code-only merge. Three sub-decisions must be made before any
conflict is resolved:

1. **Version line.** Either adopt `1.7.0i / 752` everywhere (`version.json`,
   `update.json`, README, manifest, build script) or keep `1.6.2m / 385` and
   reject upstream's version hunks. Mixing them fails `check-version.py`.
2. **Native payload.** If we ship 1.7.0 UI/engine code, the 1.6.2m prebuilt
   `.so` set is wrong — it lacks FotuFilm and the new JNI surface. Options:
   build from source (we already have a working NDK/CMake path, but P0.1's
   source-built APK currently fails three wire parameters), or obtain a 1.7.0
   release payload and track it in `app/prebuilt/`. Either way the golden
   baseline `e0517f9ff1532b87` is no longer valid and must be re-recorded.
3. **Feature scope.** Do we intend to ship the second engine and new UI modes,
   or only take upstream's bug fixes into our 1.6.2m line?

**Gate A:** these three answers are written down. Do not start Phase 1 without them.

## 5. Strategic options

### Option A — Full adoption, re-port our architecture (recommended if 1.7.0 is the product)
Merge upstream as a new baseline, then re-apply our 75 commits' architecture on
top of upstream's code: module boundaries (`domain/`, `data/`, `ui/`), workers,
stateless panel factories, verification harness, package-rule checker, signing,
device tests.
- **Pros:** one product line; all 1.7.0 features; upstream remains the easy source of future updates.
- **Cons:** very large. `MainActivity` must be refactored again from ~36k lines;
  every extracted class needs re-wiring; the patch must be re-based; golden
  baseline resets. Effectively a multi-PR program, not a single merge.
- **Mitigation:** do it on a new branch (`feat/adopt-upstream-170`) from upstream,
  keep `feat/windows-debug-builds` stable until each gate passes.

### Option B — Selective backport into our 1.6.2m line (recommended if stability matters)
Keep our branch as-is; cherry-pick only upstream fixes that map cleanly onto our
refactored code (import OOM/`12 B/px` staging, decimal-comma RAW import, engine
teardown abort, rebate self-heal, `onTrimMemory`, small view-haptics/permission
additions), adapting each to our extracted classes.
- **Pros:** preserves our architecture, tests, golden hash and release identity;
  small, reviewable changes; no second-engine payload work.
- **Cons:** does not deliver 1.7.0 features; recurring manual work as upstream moves.

### Option C — Mechanical merge then reconcile (not recommended)
Resolve all six conflicts in the working tree (taking upstream for `MainActivity`
and the patch), then try to restore our refactors. Highest risk: 138 conflict
hunks in a 36k-line file plus a 21k-line patch, with no clean checkpoint where
tests pass.

**Recommendation:** choose **B now** for near-term stability and open a **new
branch for A** to evaluate 1.7.0 behind the gates in §7. Do not attempt C.

## 6. Execution plan (per recommended option)

### Phase 0 — decide and record (Gate A)
- Answer §4 in `docs/upstream-android-merge-plan.md` (this file) and link it from
  `docs/improvement-plan.md`.
- Record rollback points: ours `9990a8a`, upstream `9709412`, base `b17f6d2`,
  engine fork `445e314e`.

### Phase 1 — land the non-conflicting upstream additions
- On the chosen integration branch, merge or cherry-pick upstream with
  `--no-commit`; take **all new files** (FotuFilm tree, new Java subsystems,
  build scripts, docs, tools) while keeping our versions of the six conflicts.
- Easy conflict resolutions: Manifest (keep both permissions, take network
  security config), `spektralab_jni.cpp` (keep both JNI sets), `SpkError.java`
  (merge taxonomy), `README.md` (merge docs, align version).
- `MainActivity` and the engine patch stay staged but unresolved until Phase 2/3.

### Phase 2 — `MainActivity` reconciliation (Option A only)
- Do not text-accept either side. Create a feature inventory from upstream's
  `MainActivity` diff (new engine dispatch, landscape two-column UI, negative
  desk, motion photo, self-heal, import/export fixes).
- Port features one at a time into **our** 10k-line Activity and its extracted
  `ui/`, `domain/`, `data/` and worker classes; each port gets its own commit and
  its own `verify.ps1` pass.
- Keep the package-rule checker green: no new Android dependencies in `domain/`.

### Phase 3 — engine patch reconciliation (both options)
- **Option A:** adopt upstream's 1.7.0 patch as the base; re-apply only the
  Android-specific additions from our 113-section patch that upstream still
  lacks (verify with `check-patch.py` against the appropriate upstream engine
  checkout). Delete our stale repair-only sections rather than merging text.
- **Option B:** leave our patch untouched; upstream's patch changes are out of
  scope.
- Note the open risk from the earlier merge: the source-built 1.6.2m engine
  rejects `halation_additive`, `halation_scatter_scale`, `hue_correction`
  (see `docs/repair-merge-plan.md` §8). Re-check those names against upstream's
  params table before claiming any source-built equivalence.

### Phase 4 — identity, payload and build convergence
- Align `version.json`, `update.json`, README, manifest and `build-windows.ps1`
  to the chosen identity; `check-version.py` must exit 0.
- Provide the matching native payload (source build or a verified 1.7.0 release
  APK cached via `scripts/cache-release.ps1`); update `app/prebuilt/sha256.json`
  and `check-prebuilt.py` expectations together.
- Reconcile build scripts: ours (`scripts/verify.ps1`, `common.ps1`,
  `device-common.ps1`, APK size guard) with upstream's `build-fotu*.sh`,
  `scripts/audit.py`, `tools/regress.sh`.

### Phase 5 — verification gates
- `.\scripts\verify.ps1` and `.\scripts\verify.ps1 -EngineRoot <clean checkout>` — all PASS.
- `check-patch.py` against the chosen engine base; full NDK/CMake build.
- APK build in both modes; size within the budget in `docs/modernization-plan.md` §5.
- Device: `device-golden.ps1 -Bands 0,128,512` against a **new** recorded baseline
  (the old `e0517f9ff1532b87` only applies to the 1.6.2m prebuilt payload);
  `device-smoke.ps1`; UI export smoke test; export metadata comparison.
- `git grep '^<<<<<<<'` must be empty.

### Phase 6 — land
- Commit with a message naming both refs (`9709412`, `9990a8a`) and the chosen
  product identity; push the integration branch first, then fast-forward or
  merge `feat/windows-debug-builds` only after §5 passes.
- Update `docs/improvement-plan.md` and `docs/android-skill-adoption.md` with the
  new baseline and any architecture decisions.

## 7. Risks and mitigations

| Risk | Mitigation |
|---|---|
| 138-hunk `MainActivity` and 99-hunk patch | Option B avoids them; Option A ports features instead of text-merging (§6 Phase 2/3). |
| Version/payload mismatch breaks `check-version.py` and all golden tests | Gate A before edits; align all four identity files and re-record the baseline. |
| Upstream source-built engine already has a wire mismatch on our Java | Re-check the three rejected parameter names; keep prebuilt path stable until fixed. |
| Upstream has no prebuilt payload; source build requires FotuFilm + new codecs | Verify NDK/CMake build and cache the resulting payload with checksums before switching. |
| Upstream docs (`AGENTS.md`, `CLAUDE.md`, `AI-NOTICE.md`) may encode different contributor rules | Review and decide whether to keep; do not silently inherit. |
| Our verification/CI is not in upstream | Keep `scripts/verify.ps1` and CI as the single source of truth; extend, don't duplicate. |

## 8. Open questions

1. Are we shipping 1.7.0 (second engine) or staying on 1.6.2m with backports?
2. If 1.7.0: do we track a new prebuilt payload, or require source builds for everyone?
3. Should upstream's `AGENTS.md`/`CLAUDE.md`/`AI-NOTICE.md` be adopted, adapted or dropped?
4. Should the P0.1 blocker be closed before or after this merge? (It is currently
   open: the source-built 1.6.2m APK fails at runtime.)
