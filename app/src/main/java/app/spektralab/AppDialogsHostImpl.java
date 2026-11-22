package app.spektralab;

/** Delegating implementation of AppDialogsHost. */
final class AppDialogsHostImpl implements app.spektralab.ui.panels.AppDialogsHost {
    private final MainActivity activity;
    AppDialogsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.lang.String getPROMO_MUTED() { return activity.PROMO_MUTED; }
    @Override public java.lang.String getPROMO_PREFS() { return activity.PROMO_PREFS; }
    @Override public java.lang.String getPROMO_SEEN_CODE() { return activity.PROMO_SEEN_CODE; }
    @Override public java.lang.String getQQ_GROUP() { return activity.QQ_GROUP; }
    @Override public int getUI_BLUE() { return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) { activity.UI_BLUE = value; }
    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_LINE() { return activity.UI_LINE; }
    @Override public void setUI_LINE(int value) { activity.UI_LINE = value; }
    @Override public int getUI_MUTED() { return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) { activity.UI_MUTED = value; }
    @Override public int getUI_PAPER() { return activity.UI_PAPER; }
    @Override public void setUI_PAPER(int value) { activity.UI_PAPER = value; }
    @Override public boolean getBatchExportRunning() { return activity.batchExportRunning; }
    @Override public void setBatchExportRunning(boolean value) { activity.batchExportRunning = value; }
    @Override public app.spektralab.DeviceProbe getProbe() { return activity.probe; }
    @Override public void setProbe(app.spektralab.DeviceProbe value) { activity.probe = value; }
    @Override public android.graphics.Typeface getTfDisplay() { return activity.tfDisplay; }
    @Override public void setTfDisplay(android.graphics.Typeface value) { activity.tfDisplay = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public boolean getUiReady() { return activity.uiReady; }
    @Override public void setUiReady(boolean value) { activity.uiReady = value; }
    @Override public boolean copyToClipboardHostCall(java.lang.String a0, java.lang.String a1) { return activity.copyToClipboard(a0, a1); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public android.widget.LinearLayout groupRowHostCall(int a0, java.lang.String a1, java.lang.String a2, java.lang.String a3) { return activity.groupRow(a0, a1, a2, a3); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public android.widget.TextView mixedLabelHostCall(java.lang.String a0, float a1, int a2) { return activity.mixedLabel(a0, a1, a2); }
    @Override public void openQqGroupHostCall() { activity.openQqGroup(); }
    @Override public void popInCardHostCall(android.view.View a0) { activity.popInCard(a0); }
    @Override public void reportFailureHostCall(java.lang.String a0, java.lang.String a1) { activity.reportFailure(a0, a1); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void runOnUiThreadHostCall(Runnable r) { activity.runOnUiThread(r); }
    @Override public boolean isFinishingHostCall() { return activity.isFinishing(); }
    @Override public android.content.res.Resources getResourcesHostCall() { return activity.getResources(); }
    @Override public String getPackageNameHostCall() { return activity.getPackageName(); }
    @Override public android.content.Context context() { return activity; }
}
