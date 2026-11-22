package app.spektralab;

/** Delegating implementation of PickerSheetsHost. */
final class PickerSheetsHostImpl implements app.spektralab.ui.panels.PickerSheetsHost {
    private final MainActivity activity;
    PickerSheetsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public float[] getAPERTURE_STOPS() { return activity.APERTURE_STOPS; }
    @Override public java.lang.String[] getEDGE_IDS() { return activity.EDGE_IDS; }
    @Override public java.lang.String[] getEDGE_LABELS() { return activity.EDGE_LABELS; }
    @Override public void setEDGE_LABELS(java.lang.String[] value) { activity.EDGE_LABELS = value; }
    @Override public java.lang.String[] getMETER_IDS() { return activity.METER_IDS; }
    @Override public java.lang.String[] getMETER_NAMES() { return activity.METER_NAMES; }
    @Override public void setMETER_NAMES(java.lang.String[] value) { activity.METER_NAMES = value; }
    @Override public java.lang.String[] getMETER_NOTES() { return activity.METER_NOTES; }
    @Override public void setMETER_NOTES(java.lang.String[] value) { activity.METER_NOTES = value; }
    @Override public java.lang.String getPAPER_SCAN() { return activity.PAPER_SCAN; }
    @Override public java.lang.String[] getPF_IDS() { return activity.PF_IDS; }
    @Override public java.lang.String[] getPF_LABELS() { return activity.PF_LABELS; }
    @Override public void setPF_LABELS(java.lang.String[] value) { activity.PF_LABELS = value; }
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
    @Override public float getEdgeFNumber() { return activity.edgeFNumber; }
    @Override public void setEdgeFNumber(float value) { activity.edgeFNumber = value; }
    @Override public android.widget.TextView[] getEffectChips() { return activity.effectChips; }
    @Override public void setEffectChips(android.widget.TextView[] value) { activity.effectChips = value; }
    @Override public android.widget.LinearLayout[] getEffectPages() { return activity.effectPages; }
    @Override public void setEffectPages(android.widget.LinearLayout[] value) { activity.effectPages = value; }
    @Override public int getEffectTab() { return activity.effectTab; }
    @Override public void setEffectTab(int value) { activity.effectTab = value; }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public float getFotuPush() { return activity.fotuPush; }
    @Override public void setFotuPush(float value) { activity.fotuPush = value; }
    @Override public java.lang.String getFotuRoot() { return activity.fotuRoot; }
    @Override public void setFotuRoot(java.lang.String value) { activity.fotuRoot = value; }
    @Override public java.util.Map<java.lang.String, app.spektralab.engine.FotuFilm.Stock> getFotuStocks() { return activity.fotuStocks; }
    @Override public java.lang.String getMeterMethod() { return activity.meterMethod; }
    @Override public void setMeterMethod(java.lang.String value) { activity.meterMethod = value; }
    @Override public java.lang.String getOverscanFormat() { return activity.overscanFormat; }
    @Override public void setOverscanFormat(java.lang.String value) { activity.overscanFormat = value; }
    @Override public java.lang.String getPfKind() { return activity.pfKind; }
    @Override public void setPfKind(java.lang.String value) { activity.pfKind = value; }
    @Override public java.util.List<java.lang.String> getPrintIds() { return activity.printIds; }
    @Override public java.util.List<java.lang.String> getPrintLabels() { return activity.printLabels; }
    @Override public java.lang.String getPrintStock() { return activity.printStock; }
    @Override public void setPrintStock(java.lang.String value) { activity.printStock = value; }
    @Override public java.util.Set<java.lang.String> getPrinting() { return activity.printing; }
    @Override public boolean getSettingsOpen() { return activity.settingsOpen; }
    @Override public void setSettingsOpen(boolean value) { activity.settingsOpen = value; }
    @Override public boolean getShowingOriginal() { return activity.showingOriginal; }
    @Override public void setShowingOriginal(boolean value) { activity.showingOriginal = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public boolean getUiReady() { return activity.uiReady; }
    @Override public void setUiReady(boolean value) { activity.uiReady = value; }
    @Override public void applyGateHostCall(java.lang.String a0) { activity.applyGate(a0); }
    @Override public void askApertureHostCall() { activity.askAperture(); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public void ensureFotuRootAsyncHostCall(java.lang.Runnable a0) { activity.ensureFotuRootAsync(a0); }
    @Override public java.lang.String fNumLabelViaHost(float a0) { return activity.fNumLabel(a0); }
    @Override public void fillSettingsHostCall() { activity.fillSettings(); }
    @Override public java.lang.String fotuMediumHostCall() { return activity.fotuMedium(); }
    @Override public java.lang.String fotuMediumNoteViaHost(java.lang.String a0) { return activity.fotuMediumNote(a0); }
    @Override public double gateRatioAtHostCall(int a0) { return activity.gateRatioAt(a0); }
    @Override public java.lang.String gateRatioLabelHostCall(int a0) { return activity.gateRatioLabel(a0); }
    @Override public boolean hdrDisplaySupportedHostCall() { return activity.hdrDisplaySupported(); }
    @Override public float hdrHeadroomStopsHostCall() { return activity.hdrHeadroomStops(); }
    @Override public boolean isFotuStockViaHost(java.lang.String a0) { return activity.isFotuStock(a0); }
    @Override public boolean isMonochromeStockViaHost(java.lang.String a0) { return activity.isMonochromeStock(a0); }
    @Override public boolean isPositiveStockHostCall(java.lang.String a0) { return activity.isPositiveStock(a0); }
    @Override public boolean isPrintFrameGaugeViaHost(java.lang.String a0) { return activity.isPrintFrameGauge(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public void onEdgeChangedHostCall() { activity.onEdgeChanged(); }
    @Override public void onStockChangedHostCall() { activity.onStockChanged(); }
    @Override public float originalAspectHostCall() { return activity.originalAspect(); }
    @Override public boolean paperFitsFilmViaHost(java.lang.String a0, boolean a1) { return activity.paperFitsFilm(a0, a1); }
    @Override public java.lang.String paperLineViaHost(java.lang.String a0, java.lang.String a1) { return activity.paperLine(a0, a1); }
    @Override public java.lang.String paperNoteViaHost(java.lang.String a0) { return activity.paperNote(a0); }
    @Override public java.lang.String pfLabelHostCall() { return activity.pfLabel(); }
    @Override public void refreshEdgeCardsHostCall() { activity.refreshEdgeCards(); }
    @Override public void refreshFNumValueHostCall() { activity.refreshFNumValue(); }
    @Override public void refreshFotuPushHostCall() { activity.refreshFotuPush(); }
    @Override public void refreshGlowVarsHostCall() { activity.refreshGlowVars(); }
    @Override public void refreshPaperLabelHostCall() { activity.refreshPaperLabel(); }
    @Override public void refreshPfRowsHostCall() { activity.refreshPfRows(); }
    @Override public void renderFotuHostCall() { activity.renderFotu(); }
    @Override public void setHdrFloorHostCall(float a0) { activity.setHdrFloor(a0); }
    @Override public void setHdrPreviewPrefHostCall(boolean a0) { activity.setHdrPreviewPref(a0); }
    @Override public void setMeterMethodHostCall(java.lang.String a0) { activity.setMeterMethod(a0); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3) { activity.showActionSheet(a0, a1, a2, a3); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4, java.lang.String a5) { activity.showActionSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4) { activity.showActionSheet(a0, a1, a2, a3, a4); }
    @Override public void showBottomSheetHostCall(android.app.Dialog a0, android.view.View a1) { activity.showBottomSheet(a0, a1); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.lang.String a2, java.util.function.Consumer<java.lang.String> a3) { activity.showChoiceSheet(a0, a1, a2, a3); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.util.List<java.lang.String> a2, java.lang.String a3, java.util.function.IntPredicate a4, java.util.function.Consumer<java.lang.String> a5) { activity.showChoiceSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void unlockGlowPageHostCall() { activity.unlockGlowPage(); }
    @Override public app.spektralab.ui.SheetDialogs.Host sheetHostHostCall() { return activity; }
    @Override public android.content.Context context() { return activity; }
}
