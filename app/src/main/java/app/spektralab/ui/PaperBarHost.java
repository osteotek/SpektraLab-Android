package app.spektralab.ui;

/** The Activity state a {@link PaperBarView} reads. */
public interface PaperBarHost {
    boolean arrivalAnim();
    void markFile(String what);
    android.graphics.Typeface faceFor(CharSequence text);
    android.graphics.Typeface bodyFace();
    android.graphics.Typeface boldFace();
    android.graphics.Typeface hanFace();
    android.view.animation.Interpolator easeOut();
    int blue();
}
