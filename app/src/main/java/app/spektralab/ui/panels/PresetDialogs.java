package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import app.spektralab.MainActivity.Frame;
import app.spektralab.MainActivity.Look;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.ui.*;

/** PresetDialogs builders extracted from MainActivity (architecture Phase C). */
public final class PresetDialogs {
    private PresetDialogs() {}

    public static void showPresetsPanel(PresetDialogsHost host) {
        final org.json.JSONArray list = host.presetListHostCall();
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
        head.addView(host.labelHostCall(tr("preset.presets"), 12, host.getUI_INK(), host.getTfHan()),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView close = host.labelHostCall(tr("crash.close"), 9, host.getUI_MUTED(), host.getTfHan());
        close.setPadding(host.dpHostCall(12), host.dpHostCall(14), 0, host.dpHostCall(14));
        close.setOnClickListener(v -> dialog.dismiss());
        head.addView(close);
        sheet.addView(head, new LinearLayout.LayoutParams(-1, host.dpHostCall(54)));

        TextView note = host.labelHostCall(tr("preset.presets_store_film_exposure_optics"),
                8.5f, host.getUI_MUTED(), host.getTfHan());
        note.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), host.dpHostCall(10));
        sheet.addView(note);

        LinearLayout rows = new LinearLayout(host.context());
        rows.setOrientation(LinearLayout.VERTICAL);
        if (list.length() == 0) {
            TextView empty = host.labelHostCall(tr("preset.no_presets_yet_edit_a"), 9, host.getUI_MUTED(), host.getTfHan());
            empty.setPadding(host.dpHostCall(16), host.dpHostCall(12), host.dpHostCall(16), host.dpHostCall(12));
            rows.addView(empty);
        }
        // **Above the user's list, because it is not a look of theirs.**
        LinearLayout builtin = new LinearLayout(host.context());
        builtin.setOrientation(LinearLayout.HORIZONTAL);
        builtin.setGravity(android.view.Gravity.CENTER_VERTICAL);
        builtin.setPadding(host.dpHostCall(16), 0, host.dpHostCall(10), 0);
        LinearLayout builtinText = new LinearLayout(host.context());
        builtinText.setOrientation(LinearLayout.VERTICAL);
        builtinText.addView(host.labelHostCall(tr("ui170.266"), 10, host.getUI_INK(), host.getTfHan()));
        builtinText.addView(host.labelHostCall(tr("ui170.267"), 8, host.getUI_MUTED(), host.getTfHan()));
        builtin.addView(builtinText, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView builtinApply = host.labelHostCall(tr("preset.apply"), 8.5f, host.getUI_BLUE(), host.getTfHan());
        builtinApply.setPadding(host.dpHostCall(12), host.dpHostCall(12), host.dpHostCall(8), host.dpHostCall(12));
        builtinApply.setOnClickListener(v -> {
            dialog.dismiss();
            host.applyDesktopBaselineHostCall();
        });
        builtin.addView(builtinApply);
        rows.addView(builtin, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));
        View builtinRule = new View(host.context());
        builtinRule.setBackgroundColor(host.getUI_LINE());
        rows.addView(builtinRule, new LinearLayout.LayoutParams(-1, host.dpHostCall(1)));

