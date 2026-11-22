package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.DeviceProbe;
import app.spektralab.ui.*;

/** ExportDialogs builders extracted from MainActivity (architecture Phase C). */
public final class ExportDialogs {
    private ExportDialogs() {}

    public static void showExportDialog(ExportDialogsHost host) {
        host.setExportHeals(0);
        if (host.getBatchExportRunning()) {
            // **The only cancel.** A batch can run for ten minutes; without a
            // way out the user's choice is to kill the app.
            host.showActionSheetHostCall(tr("file.batch_export_in_progress"), tr("file.photos_are_exported_one_at"),
                    new String[]{tr("file.stop_batch_export"), tr("file.continue")}, which -> {
                        if (which == 0) host.setBatchExportAbort(true);
                    });
            return;
        }
        if (host.getSession() == null) {
            host.showInAppToastHostCall(tr("film.import_a_photo_first"));
            return;
        }
        final String full = (host.getSourceWidth() > 0 && host.getSourceHeight() > 0)
                ? tr("file.full_size") + host.getSourceWidth() + "×" + host.getSourceHeight() : tr("file.full_size_2");
        // No ceiling to warn about: the export renders the frame that was
        // imported, and that may be the file's own size. What is worth saying
        // is that it takes time, not that it will be refused.
        final String fullNote = tr("file.original_dimensions_large_images_are");
        // What the driver says it can take, as opposed to what this build
        // currently lets through. The two were only ever visible as one number,
        // and it was always the smaller -- so a phone reporting 2 GiB of
        // maxStorageBufferRange looked like it was capped at 15 MP for no
        // stated reason. Name both, and say which one is temporary.
        final String driverLine;
        if (host.getProbe().maxStorageBufferRange > 0) {
            final double driverMp = host.getProbe().maxStorageBufferRange / 12.0 * 0.80 / 1e6;
            final double budgetMp = host.maxImportPixelsHostCall() / 1e6;
            driverLine = String.format(java.util.Locale.US,
                    tr("file.driver_buffer_limit_0f_mib"),
                    host.getProbe().maxStorageBufferRange / 1048576.0, budgetMp);
        } else {
            driverLine = host.getProbe().valid ? tr("file.the_driver_did_not_report") : tr("file.could_not_read_device_information");
        };
        // One sheet, six rows. It was two -- format, then size -- and the first
        // thing that did was hide tr("ui170.303") behind a tap that nobody
        // expects from a menu that used to end in it.
        final boolean limited = host.getProbe().isLimited();
        // "the gallery says it is corrupt" is a question this sheet can answer
        // before the file is written, not after: Android's picture viewer does
        // not read 16-bit TIFF at all, and the phone's Lightroom does.
        // DNG is shipped as a file for a desktop, and says so on the tin. The
        // phone's Lightroom refuses every linear DNG this writer produces --
        // four containers, two photometrics, 600x400 through 3000x2000, a
        // correct XYZ(D50) colour matrix, a camera-shaped IFD0 preview -- with
        // "failed to load the development version", which Adobe lists as a
        // known Android bug. Rather than let that read as our defect, the row
        // and the note name the destination and close the question.
        final String note = host.isFotuStockViaHost(host.getFilmStock())
                ? tr("ui170.304")
                  + "\n" + driverLine
                : tr("ui170.305")
                  + tr("ui170.306")
                  + "\nUltra HDR 是同一张 JPEG 加一份增益图：HDR 屏上高光更亮，"
                  + tr("ui170.307")
                  + tr("ui170.308")
                  + "\n实况照片：成片始终是全尺寸，区别在动图 —— 原生按相机录的像素，"
                  + tr("ui170.309")
                  + "\n要 16 位 TIFF（给 Lightroom 拉高光）或 DNG，进「更多格式」。"
                  + "\n" + driverLine;
        final boolean batchable = host.getFrames().size() > 1;
        final java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        // **Rows and keys, side by side.** The dispatch used to be arithmetic on the
        // index (`which >= 3 ? tiff : jpeg`, `which == 6 ? tifflin`), which only
        // worked while both engines offered the same list. A row now says what it
        // is, and the engine that answers it is named in the key.
        //
        // **One sheet per intent.** This was a single list of eight rows in which
        // `JPEG · 预览画质 · 长边 3400` and `TIFF 16 位 · 预览画质 · 长边 3400`
        // differed by four characters -- and a field report (SL-261118-C2FF) has
        // the app exporting a 16-bit TIFF twice while the owner believes he asked
        // for a JPEG. Whether that was a mis-tap or not, a menu that cannot tell
        // the two apart is our defect: the size rows now come first and name the
        // format in a suffix, and TIFF / DNG are one tap away behind 「更多格式」.
        final java.util.ArrayList<String> keys = new java.util.ArrayList<>();
        if (batchable) {
            rows.add(tr("file.export_all") + host.getFrames().size() + tr("file.photos_full_size_jpeg"));
            keys.add("batch");
        }
        rows.add(tr("ui170.310"));     keys.add("spk|jpeg|preview");
        // **Enable HDR, as a file.** A paper's shoulder compresses the top
        // stops into a fraction of its last stop of reflectance; the gain map
        // gives them back on a screen that can show them, and the picture
        // underneath stays the JPEG above, byte for byte. It needs the first
        // engine's medium ramp, so fotufilm does not offer it.
        // **Not for a negative with no paper.** 「无相纸 · 负片本身」 has no print
        // stage at all, so there is no shoulder to give back -- and the picture
        // is an orange negative. A reversal frame keeps the row: its curve is
        // the film's own scan, which is exactly what upstream's §19.1 does.
        if (!host.isFotuStockViaHost(host.getFilmStock()) && !host.getPAPER_SCAN().equals(host.getPrintStock())) {
            rows.add(tr("ui170.311")); keys.add("spk|uhdr|preview");
            // **The same file at the frame's own pixels.** The gain map is built
            // from the encoded rows either way, so the only difference is the
            // render and the JPEG: ~6 s on a 20 MP frame against ~3 s.
            rows.add(tr("ui170.312"));       keys.add("spk|uhdr|full");
        }
        rows.add(tr("ui170.313"));           keys.add("spk|jpeg|full");
        rows.add(tr("ui170.314"));                keys.add("spk|jpeg|live");
        final String livePath = host.liveClipForCurrentFrameHostCall();
        if (livePath != null) {
            // **Two clips, not two stills.** Both rows write the still at the
            // frame's own pixels -- the moving half is what differs, and the
            // number in the label is the clip's own long edge, read out of the
            // file. A clip that is already 1440 gets one row, because a second
            // one would do exactly the same work under a different name.
            final int clipEdge = host.liveClipLongEdgeHostCall(livePath);
            if (clipEdge > host.getLIVE_CLIP_MAX_EDGE()) {
                rows.add(tr("ui170.315") + host.getLIVE_CLIP_FULL_EDGE() + tr("ui170.316"));
                keys.add("spk|jpeg|full|live|" + host.getLIVE_CLIP_FULL_EDGE());
                rows.add(tr("ui170.315") + host.getLIVE_CLIP_MAX_EDGE() + tr("ui170.317"));
                keys.add("spk|jpeg|full|live|" + host.getLIVE_CLIP_MAX_EDGE());
            } else {
                rows.add(tr("ui170.315")
                        + (clipEdge > 0 ? clipEdge + "p" : tr("ui170.318")) + "）");
                keys.add("spk|jpeg|full|live|" + host.getLIVE_CLIP_FULL_EDGE());
            }
        }
        rows.add(tr("ui170.319"));     keys.add("more");
        host.showActionSheetHostCall(tr("file.export_photo"), note, rows.toArray(new String[0]), which0 -> {
            if (which0 < 0 || which0 >= keys.size()) return;
            final String[] key = keys.get(which0).split("\\|");
            if ("batch".equals(key[0])) { host.startBatchExportHostCall("jpeg", "full"); return; }
            if ("more".equals(key[0])) { showExportFormatSheet(host); return; }
            host.beginExportHostCall(key);
        });
    }

