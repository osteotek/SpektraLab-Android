package app.spektralab.ui;

import android.graphics.*;

/** The small confirmation tick (extracted). */
    public final class CheckMark extends android.view.View {
    private final int blue;
    public CheckMark(android.content.Context c, int blue) { super(c); this.blue = blue; }
        private final Paint pen = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        @Override protected void onDraw(android.graphics.Canvas c) {
            final float w = getWidth(), h = getHeight();
            pen.setStyle(Paint.Style.STROKE);
            pen.setStrokeCap(Paint.Cap.ROUND);
            pen.setStrokeJoin(Paint.Join.ROUND);
            pen.setStrokeWidth(Math.max(2f, w * 0.13f));
            pen.setColor(blue);
            final Path p = new Path();
            p.moveTo(w * 0.14f, h * 0.54f);
            p.lineTo(w * 0.40f, h * 0.80f);
            p.lineTo(w * 0.88f, h * 0.22f);
            c.drawPath(p, pen);
        }
    }
