# Plan: merge `feat/source-build-repair` and upstream engine work into `feat/windows-debug-builds`

**Status: plan only.** No branch, patch or build file is changed by this document.
Research was done with a throwaway worktree and a dry-run merge; that worktree
and its temporary branch were removed afterwards.

## 1. Verified starting state (2026-11-22)

| Item | State |
|---|---|
| Current branch | `feat/windows-debug-builds` @ `fb99920`, clean, pushed, in sync with `origin` |
| Repair branch | `feat/source-build-repair` @ `ba0a878`, worktree `C:\Users\User\Developer\SpektraLab-Android-repair`, clean |
| Merge base | `61f095f` ("Extract photo-import worker into ImportWorker") |
| Since base | repair: 10 commits; current: 4 commits (`74a6b0f`, `009a065`, `f133b64`, `fb99920`) |
| Our `origin` | no remote commits beyond local; nothing to pull |
| Engine upstream | `C:\Users\User\AppData\Local\SpektraLabBuild\upstream`, fetched 2026-11-22 05:53, HEAD detached at fork point `445e314e` |
| Engine refs | `Windows_Linux_v2` @ `f87c5212` (678 commits since fork); `main`, `main_v1`, `Windows_Linux` are not on our lineage |

## 2. What the repair branch actually changes

`git diff --stat 61f095f..feat/source-build-repair` (6 files):

| File | Change |
|---|---|
| `patches/engine-android.patch` | **4,140 → 11,747 lines** (+7,955/−192); 113 sections. Reconstructed `tiling.hpp`, reimplemented 10 missing `spk_*` C ABI APIs, added private `spk::io` dependencies (`RawDecodeCache`, `write_dng`, `read_dng_display_profile`, capture fields), merged upstream engine commits and upgraded LibRaw to master `42f89c16`. |
| `docs/upstream-merge-plan.md` | **new** (223 lines): upstream triage, 6-phase merge strategy, risk table. |
| `docs/source-build-repair-plan.md` | +27 lines: NDK build finding for `spk::io`, resolution notes, link to upstream plan. |
| `docs/source-build.md` | −9 lines: removes the `-StrictEngine` wording (see §4). |
| `app/build/check-patch.py` | +11/−? : resolves includes through patch-added/vendored paths (needed by the bigger patch); temporarily drops `check_common` integration. |
| `scripts/build-windows.ps1` | +14/−?: LibRaw `0.22.2` → pinned master `42f89c16`, stops applying the obsolete HE-sniff/camera-table patches, re-adds an explicit `check-prebuilt` exit-code check. |

Repair commits, in order: `19be24d` → `ed3b7a2` → `24f9f63` → `9b0a891` → `1f21af1` → `265c2ac` → `5a5761b` → `1b4005b` → `2edc17e` → `ba0a878`.

## 3. What upstream contributed (already folded into the patch)

`Windows_Linux_v2` is 678 commits ahead of `445e314`; ~26 touch `engine/`. The
repair branch's `docs/upstream-merge-plan.md` triages them: low-risk takes
(`7766b847`, `dc440a34`, `0cc25987`, `53299f73`, `dcdab292`), deferred
wire/semantic changes (`2ca735ad` exposure baseline, `61f79d2b` no-film
precision, `3fbd9bd3` memory peaks), RAW/LibRaw families (`8cd9992c`,
`b5467e83`, `61aee26d`, `c59d0c2a`) and desktop/docs commits.

All were cherry-picked into the patch during the repair work. Resources
(`engine/resources`, `engine/resources_product`) are unchanged between
`445e314` and `f87c5212`, so film/paper data is unaffected. **Do not merge from
`main`** — it has no common ancestor, a moved header and a 2.4.x ABI.

## 4. Dry-run merge result (verified, not committed)

A temporary worktree based on `fb99920` was merged with
`git merge --no-commit --no-ff feat/source-build-repair`:

