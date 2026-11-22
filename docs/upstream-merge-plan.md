# Upstream merge plan — SpektraLab engine

**Status: plan only.** No engine source, patch or build files are changed by
this document.

## 1. What "upstream" means here

Our engine fork point is `JamesQiu2005/SpektraLab` commit
`445e314e28b9b455d2457471fadaffd28c07270b` (2026-10-29). The public mirror in
`%LOCALAPPDATA%\SpektraLabBuild\upstream` was fetched on 2026-11-22 and now has
four refs:

| Ref | Position vs `445e314` | suitability |
|---|---|---|
| `main` | No common ancestor; header moved to `engine/include/spektralab/spk_engine.h`, 2.4.x ABI | **Do not merge** — different wire/header path |
| `main_v1` | Diverged | Not the lineage of our patch |
| `Windows_Linux` | `445e314` is the merge base; 637 commits ahead, 20 touching `engine/` | Closest to our patch base |
| `Windows_Linux_v2` | 41 commits ahead of `Windows_Linux`, 26 engine commits | Newest compatible candidate; same header path and `spk_result` layout |

`andreavolpato/spektrafilm` (the Python reference) is a separate upstream for
*model semantics*, not for code we merge. We have confirmed that
`engine/resources` and `engine/resources_product` are **unchanged** between
`445e314` and `Windows_Linux_v2`, so merging does not alter the film/paper data
in the shipped payload.

## 2. What changed upstream since our fork

Twenty-six engine commits on `Windows_Linux_v2` since `445e314`; nine touch
files our patch also modifies. The overlap is substantial: **713 lines added /
238 deleted across ten shared files**.

| Upstream commit | Change | Overlap with our patch | Impact |
|---|---|---|---|
| `3fbd9bd3` | Reduce large-image memory peaks, 102 MP milestone | `gpu.hpp` +53/−9, `vulkan_gpu.cpp` +216/−8, `blur.cpp` +30/−6, `engine.cpp` +162/−58, `pipeline.cpp` +167/−111, `pipeline.hpp`, `view_process.cpp` | **Highest conflict risk.** Adds pool promotion, memory observations and lifetime changes around the same GPU/pipeline code where we added `TilePlan`, `dispatch_banded`, `tiling.hpp` and the 10 C ABI APIs. |
| `61f79d2b` | Preserve decoded source precision for no-film render/export | `vulkan_gpu.cpp`, `raw_decoder.cpp`, `engine.cpp`, `view_process.cpp`, `spk_view_grade_body.glsl` | Changes wire meaning of `no_film`; our Java `LookCodec` would need to carry the new field. |
| `2ca735ad` | Exposure handoff fixes; adds `camera.exposure_baseline_ev` | `params.hpp/.cpp`, `engine.cpp`, `pipeline.cpp` | Overlaps our private `pinned_auto_exposure_ev`. Upstream now has a field for the same idea. |
| `8cd9992c`, `b5467e83` | GFX100RF and Sony A7 V ARW6 RAW support | `raw_decoder.cpp`, `CMakeLists.txt`, new `engine/third_party/libraw-arw6/**` | Would improve RAW coverage but adds ~17.6k lines of vendor code and changes the LibRaw build; currently our payload pins LibRaw 0.22.2. |
| `53299f73` | Refuse Vulkan work after device loss | `vulkan_gpu.cpp`, `CMakeLists.txt`, new tests | Useful robustness fix; must be merged around our banded dispatch edits. |
| `7766b847` | Rename `meter_frame`'s `frame` to avoid `rpcndr.h` | `engine.cpp` only | Trivial, safe to take. |
| `dc440a34`, `0cc25987` | C++ standard for MSVC; DI/headroom kernel probes | `CMakeLists.txt`, tests | Low risk; not in our patch today. |
| `b5467e83` etc. | Many unrelated desktop/host/doc commits | none | Ignore for the Android fork. |

**Still absent upstream:** we checked `Windows_Linux_v2` for every API we had to
reimplement — `spk_probe_canary`, `spk_trim_pool`, `spk_pin_auto_exposure_ev`,
`spk_clear_pin_auto_exposure_ev`, `spk_set_output_linear`, `spk_stage_snapshot`,
`spk_suspend_session`/`resume`, `spk_debug_print_*`, `spk_export_source`,
`spk_max_plane_bytes`, `spk_device_profile`, `spk_error_code`, `tiling.hpp`,
`dispatch_banded`, `RawDecodeCache`, `write_dng`, `read_dng_display_profile`.
Only `spk_trim_pool` exists on `main`, with a two-argument signature that does
not match our JNI. **Our patch additions remain necessary after any merge.**

