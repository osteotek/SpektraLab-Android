package app.spektralab;

/** Delegating implementation of PaperBarHost. */
final class PaperBarHostImpl implements app.spektralab.ui.PaperBarHost {
    private final MainActivity activity;
    PaperBarHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public boolean arrivalAnim() { return activity.arrivalAnim(); }
    @Override public void markFile(String what) { MainActivity.markFile(what); }
    @Override public android.graphics.Typeface faceFor(CharSequence text) { return activity.faceFor(text); }
    @Override public android.graphics.Typeface bodyFace() { return MainActivity.tfBody; }
    @Override public android.graphics.Typeface boldFace() { return MainActivity.tfBold; }
    @Override public android.graphics.Typeface hanFace() { return activity.tfHan; }
    @Override public android.view.animation.Interpolator easeOut() { return MainActivity.EASE_OUT; }
    @Override public int blue() { return MainActivity.UI_BLUE; }
}