        for (int i = 0; i < list.length(); ++i) {
            final org.json.JSONObject p = list.optJSONObject(i);
            final int removeAt = i;
            if (p == null) continue;
            LinearLayout row = new LinearLayout(host.context());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(host.dpHostCall(16), 0, host.dpHostCall(10), 0);
            TextView name = host.labelHostCall(p.optString("name", tr("preset.preset")), 10, host.getUI_INK(), host.getTfHan());
            name.setSingleLine(true);
            name.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(name, new LinearLayout.LayoutParams(0, -2, 1f));

            TextView apply = host.labelHostCall(tr("preset.apply"), 8.5f, host.getUI_BLUE(), host.getTfHan());
            apply.setPadding(host.dpHostCall(8), host.dpHostCall(12), host.dpHostCall(8), host.dpHostCall(12));
            apply.setOnClickListener(v -> {
                dialog.dismiss();
                host.applyPresetHostCall(p);
            });
            row.addView(apply);

            TextView share = host.labelHostCall(tr("editor.share"), 8.5f, host.getUI_MUTED(), host.getTfHan());
            share.setPadding(host.dpHostCall(8), host.dpHostCall(12), host.dpHostCall(8), host.dpHostCall(12));
            share.setOnClickListener(v -> host.sharePresetHostCall(p));
            row.addView(share);

            TextView del = host.labelHostCall(tr("preset.delete"), 8.5f, host.getUI_MUTED(), host.getTfHan());
            del.setPadding(host.dpHostCall(8), host.dpHostCall(12), host.dpHostCall(4), host.dpHostCall(12));
            del.setOnClickListener(v -> {
                final org.json.JSONArray next = new org.json.JSONArray();
                for (int k = 0; k < list.length(); ++k) {
                    if (k == removeAt) continue;
                    final org.json.JSONObject q = list.optJSONObject(k);
                    if (q != null) next.put(q);
                }
                host.storePresetListHostCall(next);
                dialog.dismiss();
                showPresetsPanel(host);
            });
            row.addView(del);
            rows.addView(row, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));
        }
        sheet.addView(host.cappedScrollHostCall(rows, 300), new LinearLayout.LayoutParams(-1, -2));

        LinearLayout foot = new LinearLayout(host.context());
        foot.setOrientation(LinearLayout.HORIZONTAL);
        foot.setPadding(host.dpHostCall(10), host.dpHostCall(6), host.dpHostCall(10), host.dpHostCall(10));
        TextView save = host.labelHostCall(tr("preset.save_current_settings"), 8.5f, host.getUI_BLUE(), host.getTfHan());
        save.setGravity(android.view.Gravity.CENTER);
        save.setOnClickListener(v -> {
            dialog.dismiss();
            host.saveCurrentPresetHostCall();
        });
        foot.addView(save, new LinearLayout.LayoutParams(0, host.dpHostCall(34), 1f));
        TextView pull = host.labelHostCall(tr("preset.import_from_clipboard"), 8.5f, host.getUI_INK(), host.getTfHan());
        pull.setGravity(android.view.Gravity.CENTER);
        pull.setOnClickListener(v -> {
            dialog.dismiss();
            host.importPresetFromClipboardHostCall();
        });
        foot.addView(pull, new LinearLayout.LayoutParams(0, host.dpHostCall(34), 1f));
        TextView push = host.labelHostCall(tr("preset.share_current_settings"), 8.5f, host.getUI_INK(), host.getTfHan());
        push.setGravity(android.view.Gravity.CENTER);
        push.setOnClickListener(v -> {
            dialog.dismiss();
            host.shareCurrentLookHostCall();
        });
        foot.addView(push, new LinearLayout.LayoutParams(0, host.dpHostCall(34), 1f));
        sheet.addView(foot, new LinearLayout.LayoutParams(-1, host.dpHostCall(50)));

        dialog.setContentView(sheet);
        dialog.setCanceledOnTouchOutside(true);
        host.showBottomSheetHostCall(dialog, sheet);
        SheetDialogs.presentBottom(dialog, sheet, host.sheetHostHostCall());
    }

    public static void showBatchDeleteSheet(PresetDialogsHost host) {
        if (host.getBatchExportRunning()) {
            host.showInAppToastHostCall(tr("editor.wait_for_batch_export_to_2"));
            return;
        }
        if (host.getFrames().size() < 2) {
            host.showInAppToastHostCall(tr("preset.there_is_only_one_photo"));
            return;
        }
        final String[] labels = new String[host.getFrames().size()];
        for (int i = 0; i < host.getFrames().size(); ++i) {
            final Frame f = host.getFrames().get(i);
            labels[i] = (f.name == null || f.name.isEmpty() ? tr("preset.photo") + (i + 1) : f.name);
        }
        host.showMultiChoiceSheetHostCall(tr("preset.remove_which_photos"),
                tr("preset.removes_photos_and_their_edits"),
                labels, picked -> {
                    final java.util.ArrayList<Integer> indices =
                            new java.util.ArrayList<>(picked);
                    java.util.Collections.sort(indices, (a, b) -> b - a);
                    for (int i = 0; i < indices.size(); ++i) host.removeFrameHostCall(indices.get(i));
                    host.showInAppToastHostCall(tr("preset.removed") + indices.size() + tr("preset.photos"));
                });
    }

    public static void showApplyLookSheet(PresetDialogsHost host, final int sourceIndex) {
        if (sourceIndex < 0 || sourceIndex >= host.getFrames().size()) return;
        final java.util.ArrayList<Integer> targets = new java.util.ArrayList<>();
        for (int i = 0; i < host.getFrames().size(); ++i) {
            if (i != sourceIndex) targets.add(i);
        }
        if (targets.isEmpty()) {
            host.showInAppToastHostCall(tr("preset.there_are_no_other_photos"));
            return;
        }
        final String[] labels = new String[targets.size()];
        for (int i = 0; i < targets.size(); ++i) {
            final int index = targets.get(i);
            final Frame f = host.getFrames().get(index);
            labels[i] = (f.name == null || f.name.isEmpty() ? tr("preset.photo") + (index + 1) : f.name);
        }
        final String srcName = host.getFrames().get(sourceIndex).name == null
                ? tr("preset.current_photo") : host.getFrames().get(sourceIndex).name;
        host.showMultiChoiceSheetHostCall(tr("preset.apply_these_settings_to"), 
                tr("preset.copies_the_current_film_exposure")
                        + tr("preset.original_files_are_not_changed"),
                labels, picked -> {
                    host.saveLookIntoFramesHostCall();
                    final Look src = host.copyLookHostCall(host.getFrames().get(sourceIndex).look);
                    if (src == null) {
                        host.showInAppToastHostCall(tr("preset.this_photo_has_no_settings"));
                        return;
                    }
                    int n = 0;
                    for (int k : picked) {
                        final int index = targets.get(k);
                        host.getFrames().get(index).look = host.copyLookHostCall(src);
                        // **A copied Look is a picture that has been printed.**
                        // `Frame.original` says tr("ui170.269"), and the
                        // copy path left it alone -- so a target that had never
                        // picked a film opened on the original, drew no film at
                        // all, and the whole operation looked like it had done
                        // nothing (tr("ui170.270")). Keeping the flag
                        // is only right when the source itself has no film.
                        host.getFrames().get(index).original = (src.filmStock == null);
                        ++n;
                    }
                    host.saveSessionStateHostCall();
                    host.rebuildFrameChipsHostCall();
                    host.showInAppToastHostCall(tr("preset.applied_to") + n + tr("preset.photos_changes_appear_when_opened"));
                });
    }
}
