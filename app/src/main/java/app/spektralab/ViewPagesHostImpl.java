package app.spektralab;
import app.spektralab.ui.Square;

/** Delegating implementation of ViewPagesHost. */
final class ViewPagesHostImpl implements app.spektralab.ui.panels.ViewPagesHost {
    private final MainActivity activity;
    ViewPagesHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public android.view.animation.Interpolator getEASE_OUT() { return activity.EASE_OUT; }
    @Override public java.lang.String[] getHSL_BANDS() { return activity.HSL_BANDS; }
    @Override public void setHSL_BANDS(java.lang.String[] value) { activity.HSL_BANDS = value; }
    @Override public int getMS_SOFT() { return activity.MS_SOFT; }
    @Override public int getUI_BLUE() { return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) { activity.UI_BLUE = value; }
    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_MUTED() { return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) { activity.UI_MUTED = value; }
    @Override public java.lang.String[] getVIEW_PAGE_NAMES() { return activity.VIEW_PAGE_NAMES; }
    @Override public void setVIEW_PAGE_NAMES(java.lang.String[] value) { activity.VIEW_PAGE_NAMES = value; }
    @Override public float getCameraEv() { return activity.cameraEv; }
    @Override public void setCameraEv(float value) { activity.cameraEv = value; }
    @Override public float[] getCbLum() { return activity.cbLum; }
    @Override public android.widget.TextView getCbReadout() { return activity.cbReadout; }
    @Override public void setCbReadout(android.widget.TextView value) { activity.cbReadout = value; }
    @Override public int getCbZone() { return activity.cbZone; }
    @Override public void setCbZone(int value) { activity.cbZone = value; }
    @Override public app.spektralab.MainActivity.ColorWheel getColorWheel() { return activity.colorWheel; }
    @Override public void setColorWheel(app.spektralab.MainActivity.ColorWheel value) { activity.colorWheel = value; }
    @Override public int getCurveChannel() { return activity.curveChannel; }
    @Override public void setCurveChannel(int value) { activity.curveChannel = value; }
    @Override public app.spektralab.MainActivity.CurveView getCurveView() { return activity.curveView; }
    @Override public void setCurveView(app.spektralab.MainActivity.CurveView value) { activity.curveView = value; }
    @Override public android.widget.TextView[] getEffectChips() { return activity.effectChips; }
    @Override public void setEffectChips(android.widget.TextView[] value) { activity.effectChips = value; }
    @Override public android.widget.LinearLayout[] getEffectPages() { return activity.effectPages; }
    @Override public void setEffectPages(android.widget.LinearLayout[] value) { activity.effectPages = value; }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public int getHslBand() { return activity.hslBand; }
    @Override public void setHslBand(int value) { activity.hslBand = value; }
    @Override public float[] getHslHue() { return activity.hslHue; }
    @Override public float[] getHslLum() { return activity.hslLum; }
    @Override public float[] getHslSat() { return activity.hslSat; }
    @Override public android.view.View[] getHslSwatches() { return activity.hslSwatches; }
    @Override public void setHslSwatches(android.view.View[] value) { activity.hslSwatches = value; }
    @Override public boolean getRawAutoBright() { return activity.rawAutoBright; }
    @Override public void setRawAutoBright(boolean value) { activity.rawAutoBright = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public int getToolPage() { return activity.toolPage; }
    @Override public void setToolPage(int value) { activity.toolPage = value; }
    @Override public float getViewBlack() { return activity.viewBlack; }
    @Override public void setViewBlack(float value) { activity.viewBlack = value; }
    @Override public float getViewBrightness() { return activity.viewBrightness; }
    @Override public void setViewBrightness(float value) { activity.viewBrightness = value; }
    @Override public android.widget.LinearLayout getViewCameraEvBox() { return activity.viewCameraEvBox; }
    @Override public void setViewCameraEvBox(android.widget.LinearLayout value) { activity.viewCameraEvBox = value; }
    @Override public android.widget.TextView getViewCameraEvNote() { return activity.viewCameraEvNote; }
    @Override public void setViewCameraEvNote(android.widget.TextView value) { activity.viewCameraEvNote = value; }
    @Override public android.widget.TextView[] getViewCatChips() { return activity.viewCatChips; }
    @Override public void setViewCatChips(android.widget.TextView[] value) { activity.viewCatChips = value; }
    @Override public float getViewContrast() { return activity.viewContrast; }
    @Override public void setViewContrast(float value) { activity.viewContrast = value; }
    @Override public float getViewExposure() { return activity.viewExposure; }
    @Override public void setViewExposure(float value) { activity.viewExposure = value; }
    @Override public android.widget.TextView getViewExposureNote() { return activity.viewExposureNote; }
    @Override public void setViewExposureNote(android.widget.TextView value) { activity.viewExposureNote = value; }
    @Override public float getViewHighlights() { return activity.viewHighlights; }
    @Override public void setViewHighlights(float value) { activity.viewHighlights = value; }
    @Override public int getViewPage() { return activity.viewPage; }
    @Override public void setViewPage(int value) { activity.viewPage = value; }
    @Override public android.widget.LinearLayout[] getViewPages() { return activity.viewPages; }
    @Override public java.util.List<java.lang.Runnable> getViewRefreshers() { return activity.viewRefreshers; }
    @Override public float getViewSaturation() { return activity.viewSaturation; }
    @Override public void setViewSaturation(float value) { activity.viewSaturation = value; }
    @Override public app.spektralab.MainActivity.CappedScroll[] getViewScrolls() { return activity.viewScrolls; }
    @Override public float getViewShadows() { return activity.viewShadows; }
    @Override public void setViewShadows(float value) { activity.viewShadows = value; }
    @Override public float getViewSharpen() { return activity.viewSharpen; }
    @Override public void setViewSharpen(float value) { activity.viewSharpen = value; }
    @Override public float getViewSharpenMask() { return activity.viewSharpenMask; }
    @Override public void setViewSharpenMask(float value) { activity.viewSharpenMask = value; }
    @Override public float getViewSharpenRadius() { return activity.viewSharpenRadius; }
    @Override public void setViewSharpenRadius(float value) { activity.viewSharpenRadius = value; }
    @Override public float getViewTemperature() { return activity.viewTemperature; }
    @Override public void setViewTemperature(float value) { activity.viewTemperature = value; }
    @Override public float getViewTexture() { return activity.viewTexture; }
    @Override public void setViewTexture(float value) { activity.viewTexture = value; }
    @Override public float getViewTint() { return activity.viewTint; }
    @Override public void setViewTint(float value) { activity.viewTint = value; }
    @Override public float getViewVibrance() { return activity.viewVibrance; }
    @Override public void setViewVibrance(float value) { activity.viewVibrance = value; }
    @Override public float getViewVignette() { return activity.viewVignette; }
    @Override public void setViewVignette(float value) { activity.viewVignette = value; }
    @Override public float getViewWhite() { return activity.viewWhite; }
    @Override public void setViewWhite(float value) { activity.viewWhite = value; }
    @Override public android.widget.TextView[] getWbChips() { return activity.wbChips; }
    @Override public void setWbChips(android.widget.TextView[] value) { activity.wbChips = value; }
    @Override public android.widget.LinearLayout getWbDecodeBox() { return activity.wbDecodeBox; }
    @Override public void setWbDecodeBox(android.widget.LinearLayout value) { activity.wbDecodeBox = value; }
    @Override public android.widget.TextView getWbDecodeNote() { return activity.wbDecodeNote; }
    @Override public void setWbDecodeNote(android.widget.TextView value) { activity.wbDecodeNote = value; }
    @Override public float getWbKelvin() { return activity.wbKelvin; }
    @Override public void setWbKelvin(float value) { activity.wbKelvin = value; }
    @Override public int getWbPreset() { return activity.wbPreset; }
    @Override public void setWbPreset(int value) { activity.wbPreset = value; }
    @Override public float getWbTint() { return activity.wbTint; }
    @Override public void setWbTint(float value) { activity.wbTint = value; }
    @Override public android.widget.LinearLayout adjustRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, float a2, float a3, java.util.function.Supplier<java.lang.Float> a4, java.util.function.Consumer<java.lang.Float> a5, boolean a6) { return activity.adjustRow(a0, a1, a2, a3, a4, a5, a6); }
    @Override public android.widget.LinearLayout adjustRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, float a2, float a3, java.util.function.Supplier<java.lang.Float> a4, java.util.function.Consumer<java.lang.Float> a5, boolean a6, java.lang.String a7) { return activity.adjustRow(a0, a1, a2, a3, a4, a5, a6, a7); }
    @Override public android.widget.LinearLayout adjustRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, float a2, float a3, java.util.function.Supplier<java.lang.Float> a4, java.util.function.Consumer<java.lang.Float> a5, boolean a6, java.lang.String a7, java.util.List<java.lang.Runnable> a8, boolean a9) { return activity.adjustRow(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
    @Override public void attachBangHostCall(android.widget.LinearLayout a0, java.util.function.Supplier<java.lang.String> a1) { activity.attachBang(a0, a1); }
    @Override public android.widget.TextView choiceCellHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, java.util.function.Consumer<android.widget.TextView> a3) { return activity.choiceCell(a0, a1, a2, a3); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1) { activity.enterPanel(a0, a1); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1, boolean a2) { activity.enterPanel(a0, a1, a2); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1, boolean a2, int a3) { activity.enterPanel(a0, a1, a2, a3); }
    @Override public void fillGlowControlsHostCall(android.widget.LinearLayout a0) { activity.fillGlowControls(a0); }
    @Override public void hideAtOnceHostCall(android.view.View a0) { activity.hideAtOnce(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public void markFileViaHost(java.lang.String a0) { activity.markFile(a0); }
    @Override public void openViewToolHostCall(int a0) { activity.openViewTool(a0); }
    @Override public android.graphics.drawable.GradientDrawable outlineBoxHostCall(boolean a0) { return activity.outlineBox(a0); }
    @Override public void redecodeWithWhiteBalanceHostCall() { activity.redecodeWithWhiteBalance(); }
    @Override public void refreshCbReadoutHostCall() { activity.refreshCbReadout(); }
    @Override public void refreshHslSwatchesHostCall() { activity.refreshHslSwatches(); }
    @Override public void refreshOriginalViewHostCall() { activity.refreshOriginalView(); }
    @Override public void refreshViewPanelHostCall() { activity.refreshViewPanel(); }
    @Override public void refreshWbChipsHostCall() { activity.refreshWbChips(); }
    @Override public void showEffectTabHostCall(int a0) { activity.showEffectTab(a0); }
    @Override public void syncSubRowActiveHostCall() { activity.syncSubRowActive(); }
    @Override public void tintTextHostCall(android.widget.TextView a0, int a1, long a2) { activity.tintText(a0, a1, a2); }
    @Override public void toggleCellHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, Square a3, java.util.function.BooleanSupplier a4, java.util.function.Consumer<java.lang.Boolean> a5) { activity.toggleCell(a0, a1, a2, a3, a4, a5); }
    @Override public void toolHeaderHostCall(android.widget.LinearLayout a0, java.lang.String a1) { activity.toolHeader(a0, a1); }
    @Override public int uiToggleOffViaHost() { return activity.uiToggleOff(); }
    @Override public app.spektralab.MainActivity.ColorWheel newColorWheel() { return activity.new ColorWheel(); }
    @Override public app.spektralab.MainActivity.CurveView newCurveView() { return activity.new CurveView(); }
    @Override public android.content.Context context() { return activity; }
}
