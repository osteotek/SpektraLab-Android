package app.spektralab.ui;

import android.graphics.*;
import app.spektralab.NegativeDesk;

/** A single print flying from the wall to the negative desk (extracted). */
    public final class FlyView extends android.view.View {
    private final LightDesk lightDesk;
    private final float density;
    private int dp(float v) { return Math.round(v * density); }
    private static long nowMs() { return android.os.SystemClock.elapsedRealtime(); }
        private static final int MESH = 12;
        private static final long PICK_MS = 90L, FLY_DELAY = 60L, FLY_MS = 420L;
        public Bitmap bmp;
        /** 起点：这一格在墙的纸面坐标里（还没过鱼眼）。 */
        public final android.graphics.RectF sheet = new android.graphics.RectF();
        /** 终点：舞台矩形（与 `NegativeDesk.drawOne` 同一套边距）。 */
        public final android.graphics.RectF stage = new android.graphics.RectF();
        /** 墙上那张卡裁的是源图的哪一块（源像素）。 */
        public final int[] crop = new int[4];
        /** 这一帧画出来的包围盒，投影要用。 */
        public final android.graphics.RectF now = new android.graphics.RectF();
        private final float[] verts = new float[(MESH + 1) * (MESH + 1) * 2];
        private final float[] pt = new float[2];
        private final android.graphics.RectF shadowRect = new android.graphics.RectF();
        private final android.graphics.Paint p =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG
                        | android.graphics.Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.Paint shade =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.ColorMatrix cm = new android.graphics.ColorMatrix();
        private long t0;
        private boolean landing;
        /** 拾起的 5%：只在「刚被拿起」那一小段存在，落点必然是 1.0。 */
        private float pickup;
        /** 采样窗口：裁切 → 整张。 */
        private float winL, winT, winR, winB;

        public FlyView(android.content.Context c, LightDesk lightDesk) {
            super(c);
            this.lightDesk = lightDesk;
            this.density = c.getResources().getDisplayMetrics().density;
            setWillNotDraw(false);
            setClickable(false);
            setFocusable(false);
        }

        public void start(boolean back) {
            t0 = nowMs();
            landing = back;
            pickup = back ? 0f : 1f;
            now.set(sheet);
            invalidate();
        }

        /** 采样窗口（0..1）与顶点，`f` = 「已经在舞台上」的程度。 */
        private void build(float f) {
            final int iw = Math.max(1, bmp.getWidth()), ih = Math.max(1, bmp.getHeight());
            final boolean haveCrop = crop[2] > crop[0] && crop[3] > crop[1];
            float u0 = haveCrop ? crop[0] / (float) iw : 0f;
            float u1 = haveCrop ? crop[2] / (float) iw : 1f;
            float v0 = haveCrop ? crop[1] / (float) ih : 0f;
            float v1 = haveCrop ? crop[3] / (float) ih : 1f;
            u0 *= 1f - f;
            u1 += (1f - u1) * f;
            v0 *= 1f - f;
            v1 += (1f - v1) * f;
            winL = u0;
            winT = v0;
            winR = u1;
            winB = v1;

            // **The window and the card share their aspect at every f.** A card
            // that interpolates its own width and height separately breathes
            // ten per cent sideways halfway through -- the picture visibly
            // stretches and then settles, which is the one thing this flight
            // cannot afford. So the window decides the shape and only the width
            // is interpolated.
            final float winAsp = Math.max(0.05f,
                    ((u1 - u0) * iw) / Math.max(1e-4f, (v1 - v0) * ih));

            lightDesk.mapPoint(sheet.centerX(), sheet.centerY(), pt);
            final float c0x = pt[0], c0y = pt[1];
            final float ccx = c0x + (stage.centerX() - c0x) * f;
            final float ccy = c0y + (stage.centerY() - c0y) * f;
            final float arc = (float) Math.sin(Math.PI * f) * dp(18);
            final float cw = sheet.width() + (stage.width() - sheet.width()) * f;
            final float ch = cw / winAsp;
            final float left = ccx - cw * 0.5f;
            final float top = ccy - ch * 0.5f;
            final float scale = 1f + 0.05f * pickup * (1f - f);
            final float liftY = ccy - arc;
            final int n = MESH + 1;
            float l = Float.MAX_VALUE, t = Float.MAX_VALUE;
            float r = -Float.MAX_VALUE, btm = -Float.MAX_VALUE;
            for (int j = 0; j < n; ++j) {
                for (int i = 0; i < n; ++i) {
                    final float nu = i / (float) MESH, nv = j / (float) MESH;
                    final float su = Math.max(u0, Math.min(u1, nu));
                    final float sv = Math.max(v0, Math.min(v1, nv));
                    final float sN = (su - u0) / Math.max(1e-5f, u1 - u0);
                    final float tN = (sv - v0) / Math.max(1e-5f, v1 - v0);
                    lightDesk.mapPoint(sheet.left + sN * sheet.width(),
                            sheet.top + tN * sheet.height(), pt);
                    float px = pt[0] + (left + sN * cw - pt[0]) * f;
                    float py = pt[1] + (top + tN * ch - pt[1]) * f - arc;
                    px = ccx + (px - ccx) * scale;
                    py = liftY + (py - liftY) * scale;
                    final int o = (j * n + i) * 2;
                    verts[o] = px;
                    verts[o + 1] = py;
                    if (px < l) l = px;
                    if (px > r) r = px;
                    if (py < t) t = py;
                    if (py > btm) btm = py;
                }
            }
            now.set(l, t, r, btm);
        }

        @Override protected void onDraw(Canvas c) {
            if (bmp == null || bmp.isRecycled()) return;
            final long dt = nowMs() - t0;
            final float u = Math.max(0f, Math.min(1f, (dt - FLY_DELAY) / (float) FLY_MS));
            final float e = NegativeDesk.ease(u);
            final float f = landing ? 1f - e : e;
            if (!landing) {
                final float pu = Math.max(0f, Math.min(1f, dt / (float) PICK_MS));
                pickup = NegativeDesk.ease(pu);
            }
            build(f);
            drawShadow(c, f);
            applyTone(f);
            p.setAlpha(255);
            p.setFilterBitmap(true);
            c.drawBitmapMesh(bmp, MESH, MESH, verts, 0, null, 0, p);
            if (u < 1f) postInvalidateOnAnimation();
        }

        /**
         * **曝光也要跟着走。**
         *
         * <p>墙把每一张压到暗房的 0.76/0.73/0.68（灯亮时是 1.06 再压一点黑），
         * 而单张页画的是 `alpha = 0.55 + 0.45·lit` 的同一张纸。两边都不调的话，
         * 卡片一离开墙面就会突然变亮（或落地时突然变暗）—— 那一下正是「共享
         * 元素」最容易露馅的地方。所以这里把两组系数按 `f` 插值，起飞那帧与
         * 墙上逐像素一致，落点与舞台上逐像素一致。
         */
        private void applyTone(float f) {
            final float lt = lightDesk == null ? 0f : lightDesk.litAmount();
            // 台面上一张照片 = 0.87 透光 × 房间亮度（暗房 0.46 → 灯下 1.0），透过它
            // 看到的是灯箱面板中间那一块（`NegativeDesk.drawRoom`）。面板的那一份算成
            // 偏移：落地那一帧才和台面上的那张逐像素一致。
            final float desk = 0.87f * (0.46f + 0.54f * lt);
            final int panel = UiDraw.blend(0xFF25231F, 0xFFFDFCF8, lt);
            final float sr = lightDesk == null ? 1f : lightDesk.toneSr;
            final float sg = lightDesk == null ? 1f : lightDesk.toneSg;
            final float sb = lightDesk == null ? 1f : lightDesk.toneSb;
            final float off = lightDesk == null ? 0f : lightDesk.toneOff;
            final float r = sr + (desk - sr) * f;
            final float g = sg + (desk - sg) * f;
            final float b = sb + (desk - sb) * f;
            // 台面：照片 0.87 不透明、颜色按房间亮度压暗；透过来的 13 % 是面板。
            final float or = off + (((panel >> 16) & 255) * 0.13f - off) * f;
            final float og = off + (((panel >> 8) & 255) * 0.13f - off) * f;
            final float ob = off + ((panel & 255) * 0.13f - off) * f;
            cm.set(new float[]{
                    r, 0f, 0f, 0f, or,
                    0f, g, 0f, 0f, og,
                    0f, 0f, b, 0f, ob,
                    0f, 0f, 0f, 1f, 0f});
            p.setColorFilter(new android.graphics.ColorMatrixColorFilter(cm));
        }

        /** 拾起时的那层影：放在纸下面，飞行途中被拉长。 */
        private void drawShadow(Canvas c, float f) {
            final float a = Math.max(0f, 1f - f) * pickup * 0.55f;
            if (a <= 0.01f || now.width() <= 2f) return;
            final float w = now.width(), h = now.height();
            shadowRect.set(now.left - w * 0.08f, now.bottom - h * 0.22f,
                    now.right + w * 0.08f, now.bottom + h * 0.14f);
            shade.setShader(new android.graphics.RadialGradient(now.centerX(),
                    shadowRect.centerY(), Math.max(w, h) * 0.60f,
                    new int[]{0x88000000, 0x00000000},
                    new float[]{0f, 1f}, android.graphics.Shader.TileMode.CLAMP));
            shade.setAlpha((int) (255 * a));
            c.drawOval(shadowRect, shade);
            shade.setShader(null);
            shade.setAlpha(255);
        }
    }
