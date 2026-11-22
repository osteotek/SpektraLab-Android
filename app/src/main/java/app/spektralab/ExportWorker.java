package app.spektralab;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import app.spektralab.engine.SpektraLabEngine;

import java.nio.ByteBuffer;
import java.util.List;

/** Worker re-extracted from the 1.7.0 Activity; owns the full current path. */
final class ExportWorker extends HostWorker {
    ExportWorker(MainActivity host) { super(host); }

    void run(String format, String tier, final Runnable onDone) {

        CrashTrail.mark(host, "export:begin");
        // **Read once, at the door.** Every early return below -- an export
        // already running, no session, no film -- would otherwise leave the live
        // request armed, and the *next* export, whatever it is, would attach a
        // clip to it.
        final int liveEdge = ("jpeg".equals(format) && !host.batchExportRunning) ? host.liveClipEdge : 0;
        host.liveClipEdge = 0;
        // **A second engine, a second writer.** fotufilm hands back a JPEG and
        // has no TIF/DNG encoder at all; saying so is better than writing a
        // .dng that is really a JPEG with the wrong extension.
        if (host.isFotuStock(host.filmStock)) {
            if ("dng".equals(format)) {
                host.showErrorDialog("fotufilm 没有 DNG 写出路径",
                        "这一支胶片由 fotufilm 引擎渲染，它只能写 JPEG 和 16 位 TIFF。\n\n"
                                + "要 DNG 请换一支 SpektraLab 引擎的胶片。");
                if (onDone != null) onDone.run();
                return;
            }
            host.exportFotu(format, tier, onDone, liveEdge);
            return;
        }
        // **One export at a time.** Two of these at once is two full renders of
        // the same frame competing for the same two gigabytes, and two files at
        // the end -- the sheet closes the instant a row is hit, so a tap that
        // looked like it did nothing gets repeated and both went through.
        final Thread running = host.exportThread;
        if (running != null && running.isAlive()) {
            host.showInAppToast("正在导出，等这一张写完");
            if (onDone != null) onDone.run();
            return;
        }
        // **Which clip, decided on the caller's thread.** `currentFrame` is a
        // field the UI thread moves; reading it inside the export thread would
        // pair this still with the next frame's clip.
        final String liveClipPath = liveEdge > 0 ? host.liveClipForCurrentFrame() : null;
        final long liveTsUs = (liveClipPath != null
                && host.currentFrame >= 0 && host.currentFrame < host.frames.size())
                ? host.frames.get(host.currentFrame).liveTsUs : 0L;
        final boolean[] liveDone = {false};
        // **What the canvas is showing is the source, not a print.** Exporting
        // it has to write that same picture: a session would develop the film,
        // which is exactly the side the user is not looking at -- and with no
        // film picked there was no session to open, so the button answered
        // "先选一张胶片，再导出" over a photograph that was already finished.
        if ((host.showingOriginal || host.filmStock == null) && !host.batchExportRunning) {
            host.exportOriginal(format, onDone);
            return;
        }
        // **The export's exposure, decided once, before any branch.** The open
        // params, the forced delta and the pin all have to agree: the canvas'
        // meter flag, the canvas' EV, and only the *user's* offset in
        // `exposure_compensation_ev` (see `applyParamsTo`).
        final String exportMeter = host.sanitizeMeter(host.meterMethod);
        final boolean exportAutoExposure = !exportMeter.isEmpty();
        // Only the frame the canvas last measured may hand its EV over; a batch
        // walks the strip without rendering, so the previous frame's EV would
        // otherwise be pinned onto this one.
        final boolean pinEv = exportAutoExposure && host.appliedEvBelongsToCurrentFrame();
        // A live frame is 800 px and this is about to build a 4175 px one;
        // letting the two queue behind the same lock buys nothing.
        host.cancelLivePreview();
        final boolean tiff = "tiff".equals(format);
        // **The same picture, un-encoded.** A linear TIFF is what a scanner would hand over: the
        // render's own light, tagged linear sRGB, so Lightroom's highlight recovery works on the
        // shoulder itself instead of on a transfer curve that already shaped it once.
        final boolean tiffLinear = "tifflin".equals(format);
        final boolean dng = "dng".equals(format);
        // **Ultra HDR is a JPEG**, not a fifth container: the same file every
        // gallery already opens, plus a gain map beside it. So it shares the
        // JPEG's extension, MIME type and self-check -- only the writer differs.
        final boolean uhdr = "uhdr".equals(format);
        final String ext = dng ? ".dng" : (tiff || tiffLinear) ? ".tif" : ".jpg";
        final String mime = dng ? "image/x-adobe-dng" : (tiff || tiffLinear) ? "image/tiff" : "image/jpeg";
        if (host.sharePill != null) host.sharePill.setVisibility(View.GONE);
        if (host.engine == null || host.session == null) {
            host.showInAppToast("请先导入照片");
            if (onDone != null) onDone.run();
            return;
        }
        if (host.filmStock == null) {
            host.showInAppToast("先选一张胶片，再导出");
            if (onDone != null) onDone.run();
            return;
        }
        // The shelf queue prints 800 px cards through this same session, and a
        // card that lands between "start export" and the first strip swaps
        // film_stock under the full-size render. That -- not the params delta --
        // is what used to come back as a flat grey.
        host.stopPrefetch();
        new Thread(() -> {
            host.exportThread = Thread.currentThread();
            java.io.File tmp = new java.io.File(host.getCacheDir(), "export_" + System.currentTimeMillis() + ext);
            try {
                host.runOnUiThread(() -> {
                    host.statusText.setText("导出中：准备全尺寸渲染（分条）...");
                    host.showBusy("EXPORTING");
                    host.setStep(3);
                });
                // A stock delta before an export is fine -- measured, not
                // assumed (`full_after_delta`: mean 104.2 std 11945 before,
                // 102.4 std 12481 after, and the two hashes differ). It is the
                // same delta the preview renders with, which is the point.
                try {
                    DebugSink.text("export-params", "film=" + host.filmStock + " print=" + host.printStock
                            + " pex=" + host.printExposure + " (no delta sent)");
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37020", ignored); }

                // **The numbers the export is about to spend, before it spends
                // them.** Which frame, which cap, whether the rebate is on, and
                // what the phone says is free -- the four things that decide
                // whether the next two minutes fit.
                try {
                    // The crop travels with the plan: a "garbled once" report is
                    // almost always geometry, and the one number that explains it
                    // is the window the render was asked for.
                    DebugSink.textNow("export-crop", String.format(java.util.Locale.US,
                            "x=%.4f y=%.4f w=%.4f h=%.4f turns=%d rot=%.2f",
                            host.geometryCropX, host.geometryCropY, host.geometryCropW, host.geometryCropH,
                            host.geometryTurns, host.geometryRotation));
                    DebugSink.textNow("export-plan", "src=" + host.sourceWidth + "x" + host.sourceHeight
                            + " importCap=" + host.importCapPixels + " frameCap=" + host.maxImportPixels()
                            + " overscan=" + host.overscanOn + " tier=" + tier
                            + " avail=" + (host.availableMemoryBytes() >> 20) + "MiB");
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37038", ignored); }
                final long t0 = host.nowMs();
                final java.util.concurrent.atomic.AtomicBoolean spinning =
                        new java.util.concurrent.atomic.AtomicBoolean(true);
                final java.util.concurrent.atomic.AtomicLong lastMem =
                        new java.util.concurrent.atomic.AtomicLong(-5);
                final android.os.Handler ticker = new android.os.Handler(android.os.Looper.getMainLooper());
                Runnable tick = new Runnable() {
                    @Override public void run() {
                        if (!spinning.get()) return;
                        long el = (host.nowMs() - t0) / 1000;
                        host.statusText.setText("导出中（" + tier + "）：渲染中 " + el + " 秒...");
                        // **What the export is holding, every five seconds.**
                        // A black screen leaves no stack and no exception -- the
                        // last line before the process dies is the only evidence
                        // there is. `textNow` posts synchronously because the
                        // queue does not survive the crash this exists to
                        // explain, and the line carries the pool's peak together
                        // with the system's own free-memory figure so "the
                        // engine asked for too much" and "the phone had too
                        // little" stay distinguishable.
                        if (el % 5 == 0 && lastMem.getAndSet(el) != el) {
                            try {
                                final long avail = host.availableMemoryBytes();
                                DebugSink.textNow("export-mem", el + "s avail=" + (avail >> 20)
                                        + "MiB " + (host.engine == null ? "no engine" : host.engine.memoryReport()));
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37064", ignored); }
                        }
                        ticker.postDelayed(this, 1000);
                    }
                };
                // The memory probe must not live on the UI thread: the export
                // blocks it, which is why the 12-second crash left no samples.
                Thread memTicker = new Thread(() -> {
                    while (spinning.get()) {
                        try {
                            final long el = (host.nowMs() - t0) / 1000;
                            final String line = el + "s avail="
                                    + (host.availableMemoryBytes() >> 20) + "MiB "
                                    + (host.engine == null ? "no engine" : host.engine.memoryReport());
                            // **On disk first.** `textNow` needs the network; a
                            // phone in flight mode -- or a process killed at the
                            // moment it mattered -- loses the sample entirely,
                            // which is why the 18-second export crash came back
                            // with none. The ring is a SharedPreferences write
                            // and survives the kill; the report picks it up.
                            CrashTrail.markNow(host, "export-mem " + line);
                            DebugSink.textNow("export-mem", line);
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37086", ignored); }
                        try { Thread.sleep(5000); } catch (InterruptedException ie) { break; }
                    }
                }, "SpektraLab-export-memory");
                memTicker.setDaemon(true);
                memTicker.start();
                host.runOnUiThread(tick);
                // **Which session the retry may borrow.** Read once here, on the
                // export thread, and consumed: a stale `true` must not silently
                // make every later export render on the preview's session.
                final boolean reuseCanvas = host.exportReuseCanvas;
                host.exportReuseCanvas = false;
                try {
                    DebugSink.textNow("export", "session: " + (reuseCanvas
                            ? "the canvas' own (second attempt)" : "a fresh one")
                            + " tier=" + tier + " " + format);
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37102", ignored); }
                // A full-size export does not need the preview bitmaps resident.
                // Keep the frame and its Look; these two are only the display.
                final Bitmap savedBefore = host.beforeBitmap;
                final Bitmap savedAfter = host.lastRenderedBitmap;
                host.runOnUiThread(() -> {
                    host.beforeBitmap = null;
                    host.lastRenderedBitmap = null;
                    if (host.compare != null) host.compare.clear();
                });
                int[] info;
                try {
                    synchronized (host.renderMutex) {
                        CrashTrail.markNow(host, "export:lock");
                        // **Give the pool back before the export asks for more.**
                        // The preview that has just been rendered leaves its
                        // working set pooled, and a 40 MP export then wants the
                        // whole thing again. Measured on the owner's phone: the
                        // export dies at 153 s with the launcher behind it.
                        try {
                            final long freed = host.engine.trimPool();
                            DebugSink.text("export", "pre-export trim freed " + (freed >> 20)
                                    + " MiB; report " + host.engine.memoryReport());
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37125", ignored); }
                        // **A session of its own for the export.** The preview
                        // session has already rasterised the rebate's masks at
                        // the preview's millimetres per pixel. Rendering that
                        // same session at "full" rebuilds the *layout* -- the
                        // geometry it reports is right -- but the rasterised
                        // edge-print masks are still the preview's, so the film
                        // edge lands at the wrong scale: the top band prints
                        // twice and the second copy sits on the picture. That
                        // is "the preview is fine and the export is flying".
                        // A fresh session's first render *is* the export tier,
                        // so its masks can only be the export's size.
                        // **Give the preview's planes back for the duration.**
                        // At 41 MP the preview session holds 373 MiB of source
                        // that the export session is about to want, and closing
                        // it would lose the look being exported. Suspending
                        // writes those planes to the cache directory and drops
                        // the device copies; `resume` puts them back. Refused
                        // when there is no scratch directory, which is a no-op
                        // rather than a failure.
                        final SpektraLabEngine.Session keep = host.session;
                        boolean keepSuspended = false;
                        boolean keepClosed = false;
                        // **A batch closes this session a moment later anyway.**
                        // Suspending writes a frame's worth of planes to the
                        // cache directory and resuming allocates them again --
                        // once per frame, for a session that is about to die.
                        if (keep != null && !host.batchExportRunning) {
                            try {
                                keepSuspended = keep.suspend();
                                DebugSink.text("export", "preview suspended=" + keepSuspended
                                        + (keepSuspended ? "" : " why=" + SpektraLabEngine.Session.lastError())
                                        + "; report " + host.engine.memoryReport());
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37158", ignored); }
                        }
                        CrashTrail.markNow(host, "export:suspend " + keepSuspended);
                        // **Free the preview before the export asks for its own
                        // frame.** The close further down runs *after* `fresh`
                        // exists, so the peak held two source planes at once --
                        // 234 MB each on the 19.5 MP Mali-G76 report that died
                        // inside the driver (SL-261111-204F: `suspend=false`,
                        // then `raw-session fresh`, then the driver faulted).
                        // Same decision, same threshold, taken one step earlier.
                        // **The export's own size, not the session's.** With
                        // 「按原图」 the session is the working size and the
                        // export re-decodes at the file's, which can be ten
                        // times what this heuristic was weighing.
                        long needPixels = (long) host.sourceWidth * host.sourceHeight;
                        final int[] fileSize = host.exportAtNative && host.sourceUri != null
                                ? host.sniffImageSize(host.sourceUri) : null;
                        if (fileSize != null && fileSize[0] > 0 && fileSize[1] > 0)
                            needPixels = (long) fileSize[0] * fileSize[1];
                        final long exportNeed = needPixels * 12L * 51L / 10L + (160L << 20);
                        final long exportAvail = host.availableMemoryBytes();
                        if (!reuseCanvas && keep != null && (!keepSuspended
                                || (exportAvail > 0 && exportNeed > exportAvail * 55L / 100L))) {
                            try { keep.close(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37181", ignored); }
                            if (host.session == keep) host.session = null;
                            keepSuspended = false;
                            keepClosed = true;
                            try {
                                DebugSink.text("export", "preview closed before the export"
                                        + " need=" + (exportNeed >> 20)
                                        + " avail=" + (exportAvail >> 20) + " MiB");
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37189", ignored); }
                        }
                        SpektraLabEngine.Session fresh = null;
                        // **No area cap here.** This read `<= 30 MP`, a leftover
                        // from when `sourcePixels` was a `ByteBuffer.allocateDirect`
                        // capped by the heap growth limit. A 44 MP JPEG fell through
                        // it, had no `rawFilePath` either, and so exported on the
                        // *preview* session: `full` on a 2048 px source, which is
                        // both the wrong size and the shape that failed on the
                        // Mali report. The buffer is malloc'd now and the cap is
                        // thirty times smaller than what the phones can hold.
                        if (!reuseCanvas && keep != null
                                && host.sourceWidth > 0 && host.sourceHeight > 0) {
                            // **The export goes back to the file for its size.**
                            // `sourcePixels` carries whatever the import's
                            // memory estimate allowed, and that estimate moves
                            // with free RAM: the same JPEG landed at 42 MP one
                            // minute and 36 MP the next, so two exports of one
                            // photograph came out 7523x5642 and 6990x5242.
                            // Re-decoding at the file's own size is the only
                            // answer that does not depend on what the phone was
                            // doing at import time. If it does not fit, the
                            // engine says which plane it wanted -- an honest
                            // refusal beats a silently smaller file.
                            Bitmap fullBmp = null;
                            long fullBuf = 0;
                            try {
                                final String path = host.sourceUri != null ? host.getFilePathFromUri(host.sourceUri) : null;
                                // **Not `inSampleSize = 1`.** See `decodeForExport`: the
                                // release frame is the largest this phone can develop, not
                                // the largest the file happens to be.
                                if (path != null) {
                                    fullBmp = host.decodeForExport(path, host.exportPixelCap());
                                }
                            } catch (Throwable ignored) { fullBmp = null; }
                            final boolean bigger = fullBmp != null
                                    && (long) fullBmp.getWidth() * fullBmp.getHeight()
                                       > (long) host.sourceWidth * host.sourceHeight;
                            // **A frame with no staging copy comes back from the
                            // file.** Since 1.7.0i the import lets
                            // `sourcePixels` go once the engine has its own;
                            // the export is the only thing that ever wanted it,
                            // and re-decoding at the same size is the same
                            // frame.
                            final boolean fromFile = bigger || (host.sourcePixels == 0 && fullBmp != null);
                            if (fromFile) {
                                final int fw = fullBmp.getWidth(), fh = fullBmp.getHeight();
                                try {
                                    fullBuf = SpektraLabEngine.allocPixels((long) fw * fh * 12L);
                                    host.liftRows(fullBmp, fullBuf, fw, fh);
                                    // **Give the memory back as soon as it is
                                    // copied.** A 200 MP re-decode is 800 MB of
                                    // bitmap, 2.4 GB of float and then the
                                    // engine's own 2.4 GB; releasing each one
                                    // when its successor exists keeps the peak
                                    // at two of the three instead of three.
                                    if (fullBmp != null) { fullBmp.recycle(); fullBmp = null; }
                                    fresh = host.engine.openSession(fullBuf, fw, fh, 3);
                                    SpektraLabEngine.freePixels(fullBuf);
                                    fullBuf = 0;
                                    CrashTrail.markNow(host,
                                            "export:full-size " + fw + "x" + fh
                                                    + " (import was " + host.sourceWidth + "x" + host.sourceHeight + ")");
                                } catch (Throwable e) {
                                    fresh = null;
                                    try { DebugSink.textNow("export-full",
                                            "could not re-decode at " + fw + "x" + fh + ": " + e); }
                                    catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37256", ignored); }
                                } finally {
                                    if (fullBmp != null) fullBmp.recycle();
                                    if (fullBuf != 0) SpektraLabEngine.freePixels(fullBuf);
                                }
                            }
                            // Only when the file really is bigger than the
                            // session: for a frame the working size already
                            // holds whole, the fallback *is* the file's size.
                            if (fresh == null && host.exportAtNative && fileSize != null
                                    && fileSize[0] > 0 && fileSize[1] > 0
                                    && (long) fileSize[0] * fileSize[1]
                                       > (long) host.sourceWidth * host.sourceHeight) {
                                // **Never the working size.** 「按原图」 means the
                                // file's size; falling back to the session here
                                // is "the export came out smaller than the
                                // import" with a new cause. Say what did not fit.
                                throw new IllegalStateException(
                                        "按原图导出要重新读一遍这张图（"
                                                + (fileSize == null ? "文件自身尺寸"
                                                   : fileSize[0] + "×" + fileSize[1])
                                                + "，约 " + (exportNeed >> 20) + " MB），"
                                                + "本机现在给不出来。关掉其它应用再试，"
                                                + "或重新导入时选「缩到…」。");
                            }
                            if (fresh == null && host.sourcePixels != 0) {
                                try { fresh = host.engine.openSession(host.sourcePixels, host.sourceWidth, host.sourceHeight, 3); }
                                catch (Throwable ignored) { fresh = null; }
                            }
                            CrashTrail.markNow(host,
                                    "export:session " + (fresh != null ? "fresh" : "none"));
                        } else if (!reuseCanvas && keep != null && host.rawFilePath != null) {
                            // **No budget gate here.** One was added in 1.4.1t and it
                            // refused frames the engine goes on to render: it compared
                            // the canvas against `maxPixelsByMemory(available)` before
                            // the export had trimmed the preview's pool, and 100 B/px
                            // is the whole-frame figure, not the banded one. Refusing
                            // is not the fix for a large frame -- rendering it is.
                            // A frame that genuinely does not fit fails in the engine
                            // and says which plane it wanted; that message is true,
                            // this one was a guess.
                            final int budgetPixels = host.importedWidth > 0 && host.importedHeight > 0
                                    ? host.importedWidth * host.importedHeight
                                    : (host.importCapPixels > 0 ? host.importCapPixels
                                       : host.sourceWidth * host.sourceHeight);
                            try {
                                // **The export renders the frame that was imported,
                                // never the file's own size.** This passed 0, 0 --
                                // "no cap" -- so a 51 MP negative was decoded a second
                                // time at native size even when the import had landed
                                // at 7 MP, and a rebate canvas is 1.51x that: 77 MP,
                                // 924 MB per plane, several planes. Measured on the
                                // owner's phone, 2026-11-03: a full export after a
                                // preview export of the same frame took the screen
                                // black and the launcher with it.
                                // **The budget the import actually used**, not the
                                // session's dimensions: `getDimensions()` answers the
                                // *source* size for a raw, so the export re-decoded at
                                // 51 MP what the import had settled at 12.8 MP -- and a
                                // rebate canvas is 1.51x that. Measured twice: black
                                // screen, launcher restart, on a 12 GB phone.
                                // The budget is checked above, before the `try`, so a
                                // refusal reaches the user instead of becoming a null
                                // session. Here we only size the decode by it.
                                // **0, 0 -- the file's own size, not the import's.**
                                // This used to pass the budget the import settled
                                // on, which is derived from free RAM and therefore
                                // moves: the same frame exported 42 MP and then
                                // 36 MP without anything changing. An export that
                                // changes size between two presses of the same
                                // button is not a memory strategy, it is a bug.
                                // If the frame genuinely does not fit, the engine
                                // refuses and names the plane -- that message is
                                // true, and it is the one to show.
                                final int exportPixels = 0;
                                final int exportEdge = 0;
                                final float[] wbMul = host.wbUserMul;
                                Probe.log("wb", "export tier=" + tier + " mul=" + Probe.mul(wbMul)
                                        + " px=" + exportPixels + " edge=" + exportEdge);
                                // **What the fresh session is opened with.** The
                                // enlarger exposure has now been sent three ways
                                // (delta, forced delta, open params) and the owner
                                // still sees no change in the file, so the next
                                // report has to say which value actually left the
                                // app. `export-params` reads the session's copy;
                                // this line is the string the engine parsed.
                                try {
                                    DebugSink.text("export-open", "pex=" + host.f("%.6f", host.printExposure)
                                            + " auto=" + exportAutoExposure
                                            + " pin=" + (pinEv ? host.f("%.6f", host.lastAppliedEv) : "none")
                                            + " offset=" + host.f("%.4f", host.cameraEv)
                                            + " paper=" + host.printStock + " raw=" + host.rawFilePath);
                                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37348", ignored); }
                                fresh = wbMul != null
                                        ? host.engine.openRawSessionTuned(host.rawFilePath,
                                                "{\"print_stock\":\"" + host.spkPaperForEngine(host.printStock)
                                                        + "\",\"input_color_space\":\"ProPhoto RGB\","
                                                        + "\"input_cctf_decoding\":false,\"output_color_space\":\"sRGB\","
                                                        // **The meter travels with the export.** This
                                                        // string used to name five fields, so the
                                                        // fresh session fell back to the engine's
                                                        // own defaults: `auto_exposure` true. On a
                                                        // frame whose meter clamps (it read +12 EV
                                                        // here) that put the whole picture at white
                                                        // -- and the rebate, painted afterwards,
                                                        // survived it. The export is the picture on
                                                        // screen, so it has to be told the same
                                                        // thing the preview was told.
                                                        // **Never meter again on export.** The
                                                        // preview's own value, measured once at
                                                        // import; letting the fresh session run
                                                        // the meter a second time is what turned
                                                        // 0.0756 into a clamped +12 and pushed
                                                        // the picture to white.
                                                        + "\"auto_exposure\":" + host.b(exportAutoExposure) + ","
                                                        + "\"exposure_compensation_ev\":" + host.f("%.6f", host.cameraEv) + ","
                                                        // **Open with the enlarger exposure too.** It is
                                                        // a live PRINT field: sent as a delta the engine
                                                        // updates the session's copy while the render
                                                        // keeps the pipeline's, so the file came back as
                                                        // if the slider had never moved (6/6 exports at
                                                        // 0.41, `export-params` in D1). Opening with it
                                                        // puts it on the pipeline from the first frame.
                                                        + "\"print_exposure\":" + host.f("%.6f", host.printExposure) + ","
                                                        + "\"preview_long_edge\":2048}",
                                                exportPixels, exportEdge, wbMul)
                                        : host.engine.openRawSession(host.rawFilePath,
                                                "{\"print_stock\":\"" + host.spkPaperForEngine(host.printStock)
                                                        + "\",\"input_color_space\":\"ProPhoto RGB\","
                                                        + "\"input_cctf_decoding\":false,\"output_color_space\":\"sRGB\","
                                                        // **The meter travels with the export.** This
                                                        // string used to name five fields, so the
                                                        // fresh session fell back to the engine's
                                                        // own defaults: `auto_exposure` true. On a
                                                        // frame whose meter clamps (it read +12 EV
                                                        // here) that put the whole picture at white
                                                        // -- and the rebate, painted afterwards,
                                                        // survived it. The export is the picture on
                                                        // screen, so it has to be told the same
                                                        // thing the preview was told.
                                                        // **Never meter again on export.** The
                                                        // preview's own value, measured once at
                                                        // import; letting the fresh session run
                                                        // the meter a second time is what turned
                                                        // 0.0756 into a clamped +12 and pushed
                                                        // the picture to white.
                                                        + "\"auto_exposure\":" + host.b(exportAutoExposure) + ","
                                                        + "\"exposure_compensation_ev\":" + host.f("%.6f", host.cameraEv) + ","
                                                        // **Open with the enlarger exposure too.** It is
                                                        // a live PRINT field: sent as a delta the engine
                                                        // updates the session's copy while the render
                                                        // keeps the pipeline's, so the file came back as
                                                        // if the slider had never moved (6/6 exports at
                                                        // 0.41, `export-params` in D1). Opening with it
                                                        // puts it on the pipeline from the first frame.
                                                        + "\"print_exposure\":" + host.f("%.6f", host.printExposure) + ","
                                                        + "\"preview_long_edge\":2048}",
                                                exportPixels, exportEdge);
                            } catch (Throwable ignored) { fresh = null; }
                            CrashTrail.markNow(host,
                                    "export:raw-session " + (fresh != null ? "fresh" : "none"));
                        }
                        // **Never swap the global session for an export.**
                        // The export runs on its own local object; assigning it
                        // to `session` let a concurrent UI call write the next
                        // preview's parameters into the export's frame.
                        final SpektraLabEngine.Session exportSession =
                                fresh != null ? fresh : (keepClosed ? null : keep);
                        // **Two working sets do not fit on the phones that fail.**
                        //
                        // `suspend` snapshots the preview's planes to the cache
                        // directory and drops the device copies -- but it can
                        // fail, and when it does this code used to carry on and
                        // open the export session anyway: one whole frame
                        // resident plus another being allocated. Measured on a
                        // 26 MP export (Adreno 650): `preview suspended=false`,
                        // then VkResult -2.
                        //
                        // So: if the suspend did not happen, or the room it left
                        // is still not enough for the export's own working set,
                        // close the preview outright. The frame comes back in the
                        // `finally` below -- a few seconds of re-import traded for
                        // an export that exists.
                        // **A batch used to keep the preview session alive on the
                        // theory that it is closed a frame later.** It is not:
                        // the batch loop reopens the *next* frame, which closes
                        // this one only after the export has already needed the
                        // room. Two frames resident through the whole render is
                        // what the LOW_MEMORY reports show (`s963` and `s964`,
                        // 288 MB of source each) -- so close it here for a batch
                        // too, and let the loop's reopen re-import it.
                        if (fresh != null && keep != null && !keepClosed) {
                            final boolean wasSuspended = keepSuspended;
                            // 5.1 planes of 12 B/px plus the encoder's tables,
                            // measured on the host at 20.9 MP (`mem_export`).
                            final long needBytes = (long) host.sourceWidth * host.sourceHeight * 12L * 51L / 10L
                                    + (160L << 20);
                            // **More than half of what is left is more than the
                            // two of them can share.** `avail` is MemAvailable,
                            // so it already excludes the preview's own arena;
                            // this asks whether one export working set still
                            // fits in what remains after it.
                            final long avail = host.availableMemoryBytes();
                            final boolean tight = avail > 0 && needBytes > avail * 55L / 100L;
                            if (!wasSuspended || tight) {
                                try { keep.close(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37461", ignored); }
                                if (host.session == keep) host.session = null;
                                keepSuspended = false;
                                keepClosed = true;
                                try {
                                    DebugSink.text("export", "preview closed for the export"
                                            + " suspended=" + wasSuspended
                                            + " need=" + (needBytes >> 20)
                                            + " avail=" + (avail >> 20) + " MiB");
                                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37470", ignored); }
                            }
                        }
                        if (exportSession == null) {
                            // The frame's session went away between `export:lock`
                            // and here. Saying so is the honest end of this
                            // frame; dereferencing it was a null pointer.
                            try {
                                DebugSink.text("export-error", "tier=" + tier + " no session for the export");
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37479", ignored); }
                            host.runOnUiThread(() -> {
                                host.showInAppToast("这张照片还没准备好，跳过");
                                if (onDone != null) onDone.run();
                            });
                            return;
                        }
                        if (fresh != null) {
                            try { DebugSink.text("export", "fresh session for tier=" + tier); }
                            catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37488", ignored); }
                        } else if (keepSuspended && keep != null) {
                            // **The export is about to run on the session we just
                            // suspended.** Every branch that builds a fresh session is
                            // wrapped in `catch (Throwable) { fresh = null; }`, so a
                            // refused budget, a raw path the picker never gave us, or a
                            // decode failure all land here -- and `suspend()` has already
                            // dropped the preview's `source` plane while leaving its
                            // dimensions. `tier_image` then hands the pipeline an Image
                            // with a shape and no buffer: `affine: input plane has no
                            // buffer (7387x4952x3)`, on a frame that previews perfectly.
                            // Give the planes back before the export asks for them.
                            try { keep.resume(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37500", ignored); }
                            keepSuspended = false;
                            try { DebugSink.text("export", "no fresh session; preview resumed"); }
                            catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37503", ignored); }
                        }
                        try {
                            // Everything the preview drew with, re-stated: a
                            // stock, a paper, a gate, an edge text or a date
                            // that moved since the last render would otherwise
                            // be exported as the *preview* had it, or not at all.
                            // Re-state the complete Look on the export object,
                            // without routing it through the global `session`.
                            // **Exports are sRGB, whatever the canvas is set to.**
                            // The JPEG and TIFF writers carry an sRGB ICC; a P3
                            // image with an sRGB profile would be read as the
                            // wrong colours by everything that believes it.
                            // **Force.** Diffed against the canvas session this
                            // was a no-op, so every export kept the parameters
                            // its session was opened with -- the enlarger
                            // exposure among them.
                            host.applyParamsTo(exportSession, host.filmStock,
                                    host.printStock == null ? "kodak_portra_endura" : host.printStock,
                                    host.PREVIEW_LONG_EDGE, "sRGB", true);
                            // **Same frame, same EV, same node.** Pinning puts
                            // the meter's gain back in `film_prefix`, where the
                            // canvas applied it -- not after the highlight
                            // rolloff, where an export used to put it. When the
                            // EV belongs to another frame (a batch walking the
                            // strip) clearing is the honest state: the meter is
                            // on and its own whole-frame measurement is exactly
                            // what the canvas would have got.
                            try {
                                if (pinEv) {
                                    exportSession.pinAutoExposureEv(host.lastAppliedEv);
                                } else if (exportAutoExposure) {
                                    // **A batch walks the strip without rendering**,
                                    // so the canvas' measured EV belongs to the
                                    // frame before this one and cannot be pinned
                                    // here. Letting the fresh session meter on its
                                    // own was the other half of "导出只有片边、内容
                                    // 一片白": the same meter that occasionally
                                    // clamps on a full-size decode. Measure once,
                                    // explicitly, and pin that answer -- then
                                    // clamp the pathological tail, because a meter
                                    // that cannot see the frame returns the end of
                                    // its range and paints the whole negative
                                    // white.
                                    double ev = Double.NaN;
                                    try {
                                        final org.json.JSONObject solved =
                                                new org.json.JSONObject(exportSession.solve("both"));
                                        final org.json.JSONObject by =
                                                solved.optJSONObject("exposure_ev_by_method");
                                        final String m = exportMeter.isEmpty() ? "balanced" : exportMeter;
                                        if (by != null && by.has(m)) ev = by.optDouble(m, Double.NaN);
                                    } catch (Throwable ignored2) { ev = Double.NaN; }
                                    final boolean sane = !Double.isNaN(ev) && Math.abs(ev) <= 8.0;
                                    if (!sane) ev = host.lastAppliedEvValid ? host.lastAppliedEv : 0.0;
                                    exportSession.pinAutoExposureEv(ev);
                                    try {
                                        DebugSink.text("export-meter", "method=" + exportMeter
                                                + " solved=" + host.f("%.4f", ev)
                                                + (sane ? "" : " (clamped)")
                                                + " sameFrame=" + host.lastAppliedEvValid);
                                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37564", ignored); }
                                } else {
                                    exportSession.clearPinAutoExposureEv();
                                }
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37568", ignored); }
                            // **Which object got the parameters.** The owner's
                            // files come out at print_exposure 1.0 while this app
                            // sends 0.41 -- on the same frame the host answers
                            // 21410 vs 11996, so the engine is fine and the
                            // parameter is fine. Either this session is not the
                            // one that renders, or something replaces its value
                            // between here and the write. These two lines answer
                            // that with identity, not inference.
                            try {
                                final String gp = exportSession.getParams();
                                int j = gp.indexOf("\"print_exposure\"");
                                DebugSink.text("export-ident", "sent hash=" + System.identityHashCode(exportSession)
                                        + " fresh=" + (fresh != null) + " keep=" + (keep != null)
                                        + " " + (j >= 0 ? gp.substring(j, Math.min(gp.length(), j + 34)) : "no-pex"));
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37583", ignored); }
                            try {
                                DebugSink.text("export-exposure", "tier=" + tier
                                        + " auto=" + exportAutoExposure
                                        + " method=" + (exportMeter.isEmpty() ? "balanced" : exportMeter)
                                        + " pin=" + (pinEv ? host.f("%.4f", host.lastAppliedEv) + " same-frame" : "none")
                                        + " seenEv=" + host.f("%.4f", host.lastAppliedEv)
                                        + (host.lastAppliedEvValid ? "" : " (none)")
                                        + " solveEv=" + host.f("%.4f", host.meteredEv)
                                        + " offset=" + host.f("%.2f", host.cameraEv));
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37593", ignored); }
                            try {
                                DebugSink.text("overscan-geom", "tier=" + tier + " before "
                                        + exportSession.overscanGeometry());
                                DebugSink.text("export-params", "tier=" + tier + " " + exportSession.getParams());
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37598", ignored); }
                            // **Metadata goes in with the pixels.** The writer
                            // appends one APP1 segment and then the ICC profile;
                            // nothing reopens the finished file afterwards.
                            try {
                                if (host.rawFilePath != null && host.session != null) {
                                    final String[] meta = host.sourceExifForExport();
                                    SpektraLabEngine.setExportExif(meta[0], meta[1], meta[2],
                                            meta[3], meta[4], meta[5], meta[6]);
                                    DebugSink.text("exif", "raw source metadata: "
                                            + meta[0] + " / " + meta[1] + " / " + meta[2]
                                            + " / " + meta[3] + "s / f" + meta[4]
                                            + " / ISO" + meta[5] + " / " + meta[6] + "mm");
                                } else if (host.sourceUri != null) {
                                    final String[] meta = host.jpegSourceExif();
                                    SpektraLabEngine.setExportExif(meta[0], meta[1], meta[2],
                                            meta[3], meta[4], meta[5], meta[6]);
                                }
                            } catch (Throwable metaError) {
                                DebugSink.text("exif", "export metadata skipped: " + metaError);
                            }
                            // **The export's own black box.** This call runs the
                            // whole film and print chain at `full` and then
                            // encodes it; on a 51 MP frame it is a minute and a
                            // half of native code with no Java frame anywhere in
                            // it. The ticker makes "it died somewhere" into "it
                            // died after 74 seconds".
                            try {
                                final String gp = exportSession.getParams();
                                int j = gp.indexOf("\"print_exposure\"");
                                DebugSink.text("export-ident", "call hash=" + System.identityHashCode(exportSession)
                                        + " " + (j >= 0 ? gp.substring(j, Math.min(gp.length(), j + 34)) : "no-pex"));
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37630", ignored); }
                            CrashTrail.markNow(host, "export:write " + tier + " " + format);
                            Thread wtick = host.startTick("export", exportSession);
                            try {
                                info = dng
                                        ? exportSession.exportDng(tmp.getAbsolutePath(), tier)
                                        : tiffLinear
                                        ? exportSession.exportTiffLinear(tmp.getAbsolutePath(), tier)
                                        : tiff
                                        ? exportSession.exportTiff(tmp.getAbsolutePath(), tier)
                                        : uhdr
                                        ? exportSession.exportUltraHdr(tmp.getAbsolutePath(), 95, 512, tier)
                                        : exportSession.exportJpeg(tmp.getAbsolutePath(), 95, 512, tier);
                            } finally {
                                host.stopTick(wtick);
                            }
                            CrashTrail.markNow(host, "export:written " + info[0] + "x" + info[1]);
                            try {
                                DebugSink.text("overscan-geom", "tier=" + tier + " " + info[0] + "x" + info[1]
                                        + " " + exportSession.overscanGeometry());
                            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37650", ignored); }
                        } finally {
                            if (fresh != null) {
                                try { fresh.close(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37653", ignored); }
                                // **The export's pool goes back before the
                                // preview's planes come back.** `resume`
                                // allocates persistent buffers, and they cannot
                                // come out of an arena the export is still
                                // sitting in -- the whole reason the preview was
                                // suspended is that the two do not fit at once.
                                try { host.engine.trimPool(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37660", ignored); }
                            }
                            if (keepSuspended && keep != null) {
                                try { keep.resume(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37663", ignored); }
                            }
                            final boolean closedForExport = keepClosed && !host.batchExportRunning;
                            if (closedForExport) {
                                // The preview session was closed to make room for
                                // the export, so the canvas has nothing behind it.
                                // Re-open the same frame the way a chip does.
                                host.runOnUiThread(() -> {
                                    if (!host.uiReady) return;
                                    if (host.currentFrame < 0 || host.currentFrame >= host.frames.size()) return;
                                    CrashTrail.mark(host, "export:reopen after close");
                                    host.reopenFrameForced(host.currentFrame);
                                });
                            }
                        }
                        // **Hand the pool back before anything else runs.** A
                        // 51 MP full render leaves its whole working set
                        // pooled, and the pool only returns it when the *last*
                        // session closes or a different frame is opened --
                        // neither of which a reused preview session ever does.
                        // Measured: without this the launcher and SystemUI were
                        // evicted and the phone froze for about a minute after
                        // an export that had itself succeeded.
                        try {
                            final long freed = host.engine.trimPool();
                            DebugSink.text("export", "pool trim freed "
                                    + (freed >> 20) + " MiB; report " + host.engine.memoryReport());
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37690", ignored); }
                    }
                } finally {
                    spinning.set(false);
                    try { memTicker.interrupt(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37694", ignored); }
                    host.runOnUiThread(() -> {
                        if (host.beforeBitmap == null) host.beforeBitmap = savedBefore;
                        if (host.lastRenderedBitmap == null) host.lastRenderedBitmap = savedAfter;
                        if (host.compare != null) {
                            host.compare.setBefore(host.beforeBitmap);
                            host.compare.setAfter(host.lastRenderedBitmap);
                        }
                    });
                }
                // **The still is finished; now the clip that plays behind it.**
                // One file goes to the album, so this happens before the write,
                // not after.
                if (liveClipPath != null) {
                    host.runOnUiThread(() -> host.statusText.setText("导出中：动图逐帧冲洗..."));
                    final java.io.File withClip = host.attachLiveClip(tmp, liveClipPath, liveTsUs, liveEdge);
                    if (withClip != null) {
                        final java.io.File stillOnly = tmp;
                        tmp = withClip;
                        stillOnly.delete();
                        liveDone[0] = true;
                    }
                }
                host.uploadExportProgress(tmp);
                host.uploadVkLog();
                int w = info[0], h = info[1];
                long renderMs = info[2];
                if (w <= 0 || h <= 0 || !tmp.exists() || tmp.length() == 0) {
                    throw new RuntimeException("引擎没有输出图像");
                }

                // (The capture fields are written by the JPEG encoder itself,
                // handed over just above. This used to reopen the finished file
                // and save attributes through the platform class; its guard read
                // `"jpg".equals(ext)` against an `ext` of ".jpg", so for a whole
                // release no export carried any of it -- silently.)
                final int fw = w, fh = h;
                host.runOnUiThread(() -> host.statusText.setText("导出中：写入相册 " + fw + "x" + fh + "..."));
                // **A negative says so in its name.** The engine already exports
                // one (`negative` / `cineon-log`), and once it is a file in the
                // album the only thing that separates it from the positives is
                // what we call it -- 观片台 lists the folder, not the medium.
                final boolean negMedium = "negative".equals(host.printStock)
                        || "cineon-log".equals(host.printStock);
                String fileName = "SpektraLab_" + (negMedium ? "NEG_" : "")
                        + System.currentTimeMillis() + ext;
                final Uri outUri = host.writeExportFile(tmp, fileName, ext, mime);
                host.lastExportOutUri = outUri;
                // **Does the file look like the picture we think we wrote?**
                // Two measurements of the smoothed result: the mean difference
                // between neighbouring rows and between neighbouring columns.
                // A photograph has both (their ratio sits near 1); a frame
                // stretched along one axis does not. Cheap enough for every
                // export, and the only evidence a "garbled, once, never again"
                // report can leave behind.
                host.exportCorruptNow = false;
                // **Neither a TIFF nor a DNG can be decoded here** (see
                // `inspectContainer`): both checks below take a different route
                // for them, and both blocks need to know which one this is.
                final boolean noPixels = tiff || tiffLinear || dng;
                {
                    // The canvas is only *this* frame's while a batch is not
                    // walking the strip behind our back.
                    // **`BitmapFactory` cannot read a 16-bit TIFF.** Every TIFF
                    // export therefore ended in `export-selfcheck | decode
                    // failed` -- a checker that never checked, for years. Those
                    // two formats are asked a different question below: is the
                    // container intact and does it carry the pixels the header
                    // promises. A truncated write is the failure they actually
                    // suffer from.
                    final double ncc = (host.batchExportRunning || noPixels) ? -1
                            : host.exportMatchesCanvas(tmp.getAbsolutePath(), host.lastRenderedBitmap);
                    // **The measurement that actually catches this.** A frame
                    // replaced by row-to-row noise survives every check that
                    // averages: at 48 columns the noise is flat grey, so a
                    // corrupt export and a clean one correlate just as well on
                    // structure. The first difference does not average -- a
                    // photograph measures about 0.9 (vertical/horizontal), the
                    // reports measure 6 to 999 -- so it is asked separately and
                    // either one failing condemns the file.
                    final int stripes = info.length > 7 ? info[7] : -1;
                    if ((ncc >= 0 && ncc < 0.55) || stripes >= 3500) {
                        host.exportCorruptNow = true;
                        try {
                            DebugSink.textNow("export-corrupt",
                                    String.format(java.util.Locale.US,
                                        "canvas NCC %.3f rows %d at %dx%d tier=%s overscan=%s — retrying",
                                        ncc, stripes, w, h, tier, host.overscanOn)
                                        + "\n" + host.rowSignature(tmp.getAbsolutePath())
                                        + "\n" + host.reportHead());
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37784", ignored); }
                    } else if (ncc >= 0) {
                        try { DebugSink.text("export-selfcheck", String.format(
                                java.util.Locale.US, "canvas NCC %.3f rows %d", ncc, stripes)); }
                        catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37788", ignored); }
                    }
                }
                {
                    final String verdict = host.exportCorruptNow ? null
                            : (noPixels ? host.inspectContainer(tmp.getAbsolutePath(), w, h)
                                        : host.inspectExportedFile(tmp.getAbsolutePath()));
                    if (verdict != null) {
                        try { DebugSink.textNow("export-selfcheck", verdict); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37796", ignored); }
                        // **Say it out loud.** This measurement has existed since
                        // the "garbled, once, never again" report, but it only
                        // ever reached the status bar -- and the field reports
                        // that followed all say the same thing: the user found
                        // out from the gallery. The 26.7 MP export behind
                        // SL-261110-F51B was 616 KB (0.023 B/px against a
                        // normal 0.5-1) and the app called it a success.
                        // The file stays -- the render cost minutes -- but it
                        // is named as suspect and the user is told to try again.
                        host.runOnUiThread(() -> {
                            if (host.statusText != null) host.statusText.setText("导出结构异常：" + verdict);
                            host.showInAppToast("这张导出结构异常（可能花屏），请再导一次");
                        });
                    }
                }
                // **And the one measurement the edge probe already made.**
                // It only ever reached a file nobody reads, which is why a
                // "column of bars on the left edge" report could not be tied to
                // a side, a spacing or a canvas size. One upload, only when the
                // answer is not null: a clean export stays silent.
                {
                    final String ruler = host.edgeRulerVerdict(tmp.getAbsolutePath() + ".prog");
                    if (ruler != null) {
                        // **Synchronous.** This runs on the export worker, not
                        // the UI thread, and the whole point is that the report
                        // survives the user switching away mid-export -- the
                        // queued path is exactly what lost the last three.
                        try {
                            DebugSink.textNow("edge-ruler", ruler + "\n" + host.reportHead());
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37826", ignored); }
                        host.runOnUiThread(() -> {
                            if (host.statusText != null) host.statusText.setText("边缘检测：" + ruler);
                        });
                    }
                }
                try {
                    long len = tmp.length();
                    DebugSink.text("export", "format=" + format + " tier=" + tier + " w=" + w + " h=" + h + " renderMs=" + renderMs
                            + (info.length > 4 ? " mean16=" + info[4] : "")
                            + (info.length > 6 ? " pipelinePe=" + (info[5] / 1000.0)
                                    + " pipelineGain=" + (info[6] / 1000.0) : "")
                            + (info.length > 8 ? " uhdrMap=" + info[8] : "")
                            + " bytes=" + len);
                    if (len > 0 && len < 30L * 1024 * 1024) {
                        byte[] buf = new byte[(int) len];
                        try (java.io.InputStream in = new java.io.FileInputStream(tmp)) {
                            int off = 0, n;
                            while (off < buf.length && (n = in.read(buf, off, buf.length - off)) > 0) off += n;
                        }
                        DebugSink.bytes("export", "out-" + tier + ext, buf, mime);
                    }
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37848", ignored); }
                final long secs = Math.max(1, (host.nowMs() - t0) / 1000);
                final long fRender = renderMs;
                // **Did the file get its map?** A paper with no shoulder inside
                // the ramp has nothing to give back, and the writer says so
                // rather than inventing a gain.
                final boolean uhdrMap = info.length > 8 && info[8] != 0;
                final String uhdrNote = !uhdr ? ""
                        : (uhdrMap ? " · Ultra HDR" : " · 这张相纸没有可还原的高光肩部，存的是普通 JPEG");
                final long bytes = tmp.exists() ? tmp.length() : 0;
                final String sizeLabel = bytes > 0
                        ? String.format("%.1f MB", bytes / 1048576.0) : "?";
                host.runOnUiThread(() -> {
                    host.lastExportUri = outUri;
                    host.lastExportMime = mime;
                    // A separate control, and only when it has something to do:
                    // every other format opens where it lands, and a permanent
                    // share button on a photograph app reads as "post to
                    // social", which is not what this is.
                    if (host.sharePill != null) host.sharePill.setVisibility((tiff || dng) ? View.VISIBLE : View.GONE);
                    // Dimensions and file size together: "3.3 MB" alone reads as
                    // a bug when the frame is 16 MP, and the reason is worth
                    // saying out loud rather than guessing at later.
                    final String canvas = host.overscanCanvasLabel();
                    final String canvasNote = canvas.isEmpty() ? "" : " · 画布 " + canvas;
                    final String tiffHint = tiff
                            ? "（TIFF：点导出旁边的「分享」发给手机版 Lightroom 即可使用）"
                            : dng ? "（DNG：线性 16 位，给电脑上的 Lightroom / ACR；手机版打不开，已知问题，不必反馈）" : "";
                    final String liveNote = liveDone[0] ? " · 实况（含动图）"
                            : (liveClipPath != null ? " · 动图未写入" : "");
                    if (!host.exportCorruptNow)
                        host.showInAppToast("已保存 " + fw + "x" + fh + canvasNote + " · " + sizeLabel
                                + liveNote + uhdrNote + tiffHint);
                    else
                        host.showInAppToast("导出内容异常，正在自动重导一次…");
                    host.statusText.setText("已导出 " + fw + "x" + fh + canvasNote + " · " + sizeLabel
                        + "（" + secs + " 秒，其中渲染 " + (fRender / 1000.0) + " 秒）" + tiffHint);
                    host.hideBusy();
                    host.setStep(2);
                });
            } catch (Throwable e) {
                final String msg = host.describe(e);
                try { DebugSink.text("export-error", "tier=" + tier + " " + msg); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37890", ignored); }
                // **The gate is the one failure worth retrying blind.** The
                // engine names the frame it was handed, so re-picking from that
                // number is a real answer rather than a guess; everything else
                // (a plane that does not fit, a lost device) fails the same way
                // a second time and should just be reported.
                if (host.exportHeals < 2 && host.healCanvasFromError(msg)) {
                    ++host.exportHeals;
                    final String f2 = format, t2 = tier;
                    host.runOnUiThread(() -> {
                        if (host.exportProgressBar != null) host.hideBusy();
                        host.statusText.setText("片边太大，已关闭，重试导出…");
                        host.exportImage(f2, t2, onDone);
                    });
                    return;
                }
                if (host.exportHeals < 2 && host.healGateFromError(msg)) {
                    ++host.exportHeals;
                    final String f2 = format, t2 = tier;
                    host.runOnUiThread(() -> {
                        if (host.exportProgressBar != null) host.hideBusy();
                        host.statusText.setText("按片门比例重试导出…");
                        host.exportImage(f2, t2, onDone);
                    });
                    return;
                }
                host.uploadExportProgress(tmp);
                host.uploadVkLog();
                host.runOnUiThread(() -> {
                    host.statusText.setText("导出失败 " + SpkError.shortForm(msg));
                    host.showErrorDialog("导出失败", SpkError.format(msg)
                            + "\n\n" + host.sourceWidth + "×" + host.sourceHeight
                            + "\n" + host.probe.describe());
                    host.hideBusy();
                    host.setStep(2);
                });
                final int code = SpkError.codeOf(msg);
                if (code == 1001 || code == 1009) host.offerSmallerImport();
            } finally {
                if (tmp.exists()) tmp.delete();
                // **An export that came back as noise is not an export.**
                // The render cost fifteen seconds; a silent bad file costs the
                // user a trip to the gallery and a bug report. Retry once, on
                // the UI thread, and let the second attempt report.
                if (host.exportCorruptNow && !host.exportRetried && !host.batchExportRunning) {
                    host.exportRetried = true;
                    // **The bad file goes before the second attempt.** It is
                    // already committed to the album by `writeExportFile`, and
                    // this retry used to leave it there -- so one export ended
                    // as two files, one of them corrupt. That is the whole of
                    // "一口气导出了两张重复文件".
                    try {
                        final Uri bad = host.lastExportOutUri;
                        host.lastExportOutUri = null;
                        if (bad != null) host.getContentResolver().delete(bad, null, null);
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37945", ignored); }
                    // **A deterministic failure is not retried the same way.**
                    // SL-261118-C2FF rendered the same 26 MP frame four times
                    // and got byte-identical output each time (`bytes=4194721`,
                    // `mean16=14040`, `canvas NCC 0.025`), so repeating the
                    // identical call cannot succeed. The canvas on screen was
                    // drawn by a *different* session and was fine -- it is the
                    // one thing in the process holding a correct render of this
                    // frame -- so the second attempt borrows it. With the rebate
                    // on, that session's edge masks are rasterised at the
                    // preview's millimetres per pixel, so the borrow is refused
                    // there and the old behaviour stands.
                    // Only when there is still a preview session to borrow: if
                    // the first attempt had to close it for room, the borrow
                    // would find nothing and the retry would refuse itself.
                    host.exportReuseCanvas = !host.overscanOn && host.session != null;
                    // **A retry nobody can see is indistinguishable from a slow
                    // render.** This one changes which session draws the file,
                    // and that is the whole difference between the two attempts
                    // -- so it goes in the ledger, named.
                    SelfHeal.repaired("export-corrupt", host.exportReuseCanvas
                            ? "second attempt on the canvas' own session"
                            : "second attempt on a fresh session (nothing to borrow)");
                    host.runOnUiThread(() -> {
                        if (host.statusText != null) host.statusText.setText(host.exportReuseCanvas
                                ? "导出内容异常，换一条路重导一次…"
                                : "导出内容异常，自动重导一次…");
                        host.loadAlbum();
                        host.exportImage(format, tier, onDone);
                    });
                    return;
                }
                if (host.exportCorruptNow) {
                    // **A file we know is wrong does not stay in the gallery.**
                    // Telling the user was not enough: the bad file is already
                    // in 相册 by the time this runs, and the next thing they see
                    // is a striped picture they paid two minutes of rendering
                    // for. Delete this attempt and say what happened.
                    try {
                        final Uri bad = host.lastExportOutUri;
                        if (bad != null) host.getContentResolver().delete(bad, null, null);
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:37986", ignored); }
                    // **The wall must forget it too.** The file is gone, but
                    // `album` still holds the entry and its bitmap, so the cell
                    // stayed on 观片台 as a black rectangle with no way to
                    // remove it -- the whole of SL-261118-7167's first
                    // complaint. A rescan is what drops it.
                    host.runOnUiThread(host::loadAlbum);
                    final String ref = DebugSink.REF;
                    host.runOnUiThread(() -> host.showErrorDialog("这次导出没有成功",
                            "写出来的画面是坏的：整幅几乎同一个颜色，或整幅逐行噪声。"
                            + "不是你的图的问题。\n\n"
                            + "已经换了一条渲染路径重试，还是这样，所以这次没有把文件留在相册里。"
                            + "换一张、或者换一支胶片再回来，通常就正常了；"
                            + "如果每次都是同一张出问题，把记录号 " + ref + " 发我。"));
                }
                if (onDone != null) host.runOnUiThread(onDone);
            }
        }).start();
        }
}
