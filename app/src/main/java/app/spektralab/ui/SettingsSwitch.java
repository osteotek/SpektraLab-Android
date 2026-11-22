package app.spektralab.ui;

/** Extracted from MainActivity (custom-view refactor). */
    public final class SettingsSwitch extends android.view.View {
        private final android.graphics.Paint p =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF r = new android.graphics.RectF();
        private boolean on;
        private float pos, fromPos;
        private long animAt;
    private final int blue, line;
    private final boolean dark;

        public SettingsSwitch(android.content.Context c, int blue, int line, boolean dark) {
        super(c); this.blue = blue; this.line = line; this.dark = dark;
        setClickable(true);
    }
        public boolean isOn() { return on; }

        public void setOn(boolean v) {
            if (on == v) return;
            fromPos = pos;
            on = v;
            animAt = android.os.SystemClock.uptimeMillis();
            invalidate();
        }

        @Override protected void onDraw(android.graphics.Canvas c) {
            final float w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;
            if (animAt != 0L) {
                final float u = Math.min(1f, (android.os.SystemClock.uptimeMillis() - animAt) / 160f);
                final float e = 1f - (1f - u) * (1f - u);
                pos = fromPos + ((on ? 1f : 0f) - fromPos) * e;
                if (u >= 1f) { pos = on ? 1f : 0f; animAt = 0L; }
                else postInvalidateOnAnimation();
            } else {
                pos = on ? 1f : 0f;
            }
            final float tr = h * 0.60f, ty = (h - tr) * 0.5f;
            r.set(0f, ty, w, ty + tr);
            p.setStyle(android.graphics.Paint.Style.FILL);
            p.setColor(on ? ((blue & 0x00FFFFFF) | 0x73000000) : line);
            c.drawRoundRect(r, tr * 0.5f, tr * 0.5f, p);
            final float d = h * 0.86f, rad = d * 0.5f;
            final float cx = rad + (w - d) * pos, cy = h * 0.5f;
            p.setColor(on ? blue : (dark ? 0xFF7A736A : 0xFFFFFFFF));
            c.drawCircle(cx, cy, rad, p);
            if (!on) {
                p.setStyle(android.graphics.Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(1f, rad * 0.09f));
                p.setColor(dark ? 0x33FFFFFF : 0x22000000);
                c.drawCircle(cx, cy, rad - p.getStrokeWidth() * 0.5f, p);
            }
        }
    }