- **Files changed by both sides:** `app/build/check-patch.py`,
  `docs/source-build-repair-plan.md`, `docs/source-build.md`,
  `scripts/build-windows.ps1`.
- **Only textual conflict:** `docs/source-build-repair-plan.md` (add/add, two
  hunks). Both hunks have an **empty HEAD side** and a populated repair side →
  resolve by taking the repair version of both hunks.
- **Auto-merges are semantically correct** — verified by reading the merged files:
  - `check-patch.py`: keeps our `check_common` import/`report(...)` integration
    **and** repair's better include resolution.
  - `build-windows.ps1`: keeps our `common.ps1` helpers, `Resolve-RepoRoot`,
    `Get-Jdk17`, `Read-Version`, the `--hint` for `check-libs.py` and the
    "no ad-hoc `$LASTEXITCODE`" convention, **and** takes repair's LibRaw
    `42f89c16` pin.
  - `source-build.md`: keeps our `-StrictEngine` documentation and repair's
    wording; no action needed.

Validation performed in that dry-run tree:

- `python app/build/check-patch.py <clean 445e314 checkout>` → **PASS**,
  `113 patched files, 31 JNI spk_* calls all declared`.
- `scripts/verify.ps1 -EngineRoot <that checkout>` → **all PASS**, including
  `check-patch`, the shimmed JNI compile, locales, version, prebuilt payload,
  both JVM suites, javac and the C++ compile checks. Native *run* steps remain
  SKIP on this Windows host (CI/ubuntu runs them with g++).

## 5. Risks and gates

1. **Phase 0 was skipped.** `docs/upstream-merge-plan.md` records that the
   operator did not capture a physical-device pixel/export baseline before the
   engine merge. No claim of pixel equivalence for the *source-built* engine is
   valid yet. **Gate:** record band 512/128 hashes and 12 MP JPEG/DNG metadata
   on a physical ARM64 device for the source-built APK before shipping it.
2. **The normal build is unaffected.** The patch does not change
   `app/prebuilt/`, so `-UsePrebuiltNative` builds still use the shipped 1.6.2m
   payload; golden bands 0/128/512 remain `e0517f9ff1532b87`. This is the
   regression oracle for the merge itself.
3. **APK size mode changes.** Repair's offline note reports an 18.6 MB
   source-built side-by-side APK versus our 15.2 MB prebuilt one. Check against
   the size table in `docs/modernization-plan.md` §5.
4. **LibRaw pin is not local.** `build-windows.ps1` will require
   `$BuildHome\LibRaw-42f89c16…`. Obtain it from a `445e314` checkout with
   `engine/setup-libraw.ps1 -DependencyDirectory $BuildHome` before any
   source-native build. The old `app/build/libraw-*.patch` scripts are no longer
   applied; keep them until the new path is proven, then decide separately.
5. **Checker gap.** `check-patch.py` scans `spk_*` C ABI calls only; it does not
   detect missing `spk::io` namespace APIs. Track the planned extension as a
   follow-up.
6. **Wire semantics deferred.** `camera.exposure_baseline_ev` and the no-film
   precision change are merged upstream but not adopted in Java. Keep
   `LookCodec`/`SessionState` as they are; adopting them is a separate wire
   decision with device revalidation.

## 6. Execution plan

### Step 0 — preflight
- Confirm both branches are clean; push current branch (already done).
- Note the rollback points: current `fb99920`, repair `1f21af1` (pre-upstream
  merge), engine fork `445e314e`.
- Ensure the upstream mirror is fetched (already fetched today); if `445e314e`
  is no longer checked out, `git -C <upstream> checkout --detach 445e314e`.

### Step 1 — integrate
- Work directly on `feat/windows-debug-builds` (or create
  `feat/merge-source-build-repair` from it for review).
- `git merge --no-ff feat/source-build-repair`.
- Resolve `docs/source-build-repair-plan.md` by taking the repair side of both
  conflict hunks; leave the three auto-merged files exactly as produced.

