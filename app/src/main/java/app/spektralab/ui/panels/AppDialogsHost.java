package app.spektralab.ui.panels;

/** Narrow Activity surface for the AppDialogs builders. Implemented by AppDialogsHostImpl. */
public interface AppDialogsHost {
    java.lang.String getPROMO_MUTED();
    java.lang.String getPROMO_PREFS();
    java.lang.String getPROMO_SEEN_CODE();
    java.lang.String getQQ_GROUP();
    int getUI_BLUE();
    void setUI_BLUE(int value);
    int getUI_INK();
    void setUI_INK(int value);
    int getUI_LINE();
    void setUI_LINE(int value);
    int getUI_MUTED();
    void setUI_MUTED(int value);
    int getUI_PAPER();
    void setUI_PAPER(int value);
    boolean getBatchExportRunning();
    void setBatchExportRunning(boolean value);
    app.spektralab.DeviceProbe getProbe();
    void setProbe(app.spektralab.DeviceProbe value);
    android.graphics.Typeface getTfDisplay();
    void setTfDisplay(android.graphics.Typeface value);
    android.graphics.Typeface getTfHan();
    void setTfHan(android.graphics.Typeface value);
    boolean getUiReady();
    void setUiReady(boolean value);
    boolean copyToClipboardHostCall(java.lang.String a0, java.lang.String a1);
    int dpHostCall(float a0);
    android.widget.LinearLayout groupRowHostCall(int a0, java.lang.String a1, java.lang.String a2, java.lang.String a3);
    android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3);
    android.widget.TextView mixedLabelHostCall(java.lang.String a0, float a1, int a2);
    void openQqGroupHostCall();
    void popInCardHostCall(android.view.View a0);
    void reportFailureHostCall(java.lang.String a0, java.lang.String a1);
    void showInAppToastHostCall(java.lang.CharSequence a0);
    boolean isFinishingHostCall();
    void runOnUiThreadHostCall(Runnable r);
    android.content.res.Resources getResourcesHostCall();
    String getPackageNameHostCall();
    android.content.Context context();
}
