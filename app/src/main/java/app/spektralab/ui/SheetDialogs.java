package app.spektralab.ui;

/** Shared shells for the editor's bottom sheets.
 *
 * Extracted in architecture Phase 4. The Activity still owns each sheet's
 * content; this class owns the paper root, the title/close header, the
 * long-note scroll rule and the window presentation that every sheet repeats.
 */
public final class SheetDialogs {
    public interface Host {
        android.widget.TextView sheetLabel(String text, float sp, int color,
                                           android.graphics.Typeface font);
        int dp(float value);
        int paperColor();
        int blueColor();
        int inkColor();
        int mutedColor();
        android.graphics.Typeface hanFont();
        int screenHeightPx();
        void showBottomSheet(android.app.Dialog dialog, android.view.View sheet);
    }

    private SheetDialogs() {}

    /** The paper panel with the three-pixel blue cap at the top. */
    public static android.widget.LinearLayout sheetRoot(Host h,
                                                        android.content.Context context) {
        final android.widget.LinearLayout sheet =
                new android.widget.LinearLayout(context);
        sheet.setOrientation(android.widget.LinearLayout.VERTICAL);
        sheet.setBackgroundColor(h.paperColor());
        final android.view.View cap = new android.view.View(context);
        cap.setBackgroundColor(h.blueColor());
        sheet.addView(cap, new android.widget.LinearLayout.LayoutParams(
                -1, h.dp(3)));
        return sheet;
    }

    /** The title row with its close action. */
    public static android.widget.LinearLayout header(Host h,
                                                     android.app.Dialog dialog,
                                                     String title,
                                                     boolean underlineClose,
                                                     final Runnable onTitleLongPress) {
        final android.widget.LinearLayout head =
                new android.widget.LinearLayout(dialog.getContext());
        head.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(h.dp(16), 0, h.dp(16), 0);
        final android.widget.TextView titleView = h.sheetLabel(title, 12, h.inkColor(), h.hanFont());
        if (onTitleLongPress != null) {
            titleView.setOnLongClickListener(v -> {
                dialog.dismiss();
                onTitleLongPress.run();
                return true;
            });
        }
        head.addView(titleView, new android.widget.LinearLayout.LayoutParams(0, -2, 1f));
        final android.widget.TextView close =
                h.sheetLabel(app.spektralab.AppLanguage.tr("crash.close"), 9, h.mutedColor(), h.hanFont());
        if (underlineClose) {
            close.setPaintFlags(close.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        }
        close.setPadding(h.dp(12), h.dp(14), 0, h.dp(14));
        close.setOnClickListener(v -> dialog.dismiss());
        head.addView(close);
        return head;
    }

    /** Returns the pinned note's height so the note-scroll rule can reserve it. */
    public static int pinnedNote(Host h, android.widget.LinearLayout sheet, String pinned) {
        if (pinned == null || pinned.isEmpty()) return 0;
        final android.widget.TextView view = h.sheetLabel(pinned, 9f, h.blueColor(), h.hanFont());
        view.setTextIsSelectable(true);
        view.setPadding(h.dp(16), 0, h.dp(16), h.dp(10));
        sheet.addView(view);
        return h.dp(34);
    }

    /** A note scrolls only when it is long enough to push the action rows away. */
    public static void note(Host h, android.app.Dialog dialog,
                            android.widget.LinearLayout sheet, String note,
                            int pinnedHeight, int itemCount) {
        if (note == null || note.isEmpty()) return;
        final android.widget.TextView view = h.sheetLabel(note, 8.5f, h.mutedColor(), h.hanFont());
        view.setPadding(h.dp(16), 0, h.dp(16), h.dp(12));
        final int lines = note.split("\n", -1).length;
        final int reserved = h.dp(54) + pinnedHeight + itemCount * h.dp(50) + h.dp(28);
        final int room = Math.max(h.dp(96), h.screenHeightPx() - reserved);
        if (lines > 8 && room < lines * h.dp(16)) {
            final android.widget.ScrollView scroll = new android.widget.ScrollView(dialog.getContext());
            scroll.setVerticalScrollBarEnabled(false);
            scroll.addView(view, new android.widget.FrameLayout.LayoutParams(-1, -2));
            sheet.addView(scroll, new android.widget.LinearLayout.LayoutParams(
                    -1, Math.min(room, h.dp(420))));
        } else {
            sheet.addView(view);
        }
    }

    /** Show the sheet and apply the shared dimmed, bottom-gravity window. */
    public static void presentBottom(android.app.Dialog dialog,
                                     android.view.View sheet, Host h) {
        h.showBottomSheet(dialog, sheet);
        final android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(0x00000000));
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.BOTTOM);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.35f);
        }
    }
}
