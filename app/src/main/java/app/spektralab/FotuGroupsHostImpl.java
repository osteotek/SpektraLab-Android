package app.spektralab;

/** Delegating implementation of FotuGroupsHost. */
final class FotuGroupsHostImpl implements app.spektralab.ui.panels.FotuGroupsHost {
    private final MainActivity activity;
    FotuGroupsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public int getMS_SOFT() { return activity.MS_SOFT; }
    @Override public int getUI_BLUE() { return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) { activity.UI_BLUE = value; }
    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_MUTED() { return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) { activity.UI_MUTED = value; }
    @Override public java.util.HashMap<java.lang.String, java.lang.Long> getArrived() { return activity.arrived; }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public float getFotuBlackPoint() { return activity.fotuBlackPoint; }
    @Override public void setFotuBlackPoint(float value) { activity.fotuBlackPoint = value; }
    @Override public float getFotuBleach() { return activity.fotuBleach; }
    @Override public void setFotuBleach(float value) { activity.fotuBleach = value; }
    @Override public android.widget.LinearLayout getFotuBody() { return activity.fotuBody; }
    @Override public void setFotuBody(android.widget.LinearLayout value) { activity.fotuBody = value; }
    @Override public float getFotuCondenser() { return activity.fotuCondenser; }
    @Override public void setFotuCondenser(float value) { activity.fotuCondenser = value; }
    @Override public float getFotuCouplerGB() { return activity.fotuCouplerGB; }
    @Override public void setFotuCouplerGB(float value) { activity.fotuCouplerGB = value; }
    @Override public float getFotuCouplerRG() { return activity.fotuCouplerRG; }
    @Override public void setFotuCouplerRG(float value) { activity.fotuCouplerRG = value; }
    @Override public float getFotuCouplerReach() { return activity.fotuCouplerReach; }
    @Override public void setFotuCouplerReach(float value) { activity.fotuCouplerReach = value; }
    @Override public float getFotuCouplerSelf() { return activity.fotuCouplerSelf; }
    @Override public void setFotuCouplerSelf(float value) { activity.fotuCouplerSelf = value; }
    @Override public float getFotuCouplers() { return activity.fotuCouplers; }
    @Override public void setFotuCouplers(float value) { activity.fotuCouplers = value; }
    @Override public java.lang.String getFotuDigitalRef() { return activity.fotuDigitalRef; }
    @Override public void setFotuDigitalRef(java.lang.String value) { activity.fotuDigitalRef = value; }
    @Override public float getFotuEncodedGrade() { return activity.fotuEncodedGrade; }
    @Override public void setFotuEncodedGrade(float value) { activity.fotuEncodedGrade = value; }
    @Override public float getFotuEv() { return activity.fotuEv; }
    @Override public void setFotuEv(float value) { activity.fotuEv = value; }
    @Override public float getFotuExpired() { return activity.fotuExpired; }
    @Override public void setFotuExpired(float value) { activity.fotuExpired = value; }
    @Override public boolean getFotuFilmGrain() { return activity.fotuFilmGrain; }
    @Override public void setFotuFilmGrain(boolean value) { activity.fotuFilmGrain = value; }
    @Override public app.spektralab.MainActivity.Square getFotuFilmGrainMark() { return activity.fotuFilmGrainMark; }
    @Override public void setFotuFilmGrainMark(app.spektralab.MainActivity.Square value) { activity.fotuFilmGrainMark = value; }
    @Override public float getFotuFlare() { return activity.fotuFlare; }
    @Override public void setFotuFlare(float value) { activity.fotuFlare = value; }
    @Override public float getFotuFringe() { return activity.fotuFringe; }
    @Override public void setFotuFringe(float value) { activity.fotuFringe = value; }
    @Override public float getFotuFringeRadius() { return activity.fotuFringeRadius; }
    @Override public void setFotuFringeRadius(float value) { activity.fotuFringeRadius = value; }
    @Override public float getFotuGradeGain() { return activity.fotuGradeGain; }
    @Override public void setFotuGradeGain(float value) { activity.fotuGradeGain = value; }
    @Override public float getFotuGradeLift() { return activity.fotuGradeLift; }
    @Override public void setFotuGradeLift(float value) { activity.fotuGradeLift = value; }
    @Override public float getFotuGrain() { return activity.fotuGrain; }
    @Override public void setFotuGrain(float value) { activity.fotuGrain = value; }
    @Override public float getFotuGrainB() { return activity.fotuGrainB; }
    @Override public void setFotuGrainB(float value) { activity.fotuGrainB = value; }
    @Override public float getFotuGrainColour() { return activity.fotuGrainColour; }
    @Override public void setFotuGrainColour(float value) { activity.fotuGrainColour = value; }
    @Override public float getFotuGrainG() { return activity.fotuGrainG; }
    @Override public void setFotuGrainG(float value) { activity.fotuGrainG = value; }
    @Override public android.widget.TextView getFotuGrainNote() { return activity.fotuGrainNote; }
    @Override public void setFotuGrainNote(android.widget.TextView value) { activity.fotuGrainNote = value; }
    @Override public float getFotuGrainR() { return activity.fotuGrainR; }
    @Override public void setFotuGrainR(float value) { activity.fotuGrainR = value; }
    @Override public float getFotuGrainSize() { return activity.fotuGrainSize; }
    @Override public void setFotuGrainSize(float value) { activity.fotuGrainSize = value; }
    @Override public float getFotuGrainSoft() { return activity.fotuGrainSoft; }
    @Override public void setFotuGrainSoft(float value) { activity.fotuGrainSoft = value; }
    @Override public int getFotuGroup() { return activity.fotuGroup; }
    @Override public void setFotuGroup(int value) { activity.fotuGroup = value; }
    @Override public int getFotuGroupShown() { return activity.fotuGroupShown; }
    @Override public void setFotuGroupShown(int value) { activity.fotuGroupShown = value; }
    @Override public android.widget.TextView[] getFotuGroupTabs() { return activity.fotuGroupTabs; }
    @Override public void setFotuGroupTabs(android.widget.TextView[] value) { activity.fotuGroupTabs = value; }
    @Override public float getFotuHalation() { return activity.fotuHalation; }
    @Override public void setFotuHalation(float value) { activity.fotuHalation = value; }
    @Override public float getFotuHalationColour() { return activity.fotuHalationColour; }
    @Override public void setFotuHalationColour(float value) { activity.fotuHalationColour = value; }
    @Override public float getFotuHighlightDetail() { return activity.fotuHighlightDetail; }
    @Override public void setFotuHighlightDetail(float value) { activity.fotuHighlightDetail = value; }
    @Override public float getFotuHighlights() { return activity.fotuHighlights; }
    @Override public void setFotuHighlights(float value) { activity.fotuHighlights = value; }
    @Override public float getFotuLocalTone() { return activity.fotuLocalTone; }
    @Override public void setFotuLocalTone(float value) { activity.fotuLocalTone = value; }
    @Override public app.spektralab.MainActivity.Square getFotuLocalToneMark() { return activity.fotuLocalToneMark; }
    @Override public void setFotuLocalToneMark(app.spektralab.MainActivity.Square value) { activity.fotuLocalToneMark = value; }
    @Override public float getFotuMottle() { return activity.fotuMottle; }
    @Override public void setFotuMottle(float value) { activity.fotuMottle = value; }
    @Override public float getFotuPreflash() { return activity.fotuPreflash; }
    @Override public void setFotuPreflash(float value) { activity.fotuPreflash = value; }
    @Override public float getFotuPrintCorrection() { return activity.fotuPrintCorrection; }
    @Override public void setFotuPrintCorrection(float value) { activity.fotuPrintCorrection = value; }
    @Override public java.lang.String getFotuPrintLight() { return activity.fotuPrintLight; }
    @Override public void setFotuPrintLight(java.lang.String value) { activity.fotuPrintLight = value; }
    @Override public float getFotuPrinter() { return activity.fotuPrinter; }
    @Override public void setFotuPrinter(float value) { activity.fotuPrinter = value; }
    @Override public float getFotuPrinterB() { return activity.fotuPrinterB; }
    @Override public void setFotuPrinterB(float value) { activity.fotuPrinterB = value; }
    @Override public float getFotuPrinterEv() { return activity.fotuPrinterEv; }
    @Override public void setFotuPrinterEv(float value) { activity.fotuPrinterEv = value; }
    @Override public float getFotuPrinterG() { return activity.fotuPrinterG; }
    @Override public void setFotuPrinterG(float value) { activity.fotuPrinterG = value; }
    @Override public float getFotuPrinterLamp() { return activity.fotuPrinterLamp; }
    @Override public void setFotuPrinterLamp(float value) { activity.fotuPrinterLamp = value; }
    @Override public float getFotuPrinterM() { return activity.fotuPrinterM; }
    @Override public void setFotuPrinterM(float value) { activity.fotuPrinterM = value; }
    @Override public app.spektralab.MainActivity.Square getFotuPrinterMark() { return activity.fotuPrinterMark; }
    @Override public void setFotuPrinterMark(app.spektralab.MainActivity.Square value) { activity.fotuPrinterMark = value; }
    @Override public float getFotuPrinterPreflash() { return activity.fotuPrinterPreflash; }
    @Override public void setFotuPrinterPreflash(float value) { activity.fotuPrinterPreflash = value; }
    @Override public float getFotuPrinterR() { return activity.fotuPrinterR; }
    @Override public void setFotuPrinterR(float value) { activity.fotuPrinterR = value; }
    @Override public float getFotuPrinterY() { return activity.fotuPrinterY; }
    @Override public void setFotuPrinterY(float value) { activity.fotuPrinterY = value; }
    @Override public android.widget.TextView getFotuPushValue() { return activity.fotuPushValue; }
    @Override public void setFotuPushValue(android.widget.TextView value) { activity.fotuPushValue = value; }
    @Override public app.spektralab.MainActivity.Square getFotuRebateMark() { return activity.fotuRebateMark; }
    @Override public void setFotuRebateMark(app.spektralab.MainActivity.Square value) { activity.fotuRebateMark = value; }
    @Override public java.util.List<java.lang.Runnable> getFotuRefreshers() { return activity.fotuRefreshers; }
    @Override public float getFotuSaturation() { return activity.fotuSaturation; }
    @Override public void setFotuSaturation(float value) { activity.fotuSaturation = value; }
    @Override public float getFotuShadowDetail() { return activity.fotuShadowDetail; }
    @Override public void setFotuShadowDetail(float value) { activity.fotuShadowDetail = value; }
    @Override public float getFotuShadows() { return activity.fotuShadows; }
    @Override public void setFotuShadows(float value) { activity.fotuShadows = value; }
    @Override public float getFotuTint() { return activity.fotuTint; }
    @Override public void setFotuTint(float value) { activity.fotuTint = value; }
    @Override public float getFotuVibrance() { return activity.fotuVibrance; }
    @Override public void setFotuVibrance(float value) { activity.fotuVibrance = value; }
    @Override public float getFotuWarmth() { return activity.fotuWarmth; }
    @Override public void setFotuWarmth(float value) { activity.fotuWarmth = value; }
    @Override public float getFotuWhitePoint() { return activity.fotuWhitePoint; }
    @Override public void setFotuWhitePoint(float value) { activity.fotuWhitePoint = value; }
    @Override public java.util.List<android.view.View> getGrainGated() { return activity.grainGated; }
    @Override public java.lang.String getOverscanFormat() { return activity.overscanFormat; }
    @Override public void setOverscanFormat(java.lang.String value) { activity.overscanFormat = value; }
    @Override public app.spektralab.MainActivity.Square getOverscanMark() { return activity.overscanMark; }
    @Override public void setOverscanMark(app.spektralab.MainActivity.Square value) { activity.overscanMark = value; }
    @Override public boolean getOverscanOn() { return activity.overscanOn; }
    @Override public void setOverscanOn(boolean value) { activity.overscanOn = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2) { activity.animateHeightTo(a0, a1, a2); }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2, int a3, java.lang.Runnable a4) { activity.animateHeightTo(a0, a1, a2, a3, a4); }
    @Override public void applyGateHostCall(java.lang.String a0) { activity.applyGate(a0); }
    @Override public void componentArrivalHostCall(android.view.View a0) { activity.componentArrival(a0); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1) { activity.enterPanel(a0, a1); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1, boolean a2) { activity.enterPanel(a0, a1, a2); }
    @Override public void enterPanelHostCall(android.view.View a0, int a1, boolean a2, int a3) { activity.enterPanel(a0, a1, a2, a3); }
    @Override public void fotuChoiceRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, java.lang.String[] a3, java.lang.String[] a4, java.util.function.Supplier<java.lang.String> a5, java.util.function.Consumer<java.lang.String> a6) { activity.fotuChoiceRow(a0, a1, a2, a3, a4, a5, a6); }
    @Override public android.view.View[] fotuRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, float a2, float a3, java.util.function.Supplier<java.lang.Float> a4, java.util.function.Consumer<java.lang.Float> a5, java.lang.String a6) { return activity.fotuRow(a0, a1, a2, a3, a4, a5, a6); }
    @Override public boolean fotuScreenMediumHostCall() { return activity.fotuScreenMedium(); }
    @Override public void gateGrainHostCall(android.view.View[] a0) { activity.gateGrain(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public int naturalHeightViaHost(android.view.View a0, int a1) { return activity.naturalHeight(a0, a1); }
    @Override public void refreshFotuPushHostCall() { activity.refreshFotuPush(); }
    @Override public void refreshGrainGatesHostCall() { activity.refreshGrainGates(); }
    @Override public void renderFotuHostCall() { activity.renderFotu(); }
    @Override public void renderImageHostCall() { activity.renderImage(); }
    @Override public void renderImageHostCall(int a0) { activity.renderImage(a0); }
    @Override public void showFotuPushPickerHostCall() { activity.showFotuPushPicker(); }
    @Override public void syncSubRowActiveHostCall() { activity.syncSubRowActive(); }
    @Override public void tintTextHostCall(android.widget.TextView a0, int a1, long a2) { activity.tintText(a0, a1, a2); }
    @Override public void toggleCellHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, app.spektralab.MainActivity.Square a3, java.util.function.BooleanSupplier a4, java.util.function.Consumer<java.lang.Boolean> a5) { activity.toggleCell(a0, a1, a2, a3, a4, a5); }
    @Override public android.view.View toggleRowHostCall(java.lang.String a0, app.spektralab.MainActivity.Square a1, java.util.function.BooleanSupplier a2, java.util.function.Consumer<java.lang.Boolean> a3) { return activity.toggleRow(a0, a1, a2, a3); }
    @Override public int uiToggleOffViaHost() { return activity.uiToggleOff(); }
    @Override public void underlineViaHost(android.widget.TextView a0, boolean a1) { activity.underline(a0, a1); }
    @Override public void runOnUiThreadHostCall(Runnable r) { activity.runOnUiThread(r); }
    @Override public android.content.Context context() { return activity; }
}
