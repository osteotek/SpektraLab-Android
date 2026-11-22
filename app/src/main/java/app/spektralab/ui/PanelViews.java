package app.spektralab.ui;

/** Shared view factory for the editor's parameter panels.
 *
 * Extracted in architecture Phase 3. Each panel still owns its controls; this
 * class owns the section header that every panel repeats, so the title font,
 * letter spacing and reset affordance have one definition.
 */
public final class PanelViews {
    public interface Host {
        android.content.Context context();
        android.widget.TextView panelMixedLabel(String text, float sp, int color);
        android.widget.TextView panelLabel(String text, float sp, int color,
                                           android.graphics.Typeface font);
        int dp(float value);
        int inkColor();
        int mutedColor();
        android.graphics.Typeface hanFont();
        String resetText();
    }

    private PanelViews() {}

    /** Horizontal title row with the underlined "Reset this page" action. */
    public static android.widget.LinearLayout sectionHead(Host h,
                                                          String title,
                                                          Runnable reset) {
        final android.widget.LinearLayout head =
                new android.widget.LinearLayout(h.context());
        head.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        final android.widget.TextView titleView = h.panelMixedLabel(title, 8.5f, h.inkColor());
        titleView.setLetterSpacing(0.06f);
        head.addView(titleView, new android.widget.LinearLayout.LayoutParams(0, -2, 1f));
        final android.widget.TextView resetView = h.panelLabel(h.resetText(), 8.5f,
                h.mutedColor(), h.hanFont());
        resetView.setPaintFlags(resetView.getPaintFlags()
                | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        resetView.setPadding(h.dp(8), h.dp(10), 0, h.dp(10));
        resetView.setOnClickListener(v -> reset.run());
        head.addView(resetView);
        return head;
    }
}
