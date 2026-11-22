package app.spektralab.ui;

import android.graphics.*;

/** The darkroom/light-box rocker switch (extracted from MainActivity). */
    public final class RockerSwitch extends android.view.View {
        private final float density;
        private int dp(float v) { return Math.round(v * density); }
        private final Paint rp = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path up = new Path(), dn = new Path(), all = new Path();
        private final Path wellClip = new Path();
        private final RectF rr = new RectF();

        private static final long THROW_MS = 220L;
        private static final long PRESS_MS = 90L;

        /** 0 = the darkroom (O down), 1 = the light box (I down). */
        private float flip = 0f, flipFrom = 0f, flipTo = 0f;
        private long flipAt = 0L;
        private float press = 0f, pressFrom = 0f, pressTo = 0f;
        private long pressAt = 0L;

        public RockerSwitch(android.content.Context c) {
            super(c);
            this.density = c.getResources().getDisplayMetrics().density;
            setClickable(true);
            setFocusable(false);
        }

        /** How lit the rocker looks, independently of the 800 ms lamp. */
        public float lit() { return flip; }

        public void setThrown(boolean on) { setThrown(on, true); }

        public void setThrown(boolean on, boolean animate) {
            final float to = on ? 1f : 0f;
            if (flipTo == to) return;
            flipFrom = flip;
            flipTo = to;
            flipAt = System.nanoTime();
            if (!animate) {
                flip = to;
                flipFrom = to;
            }
            postInvalidateOnAnimation();
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            final int a = e.getActionMasked();
            if (a == android.view.MotionEvent.ACTION_DOWN) {
                pressFrom = press;
                pressTo = 1f;
                pressAt = System.nanoTime();
                postInvalidateOnAnimation();
            } else if (a == android.view.MotionEvent.ACTION_UP
                    || a == android.view.MotionEvent.ACTION_CANCEL) {
                pressFrom = press;
                pressTo = 0f;
                pressAt = System.nanoTime();
                postInvalidateOnAnimation();
            }
            return super.onTouchEvent(e);
        }

        /** Ease-out cubic: fast off the detent, then it settles. */
        private float easeOutCubic(float u) {
            final float v = 1f - u;
            return 1f - v * v * v;
        }

        private void advance() {
            final long now = System.nanoTime();
            if (flip != flipTo) {
                final float u = Math.min(1f, (now - flipAt) / 1e6f / THROW_MS);
                flip = flipFrom + (flipTo - flipFrom) * easeOutCubic(u);
                if (u >= 1f) flip = flipTo;
            }
            if (press != pressTo) {
                final float u = Math.min(1f, (now - pressAt) / 1e6f / PRESS_MS);
                press = pressFrom + (pressTo - pressFrom) * u;
                if (u >= 1f) press = pressTo;
            }
        }

        @Override protected void onDraw(Canvas c) {
            advance();
            if (flip != flipTo || press != pressTo) postInvalidateOnAnimation();
            final float w = getWidth(), h = getHeight();
            if (w <= 0f || h <= 0f) return;
            drawBezel(c, w, h);
            drawWell(c, w, h);
            drawRocker(c, w, h);
        }

        /** The housing: a dark slab with a bevel only on its lit edge. */
        private void drawBezel(Canvas c, float w, float h) {
            final float rad = dp(5.2f);
            rr.set(0, 0, w, h);
            rp.setStyle(Paint.Style.FILL);
            rp.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{0xFF35353F, 0xFF1D1D25, 0xFF0E0E14},
                    new float[]{0f, 0.38f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(rr, rad, rad, rp);

            // **A one-pixel bevel, drawn rather than shadowed.** It is the whole
            // reason the slab reads as a turned part instead of a grey rectangle.
            rp.setStyle(Paint.Style.STROKE);
            rp.setStrokeWidth(Math.max(1f, dp(0.8f)));
            rp.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{0x9EFFFFFF, 0x1FFFFFFF, 0x00000000},
                    new float[]{0f, 0.34f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            rr.set(dp(0.45f), dp(0.45f), w - dp(0.45f), h - dp(0.45f));
            c.drawRoundRect(rr, rad - dp(0.45f), rad - dp(0.45f), rp);
            rp.setShader(null);
            rp.setStyle(Paint.Style.FILL);
        }

        /** The recess: black, with the lamp's own colour leaking out of it. */
        private void drawWell(Canvas c, float w, float h) {
            final float t = flip;
            final float ix = dp(2.6f), iy = dp(2.4f);
            final float rad = dp(3.6f);
            rr.set(ix, iy, w - ix, h - iy);
            if (rr.width() <= 0f || rr.height() <= 0f) return;

            rp.setStyle(Paint.Style.FILL);
            rp.setShader(new RadialGradient(w * 0.5f, h * 0.5f,
                    Math.max(rr.width(), rr.height()) * 0.62f,
                    new int[]{UiDraw.blend(0xFF08080C, 0xFF3A0C03, t),
                              UiDraw.blend(0xFF030304, 0xFF150402, t),
                              0xFF000000},
                    new float[]{0f, 0.62f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRoundRect(rr, rad, rad, rp);
            rp.setShader(null);

            wellClip.reset();
            wellClip.addRoundRect(rr, rad, rad, Path.Direction.CW);
            c.save();
            c.clipPath(wellClip);

            // the lip's shadow at the top, and the pad's shadow at the bottom
            rp.setShader(new LinearGradient(0, iy, 0, iy + dp(4.6f),
                    new int[]{0xE0000000, 0x00000000}, null,
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(rr.left, iy, rr.right, iy + dp(4.6f), rp);
            rp.setShader(new LinearGradient(0, rr.bottom - dp(3.2f), 0, rr.bottom,
                    new int[]{0x00000000, 0xB8000000}, null,
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(rr.left, rr.bottom - dp(3.2f), rr.right, rr.bottom, rp);
            rp.setShader(null);

            // **The walls catch the lamp.** Two red bands down the long edges --
            // that is what makes the light read as *inside* the recess.
            if (t > 0.01f) {
                final float[] xs = {rr.left, rr.right};
                for (int i = 0; i < 2; ++i) {
                    rp.setShader(new RadialGradient(xs[i], h * 0.5f, dp(19f),
                            new int[]{UiDraw.withAlpha(0xFFFF0015, (int) (165 * t)),
                                      UiDraw.withAlpha(0xFFFF0015, (int) (40 * t)),
                                      0x00000000},
                            new float[]{0.06f, 0.42f, 1f},
                            android.graphics.Shader.TileMode.CLAMP));
                    c.drawRect(rr.left, iy, rr.right, rr.bottom, rp);
                }
                rp.setShader(null);
            }
            c.restore();
        }

        /** The paddle itself: two trapezoids on one pivot, lit at the proud end. */
        private void drawRocker(Canvas c, float w, float h) {
            final float t = flip;
            final float ix = dp(2.6f), iy = dp(2.4f);
            final float rx0 = ix + w * 0.070f;
            final float rx1 = w - rx0;
            final float ry0 = iy + h * 0.030f;
            final float ry1 = h - ry0;
            if (rx1 - rx0 < dp(6f) || ry1 - ry0 < dp(10f)) return;

            final float cx = w * 0.5f;
            final float hw = (rx1 - rx0) * 0.5f;
            final float mid = (ry0 + ry1) * 0.5f;

            // **The tilt is in the silhouette.** The proud end is wider, because
            // it is nearer; the sunken end is narrower *and* shorter, because the
            // well's lip eats it. Neither of those is a shadow.
            final float k = 1f - 2f * t;                     // +1 dark, -1 lit
            final float topHW = hw * (1f + 0.080f * k);
            final float botHW = hw * (1f - 0.080f * k);
            final float sink = dp(2.0f) * (1f - press * 0.30f);
            final float y0 = ry0 + sink * t;
            final float y1 = ry1 - sink * (1f - t);

            up.reset();
            up.moveTo(cx - topHW, y0);
            up.lineTo(cx + topHW, y0);
            up.lineTo(cx + hw, mid);
            up.lineTo(cx - hw, mid);
            up.close();

            dn.reset();
            dn.moveTo(cx - hw, mid);
            dn.lineTo(cx + hw, mid);
            dn.lineTo(cx + botHW, y1);
            dn.lineTo(cx - botHW, y1);
            dn.close();

            all.reset();
            all.addPath(up);
            all.addPath(dn);

            // ---- the proud face: rim light at the outer edge, black at the pivot
            rp.setStyle(Paint.Style.FILL);
            rp.setShader(new LinearGradient(0, y0, 0, mid,
                    new int[]{UiDraw.blend(0xFFB6B2AA, 0xFF191520, t),
                              UiDraw.blend(0xFF38323A, 0xFF120F18, t),
                              UiDraw.blend(0xFF1E1A22, 0xFF22101A, t),
                              UiDraw.blend(0xFF151218, 0xFF331016, t)},
                    new float[]{0f, 0.10f, 0.50f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawPath(up, rp);

            // ---- the sunken face: black at the pivot, and the lamp under it
            rp.setShader(new LinearGradient(0, mid, 0, y1,
                    new int[]{UiDraw.blend(0xFF3A2E33, 0xFFC0420C, t),
                              UiDraw.blend(0xFF241E27, 0xFFE06C18, t),
                              UiDraw.blend(0xFF2E1216, 0xFFFF9A34, t)},
                    new float[]{0f, 0.55f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawPath(dn, rp);
            rp.setShader(null);

            c.save();
            c.clipPath(all);

            // **The lamp is behind the acrylic.** A warm core through the middle
            // of the sunken half is what makes the glow read as transmitted
            // rather than as an orange gradient painted on a dark face.
            if (t > 0.01f) {
                rp.setShader(new RadialGradient(cx, mid + (y1 - mid) * 0.56f,
                        Math.max(dp(6f), (rx1 - rx0) * 0.74f),
                        new int[]{UiDraw.withAlpha(0xFFFFC888, (int) (118 * t)),
                                  UiDraw.withAlpha(0xFFFF8A30, (int) (46 * t)),
                                  0x00000000},
                        new float[]{0f, 0.46f, 1f},
                        android.graphics.Shader.TileMode.CLAMP));
                c.drawRect(rx0, mid, rx1, y1, rp);
                rp.setShader(null);
            }

            // ---- brushed acrylic: ribs, then one broad sheen across them
            final float step = dp(1.6f);
            rp.setStyle(Paint.Style.STROKE);
            rp.setStrokeWidth(dp(0.6f));
            rp.setColor(UiDraw.withAlpha(0xFFFFFFFF, 14));
            for (float x = rx0 + step * 0.5f; x < rx1; x += step) {
                c.drawLine(x, y0, x, y1, rp);
            }
            rp.setStyle(Paint.Style.FILL);
            rp.setShader(new LinearGradient(rx0, 0, rx1, 0,
                    new int[]{0x00FFFFFF, 0x16FFFFFF, 0x06FFFFFF,
                              0x1CFFFFFF, 0x04FFFFFF, 0x0EFFFFFF, 0x00FFFFFF},
                    new float[]{0f, 0.09f, 0.26f, 0.39f, 0.63f, 0.85f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(rx0, y0, rx1, y1, rp);
            rp.setShader(null);

            // ---- the terminator where the well's lip cuts the sunken end
            if (t < 0.99f) {
                rp.setShader(new LinearGradient(0, y1 - dp(3.4f), 0, y1,
                        new int[]{0x00000000, UiDraw.withAlpha(0xFF000000, (int) (185 * (1f - t)))},
                        null, android.graphics.Shader.TileMode.CLAMP));
                c.drawRect(rx0, y1 - dp(3.4f), rx1, y1, rp);
            }
            if (t > 0.01f) {
                rp.setShader(new LinearGradient(0, y0, 0, y0 + dp(3.4f),
                        new int[]{UiDraw.withAlpha(0xFF000000, (int) (185 * t)), 0x00000000},
                        null, android.graphics.Shader.TileMode.CLAMP));
                c.drawRect(rx0, y0, rx1, y0 + dp(3.4f), rp);
            }
            rp.setShader(null);

            // ---- the pivot: one cut, one highlight under it
            final float seam = mid;
            rp.setStyle(Paint.Style.STROKE);
            rp.setStrokeWidth(Math.max(1f, dp(1.0f)));
            rp.setColor(UiDraw.blend(0xFF09070B, 0xFF7E2C09, t));
            c.drawLine(cx - hw, seam, cx + hw, seam, rp);
            rp.setStrokeWidth(dp(0.8f));
            rp.setColor(UiDraw.blend(0x30FFFFFF, 0x66FFC384, t));
            c.drawLine(cx - hw * 0.92f, seam + dp(1.05f),
                       cx + hw * 0.92f, seam + dp(1.05f), rp);
            rp.setStyle(Paint.Style.FILL);

            // ---- I and O, cut into the paddle
            final float barH = Math.max(dp(3.0f), (mid - y0) * 0.30f);
            final float barW = Math.max(1f, dp(1.3f));
            final float cyI = y0 + (mid - y0) * 0.33f;
            final float cyO = y1 - (y1 - mid) * 0.33f;
            final float ringD = Math.min((rx1 - rx0) * 0.34f, (y1 - mid) * 0.42f);

            // engraved: a hair of black under the ink, then the ink
            rp.setStyle(Paint.Style.FILL);
            rp.setColor(0x66000000);
            rr.set(cx - barW * 0.5f, cyI - barH * 0.5f + dp(0.7f),
                   cx + barW * 0.5f, cyI + barH * 0.5f + dp(0.7f));
            c.drawRoundRect(rr, barW * 0.45f, barW * 0.45f, rp);
            rp.setColor(UiDraw.blend(0xFFF3F0EA, 0xFF4A322C, t));
            rr.set(cx - barW * 0.5f, cyI - barH * 0.5f,
                   cx + barW * 0.5f, cyI + barH * 0.5f);
            c.drawRoundRect(rr, barW * 0.45f, barW * 0.45f, rp);

            rp.setStyle(Paint.Style.STROKE);
            rp.setStrokeWidth(Math.max(1f, dp(1.05f)));
            rp.setColor(0x66000000);
            c.drawCircle(cx, cyO + dp(0.7f), ringD * 0.5f, rp);
            rp.setColor(UiDraw.blend(0xFF736D68, 0xFFFFD9A4, t));
            c.drawCircle(cx, cyO, ringD * 0.5f, rp);
            rp.setStyle(Paint.Style.FILL);

            // ---- the finger's own shadow while it is down
            if (press > 0.01f) {
                rp.setColor(UiDraw.withAlpha(0xFF000000, (int) (46 * press)));
                c.drawPath(all, rp);
            }
            c.restore();
        }
    }