## 3. Merge strategy

### Recommended: cherry-pick into the current patch, do not rebase wholesale

Rationale: the repaired patch now applies cleanly, passes `check-patch.py`, and
builds `libspektrafilm_engine.so` with the NDK. Before changing the engine
base we must first prove that the rebuilt payload is pixel-equivalent to the
shipped 1.6.2m payload on a physical device. A wholesale rebase onto
`Windows_Linux_v2` would invalidate that baseline and mix 713 lines of upstream
engine work with our 245-line patch in one unverifiable step.

The alternative — rebasing the patch onto `Windows_Linux_v2` — is only
justified if we actually need upstream's memory/RAW/wire features. If chosen,
it must be a separate PR with its own full revalidation (§5).

### Ref to target when cherry-picking

Use `Windows_Linux_v2` tip (`f87c5212`, 2026-11-18) as the source of individual
commits: it is the newest ref with our header path and ABI. Do **not** take
commits from `main`.

## 4. Work phases

### Phase 0 — Freeze the current baseline (gate before any merge)

- Build a side-by-side APK from the rebuilt payload with
  `-UsePrebuiltNative -SideBySide` (or the rebuilt `.so` staged in its place).
- On a physical ARM64 Vulkan device, record band 512/128 hashes and the 12 MP
  JPEG/DNG export metadata, and compare with the shipped 1.6.2m payload.
- Keep `feat/source-build-repair` at `1f21af1` as the rollback point.

**Gate 0:** pixel/export equivalence established. If it fails, stop merging and
fix our reimplementations first.

### Phase 1 — Triage upstream commits

Classify each of the 26 engine commits as:

- **take**: no wire change, no resource change, and no overlap with our patch;
- **take with adaptation**: overlaps our patch but the benefit justifies a
  three-way merge;
- **defer**: changes wire semantics or adds large vendor payloads.

Triage completed 2026-11-22 against `Windows_Linux_v2` tip `f87c5212`:

| Commit | Class | Reason |
|---|---|---|
| `7766b847` | take | Rename only; avoids Windows `rpcndr.h` collision |
| `dc440a34` | take | CMake C++ standard; no source or wire change |
| `a6bcacf6` | take | Merge of the MSVC fix above |
| `0cc25987` | take | Vulkan DI/headroom test targets only |
| `dcdab292` | take with adaptation | Restores MoltenVK guards in `engine.cpp`; small edit beside our C ABI additions |
| `53299f73` | take with adaptation | Device-lost guard in `vulkan_gpu.cpp`; useful, but edits the file our banded dispatch changes |
| `6dd3d47d` | defer | Docs about the MoltenVK Vulkan-Headers pin |
| `a764653c` | defer | Windows packaging docs |
| `05c5b23b` | defer | Windows dependency fetch script |
| `255ea880` | defer | Windows cross-build check |
| `6fa28dfa`, `268af5f0` | defer | Windows PNG XMP metadata, desktop host only |
| `223205fd`, `0799cf73` | defer | Fuji X-E5 RAF calibration; camera table only |
| `d978ba9b` | defer | Canon R6 III CR3 colour; camera table only |
| `8cd9992c` | defer | GFX100RF RAW support; adds `raw_camera.hpp` |
| `b5467e83`, `68b78bec` | defer | Sony ARW6 vendor overlay (~17.6k lines) and new LibRaw build path |
| `61aee26d`, `c59d0c2a`, `4dec4cf3` | defer | Moves LibRaw to upstream master and removes the ARW6 overlay; conflicts with our pinned LibRaw 0.22.2 |
| `aaec9528` | defer | Merge of the Windows/Linux work already classified individually |
| `30eb6930` | defer | Desktop host scheduler trim, not the C ABI; our `spk_trim_pool` stays |
| `2ca735ad` | defer | Adds `camera.exposure_baseline_ev` — wire change, Phase 4 decision |
| `61f79d2b` | defer | No-film precision and wire semantics, Phase 4 decision |
| `3fbd9bd3` | defer | Memory/lifetime work with the largest overlap; Phase 3 decision |

**Gate 1:** triage table above — complete.

### Phase 2 — Land low-risk commits

