package app.spektralab.ui.panels;
import app.spektralab.ui.CappedScroll;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.UpdateCheck;
import app.spektralab.ui.CappedScroll;

/** AppDialogs builders extracted from MainActivity (architecture Phase C). */
public final class AppDialogs {
    private AppDialogs() {}

    public static void maybeShowFeedbackGroup(AppDialogsHost host) {
        final android.content.SharedPreferences p = host.context().getSharedPreferences(host.getPROMO_PREFS(), android.content.Context.MODE_PRIVATE);
        if (p.getBoolean(host.getPROMO_MUTED(), false)) return;
        if (p.getInt(host.getPROMO_SEEN_CODE(), 0) == UpdateCheck.versionCode(host.context())) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (host.isFinishingHostCall() || !host.getUiReady()) return;
            showFeedbackGroup(host, true);
        }, 1800);
    }

    public static void showFeedbackGroup(AppDialogsHost host, final boolean auto) {
        if (host.isFinishingHostCall()) return;
        final android.content.SharedPreferences p = host.context().getSharedPreferences(host.getPROMO_PREFS(), android.content.Context.MODE_PRIVATE);
        final android.app.Dialog dialog = new android.app.Dialog(host.context());
        LinearLayout card = new LinearLayout(host.context());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(host.getUI_PAPER());
        View cap = new View(host.context());
        cap.setBackgroundColor(host.getUI_BLUE());
        card.addView(cap, new LinearLayout.LayoutParams(-1, host.dpHostCall(3)));

        LinearLayout head = new LinearLayout(host.context());
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), 0);
        head.addView(host.mixedLabelHostCall(tr("feedback.feedback_community"), 8, host.getUI_BLUE()),
                new LinearLayout.LayoutParams(0, -2, 1f));
        TextView close = host.labelHostCall(tr("crash.close"), 9, host.getUI_MUTED(), host.getTfHan());
        close.setPaintFlags(close.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        close.setPadding(host.dpHostCall(12), host.dpHostCall(14), 0, host.dpHostCall(14));
        close.setOnClickListener(v -> dialog.dismiss());
        head.addView(close);
        card.addView(head, new LinearLayout.LayoutParams(-1, host.dpHostCall(42)));

        LinearLayout body = new LinearLayout(host.context());
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), 4);
        TextView title = host.labelHostCall(tr("feedback.need_help_or_want_to"), 17, host.getUI_INK(), host.getTfHan());
        body.addView(title);
        TextView copyText = host.labelHostCall(tr("feedback.use_the_group_to_report")
                + tr("feedback.if_you_can_reproduce_the"), 10, host.getUI_MUTED(), host.getTfHan());
        copyText.setLineSpacing(host.dpHostCall(4), 1f);
        LinearLayout.LayoutParams copyTextLp = new LinearLayout.LayoutParams(-1, -2);
        copyTextLp.topMargin = host.dpHostCall(9);
        body.addView(copyText, copyTextLp);

        // **Two rooms, one door.** The QQ group is a number you type; the
        // WeChat group is a code you scan. A report should never stall on
        // "which app do I not have installed".
        LinearLayout.LayoutParams groupLp = new LinearLayout.LayoutParams(-1, -2);
        groupLp.topMargin = host.dpHostCall(16);
        body.addView(host.groupRowHostCall(
                host.getResourcesHostCall().getIdentifier("wechat_qr", "drawable", host.getPackageNameHostCall()),
                tr("feedback.wechat_group"), tr("feedback.scan_with_wechat_to_join"), tr("feedback.if_the_qr_code_has")), groupLp);

        // **维修二群.** The first room filled up, so the invite that still works
        // has to be the second one, and the card has to say so -- otherwise a
        // newcomer scans the first code, is refused, and has nowhere to go.
        final int qr2 = host.getResourcesHostCall().getIdentifier("group_qr_2", "drawable", host.getPackageNameHostCall());
        if (qr2 != 0) {
            LinearLayout.LayoutParams group2Lp = new LinearLayout.LayoutParams(-1, -2);
            group2Lp.topMargin = host.dpHostCall(12);
            body.addView(host.groupRowHostCall(qr2, tr("ui170.271"), tr("ui170.272"), null), group2Lp);
        }

        View divider = new View(host.context());
        divider.setBackgroundColor(host.getUI_LINE());
        LinearLayout.LayoutParams dividerLp = new LinearLayout.LayoutParams(-1, host.dpHostCall(1));
        dividerLp.topMargin = host.dpHostCall(16);
        body.addView(divider, dividerLp);

        TextView number = host.labelHostCall(host.getQQ_GROUP(), 20, host.getUI_BLUE(), host.getTfDisplay());
        number.setLetterSpacing(-0.02f);
        LinearLayout.LayoutParams numberLp = new LinearLayout.LayoutParams(-1, -2);
        numberLp.topMargin = host.dpHostCall(16);
        body.addView(number, numberLp);
        TextView hint = host.labelHostCall(tr("ui170.273"), 9, host.getUI_MUTED(), host.getTfHan());
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
        hintLp.topMargin = host.dpHostCall(5);
        body.addView(hint, hintLp);
        // **The body scrolls, the buttons do not.** Two group codes made the card
        // ~630 dp tall -- taller than a 640 dp screen, which is the shape of bug
        // that once pushed 「去更新」 off the update dialog. The cap leaves the
        // head and the two actions where they are and scrolls the middle.
        final CappedScroll bodyScroll = new CappedScroll(host.context(), Math.max(host.dpHostCall(300),
                (int) (host.getResourcesHostCall().getDisplayMetrics().heightPixels * 0.62f)));
        bodyScroll.addView(body, new android.widget.FrameLayout.LayoutParams(-1, -2));
        card.addView(bodyScroll, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(host.context());
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(host.dpHostCall(16), host.dpHostCall(16), host.dpHostCall(16), host.dpHostCall(16));

        TextView open = host.labelHostCall(tr("feedback.open_qq"), 10, host.getUI_BLUE(), host.getTfHan());
        open.setGravity(android.view.Gravity.CENTER);
        android.graphics.drawable.GradientDrawable openBg = new android.graphics.drawable.GradientDrawable();
        openBg.setColor(host.getUI_PAPER());
        openBg.setStroke(host.dpHostCall(1), host.getUI_BLUE());
        open.setBackground(openBg);
        open.setOnClickListener(v -> host.openQqGroupHostCall());
        LinearLayout.LayoutParams openLp = new LinearLayout.LayoutParams(0, host.dpHostCall(42), 1f);
        openLp.rightMargin = host.dpHostCall(8);
        actions.addView(open, openLp);

        TextView copy = host.labelHostCall(tr("feedback.copy_group_id"), 10, host.getUI_PAPER(), host.getTfHan());
        copy.setGravity(android.view.Gravity.CENTER);
        android.graphics.drawable.GradientDrawable copyBg = new android.graphics.drawable.GradientDrawable();
        copyBg.setColor(host.getUI_BLUE());
        copy.setBackground(copyBg);
        copy.setOnClickListener(v -> {
            if (host.copyToClipboardHostCall(tr("feedback.spektralab_feedback_group"), host.getQQ_GROUP())) {
                copy.setText(tr("feedback.copied"));
            } else {
                copy.setText(tr("feedback.could_not_copy_press_and"));
            }
        });
        copy.setLongClickable(true);
        copy.setOnLongClickListener(v -> {
            host.copyToClipboardHostCall(tr("feedback.spektralab_feedback_group"), host.getQQ_GROUP());
            return true;
        });
        actions.addView(copy, new LinearLayout.LayoutParams(0, host.dpHostCall(42), 1.45f));
        card.addView(actions, new LinearLayout.LayoutParams(-1, -2));

        if (auto) {
            TextView mute = host.labelHostCall(tr("feedback.don_t_show_automatically"), 8.5f, host.getUI_MUTED(), host.getTfHan());
            mute.setGravity(android.view.Gravity.CENTER);
            mute.setPaintFlags(mute.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            mute.setPadding(0, 0, 0, host.dpHostCall(14));
            mute.setOnClickListener(v -> {
                p.edit().putBoolean(host.getPROMO_MUTED(), true).apply();
                dialog.dismiss();
                host.showInAppToastHostCall(tr("feedback.automatic_prompts_are_off_the"));
            });
            card.addView(mute, new LinearLayout.LayoutParams(-1, host.dpHostCall(30)));
        }

        dialog.setContentView(card);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0x00000000));
            int width = Math.min(host.dpHostCall(358), host.getResourcesHostCall().getDisplayMetrics().widthPixels - host.dpHostCall(32));
            dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.32f);
        }
        host.popInCardHostCall(card);
        // Shown counts as seen: the point is that a new build mentions it once.
        p.edit().putInt(host.getPROMO_SEEN_CODE(), UpdateCheck.versionCode(host.context())).apply();
    }

    public static void showTextDialog(AppDialogsHost host, String title, String body, boolean report) {
        // **A batch must not stop at the first bad frame.** One dialog per
        // failure would hide the queue behind a stack of sheets; the line is
        // recorded, the queue moves on, and the summary at the end counts it.
        if (host.getBatchExportRunning()) {
            try { DebugSink.text("batch-error", title + " | " + body); }
            catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30007", ignored); }
            return;
        }
        final String shownTitle = (title == null || title.isEmpty()) ? tr("import.error") : title;
        final String shownBody = (body == null || body.isEmpty()) ? tr("import.e_0000_unknown_error") : body;
        // **One row per failure.** The ring is already in memory; sending it
        // here is what makes an error report readable without the user having
        // to copy anything, and it costs one write instead of sixty-four.
        if (report) host.reportFailureHostCall(shownTitle, shownBody);
        host.runOnUiThreadHostCall(() -> {
            final android.app.Dialog dialog = new android.app.Dialog(host.context());
            LinearLayout card = new LinearLayout(host.context());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(host.getUI_PAPER());
            View cap = new View(host.context());
            cap.setBackgroundColor(host.getUI_BLUE());
            card.addView(cap, new LinearLayout.LayoutParams(-1, host.dpHostCall(3)));

            LinearLayout head = new LinearLayout(host.context());
            head.setOrientation(LinearLayout.HORIZONTAL);
            head.setGravity(android.view.Gravity.CENTER_VERTICAL);
            head.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), 0);
            TextView kicker = host.mixedLabelHostCall(tr("import.error_details"), 8, host.getUI_BLUE());
            kicker.setLetterSpacing(0.08f);
            head.addView(kicker, new LinearLayout.LayoutParams(0, -2, 1f));
            TextView close = host.labelHostCall(tr("crash.close"), 9, host.getUI_MUTED(), host.getTfHan());
            close.setPaintFlags(close.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            close.setPadding(host.dpHostCall(12), host.dpHostCall(14), 0, host.dpHostCall(14));
            close.setOnClickListener(v -> dialog.dismiss());
            head.addView(close);
            card.addView(head, new LinearLayout.LayoutParams(-1, host.dpHostCall(42)));

            ScrollView scroll = new ScrollView(host.context());
            LinearLayout bodyBox = new LinearLayout(host.context());
            bodyBox.setOrientation(LinearLayout.VERTICAL);
            bodyBox.setPadding(host.dpHostCall(16), 0, host.dpHostCall(16), 4);
            TextView titleView = host.labelHostCall(shownTitle, 18, host.getUI_INK(), host.getTfHan());
            titleView.setLetterSpacing(-0.02f);
            bodyBox.addView(titleView);
            TextView advice = host.labelHostCall(tr("import.copy_the_full_details_below") + DebugSink.REF, 10, host.getUI_MUTED(), host.getTfHan());
            LinearLayout.LayoutParams adviceLp = new LinearLayout.LayoutParams(-1, -2);
            adviceLp.topMargin = host.dpHostCall(8);
            bodyBox.addView(advice, adviceLp);
            TextView detail = host.labelHostCall(shownBody, 9.5f, 0xFF454640, host.getTfHan());
            detail.setTextIsSelectable(true);
            detail.setLineSpacing(host.dpHostCall(4), 1f);
            detail.setPadding(host.dpHostCall(12), host.dpHostCall(11), host.dpHostCall(12), host.dpHostCall(11));
            android.graphics.drawable.GradientDrawable detailBg = new android.graphics.drawable.GradientDrawable();
            detailBg.setColor(0xFFE7E8E3);
            detail.setBackground(detailBg);
            LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(-1, -2);
            detailLp.topMargin = host.dpHostCall(14);
            bodyBox.addView(detail, detailLp);
            scroll.addView(bodyBox, new android.widget.FrameLayout.LayoutParams(-1, -2));
            int bodyHeight = (int) Math.min(host.dpHostCall(340), host.getResourcesHostCall().getDisplayMetrics().heightPixels * 0.44f);
            card.addView(scroll, new LinearLayout.LayoutParams(-1, bodyHeight));

            LinearLayout actions = new LinearLayout(host.context());
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(host.dpHostCall(16), host.dpHostCall(14), host.dpHostCall(16), host.dpHostCall(16));
            TextView closeAction = host.labelHostCall(tr("crash.close"), 10, host.getUI_BLUE(), host.getTfHan());
            closeAction.setGravity(android.view.Gravity.CENTER);
            android.graphics.drawable.GradientDrawable closeBg = new android.graphics.drawable.GradientDrawable();
            closeBg.setColor(host.getUI_PAPER());
            closeBg.setStroke(host.dpHostCall(1), host.getUI_BLUE());
            closeAction.setBackground(closeBg);
            closeAction.setOnClickListener(v -> dialog.dismiss());
            LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(0, host.dpHostCall(42), 1f);
            closeLp.rightMargin = host.dpHostCall(8);
            actions.addView(closeAction, closeLp);
            TextView copy = host.labelHostCall(tr("import.copy_error_details"), 10, host.getUI_PAPER(), host.getTfHan());
            copy.setGravity(android.view.Gravity.CENTER);
            android.graphics.drawable.GradientDrawable copyBg = new android.graphics.drawable.GradientDrawable();
            copyBg.setColor(host.getUI_BLUE());
            copy.setBackground(copyBg);
            copy.setOnClickListener(v -> {
                try {
                    // **The ref goes first.** It is the only line that leads
                    // back to the D1 rows; a screenshot of the dialog is not
                    // always possible, and a pasted body without it is a
                    // report with no address.
                    final String text = tr("settings.report_id") + DebugSink.REF
                            + "\n\n" + shownTitle + "\n\n" + shownBody
                            + "\n\nbuild=SpektraLab Android " + UpdateCheck.versionName(host.context())
                            + "\n" + host.getProbe().describe();
                    android.content.ClipboardManager cm =
                            (android.content.ClipboardManager) host.context().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("SpektraLab", text));
                    copy.setText(tr("feedback.copied"));
                } catch (Throwable t) {
                    copy.setText(tr("import.could_not_copy_press_and"));
                }
            });
            actions.addView(copy, new LinearLayout.LayoutParams(0, host.dpHostCall(42), 1.45f));
            card.addView(actions, new LinearLayout.LayoutParams(-1, -2));

            dialog.setContentView(card);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0x00000000));
                int width = Math.min(host.dpHostCall(358),
                        host.getResourcesHostCall().getDisplayMetrics().widthPixels - host.dpHostCall(32));
                dialog.getWindow().setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.getWindow().setDimAmount(0.30f);
            }
            host.popInCardHostCall(card);
        });
    }

    public static void showErrorDialog(AppDialogsHost host, String title, String body) {
        showTextDialog(host, title, body, true);
    }
}
