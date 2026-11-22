package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import java.util.ArrayList;
import java.util.List;
import app.spektralab.engine.FotuFilm;
import app.spektralab.ui.SheetDialogs;
import app.spektralab.Probe;

/** PickerSheets builders extracted from MainActivity (architecture Phase C). */
public final class PickerSheets {
    private PickerSheets() {}

    public static void showMeterPicker(PickerSheetsHost host) {
        final java.util.List<String> labels = new java.util.ArrayList<>();
        final java.util.List<String> notes = new java.util.ArrayList<>();
        int at = 0;
        for (int i = 0; i < host.getMETER_IDS().length; ++i) {
            labels.add(host.getMETER_NAMES()[i]);
            notes.add(host.getMETER_NOTES()[i]);
            if (host.getMETER_IDS()[i].equals(host.getMeterMethod() == null ? "" : host.getMeterMethod())) at = i;
        }
        host.showChoiceSheetHostCall(tr("film.metering_mode_sets_overall_exposure"), labels, notes, labels.get(at), null,
                picked -> {
                    final int index = labels.indexOf(picked);
                    if (index >= 0) host.setMeterMethodHostCall(host.getMETER_IDS()[index]);
                });
    }

    public static void showPaperPicker(PickerSheetsHost host) {
        if (host.isFotuStockViaHost(host.getFilmStock())) { showFotuMediumPicker(host); return; }
        if (host.getPrintIds().isEmpty()) return;
        if (host.isPositiveStockHostCall(host.getFilmStock())) {
            host.showInAppToastHostCall(tr("film.slide_film_is_scanned_directly"));
            return;
        }
        // The current paper, by its **label**: this sheet matched the id
        // against a list of names, so nothing was ever marked as selected.
        // **The list plus the one entry that is not a sheet.** 无相纸 lives here
        // because this is where the question "what am I printing on" is asked;
        // it rides the same sheet so the selection reads like every other one.
        final List<String> ids = new ArrayList<>(host.getPrintIds());
        final List<String> names = new ArrayList<>(host.getPrintLabels());
        final List<String> notes = new ArrayList<>();
        for (String id : host.getPrintIds()) notes.add(host.paperLineViaHost(id, host.paperNoteViaHost(id)));
        ids.add(host.getPAPER_SCAN());
        names.add(tr("ui170.100"));
        notes.add(tr("ui170.101"));
        final int current = ids.indexOf(host.getPrintStock());
        final boolean filmKnown = host.getFilmStock() != null;
        final boolean filmMono = host.isMonochromeStockViaHost(host.getFilmStock());
        host.showChoiceSheetHostCall(tr("film.choose_paper_or_print_film"), names, notes,
                current >= 0 && current < names.size() ? names.get(current) : null,
                i -> host.getPAPER_SCAN().equals(ids.get(i))
                        || !filmKnown || host.paperFitsFilmViaHost(ids.get(i), filmMono),
                picked -> {
                    final int i = names.indexOf(picked);
                    if (i < 0 || host.getPrintStock().equals(ids.get(i))) return;
                    host.setPrintStock(ids.get(i));
                    host.refreshPaperLabelHostCall();
                    host.setShowingOriginal(false);
                    host.onStockChangedHostCall();
                });
    }

    public static void showHdrSheet(PickerSheetsHost host) {
        if (!host.hdrDisplaySupportedHostCall()) {
            host.showActionSheetHostCall(tr("ui170.039"),
                    tr("ui170.040")
                    + tr("ui170.041"),
                    new String[]{tr("print.ok")}, done -> { });
            return;
        }
        final float h = host.hdrHeadroomStopsHostCall();
        final String note = String.format(java.util.Locale.US,
                "屏幕余量 %.1f 档。\n"
                + tr("ui170.042")
                + tr("ui170.043"), h, h);
        final String[] names = {
                tr("ui170.044"),
                tr("ui170.045"),
                tr("ui170.046"),
                tr("ui170.047"),
                tr("ui170.048"),
                tr("ui170.049"),
                tr("ui170.050")
        };
        final float[] vals = {-1f, 0f, 0.5f, 0.8f, 1.2f, 1.6f, 2.0f};
        host.showActionSheetHostCall(tr("ui170.051"), note, names, which -> {
            if (which < 0 || which >= vals.length) return;
            if (vals[which] < 0f) {
                host.setHdrPreviewPrefHostCall(false);
            } else {
                host.setHdrFloorHostCall(vals[which]);
                host.setHdrPreviewPrefHostCall(true);
            }
            if (host.getSettingsOpen()) host.fillSettingsHostCall();
        });
    }