### Step 2 — offline verification (must all pass)
- `.\scripts\verify.ps1`
- `.\scripts\verify.ps1 -EngineRoot <clean 445e314 checkout>` (check-patch +
  JNI compile)
- `python app/build/check-patch.py <clean 445e314 checkout>` → expect
  `113 patched files, 31 JNI spk_* calls all declared`
- Confirm no `<<<<<<<` markers remain:
  `git grep -n '^<<<<<<<'` over the tree.

### Step 3 — native rebuild gate
- Fetch the pinned LibRaw master tree via `engine/setup-libraw.ps1`.
- `.\scripts\build-windows.ps1 -EngineCheckout <patched 445e314 tree>` and
  confirm `libspektrafilm_engine.so` builds with all ten new symbols.
- Stage that `.so` into a side-by-side APK; do **not** overwrite `app/prebuilt/`.

### Step 4 — device gates
- Prebuilt side-by-side APK: golden bands 0/128/512 must equal
  `e0517f9ff1532b87`; UI export smoke test must still succeed.
- Source-built side-by-side APK: golden bands and JPEG/DNG export metadata must
  match the prebuilt baseline on a physical device. This is the skipped Phase 0
  gate; if it cannot be run, mark the source-built path as **unvalidated** and
  keep shipping the prebuilt payload.

### Step 5 — land
- Commit the merge with a message naming both branches and the upstream tip
  `f87c5212`; push `feat/windows-debug-builds`.
- Update `docs/improvement-plan.md` P0.1 only after Step 4 passes; otherwise
  leave it open and link this plan.

## 7. Follow-ups (separate PRs, not part of this merge)

- Extend `check-patch.py` to detect missing `spk::io` namespace APIs.
- Contribute `tiling.hpp`, the private `spk_*` ABI and `spk::io` helpers upstream
  against `Windows_Linux_v2` to shrink the patch.
- Decide the `pinned_auto_exposure_ev` vs `camera.exposure_baseline_ev`
  reconciliation and the no-film wire field with device tests.
- Re-evaluate the LibRaw 0.22.2 patch files once the master pin is proven.

## 8. Implementation outcome (2026-11-22)

Executed through Step 4. Results:

- **Step 0/1:** both branches clean; plan doc committed (`fbb564d`);
  `git merge --no-ff feat/source-build-repair` produced exactly one conflict
  (`docs/source-build-repair-plan.md`, add/add) resolved by taking the repair
  side of both hunks. The three auto-merges were verified semantically correct.
- **Step 2:** `scripts/verify.ps1` → all PASS;
  `scripts/verify.ps1 -EngineRoot <445e314 checkout>` → all PASS including
  `check-patch` and the shimmed JNI compile; direct `check-patch.py` →
  `113 patched files, 31 JNI spk_* calls all declared`. No conflict markers.
- **Step 3:** the pinned LibRaw master tree was already present locally. A
  source-native build from the merged patch produced
  `SpektraLab-windows-debug-side-by-side.apk` (**18.6 MB**, signed), with all
  new `spk_*` symbols exported (14 checked). `app/prebuilt/` was not touched.
- **Step 4:** prebuilt side-by-side APK golden bands 0/128/512 =
  `e0517f9ff1532b87` — unchanged. **The source-built APK fails the gate:**
  it installs but rendering aborts with
  `spektralab: unknown parameter 'halation_additive'`. A static comparison of
  the 81 parameters Java sends against the merged engine's table finds three
  unsupported names: `halation_additive`, `halation_scatter_scale`,
  `hue_correction`. This is the deferred wire-semantics risk (§5.6), now
  confirmed on device (emulator).

**Decision per the plan:** the source-built path is marked **unvalidated**;
the prebuilt 1.6.2m payload continues to ship. P0.1 remains open and links this
plan. Wire compatibility (aliases or a Java mapping for the three parameters)
is a follow-up requiring the physical-device equivalence baseline that was
skipped in Phase 0.
