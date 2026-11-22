package app.spektralab;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import app.spektralab.engine.SpektraLabEngine;

/** Worker extracted from MainActivity; delegates Android/Activity access to host. */
final class BatchExportWorker extends HostWorker {
    BatchExportWorker(MainActivity host) { super(host); }

    void run(String format, String tier) {
        if (host.batchExportRunning) {
            host.showInAppToast(tr("export.batch_export_is_already_running"));
            return;
        }
        if (host.frames.size() < 2) {
            host.showInAppToast(tr("export.only_one_photo_is_available"));
            return;
        }
        host.saveLookIntoFrames();
        final int total = host.frames.size();
        final int home = host.currentFrame;
        host.batchExportRunning = true;
        host.batchExportAbort = false;
        if (host.abortBatchPill != null) host.abortBatchPill.setVisibility(View.VISIBLE);
        // **Stop the shelf that is already running.** `startPrefetch` refuses
        // to start a new one while a batch is going, but the queue the last
        // import kicked off keeps painting twenty-five cards -- and the trail
        // of the crash at `export:write full jpeg` shows it doing exactly that
        // inside the export: twenty-five pipeline rebuilds competing for the
        // same pool with a 12.6 MP render that is about to allocate.
        host.stopPrefetch();
        if (host.sharePill != null) host.sharePill.setVisibility(View.GONE);
        new Thread(() -> {
            int ok = 0, failed = 0;
            try {
            for (int i = 0; i < total; ++i) {
                if (host.batchExportAbort) break;
                final int index = i;
                // **The strip can shrink while the batch runs.** `total` was
                // captured when the loop started; deleting a chip (or the
                // restore path rewriting the strip) leaves fewer frames than
                // that, and `frames.get(i)` then throws
                // `IndexOutOfBoundsException: Index 3 out of bounds for length
                // 3` on the batch thread -- which looks like "批量导出结束了，
                // 然后报错" and loses the remaining frames.
                if (index >= host.frames.size()) { ++failed; continue; }
                final MainActivity.Frame frame = host.frames.get(index);
                final String name = frame.name == null
                        ? tr("export.photo") + (i + 1) + tr("export.suffix") : frame.name;
                if (frame.look == null || frame.look.filmStock == null) {
                    ++failed;               // nothing to print: no film chosen
                    continue;
                }
                UiDispatcher.post(() -> {
                    if (host.statusText != null) {
                        host.statusText.setText(tr("export.batch_export") + (index + 1) + "/" + total + ": " + name);
                    }
                    if (host.exportProgressBar != null) host.exportProgressBar.setVisibility(View.VISIBLE);
                });
                // **State the Look before the decode, every frame.** `loadImage`
                // also applies it, but only while `reopeningFrame` survives the
                // trip; stating it here makes the export independent of that
                // flag. Reported: six frames came back with the film but not
                // the print exposure or the optics, which is what a half-applied
                // Look looks like.
                try {
                    DebugSink.text("batch-look", (index + 1) + "/" + total
                            + " film=" + frame.look.filmStock
                            + " print=" + frame.look.printStock
                            + " exp=" + frame.look.printExposure
                            + " grain=" + frame.look.grainAmount
                            + " hal=" + frame.look.halationAmount
                            + " glare=" + frame.look.glareAmount
                            + " edr=" + frame.look.edrOn
                            + " viewC=" + frame.look.viewContrast);
                } catch (Throwable spkIgnored) { Diagnostics.ignored("get:14331", spkIgnored); }
                host.uiWait(() -> {
                    host.applyLook(frame.look);
                    // **Drop the previous frame's session before asking for
                    // this one.** `loadImage` closes it on its own worker, so
                    // for a moment `session` still held the *old* object -- the
                    // poll below was satisfied by it, the export started, and
                    // the worker closed that session underneath (NPE in
                    // `exportJpeg`, E-9000). Closing it here, under the lock,
                    // makes `session != null` mean this frame and nothing else.
                    host.closeSessionSafely();
                    // **Forced, even for the frame already on screen.** The
                    // shortcut `reopenFrame` takes (`index == currentFrame &&
                    // session != null`) leaves the first export on the session
                    // the import opened, while every later frame is re-decoded.
                    host.reopenFrameForced(index);
                }, 5000);
                if (!host.pollUntil(() -> host.session != null, 180000)) {
                    ++failed;
                    Diagnostics.log("batch-export", "open failed " + index + " " + name, "get:14353");
                    continue;
                }
                final java.util.concurrent.CountDownLatch done =
                        new java.util.concurrent.CountDownLatch(1);
                host.uiWait(() -> host.exportImage(format, tier, done::countDown), 5000);
                boolean finished = false;
                try { finished = done.await(20, java.util.concurrent.TimeUnit.MINUTES); }
                catch (InterruptedException ie) { break; }
                if (finished) ++ok; else ++failed;
                // **Before the next frame asks for its own planes.**
                host.uiWait(() -> {
                    host.closeSessionSafely();
                }, 5000);
                try { if (host.engine != null) host.engine.trimPool(); } catch (Throwable spkIgnored) { Diagnostics.ignored("get:14369", spkIgnored); }
                // **The next frame starts from zero.** The decoded raw cache
                // holds one frame's unpacked plane, and the heap still carries
                // the last frame's bitmaps; six frames of that is what made the
                // fifth one die.
                try { SpektraLabEngine.clearRawCache(); } catch (Throwable spkIgnored) { Diagnostics.ignored("get:14374", spkIgnored); }
                System.gc();
            }
            } finally {
                // **The flag has to come down even when the loop throws**, or
                // every later batch is refused and every error dialog is eaten.
                final int fok = ok, ffail = failed;
                UiDispatcher.post(() -> {
                    host.batchExportRunning = false;
                    if (host.abortBatchPill != null) host.abortBatchPill.setVisibility(View.GONE);
                    if (host.exportProgressBar != null) host.exportProgressBar.setVisibility(View.GONE);
                    final String msg = tr("export.batch_export_finished") + fok + tr("export.succeeded")
                            + (ffail > 0 ? ", " + ffail + tr("export.failed_no_film_selected_or") : "");
                    if (host.statusText != null) host.statusText.setText(msg);
                    host.showInAppToast(msg);
                    if (home >= 0 && home < host.frames.size()) {
                        final int back = home;
                        host.reopenFrame(back);
                        // **The restore has to render.** `loadImage` deliberately
                        // stops at the original, so without this the canvas stayed
                        // on "RENDERING / Decoding RAW…" after a batch that had
                        // already finished.
                        new Thread(() -> {
                            if (!host.pollUntil(() -> host.session != null, 120000)) return;
                            UiDispatcher.post(() -> {
                                // **800, not 2048.** Six full-size exports leave
                                // the pool carrying a gigabyte of lookup tables
                                // that nothing reclaims; the redisplay then asked
                                // for another 20 MP chain and the system killed
                                // the process right there -- two reports, both
                                // "it dies when it goes back to the first frame".
                                // The frame is a preview at this point anyway;
                                // tapping a film re-renders at full size.
                                if (back < host.frames.size() && host.filmStock != null) host.renderImage(host.LIVE_LONG_EDGE);
                            });
                        }, "SpektraLab-batch-restore").start();
                    }
                });
            }
        }, "SpektraLab-batch-export").start();
      }
}
