package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.MainActivity.AlbumEntry;
import app.spektralab.engine.FotuFilm;
import app.spektralab.ui.*;

/** InfoDialogs builders extracted from MainActivity (architecture Phase C). */
public final class InfoDialogs {
    private InfoDialogs() {}

    public static void showInfoFor(InfoDialogsHost host, final int index) {
        if (host.getLightInfo() == null || index < 0 || index >= host.getAlbum().size()) return;
        final AlbumEntry e = host.getAlbum().get(index);
        if (e.key().equals(host.getLightInfoKey())) return;
        host.setLightInfoKey(e.key());

        final java.io.File f = "file".equals(e.uri.getScheme())
                ? new java.io.File(e.uri.getPath()) : null;
        final String path = f == null ? null : f.getAbsolutePath();

        // ---- the EXIF line: only what is in the file
        final StringBuilder meta = new StringBuilder();
        if (path != null) {
            try {
                final android.media.ExifInterface ex = new android.media.ExifInterface(path);
                final String make = ex.getAttribute(android.media.ExifInterface.TAG_MAKE);
                final String model = ex.getAttribute(android.media.ExifInterface.TAG_MODEL);
                if (model != null && !model.trim().isEmpty()) {
                    String cam = model.trim();
                    if (make != null && !make.trim().isEmpty()
                            && !cam.toLowerCase(java.util.Locale.US)
                            .startsWith(make.trim().toLowerCase(java.util.Locale.US))) {
                        cam = make.trim() + " " + cam;
                    }
                    meta.append(cam);
                }
                final String lens = ex.getAttribute("LensModel");
                if (lens != null && !lens.trim().isEmpty()) {
                    meta.append(meta.length() > 0 ? "  ·  " : "").append(lens.trim());
                }
                final String focal = host.ratioViaHost(ex.getAttribute(
                        android.media.ExifInterface.TAG_FOCAL_LENGTH));
                if (!focal.isEmpty()) {
                    meta.append(meta.length() > 0 ? "  ·  " : "").append(focal).append(" mm");
                }
                final String fnum = host.ratioViaHost(ex.getAttribute(
                        android.media.ExifInterface.TAG_F_NUMBER));
                if (!fnum.isEmpty()) {
                    meta.append(meta.length() > 0 ? "  ·  " : "").append("f/").append(fnum);
                }
                final String shutter = ex.getAttribute(
                        android.media.ExifInterface.TAG_EXPOSURE_TIME);
                if (shutter != null && !shutter.trim().isEmpty()) {
                    final String sv = shutter.trim();
                    final String shown = sv.indexOf('/') > 0 ? sv : host.ratioViaHost(sv) + " s";
                    meta.append(meta.length() > 0 ? "  ·  " : "").append(shown);
                }
                final int iso = ex.getAttributeInt("PhotographicSensitivity", 0);
                if (iso > 0) {
                    meta.append(meta.length() > 0 ? "  ·  " : "").append("ISO ").append(iso);
                }
                final String taken = ex.getAttribute(
                        android.media.ExifInterface.TAG_DATETIME_ORIGINAL);
                if (taken != null && taken.length() >= 10) {
                    meta.append(meta.length() > 0 ? "  ·  " : "")
                            .append(taken.substring(0, 10).replace(':', '.'));
                }
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:11580", ignored); }
        }
        // What the file does not carry, the exporter wrote down while it still
        // knew the frame: the same photograph's own numbers, just not in the JPEG.
        final String rec = host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE)
                .getString("exp_" + e.name, null);
        String film = "", paper = "";
        if (rec != null) {
            final String[] p = rec.split("\\|", -1);
            film = p.length > 0 ? p[0] : "";
            paper = p.length > 1 ? p[1] : "";
            if (meta.length() == 0) {
                final StringBuilder more = new StringBuilder();
                if (p.length > 2 && !p[2].isEmpty()) more.append(p[2]);
                if (p.length > 3 && !p[3].isEmpty()) {
                    more.append(more.length() > 0 ? "  ·  " : "").append(p[3]).append(" mm");
                }
                if (p.length > 4 && !p[4].isEmpty()) {
                    more.append(more.length() > 0 ? "  ·  " : "").append("f/").append(p[4]);
                }
                if (p.length > 5 && !p[5].isEmpty()) {
                    more.append(more.length() > 0 ? "  ·  " : "").append(p[5]);
                }
                if (p.length > 6 && !p[6].isEmpty() && !"0".equals(p[6])) {
                    more.append(more.length() > 0 ? "  ·  " : "").append("ISO ").append(p[6]);
                }
                meta.append(more);
            }
        }
        final String metaText = meta.length() > 0 ? meta.toString() : e.name;

        // ---- the second line: the file's own pixel size, weight, and mtime
        final StringBuilder line = new StringBuilder();
        final int[] dims = host.dimsOfHostCall(path);
        if (dims != null) {
            line.append(dims[0]).append(" × ").append(dims[1]);
        }
        if (f != null && f.isFile()) {
            if (line.length() > 0) line.append("  ·  ");
            line.append(String.format(java.util.Locale.US, "%.1f MB", f.length() / 1048576.0));
            line.append("  ·  ").append(new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm",
                    java.util.Locale.US).format(new java.util.Date(f.lastModified())));
        }
        if (line.length() == 0) line.append(e.name);

