package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.MainActivity.ClipWriter;
import app.spektralab.engine.SpektraLabEngine;
import app.spektralab.ui.*;

/** LiveClip builders extracted from MainActivity (architecture Phase C). */
public final class LiveClipRenderer {
    private LiveClipRenderer() {}

    public static java.io.File renderLiveClip(LiveClipHost host, java.io.File clip, int maxEdge, java.io.File still) {
        host.setLiveClipPtsUs(-1L);
        android.media.MediaExtractor ex = null;
        android.media.MediaMetadataRetriever mmr = null;
        ClipWriter w = null;
        long pixels = 0;
        long pixelBytes = 0;
        // **The next frame is cut out while the engine draws this one.**
        // `MediaMetadataRetriever` seeks and decodes on whichever thread calls
        // it, and at 4K that is a third of a frame's wall time that needs no GPU
        // at all. One producer, one slot, two sentinels -- the queue cannot hold
        // null, and "the retriever gave nothing" is a frame to skip, not the end.
        Thread feeder = null;
        java.util.concurrent.LinkedBlockingQueue<Object> aheadQ = null;
        final Object kNoFrame = new Object();
        final Object kEndOfClip = new Object();
        try {
            ex = new android.media.MediaExtractor();
            ex.setDataSource(clip.getAbsolutePath());
            int vTrack = -1, aTrack = -1;
            long durationUs = 0;
            float fps = 30f;
            for (int i = 0; i < ex.getTrackCount(); ++i) {
                final android.media.MediaFormat f = ex.getTrackFormat(i);
                final String mime = f.getString(android.media.MediaFormat.KEY_MIME);
                if (mime == null) continue;
                if (mime.startsWith("video/")) {
                    vTrack = i;
                    if (f.containsKey(android.media.MediaFormat.KEY_DURATION)) {
                        durationUs = f.getLong(android.media.MediaFormat.KEY_DURATION);
                    }
                    if (f.containsKey(android.media.MediaFormat.KEY_FRAME_RATE)) {
                        try {
                            fps = f.getInteger(android.media.MediaFormat.KEY_FRAME_RATE);
                        } catch (Throwable notInt) {
                            try { fps = f.getFloat(android.media.MediaFormat.KEY_FRAME_RATE); }
                            catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35277", ignored); }
                        }
                    }
                } else if (mime.startsWith("audio/")) {
                    aTrack = i;
                }
            }
            if (vTrack < 0 || durationUs <= 0) {
                host.reportLiveHostCall("no video track", "tracks=" + ex.getTrackCount() + " dur=" + durationUs);
                return null;
            }
            if (fps < 1f || fps > 240f) fps = 30f;
            // **A frame cap must not shorten the clip.** 60 fps x 3 s wants 181
            // frames; capping at 150 ended the picture half a second before the
            // sound did. Stretch the step over the whole clip instead, and hand
            // the encoder the rate we actually deliver.
            final int wanted = (int) Math.max(1,
                    Math.round(durationUs * (double) fps / 1000000.0));
            final int n = Math.min(150, wanted);
            final long stepUs = wanted > n ? Math.max(1, durationUs / n)
                                           : (long) (1000000.0 / fps);
            final float encFps = Math.min(fps, 1000000f / stepUs);

            mmr = new android.media.MediaMetadataRetriever();
            mmr.setDataSource(clip.getAbsolutePath());

            final android.media.MediaCodec.BufferInfo info = new android.media.MediaCodec.BufferInfo();
            int[] argb = null;
            int[] bulk = null;
            int encW = 0, encH = 0;
            final long clipT0 = host.nowMsViaHost();

            aheadQ = new java.util.concurrent.LinkedBlockingQueue<>(3);
            final java.util.concurrent.LinkedBlockingQueue<Object> q = aheadQ;
            final android.media.MediaMetadataRetriever retriever = mmr;
            final int frameCount = n;
            final long step = stepUs;
            feeder = new Thread(() -> {
                // **The end marker is promised, whatever happens above it.** A
                // producer that dies without it leaves the consumer blocked on
                // `take()` for ever -- an export that never finishes, with no
                // exception anywhere. `finally` and a timed offer close that door.
                try {
                    for (int k = 0; k < frameCount; ++k) {
                        android.graphics.Bitmap b = null;
                        try {
                            b = retriever.getFrameAtTime((long) k * step,
                                    android.media.MediaMetadataRetriever.OPTION_CLOSEST);
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35325", ignored); }
                        try { q.put(b == null ? kNoFrame : b); }
                        catch (InterruptedException stop) { return; }
                    }
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35329", ignored); } finally {
                    try {
                        q.offer(kEndOfClip, 5, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35333", ignored); }
                }
            }, "SpektraLab-live-decode");
            feeder.start();

            for (int i = 0; i < n; ++i) {
                final Object next;
                try {
                    next = q.take();
                } catch (InterruptedException stop) {
                    break;
                }
                if (next == kEndOfClip) break;
                android.graphics.Bitmap bmp = next == kNoFrame ? null : (android.graphics.Bitmap) next;
                if (bmp == null) continue;
                // **The clip carries its own colour, and it is not sRGB.** The
                // sample is tagged primaries=smpte432 (P3), matrix=BT.601,
                // transfer=sRGB -- but the still beside it goes to the engine as
                // `sRGB` + decode, so the clip must be declared the same way or
                // the two halves of one file would not match.
                int fw = bmp.getWidth(), fh = bmp.getHeight();
                final int longEdge = Math.max(fw, fh);
                if (maxEdge > 0 && longEdge > maxEdge) {
                    final float k = maxEdge / (float) longEdge;
                    final int nw = Math.max(2, (Math.round(fw * k) >> 1) << 1);
                    final int nh = Math.max(2, (Math.round(fh * k) >> 1) << 1);
                    final android.graphics.Bitmap sc =
                            android.graphics.Bitmap.createScaledBitmap(bmp, nw, nh, true);
                    if (sc != bmp) { bmp.recycle(); bmp = sc; }
                    fw = bmp.getWidth();
                    fh = bmp.getHeight();
                }
                fw &= ~1;                       // H.264 4:2:0 wants both edges even
                fh &= ~1;
                if (fw < 2 || fh < 2) { bmp.recycle(); continue; }

                final long need = (long) fw * fh * 3L * 4L;
                if (need > pixelBytes) {
                    if (pixels != 0) { SpektraLabEngine.freePixels(pixels); pixels = 0; }
                    pixels = SpektraLabEngine.allocPixels(need);
                    pixelBytes = need;
                }
                // **One call, not two per row.** This walked the bitmap a row at
                // a time -- 1440 `getPixels` calls and 1440 JNI crossings per
                // frame, 2160 of each at 4K -- for a copy the runtime does in one
                // go. `fillRow` already takes a count, so the whole frame is one
                // call with a pixel offset of zero.
                if (bulk == null || bulk.length < fw * fh) bulk = new int[fw * fh];
                bmp.getPixels(bulk, 0, fw, 0, 0, fw, fh);
                bmp.recycle();
                SpektraLabEngine.fillRow(pixels, 0, bulk, fw * fh);

                final SpektraLabEngine.Session live = host.getEngine().openSession(pixels, fw, fh, 3);
                try {
                    // **The same numbers as the still, forced.** `force` clears
                    // the delta ledger so nothing is skipped, exactly as an
                    // export must; the clip is a different session, and a
                    // partial delta there would develop it with the previous
                    // session's film.
                    host.applyParamsToHostCall(live, host.getFilmStock(), host.getPrintStock(), Math.max(fw, fh), "sRGB", true);
                    live.setParams("{\"overscan_active\":false}");   // a rebate is a print's, not a video's
                    final int[] dims = live.renderBegin("full");
                    if (dims == null || dims[0] <= 0 || dims[1] <= 0) {
                        throw new IllegalStateException("renderBegin returned " + java.util.Arrays.toString(dims));
                    }
                    final int rw = dims[0], rh = dims[1];
                    if (argb == null || argb.length < rw * rh) argb = new int[rw * rh];
                    final int copied = live.copyArgbRange(argb, 0, rh);
                    if (copied != rw * rh) {
                        throw new IllegalStateException("short copy " + copied + "/" + (rw * rh));
                    }
                    if (w == null) {
                        encW = rw;
                        encH = rh;
                        w = host.openClipWriterHostCall(rw, rh, encFps, argb, 0L, ex, aTrack, info);
                        if (w == null) return null;          // it reported why
                    } else {
                        if (rw != w.width || rh != w.height) {
                            throw new IllegalStateException("the clip changed size mid-render: "
                                    + rw + "x" + rh + " vs " + w.width + "x" + w.height);
                        }
                        host.queueFrameViaHost(w, argb, (long) i * stepUs, info);
                    }
                } finally {
                    try { live.renderEnd(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35417", ignored); }
                    try { live.close(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35418", ignored); }
                }
                final int done = i + 1;
                if (done == n || (done % 5) == 0) {
                    final int total = n;
                    final long el = host.nowMsViaHost() - clipT0;
                    final long per = el / Math.max(1, done);
                    host.runOnUiThreadHostCall(() -> host.getStatusText().setText(
                            tr("ui170.577") + done + "/" + total + tr("ui170.579")
                                    + per + tr("ui170.580") + (el / 1000) + tr("ui170.458")));
                }
            }
            if (w == null) {
                host.reportLiveHostCall("no frames encoded", "n=" + n + " fps=" + fps);
                return null;
            }
            // **The last frame is the print itself.** The clip's own final frame
            // is a different moment from the still (this camera's key photo sits
            // at 0.3 s of a 0.766 s clip), so playing to the end and cutting to
            // the still is a jump. One extra frame -- decoded from the file we
            // are about to embed it into, downsampled, never re-rendered -- makes
            // the last frame and the still the same picture, and the timestamp we
            // declare points at it.
            int frames = n;
            if (still != null && argb != null) {
                android.graphics.Bitmap big = null;
                try {
                    final android.graphics.BitmapFactory.Options o =
                            new android.graphics.BitmapFactory.Options();
                    o.inSampleSize = 2;                  // 12 MP still -> 3 MP, never 48 MB
                    big = android.graphics.BitmapFactory.decodeFile(
                            still.getAbsolutePath(), o);
                    if (big != null) {
                        android.graphics.Bitmap sized = host.liveTailFrameViaHost(big, encW, encH);
                        if (sized != big) { big.recycle(); big = null; }
                        if (argb.length < encW * encH) argb = new int[encW * encH];
                        sized.getPixels(argb, 0, encW, 0, 0, encW, encH);
                        if (sized != big) sized.recycle();
                        host.queueFrameViaHost(w, argb, (long) n * stepUs, info);
                        frames = n + 1;
                        host.setLiveClipPtsUs((long) n * stepUs);
                    }
                } catch (Throwable t) {
                    try { DebugSink.send("live-fail", "tail frame skipped: " + t); }
                    catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35462", ignored); }
                } finally {
                    if (big != null && !big.isRecycled()) big.recycle();
                }
            }
            final int eos = w.codec.dequeueInputBuffer(20000);
            if (eos >= 0) {
                w.codec.queueInputBuffer(eos, 0, 0, (long) frames * stepUs,
                        android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }
            final long deadline = host.nowMsViaHost() + 30000;
            while (host.nowMsViaHost() < deadline) {
                if (host.drainVideoViaHost(w, info, true)) break;
            }
            // **The bytes only reach disk at `stop()`.** MediaMuxer writes the
            // ftyp up front and holds every sample in memory until then, so a
            // size read before it is a lie -- the first *working* export logged
            // "40 B" and read as a failure. Stop here, and let `finally` skip
            // the second stop.
            try { w.mux.stop(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35481", ignored); }
            w.started = false;
            DebugSink.send("live-ok", "clip redeveloped: " + frames + " frames (tail="
                    + (host.getLiveClipPtsUs() >= 0 ? "print" : "none") + "), "
                    + encW + "x" + encH + ", fmt=" + w.format + " size=" + w.size
                    + ", " + w.file.length() + " B");
            return w.file;
        } catch (Throwable t) {
            host.reportLiveHostCall("clip render failed", String.valueOf(t));
            if (w != null) w.file.delete();
            return null;
        } finally {
            // **The producer goes first.** It is the only other user of the
            // retriever and of its own decoded bitmaps; releasing either under it
            // is a crash in a thread that has no caller to report to.
            if (feeder != null) {
                feeder.interrupt();
                try { feeder.join(1500); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35498", ignored); }
            }
            if (aheadQ != null) {
                Object left;
                while ((left = aheadQ.poll()) != null) {
                    if (left instanceof android.graphics.Bitmap) {
                        try { ((android.graphics.Bitmap) left).recycle(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35504", ignored); }
                    }
                }
            }
            if (pixels != 0) { try { SpektraLabEngine.freePixels(pixels); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35508", ignored); } }
            if (mmr != null) { try { mmr.release(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35509", ignored); } }
            if (ex != null) { try { ex.release(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35510", ignored); } }
            if (w != null) {
                try { w.codec.stop(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35512", ignored); }
                try { w.codec.release(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35513", ignored); }
                try { if (w.started) w.mux.stop(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35514", ignored); }
                try { w.mux.release(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:35515", ignored); }
            }
        }
    }
}
