package app.spektralab.ui;

import app.spektralab.ui.UiPalette;

public final class EdgeBackdrop extends android.graphics.drawable.Drawable {
        private final int top;
        private final android.graphics.Paint paint = new android.graphics.Paint();
        /** Light-table mode paints the whole strip as the darkroom wall. */
        private boolean room;
        private int roomColour;
        public EdgeBackdrop(int top) { this.top = top; }
        public void setRoom(boolean on, int colour) {
            if (room == on && (!on || roomColour == colour)) return;
            room = on;
            roomColour = colour;
            invalidateSelf();
        }
        @Override public void draw(android.graphics.Canvas canvas) {
            final android.graphics.Rect b = getBounds();
            if (room) {
                paint.setColor(roomColour);
                canvas.drawRect(b, paint);
                return;
            }
            paint.setColor(UiPalette.PAPER);
            canvas.drawRect(b, paint);
            if (top > 0) {
                paint.setColor(UiPalette.STAGE);
                canvas.drawRect(b.left, b.top, b.right, b.top + top, paint);
            }
        }
        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { }
        @Override public int getOpacity() { return android.graphics.PixelFormat.OPAQUE; }
    }