        host.getLightInfoMeta().setText(metaText);
        host.getLightInfoFile().setText(line.toString());
        try { host.typeNoteHostCall(host.getLightInfoMeta(), metaText); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:11627", ignored); }

        // ---- the two cards, from what the exporter wrote down
        String filmId = "", paperId = "";
        if (rec != null) {
            final String[] p2 = rec.split("\\|", -1);
            filmId = p2.length > 7 ? p2[7] : "";
            paperId = p2.length > 8 ? p2[8] : "";
        }
        if (filmId.isEmpty()) filmId = host.idFromPrettyHostCall(film, false);
        if (paperId.isEmpty()) paperId = host.idFromPrettyHostCall(paper, true);

        host.getLightFilmValue().setText(film.isEmpty() ? "—" : film);

        host.getLightPaperValue().setText(paper.isEmpty() ? "—" : paper);

        host.markFileViaHost("info: " + e.name + " rec=" + rec);

        host.getLightInfo().setVisibility(View.VISIBLE);
        host.getLightInfo().animate().alpha(1f).setDuration(240).start();
        try { host.componentArrivalHostCall(host.getLightInfo()); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:11647", ignored); }
    }

    public static void showNote(InfoDialogsHost host, final View anchor, final String text) {
        if (host.getNotePopup() != null) {
            try { host.getNotePopup().dismiss(); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:4829", ignored); }
            host.setNotePopup(null);
        }
        final TextView body = host.labelHostCall("", 9, host.getUI_INK(), host.getTfHan());
        body.setLineSpacing(host.dpHostCall(5), 1f);
        final LinearLayout card = new LinearLayout(host.context());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(host.dpHostCall(14), host.dpHostCall(11), host.dpHostCall(14), host.dpHostCall(13));
        final android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(host.getUI_PAPER());
        // **Square.** A rounded card here was the only rounded rectangle in a
        // screen built out of right angles -- the toggle cells, the chips and
        // the panel all meet at 90 degrees.
        bg.setCornerRadius(0f);
        bg.setStroke(host.dpHostCall(1), host.getUI_LINE());
        card.setBackground(bg);
        card.addView(body, new LinearLayout.LayoutParams(-1, -2));
        final int width = Math.min(Math.round(host.context().getResources().getConfiguration().screenWidthDp
                * host.context().getResources().getDisplayMetrics().density * 0.74f), host.dpHostCall(330));
        final android.widget.PopupWindow pw = new android.widget.PopupWindow(card, width,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pw.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        pw.setOutsideTouchable(true);
        try { pw.setElevation(host.dpHostCall(8)); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:4853", ignored); }
        pw.setOnDismissListener(() -> {
            if (host.getNoteAnim() != null) { host.getNoteAnim().cancel(); host.setNoteAnim(null); }
        });
        host.setNotePopup(pw);
        pw.showAsDropDown(anchor, -host.dpHostCall(6), host.dpHostCall(3));
        body.post(() -> host.typeNoteHostCall(body, text));
    }

    public static void showEngineChooser(InfoDialogsHost host, final String spkId, final String fotuId) {
        final List<String> labels = new ArrayList<>();
        labels.add(tr("ui170.091"));
        labels.add(tr("ui170.092"));
        final List<String> notes = new ArrayList<>();
        notes.add(tr("ui170.093"));
        notes.add(tr("ui170.094"));
        String current = null;
        if (!host.getShowingOriginal() && host.cardShowsStockViaHost(spkId, host.getFilmStock())) {
            current = host.isFotuStockViaHost(host.getFilmStock()) ? labels.get(1) : labels.get(0);
        }
        host.showChoiceSheetHostCall(tr("ui170.095"), labels, notes, current, i -> true, picked -> {
            if (labels.get(1).equals(picked)) host.selectFotuStockHostCall(FotuFilm.PREFIX + fotuId);
            else host.selectSpkStockHostCall(spkId, host.getFilmIds().indexOf(spkId));
        });
    }
}
