package app.spektralab.ui;

import android.graphics.*;

/** The pale backdrop behind the negative desk (extracted). */
    public final class LightBox extends android.view.View {
    private static final int LT_WALL = 0xFF151310;
        private final android.graphics.Paint sheet =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        public LightBox(android.content.Context c) {
            super(c);
        }

        /** The same paper the desk paints, so the empty state is one room. */
        private int colour = LT_WALL;

        public void setLit(float t) {
            final int want = UiDraw.blend(LT_WALL, 0xFFF4EFE7, t);
            if (want != colour) {
                colour = want;
                invalidate();
            }
        }

        @Override protected void onDraw(android.graphics.Canvas c) {
            final float w = getWidth(), h = getHeight();
            // **Black, or paper.** The desk is a room with the prints in it;
            // this view is only here so the empty state has something behind it,
            // and it has to be the same room the light switch makes.
            sheet.setShader(null);
            sheet.setColor(colour);
            c.drawRect(0, 0, w, h, sheet);
        }
    }
