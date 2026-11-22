package app.spektralab;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import app.spektralab.engine.SpektraLabEngine;

import java.nio.ByteBuffer;
import java.util.List;

/** Worker re-extracted from the 1.7.0 Activity; owns the full current path. */
final class RawImportWorker extends HostWorker {
    private final RawImportWorkerHost host;

    RawImportWorker(RawImportWorkerHost host) { this.host = host; }

    void run(Uri uri, int reopenIndex) {

        host.setFotuSourcePath(null);
        host.setFotuSourceDead(null);
        host.setFotuGeneration(host.getFotuGeneration() + 1);
        // **This decode has a serial number.** A RAW takes three to five seconds, and the strip
        // is right there: tap chip A, tap chip B before A finishes, and A's worker woke up last
        // -- closed B's session, installed A's pixels and drew A under B's chip. That is the
        // "quick taps give the wrong picture / half-drawn canvas" report. `loadImage` bumps the
        // epoch; this worker is the one place that has to honour it.
        final int importEpoch = host.getRenderEpoch();
        // Read once, on the UI thread, before the decode starts: the worker
        // below cannot be allowed to see a flag that a later tap already moved.
        final boolean stayOnOriginal = host.getReopenToOriginal();
        host.setReopenToOriginal(false);
        // **A different import is a different frame.** The cache keys on path +
        // mtime, and every import is copied to `files/import/<same name>`; two
        // copies inside one second share an mtime, so the second import could
        // be handed the first one's decoded pixels. Clearing here costs one
        // unpack and buys the guarantee that the pixels belong to this file.
        try { SpektraLabEngine.clearRawCache(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29554", ignored); }
        CrashTrail.mark(host.context(), "import:raw " + uri);
        try {
            String filePath = host.getFilePathFromUriHostCall(uri);
            host.setRawFilePath(filePath);
            // **The phone's own idea of how this raw should look.** OnePlus and
            // OPPO ship a 128/140-point `ProfileToneCurve` plus a
            // `BaselineExposure`; LibRaw parses neither, which is the whole of
            // "our canvas is flat and Lightroom's is not". Read once here, a
            // 6 KB seek, and handed to the canvas only.
            host.setSourceToneCurve(SpektraLabEngine.sourceDisplayProfile(filePath));
            final int curvePts = host.getSourceToneCurve() == null ? 0 : (host.getSourceToneCurve().length - 1) / 2;
            DebugSink.text("raw-view", "display profile: "
                    + (host.getSourceToneCurve() == null ? "none"
                       : curvePts + " points, baseline "
                         + String.format(java.util.Locale.US, "%.2f", host.getSourceToneCurve()[0]) + " EV"));
            // **Which half of the import we got to.** Until now the RAW path
            // filed one mark (`import:raw`) and then nothing until the decode
            // had already finished, so a report of the form "crashed importing
            // a RAW" could not be narrowed past "somewhere in the next four
            // seconds" -- the file copy, `open_file`, the decode, `spk_open`,
            // the meter or the source view. The JPEG path has had these marks
            // for a while; this one had not.
            CrashTrail.markNow(host.context(), "import:path " + (filePath == null ? "(null)" : "ok"));
            if (filePath == null) {
                host.showInAppToastHostCall("无法获取RAW文件路径");
                return;
            }

            // **Ask before the decode.** `open_file` reads the container header
            // in milliseconds; the decode that follows commits 12 bytes per
            // pixel before anyone is consulted, and at 50 MP that is 600 MB. A
            // sheet that appears after that memory is spent is a post-mortem,
            // not a choice.
            if (!host.getSuppressBudgetDialog() && !host.getImportAtNativeSize() && host.getPendingCapPixels() <= 0) {
                final int[] dims = SpektraLabEngine.rawDimensions(filePath);
                if (dims != null && dims[0] > 0 && dims[1] > 0) {
                    final long area = (long) dims[0] * (long) dims[1];
                    final int cap = host.maxImportPixelsHostCall();
                    if (area > cap || Math.max(dims[0], dims[1]) > host.getIMPORT_LONG_EDGE()) {
                        host.getStatusText().setText("选择导入尺寸");
                        host.showRawBudgetDialogHostCall(uri, dims[0], dims[1], cap, reopenIndex);
                        return;
                    }
                }
            }

            host.getStatusText().setText("Decoding RAW…");
            // **The same three things the JPEG path does, for the same three
            // reasons** — and this path had none of them, which is why a RAW
            // import kept the previous photograph everywhere except the little
            // source thumbnail:
            //   · stopPrefetch() retires the shelf queue, whose generation is
            //     what stops last frame's cards being filed under this frame;
            //   · compare.clear() drops `CompareView.after`, the previous
            //     render, which nothing else ever clears;
            //   · filmThumbs.clear() is the shelf's own cache, keyed by film
            //     id with no frame identity in the key.
            host.stopPrefetchHostCall();
            if (host.getCompare() != null) host.getCompare().clear();
            host.clearShelfCacheHostCall();
            host.setLastRenderedBitmap(null);
            host.refreshPanelLocksHostCall();
            // **Not `clearBusyPlate()`.** That emptied the canvas for the
            // decode and left "RAW LOADED" standing there while it worked --
            // the old behaviour the import was asked to stop having.
            host.setDateText(null);
            // **This method raises its own plate.** `loadImage` no longer does:
            // it can return on a size question, and a plate with nothing under
            // it is what "导入选项不显示" was. The decode sets the stage itself
            // as soon as it has a preview; the plate sits on top of that.
            host.showBusyHostCall("READING");
            new Thread(() -> {
                try {
                    // Someone tapped another chip while this file was decoding. The newer import
                    // owns the canvas, the session and the strip: this one must not touch any of
                    // them, or the picture and the selected chip disagree.
                    if (importEpoch != host.getRenderEpoch()) {
                        CrashTrail.mark(host.context(), "import:superseded " + importEpoch + "/" + host.getRenderEpoch());
                        return;
                    }
                    String resourcesDir = host.getResourcesDirHostCall();
                    if (host.getEngine() == null) {
                        host.vkLogFileHostCall().delete();
                        host.setEngine(SpektraLabEngine.create(resourcesDir, host.vkLogFileHostCall().getAbsolutePath()));
                        host.uploadCapabilitiesHostCall();
                    }
                    // Under the render lock: the shelf queue can still be
                    // inside `renderBegin` when this thread gets here, and
                    // closing a session out from under it is a native crash,
                    // not an exception.
                    synchronized (host.getRenderMutex()) {
                        if (host.getSession() != null) { host.getSession().close(); host.setSession(null); }
                    }
                    Probe.log("import", "pre avail=" + Probe.mb(host.availableMemoryBytesHostCall())
                            + " cap=" + host.maxImportPixelsHostCall() + " native=" + host.getImportAtNativeSize()
                            + " pool=" + Probe.pool(host.getEngine()));
                    Probe.step(host.context(), "import:pre");
                    // **Same trim as the JPEG path.** A 50 MP frame needs
                    // ~76 B/px of planes (3.8 GB); the previous session's pool
                    // is freed here rather than left for the allocator to run
                    // out against.
                    try { if (host.getEngine() != null) host.getEngine().trimPool(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29656", ignored); }
                    Probe.log("import", "trimmed pool=" + Probe.pool(host.getEngine()));
                    
                    // `film_stock` names a profile file: "" and "null" both make
                    // `load_profile` fail ("bad stock name"), so an unchosen film
                    // omits the key and the engine keeps its own default. The UI
                    // still shows 原图 and renders nothing until a card is tapped.
                    // **Two engines, two id spaces.** A frame developed by the
                    // second engine keeps `fotu:c200` in `filmStock` and a
                    // fotufilm medium in `printStock`. To `spk_open` both read
                    // as profile file names, and neither exists -- so restoring
                    // such a frame died with "bad stock name 'fotu:c200'" before
                    // one pixel was shown, on every launch. This session still
                    // has work to do (it decodes the raw and paints 原图), so it
                    // opens with no film and the default paper; the picture
                    // itself is developed by `renderFotu` through `renderImage`.
                    final boolean fotuFrame = host.isFotuStockViaHost(host.getFilmStock());
                    final String spkFilm = fotuFrame ? null : host.getFilmStock();
                    // **Never the sentinel.** 无相纸 is a UI idea; `spk_open`
                    // reads this as a file name (SL-261119-938F).
                    final String spkPaper = fotuFrame ? "kodak_portra_endura"
                            : host.spkPaperForEngineHostCall(host.getPrintStock());
                    final String filmField = spkFilm == null ? ""
                            : String.format(java.util.Locale.US, "\"film_stock\":\"%s\",", spkFilm);
                    // **`Locale.US`, always, for anything that becomes JSON.**
                    // Without it `%.2f` follows the phone's locale: a device set
                    // to German (or any comma-decimal locale) sent
                    // `"print_exposure":1,00`, the engine's parser read `1` and
                    // then wanted a key where the `,` stood, and answered
                    // `params_delta: expected a string at byte 147` -- byte 147
                    // is the first `0` of `00`. Every raw import on such a phone
                    // died with E-1106 (SL-261121-130E).
                    String params = String.format(java.util.Locale.US,
                        "{%s\"print_stock\":\"%s\",\"input_color_space\":\"ProPhoto RGB\",\"input_cctf_decoding\":false,\"output_color_space\":\"sRGB\",\"print_exposure\":%.2f,\"preview_long_edge\":2048}",
                        filmField, spkPaper, host.getPrintExposure()
                    );
                    
                    try { DebugSink.text("open-params", "path=" + filePath + "\n" + params); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29693", ignored); }
                    // The cap comes from the device, not from a constant: a
                    // plane is 12 B/px and a binding may not exceed the GPU's
                    // maxStorageBufferRange. Two limits apply, whichever is
                    // lower -- that range, and the 65 535-workgroup dispatch
                    // limit (16,776,960 px) this driver still loses blocks over.
                    final boolean nativeSize = host.getImportAtNativeSize();
                    host.setImportAtNativeSize(false);                   // one import only
                    // The raw path re-opens at the file's own size for its
                    // export already (`openRawSession(..., 0, 0)`), so a flag
                    // left over from a JPEG import must not reach it.
                    host.setExportAtNative(false);
                    if (nativeSize) {
                        try { DebugSink.text("import-mode", "native size, no cap"); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29706", ignored); }
                    }
                    host.setRawFilePath(filePath);
                    host.setImportCapPixels(nativeSize ? 0 : host.maxImportPixelsHostCall());
                    // **The frame's own balance, if it had one.** A frame
                    // reopened from the strip restores `wbUserMul` through
                    // `applyLook`; without passing it here the reopen decodes
                    // as-shot while the sliders still show the saved Kelvin.
                    final float[] wbMul = host.getWbUserMul();
                    CrashTrail.markNow(host.context(), "import:opening cap=" + host.getImportCapPixels());
                    host.setSession(wbMul != null
                            ? host.getEngine().openRawSessionTuned(filePath, params,
                                    host.getImportCapPixels(), nativeSize ? 0 : host.getIMPORT_LONG_EDGE(), wbMul)
                            : host.getEngine().openRawSession(filePath, params,
                                    host.getImportCapPixels(), nativeSize ? 0 : host.getIMPORT_LONG_EDGE()));
                    Probe.log("import", "opened=" + (host.getSession() != null)
                            + " cap=" + host.getImportCapPixels()
                            + " edge=" + (nativeSize ? 0 : host.getIMPORT_LONG_EDGE())
                            + " mul=" + Probe.mul(wbMul)
                            + " pool=" + Probe.pool(host.getEngine()));
                    host.setLastImportError(null);
                    {
                        // The size the engine actually opened, which is not
                        // always the one `rawDimensions` promised: `half_size`
                        // and the budget can both change it.
                        final int[] sd = host.getSession() == null ? null : host.getSession().getDimensions();
                        CrashTrail.markNow(host.context(), "import:opened "
                                + (sd == null || sd.length < 2 ? "none" : sd[0] + "x" + sd[1]));
                    }
                    // **The session's own parameters, which this path never sent.**
                    // `openRawSession` carries the film, the paper and the decode
                    // colour space; it does not carry grain, halation, glare, the
                    // format or the rebate, and those live in `applyParams()`. The
                    // bitmap path has always called this pair; the raw path did
                    // not, so a raw kept the engine's defaults for every optical
                    // control until the user moved a slider -- and a rebate or a
                    // date back chosen before the import never reached the render
                    // at all.
                    host.applySessionParamsHostCall();

                    // **Meter before the source view.** `spk_export_source`
                    // materialises the live tier, and a meter taken after it
                    // has come back -11 EV on a frame whose own export read a
                    // perfectly normal mean -- the second RAW opened from a
                    // batch metered black, rendered black, and ignored every
                    // metering mode. Measuring first makes the `solve` below a
                    // cache hit (`meter_key` does not change in between).
                    // **Under the same lock `close()` takes.** These two solves
                    // ran outside `renderMutex`, so a close from another path
                    // could release the session between the null check and the
                    // call -- which is exactly the dangling handle that killed
                    // the process in the meter (SL-261110-F858). The session
                    // field is also re-read inside the lock, so a swap that
                    // lands in between simply skips the solve.
                    try {
                        synchronized (host.getRenderMutex()) {
                            if (host.getSession() != null) host.getSession().solve("exposure");
                        }
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29764", ignored); }
                    // **The raw itself**, which is the "before" side of every
                    // comparison and the only honest thing to show on import:
                    // the decoded frame in working space, packed with no
                    // transfer function, so a display reads it the way it reads
                    // a raw -- flat, and dark. The camera's own JPEG is **not**
                    // a stand-in for it: a preview is the camera's rendering,
                    // not this frame, and showing it as "原图" is the same lie
                    // as importing it would be. No frame, no picture.
                    Bitmap rawView = host.exportSourceBitmapHostCall(host.getSession(), 0.0);
                    final Bitmap preview = rawView;
                    CrashTrail.markNow(host.context(), "import:source " + (preview == null ? "none"
                            : preview.getWidth() + "x" + preview.getHeight()));
                    final boolean hasPreview = preview != null;
                    if (!hasPreview) {
                        // The canvas is not allowed to be blank for no reason.
                        // A session that opened but produced no source frame is
                        // a bug, and it has to say so rather than look like a
                        // photo that failed to load.
                        try { DebugSink.text("raw-view", "no source frame for the canvas"); }
                        catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29784", ignored); }
                        host.runOnUiThread(() -> host.getStatusText().setText("RAW 已载入，原图预览未生成"));
                    }
                    if (hasPreview) {
                        host.runOnUiThread(() -> {
                            host.setBeforeBitmap(preview);
                            host.getCompare().setBefore(preview);
                            host.setStageHostCall(1, host.getFileNameHostCall(uri));
                            host.rememberFrameHostCall(uri, reopenIndex);
                            host.rebuildFilmCardsHostCall();
                            host.setStepHostCall(1);
                            host.getStatusText().setText("Raw preview");
                        });
                    }

                    // "Process" (配光): without a solved print exposure and
                    // filter pack the print runs on defaults and blows the
                    // highlights. The mac app makes the user press Process;
                    // this build does it once on open.
                    String solvedJson = null;
                    try {
                        synchronized (host.getRenderMutex()) {
                            solvedJson = host.getSession() == null ? null : host.getSession().solve("both");
                        }
                        DebugSink.text("solve", solvedJson == null ? "(null)" : solvedJson);
                        CrashTrail.markNow(host.context(), "import:print " + (solvedJson == null ? "none" : "ok"));
                    } catch (Throwable se) {
                        DebugSink.text("solve-failed", String.valueOf(se));
                    }
                    // **The meter's EV, applied to what the screen shows.** The
                    // developed side gets it; leaving the decoded side at 0 EV
                    // is what made a night frame look like a black rectangle
                    // next to a correctly exposed print.
                    if (solvedJson != null) {
                        // The applied value, for the export. See `meteredEv`.
                        try {
                            final org.json.JSONObject root = new org.json.JSONObject(solvedJson);
                            final org.json.JSONObject sp = root.optJSONObject("solved_params");
                            host.setMeteredEv(sp == null ? 0.0 : sp.optDouble("exposure_compensation_ev", 0.0));
                            Probe.log("meter", "applied ev=" + host.getMeteredEv());
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29824", ignored); }
                    }
                    if (hasPreview && solvedJson != null) {
                        double solvedEv = 0.0;
                        try {
                            solvedEv = new org.json.JSONObject(solvedJson)
                                    .getJSONObject("exposure_ev_by_method")
                                    .optDouble("balanced", 0.0);
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29832", ignored); }
                        // **Shown a little under the meter, on purpose.** At the
                        // full metered EV the decoded side reads brighter than
                        // the developed one, and the film's own contrast stops
                        // showing. 85 % is the same exposure a "before" frame is
                        // expected to have.
                        solvedEv *= host.getSOURCE_VIEW_EV_SCALE();
                        if (Math.abs(solvedEv) > 0.02) {
                            final Bitmap lifted = host.exportSourceBitmapHostCall(host.getSession(), solvedEv);
                            if (lifted != null) {
                                final double shownEv = solvedEv;
                                host.runOnUiThread(() -> {
                                    host.setBeforeBitmap(lifted);
                                    if (host.getCompare() != null) host.getCompare().setBefore(lifted);
                                    if (host.getCurrentFrame() >= 0 && host.getCurrentFrame() < host.getFrames().size()) {
                                        final MainActivity.Frame frame = host.getFrames().get(host.getCurrentFrame());
                                        try {
                                            final int tw = Math.max(1, host.dpHostCall(42));
                                            final int th = Math.max(1, Math.round(
                                                    tw * lifted.getHeight() / (float) lifted.getWidth()));
                                            frame.thumb = Bitmap.createScaledBitmap(lifted, tw, th, true);
                                            host.persistThumbHostCall(frame);
                                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29854", ignored); }
                                        host.rebuildFrameChipsHostCall();
                                    }
                                    try {
                                        DebugSink.text("raw-view", "display ev=" + shownEv);
                                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29859", ignored); }
                                });
                            }
                        }
                    }
                    host.runOnUiThread(() -> {
                        if (host.getSession() == null) return;
                        host.setShowingOriginal(true);
                        host.setLastRenderedBitmap(null);
                        host.setOriginalVisualHostCall(true);
                        host.rebuildFilmCardsHostCall();
                        if (host.getFilmStock() == null) host.getStatusText().setText("未渲染 · 选择胶片开始");
                        // See the JPEG import: a photo now exists, so the lock
                        // has to be recomputed even when no render follows.
                        host.refreshPanelLocksHostCall();
                        host.refreshGlowForCurrentViewHostCall();
                        // **No render from here.** `sourceWidth/Height` are
                        // still the previous frame's and `refreshEdgeFormats`
                        // has not run, so a render now used the old shape and
                        // the old gate: that is E-1305 the moment the user
                        // switches between a landscape and a portrait frame.
                        // The block after `dims` renders once this frame's own
                        // numbers are in.
                    });

                    // Get dimensions from session
                    int[] dims = host.getSession() == null ? null : host.getSession().getDimensions();
                    if (dims == null || dims.length < 2 || dims[0] <= 0 || dims[1] <= 0) {
                        // **A session that will not say how big it is failed
                        // quietly.** Seen while switching back and forth between
                        // two photographs: the previous import's close raced
                        // this one's open, and the handle that came back had no
                        // size. One clean reopen is worth trying before asking
                        // the user to start again -- and if it fails the same
                        // way, that is a decode problem and the message below is
                        // the honest one.
                        try { if (host.getSession() != null) host.getSession().close(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29895", ignored); }
                        host.setSession(wbMul != null
                                ? host.getEngine().openRawSessionTuned(filePath, params,
                                        host.getImportCapPixels(), nativeSize ? 0 : host.getIMPORT_LONG_EDGE(), wbMul)
                                : host.getEngine().openRawSession(filePath, params,
                                        host.getImportCapPixels(), nativeSize ? 0 : host.getIMPORT_LONG_EDGE()));
                        dims = host.getSession() == null ? null : host.getSession().getDimensions();
                    }
                    if (dims == null || dims.length < 2 || dims[0] <= 0 || dims[1] <= 0) {
                        // A zero here used to travel silently: the canvas laid
                        // itself out for nothing, every later size was wrong,
                        // and the reason was never on screen.
                        throw new IllegalStateException(
                                "session reported no size: " + java.util.Arrays.toString(dims));
                    }
                    host.setImportedWidth(dims[0]);
                    host.setImportedHeight(dims[1]);
                    int w = dims[0];
                    int h = dims[1];
                    host.setSourceWidth(w);
                    host.setSourceHeight(h);
                    try { DebugSink.text("raw-dims", "w=" + w + " h=" + h); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29916", ignored); }

                    // **The session's own size, not `rawSourceSize()`.** That
                    // legacy query answers {0,0} on purpose -- its comment says
                    // callers must ask the session -- and reading it put
                    // "RAW 0×0 · 0.0 MP" on the status bar.
                    final boolean capped = host.getImportCapPixels() > 0;
                    try {
                        DebugSink.text("raw-cap", (capped ? "capped at " + host.getImportCapPixels() : "native")
                                + " -> session " + w + "x" + h);
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29926", ignored); }

                    host.runOnUiThread(() -> {
                        host.refreshEdgeFormatsHostCall();
                        host.setStepHostCall(1);
                        // **No sheet.** The raw arrives at the size it was
                        // imported at; the suggestion is a line in the status
                        // bar, not a gate.
                        host.hideBusyHostCall();
                        host.getStatusText().setText("RAW " + w + "×" + h
                                + " · " + host.megapixelsViaHost(w, h) + " MP"
                                + host.importSizeNoteHostCall(w, h));
                        host.refreshViewPanelHostCall();
                        // **Render only now.** The frame's real size and the
                        // gates that fit it are both known, so the render the
                        // chip's Look asks for uses this picture's shape.
                        if (host.getFilmStock() != null && stayOnOriginal) {
                            host.setShowingOriginal(true);
                            host.setOriginalVisualHostCall(true);
                            host.updateFilmSelectionHostCall();
                            host.getStatusText().setText("ORIGINAL · RAW 原始数据（未调色，选胶片后才成片）");
                            if (!host.getBatchExportRunning()) host.startPrefetchHostCall();
                        } else if (host.getFilmStock() != null) {
                            host.setShowingOriginal(false);
                            host.updateFilmSelectionHostCall();
                            if (!host.getBatchExportRunning()) host.renderImageHostCall();
                        } else if (!host.getBatchExportRunning()) {
                            host.startPrefetchHostCall();
                        }
                    });
                } catch (Throwable t) {
                    final String said = host.describeImportFailureHostCall(t);
                    host.setLastImportError(said);
                    // A High-Efficiency NEF is not a preview job. Say what it
                    // is (E-1108, a compression this build has no decoder for)
                    // and what to change, and leave the file alone.
                    host.runOnUiThread(() -> {
                        host.getStatusText().setText("RAW加载失败 " + SpkError.shortForm(said));
                        host.showErrorDialogHostCall("RAW 加载失败", said);
                        // **The plate belongs to whoever raised it.** This path
                        // returned with `showBusy("READING")` still up, so a
                        // failed import left the canvas saying 「加载中」 for
                        // ever -- and deleting the frame did not clear it,
                        // because nothing owned it.
                        host.hideBusyHostCall();
                    });
                }
            }).start();
        } catch (Throwable t) {
            host.setLastImportError(host.describeImportFailureHostCall(t));
            host.hideBusyHostCall();
            host.showErrorDialogHostCall("RAW 加载失败", host.getLastImportError());
        }
        }
}
