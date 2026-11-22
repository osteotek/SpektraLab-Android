package app.spektralab;
import app.spektralab.ui.CropOverlay;
import app.spektralab.ui.RotateScale;

/** Delegating implementation of ComposePagesHost. */
final class ComposePagesHostImpl implements app.spektralab.ui.panels.ComposePagesHost {
    private final MainActivity activity;
    ComposePagesHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.lang.String[] getEDGE_LABELS() { return activity.EDGE_LABELS; }
    @Override public void setEDGE_LABELS(java.lang.String[] value) { activity.EDGE_LABELS = value; }
    @Override public double[] getEDGE_RATIOS() { return activity.EDGE_RATIOS; }
    @Override public java.lang.String[] getEDGE_RATIO_LABELS() { return activity.EDGE_RATIO_LABELS; }
    @Override public java.lang.String[] getPORTRAIT_LABELS() { return activity.PORTRAIT_LABELS; }
    @Override public float[] getPORTRAIT_RATIOS() { return activity.PORTRAIT_RATIOS; }
    @Override public int getUI_BLUE() { return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) { activity.UI_BLUE = value; }
    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_MUTED() { return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) { activity.UI_MUTED = value; }
    @Override public int getActiveTabIndex() { return activity.activeTabIndex; }
    @Override public void setActiveTabIndex(int value) { activity.activeTabIndex = value; }
    @Override public android.widget.LinearLayout getComposeBody() { return activity.composeBody; }
    @Override public void setComposeBody(android.widget.LinearLayout value) { activity.composeBody = value; }
    @Override public android.widget.TextView getComposeCropTab() { return activity.composeCropTab; }
    @Override public void setComposeCropTab(android.widget.TextView value) { activity.composeCropTab = value; }
    @Override public android.widget.TextView getComposeRotateTab() { return activity.composeRotateTab; }
    @Override public void setComposeRotateTab(android.widget.TextView value) { activity.composeRotateTab = value; }
    @Override public int getComposeSubPage() { return activity.composeSubPage; }
    @Override public void setComposeSubPage(int value) { activity.composeSubPage = value; }
    @Override public CropOverlay getCropOverlay() { return activity.cropOverlay; }
    @Override public void setCropOverlay(CropOverlay value) { activity.cropOverlay = value; }
    @Override public int getCropRatioIndex() { return activity.cropRatioIndex; }
    @Override public void setCropRatioIndex(int value) { activity.cropRatioIndex = value; }
    @Override public android.widget.HorizontalScrollView getCropScroll() { return activity.cropScroll; }
    @Override public void setCropScroll(android.widget.HorizontalScrollView value) { activity.cropScroll = value; }
    @Override public int getCropScrollX() { return activity.cropScrollX; }
    @Override public void setCropScrollX(int value) { activity.cropScrollX = value; }
    @Override public boolean getGeometryFlipH() { return activity.geometryFlipH; }
    @Override public void setGeometryFlipH(boolean value) { activity.geometryFlipH = value; }
    @Override public boolean getGeometryFlipV() { return activity.geometryFlipV; }
    @Override public void setGeometryFlipV(boolean value) { activity.geometryFlipV = value; }
    @Override public float getGeometryRotation() { return activity.geometryRotation; }
    @Override public void setGeometryRotation(float value) { activity.geometryRotation = value; }
    @Override public float getPendingCropH() { return activity.pendingCropH; }
    @Override public void setPendingCropH(float value) { activity.pendingCropH = value; }
    @Override public float getPendingCropW() { return activity.pendingCropW; }
    @Override public void setPendingCropW(float value) { activity.pendingCropW = value; }
    @Override public float getPendingCropX() { return activity.pendingCropX; }
    @Override public void setPendingCropX(float value) { activity.pendingCropX = value; }
    @Override public float getPendingCropY() { return activity.pendingCropY; }
    @Override public void setPendingCropY(float value) { activity.pendingCropY = value; }
    @Override public RotateScale getRotateArc() { return activity.rotateArc; }
    @Override public void setRotateArc(RotateScale value) { activity.rotateArc = value; }
    @Override public boolean getRotateGridOn() { return activity.rotateGridOn; }
    @Override public void setRotateGridOn(boolean value) { activity.rotateGridOn = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public void applyCropRatioHostCall(int a0) { activity.applyCropRatio(a0); }
    @Override public android.graphics.drawable.GradientDrawable cardBorderHostCall(boolean a0) { return activity.cardBorder(a0); }
    @Override public android.view.View circleActionHostCall(java.lang.String a0, java.lang.String a1, java.lang.Runnable a2) { return activity.circleAction(a0, a1, a2); }
    @Override public void commitComposeHostCall() { activity.commitCompose(); }
    @Override public void commitPendingCropHostCall() { activity.commitPendingCrop(); }
    @Override public float cropAspectForSelectionHostCall() { return activity.cropAspectForSelection(); }
    @Override public java.lang.String cropSummaryHostCall() { return activity.cropSummary(); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public float originalAspectHostCall() { return activity.originalAspect(); }
    @Override public void syncRotateGridHostCall() { activity.syncRotateGrid(); }
    @Override public void turnByHostCall(int a0) { activity.turnBy(a0); }
    @Override public void underlineViaHost(android.widget.TextView a0, boolean a1) { activity.underline(a0, a1); }
    @Override public RotateScale newRotateScale() { return new app.spektralab.ui.RotateScale(activity, app.spektralab.MainActivity.tfBody, activity.rotateScaleDelegate); }
    @Override public android.content.Context context() { return activity; }
}
