# Architectural improvement plan (post-1.7.0 adoption)

**Status:** Phase 0 PASSED (2026-11-22). Phase 1 partially implemented.

Phase 0: after restoring the emulator and fixing the prebuilt APK's asset
staging (prefer the cached release APK, which carries all 48 Vulkan shaders),
`device-golden.ps1 -Bands 0,128,512` gives `e60490499b9a94eb` with the four
wired workers. Offline verification and the APK build also pass.

### Phase 1 progress
- Removed the dead direct-buffer compatibility surface from `SpektraLabEngine`
  (`nativeBufferAddress` JNI declaration, `allocDirect`, `freeDirect`,
  `openSession(ByteBuffer)`, `liftRows(Bitmap, ByteBuffer)`) and the matching
  `nativeBufferAddress` JNI function in `spektralab_jni.cpp`. All were confirmed
  to have zero callers after the workers were re-extracted from the 1.7.0
  pointer-based code.
- Left the three domain controllers in place: they have no production callers,
  but `DomainTest` exercises them, so removing them would delete tested seams.
  Decide in Phase 2 whether to wire them into the workers instead.

## 1. Review findings

| # | Finding | Evidence | Risk if left |
|---|---|---|---|
| 1 | `MainActivity` is still a god object: ~34,000 lines after the four worker delegations. It owns 8 panel builders (`buildFilmPanel`, `buildViewPanel`, `buildOpticsPanel`, `buildComposePanel`, `buildFormatPanel`, `buildEdgePanel`, `buildFotuPanel`, plus sources/shelf) and ~20 dialog/sheet builders. | `grep 'private (View\|LinearLayout) build\w+Panel'` and `show*Dialog`/`show*Sheet` in `MainActivity.java` | Every feature change touches one file; the package boundaries the project adopted are not actually realised. |
| 2 | The `ui/` panel factories from the earlier refactor are **unused**: `FilmPanel`, `ViewPanel`, `OpticsPanel`, `SimplePanels`, `PanelFactories`, `TabBar` are never referenced by `MainActivity`. | Only `buildFilmPanel()` etc. are called; no `new FilmPanel(...)` | Dead architecture — either wire it or delete it. |
| 3 | Workers depend on the **whole Activity** through `HostWorker`. ~200 members were widened from `private` to package-private to satisfy them. | `HostWorker` exposes `final MainActivity host`; widened fields across `MainActivity.java` | No real encapsulation; any worker can reach any Activity state. |
| 4 | `ExportController`, `RenderController`, `ImportController` are constructed as Activity fields but **never used** (workers call native/engine paths directly). | grep shows only declarations at `MainActivity.java:552-555` | Dead seams; misleading architecture. |
| 5 | Two parallel type systems: top-level `Look`/`Frame` (used by `SessionState`, `LookCodec`, `Frame.java`) **and** nested `MainActivity.Look`/`Frame` (used by the Activity and workers). | `new Look()` appears in both `MainActivity.java` and `SessionState.java` | Confusing conversions, two sources of truth. |
| 6 | Dead compatibility shims in `SpektraLabEngine`: `allocDirect`, `freeDirect`, `liftRows(Bitmap, ByteBuffer, …)` are no longer called after the workers were re-extracted from the 1.7.0 pointer-based code. | grep shows only declarations; workers call `host.liftRows(Bitmap, long, …)` | Dead API surface. |
| 7 | Package checker is weak: workers, controllers, state and helpers all live in root `app.spektralab`, so boundary enforcement stops at `ui/`, `domain/`, `data/`. | `check-package-rules.py` | Fine-grained boundaries cannot be enforced. |

## 2. Principles

- Pure decisions → `domain/`; Android storage → `data/`; Views and dialogs → `ui/`;
  `MainActivity` wires.
- Views talk to the Activity through **narrow interfaces**, not raw fields.
- Workers talk to the Activity through **narrow host interfaces**, not `MainActivity`.
- No pixel-path change without a golden re-check (`e60490499b9a94eb` on the
  emulator; device currently unavailable).
- Keep all GPL / CC BY-SA notices intact (see `AI-NOTICE.md`).

## 3. Phase plan

### Phase 0 — baseline gate (blocking)
- Restore a working emulator; re-run `device-golden.ps1 -Bands 0,128,512` and
  confirm `e60490499b9a94eb` with the currently wired workers.
- This is required before any further architectural change lands.

### Phase 1 — remove dead surface (low risk)
- Delete unused `ExportController`, `RenderController`, `ImportController`
  fields and their classes (or wire them properly in Phase 2 — decide then).
- Delete unused engine shims `allocDirect`, `freeDirect`,
  `liftRows(Bitmap, ByteBuffer, …)` and the now-unused `openSession(ByteBuffer)`
  overload / `nativeBufferAddress` JNI shim, **if** no callers remain.
- Verify: compile, offline checks.

### Phase 2 — narrow worker coupling (medium risk, highest value)
- Define one interface per worker with only the members it actually uses:
  `BatchHost`, `ImportHost`, `RawImportHost`, `ExportHost`, `RenderHost`.
- `HostWorker` becomes a marker with no exposed Activity; each worker holds its
  own typed host interface.
- `MainActivity` implements the interfaces (its widened members can then be
  re-narrowed to private where no longer needed).
- Verify: compile, offline checks, golden.

### Phase 3 — extract panel builders into `ui/` (medium risk)
- Either wire the existing unused panel factories or replace them with factories
  generated from the 1.7.0 builders; do not keep two implementations.
- Convert `buildFilmPanel`, `buildViewPanel`, `buildOpticsPanel`,
  `buildComposePanel`, `buildFormatPanel`, `buildEdgePanel`, `buildFotuPanel`
  into stateless `EditorState → View` functions with injected callbacks.
- Verify: UI smoke (panel opens), golden.

### Phase 4 — extract dialog/sheet builders (low-medium risk)
- Move `show*Sheet`/`show*Dialog` shells into `ui/Dialogs.java` with a shared
  bottom-sheet builder; keep content as callbacks.
- Verify: UI smoke, golden.

### Phase 5 — consolidate `Look`/`Frame` (high risk, optional)
- Make the top-level types the single definition and remove the nested ones, or
  vice versa. Requires touching `SessionState`, `LookCodec`, all workers and
  every frame call site.
- Only do this after Phases 2–4 are stable.

### Phase 6 — strengthen boundaries (low risk)
- Extend `check-package-rules.py` to assert workers depend only on their host
  interfaces and never on `MainActivity` directly.

## 4. Execution order and gates

1. Phase 0 must pass before Phase 2+ (pixel paths are affected).
2. Phase 1 can proceed while the emulator is being repaired.
3. Each phase: compile → `scripts/verify.ps1` → APK build → device golden/smoke.
4. Commit each phase separately; do not fast-forward `master` until the full
   gate passes.

## 5. Out of scope
- Kotlin/Compose/Gradle migration (see `docs/modernization-plan.md`).
- Changing the engine patch or LibRaw pin.
- Removing or weakening licence notices.
