package app.spektralab.ui;

/** Extracted from MainActivity (custom-view refactor). */
    public final class ChipRow extends android.widget.LinearLayout {
    public interface LabelFactory {
        android.widget.TextView label(String s, float sp, int color, android.graphics.Typeface tf);
    }
        public final android.widget.TextView[] labels;
        private final int accent, muted;
        private int active = -1;

        public ChipRow(android.content.Context c, String[] names, float sp, int cellH, int accent, int muted,
                java.lang.Runnable[] taps, android.graphics.Typeface han, LabelFactory factory) {
            super(c);
            this.accent = accent;
            this.muted = muted;
            setOrientation(HORIZONTAL);
            labels = new android.widget.TextView[names.length];
            for (int i = 0; i < names.length; ++i) {
                final android.widget.FrameLayout cell =
                        new android.widget.FrameLayout(c);
                // **Through `label`, not `new TextView`.** These were built by
                // hand and never given a face, so the one row a user looks at on
                // every page came out in the ROM's own font while everything
                // around it was Inter and OPPO Sans.
                final android.widget.TextView t = factory.label(names[i], sp, muted, han);
                t.setSingleLine(true);
                t.setGravity(android.view.Gravity.CENTER);
                t.setTextColor(muted);
                cell.addView(t, new android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                        android.view.Gravity.CENTER));
                cell.setClickable(true);
                final int index = i;
                cell.setOnClickListener(v -> {
                    if (taps != null && index < taps.length && taps[index] != null) {
                        taps[index].run();
                    }
                });
                addView(cell, new android.widget.LinearLayout.LayoutParams(0, cellH, 1f));
                labels[i] = t;
            }
        }

        /** Accent colour plus the underline -- the language the old chips had. */
        public void setActive(int index) {
            active = index;
            for (int i = 0; i < labels.length; ++i) {
                final boolean on = i == index;
                labels[i].setTextColor(on ? accent : muted);
                underlineLocal(labels[i], on);
            }
        }
    
    private static void underlineLocal(android.widget.TextView tv, boolean on) {
        final int flags = tv.getPaintFlags();
        tv.setPaintFlags(on ? (flags | android.graphics.Paint.UNDERLINE_TEXT_FLAG)
                : (flags & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
    }
}