- Cherry-pick the "take" commits into a temp engine tree (not into our repo,
  which stores only the patch).
- Regenerate `patches/engine-android.patch` from that tree, as done in the
  repair work.
- Run `check-patch.py`, `git apply --check` on a clean `445e314` checkout, and
  the full NDK/CMake build.

**Gate 2:** all three pass; JNI symbol count unchanged.

**Progress (2026-11-22):** Phase 0 was explicitly skipped by the operator; no
physical-device equivalence baseline was recorded. Cherry-picked into a temp
engine tree: `0cc25987` (DI/headroom test probes), `dc440a34` (C++ standard),
`7766b847` (`meter_frame` rename) and `53299f73` (Vulkan device-lost guard).
`dcdab292` was skipped: after conflict resolution it was empty because our
`out->owner` line was preserved. Conflicts in `engine/CMakeLists.txt` and
`vulkan_gpu.cpp` were resolved by keeping our banded dispatch and `host_storage`
exclusion while adding the upstream device checks. The regenerated patch has
**63 sections**, applies cleanly to `445e314`, passes `check-patch.py`, and the
NDK/CMake build of `libspektrafilm_engine.so` succeeds. No pixel-equivalence
claim is made because Phase 0 was skipped.

### Phase 3 — Reconcile the memory work (`3fbd9bd3`)

- Three-way merge our `gpu.hpp`/`vulkan_gpu.cpp`/`pipeline.cpp` additions with
  upstream's pool/lifetime changes.
- Decide whether `TilePlan`/`dispatch_banded` should be expressed in terms of
  upstream's new pool observations rather than our own helpers.
- Keep the band 512/128 byte-equality test as the correctness oracle.

**Gate 3:** build passes; banded vs whole renders remain byte-identical.

### Phase 4 — Reconcile exposure and no-film fields

- Decide between our `pinned_auto_exposure_ev` and upstream's
  `camera.exposure_baseline_ev`. Preferred: adopt the upstream field and add it
  to `LookCodec`/`SessionState` with a Safe default, so the wire stays coherent.
- If `no_film` is adopted, add the field to the Java wire and verify the
  before/after comparison export still matches.

**Gate 4:** import, drag, pin EV and no-film export behave as before on device.

### Phase 5 — Full revalidation

- `scripts/verify.ps1` (offline checks, checkers, JVM tests, C++ compile checks)
- `check-patch.py` and NDK/CMake build of `spektrafilm_engine.so`
- Side-by-side install and `device-golden.ps1 -Bands 512,128`
- 12 MP JPEG/DNG export metadata comparison
- APK size within the per-mode baseline from
  [`docs/modernization-plan.md`](modernization-plan.md) §5

**Gate 5:** no unexplained pixel/metadata/size change.

### Phase 6 — Reduce future fork drift (optional)

Propose the Android-private APIs (`tiling.hpp`, JNI ABI additions, IO helpers)
as an upstream PR against `Windows_Linux_v2`. If accepted, our patch shrinks to
only the true Android-specific scheduling changes. Do not block Phase 0–5 on
this.

## 5. Risks and mitigations

| Risk | Mitigation |
|---|---|
| 713 added / 238 deleted lines overlap our patch | Cherry-pick in small commits; regenerate and rebuild after each; keep `1f21af1` as rollback |
| Upstream `exposure_baseline_ev` duplicates our pin | One field only; explicit mapping in `LookCodec`; device EV test |
| Raw camera support pulls ~17.6k lines and a new LibRaw build | Defer until a device need is proven; keep pinned LibRaw 0.22.2 |
| Resource data changes would alter pixels | Confirmed none between `445e314` and v2; re-check after each cherry-pick |
| Upstream tests run on Windows/Metal hosts, not Android | Treat them as design references; our gates remain `check-patch.py`, NDK build and device hashes |
| No physical device attached | Phase 0 cannot be completed here; do not start Phase 3+ without it |

## 6. Open questions

1. Target ref: `Windows_Linux_v2` tip (recommended) or the more stable
   `spektralab-windows-v1.3.4-beta.3` tag?
2. Is the memory-peak work (`3fbd9bd3`) worth the conflict? It overlaps our
   tiling work but may supersede part of it.
3. Do we want the new RAW camera families, with the LibRaw source payload and
   APK-size implications?
4. Should our private APIs be contributed upstream before or after the device
   equivalence gate?
