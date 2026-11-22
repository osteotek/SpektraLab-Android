package app.spektralab.ui;

import android.graphics.*;
import android.view.View;
import android.widget.TextView;
import app.spektralab.CrashTrail;
import app.spektralab.engine.SpektraLabEngine;
import static app.spektralab.AppLanguage.tr;

/** The darkroom/light-table canvas (extracted from MainActivity). */
    public final class LightDesk extends android.view.View {
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final int SOFT_PAD = 6;
    private static final int SOFT_LEVELS = 6;
    private static final float WALL_FILL = 1.18f;
    private static final int WALL_MESH = 26;
    private static final int LT_PASSES = 3;
    private static final int LT_WALL = 0xFF151310;
    private static final int LT_WALL_POOL = 0x2C2721;
    private final LightDeskHost host;
    private final float density;
    private int dp(float v) { return Math.round(v * density); }
        /** The camera sits this far in front of the path. */
        private static final float CAM_Z = 12f;
        /** Inside this radius in the XY plane a plane starts to swell. */
        private static final float FOCUS_DIST = 9f;
        /** And inside this depth. */
        private static final float Z_GATE = 18f;
        /** How large the photograph in front of the lens becomes. */
        private static final float MAX_SCALE = 4.6f;
        private float clamp01(float v) { return v < 0f ? 0f : (v > 1f ? 1f : v); }

        /** Fog: opaque inside FOG_NEAR, gone by FOG_FAR. */
        private static final float FOG_NEAR = 26f;
        private static final float FOG_FAR = 80f;
        /** What the far plane keeps: never 0, so nothing dissolves into a pop. */
        private static final float FOG_FLOOR = 0.22f;

        private final android.graphics.Paint paint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG
                        | android.graphics.Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.RectF dst = new android.graphics.RectF();
        private final android.graphics.RectF dst2 = new android.graphics.RectF();

        // ---- the sheet ------------------------------------------------------

        /** Every row holds this many prints before they start repeating. */
        private static final int GRID_COLS = 6;
        /**
         * A cell, as a share of the short side of the window.
         *
         * <p>The reference's wall is 320 px cells at zoom 0.6 -- 192 px in an
         * 880 px viewport, so four and a bit columns. That is the density to
         * hold on to: a wall you read at a glance, not a shelf of big frames.
         */
        private static final float GRID_CELL = 0.310f;
        /** The gutter, as a share of a cell -- the site's 32 px in 320. */
        private static final float GRID_GAP = 0.10f;
        /**
         * **The frame breathes with the zoom.** `calculateGapForZoom` in the
         * reference: gap 16 at zoom >= 1.0, 32 at >= 0.6, otherwise 64 -- i.e.
         * 5 %, 10 % and 20 % of a cell. Zoomed in the grid closes up; zoomed
         * out it opens. Animated, this is what makes the zoom read as the wall
         * re-flowing rather than a picture being scaled.
         */
        private float gapNow = GRID_GAP;

        private float gapFor(float z) {
            return z >= 1.30f ? 0.05f : (z >= 0.70f ? 0.10f : 0.20f);
        }
        /**
         * A cell's height over its width. The site's cells are square; change
         * this one number and the whole sheet becomes 4:5 or 3:2 -- every
         * rectangle, every hit test and every pinch anchor follows it.
         */
        private static final float GRID_ASPECT = 1f;
        /**
         * How much further in than a plain `cover` the crop goes.
         *
         * <p>**This is the number that was rendered offline and approved**
         * (`/sdcard/Download/wall_sim.png`): `cover * 1.18`, cropped about the
         * centre. On the real host.getAlbum() it leaves 0.00-0.09 bright pixels along
         * each cell edge, i.e. the rebate is gone and the picture reaches the
         * border.
         */
        private static final float GRID_FILL = WALL_FILL;
        private static final float GRID_ZOOM_MIN = 0.20f;
        private static final float GRID_ZOOM_MAX = 2.40f;
        /**
         * **The four sizes the wall can be looked at**, as the reference's
         * ZOOM OUT / NORMAL / ZOOM IN / FIT. A pinch moves between them; it no
         * longer leaves the wall at an arbitrary size.
         */
        private static final float[] ZOOM_STEPS = {0.50f, 1.00f, 1.67f};
        /** Where a pinch lands when it is let go. */
        private float zoomTarget = 1f;
        /** The tween the reference runs: 1.2 s, cubic-bezier(.87,0,.13,1). */
        public float zoomFrom = 1f, zoomTo = 1f, gapFrom = GRID_GAP, gapTo = GRID_GAP;
        private long zoomStart = 0L;
        private float zoomSecs = 0f;

        /** The reference's `CustomEase.create("smooth", ".87,0,.13,1")`. */
        private static float smoothEase(float u) {
            if (u <= 0f) return 0f;
            if (u >= 1f) return 1f;
            float t = u;
            for (int i = 0; i < 6; ++i) {
                final float m = 1f - t;
                final float x = 3f * m * m * t * 0.87f + 3f * m * t * t * 0.13f
                        + t * t * t - u;
                final float d = 3f * m * m * 0.87f
                        + 6f * m * t * (0.13f - 0.87f)
                        + 3f * t * t * (1f - 0.13f);
                if (Math.abs(d) < 1e-4f) break;
                t -= x / d;
                t = Math.max(0f, Math.min(1f, t));
            }
            // y(s) = 3(1-s) s^2 + s^3 = smoothstep
            return t * t * (3f - 2f * t);
        }

        private void startZoom(float to, float secs) {
            // `to` is what the user asked to see, so the animation runs in
            // visible zoom and is divided back down into `gZoom` every frame
            zoomFrom = visibleZoom();
            zoomTo = to;
            gapFrom = gapNow;
            gapTo = gapFor(to);
            zoomStart = System.nanoTime();
            zoomSecs = Math.max(0.05f, secs);
            zoomTarget = to;
            postInvalidateOnAnimation();
        }
        private int lastPct = -1;
        /** Told whenever the size changes, so the bar can follow. */
        public Runnable zoomChanged;
        /** Past this much pinch the sheet has become the room. */
        private static final float GRID_FLIGHT_ZOOM = 2.0f;
        /** How far the sheet keeps growing while it dissolves. */
        private static final float GRID_MORPH_ZOOM = 3.2f;

        /** The sheet's origin, in pixels: drag it and the world moves. */
        private float gOx, gOy;
        /** Pixels per second, left over from the last finger. */
        private float gVx, gVy;
        private boolean panning;
        private float gLastX, gLastY, gDownX, gDownY;
        private long gDownAt, gLastT;
        /** The pinch: the factor the fingers have put on the cells. */
        private float gZoom = 1f, gPinch = -1f, gPinch0 = 1f, gPinchX, gPinchY;
        /** The print the caption was last asked about, so it is asked once. */
        private int gShown = -1;

        // ---- the wall's pattern ---------------------------------------------

        /** The reference's wall is 12 wide and 8 tall; ours grows in rows. */
        private static final int WALL_COLS = 12;
        /**
         * **Twelve rows, not eight.** The dome reads the sheet out to 1.8
         * screens' radius at the corner, so a wall eight rows tall leaves the
         * top and bottom corners of the window empty on any phone taller than
         * it is wide -- about a third of a 2400 px screen at zoom 1. Twelve is
         * how many rows that is.
         */
        private static final int GRID_MIN_ROWS = 12;
        private int[] wall = new int[WALL_COLS * GRID_MIN_ROWS];
        private int wallRows = GRID_MIN_ROWS;
        private int wallFor = -1;

        /**
         * Which print sits at (row, col).
         *
         * <p>**Not `(row * 6 + col) % n`.** With twenty-odd prints that repeats
         * every four rows, and a wall whose top half comes back in the bottom
         * half is wallpaper, not a wall. A shuffled 16 x 16 block keeps the same
         * print ten cells apart at the very least, and the block itself comes
         * back about two screens down.
         */
        private int printAt(int row, int col) {
            final int n = host.getAlbum().size();
            if (n <= 0) return 0;
            buildWall(n);
            if (row < 0 || col < 0 || col >= WALL_COLS || row >= wallRows) return 0;
            return wall[row * WALL_COLS + col] % n;
        }

        private int wallRows() {
            buildWall(Math.max(1, host.getAlbum().size()));
            return wallRows;
        }

        private void buildWall(int n) {
            if (wallFor == n && wall.length > 0) return;
            wallFor = n;
            // **Finite, like the reference's.** As many rows as the host.getAlbum() needs,
            // never fewer than eight, so FIT has a wall to fit.
            wallRows = Math.max(GRID_MIN_ROWS, (n + WALL_COLS - 1) / WALL_COLS);
            final int total = WALL_COLS * wallRows;
            if (wall.length != total) wall = new int[total];
            for (int i = 0; i < total; ++i) wall[i] = i % n;
            final java.util.Random rnd = new java.util.Random(0x5EED5EEDL ^ n);
            for (int i = total - 1; i > 0; --i) {
                final int j = rnd.nextInt(i + 1);
                final int t = wall[i]; wall[i] = wall[j]; wall[j] = t;
            }
            // **No two neighbours alike.** The eye finds a pair instantly, even
            // in a crowd, and a pair is what makes a wall read as a pattern.
            for (int pass = 0; pass < 16; ++pass) {
                boolean clean = true;
                for (int r = 0; r < wallRows; ++r) {
                    for (int c = 0; c < WALL_COLS; ++c) {
                        final int i = r * WALL_COLS + c;
                        final int right = r * WALL_COLS + (c + 1) % WALL_COLS;
                        final int down = ((r + 1) % wallRows) * WALL_COLS + c;
                        if (wall[i] == wall[right] || wall[i] == wall[down]) {
                            final int q = rnd.nextInt(total);
                            final int t = wall[i]; wall[i] = wall[q]; wall[q] = t;
                            clean = false;
                        }
                    }
                }
                if (clean) break;
            }
        }

        /** The room's own pinch, so two fingers closing can back out of it. */
        private float fPinch = -1f;
        /** When the room opened, so the fingers that opened it cannot close it. */
        private long enteredAt = 0L;
        /** The pinch that brought us back must not also shrink the wall. */
        private boolean gLockZoom;
        /** Centre the sheet at the next chance, once the window has a size. */
        private boolean recentre;
        /** A DOWN the room's handler saw itself: a pinch that began on the
         *  sheet must not end as a tap in here. */
        private boolean fDown, gDown;

        /** 0 = the sheet, 1 = the room. Eased, never stepped. */
        private float flightIn = 0f;
        public float flightTarget = 0f;
        private long morphNanos = 0L;
        private float morphZoom0 = 1f, morphX, morphY;

        private final android.graphics.RectF dstTile = new android.graphics.RectF();
        private final android.graphics.RectF srcTile = new android.graphics.RectF();
        private final android.graphics.Paint gridInk =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.Paint gridPic =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG
                        | android.graphics.Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.Matrix gridM = new android.graphics.Matrix();

        // ---- the loupe ------------------------------------------------------
        /** Held down: a glass over the print under the finger. */
        private boolean loupeOn;
        private float loupeX, loupeY;
        /** 0..1, eased so the lens arrives and leaves instead of blinking. */
        private float loupeA;

        // ---- the loupe that lives on the table ------------------------------

        /** It is on the glass, and it shows one frame at a time, sharp. */
        private boolean toolOn;
        /** 0..1: the hand setting it down, or lifting it off. */
        private float toolT;
        private float toolX, toolY;
        private float toolVX, toolVY;
        private boolean toolDrag;
        private float toolGrabX, toolGrabY;
        private float toolLastX, toolLastY;
        private long toolLastT, toolDownT;
        private boolean toolMoved;
        private float toolMag = 2.5f;
        private long toolNanos;
        private final android.graphics.Paint toolPaint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG
                        | android.graphics.Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.Path toolClip = new android.graphics.Path();
        /** Set while `drawGrid` is being called for the glass. */
        private boolean toolPass;
        /** The plane-space disc that pass has to cover, 0 when off. */
        private float toolCullX, toolCullY, toolCullR;
        // ---- the sheet's boxes, rebuilt every frame -------------------------

        /**
         * **Columns, not rows.**
         *
         * <p>Every print keeps the column it has always been in and the order it
         * has always been in; a column is one width for the whole sheet, so the
         * sheet never re-flows sideways and nothing has to be shuffled across
         * columns to make the layout work. What changes is only the *height* of
         * each print inside its column: the dark room's square crop becomes the
         * frame's own shape, stacked with the same compact gutter as everything
         * else -- no row is ever as tall as its tallest print, so a landscape
         * never gets a band of paper above and below it.
         *
         * <p>The column widens by exactly `WALL_FILL` -- the amount the dark
         * room had cropped away. That is the one width where the *content* is
         * the same size in both states: the crop took 18 % of the frame, the
         * light gives that 18 % back, and nothing zooms. A wider column than
         * that would magnify the prints (and then the zoom bar would be lying
         * about what the wall is showing); a narrower one would shrink them.
         */
        private float[] colTop = new float[0];     // per column, per row
        private float litColW = 1f;
        private float albumA = 1.33f;
        private float wallW = 1f, wallH = 1f;
        // ---- the middle of the window, held still ---------------------------

        /**
         * **The print in the middle of the window does not move.** When the
         * lamp is thrown the sheet changes shape under it -- every print gets
         * wider or taller and every column below the first slides down -- and
         * without this the one you were looking at would simply leave. So the
         * print under the middle, and the point of it that was under the
         * middle, are recorded before the light moves, and the scroll offset is
         * corrected every frame to put that same point back. Everything else
         * then opens away from it, in every direction, which is the "radiating
         * from the middle" the wall should have had from the start.
         */
        private int anchorRow = -1, anchorCol = -1;
        private float anchorFx, anchorFy;
        private float anchorPx, anchorPy;

        private void captureAnchor() {
            final int w = getWidth(), h = getHeight();
            anchorRow = -1;
            if (w <= 0 || h <= 0 || host.getAlbum().isEmpty()) return;
            buildDome(w, h);
            domeInvAt(w * 0.5f, h * 0.5f, domeTmp);
            anchorPx = domeTmp[0];
            anchorPy = domeTmp[1];
            buildRows();
            final int rows = wallRows();
            final float px = Math.max(1f, pitchX());
            int col = (int) Math.floor((anchorPx - gOx) / px);
            col = Math.max(0, Math.min(WALL_COLS - 1, col));
            final int o = col * (rows + 1);
            int row = 0;
            for (int r = 0; r < rows; ++r) {
                if (anchorPy >= gOy + colTop[o + r]) row = r; else break;
            }
            anchorRow = row;
            anchorCol = col;
            anchorFx = clamp01((anchorPx - boxX(row, col)) / Math.max(1f, boxW(row, col)));
            anchorFy = clamp01((anchorPy - boxY(row, col)) / Math.max(1f, boxH(row, col)));
        }

        private void holdAnchor() {
            if (anchorRow < 0 || anchorCol < 0) return;
            final float bx = boxX(anchorRow, anchorCol)
                    + anchorFx * boxW(anchorRow, anchorCol);
            final float by = boxY(anchorRow, anchorCol)
                    + anchorFy * boxH(anchorRow, anchorCol);
            gOx += anchorPx - bx;
            gOy += anchorPy - by;
        }

        /** No extra punch: the width already grows to the shape it has to be. */
        private static final float LIT_EXTRA = 1.0f;

        /**
         * **How much wider the sheet is drawn once the light is on.**
         *
         * <p>This is a *layout* factor, not a zoom step, but the two multiply:
         * the wall on screen is `gZoom × litGrowth()`. Which means the zoom bar
         * has to speak in that product -- otherwise the bar says 100 % while the
         * wall is at 118 %, and the next press of 放大 jumps to 167 % *of the
         * already-enlarged* wall, which is 197 % of what the user asked for.
         * `visibleZoom()` is that product, and it is the only thing the bar, the
         * percentage readout and the three steps are allowed to use.
         */
        float litGrowth() { return 1f + (WALL_FILL * LIT_EXTRA - 1f) * litCards; }

        float visibleZoom() { return gZoom * litGrowth(); }

        private float aspectAt(int r, int c) {
            final int k = printAt(r, c);
            if (k < 0 || k >= host.getAlbum().size()) return 1f;
            final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(k).key());
            if (b == null || b.isRecycled() || b.getWidth() <= 0) return 1f;
            return b.getWidth() / (float) Math.max(1, b.getHeight());
        }

        private void buildRows() {
            final int rows = wallRows();
            final int n = WALL_COLS * (rows + 1);
            if (colTop.length < n) colTop = new float[n];
            // the widest shape on the wall decides the column
            float want = 1f;
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < WALL_COLS; ++c) {
                    final float a = aspectAt(r, c);
                    final float m = Math.max(a, 1f / a);
                    if (m > want) want = m;
                }
            }
            albumA = Math.max(1.0f, Math.min(1.6f, want));
            final float lt = litCards;
            litColW = cell() * litGrowth();
            final float gap = gapEff() * cell();
            wallW = 1f;
            wallH = 1f;                 // it only ever grew: the sheet would
            for (int c = 0; c < WALL_COLS; ++c) {   // never be able to scroll up
                final int o = c * (rows + 1);
                float y = 0f;
                for (int r = 0; r < rows; ++r) {
                    colTop[o + r] = y;
                    y += cardHeightAt(r, c) + gap;
                }
                colTop[o + rows] = Math.max(1f, y - gap);
                if (colTop[o + rows] > wallH) wallH = colTop[o + rows];
            }
            wallH = Math.max(1f, wallH);
            wallW = Math.max(1f, WALL_COLS * litColW
                    + (WALL_COLS - 1) * gap);
        }

        /** A print's height: its square tile in the dark, its own shape in the light. */
        private float cardHeightAt(int r, int c) {
            final float lt = litCards;
            if (lt <= 0f) return cellH();
            final float a = aspectAt(r, c);
            final float h = litColW / Math.max(0.2f, a);
            return cellH() + (h - cellH()) * lt;
        }

        private float boxX(int r, int c) { return gOx + c * pitchX(); }

        private float boxY(int r, int c) {
            final int rows = wallRows();
            return gOy + colTop[c * (rows + 1) + r];
        }

        private float boxW(int r, int c) { return litColW; }

        private float boxH(int r, int c) { return cardHeightAt(r, c); }

        /** Where it is drawn, which trails where it is put: glass on felt. */
        private float toolDrawX, toolDrawY;
        /** When the magnification was last touched. */
        private long toolMagAt;
        /** The pinch in progress: the span and the magnification it started at. */
        private float toolSpan0 = -1f, toolMag0 = 1f, toolPinchX, toolPinchY;


        /** **The glass is 55 % of the window across**: 200-220 dp on a phone. */
        private float toolR() {
            return Math.max(96f, getWidth() * 0.275f);
        }

        public void setTool(boolean on) {
            if (toolOn == on) return;
            toolOn = on;
            if (on) {
                if (toolX <= 0f || toolY <= 0f) {
                    toolX = getWidth() * 0.5f;
                    toolY = getHeight() * 0.44f;
                }
                toolVX = toolVY = 0f;
                toolNanos = 0L;
                toolDrawX = toolX;
                toolDrawY = toolY;
                toolMagAt = android.os.SystemClock.uptimeMillis();
                // the quick press-and-hold lens steps aside: two lenses on one
                // table is two answers to the same question
                loupeOn = false;
                loupeA = 0f;
                removeCallbacks(armLoupe);
            } else {
                toolDrag = false;
                toolVX = toolVY = 0f;
            }
            clampTool();
            host.markFile("loupe " + (on ? "on at " + Math.round(toolX) + ","
                    + Math.round(toolY) : "off"));
            postInvalidateOnAnimation();
        }

        public boolean isToolOn() { return toolOn; }

        private void clampTool() {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            final float m = toolR() * 1.18f;
            if (toolX < m) toolX = m; else if (toolX > w - m) toolX = w - m;
            if (toolY < m) toolY = m; else if (toolY > h - m) toolY = h - m;
        }

        /** The place, the slide, and the ask for a better copy of the print. */
        private void advanceTool() {
            final float target = toolOn ? 1f : 0f;
            final boolean coasting = toolVX != 0f || toolVY != 0f;
            final boolean trailing = Math.abs(toolX - toolDrawX)
                    + Math.abs(toolY - toolDrawY) > 0.5f;
            if (toolT == target && !coasting && !trailing) {
                // the magnification still has to fade itself out
                if (toolOn && android.os.SystemClock.uptimeMillis() - toolMagAt < 1520L) {
                    postInvalidateOnAnimation();
                }
                return;
            }
            final long now = System.nanoTime();
            final float dt = toolNanos == 0L ? 0.016f
                    : Math.min(0.05f, (now - toolNanos) / 1e9f);
            toolNanos = now;
            if (toolT != target) {
                // **Set down, not faded in**: 110 ms to the eye, which with the
                // 1.12 scale is the weight of the thing landing on glass.
                final float k = 1f - (float) Math.exp(-dt / 0.11f);
                toolT += (target - toolT) * k;
                if (Math.abs(target - toolT) < 0.004f) toolT = target;
            }
            if (!toolDrag && coasting) {
                toolX += toolVX * dt;
                toolY += toolVY * dt;
                final float damp = (float) Math.exp(-dt * 3.6f);
                toolVX *= damp;
                toolVY *= damp;
                if (Math.abs(toolVX) < 8f) toolVX = 0f;
                if (Math.abs(toolVY) < 8f) toolVY = 0f;
                clampTool();
            }
            // **Glass on felt, not a cursor.** What is drawn trails where the
            // lens has been put by 50-80 ms -- and the lag grows with the
            // magnification, because at 5x a one-pixel wobble at the finger is
            // five pixels of grain sliding about under the glass.
            final float kd = 1f - (float) Math.exp(-dt * (20f - 2.6f * (toolMag - 2f)));
            toolDrawX += (toolX - toolDrawX) * kd;
            toolDrawY += (toolY - toolDrawY) * kd;
            if (Math.abs(toolX - toolDrawX) < 0.4f) toolDrawX = toolX;
            if (Math.abs(toolY - toolDrawY) < 0.4f) toolDrawY = toolY;
            postInvalidateOnAnimation();
        }


        /** Is this point on the glass or its skirt? */
        private boolean insideTool(float x, float y, float R) {
            return Math.hypot(x - toolDrawX, y - toolDrawY) <= R * 1.30f;
        }

        /**
         * **One finger carries it, two fingers focus it.**
         *
         * <p>Grab the skirt and the lens hangs where it is; grab the glass and
         * it lifts clear of the hand, because a thumb over the middle of a
         * loupe is a thumb over the picture. Two fingers pinch the
         * magnification continuously between 2.0 and 5.0 and carry the lens at
         * the same time, and when one of them lifts the grab is re-measured
         * from the finger that stayed -- so handing back to a single finger
         * cannot move the lens by a pixel.
         *
         * <p>Everything that starts on the glass is consumed, so the wall never
         * zooms while a loupe is being focused.
         */
        private boolean toolTouch(android.view.MotionEvent e) {
            final float R = toolR();
            switch (e.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN: {
                    if (toolT < 0.6f) return false;
                    if (!insideTool(e.getX(), e.getY(), R)) return false;
                    toolDrag = true;
                    toolVX = toolVY = 0f;
                    toolSpan0 = -1f;
                    final float d = (float) Math.hypot(e.getX() - toolDrawX,
                            e.getY() - toolDrawY);
                    final float lift = d < R * 0.55f ? -R * 0.45f : 0f;
                    toolGrabX = toolX - e.getX();
                    toolGrabY = toolY + lift - e.getY();
                    toolLastX = e.getX();
                    toolLastY = e.getY();
                    toolLastT = android.os.SystemClock.uptimeMillis();
                    toolDownT = toolLastT;
                    toolMoved = false;
                    return true;
                }
                case android.view.MotionEvent.ACTION_POINTER_DOWN: {
                    if (!toolDrag) return false;
                    final int idx = e.getActionIndex();
                    if (!insideTool(e.getX(idx), e.getY(idx), R)) {
                        // the second finger is on the table, not on the glass:
                        // hand the pinch back to the wall
                        toolDrag = false;
                        toolSpan0 = -1f;
                        return false;
                    }
                    toolSpan0 = Math.max(1f, span(e));
                    toolMag0 = toolMag;
                    toolPinchX = midX(e);
                    toolPinchY = midY(e);
                    toolVX = toolVY = 0f;
                    toolMoved = true;
                    return true;
                }
                case android.view.MotionEvent.ACTION_MOVE: {
                    if (!toolDrag) return false;
                    if (e.getPointerCount() >= 2 && toolSpan0 > 0f) {
                        final float sNow = span(e);
                        // **2.0 to 5.0, continuous, and never outside**
                        final float want = toolMag0 * (sNow / toolSpan0);
                        final float next = Math.max(2f, Math.min(5f, want));
                        if (Math.abs(next - toolMag) > 0.001f) {
                            toolMag = next;
                            toolMagAt = android.os.SystemClock.uptimeMillis();
                        }
                        final float mx = midX(e), my = midY(e);
                        toolX += mx - toolPinchX;
                        toolY += my - toolPinchY;
                        toolPinchX = mx;
                        toolPinchY = my;
                        toolMoved = true;
                        clampTool();
                        postInvalidateOnAnimation();
                        return true;
                    }
                    final long t = android.os.SystemClock.uptimeMillis();
                    final float dt = Math.max(0.001f, (t - toolLastT) / 1000f);
                    final float nx = e.getX() + toolGrabX;
                    final float ny = e.getY() + toolGrabY;
                    final float vx = (nx - toolX) / dt, vy = (ny - toolY) / dt;
                    toolX = nx;
                    toolY = ny;
                    // **The hand is slower than the eye.** A finger dragging
                    // glass over felt does not flick it at 3000 px/s, and a
                    // loupe that flew off the table would be a puck.
                    toolVX = Math.max(-2600f, Math.min(2600f,
                            toolVX * 0.55f + vx * 0.45f));
                    toolVY = Math.max(-2600f, Math.min(2600f,
                            toolVY * 0.55f + vy * 0.45f));
                    if (Math.abs(e.getX() - toolLastX) + Math.abs(e.getY() - toolLastY) > 2f) {
                        toolMoved = true;
                    }
                    toolLastX = e.getX();
                    toolLastY = e.getY();
                    toolLastT = t;
                    clampTool();
                    postInvalidateOnAnimation();
                    return true;
                }
                case android.view.MotionEvent.ACTION_POINTER_UP: {
                    if (!toolDrag) return false;
                    toolSpan0 = -1f;
                    // **hand back to one finger without a jump**: the grab is
                    // measured again from the finger that stayed
                    final int up = e.getActionIndex();
                    final int stay = up == 0 ? 1 : 0;
                    if (stay < e.getPointerCount()) {
                        toolGrabX = toolX - e.getX(stay);
                        toolGrabY = toolY - e.getY(stay);
                        toolLastX = e.getX(stay);
                        toolLastY = e.getY(stay);
                        toolLastT = android.os.SystemClock.uptimeMillis();
                    }
                    return true;
                }
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL: {
                    if (!toolDrag) return false;
                    toolDrag = false;
                    toolSpan0 = -1f;
                    final long t = android.os.SystemClock.uptimeMillis();
                    if (!toolMoved && t - toolDownT < 300) {
                        toolMag = toolMag > 3.2f ? 2.5f : 4.0f;
                        toolMagAt = t;
                        toolVX = toolVY = 0f;
                    } else if (t - toolLastT > 90) {
                        toolVX = toolVY = 0f;      // it stopped before the lift
                    }
                    postInvalidateOnAnimation();
                    return true;
                }
                default:
                    return false;
            }
        }

        /**
         * **The loupe.** A brushed bezel, an acrylic skirt that catches the
         * lightbox, a contact shadow that tightens as it lands.
         *
         * <p>And inside it, **the table itself, drawn a second time**: the same
         * `drawGrid`, under the same dome, into a canvas scaled about the middle
         * of the glass and clipped to a circle. That is the whole trick, and it
         * is why the glass shows something everywhere -- over a print, over the
         * gutter between two, over the paper at the end of the sheet, with the
         * rebate of one frame running into the next exactly as it does outside
         * the glass. Sampling one print's bitmap could never do that: the
         * moment the middle of the lens left that print there was nothing to
         * sample, which is what a transparent shell was.
         *
         * <p>It also settles the alignment: there is no mapping to get wrong,
         * because it is not a mapping, it is the same drawing code with one
         * canvas scale in front of it.
         */
        /**
         * **A piece of optical glass, floating.**
         *
         * <p>Nothing sits between the picture and the edge of the lens: one
         * hairline, one pixel of polish, one faint sheen, and a shadow you can
         * see the paper through. The stack of grey gradients that stood here was
         * an attempt at a machined barrel, and at three hundred pixels across it
         * read as dirt smeared round the picture. A magnifier is not a camera
         * lens; what makes it feel real is the picture being *clean* right up to
         * a hairline, and the glass lifting off the paper by a millimetre.
         */
        private void drawTool(android.graphics.Canvas c, int w, int h) {
            if (toolT <= 0.004f) return;
            final float R = toolR();
            final float cx = toolDrawX, cy = toolDrawY;
            final int save = c.save();
            final float sc = 1f + 0.10f * (1f - toolT);
            c.scale(sc, sc, cx, cy);
            toolPaint.setStyle(android.graphics.Paint.Style.FILL);
            toolPaint.setShader(null);
            toolPaint.setColorFilter(null);
            toolPaint.setXfermode(null);
            toolPaint.setPathEffect(null);
            toolPaint.setAlpha(255);

            // ---- 1. it floats ------------------------------------------------
            // One shadow, sixteen per cent at its centre, gone by the rim of the
            // lens: a lift, not a hole. No ring of black anywhere -- the lightbox
            // is the brightest thing on the screen and a dark halo round the
            // glass reads as a stain.
            toolPaint.setShader(new android.graphics.RadialGradient(cx, cy + R * 0.10f, R * 1.21f,
                    new int[]{0x29000000, 0x12000000, 0x00000000},
                    new float[]{0f, 0.70f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy + R * 0.10f, R * 1.21f, toolPaint);
            toolPaint.setShader(null);

            // ---- 2. the picture, through the glass ---------------------------
            toolClip.reset();
            toolClip.addCircle(cx, cy, R, android.graphics.Path.Direction.CW);
            final int save2 = c.save();
            c.clipPath(toolClip);
            c.drawColor(bgColour());
            drawRoomGlow(c, w, h);
            buildDome(w, h);
            domeInvAt(cx, cy, domeTmp);
            toolCullX = domeTmp[0];
            toolCullY = domeTmp[1];
            toolCullR = (float) Math.sqrt(2.0) * R / Math.max(2f, toolMag) + cell();
            toolWarpR = R / Math.max(2f, toolMag);
            final int save3 = c.save();
            c.scale(toolMag, toolMag, cx, cy);
            toolPass = true;
            toolWarpK = 1f;
            drawGrid(c);
            toolPass = false;
            toolWarpR = 0f;
            toolCullR = 0f;
            c.restoreToCount(save3);

            // ---- 3. the polish: one pixel of pure white, inside the edge ------
            toolPaint.setStyle(android.graphics.Paint.Style.STROKE);
            toolPaint.setShader(null);
            toolPaint.setStrokeWidth(Math.max(1f, R * 0.005f));
            toolPaint.setColor(0xB4FFFFFF);
            c.drawCircle(cx, cy, R - toolPaint.getStrokeWidth() * 0.5f, toolPaint);

            // ---- 4. the sheen: one clean arc, upper left, twelve per cent -----
            final float px = (cx - w * 0.5f) / Math.max(1f, w * 0.5f);
            final float py = (cy - h * 0.5f) / Math.max(1f, h * 0.5f);
            final int save4 = c.save();
            c.scale(1f, 0.62f, cx, cy);
            toolPaint.setStyle(android.graphics.Paint.Style.FILL);
            toolPaint.setShader(new android.graphics.RadialGradient(
                    cx - R * 0.34f + px * R * 0.10f, cy - R * 0.58f + py * R * 0.10f, R * 1.00f,
                    new int[]{0x1FFFFFFF, 0x0DFFFFFF, 0x00000000},
                    new float[]{0f, 0.54f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawCircle(cx, cy, R * 0.99f, toolPaint);
            toolPaint.setShader(null);
            c.restoreToCount(save4);
            c.restoreToCount(save2);

            // ---- 5. the frame: a hairline, cool white, with a breath of glow --
            toolPaint.setStyle(android.graphics.Paint.Style.STROKE);
            final float hair = Math.max(1.6f, R * 0.0075f);
            toolPaint.setStrokeWidth(hair * 2.6f);
            toolPaint.setColor(0x18FFFFFF);
            c.drawCircle(cx, cy, R + hair * 0.5f, toolPaint);
            toolPaint.setStrokeWidth(hair);
            toolPaint.setShader(new android.graphics.SweepGradient(cx, cy,
                    new int[]{0x9AE6EEFC, 0xE0FFFFFF, 0x9AE6EEFC, 0x70D8E2F2, 0x9AE6EEFC},
                    new float[]{0f, 0.22f, 0.50f, 0.76f, 1f}));
            c.drawCircle(cx, cy, R + hair * 0.5f, toolPaint);
            toolPaint.setShader(null);

            // ---- 6. what it is set to, outside the glass ----------------------
            final long ms = android.os.SystemClock.uptimeMillis();
            final float magA = 1f - clamp01((ms - toolMagAt - 1200f) / 280f);
            if (magA > 0.004f) {
                toolPaint.setStyle(android.graphics.Paint.Style.FILL);
                toolPaint.setTypeface(host.getTfBody());
                toolPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
                toolPaint.setTextSize(R * 0.082f);
                toolPaint.setColor(((int) (0xC0 * magA * toolT) << 24) | 0x3A3B3D);
                c.drawText(String.format(java.util.Locale.US, "%.1f×", toolMag),
                        cx, cy + R * 1.17f, toolPaint);
                if (magA < 1f) postInvalidateOnAnimation();
            }
            toolPaint.setStyle(android.graphics.Paint.Style.FILL);
            c.restoreToCount(save);
        }


        /** Which print the glass is over, and the rect it was drawn in. */
        private int loupeIndex = -1;
        private final android.graphics.RectF loupeDst = new android.graphics.RectF();
        /** Which slice of the print that cell is showing, in bitmap pixels. */
        private final android.graphics.RectF loupeSrc = new android.graphics.RectF();
        /** Where that print sits in the sheet, and its outline as it is painted. */
        private final android.graphics.RectF loupeCell = new android.graphics.RectF();
        private final android.graphics.Path loupePath = new android.graphics.Path();
        /** How much it magnifies. */
        private static final float LOUPE_MAG = 2.4f;
        /** The refracted face, rendered small and scaled up by the canvas. */
        private Bitmap lensBmp;
        private int[] lensPx;
        private int[] lensSrc;
        private Bitmap lensSrcKey;
        private int lensSrcW, lensSrcH;
        /** The lens' radius, in dp. */
        private static final float LOUPE_R_DP = 78f;
        private final Runnable armLoupe = new Runnable() {
            @Override public void run() {
                if (!isAttachedToWindow()) return;
                loupeOn = true;
                loupeX = downX;
                loupeY = downY;
                lensArmed = false;          // the lens starts under the finger
                postInvalidateOnAnimation();
            }
        };

        /**
         * Hold a print to be rid of it.
         *
         * <p>**Removal needs a gesture, not a setting.** A cell whose file is
         * gone, or is a 16-bit TIFF `BitmapFactory` cannot read, draws as a
         * black rectangle and there is nothing on the wall to press to get rid
         * of it. 620 ms is past the lens's own 140 ms and past a tap, so the
         * three gestures stay distinct: tap opens, hold offers, drag pans.
         */
        private final Runnable armPrintMenu = new Runnable() {
            @Override public void run() {
                if (!isAttachedToWindow() || moved || !gDown) return;
                final int k = at(downX, downY);
                if (k < 0 || k >= host.getAlbum().size()) return;
                loupeOn = false;
                postInvalidateOnAnimation();
                host.showPrintActions(k);
            }
        };

        // ---- the room's own light -------------------------------------------
        /** The print the light is taken from. */
        private int bgIdx = -1;
        /** Its colour, eased: the backdrop follows, it never jumps. */
        private float bgR = 1f, bgG = 1f, bgB = 1f;
        /** One average per bitmap; a photograph's key does not change. */
        private final java.util.HashMap<Bitmap, float[]> tintCache =
                new java.util.HashMap<>();

        /**
         * **The backdrop is the photograph.** DepthGallery does this: the room
         * takes its light from the print in focus, so the wall behind the pile
         * belongs to the picture being looked at. Ours is one colour pulled
         * from that print (an 8 x 8 average, lifted 86 % toward white) and
         * eased at 5.5 % a frame, so a scroll reads as the light changing.
         */
        private void backdrop(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            // **Two colours, two blobs, all the way to the edges.** DepthGallery
            // is a diffuse shadow, not a tint: the light reads as one wash at
            // the top left and a deeper one at the bottom, and neither of them
            // turns white anywhere -- the corners keep their colour.
            float ar = 1f, ag = 1f, ab = 1f, br = 1f, bg2 = 1f, bb = 1f;
            if (bgIdx >= 0 && bgIdx < host.getAlbum().size()) {
                final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(bgIdx).key());
                final float[] pair = tints(b);
                if (pair != null) {
                    ar = pair[0]; ag = pair[1]; ab = pair[2];
                    br = pair[3]; bg2 = pair[4]; bb = pair[5];
                }
            }
            lightR += (ar - lightR) * 0.06f;
            lightG += (ag - lightG) * 0.06f;
            lightB += (ab - lightB) * 0.06f;
            deepR += (br - deepR) * 0.06f;
            deepG += (bg2 - deepG) * 0.06f;
            deepB += (bb - deepB) * 0.06f;

            // **Lit, the room keeps its photograph but loses its dark.** The
            // two washes are pulled 80 % of the way to paper, so the room and
            // the wall are the same room in both states.
            final float lm = litT * 0.80f;
            final int pale = UiDraw.blend(
                    wash(lightR, lightG, lightB, 0.15f), 0xFFF4EFE7, lm);
            final int dark = UiDraw.blend(
                    wash(deepR, deepG, deepB, 0.30f), 0xFFF4EFE7, lm);
            final int glow = UiDraw.blend(
                    wash(lightR, lightG, lightB, 0.42f), 0xFFFBFAF7, lm);
            final int long_ = Math.max(w, h);

            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setAlpha(255);
            c.drawColor(pale);

            // The deep one, low and to the left.
            paint.setShader(new android.graphics.RadialGradient(
                    w * 0.14f, h * 0.86f, long_ * 0.95f,
                    new int[]{(dark & 0x00FFFFFF) | 0xC8000000,
                            (dark & 0x00FFFFFF) | 0x30000000,
                            dark & 0x00FFFFFF},
                    new float[]{0f, 0.55f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0f, 0f, w, h, paint);

            // The light one, up and to the left, as the source of the light.
            paint.setShader(new android.graphics.RadialGradient(
                    w * 0.06f, h * 0.02f, long_ * 0.72f,
                    new int[]{(glow & 0x00FFFFFF) | 0x8C000000,
                            (glow & 0x00FFFFFF) | 0x00000000},
                    new float[]{0f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0f, 0f, w, h, paint);
            paint.setShader(null);

            if (Math.abs(lightR - ar) + Math.abs(deepR - br) > 0.004f) {
                postInvalidateOnAnimation();
            }
        }

        /**
         * One wash colour: the print's own, **taken down** into the room's dark
         * by `k`.
         *
         * <p>It used to be lifted toward white. That was right while the desk
         * was paper; now the wall the photographs hang on is black, so the light
         * a print lends the room is a deep tint of itself, not a pale one.
         */
        private int wash(float r, float g, float b, float k) {
            return 0xFF000000
                    | (Math.round(255 * Math.min(1f, r * k)) << 16)
                    | (Math.round(255 * Math.min(1f, g * k)) << 8)
                    | Math.round(255 * Math.min(1f, b * k));
        }

        /** The pair, eased, as fields. */
        private float lightR = 1f, lightG = 1f, lightB = 1f;
        private float deepR = 1f, deepG = 1f, deepB = 1f;

        /**
         * The print as **two** colours: the lit side and the shadowed side.
         *
         * <p>A single average is grey -- a night frame is mostly black border.
         * Splitting the samples at their own mean luminance and taking each side
         * as a saturation-weighted mean gives the pair the backdrop is built
         * from: the cream of a wall in light, the mauve of the same wall in
         * shade. Cached per bitmap, six floats.
         */
        private float[] tints(Bitmap b) {
            if (b == null || b.isRecycled()) return null;
            float[] got = tintCache.get(b);
            if (got != null) return got;
            final int stepX = Math.max(1, b.getWidth() / 16);
            final int stepY = Math.max(1, b.getHeight() / 16);
            // A first pass for the mean luminance the split is made at.
            float lumaSum = 0f;
            int n = 0;
            for (int y = stepY / 2; y < b.getHeight(); y += stepY) {
                for (int x = stepX / 2; x < b.getWidth(); x += stepX) {
                    final int p = b.getPixel(x, y);
                    lumaSum += (((p >> 16) & 255) * 0.30f + ((p >> 8) & 255) * 0.59f
                            + (p & 255) * 0.11f) / 255f;
                    ++n;
                }
            }
            if (n == 0) return null;
            final float pivot = lumaSum / n;
            float lr = 0f, lg = 0f, lb = 0f, lw = 0f;
            float dr = 0f, dg = 0f, db = 0f, dw = 0f;
            for (int y = stepY / 2; y < b.getHeight(); y += stepY) {
                for (int x = stepX / 2; x < b.getWidth(); x += stepX) {
                    final int p = b.getPixel(x, y);
                    final float r = ((p >> 16) & 255) / 255f;
                    final float g = ((p >> 8) & 255) / 255f;
                    final float bl = (p & 255) / 255f;
                    final float mx = Math.max(r, Math.max(g, bl));
                    final float mn = Math.min(r, Math.min(g, bl));
                    final float sat = mx > 1e-3f ? (mx - mn) / mx : 0f;
                    final float wgt = sat * sat * mx + 0.02f;
                    final float luma = r * 0.30f + g * 0.59f + bl * 0.11f;
                    if (luma >= pivot) {
                        lr += r * wgt; lg += g * wgt; lb += bl * wgt; lw += wgt;
                    } else {
                        dr += r * wgt; dg += g * wgt; db += bl * wgt; dw += wgt;
                    }
                }
            }
            final float[] outv = new float[6];
            final float[] a = lw > 0f ? new float[]{lr / lw, lg / lw, lb / lw}
                    : new float[]{1f, 1f, 1f};
            final float[] d = dw > 0f ? new float[]{dr / dw, dg / dw, db / dw}
                    : new float[]{1f, 1f, 1f};
            for (int k = 0; k < 3; ++k) {
                // A push, so the wall reads as a colour and not as paper.
                final float m = (a[k] + d[k]) * 0.5f;
                outv[k] = clamp01(m + (a[k] - m) * 1.6f);
                outv[k + 3] = clamp01(m + (d[k] - m) * 1.6f);
            }
            if (tintCache.size() > 32) tintCache.clear();
            tintCache.put(b, outv);
            return outv;
        }
        /** The station's own rectangle while it is being kept clear. */
        private final android.graphics.RectF hero = new android.graphics.RectF();
        final android.graphics.RectF probe = new android.graphics.RectF();
        private final java.util.ArrayList<Plane> planes = new java.util.ArrayList<>();
        private final int[] order = new int[512];
        private final float[] depth = new float[512];

        public int infoStation = -1;
        private float camT = 0f, targetT = 0f;
        private long lastNanos = 0L;
        private float downX = 0f, downY = 0f, lastY = 0f;
        private boolean moved = false;
        private long downAt = 0L;

        public LightDesk(android.content.Context c, LightDeskHost host) {
            super(c);
            this.host = host;
            this.density = c.getResources().getDisplayMetrics().density;
        }

        // ---- the path -------------------------------------------------------

        private float curveX(float t) {
            return 26f * (float) Math.sin(TAU * t);
        }

        private float curveY(float t) {
            return 9f * (float) Math.sin(2f * TAU * t + 0.7f);
        }

        private float curveZ(float t) {
            return 18f * (float) Math.cos(TAU * t) - 18f;
        }

        private float dx(float t) {
            return 26f * TAU * (float) Math.cos(TAU * t);
        }

        private float dy(float t) {
            return 9f * 2f * TAU * (float) Math.cos(2f * TAU * t + 0.7f);
        }

        /**
         * Rebuild the room.
         *
         * <p>**Every third plane is a station**: one per file, dead ahead on the
         * path, at `t = k / n`. The camera snaps from station to station, so
         * every scroll ends with a photograph centred in the frame; the two
         * planes between stations are the room around it, scattered as before.
         */
        public void rebuild() {
            gShown = -1;
            infoStation = -1;
            planes.clear();
            final int n = host.getAlbum().size();
            if (n == 0) return;
            final int total = n * LT_PASSES;
            for (int i = 0; i < total; ++i) {
                final float t = (float) i / total;
                final boolean station = (i % LT_PASSES) == 0;
                final int index = station ? (i / LT_PASSES) % n : i % n;
                final java.util.Random rnd =
                        new java.util.Random(host.getAlbum().get(index).key().hashCode() * 31L + i);
                planes.add(new Plane(index, t,
                        station ? 0f : (rnd.nextFloat() - 0.5f) * 10.0f,   // lateral
                        station ? 0f : (rnd.nextFloat() - 0.5f) * 10.0f,   // depth
                        station ? 2.3f : 1.7f + rnd.nextFloat() * 1.4f));  // width
            }
        }

        /**
         * Opening the desk: **the sheet**, still, one-to-one.
         *
         * <p>The grid is the landing view because it is the one that shows the
         * work. The room is still there, one pinch away.
         */
        public void home() {
            flightIn = 0f;
            flightTarget = 0f;
            morphNanos = 0L;
            camT = 0f;
            targetT = 0f;
            lastNanos = 0L;
            for (Plane p : planes) p.scale = 1f;
            // **The middle of the sheet.** `gOx = 0` is the sheet's top-left
            // corner, which on a twelve-column wall is nothing but the edge:
            // every entry into the light table opened on a corner. Centred
            // instead -- and if the sheet is smaller than the window the clamp
            // would centre it anyway, so both cases agree.
            gZoom = 1f;
            gVx = gVy = 0f;
            recentre = true;
            gPinch = -1f;
            fPinch = -1f;
            gDown = fDown = false;
            panning = false;
            loupeOn = false;
            removeCallbacks(armLoupe);
            infoStation = -1;
            gShown = -1;
            host.hideInfo();
            if (host.getLightGridHint() != null) host.getLightGridHint().setVisibility(View.VISIBLE);
            if (host.getLightZoomBar() != null) host.getLightZoomBar().setVisibility(View.VISIBLE);
            zoomTarget = 1f;
            zoomTo = 1f;
            zoomSecs = 0f;
            gapNow = gapFor(1f);
            lastPct = -1;
            invalidate();
        }

        @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
            super.onSizeChanged(w, h, ow, oh);
            if (w > 0 && h > 0) {
                // One line, so a wall that is not square can be proved rather
                // than argued about.
                final float c = Math.min(w, h) * GRID_CELL;
                final int across = (int) (w / (c * (1f + GRID_GAP)));
                host.markFile("desk " + w + "x" + h + " cell " + Math.round(c) + "x"
                        + Math.round(c * GRID_ASPECT) + "px, across " + across);
                // **The build says its own name**, on screen, because three
                // rounds of "it still looks wrong" may have been three rounds
                // of an old APK.
                if (host.getLightGridHint() != null) {
                    // **Two lines.** A single line was measured before the tag
                    // was appended and the tag was clipped off the end -- which
                    // is exactly the thing it exists to prevent.
                    host.getLightGridHint().setText("双指放大 · 进入三维\n"
                            + host.appTag() + " · " + Math.round(c) + "px · " + across + tr("ui170.059"));
                }
            }
            invalidate();
        }

        /** Screen rectangle of one plane, or null when it is behind the lens. */
        private float project(Plane p, float cx, float cy, float cz, float focal,
                              int w, int h, android.graphics.RectF out) {
            final float pz = curveZ(p.t) + p.depth;
            final float dz = cz - pz;
            if (dz < 0.6f || dz > FOG_FAR + 24f) return -1f;
            final float nx = -dy(p.t), ny = dx(p.t);
            final float len = (float) Math.hypot(nx, ny);
            final float ux = len > 1e-4f ? nx / len : 0f;
            final float uy = len > 1e-4f ? ny / len : 1f;
            final float px = curveX(p.t) + ux * p.lateral;
            final float py = curveY(p.t) + uy * p.lateral;
            final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(p.index).key());
            if (b == null || b.isRecycled() || b.getWidth() <= 0) return -1f;
            final float k = focal / dz;
            final float ow = p.width * k * p.scale;
            final float oh = ow * ((float) b.getHeight() / b.getWidth());
            final float sx = w * 0.5f + (px - cx) * k;
            final float sy = h * 0.5f - (py - cy) * k;
            out.set(sx - ow * 0.5f, sy - oh * 0.5f, sx + ow * 0.5f, sy + oh * 0.5f);
            return dz;
        }

        @Override protected void onDraw(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            // How far the cross-fade between the sheet and the room has got.
            final long now = System.nanoTime();
            final float mdt = morphNanos == 0L ? 0.016f
                    : Math.min(0.06f, (now - morphNanos) / 1_000_000_000f);
            morphNanos = now;
            // **The light runs on its own clock**, whether the wall or the room
            // is the one on screen: a switch that only moves when you are
            // looking at one of them is a switch that can get stuck half way.
            advanceLit();
            // The exposure belongs to the room, not to the wall: the room draws
            // prints too, and it never calls drawGrid.
            buildTone();
            if (flightIn != flightTarget) {
                flightIn += (flightTarget - flightIn)
                        * (1f - (float) Math.exp(-mdt * 11f));
                if (Math.abs(flightTarget - flightIn) < 0.004f) {
                    flightIn = flightTarget;
                }
            }

            if (flightIn <= 0.001f) {
                // **Zoom lands on a size, it does not drift.** The pinch follows
                // the hand 1:1; letting go settles on the nearest of the four
                // sizes, eased about the middle of the window so the picture
                // being looked at stays where it is.
                if (zoomSecs > 0f) {
                    final float u = Math.min(1f, (now - zoomStart) / 1e9f / zoomSecs);
                    final float e = smoothEase(u);
                    gapNow = gapFrom + (gapTo - gapFrom) * e;
                    zoomAt((zoomFrom + (zoomTo - zoomFrom) * e) / litGrowth(),
                            w * 0.5f, h * 0.5f);
                    if (u >= 1f) {
                        zoomSecs = 0f;
                        gapNow = gapTo;
                        gZoom = zoomTo / litGrowth();
                    }
                    postInvalidateOnAnimation();
                }
                final int pct = Math.round(visibleZoom() * 100f);
                if (pct != lastPct) {
                    lastPct = pct;
                    if (zoomChanged != null) zoomChanged.run();
                }
                // **The wall is black.** `dPGKGOo` is a picture library on a
                // black page: the gutters *are* the background, so a photograph
                // reads as a window cut in a dark wall instead of a print lying
                // on white paper. Repainting it white is what made the first
                // port look like a contact sheet.
                // **The wall is a light box.** Background and prints go into a
                // half-float layer while the lamp is on, so the panel can sit
                // above SDR white and a clear piece of film above the panel.
                // The glass and the tool bar stay on the window's own canvas:
                // they are chrome, and chrome must not be over-exposed.
                // **The panel is drawn straight onto the window, not into a
                // layer of its own.**
                //
                // The layer was 20 MB of half-float rebuilt and composited every
                // frame, and the wall is the one screen in this app that runs on
                // a clock: the dome, forty cells, the blur ladder, the grain.
                // An extra full-screen surface is the difference between fluid
                // and a slideshow on a phone. The window is already half-float
                // when HDR is on, so an opaque patch of the panel colour lands
                // above SDR white with no intermediate anything.
                // **1.35, not 1.5.** At 1.5 the panel was bright enough to make
                // every print's edge a hard silhouette and the grain a speckle;
                // the brief's own range is 1.25-1.45, and 1.35 still reads as a
                // lit panel while the prints stay the subject.
                final Bitmap panel = hdrPanel()
                        ? panelPatch(bgColour(), 1f + 0.35f * Math.max(0f, Math.min(1f, litT)))
                        : null;
                if (panel != null) {
                    panelRect.set(0, 0, w, h);
                    c.drawBitmap(panel, null, panelRect, hdrLayerPaint);
                } else {
                    c.drawColor(bgColour());
                }
                drawRoomGlow(c, w, h);
                if (litT > 0.001f) drawLightWave(c, w, h);
                buildTone();
                // **The glass is settled before the wall is drawn**, so the
                // magnified pass in `drawTool` uses this frame's position and
                // not the last one's.
                advanceTool();
                drawGrid(c);
                // **The page's own vignette** (`page-vignette-extreme`): the
                // wall does not stop at the edge of the screen, it goes out of
                // the light. Without it the top row is a row of photographs cut
                // in half by the status bar.
                vignette(c, w, h);
                drawGrain(c, w, h);
                drawLoupe(c, w, h);
                drawTool(c, w, h);
                if (loupeOn || loupeA > 0f) postInvalidateOnAnimation();
                gridIdle();
                return;
            }

            // The room is the one place with a wall of its own.
            backdrop(c);
            drawFlight(c);

            if (flightIn < 0.999f) {
                // One layer, on top: the black wall coming back as the room
                // goes. Two layers would be two full-screen composites for a
                // third of a second, for the same picture.
                final int save = c.saveLayerAlpha(0f, 0f, w, h,
                        (int) (255 * (1f - flightIn)));
                c.drawColor(bgColour());
                drawRoomGlow(c, w, h);
                if (litT > 0.001f) drawLightWave(c, w, h);
                buildTone();
                drawGrid(c);
                c.restoreToCount(save);
            }

            // Animate only while this page is the one on screen.
            if (getVisibility() == VISIBLE && host.getLightTable() != null
                    && host.getLightTable().getVisibility() == VISIBLE) {
                postInvalidateOnAnimation();
            }
        }

        // ---- the sheet ------------------------------------------------------

        // ---- the light ------------------------------------------------------

        /**
         * **Two rooms, one wall.**
         *
         * <p>Dark, the desk is a darkroom: black paper, every print cropped to a
         * square, and the rim of the screen going *out* of the light. Lit, it
         * is a light box: warm paper behind the prints, every frame whole at
         * its own aspect, and the rim of the screen going *into* the light
         * instead of out of it. Nothing else changes -- the dome, the defocus
         * and the lens are the same arithmetic in both, so the switch reads as
         * the light coming up rather than as a different screen.
         */
        // **The paper is warm; the light over the prints is not.** A cream
        // background reads as a bulb behind the bench, which is what it is --
        // but the haze that lies over the photographs has to stay neutral, or
        // every film on the wall turns the colour of the paper.
        private static final int LIGHT_BG = 0xFFF4EFE7;
        private static final int LIGHT_MIST = 0xFFFBFAF7;
        /** The whole move, on `cubic-bezier(.22, 1, .36, 1)`. */
        private static final float LIT_SECS = 0.65f;
        /** How much wider the gutters open when the prints come up for air. */
        private static final float LIT_GAP = 0.08f;

        /** 0 is the darkroom, 1 is the light box. `litT` is the eased position. */
        private float litT = 0f, litFrom = 0f, litTo = 0f;
        /** The two phases the drawing reads: the cards, and the mist. */
        private float litCards = 0f, litMist = 0f;

        // ---- HDR: the light box is a light source --------------------
        //
        // **The prints are transparencies on a lit panel**, so what the wall
        // shows is Beer-Lambert over each print's own density (`I = backlight *
        // Y^(p-1)`), not a brightness multiplier. The copies are 8 B a pixel,
        // so they are built at the size the cell is drawn at, a few at a time,
        // and the ones that have not host.getArrived() yet draw as SDR -- the wall fills
        // in as if the light were soaking through it.
        private final java.util.LinkedHashMap<String, Bitmap> hdrPrints =
                new java.util.LinkedHashMap<>(16, 0.75f, true);
        private final java.util.HashSet<String> hdrAsked = new java.util.HashSet<>();
        private long hdrBytes;
        /** Builds on the way. Three at a time: a wall of 84 wants all of them
         *  at once, and 84 threads of JPEG-to-half-float is a stutter, not a
         *  light box. */
        private int hdrInFlight;
        private static final long HDR_BYTES_MAX = 72L * 1024 * 1024;
        private final android.graphics.Paint hdrPic =
                new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
        private Bitmap hdrLayer;
        private Canvas hdrLayerCanvas;
        /**
         * For the panel blit, and **without filtering on purpose**: the patch is
         * a flat colour, and bilinear filtering of a half-float texture across a
         * whole screen is the slow path this device takes for F16 sources.
         */
        private final android.graphics.Paint hdrLayerPaint =
                new android.graphics.Paint();

        /** The window is HDR and the lamp is on: the wall can use it. */
        private boolean hdrWall() {
            return host.getHdrWindow() && host.hdrPreviewPref() && litCards > 0.06f && host.getAlbum().size() > 0;
        }

        /**
         * **The panel is the same source all the way through the ramp.**
         *
         * <p>`hdrWall()` cannot gate it: that switches from `drawColor` to a
         * half-float patch the moment the lamp crosses 6 %, which is a seam in
         * the one place the eye is watching -- the dark-to-lit hand-over. The
         * patch at gain 1.0 *is* the colour `drawColor` would have laid down, so
         * it is used from the first frame of the throw (where the wall is still
         * black) and simply gains brightness with `litT`.
         */
        private boolean hdrPanel() {
            return host.getHdrWindow() && host.hdrPreviewPref();
        }

        private Bitmap panelPatch;
        private int panelPatchColour;
        private float panelPatchGain;
        private final android.graphics.Rect panelRect = new android.graphics.Rect();

        /** sRGB-encoded 0..1 -> linear. The layer holds linear light. */
        private float toLinear(float v) {
            return v <= 0.04045f ? v / 12.92f
                    : (float) Math.pow((v + 0.055) / 1.055, 2.4);
        }

        /**
         * **The panel is a source, not a blend.** A half-float bitmap holding
         * exactly the colour the panel emits, drawn over the layer.
         *
         * <p>`drawColor(0x80FFFFFF, ADD)` was the obvious way and it was wrong
         * on this device: the panel came out *grey* -- 50 % white laid over
         * nothing, rather than white added to the paper. An opaque fill cannot
         * be read that way.
         */
        private Bitmap panelPatch(int argb, float gain) {
            if (panelPatch != null && !panelPatch.isRecycled()
                    && panelPatchColour == argb
                    && Math.abs(panelPatchGain - gain) < 0.02f) {
                return panelPatch;
            }
            if (panelPatch != null) panelPatch.recycle();
            panelPatch = null;
            try {
                final Bitmap b = Bitmap.createBitmap(2, 2, Bitmap.Config.RGBA_F16);
                b.setColorSpace(android.graphics.ColorSpace.get(
                        android.graphics.ColorSpace.Named.LINEAR_EXTENDED_SRGB));
                final int rc = SpektraLabEngine.fillHalf(b,
                        toLinear(((argb >> 16) & 255) / 255f) * gain,
                        toLinear(((argb >> 8) & 255) / 255f) * gain,
                        toLinear((argb & 255) / 255f) * gain);
                if (rc != 1) { b.recycle(); return null; }
                panelPatch = b;
                panelPatchColour = argb;
                panelPatchGain = gain;
                return b;
            } catch (Throwable t) {
                return null;
            }
        }

        /** Unused since 675: the panel goes straight onto the window. */
        @SuppressWarnings("unused")
        private Canvas hdrWallLayer(int w, int h) {
            if (!hdrWall()) return null;
            if (hdrLayer == null || hdrLayer.isRecycled()
                    || hdrLayer.getWidth() != w || hdrLayer.getHeight() != h) {
                if (hdrLayer != null) hdrLayer.recycle();
                hdrLayer = null;
                hdrLayerCanvas = null;
                try {
                    hdrLayer = Bitmap.createBitmap(w, h, Bitmap.Config.RGBA_F16);
                    hdrLayer.setColorSpace(android.graphics.ColorSpace.get(
                            android.graphics.ColorSpace.Named.LINEAR_EXTENDED_SRGB));
                    hdrLayerCanvas = new Canvas(hdrLayer);
                } catch (Throwable t) {
                    hdrLayer = null;
                    hdrLayerCanvas = null;
                }
            }
            return hdrLayerCanvas;
        }

        /** The copy this cell wants, or null -- in which case it draws SDR. */
        /**
         * **Off, for now.** The copies are correct but every one of them is an
         * 8-bit-to-half-float source drawn through `drawBitmapMesh`, and a wall
         * of forty is a conversion a cell a frame: the phone went from fluid to
         * a slideshow. The panel keeps the HDR; the prints get theirs back when
         * the lift can be done in one pass over the layer instead of forty.
         */
        private static final boolean HDR_PRINTS = true;

        private Bitmap hdrPrint(String key, Bitmap src, int wantPx) {
            if (!HDR_PRINTS) return null;
            if (!hdrWall() || src == null || src.isRecycled()) return null;
            final int px = hdrPx(src, wantPx);
            final String k = key + "@" + px;
            final Bitmap got = hdrPrints.get(k);
            if (got != null && !got.isRecycled()) return got;
            if (hdrInFlight >= 3) return null;
            if (hdrAsked.add(k)) buildHdrPrint(k, src, px);
            return null;
        }

        /**
         * The copy's long edge: the next half-octave step above what the cell is
         * drawn at, capped at the source.
         *
         * <p>**Half an octave, not a whole one.** At whole steps a cell drawn
         * at 240 px takes a 384 px copy -- 1.18 MB -- and forty of them sit
         * exactly on the cache's ceiling: the eldest were evicted, redrawn as
         * SDR, asked for again, and the wall never settled ("some prints are
         * brighter and some are not"). √2 steps put the same wall at 0.59 MB a
         * print.
         */
        private static int hdrPx(Bitmap src, int want) {
            // **420 is the ceiling.** Past it the copy is four times the texture
            // for a tenth of the visible gain, and every one of them is drawn
            // through a warp that this device does not accelerate for F16.
            final int cap = Math.min(420, Math.max(src.getWidth(), src.getHeight()));
            int px = 96;
            while (px < want && px < cap) px = Math.min(cap, (int) Math.ceil(px * 1.4142f));
            return Math.min(px, cap);
        }

        private void buildHdrPrint(final String k, final Bitmap src, final int px) {
            ++hdrInFlight;
            final int sw = src.getWidth(), sh = src.getHeight();
            final float k2 = Math.min(1f, px / (float) Math.max(sw, sh));
            final int tw = Math.max(8, Math.round(sw * k2)), th = Math.max(8, Math.round(sh * k2));
            final Bitmap scaled = k2 < 0.999f
                    ? Bitmap.createScaledBitmap(src, tw, th, true) : src;
            new Thread(() -> {
                Bitmap out = null;
                try {
                    out = Bitmap.createBitmap(tw, th, Bitmap.Config.RGBA_F16);
                    out.setColorSpace(android.graphics.ColorSpace.get(
                            android.graphics.ColorSpace.Named.LINEAR_EXTENDED_SRGB));
                    // **The same light the viewing desk uses**: `I = 1.55 * Y`.
                    // A print on the wall and the same print on the desk must be
                    // the same object, or the wall is showing a different lamp.
                    if (SpektraLabEngine.transilluminate(scaled, out, 1.55f, 1.0f) != 1) {
                        out.recycle();
                        out = null;
                    }
                } catch (Throwable t) {
                    if (out != null) out.recycle();
                    out = null;
                }
                if (scaled != src) scaled.recycle();
                final Bitmap made = out;
                post(() -> {
                    --hdrInFlight;
                    hdrAsked.remove(k);
                    if (made == null) return;
                    if (!hdrWall()) { made.recycle(); return; }
                    final Bitmap old = hdrPrints.put(k, made);
                    if (old != null && old != made) {
                        hdrBytes -= host.bitmapBytes(old);
                        old.recycle();
                    }
                    hdrBytes += host.bitmapBytes(made);
                    trimHdr();
                    invalidate();
                });
            }, "SpektraLab-wallhdr").start();
        }

        /** The eldest go first; the frame that just landed is the youngest. */
        private void trimHdr() {
            final java.util.Iterator<java.util.Map.Entry<String, Bitmap>> it =
                    hdrPrints.entrySet().iterator();
            while (hdrBytes > HDR_BYTES_MAX && it.hasNext()) {
                final java.util.Map.Entry<String, Bitmap> e = it.next();
                hdrBytes -= host.bitmapBytes(e.getValue());
                e.getValue().recycle();
                it.remove();
            }
        }

        public void releaseHdr() {
            for (Bitmap b : hdrPrints.values()) if (b != null) b.recycle();
            hdrPrints.clear();
            hdrAsked.clear();
            hdrBytes = 0;
            if (hdrLayer != null) { hdrLayer.recycle(); hdrLayer = null; hdrLayerCanvas = null; }
        }
        private long litStart = 0L;
        private float litOriginX = 0f, litOriginY = 0f;
        private android.graphics.Bitmap grainTile;
        private android.graphics.Shader grainShader;
        /** Told whenever the light moves, so the HUD can follow it. */
        public Runnable litChanged;

        public void setLit(boolean on) {
            final float to = on ? 1f : 0f;
            if (litTo == to) return;
            litFrom = litT;
            litTo = to;
            // **Both ways.** The anchor belongs to the transition, not to the
            // lamp: turning the light off has to unwind the same path, and with
            // nothing holding the middle the closing was a different animation
            // -- the wall re-shuffled itself around a fixed scroll offset and
            // the print you were looking at sailed away. Captured here for
            // either direction, held for either direction.
            captureAnchor();
            litStart = System.nanoTime();
            postInvalidateOnAnimation();
        }

        public boolean isLit() { return litTo > 0.5f; }

        public float litAmount() { return litT; }

        /** Where the light comes up from: the switch's own centre. */
        public void setLightOrigin(float x, float y) {
            litOriginX = x;
            litOriginY = y;
        }

        /**
         * **The same throw the viewing desk uses** (the owner, 2026-11-19: "关灯
         * 开灯的效果都很棒… 移植到照片墙").
         *
         * <p>It was a spring with a flick: fast out of the gate, through the mark
         * by a per cent and a half, then a small wobble back. Read on a single
         * sheet it felt alive; read across a wall of prints -- each of them also
         * opening its gutters -- the overshoot was the wall visibly bouncing.
         * The desk's exponential has no second thought in it: everything is lit
         * by the time the eye arrives.
         */
        private static float easeLit(float u) {
            if (u <= 0f) return 0f;
            if (u >= 1f) return 1f;
            return (float) ((1.0 - Math.exp(-4.6 * u)) / (1.0 - Math.exp(-4.6)));
        }

        private float smooth01(float v) {
            final float t = clamp01(v);
            return t * t * (3f - 2f * t);
        }

        /**
         * **The light leads, the cards follow, the type last.** One clock, three
         * offsets: a wave that reaches the corners at about 40 % of the move, a
         * set of prints that unfolds from 26 % to the end, and a HUD that
         * changes sides once the paper behind it is already bright.
         */
        private void advanceLit() {
            if (litT == litTo) return;
            final float u = Math.min(1f, (System.nanoTime() - litStart) / 1e9f / LIT_SECS);
            final float e = easeLit(u);
            litT = litFrom + (litTo - litFrom) * e;
            if (u >= 1f) litT = litTo;
            // **The gutters open about the middle of the window.** They are the
            // only thing in the wall whose size changes here, and a wall that
            // grows about its own top-left corner slides a print six cells wide
            // a hundred and sixty pixels sideways; anchored, the print you are
            // looking at stays exactly where it is while the air comes in
            // around it.
            final float opx = pitchX(), opy = pitchY();
            // **The phases are functions of the light's *position*, not of the
            // move.** `e` runs 0 -> 1 whichever way the switch was thrown, so a
            // phase built on it unfolds the prints on the way *out* as well as
            // on the way in: the room went dark and the wall stayed open, and
            // nothing but a restart would close it again. `litT` is 0 and 1 at
            // the two ends, so both directions are the same list of pictures
            // read backwards.
            if (litTo < 0.5f && toolOn) setTool(false);
            litCards = smooth01((litT - 0.26f) / 0.74f);
            litMist = smooth01(litT / 0.72f);
            // **The middle holds.** Re-measured every frame against the sheet as
            // it is this frame, so the print you were looking at does not drift
            // by even a pixel while the rest of the wall opens out around it.
            if (litT != litTo && !gDown) {
                buildRows();
                holdAnchor();
            }
            if (litChanged != null) litChanged.run();
            if (litT == litTo) {
                // **One line in boot.txt per throw of the switch.** If the wall
                // ever comes back wrong again, this says whether the state got
                // there at all before anyone has to guess.
                host.markFile("lit " + (litTo > 0.5f ? "on" : "off")
                        + " cards=" + Math.round(litCards * 100f)
                        + " mist=" + Math.round(litMist * 100f));
            }
            postInvalidateOnAnimation();
        }

        /** Keep the sheet point under the middle of the window where it was. */
        private void anchorGap(float opx, float opy, float npx, float npy) {
            if (opx <= 0f || opy <= 0f || npx <= 0f || npy <= 0f) return;
            if (opx == npx && opy == npy) return;
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            buildDome(w, h);
            domeInvAt(w * 0.5f, h * 0.5f, domeTmp);
            final float u = (domeTmp[0] - gOx) / opx, v = (domeTmp[1] - gOy) / opy;
            gOx = domeTmp[0] - u * npx;
            gOy = domeTmp[1] - v * npy;
        }

        /**
         * **What a frame looks like before the light is behind it, and after.**
         *
         * <p>Unlit, a slide on a darkroom table is not a photograph with the
         * brightness turned down: the light that would have carried the picture
         * is not there at all. It reads as a dense, slightly warm plate -- the
         * range collapses toward the middle, the highlights stop glowing, and
         * what is left of the base tints everything a little brown.
         *
         * <p>Lit, the opposite: the backlight drives the mid-tones and the
         * highlights *through* the emulsion while the dense areas keep
         * blocking, so the picture gains contrast rather than brightness. A
         * colour matrix is the whole of it, and a colour matrix interpolates --
         * which is why the prints "come up" as one continuous exposure instead
         * of as a fade between two copies.
         */
        private android.graphics.ColorMatrixColorFilter toneFilter;
        private float toneT = -1f;

        /** The exposure the wall is drawing its prints with, for the flight. */
        public float toneSr = 0.76f, toneSg = 0.73f, toneSb = 0.68f, toneOff = 3.06f;

        private void buildTone() {
            if (litT == toneT) return;
            toneT = litT;
            final float t = litT;
            // **Nothing clips.** The first version drove the light to 1.14 and
            // the red channel reached the ceiling before the blue one did, so
            // every warm highlight turned yellow -- a hue shift, not a
            // brightness. 1.06 with a small negative offset keeps the whole
            // picture under the ceiling and gets its punch from the halation
            // instead.
            final float sr = 0.76f + 0.30f * t;      // unlit base ... transilluminated
            final float sg = 0.73f + 0.33f * t;
            final float sb = 0.68f + 0.38f * t;
            final float off = (0.012f - 0.026f * t) * 255f;   // a lift, then a crush
            toneSr = sr;
            toneSg = sg;
            toneSb = sb;
            toneOff = off;
            toneFilter = new android.graphics.ColorMatrixColorFilter(
                    new android.graphics.ColorMatrix(new float[]{
                            sr, 0f, 0f, 0f, off,
                            0f, sg, 0f, 0f, off,
                            0f, 0f, sb, 0f, off,
                            0f, 0f, 0f, 1f, 0f}));
        }


        /**
         * **The light the room itself gives off.** Not much of it: the desk is
         * switched off, and what is left is the faint wash a dark bench has
         * after a lamp has been on in the next room. It is what keeps the black
         * from being a hole, and it is why the wall reads as a *surface*.
         */
        private void drawRoomGlow(android.graphics.Canvas c, int w, int h) {
            final float k = 1f - clamp01(litT * 1.6f);
            if (k <= 0.01f) return;
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setAlpha(255);
            // **一池暖光，中间偏上.** 像一盏从上面打下来的灯：池心在 (0.50, 0.34)，
            // 半径 0.80 个长边，到 55 % 还剩四成。暗角（`domeShade` + 四条渐隐）再
            // 从四边把它收进黑里 —— 墙有了深度，照片是挂在光里的。
            paint.setShader(new android.graphics.RadialGradient(w * 0.5f, h * 0.34f,
                    Math.max(w, h) * 0.80f,
                    new int[]{(int) (0xFF * k) << 24 | LT_WALL_POOL,
                            (int) (0x6B * k) << 24 | LT_WALL_POOL,
                            LT_WALL_POOL},
                    new float[]{0f, 0.55f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0f, 0f, w, h, paint);
            paint.setShader(null);
        }

        /** The paper behind the wall. */
        private int bgColour() {
            if (litT <= 0f) return LT_WALL;
            if (litT >= 1f) return LIGHT_BG;
            return UiDraw.blend(LT_WALL, LIGHT_BG, litT);
        }

        /**
         * **The light arrives as a wave, not as a fade.** A circle of paper
         * grows out of the switch and eats the dark: everything inside it is
         * already the lit room and the only thing that moves is the boundary,
         * which is what makes it read as a lamp being switched on rather than
         * as a white wash fading in.
         */
        private void drawLightWave(android.graphics.Canvas c, int w, int h) {
            final float e = easeLit(clamp01(litT));
            if (e >= 0.999f) return;
            final float reach = (float) Math.hypot(
                    Math.max(litOriginX, w - litOriginX),
                    Math.max(litOriginY, h - litOriginY)) * 1.04f;
            final int keep = (int) (255 * (1f - clamp01((litT - 0.62f) / 0.38f)));
            if (keep <= 2) return;
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setAlpha(255);
            paint.setShader(new android.graphics.RadialGradient(litOriginX, litOriginY,
                    Math.max(2f, reach * (0.04f + e)),
                    new int[]{0x00000000, 0x00000000, keep << 24 | (LT_WALL & 0xFFFFFF)},
                    new float[]{0f, 0.80f, 1f},
                    android.graphics.Shader.TileMode.CLAMP));
            c.drawRect(0f, 0f, w, h, paint);
            paint.setShader(null);
        }

        /**
         * **Grain over the light.** Not noise for its own sake: a lit sheet of
         * paper is not a flat field, and a per-pixel grey at five per cent is
         * what stops a cream background reading as a fill colour -- and it puts
         * the photographs back on film stock at the same time.
         */
        private void drawGrain(android.graphics.Canvas c, int w, int h) {
            // **Half of it over a lit panel.** The grain is a grey tile at a few
            // percent: on black it is texture, on a panel at 1.35x it is a dark
            // speckle that reads as noise along every edge.
            // **Ramped, not switched.** Halving the grain the instant
            // `hdrWall()` goes true was a visible pop in the middle of the
            // throw: the texture the whole wall is made of changed in one frame.
            final float lit = Math.max(0f, Math.min(1f, (litCards - 0.06f) / 0.60f));
            final float k = hdrPanel() ? 1f - 0.5f * lit : 1f;
            final int a = (int) (255 * (0.014f + 0.036f * litMist) * k);
            if (a <= 2) return;
            if (grainTile == null) {
                final int n = 128;
                grainTile = android.graphics.Bitmap.createBitmap(n, n,
                        android.graphics.Bitmap.Config.ARGB_8888);
                final int[] px = new int[n * n];
                final java.util.Random rnd = new java.util.Random(0x5EED5EEDL);
                for (int i = 0; i < px.length; ++i) {
                    final int v = rnd.nextInt(256);
                    px[i] = 0xFF000000 | (v << 16) | (v << 8) | v;
                }
                grainTile.setPixels(px, 0, n, 0, 0, n, n);
                grainShader = new android.graphics.BitmapShader(grainTile,
                        android.graphics.Shader.TileMode.REPEAT,
                        android.graphics.Shader.TileMode.REPEAT);
            }
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setShader(grainShader);
            paint.setAlpha(a);
            c.drawRect(0f, 0f, w, h, paint);
            paint.setShader(null);
            paint.setAlpha(255);
        }

        /** The gutter, as a share of a cell: the light opens it a little. */
        private float gapEff() {
            return gapNow + LIT_GAP * litCards;
        }

        /**
         * The sheet: a grid of prints you drag, endlessly.
         *
         * <p>From filipz's `dPGKGOo` (FashionGallery) -- square cells on a
         * narrow gutter, dragged in any direction with inertia, the grid
         * repeating so there is no edge to reach.
         *
         * <p>**Cropped, not mounted.** Every cell is filled edge to edge: the
         * picture is cut to the window the way the site cuts it, and is never
         * shrunk to fit with paper showing down two sides. That is what the
         * sheet is for -- the frame of a grid, not a mat around each print.
         */
        /**
         * Top and bottom of the wall going out of the light.
         *
         * <p>The reference does this with one `linear-gradient` from
         * `rgba(0,0,0,.9)` at the top edge to nothing at 40 %, and the same at
         * the foot. Ours is the same two ramps, painted over the prints and
         * under the captions.
         */
        // ---- the dome --------------------------------------------------------

        /**
         * **The wall is a dome, not a plane.**
         *
         * <p>A print whose centre sits at normalised radius `r` -- 1 at the
         * middle of an edge, 1.41 at a corner -- is drawn `q(r)` times its size:
         *
         * <pre>
         *   q(r) = 1 - K (1 - e^-(r/R)^2)
         * </pre>
         *
         * <p>`K` is how much smaller the rim gets, `R` how far out that starts.
         * At 0.50 and 2.50 the middle of the wall is untouched (q is 0.98 a
         * third of the way out), the middle of an edge is 0.91 and a corner
         * 0.81 -- and because q falls with r, the scale *along* the radius,
         * `q + r q'`, falls faster still: 0.75 and 0.51. A print at the rim is
         * therefore narrower than it was and squashed towards the middle of
         * the screen, which is the "wide and smeared at the edge" a fisheye
         * gives a flat wall. The grid lines bend with it: rows bow away from
         * the centre and columns bow away from it the other way, because the
         * map is radial and the wall is not.
         *
         * <p>**The square in the exponent is what keeps the print you are
         * looking at undistorted.** q' is zero at the centre and rises from
         * there, so the middle of the wall is not just the right size, it is
         * the right shape -- which an affine bulge per cell can never be.
         *
         * <p>Everything else follows from these two numbers: the defocus reads
         * the radius, the shading reads the radius, and the hit test is the
         * inverse of this map. K = 0 would be the flat sheet.
         */
        private static final float DOME_K = 0.50f;
        private static final float DOME_R = 2.50f;

        /**
         * **The ellipse the wall stays sharp inside**, and where the fall-off
         * has finished. 1.0 is the curve itself: 78 % of the way to the sides,
         * 92 % of the way to the top and bottom -- taller than it is wide, the
         * shape an eye scans a portrait screen in. Outside it the lens gives
         * up, and it has given up completely by 1.55, which is a little past
         * the corners (1.68), so every one of them is fully soft.
         */
        private static final float DOME_BLUR0 = 1.00f, DOME_BLUR1 = 1.55f;
        /** How much of the half-window the sharp ellipse reaches, across and down. */
        private static final float BLUR_X = 0.78f, BLUR_Y = 0.92f;

        /** Tables of `r q(r)` and of its inverse: no exp and no hypot per vertex. */
        private static final int DOME_LUT = 512;
        private float[] domeF = new float[0];
        private float[] domeG = new float[0];
        private float domeStep = 1f, domeInvStep = 1f;
        private float domeSpan = 3.0f;
        /** Every drawn corner is inside this: the cull uses it, and the inverse table stops there. */
        private static final float DOME_INV_SPAN = 1.85f;
        private int domeW = -1, domeH = -1;
        /** The window's half extents, in pixels. */
        private float domeHW = 1f, domeHH = 1f;
        /** The half axes of the sharp ellipse, refreshed every frame. */
        private float blurHW = 1f, blurHH = 1f;

        /** Vertices per side of one print. The crop is nine of them wide. */
        private static final int MESH = WALL_MESH;
        private final float[] mesh = new float[(MESH + 1) * (MESH + 1) * 2];
        /** The cell's own outline, for the anti-aliased clip round the warp. */
        private final android.graphics.Path sharpPath = new android.graphics.Path();
        /**
         * The soft copies are already smooth, so their mesh only has to carry
         * the *bend*: six quads is plenty and a quarter of the vertices.
         */
        private static final int MESH_SOFT = 8;
        private final float[] meshSoft = new float[(MESH_SOFT + 1) * (MESH_SOFT + 1) * 2];
        /** The crop the wall shows, in print pixels (see `wallCrop`). */
        private final int[] cropPx = new int[4];
        /** The cell the last tap landed on. Only `at()` sets it. */
        private int hitRow = -1, hitCol = -1;
        /** Scratch for the flight: one cell in sheet space, and its painted box. */
        private final android.graphics.RectF cellSheet = new android.graphics.RectF();
        /** The part of a soft copy that is on screen this frame. */
        private final android.graphics.RectF softWin = new android.graphics.RectF();
        private final float[] domeTmp = new float[2];
        private final android.graphics.RectF cellBox = new android.graphics.RectF();
        private final android.graphics.Path cellPath = new android.graphics.Path();

        private float domeQ(float r) {
            final float u = r / DOME_R;
            return 1f - DOME_K * (1f - (float) Math.exp(-u * u));
        }

        /**
         * **Two tables, once per window size.**
         *
         * <p>512 samples each: a vertex costs one hypot, one truncation and a
         * lerp. That is the difference between a mesh and a slide show on a
         * screen holding a hundred of them.
         */
        private void buildDome(int w, int h) {
            if (w <= 0 || h <= 0) return;
            if (w == domeW && h == domeH && domeF.length > 0) return;
            domeW = w;
            domeH = h;
            domeHW = w * 0.5f;
            domeHH = h * 0.5f;
            domeF = new float[DOME_LUT + 1];
            domeStep = domeSpan / DOME_LUT;
            for (int i = 0; i <= DOME_LUT; ++i) {
                final float r = i * domeStep;
                domeF[i] = r * domeQ(r);
            }
            domeG = new float[DOME_LUT + 1];
            domeInvStep = DOME_INV_SPAN / DOME_LUT;
            int j = 0;
            for (int i = 0; i <= DOME_LUT; ++i) {
                final float rs = i * domeInvStep;
                while (j < DOME_LUT && domeF[j + 1] < rs) ++j;
                final float a = domeF[j], b = domeF[j + 1];
                float t = b > a ? (rs - a) / (b - a) : 0f;
                if (t < 0f) t = 0f; else if (t > 1f) t = 1f;
                domeG[i] = (j + t) * domeStep;
            }
        }

        /** The sheet radius that lands on screen radius `rs` (both in rho). */
        private float domeInvR(float rs) {
            if (rs <= 0f) return 0f;
            final float x = rs / domeInvStep;
            final int i = (int) x;
            if (i >= DOME_LUT) return domeG[DOME_LUT] + (rs - DOME_INV_SPAN);
            final float t = x - i;
            return domeG[i] + (domeG[i + 1] - domeG[i]) * t;
        }

        /** Where a point of the sheet is painted. */
        private void domeAt(float x, float y, float[] out) {
            final float dx = x - domeHW, dy = y - domeHH;
            final float rho = (float) Math.hypot(dx / domeHW, dy / domeHH);
            if (rho < 1e-6f) {
                out[0] = x;
                out[1] = y;
                return;
            }
            final float xr = rho / domeStep;
            final int i = (int) xr;
            float mapped;
            if (i >= DOME_LUT) {
                mapped = domeF[DOME_LUT] + (rho - domeSpan);
            } else {
                final float t = xr - i;
                mapped = domeF[i] + (domeF[i + 1] - domeF[i]) * t;
            }
            final float sc = mapped / rho;
            out[0] = domeHW + dx * sc;
            out[1] = domeHH + dy * sc;
        }

        /** The point of the sheet a finger is actually over. */
        private void domeInvAt(float x, float y, float[] out) {
            final float dx = x - domeHW, dy = y - domeHH;
            final float rs = (float) Math.hypot(dx / domeHW, dy / domeHH);
            if (rs < 1e-6f) {
                out[0] = x;
                out[1] = y;
                return;
            }
            final float sc = domeInvR(rs) / rs;
            out[0] = domeHW + dx * sc;
            out[1] = domeHH + dy * sc;
        }

        /** A cell's bounding box as it is painted. */
        private void domeBounds(android.graphics.RectF r, android.graphics.RectF out) {
            domeAt(r.left, r.top, domeTmp);
            float l = domeTmp[0], rt = domeTmp[0], tp = domeTmp[1], bt = domeTmp[1];
            domeAt(r.right, r.top, domeTmp);
            if (domeTmp[0] < l) l = domeTmp[0];
            if (domeTmp[0] > rt) rt = domeTmp[0];
            if (domeTmp[1] < tp) tp = domeTmp[1];
            if (domeTmp[1] > bt) bt = domeTmp[1];
            domeAt(r.right, r.bottom, domeTmp);
            if (domeTmp[0] < l) l = domeTmp[0];
            if (domeTmp[0] > rt) rt = domeTmp[0];
            if (domeTmp[1] < tp) tp = domeTmp[1];
            if (domeTmp[1] > bt) bt = domeTmp[1];
            domeAt(r.left, r.bottom, domeTmp);
            if (domeTmp[0] < l) l = domeTmp[0];
            if (domeTmp[0] > rt) rt = domeTmp[0];
            if (domeTmp[1] < tp) tp = domeTmp[1];
            if (domeTmp[1] > bt) bt = domeTmp[1];
            out.set(l, tp, rt, bt);
        }

        /** The rim as a closed path, so the lens is cut to the print and not to a box. */
        private void cellOutline(android.graphics.RectF r, android.graphics.Path out) {
            out.rewind();
            final int seg = 6;
            for (int e = 0; e < 4; ++e) {
                for (int i = 0; i <= seg; ++i) {
                    final float u = i / (float) seg;
                    float x, y;
                    if (e == 0) { x = r.left + u * r.width(); y = r.top; }
                    else if (e == 1) { x = r.right; y = r.top + u * r.height(); }
                    else if (e == 2) { x = r.right - u * r.width(); y = r.bottom; }
                    else { x = r.left; y = r.bottom - u * r.height(); }
                    domeAt(x, y, domeTmp);
                    if (e == 0 && i == 0) out.moveTo(domeTmp[0], domeTmp[1]);
                    else out.lineTo(domeTmp[0], domeTmp[1]);
                }
            }
            out.close();
        }

        /**
         * **The crop, bent.** A print is a sheet of `MESH x MESH` quads whose
         * vertices are the crop read through the dome, so a cell's edges are
         * curves and not the straightened sides of a matrix.
         *
         * <p>Mesh vertex (i,j) always reads bitmap point (i,j) * cell size --
         * the whole bitmap, every time -- so vertices outside the crop fold
         * onto the cell's border, where they cover nothing. That is why the
         * crop is snapped to these lines first: a crop edge falling *between*
         * two of them would squeeze whatever lies outside it into the border
         * strip, and the picture would start a few per cent too far in with a
         * straight seam across the corner.
         */
        // ---- the glass ------------------------------------------------------

        /** Flat, straight, one hundred per cent: everything inside this radius. */
        private static final float LENS_FLAT = 0.85f;
        /** The most the rim is allowed to move: three and a half per cent. */
        private static final float LENS_BEND = 0.035f;
        /**
         * **Where the rim comes from.** The glass compresses what it shows as it
         * approaches its edge, so the picture has to be read from *further out*
         * than the disc: the source radius that lands on R is this much bigger.
         * The factor is then fixed by that requirement alone -- `1 - 1/1.15` --
         * which makes the map monotone, leaves the middle untouched, and puts
         * the edge of the world exactly on the edge of the lens.
         */
        private static final float LENS_EDGE = 1.0f;

        /** Radius the warp is measured against, and a multiplier for fringing. */
        private float toolWarpR = 0f, toolWarpK = 1f;

        /** Red only and blue only: the channels a real lens separates at its rim. */
        private final android.graphics.ColorFilter caRed =
                new android.graphics.ColorMatrixColorFilter(new float[]{
                        1f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f});
        private final android.graphics.ColorFilter caBlue =
                new android.graphics.ColorMatrixColorFilter(new float[]{
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f});

        /**
         * `1` in the middle, less at the rim: how much of the radius a point is
         * sampled at. The bend only starts past {@link #LENS_FLAT} and ramps in
         * as a smoothstep, so there is no visible ring where it begins.
         */
        private float lensWarp(float rr) {
            if (rr <= LENS_FLAT || rr > LENS_EDGE) return 1f;
            final float u = (rr - LENS_FLAT) / (LENS_EDGE - LENS_FLAT);
            final float b = u * u * (3f - 2f * u);
            // **Outward, and only just.** The picture grows by 3.5 % at the very
            // rim, so a straight edge that runs out to the glass turns up by a
            // few pixels and no more. Anything stronger folds the edge of a
            // print back over itself, which is not a lens, it is a fairground
            // mirror.
            return 1f + LENS_BEND * toolWarpK * b;
        }

        private void buildMesh(Bitmap b, float sl, float st, float sr, float sb,
                               float dx0, float dy0, float dw, float dh) {
            final float stepX = b.getWidth() / (float) MESH;
            final float stepY = b.getHeight() / (float) MESH;
            final float kx = dw / Math.max(1f, sr - sl);
            final float ky = dh / Math.max(1f, sb - st);
            int o = 0;
            for (int j = 0; j <= MESH; ++j) {
                float cy = dy0 + (j * stepY - st) * ky;
                if (cy < dy0) cy = dy0; else if (cy > dy0 + dh) cy = dy0 + dh;
                for (int i = 0; i <= MESH; ++i, o += 2) {
                    float cx = dx0 + (i * stepX - sl) * kx;
                    if (cx < dx0) cx = dx0; else if (cx > dx0 + dw) cx = dx0 + dw;
                    domeAt(cx, cy, domeTmp);
                    float vx = domeTmp[0], vy = domeTmp[1];
                    // **The glass is thick.** Under the loupe every vertex is
                    // pulled towards the middle by an amount that grows with the
                    // cube of the radius, so the straight edge of a frame, or a
                    // row of sprockets, bends inwards as it crosses the rim --
                    // which is what a magnifier does and what a scaled bitmap
                    // can never do. Inside LENS_FLAT it is exactly 1, so the
                    // middle stays a flat field to read grain in.
                    if (toolPass && toolWarpR > 0f) {
                        final float ux = vx - toolDrawX, uy = vy - toolDrawY;
                        final float rr = (float) Math.hypot(ux, uy) / toolWarpR;
                        // **Only inside the glass.** Past `LENS_EDGE` the map has
                        // no meaning -- and applying its tail to the rest of the
                        // sheet is what turned the lens into a kaleidoscope.
                        if (rr > LENS_FLAT && rr <= LENS_EDGE) {
                            final float q = lensWarp(rr);
                            vx = toolDrawX + ux * q;
                            vy = toolDrawY + uy * q;
                        }
                    }
                    mesh[o] = vx;
                    mesh[o + 1] = vy;
                }
            }
        }

        /**
         * **One soft copy, matte and all.**
         *
         * <p>The copy was cut from the same box as the sharp print and padded
         * with transparency, so its matte lands in the gutter and the black
         * frame is blurred *into* the picture instead of ruling a line across
         * it. The pad is six texels of that copy, which in the sheet is
         * `6 * cw / (width - 12)` -- bigger the blurrier the copy, which is
         * right: the further out of focus, the further the light carries.
         */
        /**
         * One soft copy, for one cell.
         *
         * <p>The bitmap holds the **whole frame** with a transparent matte
         * round it, so a call says which rectangle the frame's own box goes on
         * and which part of it is wanted. Dark, the wall wants the crop and the
         * matte is clipped away; lit, it wants the whole frame and the matte
         * *is* the soft edge, dissolving the print into the mist.
         */
        private void drawSoftLvl(android.graphics.Canvas c, Bitmap s,
                                 float fx, float fy, float fw, float fh,
                                 float x, float y, float cw, float ch,
                                 float lt, float alpha) {
            if (alpha <= 0.004f) return;
            if (s == null || s.isRecycled() || s.getWidth() <= 2 * SOFT_PAD) return;
            final float lw = s.getWidth(), lh = s.getHeight();
            final float mx = SOFT_PAD * fw / Math.max(1f, lw - 2f * SOFT_PAD);
            final float my = SOFT_PAD * fh / Math.max(1f, lh - 2f * SOFT_PAD);
            // the level's own rect on the sheet: the frame grown by its matte
            final float lx = fx - mx, ly = fy - my;
            final float lwd = fw + 2f * mx, lhd = fh + 2f * my;
            // which part of it is wanted: the cell when the wall crops, the
            // whole level when it shows the frame
            final float wx0 = x + (lx - x) * lt, wy0 = y + (ly - y) * lt;
            final float ww0 = cw + (lwd - cw) * lt, wh0 = ch + (lhd - ch) * lt;
            final float sx = lwd / lw, sy = lhd / lh;
            final float stepX = lw / (float) MESH_SOFT * sx, stepY = lh / (float) MESH_SOFT * sy;

            // **Clip, never squeeze.** `drawBitmapMesh` always maps the whole
            // bitmap, so the frame outside the window would be painted too; the
            // usual trick -- clamp those vertices onto the border -- crams a
            // fifth of the picture into the edge. A path through the dome cuts
            // it exactly instead, and the rim below puts back the soft edge a
            // cut does not have.
            softWin.set(wx0, wy0, wx0 + ww0, wy0 + wh0);
            cellOutline(softWin, cellPath);
            final int save = c.save();
            c.clipPath(cellPath);
            int o = 0;
            for (int j = 0; j <= MESH_SOFT; ++j) {
                for (int i = 0; i <= MESH_SOFT; ++i, o += 2) {
                    domeAt(lx + i * stepX, ly + j * stepY, domeTmp);
                    meshSoft[o] = domeTmp[0];
                    meshSoft[o + 1] = domeTmp[1];
                }
            }
            gridPic.setAlpha((int) (255 * alpha));
            c.drawBitmapMesh(s, MESH_SOFT, MESH_SOFT, meshSoft, 0, null, 0, gridPic);
            c.restoreToCount(save);

            // **The rim.** Four strokes inside the boundary, the widest and
            // faintest first: a print that is out of focus does not stop at its
            // own edge, the light from it carries on into the room. Lit, the
            // matte in the bitmap already does this and the rim is faded out.
            final float feather = (1f - lt) * alpha;
            if (feather > 0.02f) {
                final int bg = bgColour();
                gridInk.setShader(null);
                gridInk.setStyle(android.graphics.Paint.Style.STROKE);
                gridInk.setStrokeWidth(0f);
                for (int i = 0; i < 4; ++i) {
                    final float k = 1f - i * 0.24f;
                    // **The colour first, the alpha after.** `setColor` carries
                    // an alpha of its own and would overwrite the fade, which
                    // turns four steps of a feather into one solid band.
                    gridInk.setColor(bg);
                    gridInk.setAlpha((int) (255 * feather * (0.16f + i * 0.19f)));
                    gridInk.setStrokeWidth(cw * 0.045f * k);
                    c.drawPath(cellPath, gridInk);
                }
                gridInk.setStyle(android.graphics.Paint.Style.FILL);
                gridInk.setAlpha(255);
            }
        }


        /**
         * **How far out a print is, measured on the sharp ellipse.** 0 at the
         * middle, 1 on the curve the wall stays in focus inside, 1.68 at a
         * corner.
         *
         * <p>Not the same normalisation the dome uses: the distortion is
         * symmetric in the window, the *focus* is not -- a portrait screen has
         * more room above and below than it has to the sides, and the wall is
         * allowed to stay sharp further up than it is allowed to stay sharp out.
         */
        private float blurRho(float x, float y) {
            return (float) Math.hypot((x - domeHW) / Math.max(1f, blurHW),
                    (y - domeHH) / Math.max(1f, blurHH));
        }

        /**
         * **The sharp ellipse follows the wall when the wall is the smaller
         * thing.**
         *
         * <p>Zoomed out to FIT the whole sheet is on screen, and a
         * window-sized ellipse would then hold every print in it -- no blur at
         * any zoom-out at all, which is not what a lens does and not what the
         * wall looked like before. So the ellipse takes the *smaller* of the
         * window and the sheet: at 100 % and 167 % the window decides and the
         * rim of the screen goes soft, at FIT the sheet decides and the rim of
         * the sheet does, and every size in between has a rim that is genuinely
         * out of focus. The blur is measured in the print's own pixels too, so
         * a cell that is twice as big on screen is blurred by twice as many
         * pixels -- the *proportion* is the same at every zoom.
         */
        private void updateBlurEllipse() {
            final float wallW = WALL_COLS * pitchX() * 0.5f;
            final float wallH = wallRows() * pitchY() * 0.5f;
            blurHW = Math.max(32f, Math.min(domeHW, wallW) * BLUR_X);
            blurHH = Math.max(32f, Math.min(domeHH, wallH) * BLUR_Y);
        }

        /** How far past its cell a soft copy's glow reaches, in sheet pixels. */
        private float softMatte(Bitmap s, float cw) {
            if (s == null || s.isRecycled()) return 0f;
            return SOFT_PAD * cw / Math.max(1f, s.getWidth() - 2f * SOFT_PAD);
        }

        /**
         * **The dome's shadow.** A radial ramp over the whole window, clear in
         * the middle and black at the rim: the sheet is a sphere, and a sphere
         * falls away from the light at its edge.
         *
         * <p>It is painted over everything rather than multiplied into each
         * print, because the gutters are black already: over the background the
         * ramp does nothing, and over a photograph it does exactly what a
         * per-vertex shade would have done -- without a shade per vertex,
         * without a seam at every cell border, and without the mesh resolution
         * showing in the fall-off.
         */
        private android.graphics.Shader domeGrad;
        private int domeGradW = -1, domeGradH = -1;
        /** Which state of the light the shade was built for. */
        private float domeGradT = -1f;
        /** Stops are measured in corners: 1.0 is the edge of the window, 1.42 a corner. */
        private static final float DOME_REACH = 1.42f;

        private void domeShade(android.graphics.Canvas c, int w, int h) {
            if (w <= 0 || h <= 0) return;
            buildDome(w, h);
            if (w != domeGradW || h != domeGradH || domeGradT != litT) {
                domeGradW = w;
                domeGradH = h;
                domeGradT = litT;
                // **The same fall-off, as light.** Dark it darkens the rim; lit
                // it lifts it, and it keeps a veil over the middle as well --
                // five per cent of paper, which is what puts the photographs
                // *in* the light instead of on top of it.
                domeGrad = new android.graphics.RadialGradient(0f, 0f, 1f,
                        new int[]{
                                UiDraw.blend(0x00000000, 0x0AFBFBF9, litT),
                                UiDraw.blend(0x00000000, 0x11FAFAF8, litT),
                                UiDraw.blend(0x1E000000, 0x28FAF9F6, litT),
                                UiDraw.blend(0x46000000, 0x3EF8F7F3, litT),
                                UiDraw.blend(0x78000000, 0x60F6F5F0, litT)},
                        new float[]{0f, 0.40f / DOME_REACH, 0.72f / DOME_REACH,
                                1.00f / DOME_REACH, 1.34f / DOME_REACH},
                        android.graphics.Shader.TileMode.CLAMP);
            }
            final int save = c.save();
            c.translate(domeHW, domeHH);
            c.scale(domeHW * DOME_REACH, domeHH * DOME_REACH);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setAlpha(255);
            paint.setShader(domeGrad);
            c.drawRect(-1f, -1f, 1f, 1f, paint);
            paint.setShader(null);
            c.restoreToCount(save);
        }

        /** How far the top and bottom ramps reach, as a share of the height. */
        private static final float VIG_TOP = 0.26f, VIG_BOT = 0.22f, VIG_SIDE = 0.12f;
        /** The four ramps, rebuilt only when the window changes size. */
        private android.graphics.Shader vigTop, vigBot, vigL, vigR;
        private int vigW = -1, vigH = -1;
        /** Which state of the light the ramps were built for. */
        private float vigT = -1f;

        private void vignette(android.graphics.Canvas c, int w, int h) {
            domeShade(c, w, h);
            if (w != vigW || h != vigH || vigT != litT) {
                vigW = w;
                vigH = h;
                vigT = litT;
                // **Lit, the same four ramps are white.** The wall does not stop
                // going out of the light when the light comes up -- it goes out
                // of it *into* the light, which is what puts the mist over the
                // rim instead of the dark.
                final int tA = UiDraw.blend(0xB6000000, 0x30FBFAF8, litT);
                final int tB = UiDraw.blend(0x46000000, 0x12FAF9F7, litT);
                final int tC = UiDraw.blend(0x00000000, 0x00FBFAF8, litT);
                final int bA = UiDraw.blend(0xA6000000, 0x2AFBFAF8, litT);
                final int bB = UiDraw.blend(0x36000000, 0x0FFAF9F7, litT);
                final int sA = UiDraw.blend(0xC6000000, 0x38FBFAF8, litT);
                final int sB = UiDraw.blend(0x3C000000, 0x15FAF9F7, litT);
                // **A quarter of the screen, not three quarters.** The
                // reference's ramp is 0.9 black over the top 40 %, and on its
                // sunlit portraits that reads as depth. These are night
                // photographs: the same ramp turned three quarters of the wall
                // into a single dark mass and hid the grid the wall is made of.
                vigTop = new android.graphics.LinearGradient(0f, 0f, 0f, h * VIG_TOP,
                        new int[]{tA, tB, tC},
                        new float[]{0f, 0.42f, 1f},
                        android.graphics.Shader.TileMode.CLAMP);
                vigBot = new android.graphics.LinearGradient(0f, h, 0f, h * (1f - VIG_BOT),
                        new int[]{bA, bB, tC},
                        new float[]{0f, 0.45f, 1f},
                        android.graphics.Shader.TileMode.CLAMP);
                // **And the sides.** Without them a print is cut in half by the
                // edge of the screen, which is a scrollbar's behaviour, not a
                // room's: the wall should walk out of the light on all four
                // sides. It is also what lets the per-print fade go, because a
                // screen-space ramp masks an arrival perfectly smoothly.
                vigL = new android.graphics.LinearGradient(0f, 0f, w * VIG_SIDE, 0f,
                        new int[]{sA, sB, tC},
                        new float[]{0f, 0.42f, 1f},
                        android.graphics.Shader.TileMode.CLAMP);
                vigR = new android.graphics.LinearGradient(w, 0f, w * (1f - VIG_SIDE), 0f,
                        new int[]{sA, sB, tC},
                        new float[]{0f, 0.42f, 1f},
                        android.graphics.Shader.TileMode.CLAMP);
            }
            paint.setStyle(android.graphics.Paint.Style.FILL);
            // **No allocation in the draw loop.** Two gradients a frame is
            // nothing on paper and is a stutter in the hand.
            paint.setShader(vigTop);
            c.drawRect(0f, 0f, w, h * VIG_TOP, paint);
            paint.setShader(vigBot);
            c.drawRect(0f, h * (1f - VIG_BOT), w, h, paint);
            paint.setShader(vigL);
            c.drawRect(0f, 0f, w * VIG_SIDE, h, paint);
            paint.setShader(vigR);
            c.drawRect(w * (1f - VIG_SIDE), 0f, w, h, paint);
            paint.setShader(null);
        }

        /** 0.12 while a cell is off the edge, 1 once a sixth of it is on screen. */
        /** 0 to 1 over four tenths of a second after the print lands. */
        private float arrival(String key, long now) {
            final Long t0 = host.getArrived().get(key);
            if (t0 == null) return 1f;
            final float u = clamp01((now - t0) / 400_000_000f);
            if (u >= 1f) {
                host.getArrived().remove(key);
                return 1f;
            }
            return u * u * (3f - 2f * u);
        }

        /** Ask for a print that is on the wall but has fallen out of the cache. */
        private void want(String key) {
            if (host.getLightTable() == null) return;
            host.requestPrint(key);
        }

        /**
         * **The ladder, only where it shows.** At 全览 a cell is seventy pixels
         * across: six blurred copies of it are invisible and cost a dozen
         * allocations each, on a wall that wants a hundred and forty prints at
         * once -- so the wall filled in over ten seconds, one print at a time,
         * and it blinked the whole way. Small, only the sharp copy is built; the
         * soft one is asked for when the sheet is zoomed in far enough to need
         * it, and it is built from the sharp copy already in hand, so there is
         * no second decode.
         */
        private long memMarkAt = 0L;

        /** One line in the trail every couple of seconds: what the wall holds. */
        private void noteWall() {
            final long now = host.nowMs();
            if (now - memMarkAt < 2000L) return;
            memMarkAt = now;
            CrashTrail.mark(getContext(), "light:zoom "
                    + Math.round(gZoom * litGrowth() * 100f)
                    + " prints=" + host.getAlbumPrints().size() + "/" + (host.albumPrintBytes() >> 20) + "MB"
                    + " soft=" + host.getSoftPrints().size() + "/" + (host.softPrintBytes() >> 20) + "MB");
        }

        /** Ladders still to build. **One worker, not one thread per ask.** */
        private final java.util.ArrayDeque<String> softQueue = new java.util.ArrayDeque<>();
        /** How many times a print has failed to produce a ladder. */
        private final java.util.HashMap<String, Integer> softTries = new java.util.HashMap<>();
        private boolean softWorker = false;

        private void wantSoft(final String key) {
            if (host.getSoftPrints().containsKey(key)) return;
            // **Three failures is a broken print, not bad luck.** The old shape
            // removed the key from `host.getSoftAsked()` on failure and started a *new
            // thread* on the next draw -- and a draw is 60 a second. A print that
            // cannot be laddered (a decoder that returns something the matte
            // rejects, a heap that is momentarily full) therefore became a thread
            // per frame, each one allocating a base, a chain of six and a direct
            // buffer before failing. That is a spiral, and it ends with the
            // process gone and no stack.
            final Integer tried = softTries.get(key);
            if (tried != null && tried >= 3) return;
            synchronized (softQueue) {
                if (!host.getSoftAsked().add(key)) return;
                softQueue.addLast(key);
                if (softWorker) return;
                softWorker = true;
            }
            new Thread(() -> {
                android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_BACKGROUND);
                for (;;) {
                    final String k;
                    synchronized (softQueue) {
                        k = softQueue.pollFirst();
                        if (k == null) { softWorker = false; return; }
                    }
                    final Bitmap b = host.getAlbumPrints().get(k);
                    final Bitmap[] sm = (b == null || b.isRecycled()) ? null : host.defocused(b);
                    final boolean ok = sm != null;
                    CrashTrail.mark(getContext(), "light:soft " + (ok ? "ok" : "fail"));
                    host.runOnUiThread(() -> {
                        host.getSoftAsked().remove(k);
                        if (!ok) {
                            final Integer n0 = softTries.get(k);
                            softTries.put(k, (n0 == null ? 0 : n0) + 1);
                            return;
                        }
                        softTries.remove(k);
                        host.getSoftPrints().put(k, sm);
                        if (host.getLightDesk() != null) host.getLightDesk().invalidate();
                    });
                }
            }, "host.getAlbum()-soft").start();
        }

        /**
         * **Drop the ladders when the wall is too far out to use them.**
         *
         * <p>Six blurred copies only show above {@code zoomSoft()}; below it the
         * wall draws the sharp print and nothing else. They were never released,
         * so one visit to a zoomed-in corner kept up to 65 MB resident for the
         * rest of the session -- on a light table the user then leaves sitting
         * at 全览.
         */
        public void releaseSoft() {
            if (!host.getSoftPrints().isEmpty()) host.getSoftPrints().clear();
            if (!host.getSoftAsked().isEmpty()) host.getSoftAsked().clear();
            if (!softTries.isEmpty()) softTries.clear();
            synchronized (softQueue) { softQueue.clear(); }
        }

        private void drawGrid(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            final int n = host.getAlbum().size();
            if (w <= 0 || h <= 0 || n == 0) return;
            buildDome(w, h);
            // Every print on the wall goes through the same exposure: what the
            // room's light finds, and what the light box drives through it.
            buildTone();
            gridPic.setColorFilter(toneFilter);
            final long frameNow = System.nanoTime();

            // The dolly: while the sheet dissolves it keeps growing about the
            // fingers, so the pinch reads as one continuous push into the print
            // rather than two animations that happen to overlap.
            if (flightIn > 0f && flightIn < 1f) {
                final float f = flightIn;
                final float e = f * f * (3f - 2f * f);
                zoomAt(morphZoom0 + (GRID_MORPH_ZOOM - morphZoom0) * e, morphX, morphY);
            }

            // The lens finds its print again every frame: the finger may have
            // moved off the cell it was on.
            if (loupeOn) {
                loupeIndex = -1;
                loupeDst.setEmpty();
                loupeCell.setEmpty();
            }

            final float px = pitchX(), py = pitchY();
            final int rows = wallRows();
            // **The sheet's boxes for this frame**: squares in the dark room,
            // each print's own shape on the light table.
            buildRows();
            updateBlurEllipse();
            // **The rim is read further out than the window.** The dome pulls
            // everything towards the middle, so the print that fills a corner
            // sits at 1.8 screens' radius in the sheet's own coordinates: cull
            // by the window and the corners of the wall are empty.
            final float ex = w * 0.5f * DOME_INV_SPAN, ey = h * 0.5f * DOME_INV_SPAN;
            final int c0 = Math.max(0, (int) Math.floor((w * 0.5f - ex - gOx) / px));
            final int c1 = Math.min(WALL_COLS - 1,
                    (int) Math.ceil((w * 0.5f + ex - gOx) / px));
            final int r0 = Math.max(0, (int) Math.floor((h * 0.5f - ey - gOy) / py));
            final int r1 = Math.min(rows - 1, (int) Math.ceil((h * 0.5f + ey - gOy) / py));

            // **The loupe draws the wall a second time**, magnified; this is
            // what keeps that pass to the handful of cells the glass can
            // actually see instead of all of them.
            int rr0 = r0, rr1 = r1, cc0 = c0, cc1 = c1;
            if (toolCullR > 0f) {
                cc0 = Math.max(0, (int) Math.floor((toolCullX - toolCullR - gOx) / px));
                cc1 = Math.min(WALL_COLS - 1,
                        (int) Math.ceil((toolCullX + toolCullR - gOx) / px));
                if (litCards > 0.02f) { cc0 = 0; cc1 = WALL_COLS - 1; }
                rr0 = Math.max(0, (int) Math.floor((toolCullY - toolCullR - gOy) / py));
                rr1 = Math.min(rows - 1,
                        (int) Math.ceil((toolCullY + toolCullR - gOy) / py));
            }
            if (litCards > 0.02f && toolCullR <= 0f) { cc0 = 0; cc1 = WALL_COLS - 1; }
            // **挂在墙上的照片有影子.** 整面墙的影子先画一遍，再画照片 —— 一张的影子
            // 会落进邻居那一格，邻居盖住它，读起来就是一排挂得离墙一指远的相纸。
            drawWallShadows(c, cc0, cc1, rows, w, h, frameNow);
            for (int col = cc0; col <= cc1; ++col) {
                for (int r = 0; r < rows; ++r) {
                    final int k = printAt(r, col);
                    final float cw = boxW(r, col), ch = boxH(r, col);
                    final float x = boxX(r, col), y = boxY(r, col);
                    if (!toolPass && litCards > 0.02f
                            && (y + ch < -ch || y > h + ch)) {
                        continue;             // this column carries on off-screen
                    }
                    dstTile.set(x, y, x + cw, y + ch);
                    final String pkey = host.getAlbum().get(k).key();
                    final Bitmap plain = host.getAlbumPrints().get(pkey);
                    // **The print as light, when we have it.** Same aspect, so
                    // every crop and mesh below follows whatever size it is.
                    final Bitmap hdrB = plain == null ? null
                            : hdrPrint(pkey, plain, (int) Math.ceil(Math.max(cw, ch)));
                    final Bitmap b = hdrB != null ? hdrB : plain;
                    if (b == null || b.isRecycled()
                            || b.getWidth() <= 0 || b.getHeight() <= 0) {
                        if (b == null) want(pkey);
                        // Filling in: an empty frame, never a hole in the sheet.
                        // Black, as the reference's items are: a cell that has
                        // not decoded is a hole in the wall, not a beige patch.
                        cellOutline(dstTile, cellPath);
                        // **A card, not a hole.** In the dark room that is the
                        // empty frame of the reference; on the light box it has
                        // to be the colour of the box, or the wall blinks black
                        // while it fills in.
                        gridInk.setColor(UiDraw.blend(0xFF0D0C0B, 0xFFDED6C6, litCards));
                        gridInk.setStyle(android.graphics.Paint.Style.FILL);
                        c.drawPath(cellPath, gridInk);
                        continue;
                    }
                    final float iw = Math.max(1f, b.getWidth());
                    final float ih = Math.max(1f, b.getHeight());
                    // **Cover, then further in**: a square cell filled edge to
                    // edge and cropped a third deeper, snapped to the mesh the
                    // cell is drawn through. `wallCrop` is the only place that
                    // decides it, so the soft copies are cut from exactly the
                    // same rectangle and stay registered with the sharp print
                    // when the two cross-fade.
                    host.wallCrop(b.getWidth(), b.getHeight(), cropPx);
                    srcTile.set(cropPx[0], cropPx[1], cropPx[2], cropPx[3]);

                    // ---- how far out of focus this one is -------------------
                    // **Quadratic in the radius**, and measured on the sharp
                    // ellipse rather than on the window: linear had the second
                    // ring already soft at a fifth of the way out, and a print
                    // that softened while you were still looking at it reads as
                    // a glitch. Squared, everything inside the ellipse is
                    // untouched and the lens only gives up near the corners.
                    final boolean softWanted = zoomSoft();
                    final Bitmap[] soft = softWanted ? host.getSoftPrints().get(pkey) : null;
                    // **Not while the wall is moving.** Each request builds a
                    // ladder and the cache then evicts someone else's, so a drag
                    // spent its frames asking for copies that host.getArrived() too late
                    // and pushed out the ones already on screen -- every cell
                    // swapping between sharp and soft, which is the flashing.
                    // Asked for once the throw has settled, they all arrive
                    // together.
                    if (softWanted && soft == null && !panning
                            && Math.abs(gVx) < 12f && Math.abs(gVy) < 12f) {
                        wantSoft(pkey);
                    }
                    final float rho = blurRho(dstTile.centerX(), dstTile.centerY());
                    float lv = 0f;
                    if (soft != null) {
                        final float u = clamp01((rho - DOME_BLUR0)
                                / (DOME_BLUR1 - DOME_BLUR0));
                        lv = u * u * SOFT_LEVELS;
                        // **The light box is for looking at prints.** Going out of
                        // focus is a dark-room idea: carried at full strength onto
                        // white paper it stops reading as an edge falling away and
                        // becomes a pale smear -- and every print crossing that
                        // boundary flashed as it swapped between levels. Lit, the
                        // ladder only goes two thirds of the way.
                        lv *= 1f - 0.45f * litCards;
                    }
                    final int lvl = (int) lv;
                    final float t = lv - lvl;

                    // ---- bent so far out that nothing lands on the window ---
                    // **Matte included.** A soft print's glow reaches well past
                    // its own cell: a cell that stopped drawing the moment its
                    // box left the window took a hundred-pixel halo with it, and
                    // the wall flashed at the edge.
                    final float halo = soft == null ? 0f
                            : Math.max(
                                    softMatte(soft[Math.min(SOFT_LEVELS - 1,
                                            Math.max(0, lvl - 1))], cw),
                                    softMatte(soft[Math.min(SOFT_LEVELS - 1, lvl)], cw));
                    domeBounds(dstTile, cellBox);
                    if (cellBox.right < -cw - halo || cellBox.left > w + cw + halo
                            || cellBox.bottom < -ch - halo || cellBox.top > h + ch + halo) {
                        continue;
                    }

                    // **How lit this print is: a property of where it hangs, not
                    // of where the window is.** The old ramp faded a print in as
                    // it slid into view, which meant its brightness was chasing
                    // a target that moved a tenth of the way between frames
                    // whenever the wall was thrown -- and a print that changes
                    // brightness in ten-per-cent steps reads as a stutter, which
                    // is exactly what the out-of-focus rim was doing. This one
                    // never moves: the wall's own last row and column go into the
                    // dark, everything else is fully lit, and what hides a print
                    // at the *window's* edge is the window's vignette, a
                    // screen-space ramp that is smooth at any speed.
                    final float edge = Math.min(
                            Math.min(x - gOx, (gOx + wallW) - (x + cw)),
                            Math.min(y - gOy, (gOy + wallH) - (y + ch)));
                    final float fade = clamp01(0.16f + edge / (cw * 0.85f))
                            * arrival(pkey, frameNow);

                    // ---- the frame, the crop, and the card -------------------
                    // **The card is the box.** There is no fitting left to do:
                    // the box was already built from this print's own shape, so
                    // the only thing that still interpolates is the *source*
                    // window -- the wall's crop in the dark room, the whole
                    // frame on the light table.
                    final float lt = litCards;
                    final float cu0 = srcTile.left / iw, cu1 = srcTile.right / iw;
                    final float cv0 = srcTile.top / ih, cv1 = srcTile.bottom / ih;
                    final float fdW = cw / Math.max(1e-3f, cu1 - cu0);
                    final float fdH = ch / Math.max(1e-3f, cv1 - cv0);
                    final float fxD0 = x - cu0 * fdW, fyD0 = y - cv0 * fdH;
                    final float fwD = fdW + (cw - fdW) * lt;
                    final float fhD = fdH + (ch - fdH) * lt;
                    final float fxD = fxD0 + (x - fxD0) * lt;
                    final float fyD = fyD0 + (y - fyD0) * lt;
                    final float slD = srcTile.left * (1f - lt);
                    final float srD = srcTile.right + (iw - srcTile.right) * lt;
                    final float stD = srcTile.top * (1f - lt);
                    final float sbD = srcTile.bottom + (ih - srcTile.bottom) * lt;
                    final float dxD = x, dyD = y, dwD = cw, dhD = ch;

                    // ---- the light the print spills on the glass -------------
                    // **Behind the print, never over it.** A backlit frame does
                    // glow -- but the light that leaves it lands on the glass
                    // *around* it, and that is also the only version of this a
                    // Canvas can draw without gambling: a blurred copy behind
                    // the sharp one can only ever add light, because wherever
                    // the print is opaque it covers it. (A copy laid *over* it
                    // needs a per-pixel mask, which means a colour matrix whose
                    // alpha row is honoured -- exactly the bet that blew the
                    // wall out on the device.)
                    //
                    // The frame inside a level is its own size minus the matte,
                    // so the copy has to be grown by the matte's share before
                    // the frame itself is 10 % larger than the card.
                    if (litT > 0.02f && soft != null && lvl == 0 && !toolPass) {
                        final float grow = 1f + 0.16f * litT;
                        final float bx = dxD + dwD * (1f - grow) * 0.5f;
                        final float by = dyD + dhD * (1f - grow) * 0.5f;
                        drawSoftLvl(c, soft[0], fxD, fyD, fwD, fhD,
                                bx, by, dwD * grow, dhD * grow, lt, 0.30f * litT);
                    }

                    // ---- the print itself -----------------------------------
                    // **Always, and first.** The blur is drawn *over* this, never
                    // instead of it. Every soft level is an opaque copy of the
                    // same print, so nothing shows through where one is laid --
                    // but where one feathers out (the matte, the rim, a level
                    // still being decoded) what is behind is the print, not the
                    // lit paper. Without this a defocused print was a partly
                    // transparent thing lying on a white light box, and it read
                    // as over-exposed.
                    {
                        // **Clipped, so the edge is antialiased.** `drawBitmapMesh`
                        // draws its quads with hard edges: the silhouette is a
                        // polygon and at 26 segments a side the stair is still
                        // there against a lit panel. A clip to the same outline
                        // -- a curved path, which the GPU antialiases -- cuts
                        // that boundary smoothly without touching the warp
                        // inside. The soft levels have always been clipped for
                        // their matte; the sharp print never was.
                        buildMesh(b, slD, stD, srD, sbD, dxD, dyD, dwD, dhD);
                        // **No tone filter on the transilluminated copy.** It
                        // already carries the lamp's exposure; running the lit
                        // colour matrix over it as well would lift it twice.
                        final android.graphics.Paint pic = hdrB != null ? hdrPic : gridPic;
                        pic.setAlpha((int) (255 * fade));
                        final int clip = c.save();
                        cellOutline(dstTile, sharpPath);
                        c.clipPath(sharpPath);
                        c.drawBitmapMesh(b, MESH, MESH, mesh, 0, null, 0, pic);
                        drawFinish(c, cw, fade, rho);
                        c.restoreToCount(clip);
                        if (pic != gridPic) gridPic.setAlpha((int) (255 * fade));
                    }

                    // ---- and the print going out of focus -------------------
                    // Level 0 is the sharp print, 1 to 6 are the soft copies of
                    // `defocused`. Two neighbours in that list are cross-faded:
                    // the lower one opaque, the upper one at `t`. Two layers at
                    // half alpha would leave the seam between them darker than
                    // both.
                    if (soft != null && !toolPass) {
                        if (lvl >= 1) {
                            drawSoftLvl(c, soft[Math.min(SOFT_LEVELS - 1, lvl - 1)],
                                    fxD, fyD, fwD, fhD, x, y, cw, ch, lt, fade);
                        }
                        if (lvl < SOFT_LEVELS && t > 0.02f) {
                            drawSoftLvl(c, soft[Math.min(SOFT_LEVELS - 1, Math.max(0, lvl))],
                                    fxD, fyD, fwD, fhD, x, y, cw, ch, lt, fade * t);
                        }
                    }

                    // What the finger has to hit is the print as it is *drawn*.
                    // The lens wants the cell and its outline, because a cut
                    // taken at a rectangle would show the glass past the picture
                    // at every corner of a bent cell.
                    if (loupeOn) {
                        domeInvAt(loupeX, loupeY, domeTmp);
                        if (domeTmp[0] >= dxD && domeTmp[0] <= dxD + dwD
                                && domeTmp[1] >= dyD && domeTmp[1] <= dyD + dhD) {
                            loupeIndex = k;
                            loupeCell.set(dxD, dyD, dxD + dwD, dyD + dhD);
                            loupeSrc.set((int) slD, (int) stD, (int) srD, (int) sbD);
                            loupeDst.set(cellBox);
                            cellOutline(loupeCell, loupePath);
                        }
                    }
                }
            }
        }

        // ---- 照片挂在墙上：接触阴影 / 光泽 / 纸的厚度（686） --------------------
        //
        // 三样都只属于暗房：灯一开，照片变成灯箱上的透明片，墙变成光 —— 影子、
        // 反光和纸边都随灯淡掉。

        /** 影子的外沿，占格子的宽 / 高。 */
        private static final float SHADOW_M = 0.09f;
        /** 光从上面来：影子往下偏这么多（占格宽）。 */
        private static final float SHADOW_DY = 0.04f;
        private static final float SHADOW_A = 0.72f;
        private static final int SHADOW_SPRITE = 96, SHADOW_MESH = 4;
        /** 纸边那一线的宽，占格宽。 */
        private static final float BEVEL = 0.0045f;
        private Bitmap shadowSprite;
        private final float[] shadowVerts = new float[(SHADOW_MESH + 1) * (SHADOW_MESH + 1) * 2];
        private final android.graphics.Paint shadowPic = new android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG | android.graphics.Paint.FILTER_BITMAP_FLAG);
        private final android.graphics.Paint finishPaint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private android.graphics.Shader sheenShader, bevelShader;
        private int sheenW = -1, sheenH = -1;
        private final android.graphics.Matrix bevelM = new android.graphics.Matrix();

        /**
         * 一张矩形的影子，96 px 见方，画进格子外扩 9 % 的框里。剖面是两段高斯：
         * 贴着纸边那一圈很实（接触影），外面一大片很淡（离墙一指远的那一点悬空）。
         * 两轴相乘，四个角自然是圆的。
         */
        private Bitmap shadowSprite() {
            if (shadowSprite != null && !shadowSprite.isRecycled()) return shadowSprite;
            try {
                final int n = SHADOW_SPRITE;
                final float m = SHADOW_M / (1f + 2f * SHADOW_M);
                final float[] prof = new float[n];
                for (int i = 0; i < n; ++i) {
                    final float u = (i + 0.5f) / n;
                    final float d = Math.max(0f, Math.max(m - u, u - (1f - m))) / m;
                    prof[i] = d >= 1f ? 0f
                            : (float) (0.55 * Math.exp(-(d / 0.10) * (d / 0.10))
                            + 0.45 * Math.exp(-(d / 0.42) * (d / 0.42)));
                }
                final int[] px = new int[n * n];
                for (int j = 0; j < n; ++j) {
                    for (int i = 0; i < n; ++i) {
                        px[j * n + i] = Math.round(255f * prof[i] * prof[j]) << 24;
                    }
                }
                shadowSprite = Bitmap.createBitmap(px, n, n, Bitmap.Config.ARGB_8888);
            } catch (Throwable t) {
                shadowSprite = null;
            }
            return shadowSprite;
        }

        /** 纸面上一点 → 屏幕：鱼眼，加上放大镜那一遍的镜片弯曲（和 `buildMesh` 同一套）。 */
        private void warpAt(float x, float y, float[] out) {
            domeAt(x, y, out);
            if (toolPass && toolWarpR > 0f) {
                final float ux = out[0] - toolDrawX, uy = out[1] - toolDrawY;
                final float rr = (float) Math.hypot(ux, uy) / toolWarpR;
                if (rr > LENS_FLAT && rr <= LENS_EDGE) {
                    final float q = lensWarp(rr);
                    out[0] = toolDrawX + ux * q;
                    out[1] = toolDrawY + uy * q;
                }
            }
        }

        private void buildShadowMesh(float l, float t, float wd, float hg) {
            int o = 0;
            for (int j = 0; j <= SHADOW_MESH; ++j) {
                final float yy = t + hg * j / SHADOW_MESH;
                for (int i = 0; i <= SHADOW_MESH; ++i, o += 2) {
                    warpAt(l + wd * i / SHADOW_MESH, yy, domeTmp);
                    shadowVerts[o] = domeTmp[0];
                    shadowVerts[o + 1] = domeTmp[1];
                }
            }
        }

        /** 整面墙的影子：同样的格子、同样的剔除、同样的淡入，只是更早画。 */
        private void drawWallShadows(android.graphics.Canvas c, int cc0, int cc1, int rows,
                                     int w, int h, long frameNow) {
            final float k = (1f - clamp01(litT * 1.4f)) * SHADOW_A;
            if (k <= 0.01f) return;
            final Bitmap sp = shadowSprite();
            if (sp == null) return;
            for (int col = cc0; col <= cc1; ++col) {
                for (int r = 0; r < rows; ++r) {
                    final float cw = boxW(r, col), ch = boxH(r, col);
                    final float x = boxX(r, col), y = boxY(r, col);
                    if (!toolPass && litCards > 0.02f && (y + ch < -ch || y > h + ch)) continue;
                    final float mx = SHADOW_M * cw, my = SHADOW_M * ch, dy = SHADOW_DY * cw;
                    dstTile.set(x, y, x + cw, y + ch);
                    domeBounds(dstTile, cellBox);
                    if (cellBox.right < -2f * mx || cellBox.left > w + 2f * mx
                            || cellBox.bottom < -2f * my || cellBox.top > h + 2f * my + dy) {
                        continue;
                    }
                    final String pkey = host.getAlbum().get(printAt(r, col)).key();
                    final float edge = Math.min(
                            Math.min(x - gOx, (gOx + wallW) - (x + cw)),
                            Math.min(y - gOy, (gOy + wallH) - (y + ch)));
                    final float fade = clamp01(0.16f + edge / (cw * 0.85f))
                            * arrival(pkey, frameNow);
                    // 失焦的那几张离得远，影子也跟着散一点
                    final float soft = clamp01((blurRho(dstTile.centerX(), dstTile.centerY())
                            - DOME_BLUR0) / (DOME_BLUR1 - DOME_BLUR0));
                    final float a = k * fade * (1f - 0.35f * soft);
                    if (a <= 0.01f) continue;
                    buildShadowMesh(x - mx, y - my + dy, cw + 2f * mx, ch + 2f * my);
                    shadowPic.setAlpha(Math.round(255f * Math.min(1f, a)));
                    c.drawBitmapMesh(sp, SHADOW_MESH, SHADOW_MESH, shadowVerts, 0, null, 0, shadowPic);
                }
            }
        }

        /**
         * 照片的表面：一块很淡的反光（左上方的窗，屏幕空间，所以整面墙是同一扇窗
         * 照出来的），和纸的厚度（上沿亮一线、下沿暗一线）。都画在这张照片自己的
         * 裁切里，墙上不沾。
         */
        private void drawFinish(android.graphics.Canvas c, float cw, float fade, float rho) {
            final float soft = clamp01((rho - DOME_BLUR0) / (DOME_BLUR1 - DOME_BLUR0));
            final int w = getWidth(), h = getHeight();
            if (!toolPass && w > 0 && h > 0) {
                final float sa = fade * (1f - 0.7f * clamp01(litT)) * (1f - soft);
                if (sa > 0.02f) {
                    if (sheenShader == null || sheenW != w || sheenH != h) {
                        sheenW = w;
                        sheenH = h;
                        sheenShader = new android.graphics.RadialGradient(w * 0.26f, h * 0.16f,
                                Math.max(w, h) * 0.55f,
                                new int[]{0x0BFFFFFF, 0x04FFFFFF, 0x00FFFFFF},
                                new float[]{0f, 0.45f, 1f},
                                android.graphics.Shader.TileMode.CLAMP);
                    }
                    finishPaint.setStyle(android.graphics.Paint.Style.FILL);
                    finishPaint.setShader(sheenShader);
                    finishPaint.setAlpha(Math.round(255f * Math.min(1f, sa)));
                    c.drawRect(cellBox, finishPaint);
                }
            }
            // 局部矩阵每一格换一次：API 26 起 Shader 改矩阵会换一个原生实例，已经
            // 录进显示列表的那几格不受影响；更早的系统上它是原地改的 —— 不画。
            final float ba = fade * (1f - clamp01(litT * 1.4f)) * (1f - soft);
            if (ba > 0.02f && cellBox.height() > 4f
                    && android.os.Build.VERSION.SDK_INT >= 26) {
                if (bevelShader == null) {
                    bevelShader = new android.graphics.LinearGradient(0f, 0f, 0f, 1f,
                            new int[]{0x33FFFFFF, 0x00FFFFFF, 0x00000000, 0x4D000000},
                            new float[]{0f, 0.12f, 0.88f, 1f},
                            android.graphics.Shader.TileMode.CLAMP);
                }
                bevelM.setScale(1f, cellBox.height());
                bevelM.postTranslate(0f, cellBox.top);
                bevelShader.setLocalMatrix(bevelM);
                finishPaint.setStyle(android.graphics.Paint.Style.STROKE);
                finishPaint.setStrokeWidth(Math.max(2f, BEVEL * cw * 2f));
                finishPaint.setShader(bevelShader);
                finishPaint.setAlpha(Math.round(255f * Math.min(1f, ba)));
                c.drawPath(sharpPath, finishPaint);
            }
            finishPaint.setShader(null);
            finishPaint.setStyle(android.graphics.Paint.Style.FILL);
            finishPaint.setAlpha(255);
        }

        /** A cell's width, in pixels, at the current pinch. */
        private float cell() {
            final float base = Math.min(getWidth(), getHeight()) * GRID_CELL;
            return Math.max(24f, base * gZoom);
        }

        /** A cell's height. */
        private float cellH() {
            return cell() * GRID_ASPECT;
        }

        /** Centre to centre, across. */
        private float pitchX() {
            return litColW + gapEff() * cell();
        }

        /** Centre to centre, down. */
        private float pitchY() {
            return cellH() * (1f + gapEff());
        }

        /**
         * Put the sheet point under (mx, my) back under it after a scale change.
         *
         * <p>The anchor is the *sheet* point, and the map is the same before and
         * after a zoom, so the arithmetic is still one division -- but through
         * the dome's inverse, or a pinch at the rim would drag the wall sideways
         * under the fingers.
         */
        private void zoomAt(float nz, float mx, float my) {
            buildDome(getWidth(), getHeight());
            domeInvAt(mx, my, domeTmp);
            final float wx = domeTmp[0], wy = domeTmp[1];
            // **Where in the sheet that point is, as a fraction of the sheet.**
            // Fractions survive any change of zoom, of gutter and of light; the
            // old arithmetic divided by a *pitch*, and a pitch is only the pitch
            // where the sheet is uniform -- which, with the light on, it is not.
            // That is why stepping away from 全览 could land on the edge: the
            // point under the middle of the window was kept in the wrong units.
            buildRows();
            final float fx = (wx - gOx) / Math.max(1f, wallW);
            final float fy = (wy - gOy) / Math.max(1f, wallH);
            // the limits are on what is *seen*, not on the internal factor
            final float v = Math.max(GRID_ZOOM_MIN,
                    Math.min(GRID_ZOOM_MAX, nz * litGrowth()));
            // A light table left at 全览 does not need thirty ladders in memory.
            if (v <= 0.42f) releaseSoft();
            noteWall();
            gZoom = v / Math.max(0.01f, litGrowth());
            gZoom = v / Math.max(0.01f, litGrowth());
            buildRows();
            gOx = wx - fx * Math.max(1f, wallW);
            gOy = wy - fy * Math.max(1f, wallH);
        }

        /**
         * The print under the finger, or -1.
         *
         * <p>**Through the dome.** What is under a finger is whatever the dome
         * put there, so the test is the dome's inverse -- exact for every cell,
         * bent or not, which the boxes of the last version were not: at the rim
         * they were two fifths wider than the print they stood for, and a tap in
         * the gutter between two of them opened one of them.
         */
        private int at(float x, float y) {
            final int n = host.getAlbum().size();
            hitRow = hitCol = -1;
            if (n == 0) return -1;
            buildDome(getWidth(), getHeight());
            domeInvAt(x, y, domeTmp);
            final float wx = domeTmp[0], wy = domeTmp[1];
            buildRows();
            final int rows = wallRows();
            final float px = pitchX();
            if (px <= 0f) return -1;
            final int col = (int) Math.floor((wx - gOx) / px);
            if (col < 0 || col >= WALL_COLS) return -1;
            // a gutter is not a print
            if (wx < boxX(0, col) || wx > boxX(0, col) + boxW(0, col)) return -1;
            final int o = col * (rows + 1);
            for (int r = 0; r < rows; ++r) {
                final float by = gOy + colTop[o + r];
                if (wy < by || wy > by + boxH(r, col)) continue;
                // **Which cell, not just which print.** The flight has to leave
                // from this card's own patch of the sheet: a finger that lands
                // near a corner is not at the card's centre, and the sheet is
                // bent, so the theoretical grid rectangle is not what the eye
                // sees either.
                hitRow = r;
                hitCol = col;
                return printAt(r, col);
            }
            return -1;
        }

        /** The print under the finger, or the nearest one to it. */
        private int atOrCentre(float x, float y) {
            final int hit = at(x, y);
            if (hit >= 0) return hit;
            final int n = host.getAlbum().size();
            if (n == 0) return -1;
            buildDome(getWidth(), getHeight());
            domeInvAt(x, y, domeTmp);
            final int col = Math.round((domeTmp[0] - gOx - cell() * 0.5f) / pitchX());
            final int row = Math.round((domeTmp[1] - gOy - cellH() * 0.5f) / pitchY());
            return printAt(row, col);
        }

        /**
         * **Where the picked card actually is.**
         *
         * <p>`at()` already knows which cell the finger landed on; asking the
         * grid again from the fingertip would put the flight's origin at the
         * hand instead of at the photograph -- a card tapped near its corner
         * would appear to jump sideways before it left.
         */
        public boolean pickedCell(android.graphics.RectF out, int[] crop) {
            return cellFrame(hitRow, hitCol, out, crop);
        }

        /** A cell's box in sheet space (before the dome) and the crop it shows. */
        boolean cellFrame(int row, int col, android.graphics.RectF out, int[] crop) {
            if (row < 0 || col < 0 || col >= WALL_COLS || row >= wallRows()) return false;
            final int k = printAt(row, col);
            if (k < 0 || k >= host.getAlbum().size()) return false;
            final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(k).key());
            if (b == null || b.isRecycled() || b.getWidth() <= 0 || b.getHeight() <= 0) return false;
            buildDome(getWidth(), getHeight());
            buildRows();
            final float x = boxX(row, col), y = boxY(row, col);
            out.set(x, y, x + boxW(row, col), y + boxH(row, col));
            if (crop != null) host.wallCrop(b.getWidth(), b.getHeight(), crop);
            return true;
        }

        /** Where a sheet point is painted -- the same map the mesh uses. */
        public void mapPoint(float x, float y, float[] out) {
            buildDome(getWidth(), getHeight());
            domeAt(x, y, out);
        }

        /**
         * The cell holding print `index` that is nearest the eye.
         *
         * <p>Coming back must land where the photograph **is now**: the user may
         * have swiped through twenty frames, and those cells are scattered over
         * the sheet. If the nearest one is off the window the wall is moved to
         * it first -- a card that flies to a point behind the camera is a card
         * that never lands.
         */
        public boolean cellFor(int index, android.graphics.RectF out, int[] crop) {
            final int rows = wallRows();
            final float w = getWidth(), h = getHeight();
            int br = -1, bc = -1;
            float best = Float.MAX_VALUE;
            for (int c = 0; c < WALL_COLS; ++c) {
                for (int r = 0; r < rows; ++r) {
                    if (printAt(r, c) != index) continue;
                    if (!cellFrame(r, c, cellSheet, null)) continue;
                    buildDome(getWidth(), getHeight());
                    domeBounds(cellSheet, cellBox);
                    final float d = (float) Math.hypot(cellBox.centerX() - w * 0.5f,
                            cellBox.centerY() - h * 0.5f);
                    if (d < best) { best = d; br = r; bc = c; }
                }
            }
            if (br < 0) return false;
            hitRow = br;
            hitCol = bc;
            cellFrame(br, bc, cellSheet, null);
            buildDome(getWidth(), getHeight());
            domeBounds(cellSheet, cellBox);
            if (cellBox.right < dp(8) || cellBox.left > w - dp(8)
                    || cellBox.bottom < dp(8) || cellBox.top > h - dp(8)) {
                gOx = w * 0.5f - cellSheet.centerX();
                gOy = h * 0.5f - cellSheet.centerY();
                recentre = false;
                buildRows();
                clampOffsetsNow();
                buildRows();
            }
            return cellFrame(br, bc, out, crop);
        }

        /** Aim the flight at a print by index -- the chip, not a tap. */
        boolean aimAt(int index) {
            hitRow = hitCol = -1;
            return cellFor(index, cellSheet, cropPx);
        }

        /** The clamp `clampGrid` does over time, done at once (the wall is hidden). */
        private void clampOffsetsNow() {
            final float w = getWidth(), h = getHeight();
            gOx = wallW <= w ? (w - wallW) * 0.5f : Math.max(w - wallW, Math.min(0f, gOx));
            gOy = wallH <= h ? (h - wallH) * 0.5f : Math.max(h - wallH, Math.min(0f, gOy));
        }

        /**
         * **The wall is finite now, so it can be dragged out of sight.** The
         * reference holds it with `edgeResistance: .8` -- you can pull past the
         * edge and it comes back. Ours eases back the same way.
         */
        private void clampGrid(float dt) {
            // the light is holding the middle; do not tug the other way
            if (litT != litTo) return;
            buildWall(Math.max(1, host.getAlbum().size()));
            buildRows();
            final float ww = wallW, wh = wallH;
            final float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            // **Centre once, when there is a window to centre in.** `home()` is
            // called before the view has been measured, so its 0 x 0 window made
            // `(0 - wallW) / 2` a large negative offset and the clamp then dragged
            // the sheet to the far corner: every entry, and every step of the
            // zoom, "focused on the edge" because the edge is where it had been
            // parked.
            if (recentre) {
                recentre = false;
                gOx = (w - ww) * 0.5f;
                gOy = (h - wh) * 0.5f;
                postInvalidateOnAnimation();
                return;
            }
            final float tx = ww <= w ? (w - ww) * 0.5f
                    : Math.max(w - ww, Math.min(0f, gOx));
            final float ty = wh <= h ? (h - wh) * 0.5f
                    : Math.max(h - wh, Math.min(0f, gOy));
            final float k = 1f - (float) Math.exp(-dt * 12f);
            final boolean moved = Math.abs(tx - gOx) > 0.4f || Math.abs(ty - gOy) > 0.4f;
            gOx += (tx - gOx) * k;
            gOy += (ty - gOy) * k;
            if (!moved) { gOx = tx; gOy = ty; }
            else postInvalidateOnAnimation();
        }

        /** Inertia, then the caption: only while the sheet is what is shown. */
        private void gridIdle() {
            if (flightIn > 0f) return;
            clampGrid(0.016f);
            if (panning) return;
            final long now = android.os.SystemClock.uptimeMillis();
            final float dt = Math.min(0.05f, Math.max(0.001f, (now - gLastT) / 1000f));
            gLastT = now;
            if (Math.abs(gVx) > 12f || Math.abs(gVy) > 12f) {
                gOx += gVx * dt;
                gOy += gVy * dt;
                // **Damped, not slippery.** The reference throws with an
                // inertia of resistance 300: it lets go quickly and comes to
                // rest instead of skating. 0.05 per second is that feel.
                final float damp = (float) Math.pow(0.05, dt);
                gVx *= damp;
                gVy *= damp;
                postInvalidateOnAnimation();
            } else if (gVx != 0f || gVy != 0f) {
                gVx = gVy = 0f;
            } else {
                askCaption();
            }
        }

        /**
         * **The wall has no caption.** The reference's grid is a wall and
         * nothing else: no EXIF, no film card, no title over the print. The
         * caption belongs to the room, where one print is in front of you and
         * there is room to say what it is.
         */
        private void askCaption() {
            final int n = host.getAlbum().size();
            if (n == 0) return;
            final int col = Math.round((getWidth() * 0.5f - gOx - cell() * 0.5f) / pitchX());
            final int row = Math.round((getHeight() * 0.5f - gOy - cellH() * 0.5f) / pitchY());
            final int k = printAt(row, col);
            if (k == gShown) return;
            gShown = k;
            infoStation = k;
            bgIdx = k;
            host.hideInfo();
        }

        /** Let go of a pinch: land on one of the four sizes. */
        private void snapZoom() {
            if (flightIn > 0.001f) return;
            final float vz = visibleZoom();
            float best = ZOOM_STEPS[0], bd = Math.abs(vz - ZOOM_STEPS[0]);
            for (int i = 1; i < ZOOM_STEPS.length; ++i) {
                final float d = Math.abs(vz - ZOOM_STEPS[i]);
                if (d < bd) { bd = d; best = ZOOM_STEPS[i]; }
            }
            if (Math.abs(best - gZoom) < 0.002f) {
                zoomTarget = best;
                gapNow = gapFor(best);
                return;
            }
            startZoom(best, 1.15f);
        }

        /** The reference's four buttons. */
        public void zoomTo(int which) {
            startZoom(which < ZOOM_STEPS.length ? ZOOM_STEPS[which] : fitZoom(),
                    which < ZOOM_STEPS.length ? 1.15f : 1.30f);
        }

        /** FIT: the whole wall in the window, as `autoFitZoom` does. */
        private float fitZoom() {
            final float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0 || host.getAlbum().isEmpty()) return 1f;
            // **Measured, not calculated.** The sheet's columns widen with the
            // light and every print has its own height, so the honest way to
            // fit it is to lay it out at 1x, see how big it came out, and divide.
            // The old estimate assumed square cells in a uniform grid: with the
            // light on it was 18 % too optimistic, so tr("ui170.056") never actually
            // fitted, the clamp pinned the sheet to a corner, and 缩小 from
            // there stayed pinned to that corner.
            final float keepZ = gZoom, keepGap = gapNow;
            // **The gutter is a function of the zoom**, so the fit has to settle
            // on a value that agrees with its own gutter: at 20 % the gaps are
            // twice as wide as at 100 %, and a fit computed with the wrong one
            // comes out 9 % too big -- it never actually fitted, the clamp then
            // pinned the sheet to a corner, and every step taken from there
            // started at that corner.
            float v = 0.5f;
            for (int it = 0; it < 4; ++it) {
                gZoom = 1f;
                gapNow = gapFor(v);
                buildRows();
                v = Math.min(w / Math.max(1f, wallW), h / Math.max(1f, wallH));
            }
            gZoom = keepZ;
            gapNow = keepGap;
            return Math.max(GRID_ZOOM_MIN, Math.min(GRID_ZOOM_MAX, v));
        }

        /** Which of the four the wall is on, -1 for none of them. */
        public int zoomStep() {
            for (int i = 0; i < ZOOM_STEPS.length; ++i) {
                if (Math.abs(zoomTarget - ZOOM_STEPS[i]) < 0.01f) return i;
            }
            return Math.abs(zoomTarget - fitZoom()) < 0.01f ? 3 : -1;
        }

        int zoomPercent() {
            return Math.round(visibleZoom() * 100f);
        }

        /**
         * Put print `k` in the middle of the sheet.
         *
         * <p>**The wall is shuffled**, so `k` is not at a predictable cell: the
         * block has to be searched for a cell that actually holds it, or the
         * hand comes back out of the room onto somebody else's photograph.
         */
        private void centreOn(int k) {
            final int n = Math.max(1, host.getAlbum().size());
            final int idx = Math.floorMod(k, n);
            buildWall(n);
            int row = 0, col = 0;
            for (int i = 0; i < wall.length; ++i) {
                if (wall[i] % n == idx) {
                    row = i / WALL_COLS;
                    col = i - row * WALL_COLS;
                    break;
                }
            }
            gOx = getWidth() * 0.5f - col * pitchX() - cell() * 0.5f;
            gOy = getHeight() * 0.5f - row * pitchY() - cellH() * 0.5f;
        }

        /**
         * **The pinch is the dolly.** Past `GRID_FLIGHT_ZOOM` the sheet stops
         * being a sheet: the print under the fingers becomes the one that fills
         * the frame, and the room opens behind it.
         *
         * <p>The camera is put at that print's station before the fade starts,
         * so the hero grows out of the cell that was under the hand instead of
         * out of whatever the room happened to be pointing at.
         */
        private void enterFlight(int k, float mx, float my) {
            final int nA = Math.max(1, host.getAlbum().size());
            final int station = Math.floorMod(k, nA);
            targetT = (float) station / nA;
            // **One station back**, so the room dollies in instead of cutting:
            // the camera eases forward through a whole frame while the sheet
            // dissolves, and the print under the fingers swells to fill it.
            camT = targetT - 1f / nA;
            lastNanos = 0L;
            for (Plane p : planes) p.scale = 1f;
            morphZoom0 = gZoom;
            morphX = mx;
            morphY = my;
            gVx = gVy = 0f;
            gPinch = -1f;
            panning = false;
            loupeOn = false;
            removeCallbacks(armLoupe);
            host.hideInfo();
            infoStation = -1;
            flightTarget = 1f;
            enteredAt = android.os.SystemClock.uptimeMillis();
            host.markFile("desk: dive in print " + station + " of " + nA);
            if (host.getLightGridHint() != null) host.getLightGridHint().setVisibility(View.GONE);
            if (host.getLightZoomBar() != null) host.getLightZoomBar().setVisibility(View.GONE);
            postInvalidateOnAnimation();
        }

        /** Two fingers closing: back out into the sheet, on the print we were on. */
        private void exitFlight() {
            final int nA = Math.max(1, host.getAlbum().size());
            final float tt = ((camT % 1f) + 1f) % 1f;
            final int station = Math.round(tt * nA) % nA;
            gZoom = 1f;
            centreOn(station);
            gShown = station;
            morphZoom0 = 1f;
            morphX = getWidth() * 0.5f;
            morphY = getHeight() * 0.5f;
            loupeOn = false;
            removeCallbacks(armLoupe);
            fPinch = -1f;
            gPinch = -1f;
            gLockZoom = true;
            flightTarget = 0f;
            infoStation = -1;
            host.hideInfo();
            host.markFile("desk: pinch out to the wall, print " + station);
            if (host.getLightGridHint() != null) host.getLightGridHint().setVisibility(View.VISIBLE);
            if (host.getLightZoomBar() != null) host.getLightZoomBar().setVisibility(View.VISIBLE);
            postInvalidateOnAnimation();
        }

        /** The room: planes on a curve, one station per print. */
        private void drawFlight(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;

            final long now = System.nanoTime();
            float dt = lastNanos == 0L ? 0.016f : (now - lastNanos) / 1_000_000_000f;
            lastNanos = now;
            if (dt > 0.06f) dt = 0.06f;

            // The camera chases the scroll: immediate input, eased travel.
            camT += (targetT - camT) * (1f - (float) Math.exp(-dt * 6.0f));

            final float tt = ((camT % 1f) + 1f) % 1f;
            final float cx = curveX(tt), cy = curveY(tt), cz = curveZ(tt) + CAM_Z;
            final float focal = w * 0.85f;

            final int nA = Math.max(1, host.getAlbum().size());
            final int station = Math.round(tt * nA) % nA;
            final float heroT = (float) station / nA;

            // **The caption belongs to the frame that has settled.** It is typed
            // once on arrival and faded out the moment the camera moves again.
            final boolean settled = Math.abs(targetT - camT) < 0.004f;
            final int wantInfo = settled ? station : -1;
            if (wantInfo != infoStation) {
                infoStation = wantInfo;
                bgIdx = wantInfo;
                if (wantInfo >= 0) host.showInfoFor(wantInfo);
                else host.hideInfo();
            }

            // Depth-sort, far to near.
            final int n = Math.min(planes.size(), order.length);
            for (int i = 0; i < n; ++i) {
                final Plane p = planes.get(i);
                depth[i] = cz - (curveZ(p.t) + p.depth);
                order[i] = i;
            }
            for (int i = 1; i < n; ++i) {
                final int key = order[i];
                final float kd = depth[key];
                int j = i - 1;
                while (j >= 0 && depth[order[j]] < kd) {
                    order[j + 1] = order[j];
                    --j;
                }
                order[j + 1] = key;
            }

            // **The station first, so the room can be kept off it.** Everything
            // drawn before the station is behind it anyway; anything nearer that
            // would land on top of it is left out, which is what makes the
            // photograph in the middle readable: tr("ui170.060").
            float heroDz = -1f;
            hero.setEmpty();
            for (int k = 0; k < n; ++k) {
                final Plane p = planes.get(order[k]);
                if (Math.abs(p.t - heroT) > 0.2f / nA) continue;
                final float dz = project(p, cx, cy, cz, focal, w, h, probe);
                if (dz > 0) {
                    heroDz = dz;
                    hero.set(probe);
                }
                break;
            }

            for (int k = 0; k < n; ++k) {
                final Plane p = planes.get(order[k]);
                final boolean isHero = Math.abs(p.t - heroT) <= 0.2f / nA;
                if (isHero) continue;           // drawn last, unfogged
                final float dz = project(p, cx, cy, cz, focal, w, h, probe);
                if (dz <= 0) continue;

                // A nearer plane that would cover the station is **faded out**,
                // not switched off: a hard skip is what made photographs vanish
                // between two frames (tr("ui170.061")).
                final boolean covers = heroDz > 0f && dz < heroDz - 0.5f
                        && overlaps(probe, hero) > 0.12f;
                // **3.2/s, not 9.** At 9 a covering print was half gone in 80 ms,
                // which is a cut, not a fade; a third of a second reads as the
                // print stepping aside.
                p.block += ((covers ? 0f : 1f) - p.block) * (1f - (float) Math.exp(-dt * 3.2f));
                if (p.block < 0.004f) continue;

                final float dxy = (float) Math.hypot(
                        curveX(p.t) + normalX(p.t) * p.lateral - cx,
                        curveY(p.t) + normalY(p.t) * p.lateral - cy);
                final boolean lit = dz < Z_GATE && dxy < FOCUS_DIST;
                final float want = lit
                        ? 1f + (float) Math.pow(1f - dxy / FOCUS_DIST, 3f) * (MAX_SCALE - 1f)
                        : 1f;
                p.scale += (want - p.scale) * (1f - (float) Math.exp(-dt * 8.0f));

                dst.set(probe);
                if (dst.right < 0 || dst.left > w || dst.bottom < 0 || dst.top > h) continue;
                // **Two ramps, not a floor.** The fog used to keep 22 % right up
                // to the clip, so a print crossing FOG_FAR+24 appeared at 22 %
                // out of nothing -- the pop moved from the exit to the entrance.
                // `far` brings it in from 0 over the last 24 units, `near` takes
                // it out over the 3 units before the lens; the depth fog in
                // between still gives the room its distance.
                final float far = clamp01((FOG_FAR + 24f - dz) / 24f);
                final float near = clamp01((dz - 0.6f) / 3f);
                final float depth = FOG_FLOOR + (1f - FOG_FLOOR)
                        * clamp01(1f - (dz - FOG_NEAR) / (FOG_FAR - FOG_NEAR));
                final float fog = depth * far * near;
                paint.setAlpha((int) (255 * p.block * Math.max(0f, Math.min(1f, fog))));
                final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(p.index).key());
                if (b != null && !b.isRecycled()) {
                    paint.setColorFilter(toneFilter);
                    c.drawBitmap(b, null, dst, paint);
                    paint.setColorFilter(null);
                    // Drawn last wins: that is the one nearest the eye, and the
                    // one the glass should be looking at.
                    if (loupeOn && dst.contains(loupeX, loupeY)) {
                        loupeIndex = p.index;
                        loupeDst.set(dst);
                        loupeSrc.setEmpty();        // the room shows the whole print
                        loupeCell.setEmpty();       // ... and it is not on the dome
                    }
                }
            }

            // The station last: full strength, nothing on top.
            if (heroDz > 0f) {
                for (int k = 0; k < n; ++k) {
                    final Plane p = planes.get(order[k]);
                    if (Math.abs(p.t - heroT) > 0.2f / nA) continue;
                    // Same focus rule as everything else: centred at rest it
                    // reaches MAX_SCALE, halfway between stations it is on its
                    // way out of the frame like the rest of the room.
                    final float dxyHero = (float) Math.hypot(
                            curveX(p.t) - cx, curveY(p.t) - cy);
                    final float wantHero = dxyHero < FOCUS_DIST
                            ? 1f + (float) Math.pow(1f - dxyHero / FOCUS_DIST, 3f)
                            * (MAX_SCALE - 1f)
                            : 1f;
                    p.scale += (wantHero - p.scale) * (1f - (float) Math.exp(-dt * 8.0f));
                    final float dz = project(p, cx, cy, cz, focal, w, h, dst);
                    if (dz <= 0) continue;
                    if (dst.right < 0 || dst.left > w || dst.bottom < 0 || dst.top > h) break;
                    final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(p.index).key());
                    if (b == null || b.isRecycled()) break;
                    paint.setAlpha(255);
                    paint.setColorFilter(toneFilter);
                    c.drawBitmap(b, null, dst, paint);
                    paint.setColorFilter(null);
                    break;
                }
            }
            paint.setAlpha(255);

            drawLoupe(c, w, h);
        }


        /**
         * **The lens is AtelierUI's `LensMedia`, ported line for line.**
         *
         * <p>Their fragment shader is the whole effect, and there is no ring,
         * no specular and no rim in it:
         *
         * <pre>
         *   toCentre  = uv - mouse
         *   radius    = length(toCentre)                    // pixels, not uv
         *   mask      = (1 - smoothstep(size, size + softness, radius)) * hover
         *   refracted = uv - toCentre * refraction * mask
         *   shift     = toCentre * aberration * mask
         *   col       = Σ_i map(refracted + shift * (1 - 2 t_i)) * w_i(t_i)
         *   w_i(t)    = clamp(1 - 2 |t - c_i|)              // c = 0, .5, 1 per channel
         * </pre>
         *
         * <p>Three things make it read as glass rather than as a magnifier: the
         * mask fades out over four times its core radius, the sample always
         * lands back on the same picture (so at the rim the lens *is* the print
         * and there is no edge to see), and red is read a little further out
         * than blue -- which is the dispersion a thick lens throws at its edge.
         *
         * <p>`uMouse` and `uHover` are both damped at seven per second in their
         * `useFrame`, so the lens trails the finger instead of sticking to it.
         * That lag is most of why theirs feels like a solid object.
         *
         * <p>The pass runs at half resolution over the disc, which is 24 k
         * pixels instead of a hundred thousand.
         */
        /**
         * **Three times the reference's disc**, and the falloff no longer
         * outruns the tile: `size + softness` lands exactly on the cell's
         * corner, so the mask reaches zero at the only edge a tile has and
         * the lens covers the print you are touching instead of a spot in
         * its middle.
         */
        private static final float LOUPE_SIZE = 0.36f;       // uSize
        private static final float LOUPE_SOFT = 0.36f;       // uSoftness
        private static final float LOUPE_ABERR = 0.18f;      // uAberration
        private static final float LOUPE_REFRACT = 0.40f;    // uRefraction
        private static final float LOUPE_DAMP = 7f;          // uSmoothing

        /** `t_i = (i + .5) / N` and the reference's three weight triangles. */
        private static final float[] LOUPE_T = {
                0.055555556f, 0.16666667f, 0.27777778f, 0.38888889f, 0.5f,
                0.61111111f, 0.72222222f, 0.83333333f, 0.94444444f };
        private static final float[] LOUPE_WR = {
                0.4f, 0.3f, 0.2f, 0.1f, 0f, 0f, 0f, 0f, 0f };
        private static final float[] LOUPE_WG = {
                0.024390244f, 0.073170732f, 0.12195122f, 0.17073171f,
                0.2195122f, 0.17073171f, 0.12195122f, 0.073170732f, 0.024390244f };
        private static final float[] LOUPE_WB = {
                0f, 0f, 0f, 0f, 0f, 0.1f, 0.2f, 0.3f, 0.4f };

        /** `uMouse`, damped, in screen pixels. */
        private float lensMoveX, lensMoveY;
        private boolean lensArmed;
        private long lensNanos;
        /** cell px -> bitmap px, rebuilt once per frame. */
        private float lensOx, lensOy, lensBx, lensBy;
        /** The dome's own scale at the glass: print px per screen px. */
        private float lensKx = 1f, lensKy = 1f, lensUx = 1f, lensUy = 1f;

        private static float smoothStep(float a, float b, float x) {
            float t = (x - a) / (b - a);
            t = t < 0f ? 0f : (t > 1f ? 1f : t);
            return t * t * (3f - 2f * t);
        }

        /** One pixel of the print, clamped at its edge. */
        private int lensAt(float bx, float by) {
            int ix = (int) bx, iy = (int) by;
            if (ix < 0) ix = 0; else if (ix >= lensSrcW) ix = lensSrcW - 1;
            if (iy < 0) iy = 0; else if (iy >= lensSrcH) iy = lensSrcH - 1;
            return lensSrc[iy * lensSrcW + ix];
        }

        private void drawLoupe(android.graphics.Canvas c, int w, int h) {
            if (toolOn) return;      // one lens at a time
            final long now = System.nanoTime();
            float dt = lensNanos == 0L ? 0.016f : (now - lensNanos) / 1e9f;
            lensNanos = now;
            if (dt > 0.05f) dt = 0.05f;
            if (dt <= 0.0005f) dt = 0.0005f;

            final float target = loupeOn ? 1f : 0f;
            final float k = 1f - (float) Math.exp(-LOUPE_DAMP * dt);
            loupeA += (target - loupeA) * k;
            if (loupeA < 0.004f) {
                loupeA = 0f;
                loupeIndex = -1;
                lensArmed = false;
                return;
            }
            if (loupeIndex < 0 || loupeIndex >= host.getAlbum().size()) return;
            if (loupeDst.isEmpty() || loupeDst.height() < 8f) return;
            final Bitmap b = host.getAlbumPrints().get(host.getAlbum().get(loupeIndex).key());
            if (b == null || b.isRecycled() || !lensSource(b)) return;

            if (!lensArmed) {
                lensMoveX = loupeX;
                lensMoveY = loupeY;
                lensArmed = true;
            } else {
                lensMoveX += (loupeX - lensMoveX) * k;
                lensMoveY += (loupeY - lensMoveY) * k;
            }

            final float ch = Math.max(1f, loupeDst.height());
            final float sizePx = LOUPE_SIZE * ch;
            final float softPx = LOUPE_SOFT * ch;
            // Out to the radius where the mask is *exactly* zero: there the
            // sample is the original pixel, so cutting the buffer there is
            // invisible and no alpha ramp is needed.
            final float R = sizePx + softPx;
            final int S = Math.max(24, Math.round(R / 3f));
            final int n = S * 2;
            if (lensBmp == null || lensBmp.getWidth() != n) {
                lensBmp = Bitmap.createBitmap(n, n, Bitmap.Config.ARGB_8888);
                lensPx = new int[n * n];
            }

            // **Two ways to read the glass.** In the room the print is a plain
            // rectangle on the screen and the sample is a linear map of it; on
            // the wall the cell is bent, so a screen point has to go back
            // through the dome before it can be found in the bitmap.
            final boolean flat = loupeCell.isEmpty();
            if (flat) {
                final float sl = loupeSrc.isEmpty() ? 0f : loupeSrc.left;
                final float st = loupeSrc.isEmpty() ? 0f : loupeSrc.top;
                final float sw = loupeSrc.isEmpty() ? (float) lensSrcW : loupeSrc.width();
                final float sh = loupeSrc.isEmpty() ? (float) lensSrcH : loupeSrc.height();
                lensBx = sw / Math.max(1f, loupeDst.width());
                lensBy = sh / Math.max(1f, loupeDst.height());
                lensOx = sl - loupeDst.left * lensBx;
                lensOy = st - loupeDst.top * lensBy;
                lensKx = lensBx;
                lensKy = lensBy;
            } else {
                buildDome(w, h);
                lensUx = loupeSrc.width() / Math.max(1f, loupeCell.width());
                lensUy = loupeSrc.height() / Math.max(1f, loupeCell.height());
                // How many print pixels a screen pixel is worth here, measured
                // rather than assumed: the dispersion is a screen distance and
                // has to be spent in the print's own units.
                domeInvAt(lensMoveX, lensMoveY, domeTmp);
                final float ax = domeTmp[0], ay = domeTmp[1];
                domeInvAt(lensMoveX + 8f, lensMoveY, domeTmp);
                lensKx = (domeTmp[0] - ax) * lensUx / 8f;
                domeInvAt(lensMoveX, lensMoveY + 8f, domeTmp);
                lensKy = (domeTmp[1] - ay) * lensUy / 8f;
            }

            final float step = (2f * R) / n;
            final float x0 = lensMoveX - R + step * 0.5f;
            final float y0 = lensMoveY - R + step * 0.5f;
            final float sizeHi = sizePx + softPx;

            int o = 0;
            for (int jj = 0; jj < n; ++jj) {
                final float py = y0 + jj * step;
                final float dy = py - lensMoveY;
                for (int ii = 0; ii < n; ++ii, ++o) {
                    final float px = x0 + ii * step;
                    final float dx = px - lensMoveX;
                    final float rad = (float) Math.sqrt(dx * dx + dy * dy);
                    if (rad >= R) { lensPx[o] = 0; continue; }
                    final float a = 1f;

                    final float mask = 1f - smoothStep(sizePx, sizeHi, rad);
                    if (mask <= 0.001f) { lensPx[o] = 0; continue; }

                    final float rx = px - dx * LOUPE_REFRACT * mask;
                    final float ry = py - dy * LOUPE_REFRACT * mask;
                    final float shx = dx * LOUPE_ABERR * mask;
                    final float shy = dy * LOUPE_ABERR * mask;

                    // One inverse per pixel; the nine taps are a fraction of a
                    // print apart, so they ride the local scale instead of
                    // walking the dome nine times.
                    final float bu, bv;
                    if (flat) {
                        bu = lensBx * rx + lensOx;
                        bv = lensBy * ry + lensOy;
                    } else {
                        domeInvAt(rx, ry, domeTmp);
                        bu = (domeTmp[0] - loupeCell.left) * lensUx + loupeSrc.left;
                        bv = (domeTmp[1] - loupeCell.top) * lensUy + loupeSrc.top;
                    }
                    float rr = 0f, gg = 0f, bb = 0f;
                    for (int t = 0; t < 9; ++t) {
                        final float f = 1f - 2f * LOUPE_T[t];
                        final int q = lensAt(bu + shx * f * lensKx, bv + shy * f * lensKy);
                        rr += ((q >> 16) & 255) * LOUPE_WR[t];
                        gg += ((q >> 8) & 255) * LOUPE_WG[t];
                        bb += (q & 255) * LOUPE_WB[t];
                    }
                    lensPx[o] = (((int) (255 * a)) << 24)
                            | (Math.min(255, (int) rr) << 16)
                            | (Math.min(255, (int) gg) << 8)
                            | Math.min(255, (int) bb);
                }
            }

            lensBmp.setPixels(lensPx, 0, n, 0, 0, n, n);
            final int save = c.save();
            // **Cut to the print, not to its box.** On the wall a cell is a
            // bent quad, and a rectangular cut would show the glass hanging out
            // past the picture at every corner of it.
            if (flat) c.clipRect(loupeDst);
            else c.clipPath(loupePath);
            paint.setShader(null);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setColorFilter(toneFilter);
            paint.setAlpha(Math.max(0, Math.min(255, (int) (255 * loupeA))));
            dst2.set(lensMoveX - R, lensMoveY - R, lensMoveX + R, lensMoveY + R);
            c.drawBitmap(lensBmp, null, dst2, paint);
            paint.setColorFilter(null);
            c.restoreToCount(save);
            paint.setAlpha(255);
        }

        /** Is the sheet close enough that the defocus is worth building? */
        public boolean zoomSoft() { return visibleZoom() > 0.42f; }

        /** The print's pixels, cached for the whole gesture. */
        private boolean lensSource(Bitmap b) {
            if (b == lensSrcKey) return true;
            final int bw = b.getWidth(), bh = b.getHeight();
            final int[] px = new int[bw * bh];
            try {
                b.getPixels(px, 0, bw, 0, 0, bw, bh);
            } catch (Throwable t) {
                return false;
            }
            lensSrc = px;
            lensSrcW = bw;
            lensSrcH = bh;
            lensSrcKey = b;
            return true;
        }

        private int sample(float x, float y) {
            final int ix = Math.max(0, Math.min(lensSrcW - 1, (int) x));
            final int iy = Math.max(0, Math.min(lensSrcH - 1, (int) y));
            return lensSrc[iy * lensSrcW + ix];
        }

        /** Share of `a` that lands inside `b`, 0..1. */        /** Share of `a` that lands inside `b`, 0..1. */
        private float overlaps(android.graphics.RectF a, android.graphics.RectF b) {
            if (b.isEmpty() || !android.graphics.RectF.intersects(a, b)) return 0f;
            final float w = Math.min(a.right, b.right) - Math.max(a.left, b.left);
            final float h = Math.min(a.bottom, b.bottom) - Math.max(a.top, b.top);
            if (w <= 0f || h <= 0f) return 0f;
            final float areaA = Math.max(1f, a.width() * a.height());
            return (w * h) / areaA;
        }

        private float normalX(float t) {
            final float nx = -dy(t), ny = dx(t);
            final float len = (float) Math.hypot(nx, ny);
            return len > 1e-4f ? nx / len : 0f;
        }

        private float normalY(float t) {
            final float nx = -dy(t), ny = dx(t);
            final float len = (float) Math.hypot(nx, ny);
            return len > 1e-4f ? ny / len : 1f;
        }

        // ---- input ----------------------------------------------------------

        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            // Which half of the one gesture this event belongs to is decided by
            // where the cross-fade is going, not by where it is: a finger that
            // started the pinch must finish it as a pinch.
            return flightTarget < 0.5f ? gridTouch(e) : flightTouch(e);
        }

        private float span(android.view.MotionEvent e) {
            if (e.getPointerCount() < 2) return -1f;
            final float dx = e.getX(0) - e.getX(1), dy = e.getY(0) - e.getY(1);
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        private float midX(android.view.MotionEvent e) {
            return e.getPointerCount() < 2 ? e.getX() : (e.getX(0) + e.getX(1)) * 0.5f;
        }

        private float midY(android.view.MotionEvent e) {
            return e.getPointerCount() < 2 ? e.getY() : (e.getY(0) + e.getY(1)) * 0.5f;
        }

        /** The sheet: drag to travel, pinch to dive in, tap to open a print. */
        private boolean gridTouch(android.view.MotionEvent e) {
            // **The loupe is a thing on the table**, so it gets the touch first:
            // grabbing it must never drag the wall out from under it.
            if (toolOn && toolTouch(e)) return true;
            switch (e.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    // a hand on the sheet takes the sheet: the animation keeps
                    // opening, but it stops holding the middle
                    anchorRow = -1;
                    panning = true;
                    gDown = true;
                    gLockZoom = false;
                    gVx = gVy = 0f;
                    gLastX = gDownX = downX = e.getX();
                    gLastY = gDownY = downY = e.getY();
                    gDownAt = e.getEventTime();
                    gLastT = android.os.SystemClock.uptimeMillis();
                    gPinch = -1f;
                    moved = false;
                    // A finger on the sheet means the caption is not wanted yet.
                    host.hideInfo();
                    // **The lens is on from the first touch.** The reference
                    // binds it to `pointerenter` / `pointermove`, so on a phone
                    // it is under the finger the moment the finger lands and it
                    // travels with it -- the print slides *under* the glass
                    // instead of the glass waiting for you to hold still.
                    removeCallbacks(armLoupe);
                    loupeOn = true;
                    loupeX = e.getX();
                    loupeY = e.getY();
                    lensArmed = false;
                    removeCallbacks(armPrintMenu);
                    postDelayed(armPrintMenu, 620L);
                    return true;

                case android.view.MotionEvent.ACTION_POINTER_DOWN:
                    removeCallbacks(armLoupe);
                    removeCallbacks(armPrintMenu);
                    loupeOn = false;
                    gPinch = span(e);
                    gPinch0 = gZoom;
                    gPinchX = midX(e);
                    gPinchY = midY(e);
                    moved = true;
                    return true;

                case android.view.MotionEvent.ACTION_MOVE: {
                    if (e.getPointerCount() >= 2) {
                        // The fingers that just came back out of the room are
                        // still closed; letting them go on shrinking the wall
                        // means the hand always leaves it smaller than it found
                        // it.
                        if (gLockZoom) return true;
                        final float sp = span(e);
                        if (gPinch > 0f && sp > 0f) {
                            zoomAt(gPinch0 * (sp / gPinch), gPinchX, gPinchY);
                            zoomTarget = gZoom;          // the hand owns it until it lets go
                            zoomSecs = 0f;
                            if (gZoom >= GRID_FLIGHT_ZOOM) {
                                final int k = atOrCentre(gPinchX, gPinchY);
                                if (k >= 0) enterFlight(k, gPinchX, gPinchY);
                            }
                        } else if (sp > 0f) {
                            gPinch = sp;
                            gPinch0 = gZoom;
                            gPinchX = midX(e);
                            gPinchY = midY(e);
                        }
                        // **Repaint.** Without this the wall only redraws when
                        // the finger is lifted: the pinch changes `gZoom` and
                        // nothing else asks the view to draw, so the picture
                        // sits still for a second and then jumps.
                        invalidate();
                        return true;
                    }
                    if (!gDown) {
                        // The pinch was handed back to us halfway through: the
                        // drag starts here, not where the last one ended.
                        gDown = true;
                        gLastX = e.getX();
                        gLastY = e.getY();
                        return true;
                    }
                    if (Math.hypot(e.getX() - gDownX, e.getY() - gDownY) > dp(7)) {
                        moved = true;
                        removeCallbacks(armPrintMenu);
                    }
                    if (loupeOn) {
                        loupeX = e.getX();
                        loupeY = e.getY();
                    }
                    final float dx = e.getX() - gLastX, dy = e.getY() - gLastY;
                    // **The dome is part of the drag.** A finger at the rim has
                    // already been shrunk by the map, so a screen pixel is worth
                    // more than a sheet pixel there: adding them straight would
                    // slide the print out from under the hand faster than the
                    // hand moves.
                    buildDome(getWidth(), getHeight());
                    domeInvAt(gLastX, gLastY, domeTmp);
                    final float wxo = domeTmp[0], wyo = domeTmp[1];
                    domeInvAt(gLastX + dx, gLastY + dy, domeTmp);
                    gOx += domeTmp[0] - wxo;
                    gOy += domeTmp[1] - wyo;
                    // The throw is the last frame's speed, capped at a screen a second.
                    final float limit = Math.max(getWidth(), getHeight()) * 1.2f;
                    gVx = Math.max(-limit, Math.min(limit, dx * 30f));
                    gVy = Math.max(-limit, Math.min(limit, dy * 30f));
                    gLastT = android.os.SystemClock.uptimeMillis();
                    gLastX = e.getX();
                    gLastY = e.getY();
                    invalidate();
                    return true;
                }

                case android.view.MotionEvent.ACTION_POINTER_UP: {
                    gPinch = -1f;
                    final int keep = e.getActionIndex() == 0 ? 1 : 0;
                    // The remaining finger carries on from where it is, without
                    // inheriting the pinch's throw.
                    gLastX = e.getX(keep);
                    gLastY = e.getY(keep);
                    gVx = gVy = 0f;
                    snapZoom();
                    return true;
                }

                case android.view.MotionEvent.ACTION_UP: {
                    removeCallbacks(armLoupe);
                    removeCallbacks(armPrintMenu);
                    loupeOn = false;
                    panning = false;
                    gPinch = -1f;
                    gDown = false;
                    snapZoom();
                    final long held = e.getEventTime() - gDownAt;
                    final float dx2 = e.getX() - gDownX, dy2 = e.getY() - gDownY;
                    // **8dp, not a thumb's width.** A vertical throw of the wall
                    // is a swipe, not a tap: the finger slides a few millimetres
                    // sideways while it travels, and at 9dp that was enough to
                    // open a photograph on the way past.
                    if (!moved && held < 350
                            && dx2 * dx2 + dy2 * dy2 < dp(8) * dp(8)) {
                        final int k = at(e.getX(), e.getY());
                        if (k >= 0) {
                            gVx = gVy = 0f;
                            // 单张观片台已移除：点一张 = 把它设为当前帧并进冲印。
                            host.setLightIndex(k);
                            host.crossToEditor(host.getLightDesk());
                            return true;
                        }
                    }
                    postInvalidateOnAnimation();
                    return true;
                }

                case android.view.MotionEvent.ACTION_CANCEL:
                    removeCallbacks(armLoupe);
                    removeCallbacks(armPrintMenu);
                    loupeOn = false;
                    panning = false;
                    gDown = false;
                    gPinch = -1f;
                    gVx = gVy = 0f;
                    invalidate();
                    return true;
            }
            return super.onTouchEvent(e);
        }

        /** The room: drag up and down to travel, pinch in to go back to the sheet. */
        private boolean flightTouch(android.view.MotionEvent e) {
            final int h = Math.max(1, getHeight());
            switch (e.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    downX = e.getX();
                    downY = lastY = e.getY();
                    downAt = host.nowMs();
                    moved = false;
                    fDown = true;
                    fPinch = -1f;
                    // **Hold to look closer.** A pan cannot arm the lens; 360 ms
                    // of a still finger can, and lifting the finger ends it.
                    postDelayed(armLoupe, 140);
                    return true;
                case android.view.MotionEvent.ACTION_POINTER_DOWN:
                    removeCallbacks(armLoupe);
                    loupeOn = false;
                    moved = true;
                    // **Two fingers in the room is the way back**, decided the
                    // moment the second one lands. A threshold on how far the
                    // fingers close can always be missed -- a gesture that is
                    // read as a pinch and one that is not look the same until
                    // it has moved -- so the room leaves on the touch itself.
                    // The guard is for the fingers that *brought* us in: those
                    // were already down when the room opened.
                    fPinch = -1f;
                    if (android.os.SystemClock.uptimeMillis() - enteredAt > 400L) {
                        exitFlight();
                    }
                    return true;
                case android.view.MotionEvent.ACTION_MOVE: {
                    if (e.getPointerCount() >= 2) {
                        // **Nothing.** The gesture that came in still has two
                        // fingers down; reading that as "out" would bounce the
                        // room straight back into the wall.
                        return true;
                    }
                    final float dyPix = e.getY() - lastY;
                    lastY = e.getY();
                    if (Math.hypot(e.getX() - downX, e.getY() - downY) > dp(7)) {
                        if (!loupeOn) removeCallbacks(armLoupe);
                        moved = true;
                    }
                    if (loupeOn) {
                        loupeX = e.getX();
                        loupeY = e.getY();
                        postInvalidateOnAnimation();
                        return true;
                    }
                    if (moved) {
                        targetT -= dyPix * (0.45f / h);
                        invalidate();
                    }
                    return true;
                }
                case android.view.MotionEvent.ACTION_POINTER_UP: {
                    fPinch = -1f;
                    final int keep = e.getActionIndex() == 0 ? 1 : 0;
                    lastY = e.getY(keep);
                    return true;
                }
                case android.view.MotionEvent.ACTION_UP: {
                    removeCallbacks(armLoupe);
                    final boolean wasLoupe = loupeOn;
                    loupeOn = false;
                    fPinch = -1f;
                    final long ms = host.nowMs() - downAt;
                    final boolean tap = fDown;
                    fDown = false;
                    // **Every settle lands on a station**, so the hand leaving the
                    // glass always leaves a photograph centred.
                    final int nA = Math.max(1, host.getAlbum().size());
                    targetT = Math.round(targetT * nA) / (float) nA;
                    if (tap && !moved && !wasLoupe && ms < 350) {
                        final int hit = pick(e.getX(), e.getY());
                        if (hit >= 0 && hit < host.getAlbum().size()) {
                            host.setLightIndex(hit);
                            host.openFromTable(hit);
                        }
                    }
                    invalidate();
                    return true;
                }
                case android.view.MotionEvent.ACTION_CANCEL:
                    removeCallbacks(armLoupe);
                    loupeOn = false;
                    fDown = false;
                    fPinch = -1f;
                    return true;
                default:
                    return true;
            }
        }

        /** The photograph under the finger, if it is close enough to read. */
        private int pick(float x, float y) {
            final int w = getWidth(), h = getHeight();
            final float tt = ((camT % 1f) + 1f) % 1f;
            final float cx = curveX(tt), cy = curveY(tt), cz = curveZ(tt) + CAM_Z;
            final float focal = w * 0.85f;
            int best = -1;
            float bestDz = Float.MAX_VALUE;
            for (Plane p : planes) {
                final float dz = project(p, cx, cy, cz, focal, w, h, probe);
                if (dz <= 0 || dz > 30f) continue;
                if (probe.contains(x, y) && dz < bestDz) {
                    bestDz = dz;
                    best = p.index;
                }
            }
            return best;
        }
    }