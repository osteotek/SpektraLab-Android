package app.spektralab;
import app.spektralab.ui.HistogramView;

/** Plugin-free implementation of the panel host, kept out of MainActivity
 * so the Activity remains lifecycle + wiring only. */
final class PanelHostImpl implements app.spektralab.ui.panels.PanelHost {
    private final MainActivity activity;
    PanelHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public void openEdgePageHostCall(int a0) { activity.openEdgePageHostCall(a0); }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.getEngine(); }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.setEngine(value); }
    @Override public HistogramView getHistogram() { return activity.getHistogram(); }
    @Override public void setHistogram(HistogramView value) { activity.setHistogram(value); }
    @Override public float getPrintExposure() { return activity.getPrintExposure(); }
    @Override public void setPrintExposure(float value) { activity.setPrintExposure(value); }

    @Override public int dpHostCall(float a0) { return activity.dp(a0); }

    @Override public android.content.Context context() { return activity; }

    // ---- Phase C: PanelHost implementations ----
    @Override public int getUI_BLUE() {  return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) {  activity.UI_BLUE = value; }
    @Override public int getUI_INK() {  return activity.UI_INK; }
    @Override public void setUI_INK(int value) {  activity.UI_INK = value; }
    @Override public int getUI_MUTED() {  return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) {  activity.UI_MUTED = value; }
    @Override public float getAntihalationRemoved() {  return activity.antihalationRemoved; }
    @Override public void setAntihalationRemoved(float value) {  activity.antihalationRemoved = value; }
    @Override public android.widget.LinearLayout getBrandRow() {  return activity.brandRow; }
    @Override public void setBrandRow(android.widget.LinearLayout value) {  activity.brandRow = value; }
    @Override public android.widget.LinearLayout getComposeBody() {  return activity.composeBody; }
    @Override public void setComposeBody(android.widget.LinearLayout value) {  activity.composeBody = value; }
    @Override public android.widget.TextView getComposeCropTab() {  return activity.composeCropTab; }
    @Override public void setComposeCropTab(android.widget.TextView value) {  activity.composeCropTab = value; }
    @Override public android.widget.TextView getComposeRotateTab() {  return activity.composeRotateTab; }
    @Override public void setComposeRotateTab(android.widget.TextView value) {  activity.composeRotateTab = value; }
    @Override public android.widget.LinearLayout getControls() {  return activity.controls; }
    @Override public void setControls(android.widget.LinearLayout value) {  activity.controls = value; }
    @Override public float getCouplersOn() {  return activity.couplersOn; }
    @Override public void setCouplersOn(float value) {  activity.couplersOn = value; }
    @Override public java.util.List<float[]>[] getCurvePoints() {  return activity.curvePoints; }
    @Override public float getDensityGamma() {  return activity.densityGamma; }
    @Override public void setDensityGamma(float value) {  activity.densityGamma = value; }
    @Override public boolean getDiBlueComp() {  return activity.diBlueComp; }
    @Override public void setDiBlueComp(boolean value) {  activity.diBlueComp = value; }
    @Override public boolean getDigitalIntermediate() {  return activity.digitalIntermediate; }
    @Override public void setDigitalIntermediate(boolean value) {  activity.digitalIntermediate = value; }
    @Override public android.widget.LinearLayout getEdgeRoot() {  return activity.edgeRoot; }
    @Override public void setEdgeRoot(android.widget.LinearLayout value) {  activity.edgeRoot = value; }
    @Override public android.widget.LinearLayout getFilmCardStrip() {  return activity.filmCardStrip; }
    @Override public void setFilmCardStrip(android.widget.LinearLayout value) {  activity.filmCardStrip = value; }
    @Override public android.view.View getFilmPaperRow() {  return activity.filmPaperRow; }
    @Override public void setFilmPaperRow(android.view.View value) {  activity.filmPaperRow = value; }
    @Override public android.widget.HorizontalScrollView getFilmScroll() {  return activity.filmScroll; }
    @Override public void setFilmScroll(android.widget.HorizontalScrollView value) {  activity.filmScroll = value; }
    @Override public android.widget.LinearLayout getFilmShelfHost() {  return activity.filmShelfHost; }
    @Override public void setFilmShelfHost(android.widget.LinearLayout value) {  activity.filmShelfHost = value; }
    @Override public float getFilterScale() {  return activity.filterScale; }
    @Override public void setFilterScale(float value) {  activity.filterScale = value; }
    @Override public android.widget.LinearLayout getFormatCardStrip() {  return activity.formatCardStrip; }
    @Override public void setFormatCardStrip(android.widget.LinearLayout value) {  activity.formatCardStrip = value; }
    @Override public android.widget.TextView getFormatFootLabel() {  return activity.formatFootLabel; }
    @Override public void setFormatFootLabel(android.widget.TextView value) {  activity.formatFootLabel = value; }
    @Override public android.widget.TextView getFormatFootValue() {  return activity.formatFootValue; }
    @Override public void setFormatFootValue(android.widget.TextView value) {  activity.formatFootValue = value; }
    @Override public android.widget.HorizontalScrollView getFormatScroll() {  return activity.formatScroll; }
    @Override public void setFormatScroll(android.widget.HorizontalScrollView value) {  activity.formatScroll = value; }
    @Override public android.widget.LinearLayout getFotuBody() {  return activity.fotuBody; }
    @Override public void setFotuBody(android.widget.LinearLayout value) {  activity.fotuBody = value; }
    @Override public android.widget.TextView[] getFotuGroupTabs() {  return activity.fotuGroupTabs; }
    @Override public void setFotuGroupTabs(android.widget.TextView[] value) {  activity.fotuGroupTabs = value; }
    @Override public float getGlareActive() {  return activity.glareActive; }
    @Override public void setGlareActive(float value) {  activity.glareActive = value; }
    @Override public float getGlareAmount() {  return activity.glareAmount; }
    @Override public void setGlareAmount(float value) {  activity.glareAmount = value; }
    @Override public float getGrainAmount() {  return activity.grainAmount; }
    @Override public void setGrainAmount(float value) {  activity.grainAmount = value; }
    @Override public float getGrainOn() {  return activity.grainOn; }
    @Override public void setGrainOn(float value) {  activity.grainOn = value; }
    @Override public float getHalationAmount() {  return activity.halationAmount; }
    @Override public void setHalationAmount(float value) {  activity.halationAmount = value; }
    @Override public float getHalationBoost() {  return activity.halationBoost; }
    @Override public void setHalationBoost(float value) {  activity.halationBoost = value; }
    @Override public float getHalationHeadroom() {  return activity.halationHeadroom; }
    @Override public void setHalationHeadroom(float value) {  activity.halationHeadroom = value; }
    @Override public float getHalationOn() {  return activity.halationOn; }
    @Override public void setHalationOn(float value) {  activity.halationOn = value; }
    @Override public float getHalationScatter() {  return activity.halationScatter; }
    @Override public void setHalationScatter(float value) {  activity.halationScatter = value; }
    @Override public float getHighlightRoom() {  return activity.highlightRoom; }
    @Override public void setHighlightRoom(float value) {  activity.highlightRoom = value; }
    @Override public float getHlKnee() {  return activity.hlKnee; }
    @Override public void setHlKnee(float value) {  activity.hlKnee = value; }
    @Override public boolean getHlManual() {  return activity.hlManual; }
    @Override public void setHlManual(boolean value) {  activity.hlManual = value; }
    @Override public float getHlRolloff() {  return activity.hlRolloff; }
    @Override public void setHlRolloff(float value) {  activity.hlRolloff = value; }
    @Override public float getHlRoom() {  return activity.hlRoom; }
    @Override public void setHlRoom(float value) {  activity.hlRoom = value; }
    @Override public java.util.List<app.spektralab.MainActivity.Knob> getKnobs() {  return activity.knobs; }
    @Override public float getLensBlur() {  return activity.lensBlur; }
    @Override public void setLensBlur(float value) {  activity.lensBlur = value; }
    @Override public float getMFilter() {  return activity.mFilter; }
    @Override public void setMFilter(float value) {  activity.mFilter = value; }
    @Override public float getMaxLift() {  return activity.maxLift; }
    @Override public void setMaxLift(float value) {  activity.maxLift = value; }
    @Override public android.widget.TextView getMeterChip() {  return activity.meterChip; }
    @Override public void setMeterChip(android.widget.TextView value) {  activity.meterChip = value; }
    @Override public android.widget.LinearLayout getOpticsPanel() {  return activity.opticsPanel; }
    @Override public void setOpticsPanel(android.widget.LinearLayout value) {  activity.opticsPanel = value; }
    @Override public boolean getOutputCctf() {  return activity.outputCctf; }
    @Override public void setOutputCctf(boolean value) {  activity.outputCctf = value; }
    @Override public android.widget.LinearLayout getPaperStrip() {  return activity.paperStrip; }
    @Override public void setPaperStrip(android.widget.LinearLayout value) {  activity.paperStrip = value; }
    @Override public float getPreflash() {  return activity.preflash; }
    @Override public void setPreflash(float value) {  activity.preflash = value; }
    @Override public android.widget.LinearLayout getPrintBody() {  return activity.printBody; }
    @Override public void setPrintBody(android.widget.LinearLayout value) {  activity.printBody = value; }
    @Override public float getScannerBlack() {  return activity.scannerBlack; }
    @Override public void setScannerBlack(float value) {  activity.scannerBlack = value; }
    @Override public float getScannerLensBlur() {  return activity.scannerLensBlur; }
    @Override public void setScannerLensBlur(float value) {  activity.scannerLensBlur = value; }
    @Override public float getScannerWhite() {  return activity.scannerWhite; }
    @Override public void setScannerWhite(float value) {  activity.scannerWhite = value; }
    @Override public float getScatterRadius() {  return activity.scatterRadius; }
    @Override public void setScatterRadius(float value) {  activity.scatterRadius = value; }
    @Override public float getShadowRolloff() {  return activity.shadowRolloff; }
    @Override public void setShadowRolloff(float value) {  activity.shadowRolloff = value; }
    @Override public float getShadowRoom() {  return activity.shadowRoom; }
    @Override public void setShadowRoom(float value) {  activity.shadowRoom = value; }
    @Override public float getSpotEv() {  return activity.spotEv; }
    @Override public void setSpotEv(float value) {  activity.spotEv = value; }
    @Override public float getSpotR() {  return activity.spotR; }
    @Override public void setSpotR(float value) {  activity.spotR = value; }
    @Override public float getSpotX() {  return activity.spotX; }
    @Override public void setSpotX(float value) {  activity.spotX = value; }
    @Override public float getSpotY() {  return activity.spotY; }
    @Override public void setSpotY(float value) {  activity.spotY = value; }
    @Override public android.graphics.Typeface getTfBody() {  return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) {  activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() {  return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) {  activity.tfHan = value; }
    @Override public android.widget.TextView[] getViewCatChips() {  return activity.viewCatChips; }
    @Override public void setViewCatChips(android.widget.TextView[] value) {  activity.viewCatChips = value; }
    @Override public android.widget.TextView getViewLockNote() {  return activity.viewLockNote; }
    @Override public void setViewLockNote(android.widget.TextView value) {  activity.viewLockNote = value; }
    @Override public android.widget.LinearLayout[] getViewPages() {  return activity.viewPages; }
    @Override public android.widget.LinearLayout getViewRoot() {  return activity.viewRoot; }
    @Override public void setViewRoot(android.widget.LinearLayout value) {  activity.viewRoot = value; }
    @Override public app.spektralab.MainActivity.CappedScroll[] getViewScrolls() {  return activity.viewScrolls; }
    @Override public float getYFilter() {  return activity.yFilter; }
    @Override public void setYFilter(float value) {  activity.yFilter = value; }
    @Override public android.view.View bigCardHostCall(java.lang.String a0, java.lang.String a1, java.util.function.Supplier<java.lang.String> a2, java.lang.Runnable a3) { return activity.bigCard(a0, a1, a2, a3); }
    @Override public void buildColourPageHostCall(android.widget.LinearLayout a0) { activity.buildColourPage(a0); }
    @Override public void buildCurvesPageHostCall(android.widget.LinearLayout a0) { activity.buildCurvesPage(a0); }
    @Override public void buildDetailPageHostCall(android.widget.LinearLayout a0) { activity.buildDetailPage(a0); }
    @Override public void buildEdgePagesHostCall() { activity.buildEdgePages(); }
    @Override public void buildEffectsPageHostCall(android.widget.LinearLayout a0) { activity.buildEffectsPage(a0); }
    @Override public void buildGradingPageHostCall(android.widget.LinearLayout a0) { activity.buildGradingPage(a0); }
    @Override public void buildHslPageHostCall(android.widget.LinearLayout a0) { activity.buildHslPage(a0); }
    @Override public void buildTonePageHostCall(android.widget.LinearLayout a0) { activity.buildTonePage(a0); }
    @Override public android.widget.LinearLayout.LayoutParams cardLpHostCall() { return activity.cardLp(); }
    @Override public android.widget.TextView composeTabViewHostCall(java.lang.String a0, int a1) { return activity.composeTabView(a0, a1); }
    @Override public java.lang.String dateSummaryHostCall() { return activity.dateSummary(); }
    @Override public java.lang.String edgeSummaryHostCall() { return activity.edgeSummary(); }
    @Override public void knobHostCall(java.lang.String a0, java.lang.String a1, float a2, float a3, java.util.function.Consumer<java.lang.Float> a4, java.util.function.Supplier<java.lang.Float> a5) { activity.knob(a0, a1, a2, a3, a4, a5); }
    @Override public void knobHostCall(java.lang.String a0, java.lang.String a1, float a2, float a3, java.util.function.Consumer<java.lang.Float> a4, java.util.function.Supplier<java.lang.Float> a5, boolean a6) { activity.knob(a0, a1, a2, a3, a4, a5, a6); }
    @Override public void knobHostCall(java.lang.String a0, java.lang.String a1, float a2, float a3, java.util.function.Consumer<java.lang.Float> a4, java.util.function.Supplier<java.lang.Float> a5, boolean a6, boolean a7) { activity.knob(a0, a1, a2, a3, a4, a5, a6, a7); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public java.lang.String pfSummaryHostCall() { return activity.pfSummary(); }
    @Override public void rebuildFormatCardsHostCall() { activity.rebuildFormatCards(); }
    @Override public void rebuildPaperCardsHostCall() { activity.rebuildPaperCards(); }
    @Override public void refreshEdgeGroupsHostCall() { activity.refreshEdgeGroups(); }
    @Override public void refreshMeterChipHostCall() { activity.refreshMeterChip(); }
    @Override public void refreshViewLockHostCall() { activity.refreshViewLock(); }
    @Override public void resetComposePanelHostCall() { activity.resetComposePanel(); }
    @Override public void resetEdgePanelHostCall() { activity.resetEdgePanel(); }
    @Override public void resetFilmPanelHostCall() { activity.resetFilmPanel(); }
    @Override public void resetFormatPanelHostCall() { activity.resetFormatPanel(); }
    @Override public void resetFotuPanelHostCall() { activity.resetFotuPanel(); }
    @Override public void resetOpticsPanelHostCall() { activity.resetOpticsPanel(); }
    @Override public void resetViewPanelHostCall() { activity.resetViewPanel(); }
    @Override public android.widget.LinearLayout sectionHeadHostCall(java.lang.String a0, java.lang.Runnable a1) { return activity.sectionHead(a0, a1); }
    @Override public void showComposePageHostCall(int a0) { activity.showComposePage(a0); }
    @Override public void showFotuGroupHostCall(int a0) { activity.showFotuGroup(a0); }
    @Override public void showMeterPickerHostCall() { activity.showMeterPicker(); }
    @Override public void showPaperPickerHostCall() { activity.showPaperPicker(); }
    @Override public void showPrintGroupHostCall(int a0) { activity.showPrintGroup(a0); }
}