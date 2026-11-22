# Architectural improvement plan (post-1.7.0 adoption)

**Status (2026-11-22):** Phase 0 PASSED. Phases 1, 2, 3, 4 and 6 COMPLETE.
Phase 5 is optional and deferred.

Every landed phase was gated with `scripts/verify.ps1`, a
`-UsePrebuiltNative -SideBySide` APK build, and `device-golden.ps1` on the Orca
emulator. The golden hash stayed `e60490499b9a94eb` on bands 0/128/512 through
all of them.

## 1. Review findings — disposition

| # | Finding | Disposition |
|---|---|---|
| 1 | `MainActivity` is a ~34k-line god object owning 8 panel builders and ~20 sheets. | Improved: the seven 1.7.0 panel builders now live in `ui/panels/PanelFactories`, the sheet/panel shells in `ui/SheetDialogs` / `ui/PanelViews`. Remaining size is mostly page builders, workers' wiring and state fields. |
| 2 | The `ui/` panel factories from the earlier refactor were unused dead architecture. | RESOLVED: deleted `FilmPanel`, `ViewPanel`, `OpticsPanel`, `SimplePanels`, `PanelFactories`, `PanelHost`, `TabBar` (Phase 1). One implementation remains. |
| 3 | Workers reached through `HostWorker` to the whole Activity; ~200 members were widened. | RESOLVED: all six workers hold typed narrow host interfaces; `HostWorker` is a marker; the checker enforces it (Phases 2 + 6). |
| 4 | `ExportController`/`RenderController`/`ImportController` fields were dead. | RESOLVED: dead fields removed from `MainActivity`; the classes stay because `DomainTest` uses them as tested seams. |
| 5 | Two parallel type systems: top-level vs nested `Look`/`Frame`. | Deferred (Phase 5 is optional, high risk). |
| 6 | Dead direct-buffer shims in `SpektraLabEngine` / JNI. | RESOLVED in the earlier Phase 1 pass. |
| 7 | Package checker too weak. | RESOLVED: worker boundaries are now asserted by `check-package-rules.py` (Phase 6). |

## 2. Principles

- Pure decisions → `domain/`; Android storage → `data/`; Views and dialogs → `ui/`;
  `MainActivity` wires.
- Views talk to the Activity through **narrow interfaces**, not raw fields.
- Workers talk to the Activity through **narrow host interfaces**, not `MainActivity`.
- No pixel-path change without a golden re-check (`e60490499b9a94eb`).
- Keep all GPL / CC BY-SA notices intact (see `AI-NOTICE.md`).

## 3. Completed phases

### Phase 0 — baseline gate
Restored the Orca emulator and fixed prebuilt asset staging (prefer the cached
release APK, which carries all 48 Vulkan shaders). `device-golden.ps1 -Bands
0,128,512` produced `e60490499b9a94eb` with the wired workers.

### Phase 1 — remove dead surface
- Removed the dead direct-buffer compatibility surface from `SpektraLabEngine`
  and `spektralab_jni.cpp`.
- Removed dead Activity fields (`reopeningFrame`, `exportController`,
  `renderController`).
- Deleted seven unused `ui/` scaffold classes and kept the tested domain
  controller classes.

### Phase 2 — narrow worker coupling
- `BatchImportHost`/`BatchExportHost` were already in place.
- Added `ImportWorkerHost`, `RawImportWorkerHost`, `ExportWorkerHost` and
  `RenderWorkerHost`; each worker now holds its own typed host and `HostWorker`
  is a pure marker. `MainActivity` implements all six host interfaces through
  small, explicit accessors and delegators.

### Phase 4 — dialog/sheet shells
- Added `ui/SheetDialogs` with the paper root + blue cap, title/close header,
  pinned note, long-note scroll rule and the dimmed bottom-window presentation.
  `showSheetBody`, `showMultiChoiceSheet` and the other sheets route through it.
- Emulator smoke: the Metering sheet opens with its translated title and rows.

### Phase 6 — strengthen boundaries
- `check-package-rules.py` now fails if a worker references `MainActivity`
  directly, lacks its typed host field, if `HostWorker` exposes the Activity, or
  if `MainActivity` stops implementing any host interface. Diagnostic tags and
  the still-shared nested `Frame`/`Look`/`LiveClip` types are excluded until
  Phase 5.

## 4. Phase 3 — panel builders moved to `ui/`

- `ui/PanelViews` owns the shared section header; `ui/SheetDialogs` owns the
  sheet shell.
- The seven 1.7.0 panel builders (`buildFilmPanel`, `buildViewPanel`,
  `buildOpticsPanel`, `buildComposePanel`, `buildFormatPanel`, `buildEdgePanel`,
  `buildFotuPanel`/`Into`) now live in `ui/panels/PanelFactories` as functions of
  a narrow `PanelHost`. `PanelHostImpl` (root package) implements that interface
  by delegating to `MainActivity`, keeping the Activity as lifecycle + wiring.
- The page builders followed: `ui/panels/ViewPages` (9 methods),
  `ComposePages` (3), `FotuGroups` (8) and `PrintEdge` (4), each with its own
  narrow host and root-package delegating implementation.
- Dialog/sheet groups were extracted the same way: `AppDialogs` (feedback +
  text dialogs), `SettingsDialogs` (settings/about screens and sheets),
  `PickerSheets` (meter/paper/HDR/aperture/Fotu/edge pickers) and
  `PresetDialogs` (presets panel, batch delete, apply look). Together these
  remove ~2,300 lines from `MainActivity` (now ~31,680).
- The package checker also rejects fully-qualified `MainActivity` references in
  `ui/` (with a small allow-list of still-shared nested view types).
- Emulator smoke: Film, Optics, Scale, Compose and Border panels all open with
  translated labels, no crash; golden bands 0/128/512 unchanged.

## 5. Deferred: Phase 5 (Look/Frame consolidation)

Optional in the original plan. The top-level `Look`/`Frame` (used by
`SessionState`, `LookCodec`, `Frame.java`) and the nested `MainActivity.Look`/
`Frame` are both live and are referenced across workers and every frame call
site. Consolidating them is a pure refactor with a large blast radius and no
pixel-level test coverage, so it stays deferred until there is a separate,
dedicated change window.

## 6. Verification gates per phase

1. `scripts/verify.ps1` — all offline checks PASS.
2. `scripts/build-windows.ps1 -UsePrebuiltNative -SideBySide` — APK builds.
3. `scripts/device-golden.ps1 -Apk ... -Serial emulator-5554 -Bands 0,128,512`
   — hash `e60490499b9a94eb`.
4. UI smoke on the Orca emulator for the sheets/panels touched.

## 7. Out of scope

- Kotlin/Compose/Gradle migration (see `docs/modernization-plan.md`).
- Changing the engine patch or LibRaw pin.
- Removing or weakening licence notices.
