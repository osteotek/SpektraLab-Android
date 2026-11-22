package app.spektralab;
import app.spektralab.ui.CompareView;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import app.spektralab.engine.SpektraLabEngine;

import java.nio.ByteBuffer;
import java.util.List;

/** Worker re-extracted from the 1.7.0 Activity; owns the full current path. */
final class ImportWorker extends HostWorker {
    private final ImportWorkerHost host;

    ImportWorker(ImportWorkerHost host) { this.host = host; }

    void run(Uri uri, int reopenIndex) {

        CrashTrail.mark(host.context(), "import:load " + uri);
        // **The plate waits for the decode, not for the picker.** It used to go
        // up here, before the two questions this method can ask -- the size
        // sheet and "this file will not open" -- and both of those `return`.
        // Nothing took the plate down, so the wave sat over a canvas with no
        // photograph and the 选择照片 card underneath it: "导入选项不显示",
        // reported with a 59 MP re-import of one of our own exports
        // (SL-261118-F773). A RAW raises its own, in `loadRawImage`.
        // A render of the previous photograph must not land on this one, and
        // the file path of the previous photograph is not this one's.
        host.setFotuSourcePath(null);
        host.setFotuSourceDead(null);
        host.setFotuGeneration(host.getFotuGeneration() + 1);
        // Whatever is rendering belongs to the photograph being replaced.
        host.setRenderEpoch(host.getRenderEpoch() + 1);
        // **The previous photograph's dimensions are not this one's.**
        // `originalAspect()` prefers `importedWidth` when it is set, and only
        // the RAW path ever set it. So a RAW followed by a JPEG kept the RAW's
        // shape: on a 6912x4530 raw then a 1280x1708 jpeg, `refreshEdgeFormats`
        // logged `aspect=1.5258` for the *portrait* frame it had just imported,
        // picked the 135 gate, and the render then refused it with
        // "a 1280 x 1708 frame is not the 135 gate's shape" -- E-1305.
        host.setImportedWidth(0);
        host.setImportedHeight(0);
        // A profile belongs to one file. The RAW branch below attaches this
        // frame's; a JPEG has none, and the canvas falls back to plain sRGB.
        host.setSourceToneCurve(null);
        // **The previous frame's gate is not this frame's shape.** A 6x17 chosen
        // for a panorama stayed selected for the next picture, and the render
        // then refused a 1365x1024 frame with "is not the 120_6x17 gate's
        // shape". `refreshEdgeFormats` re-picks from the new aspect a few lines
        // later, so this only clears the stale choice.
        host.setOverscanFormat("135");
        // **The one number that separates the two hypotheses.** A death inside
        // the decode leaves nothing but `import:load` behind, and at that point
        // "this file is corrupt" and "this file is 50 MP and the kernel
        // reclaimed us" look identical. The header costs a few milliseconds and
        // answers it before anything large is allocated.
        try {
            final int[] sz = host.sniffImageSizeHostCall(uri);
            final long want = sz == null ? 0L : (long) sz[0] * sz[1] * 12L;
            CrashTrail.mark(host.context(), "import:size "
                    + (sz == null ? "unknown" : sz[0] + "x" + sz[1])
                    + " need=" + (want >> 20) + "MB"
                    + " avail=" + (host.availableMemoryBytesHostCall() >> 20) + "MB");
            try { DebugSink.text("import-size", (sz == null ? "unknown" : sz[0] + "x" + sz[1])
                    + " need=" + (want >> 20) + "MB avail=" + (host.availableMemoryBytesHostCall() >> 20) + "MB"
                    + " native=" + host.getImportAtNativeSize()); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:28732", ignored); }
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:28733", ignored); }
        // A new photograph starts at the two entry cards, never on a page
        // whose switch belonged to the frame before it.
        CrashTrail.mark(host.context(), "import:page");
        if (host.getEdgePageIndex() >= 0) host.openEdgePageHostCall(-1);
        // A frame the strip remembers but the system will not open any more:
        // say so in one sentence instead of letting a SecurityException be
        // classified as [E-9000] 未归类.
        if (!host.canReadUriHostCall(uri)) {
            // **A revoked grant is permanent, so the chip goes with it.**
            // Keeping it meant this dialog on every launch -- sixty-six of them
            // in a week, the largest single error in the database -- and the
            // only way out was 「关闭」, which changed nothing. Removed once,
            // here, and the strip is honest afterwards.
            final int dead = host.getCurrentFrame();
            final boolean ownsIt = dead >= 0 && dead < host.getFrames().size()
                    && host.getFrames().get(dead).uri.equals(uri);
            if (ownsIt) {
                // **The chip stays, and the way out is one tap.** Removing it
                // on the spot threw away the look with the grant: the file is
                // still on the phone, it is the *permission* that expired.
                host.showActionSheetHostCall("照片已不可访问",
                        "系统收回了文件选择器给的临时授权，这张照片现在读不到了。\n\n"
                        + "「重新选择这个文件」会把它复制到应用自己的目录——胶片、构图、片边和日期都会保留；"
                        + "「从照片栏移除」只去掉这一条，文件本身不动。",
                        new String[]{"重新选择这个文件", "从照片栏移除", "取消"}, which -> {
                            if (which == 0) { host.setRepickFrame(dead); host.pickRawFileHostCall(); }
                            else if (which == 1) host.removeFrameHostCall(dead);
                        });
            } else {
                host.showErrorDialogHostCall("照片已不可访问",
                        "这张照片的读取权限已经失效——系统收回了文件选择器给的临时授权。\n\n"
                        + "点「导入」重新选择这张照片；新版本会把文件复制到应用自己的目录，"
                        + "之后切到别的软件回来也不会丢。");
            }
            return;
        }
        CrashTrail.mark(host.context(), "import:canread");
        host.setSourceUri(uri);
        // A different file means a different sensor balance; the previous
        // frame's gains must not be handed to it.
        host.setWbUserMul(null);
        // The frame's own look, stated before any session is built: a chip
        // returns to a photograph *and* to what was done to it; a pick from ＋
        // starts with no film at all.
        if (reopenIndex >= 0 && reopenIndex < host.getFrames().size()) {
            host.applyLookHostCall(host.getFrames().get(reopenIndex).look);
        } else {
            // **A reopen with no frame to reopen is a new photograph.** This
            // used to leave the global fields untouched when the index was
            // gone (every chip deleted, then ＋), so the new frame inherited
            // the previous one's print exposure and meter switch -- reported
            // as "重新导入一张还是黑灰", gone after a restart for the obvious
            // reason.
            host.applyLookHostCall(host.defaultLookHostCall());
        }
        CrashTrail.mark(host.context(), "import:look");
        try {
            // Check if this is a RAW file by extension
            String fileName = host.getFileNameHostCall(uri);
            CrashTrail.markNow(host.context(), "import:name " + fileName);
            final String lower = fileName == null ? "" : fileName.toLowerCase();
            host.setIsRawFile(lower.endsWith(".rw2") || lower.endsWith(".raw")
                    || lower.endsWith(".dng") || lower.endsWith(".arw")
                    || lower.endsWith(".nef") || lower.endsWith(".cr2")
                    || lower.endsWith(".cr3") || lower.endsWith(".orf")
                    || lower.endsWith(".raf") || lower.endsWith(".pef")
                    || lower.endsWith(".srw") || lower.endsWith(".x3f")
                    || lower.endsWith(".rwl") || lower.endsWith(".3fr")
                    || lower.endsWith(".mrw") || lower.endsWith(".erf"));
            // A RAW container we know of but cannot open still beats decoding
            // its bytes as a JPEG and reporting a mystery.
            final String[] knownButUnsupported = {".heic.raw", ".iiq", ".mos"};
            for (String ext : knownButUnsupported) {
                if (lower.endsWith(ext)) {
                    host.hideBusyHostCall();
                    host.showInAppToastHostCall("[E-1107] 暂不支持 " + ext + " 格式");
                    return;
                }
            }
            
            if (host.getIsRawFile()) {
                host.loadRawImageHostCall(uri, reopenIndex);
                return;
            }

            // **Ask before shrinking.** Importing at the negative's own size is
            // the point of a 45 MP camera; a silent downscale is the app making
            // that decision on the user's behalf, and it shows up later as an
            // export that is smaller than the file. The header gives the size
            // without decoding anything, so the question is free.
            // **Ask before shrinking.** The header gives the size without
            // decoding anything, so the question is free -- and the answer
            // belongs to the user, not to a memory estimate.
            if (!host.getImportAtNativeSize() && !host.getImportBudgetAccepted() && !host.getSuppressBudgetDialog()) {
                final int[] full = host.sniffImageSizeHostCall(uri);
                if (full != null) {
                    final long area = (long) full[0] * (long) full[1];
                    final int cap = host.maxImportPixelsHostCall();
                    if (area > cap || Math.max(full[0], full[1]) > host.getIMPORT_LONG_EDGE()) {
                        // A question is not an import: whatever was on the canvas
                        // stays, and no plate is left behind.
                        host.hideBusyHostCall();
                        host.showImportBudgetDialogHostCall(uri, full[0], full[1], cap, reopenIndex);
                        return;
                    }
                }
            }
            host.setImportBudgetAccepted(false);

            // Retire the shelf queue *before* the session it is holding goes
            // away. Its generation is the only thing that makes a card which
            // is already rendering drop its bitmap instead of filing the old
            // photograph's picture under this photograph's films: without this
            // bump the queue wrote last frame's thumbnails into `filmThumbs`,
            // every card then looked right in the map (`containsKey`), and the
            // new queue skipped them forever -- cards showing the previous
            // photo until each one was tapped.
            host.stopPrefetchHostCall();
            // Free the previous frame before decoding: a 20 MP pick wants a
            // ~200 MB direct buffer and the old session still holds its planes
            // (this OOM was 241 MB with 201 MB left in the heap).
            //
            // **Under the render lock.** `stopPrefetch` only bumps a generation
            // -- it does not wait for the thread, which may be inside
            // `renderBegin` on this very session. Closing it from under the GPU
            // is the SIGSEGV this device logs as `vkCmdCopyBuffer` against a
            // destroyed buffer, and it is what tapping an earlier thumbnail
            // while the shelf was still drawing looked like.
            synchronized (host.getRenderMutex()) {
                if (host.getSession() != null) { host.getSession().close(); host.setSession(null); }
            }
            // **The old frame's planes go before the new one asks for its own.**
            // `spk_open` allocates the decoded source while the previous
            // session's pool is still resident. On a 16 GB phone a 32 MP pool
            // plus a 50 MP frame is more than the app's cgroup will hand out,
            // and it surfaces as `cannot allocate Vulkan result pixels` ->
            // E-1003 even though the frame alone would have fit. The export
            // path already trims for exactly this reason.
            try { if (host.getEngine() != null) host.getEngine().trimPool(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:28872", ignored); }
            host.setLastRenderedBitmap(null);
            host.refreshPanelLocksHostCall();
            // The plate on the canvas belongs to the frame we just left.
            host.clearBusyPlateHostCall();
            host.setDateText(null);
            host.setStageHostCall(0, null);
            // The white-balance half is RAW-only; tell it which file this is.
            host.refreshViewPanelHostCall();
            host.setSourceBitmap(null);
            // `allocDirect` has no cleaner: dropping the field frees nothing.
            if (host.getSourcePixels() != 0) { SpektraLabEngine.freePixels(host.getSourcePixels()); host.setSourcePixels(0); }
            // The previous file's unpacked plane is not this frame's problem.
            try { SpektraLabEngine.clearRawCache(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:28885", ignored); }
            host.clearShelfCacheHostCall();
            // The canvas is not a control. `setOriginalVisual` only paints the
            // 原图 button; what CompareView holds is `after` (the last render)
            // and it survives a new session. That was the whole "I changed the
            // photo and the canvas still shows the old one" report: the frame
            // was never dropped, so the next render was the first thing to
            // replace it.
            if (host.getCompare() != null) host.getCompare().clear();
            System.gc();

            // Cap by area, the way the RAW path does. inSampleSize only halves,
            // so one exact scale follows if the sample still overshoots.
            final boolean nativeSize = host.getImportAtNativeSize();
            host.setImportAtNativeSize(false);                       // one import only
            // **「按原图」 is about the export, not about what the engine
            // holds.** It used to lift every ceiling here, so a 200 MP file
            // opened a 200 MP session: 12 B/px of float is 2.4 GB, beside an
            // 800 MB ARGB bitmap, for a canvas that renders at 2048 and a
            // negative cache that holds the frame again. That is the 4.9-6.5 GB
            // low-memory kills in 1.7.0's first four hours, and a five-second
            // main-thread stall on the way in.
            //
            // The session now takes the device's own budget, and
            // `exportAtNative` sends the export back to the file for its real
            // size -- `exportImage` already re-decodes and opens a fresh
            // session for exactly that (the `bigger` branch). Same promise,
            // a tenth of the frame.
            host.setExportAtNative(nativeSize);
            // A cap the last render failure handed us (see offerSmallerImport):
            // one import's worth, then back to the device's own answer.
            final int forcedCap = host.getPendingCapPixels();
            host.setPendingCapPixels(0);
            host.setE1001Offered(false);
            // An explicit "render smaller" from the failure sheet outranks the
            // native default: the user asked for that one.
            final int capPixels = forcedCap > 0 ? forcedCap : host.maxImportPixelsHostCall();
            final int edgeCap = host.getIMPORT_LONG_EDGE();
            // The cleanup above (shelf queue, session close, gc) is its own
            // stretch; without this mark a death there would still be reported
            // as `import:look` and blamed on the decoder.
            host.showBusyHostCall("READING");
            CrashTrail.mark(host.context(), "import:decoding");
            host.setSourceBitmap(host.decodeImportHostCall(uri, capPixels, edgeCap));
            if (host.getSourceBitmap() == null) {
                // The old message here was "无法解码图片", which gave the user
                // nothing to act on. A HEIC/HEIF this phone's decoder refuses is
                // the common case, so the dialog carries the way out instead of
                // describing it.
                host.showActionSheetHostCall("[E-1107] 这张图本机解不开",
                        "多半是 HEIC/HEIF 或 10-bit 的图（三星、iPhone 默认存这种）。\n"
                                + "换「从文件选择」再试一次，读不到的话请先另存为 JPEG。",
                        new String[]{"从文件选择", "取消"}, which -> {
                            if (which == 0) host.pickRawFileHostCall();
                        });
                // The decode ran and gave nothing back; the plate does not owe
                // the user a reason to stare at it.
                host.hideBusyHostCall();
                return;
            }
            host.setSourceBitmap(host.applyExifRotationHostCall(uri, host.getSourceBitmap()));
            // **Where the import actually is.** `import:load` covers everything
            // from the pick to the session, which is a fifty-line stretch with
            // a 200 MB bitmap and a 600 MB direct buffer in it; when the system
            // reclaims the process mid-way the trail cannot tell a bad file
            // from a decoder that handed back 50 MP it could not then convert.
            CrashTrail.mark(host.context(), "import:decoded " + host.getSourceBitmap().getWidth() + "x"
                    + host.getSourceBitmap().getHeight());
            final long area = (long) host.getSourceBitmap().getWidth() * host.getSourceBitmap().getHeight();
            final int longEdge = Math.max(host.getSourceBitmap().getWidth(), host.getSourceBitmap().getHeight());
            // Three ceilings in three units, and the strictest wins: area is
            // quadratic in the scale, the long edge is linear, and the third is
            // what this phone can actually hold -- the import's float buffer is
            // 12 B/px, which is 240 MB at 20 MP and the largest single
            // allocation the app makes. Asking before taking it is what turns
            // an OutOfMemoryError (an Error, so the old `catch (Exception)`
            // never saw it and the process died) into a smaller import.
            double k = 1.0;
            // **Nothing is scaled when the user asked for the original.** The
            // memory test below used to run even with `nativeSize`, so "force
            // original" still came back smaller -- the one place where the
            // suggestion was a ceiling in disguise.
            final long availBytes = host.availableMemoryBytesHostCall();
            if (!nativeSize) {
                if (area > capPixels) k = Math.min(k, Math.sqrt((double) capPixels / (double) area));
                if (longEdge > edgeCap) k = Math.min(k, (double) edgeCap / longEdge);
                // **No third ceiling from free RAM.** `capPixels` already came
                // from the pixel budget, and that budget is now derived from
                // total RAM rather than whatever happened to be free. Applying
                // `availMem - 128 MB` *again*, here, is what made two imports of
                // one file land on different sizes. The float buffer is malloc'd
                // and its failure has an honest message (`导入这张图需要 N MB`),
                // so the guess is not needed.
            }
            if (k < 1.0) {
                final int nw = Math.max(1, (int) Math.round(host.getSourceBitmap().getWidth() * k));
                final int nh = Math.max(1, (int) Math.round(host.getSourceBitmap().getHeight() * k));
                Bitmap scaled = Bitmap.createScaledBitmap(host.getSourceBitmap(), nw, nh, true);
                if (scaled != host.getSourceBitmap()) host.getSourceBitmap().recycle();
                host.setSourceBitmap(scaled);
                try { DebugSink.text("import-scale", "k=" + k + " -> " + nw + "x" + nh
                        + " avail=" + availBytes); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:28986", ignored); }
            }
            host.setSourceWidth(host.getSourceBitmap().getWidth());
            host.setSourceHeight(host.getSourceBitmap().getHeight());
            // The JPEG path sets these too, so nothing has to infer a shape from
            // whatever the last raw happened to be.
            host.setImportedWidth(host.getSourceWidth());
            host.setImportedHeight(host.getSourceHeight());
            host.refreshEdgeFormatsHostCall();

            // Convert a row at a time: a full int[] of 20 MP would be another
            // 80 MB on the Java heap next to the direct buffer.
            host.setRawFilePath(null);
            // 12 bytes a pixel, outside the Java heap, and the largest single
            // allocation this app makes: 240 MB at 20 MP, 600 MB at 50. The
            // engine takes float32 only (the Metal zero-copy door is refused by
            // the Vulkan backend), so it cannot be made smaller -- but it can be
            // made *legible*: an OutOfMemoryError here is caught and explained,
            // and only a kernel reclaim leaves the trail without this mark.
            final long pixelBytes = (long) host.getSourceWidth() * host.getSourceHeight() * 3L * 4L;
            CrashTrail.mark(host.context(), "import:pixels " + (pixelBytes >> 20) + "MB avail="
                    + (host.availableMemoryBytesHostCall() >> 20) + "MB");
            try { DebugSink.text("import-pixels", host.getSourceWidth() + "x" + host.getSourceHeight()
                    + " need=" + (pixelBytes >> 20) + "MB avail="
                    + (host.availableMemoryBytesHostCall() >> 20) + "MB"); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29010", ignored); }
            // **malloc, not allocateDirect.** The runtime caps direct-buffer
            // memory at the heap growth limit (512 MB on the phone that
            // reported this), and a 50 MP frame is 576 MB of float32, so the
            // old call was refused with 4.3 GB free -- and the message blamed
            // the device. This one asks for memory that exists.
            try {
                host.setSourcePixels(SpektraLabEngine.allocPixels(pixelBytes));
            } catch (Throwable oom) {
                throw new IllegalStateException("导入这张图需要 " + (pixelBytes >> 20)
                        + " MB 内存，本机现在给不出来（可用 "
                        + (host.availableMemoryBytesHostCall() >> 20) + " MB）。"
                        + "\n\n点「导入」再选一次，这次选「缩到建议尺寸」。", oom);
            }
            // **A band a call, not a row a call.** One row per `getPixels` and
            // per JNI call was 16,320 of each on a 200 MP import, on the UI
            // thread: the five-second freeze behind SL-261121-C907.
            host.liftRowsViaHost(host.getSourceBitmap(), host.getSourcePixels(), host.getSourceWidth(), host.getSourceHeight());
            
            final int pw = Math.max(1, Math.round(host.getSourceWidth() * Math.min(1f, 1024f / Math.max(host.getSourceWidth(), host.getSourceHeight()))));
            final int ph = Math.max(1, Math.round(host.getSourceHeight() * Math.min(1f, 1024f / Math.max(host.getSourceWidth(), host.getSourceHeight()))));
            // **`createScaledBitmap` hands back the source itself when the
            // target size already matches**, and a bitmap straight out of
            // `BitmapFactory` is immutable, so that is the branch every picture
            // whose long edge is at most 1024 takes. The recycle below then left
            // `compare` holding a dead bitmap: the next draw threw
            // "Canvas: trying to use a recycled bitmap", and because the same
            // picture is re-imported by `restoreSessionState` the app died on
            // every launch until its data was cleared.
            Bitmap preview = Bitmap.createScaledBitmap(host.getSourceBitmap(), pw, ph, true);
            if (preview == host.getSourceBitmap()) {
                preview = host.getSourceBitmap().copy(Bitmap.Config.ARGB_8888, false);
                if (preview == null) preview = host.getSourceBitmap();
            }
            host.setBeforeBitmap(preview);
            host.getCompare().setBefore(host.getBeforeBitmap());
            if (host.getSourceBitmap() != host.getBeforeBitmap()) host.getSourceBitmap().recycle();
            host.setSourceBitmap(null);
            host.getStatusText().setText("Local preview");
            host.setStageHostCall(1, host.getFileNameHostCall(uri));
            // **Before `rememberFrame`, while we still know which file it is.**
            // The canvas only ever sees the still; if the clip is not taken now
            // it is gone, and the export silently writes a plain JPEG over a
            // live photo -- which is exactly what every version before this one
            // did.
            final MainActivity.LiveClip live = host.captureLiveClipHostCall(uri);
            host.rememberFrameHostCall(uri, reopenIndex);
            if (live != null && host.getCurrentFrame() >= 0 && host.getCurrentFrame() < host.getFrames().size()) {
                final MainActivity.Frame f = host.getFrames().get(host.getCurrentFrame());
                f.liveClip = live.path;
                f.liveTsUs = live.tsUs;
                host.saveSessionStateHostCall();
            }
            host.rebuildFilmCardsHostCall();
            host.setStepHostCall(1);
            
            // Create engine and session
            CrashTrail.mark(host.context(), "import:engine");
            host.ensureEngineHostCall();
            CrashTrail.mark(host.context(), "import:engine-ok");
            synchronized (host.getRenderMutex()) {
                if (host.getSession() != null) { host.getSession().close(); host.setSession(null); }
            }
            host.setSession(host.getEngine().openSession(host.getSourcePixels(), host.getSourceWidth(), host.getSourceHeight(), 3));
            CrashTrail.mark(host.context(), "import:session");
            host.applySessionParamsHostCall();
            // **The engine has its own copy; this one is 12 B/px of dead
            // weight.** 432 MB at 36 MP, held for the whole life of the
            // session, and the only reader left was the export's last-resort
            // fallback -- which now re-decodes from the file instead. This is
            // the largest single allocation the app makes and the only one it
            // can give back without touching a pixel; the low-memory killer
            // took the process at 2.7 GB with a 36 MP frame open.
            //
            // Small frames keep it: below 4 MP it is 48 MB, and an export that
            // can no longer read its file still has the buffer that way.
            if (host.getSession() != null && host.getSourcePixels() != 0
                    && (long) host.getSourceWidth() * host.getSourceHeight() > 4_000_000L) {
                SpektraLabEngine.freePixels(host.getSourcePixels());
                host.setSourcePixels(0);
                CrashTrail.mark(host.context(), "import:staging freed");
            }
            host.setLastImportError(null);
            // An import is not a look. The frame lands unrendered with no film
            // chosen; the first render happens when the user picks a stock.
            host.setShowingOriginal(true);
            host.setLastRenderedBitmap(null);
            host.setOriginalVisualHostCall(true);
            host.rebuildFilmCardsHostCall();
            final boolean stayOnOriginal = host.getReopenToOriginal();
            host.setReopenToOriginal(false);
            if (host.getFilmStock() != null && stayOnOriginal) {
                // **Left on 原图, opens on 原图.** The look is still there -- the
                // film card is what the next tap develops -- but re-rendering
                // here is what made a chip jump back onto a film the user had
                // deliberately stepped off.
                host.setShowingOriginal(true);
                host.setOriginalVisualHostCall(true);
                host.updateFilmSelectionHostCall();
                host.getStatusText().setText("ORIGINAL · RAW 原始数据（未调色，选胶片后才成片）");
                if (!host.getBatchExportRunning()) host.startPrefetchHostCall();
            } else if (host.getFilmStock() != null) {
                // A chip came back with a look: develop it again rather than
                // making the user re-pick a film that was never unpicked.
                host.setShowingOriginal(false);
                host.updateFilmSelectionHostCall();
                // **Not while a batch is exporting.** Each frame is opened only
                // to hand it to `exportImage`, which builds its own full-size
                // session; a preview pass here is a second render of the same
                // frame, and it holds `rendering` up long enough that the next
                // parameter touch answers "渲染中". The batch's order is
                // load -> export, with nothing in between.
                if (!host.getBatchExportRunning()) host.renderImageHostCall();
            } else {
                host.getStatusText().setText("未渲染 · 选择胶片开始" + host.importSizeNoteHostCall(host.getSourceWidth(), host.getSourceHeight()));
                // Nothing is going to render, so the plate leaves here.
                host.hideBusyHostCall();
                if (!host.getBatchExportRunning()) host.startPrefetchHostCall();
                // **The one lock that happened before this photo existed.** The
                // panel was greyed at launch with no bitmap; without this the
                // 发光 page would stay grey until something else re-ran the pass.
                host.refreshPanelLocksHostCall();
                // 同一个理由的第二件事：现在有画面了，开着的辉光要落到它上面。
                host.refreshGlowForCurrentViewHostCall();
            }
        } catch (Throwable t) {
            host.hideBusyHostCall();
            host.setLastImportError(host.describeImportFailureHostCall(t));
            host.showErrorDialogHostCall("导入失败", host.getLastImportError());
            try { DebugSink.text("import-failed", host.getLastImportError()); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:29139", ignored); }
        }
        }
}
