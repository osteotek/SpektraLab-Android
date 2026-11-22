package app.spektralab.ui;

/** Extracted from MainActivity (custom-view refactor). */
    public final class TraceDraw extends android.graphics.drawable.Drawable {
        private final android.view.View host;
        private final float radius;
        private final float stroke;
        private final android.graphics.Paint paint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF box = new android.graphics.RectF();
        private float reach, alpha = 1f;
    private final int accent;
    private final float density;
    private int dp(float v) { return Math.round(v * density); }

        public TraceDraw(android.view.View host, float radius, int accent, float density) {
            this.host = host;
            this.radius = radius;
        this.accent = accent;
        this.density = density;
            this.stroke = dp(1.2f);
        }

        public void set(float reach, float alpha) {
            this.reach = reach;
            this.alpha = alpha;
            invalidateSelf();
        }

        @Override public void draw(android.graphics.Canvas c) {
            if (reach <= 0.001f || alpha <= 0.01f) return;
            final int w = host.getWidth(), h = host.getHeight();
            if (w <= 0 || h <= 0) return;
            final float inset = stroke * 0.5f;
            box.set(inset, inset, w - inset, h - inset);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(accent);
            paint.setAlpha(Math.round(255f * alpha));
            c.save();
            c.clipRect(0f, 0f, w * reach, h);
            c.drawRoundRect(box, radius, radius, paint);
            c.restore();
        }

        @Override public void setAlpha(int a) { }
        @Override public void setColorFilter(android.graphics.ColorFilter f) { }
        @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    }