    public static void showExportFormatSheet(ExportDialogsHost host) {
        final java.util.ArrayList<String> rows = new java.util.ArrayList<>();
        final java.util.ArrayList<String> keys = new java.util.ArrayList<>();
        if (host.isFotuStockViaHost(host.getFilmStock())) {
            // The second engine writes linear light and nothing else -- no encoded
            // 16-bit path, because a viewer that cannot read the ICC would show the
            // raw light as a bright, washed picture.
            rows.add(tr("ui170.322")); keys.add("fotu|tifflin|full");
        } else {
            rows.add(tr("ui170.323"));            keys.add("spk|tiff|preview");
            rows.add(tr("file.16_bit_tiff_full_size"));           keys.add("spk|tiff|full");
            rows.add(tr("ui170.324")); keys.add("spk|tifflin|full");
            rows.add(tr("file.dng_full_size_desktop_only"));  keys.add("spk|dng|full");
        }
        host.showActionSheetHostCall(tr("ui170.325"),
                tr("ui170.326")
                        + "DNG 用手机版 Lightroom 打开会报错，那是 Adobe 的已知问题，不必反馈。\n"
                        + tr("ui170.327")
                        + tr("ui170.328"),
                rows.toArray(new String[0]), which -> {
                    if (which < 0 || which >= keys.size()) return;
                    host.beginExportHostCall(keys.get(which).split("\\|"));
                });
    }

    public static void showPixelCapSheet(ExportDialogsHost host) {
        final int[] caps = {0, 20, 30, 40, 50};
        final int[] sims = {0, 128, 256};
        final int[] bands = {0, 16777216, 100000000};
        final int currentCap = host.debugPixelCapMpHostCall();
        final int currentSim = host.debugStorageLimitMibHostCall();
        final int currentBand = host.debugBandCapHostCall();
        final java.util.List<String> labels = new java.util.ArrayList<>();
        for (int v : caps)
            labels.add((v == currentCap ? "· " : "") + (v == 0 ? tr("import.pixel_cap_automatic") : tr("import.pixel_cap") + v + " MP"));
        for (int v : sims)
            labels.add((v == currentSim ? "· " : "") + (v == 0 ? tr("import.simulated_driver_buffer_limit_off") : tr("import.simulated_driver_buffer_limit") + v + " MiB"));
        for (int v : bands)
            labels.add((v == currentBand ? "· " : "") + (v == 0 ? tr("import.band_limit_engine_default_2m")
                    : (v >= 100000000 ? tr("import.band_limit_off_one_band") : tr("import.band_limit") + (v / 1000000) + tr("import.m_elements"))));
        final long memoryCap = DeviceProbe.maxPixelsByMemory(host.availableMemoryBytesHostCall());
        final String note = tr("import.the_pixel_cap_only_affects")
                + String.format(java.util.Locale.US, "%.0f", memoryCap / 1e6)
                + tr("import.mp_and_the_hard_cap")
                + tr("import.restart_the_app_to_apply");
        host.showActionSheetHostCall(tr("settings.debug_pixel_cap_and_buffer"), note, labels.toArray(new String[0]), which -> {
            if (which < caps.length) host.setDebugPixelCapMpHostCall(caps[which]);
            else if (which < caps.length + sims.length) host.setDebugStorageLimitMibHostCall(sims[which - caps.length]);
            else host.setDebugBandCapHostCall(bands[which - caps.length - sims.length]);
            host.showAboutHostCall();
        });
    }
}