    public static void showApertureSheet(PickerSheetsHost host) {
        final android.app.Dialog dialog = new android.app.Dialog(host.context());
        LinearLayout sheet = new LinearLayout(host.context());
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackgroundColor(host.getUI_PAPER());

        View cap = new View(host.context());
        cap.setBackgroundColor(host.getUI_BLUE());
        sheet.addView(cap, new LinearLayout.LayoutParams(-1, host.dpHostCall(3)));

        LinearLayout head = new LinearLayout(host.context());
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), 0);
        head.addView(host.labelHostCall(tr("ui170.278"), 10, host.getUI_INK(), host.getTfHan()),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView close = host.labelHostCall(tr("crash.close"), 9, host.getUI_MUTED(), host.getTfHan());
        close.setPaintFlags(close.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        close.setPadding(host.dpHostCall(12), host.dpHostCall(14), 0, host.dpHostCall(14));
        close.setOnClickListener(v -> dialog.dismiss());
        head.addView(close);
        sheet.addView(head, new LinearLayout.LayoutParams(-1, host.dpHostCall(48)));

        TextView note = host.labelHostCall(tr("ui170.279"),
                8f, host.getUI_MUTED(), host.getTfHan());
        note.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), host.dpHostCall(8));
        sheet.addView(note);

        final int cells = host.getAPERTURE_STOPS().length + 1;            // + 键入…
        for (int i = 0; i < cells; i += 4) {
            LinearLayout row = new LinearLayout(host.context());
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 4; ++c) {
                final int at = i + c;
                final boolean stop = at < host.getAPERTURE_STOPS().length;
                final boolean typed = at == host.getAPERTURE_STOPS().length;
                final LinearLayout.LayoutParams lp =
                        new LinearLayout.LayoutParams(0, host.dpHostCall(40), 1f);
                lp.setMargins(host.dpHostCall(3), host.dpHostCall(3), host.dpHostCall(3), host.dpHostCall(3));
                if (!stop && !typed) {
                    row.addView(new View(host.context()), lp);
                    continue;
                }
                final String text = typed ? tr("ui170.280")
                        : (at == 0 ? tr("ui170.277") : host.fNumLabelViaHost(host.getAPERTURE_STOPS()[at]));
                final boolean on = stop && Math.abs(host.getAPERTURE_STOPS()[at] - host.getEdgeFNumber()) < 0.05f;
                TextView chip = host.labelHostCall(text, 9.5f, on ? host.getUI_PAPER() : host.getUI_INK(), stop ? host.getTfBody() : host.getTfHan());
                chip.setGravity(android.view.Gravity.CENTER);
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setColor(on ? host.getUI_BLUE() : 0x00000000);
                bg.setStroke(Math.max(1, host.dpHostCall(1)), on ? host.getUI_BLUE() : host.getUI_LINE());
                chip.setBackground(bg);
                chip.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (typed) { host.askApertureHostCall(); return; }
                    host.setEdgeFNumber(host.getAPERTURE_STOPS()[at]);
                    host.refreshFNumValueHostCall();
                    host.onEdgeChangedHostCall();
                });
                row.addView(chip, lp);
            }
            sheet.addView(row, new LinearLayout.LayoutParams(-1, -2));
        }
        View pad = new View(host.context());
        sheet.addView(pad, new LinearLayout.LayoutParams(-1, host.dpHostCall(8)));

        dialog.setContentView(sheet);
        dialog.setCanceledOnTouchOutside(true);
        host.showBottomSheetHostCall(dialog, sheet);
        SheetDialogs.presentBottom(dialog, sheet, host.sheetHostHostCall());
    }

    public static void showFotuMediumPicker(PickerSheetsHost host) {
        final String id = FotuFilm.idOf(host.getFilmStock());
        final FotuFilm.Stock stock = host.getFotuStocks().get(id);
        if (stock == null) return;
        final List<String> media = FotuFilm.mediaFor(stock);
        final List<String> labels = new ArrayList<>();
        final List<String> notes = new ArrayList<>();
        for (String medium : media) {
            labels.add(FotuFilm.mediumLabel(medium));
            // What the sheet does, not just its name: this is the only place
            // the second engine's twelve media are explained.
            notes.add(host.paperLineViaHost(medium, host.fotuMediumNoteViaHost(medium)));
        }
        final String current = host.fotuMediumHostCall();
        final int at = media.indexOf(current);
        host.showChoiceSheetHostCall(tr("ui170.464"), labels, notes,
                at >= 0 ? labels.get(at) : null, index -> true,
                picked -> {
                    final int i = labels.indexOf(picked);
                    if (i < 0 || i >= media.size()) return;
                    final String want = media.get(i);
                    if (want.equals(host.fotuMediumHostCall())) return;
                    host.setPrintStock(want);
                    host.refreshPaperLabelHostCall();
                    host.setShowingOriginal(false);
                    host.onStockChangedHostCall();
                });
    }

    public static void showFotuPushPicker(PickerSheetsHost host) {
        if (host.getFotuRoot() == null) {
            host.ensureFotuRootAsyncHostCall(() -> showFotuPushPicker(host));
            return;
        }
        final String id = FotuFilm.idOf(host.getFilmStock());
        final float[] listed = FotuFilm.pushStops(host.getFotuRoot(), id);
        final List<String> labels = new ArrayList<>();
        final List<Float> values = new ArrayList<>();
        labels.add(tr("ui170.465"));
        values.add(0f);
        if (listed != null) {
            for (float v : listed) {
                labels.add(String.format(java.util.Locale.US, tr("ui170.466"), v));
                values.add(v);
            }
        }
        int selected = 0;
        for (int i = 0; i < values.size(); ++i) {
            if (Math.abs(values.get(i) - host.getFotuPush()) < 1e-4f) { selected = i; break; }
        }
        host.showChoiceSheetHostCall(tr("ui170.467"), labels, labels.get(selected), picked -> {
            final int i = labels.indexOf(picked);
            if (i < 0) return;
            host.setFotuPush(values.get(i));
            host.refreshFotuPushHostCall();
            if (host.getUiReady() && host.isFotuStockViaHost(host.getFilmStock())) {
                host.setShowingOriginal(false);
                host.renderFotuHostCall();
            }
        });
    }

    public static void showEdgePicker(PickerSheetsHost host) {
        // **Every gate is usable, because choosing one now gives the picture
        // that shape.** This sheet used to grey out every gate the frame did
        // not already match, so a 3:2 picture offered exactly two rows and both
        // ran the film the same way -- tr("ui170.262") was a selector that had
        // nothing left to pick. A gate is a physical shape; picking it now
        // crops the frame, centred, to that shape *from the whole picture* -- a
        // gate is a shape, not an extra cut on top of the previous gate.
        final float ca = host.originalAspectHostCall();
        final double aspect = ca > 0 ? (double) Math.max(ca, 1f / ca) : 0.0;
        final java.util.List<String> labels = new java.util.ArrayList<>();
        final java.util.List<String> notes = new java.util.ArrayList<>();
        String selectedLabel = null;
        for (int i = 0; i < host.getEDGE_IDS().length; ++i) {
            final boolean fits = aspect > 0
                    && Math.abs(aspect / host.gateRatioAtHostCall(i) - 1.0) <= 0.045;
            labels.add(host.getEDGE_LABELS()[i]);
            notes.add(host.isPrintFrameGaugeViaHost(host.getEDGE_IDS()[i])
                    ? tr("ui170.263")
                      + host.gateRatioLabelHostCall(i)
                    : (fits ? null : tr("border.centers_and_crops_to") + host.gateRatioLabelHostCall(i) + tr("border.to_fit_the_film_border")));
            if (host.getEDGE_IDS()[i].equals(host.getOverscanFormat())) selectedLabel = host.getEDGE_LABELS()[i];
        }
        host.showChoiceSheetHostCall(tr("border.choose_a_film_gate"), labels, notes, selectedLabel, null,
                picked -> {
                    final int i = labels.indexOf(picked);
                    if (i < 0) {
                        Probe.log("gate", "picked an unknown label: " + picked);
                        return;
                    }
                    host.applyGateHostCall(host.getEDGE_IDS()[i]);
                });
    }

    public static void showPfPicker(PickerSheetsHost host) {
        host.showChoiceSheetHostCall(tr("ui170.226"), java.util.Arrays.asList(host.getPF_LABELS()), host.pfLabelHostCall(), picked -> {
            final int i = java.util.Arrays.asList(host.getPF_LABELS()).indexOf(picked);
            if (i < 0) return;
            host.setPfKind(host.getPF_IDS()[i]);
            host.refreshPfRowsHostCall();
            host.refreshEdgeCardsHostCall();
            host.onEdgeChangedHostCall();
        });
    }

    public static void showEffectTab(PickerSheetsHost host, int tab) {
        host.setEffectTab(tab);
        if (host.getEffectPages() != null) {
            for (int i = 0; i < host.getEffectPages().length; ++i) {
                if (host.getEffectPages()[i] != null) {
                    host.getEffectPages()[i].setVisibility(i == tab ? View.VISIBLE : View.GONE);
                }
                if (host.getEffectChips() != null) {
                    host.getEffectChips()[i].setTextColor(i == tab ? host.getUI_BLUE() : host.getUI_MUTED());
                }
            }
        }
        if (tab == 1) { host.unlockGlowPageHostCall(); host.refreshGlowVarsHostCall(); }
    }
}
