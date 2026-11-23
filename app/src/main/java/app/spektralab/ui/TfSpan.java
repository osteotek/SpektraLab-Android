package app.spektralab.ui;

import android.graphics.Typeface;

/** Typeface span for mixed Latin/CJK text (extracted). */
    public class TfSpan extends android.text.style.MetricAffectingSpan {
        private final Typeface tf;
        public TfSpan(Typeface tf) { this.tf = tf; }
        @Override public void updateDrawState(android.text.TextPaint p) { p.setTypeface(tf); }
        @Override public void updateMeasureState(android.text.TextPaint p) { p.setTypeface(tf); }
    }
