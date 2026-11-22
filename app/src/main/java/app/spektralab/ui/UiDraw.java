package app.spektralab.ui;

/** Pure colour helpers shared by the extracted custom Views. */
public final class UiDraw {
    private UiDraw() {}

    /** Leave one ARGB for another by `t`. */
    public static int blend(int a, int b, float t) {
        if (t <= 0f) return a;
        if (t >= 1f) return b;
        final int aa = (a >>> 24) + Math.round(((b >>> 24) - (a >>> 24)) * t);
        final int ar = ((a >> 16) & 255)
                + Math.round((((b >> 16) & 255) - ((a >> 16) & 255)) * t);
        final int ag = ((a >> 8) & 255)
                + Math.round((((b >> 8) & 255) - ((a >> 8) & 255)) * t);
        final int ab = (a & 255) + Math.round(((b & 255) - (a & 255)) * t);
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }

    /** One channel of `#AARRGGBB` at a new alpha. */
    public static int withAlpha(int color, int a) {
        return ((a < 0 ? 0 : (a > 255 ? 255 : a)) << 24) | (color & 0x00FFFFFF);
    }
}
