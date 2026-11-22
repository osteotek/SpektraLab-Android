package app.spektralab;
import app.spektralab.ui.Square;

/** Delegating implementation of PrintEdgeHost. */
final class PrintEdgeHostImpl implements app.spektralab.ui.panels.PrintEdgeHost {
    private final MainActivity activity;
    PrintEdgeHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.lang.String[] getCARRIER_IDS() { return activity.CARRIER_IDS; }
    @Override public java.lang.String[] getCARRIER_LABELS() { return activity.CARRIER_LABELS; }
    @Override public void setCARRIER_LABELS(java.lang.String[] value) { activity.CARRIER_LABELS = value; }
    @Override public java.lang.String[] getCORNER_IDS() { return activity.CORNER_IDS; }
    @Override public java.lang.String[] getCORNER_LABELS() { return activity.CORNER_LABELS; }
    @Override public void setCORNER_LABELS(java.lang.String[] value) { activity.CORNER_LABELS = value; }
    @Override public float[] getEDGE_MM() { return activity.EDGE_MM; }
    @Override public double[] getEDGE_RATIOS() { return activity.EDGE_RATIOS; }
    @Override public java.lang.String[] getFILTER_IDS() { return activity.FILTER_IDS; }
    @Override public java.lang.String[] getFILTER_LABELS() { return activity.FILTER_LABELS; }
    @Override public void setFILTER_LABELS(java.lang.String[] value) { activity.FILTER_LABELS = value; }
    @Override public java.lang.String[] getFOTU_GROUPS() { return activity.FOTU_GROUPS; }
    @Override public void setFOTU_GROUPS(java.lang.String[] value) { activity.FOTU_GROUPS = value; }
    @Override public java.lang.String[] getGATE_IDS() { return activity.GATE_IDS; }
    @Override public java.lang.String[] getGATE_LABELS() { return activity.GATE_LABELS; }
    @Override public void setGATE_LABELS(java.lang.String[] value) { activity.GATE_LABELS = value; }
    @Override public java.lang.String[] getHOLES_IDS() { return activity.HOLES_IDS; }
    @Override public java.lang.String[] getHOLES_LABELS() { return activity.HOLES_LABELS; }
    @Override public void setHOLES_LABELS(java.lang.String[] value) { activity.HOLES_LABELS = value; }
    @Override public java.lang.String[] getLATITUDE_LABELS() { return activity.LATITUDE_LABELS; }
    @Override public void setLATITUDE_LABELS(java.lang.String[] value) { activity.LATITUDE_LABELS = value; }
    @Override public java.lang.String[] getMODE_IDS() { return activity.MODE_IDS; }
    @Override public java.lang.String[] getMODE_LABELS() { return activity.MODE_LABELS; }
    @Override public void setMODE_LABELS(java.lang.String[] value) { activity.MODE_LABELS = value; }
    @Override public java.lang.String[] getNORM_IDS() { return activity.NORM_IDS; }
    @Override public java.lang.String[] getNORM_LABELS() { return activity.NORM_LABELS; }
    @Override public void setNORM_LABELS(java.lang.String[] value) { activity.NORM_LABELS = value; }
    @Override public java.lang.String[] getPLACE_IDS() { return activity.PLACE_IDS; }
    @Override public java.lang.String[] getPLACE_LABELS() { return activity.PLACE_LABELS; }
    @Override public void setPLACE_LABELS(java.lang.String[] value) { activity.PLACE_LABELS = value; }
    @Override public java.lang.String[] getSPACES() { return activity.SPACES; }
    @Override public java.lang.String[] getSTYLE_IDS() { return activity.STYLE_IDS; }
    @Override public java.lang.String[] getSTYLE_LABELS() { return activity.STYLE_LABELS; }
    @Override public void setSTYLE_LABELS(java.lang.String[] value) { activity.STYLE_LABELS = value; }
    @Override public int getUI_BLUE() { return activity.UI_BLUE; }
    @Override public void setUI_BLUE(int value) { activity.UI_BLUE = value; }
    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_MUTED() { return activity.UI_MUTED; }
    @Override public void setUI_MUTED(int value) { activity.UI_MUTED = value; }
    @Override public java.lang.String getCameraFilter() { return activity.cameraFilter; }
    @Override public void setCameraFilter(java.lang.String value) { activity.cameraFilter = value; }
    @Override public int getCameraSeed() { return activity.cameraSeed; }
    @Override public void setCameraSeed(int value) { activity.cameraSeed = value; }
    @Override public android.widget.LinearLayout getControls() { return activity.controls; }
    @Override public void setControls(android.widget.LinearLayout value) { activity.controls = value; }
    @Override public java.lang.String getDataText() { return activity.dataText; }
    @Override public void setDataText(java.lang.String value) { activity.dataText = value; }
    @Override public java.lang.String getDateCorner() { return activity.dateCorner; }
    @Override public void setDateCorner(java.lang.String value) { activity.dateCorner = value; }
    @Override public float getDateEv() { return activity.dateEv; }
    @Override public void setDateEv(float value) { activity.dateEv = value; }
    @Override public android.widget.SeekBar getDateEvBar() { return activity.dateEvBar; }
    @Override public void setDateEvBar(android.widget.SeekBar value) { activity.dateEvBar = value; }
    @Override public android.widget.LinearLayout getDateInner() { return activity.dateInner; }
    @Override public void setDateInner(android.widget.LinearLayout value) { activity.dateInner = value; }
    @Override public float getDateInsetX() { return activity.dateInsetX; }
    @Override public void setDateInsetX(float value) { activity.dateInsetX = value; }
    @Override public android.widget.SeekBar getDateInsetXBar() { return activity.dateInsetXBar; }
    @Override public void setDateInsetXBar(android.widget.SeekBar value) { activity.dateInsetXBar = value; }
    @Override public float getDateInsetY() { return activity.dateInsetY; }
    @Override public void setDateInsetY(float value) { activity.dateInsetY = value; }
    @Override public android.widget.SeekBar getDateInsetYBar() { return activity.dateInsetYBar; }
    @Override public void setDateInsetYBar(android.widget.SeekBar value) { activity.dateInsetYBar = value; }
    @Override public Square getDateMark() { return activity.dateMark; }
    @Override public void setDateMark(Square value) { activity.dateMark = value; }
    @Override public boolean getDateOn() { return activity.dateOn; }
    @Override public void setDateOn(boolean value) { activity.dateOn = value; }
    @Override public android.widget.LinearLayout getDatePage() { return activity.datePage; }
    @Override public void setDatePage(android.widget.LinearLayout value) { activity.datePage = value; }
    @Override public java.lang.String getDatePlacement() { return activity.datePlacement; }
    @Override public void setDatePlacement(java.lang.String value) { activity.datePlacement = value; }
    @Override public android.view.View getDateRow() { return activity.dateRow; }
    @Override public void setDateRow(android.view.View value) { activity.dateRow = value; }
    @Override public float getDateSize() { return activity.dateSize; }
    @Override public void setDateSize(float value) { activity.dateSize = value; }
    @Override public android.widget.SeekBar getDateSizeBar() { return activity.dateSizeBar; }
    @Override public void setDateSizeBar(android.widget.SeekBar value) { activity.dateSizeBar = value; }
    @Override public java.lang.String getDateStyle() { return activity.dateStyle; }
    @Override public void setDateStyle(java.lang.String value) { activity.dateStyle = value; }
    @Override public java.lang.String getDateText() { return activity.dateText; }
    @Override public void setDateText(java.lang.String value) { activity.dateText = value; }
    @Override public java.lang.String getDateTextB() { return activity.dateTextB; }
    @Override public void setDateTextB(java.lang.String value) { activity.dateTextB = value; }
    @Override public java.lang.String getEdgeCarrier() { return activity.edgeCarrier; }
    @Override public void setEdgeCarrier(java.lang.String value) { activity.edgeCarrier = value; }
    @Override public android.widget.TextView getEdgeFNumValue() { return activity.edgeFNumValue; }
    @Override public void setEdgeFNumValue(android.widget.TextView value) { activity.edgeFNumValue = value; }
    @Override public float getEdgeFNumber() { return activity.edgeFNumber; }
    @Override public void setEdgeFNumber(float value) { activity.edgeFNumber = value; }
    @Override public float getEdgeFog() { return activity.edgeFog; }
    @Override public void setEdgeFog(float value) { activity.edgeFog = value; }
    @Override public android.widget.SeekBar getEdgeFogBar() { return activity.edgeFogBar; }
    @Override public void setEdgeFogBar(android.widget.SeekBar value) { activity.edgeFogBar = value; }
    @Override public android.widget.TextView getEdgeFotuNote() { return activity.edgeFotuNote; }
    @Override public void setEdgeFotuNote(android.widget.TextView value) { activity.edgeFotuNote = value; }
    @Override public java.lang.String getEdgeGate() { return activity.edgeGate; }
    @Override public void setEdgeGate(java.lang.String value) { activity.edgeGate = value; }
    @Override public java.lang.String getEdgeHoles() { return activity.edgeHoles; }
    @Override public void setEdgeHoles(java.lang.String value) { activity.edgeHoles = value; }
    @Override public java.util.List<java.lang.String> getEdgeIds() { return activity.edgeIds; }
    @Override public android.widget.LinearLayout getEdgeInner() { return activity.edgeInner; }
    @Override public void setEdgeInner(android.widget.LinearLayout value) { activity.edgeInner = value; }
    @Override public java.util.List<java.lang.String> getEdgeLabels() { return activity.edgeLabels; }
    @Override public float getEdgeLeaks() { return activity.edgeLeaks; }
    @Override public void setEdgeLeaks(float value) { activity.edgeLeaks = value; }
    @Override public android.widget.SeekBar getEdgeLeaksBar() { return activity.edgeLeaksBar; }
    @Override public void setEdgeLeaksBar(android.widget.SeekBar value) { activity.edgeLeaksBar = value; }
    @Override public java.lang.String getEdgeMode() { return activity.edgeMode; }
    @Override public void setEdgeMode(java.lang.String value) { activity.edgeMode = value; }
    @Override public android.widget.LinearLayout getEdgePage() { return activity.edgePage; }
    @Override public void setEdgePage(android.widget.LinearLayout value) { activity.edgePage = value; }
    @Override public android.widget.LinearLayout getEdgeRoot() { return activity.edgeRoot; }
    @Override public void setEdgeRoot(android.widget.LinearLayout value) { activity.edgeRoot = value; }
    @Override public android.widget.LinearLayout getEdgeShapeRow() { return activity.edgeShapeRow; }
    @Override public void setEdgeShapeRow(android.widget.LinearLayout value) { activity.edgeShapeRow = value; }
    @Override public Square getEdgeTextMark() { return activity.edgeTextMark; }
    @Override public void setEdgeTextMark(Square value) { activity.edgeTextMark = value; }
    @Override public boolean getEdgeTextOn() { return activity.edgeTextOn; }
    @Override public void setEdgeTextOn(boolean value) { activity.edgeTextOn = value; }
    @Override public android.widget.TextView getEdgeValue() { return activity.edgeValue; }
    @Override public void setEdgeValue(android.widget.TextView value) { activity.edgeValue = value; }
    @Override public Square getEdrMark() { return activity.edrMark; }
    @Override public void setEdrMark(Square value) { activity.edrMark = value; }
    @Override public float getEdrOn() { return activity.edrOn; }
    @Override public void setEdrOn(float value) { activity.edrOn = value; }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public float getFilmFormatMm() { return activity.filmFormatMm; }
    @Override public void setFilmFormatMm(float value) { activity.filmFormatMm = value; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public int getFormatIndex() { return activity.formatIndex; }
    @Override public void setFormatIndex(int value) { activity.formatIndex = value; }
    @Override public android.widget.SeekBar getFrameNoBar() { return activity.frameNoBar; }
    @Override public void setFrameNoBar(android.widget.SeekBar value) { activity.frameNoBar = value; }
    @Override public float getFrameNumber() { return activity.frameNumber; }
    @Override public void setFrameNumber(float value) { activity.frameNumber = value; }
    @Override public int getFrameSeed() { return activity.frameSeed; }
    @Override public void setFrameSeed(int value) { activity.frameSeed = value; }
    @Override public float getHueCorrection() { return activity.hueCorrection; }
    @Override public void setHueCorrection(float value) { activity.hueCorrection = value; }
    @Override public Square getHueOwnMark() { return activity.hueOwnMark; }
    @Override public void setHueOwnMark(Square value) { activity.hueOwnMark = value; }
    @Override public java.util.List<android.view.View> getLatGated() { return activity.latGated; }
    @Override public java.lang.String getOverscanFormat() { return activity.overscanFormat; }
    @Override public void setOverscanFormat(java.lang.String value) { activity.overscanFormat = value; }
    @Override public Square getOverscanMark() { return activity.overscanMark; }
    @Override public void setOverscanMark(Square value) { activity.overscanMark = value; }
    @Override public boolean getOverscanOn() { return activity.overscanOn; }
    @Override public void setOverscanOn(boolean value) { activity.overscanOn = value; }
    @Override public boolean getOverscanPair() { return activity.overscanPair; }
    @Override public void setOverscanPair(boolean value) { activity.overscanPair = value; }
    @Override public android.view.View getOverscanRow() { return activity.overscanRow; }
    @Override public void setOverscanRow(android.view.View value) { activity.overscanRow = value; }
    @Override public android.widget.LinearLayout getPairBox() { return activity.pairBox; }
    @Override public void setPairBox(android.widget.LinearLayout value) { activity.pairBox = value; }
    @Override public float getPairHighKnee() { return activity.pairHighKnee; }
    @Override public void setPairHighKnee(float value) { activity.pairHighKnee = value; }
    @Override public android.widget.SeekBar getPairHighKneeBar() { return activity.pairHighKneeBar; }
    @Override public void setPairHighKneeBar(android.widget.SeekBar value) { activity.pairHighKneeBar = value; }
    @Override public float getPairHighRoom() { return activity.pairHighRoom; }
    @Override public void setPairHighRoom(float value) { activity.pairHighRoom = value; }
    @Override public android.widget.SeekBar getPairHighRoomBar() { return activity.pairHighRoomBar; }
    @Override public void setPairHighRoomBar(android.widget.SeekBar value) { activity.pairHighRoomBar = value; }
    @Override public Square getPairMark() { return activity.pairMark; }
    @Override public void setPairMark(Square value) { activity.pairMark = value; }
    @Override public android.view.View getPairRow() { return activity.pairRow; }
    @Override public void setPairRow(android.view.View value) { activity.pairRow = value; }
    @Override public float getPairShadowKnee() { return activity.pairShadowKnee; }
    @Override public void setPairShadowKnee(float value) { activity.pairShadowKnee = value; }
    @Override public android.widget.SeekBar getPairShadowKneeBar() { return activity.pairShadowKneeBar; }
    @Override public void setPairShadowKneeBar(android.widget.SeekBar value) { activity.pairShadowKneeBar = value; }
    @Override public float getPairShadowRoom() { return activity.pairShadowRoom; }
    @Override public void setPairShadowRoom(float value) { activity.pairShadowRoom = value; }
    @Override public android.widget.SeekBar getPairShadowRoomBar() { return activity.pairShadowRoomBar; }
    @Override public void setPairShadowRoomBar(android.widget.SeekBar value) { activity.pairShadowRoomBar = value; }
    @Override public float getPairSplit() { return activity.pairSplit; }
    @Override public void setPairSplit(float value) { activity.pairSplit = value; }
    @Override public android.widget.SeekBar getPairSplitBar() { return activity.pairSplitBar; }
    @Override public void setPairSplitBar(android.widget.SeekBar value) { activity.pairSplitBar = value; }
    @Override public boolean getPairTone() { return activity.pairTone; }
    @Override public void setPairTone(boolean value) { activity.pairTone = value; }
    @Override public Square getPairToneMark() { return activity.pairToneMark; }
    @Override public void setPairToneMark(Square value) { activity.pairToneMark = value; }
    @Override public float getPfFx() { return activity.pfFx; }
    @Override public void setPfFx(float value) { activity.pfFx = value; }
    @Override public android.widget.SeekBar getPfFxBar() { return activity.pfFxBar; }
    @Override public void setPfFxBar(android.widget.SeekBar value) { activity.pfFxBar = value; }
    @Override public float getPfFy() { return activity.pfFy; }
    @Override public void setPfFy(float value) { activity.pfFy = value; }
    @Override public android.widget.SeekBar getPfFyBar() { return activity.pfFyBar; }
    @Override public void setPfFyBar(android.widget.SeekBar value) { activity.pfFyBar = value; }
    @Override public float getPfMargin() { return activity.pfMargin; }
    @Override public void setPfMargin(float value) { activity.pfMargin = value; }
    @Override public android.widget.SeekBar getPfMarginBar() { return activity.pfMarginBar; }
    @Override public void setPfMarginBar(android.widget.SeekBar value) { activity.pfMarginBar = value; }
    @Override public android.widget.LinearLayout getPfPage() { return activity.pfPage; }
    @Override public void setPfPage(android.widget.LinearLayout value) { activity.pfPage = value; }
    @Override public android.widget.TextView getPfValue() { return activity.pfValue; }
    @Override public void setPfValue(android.widget.TextView value) { activity.pfValue = value; }
    @Override public java.lang.String getPreviewSpace() { return activity.previewSpace; }
    @Override public void setPreviewSpace(java.lang.String value) { activity.previewSpace = value; }
    @Override public android.widget.LinearLayout getPrintBody() { return activity.printBody; }
    @Override public void setPrintBody(android.widget.LinearLayout value) { activity.printBody = value; }
    @Override public int getPrintGroup() { return activity.printGroup; }
    @Override public void setPrintGroup(int value) { activity.printGroup = value; }
    @Override public int getPrintLatitude() { return activity.printLatitude; }
    @Override public void setPrintLatitude(int value) { activity.printLatitude = value; }
    @Override public java.util.List<java.lang.Runnable> getPrintRefreshers() { return activity.printRefreshers; }
    @Override public java.lang.String getSceneNorm() { return activity.sceneNorm; }
    @Override public void setSceneNorm(java.lang.String value) { activity.sceneNorm = value; }
    @Override public app.spektralab.engine.SpektraLabEngine.Session getSession() { return activity.session; }
    @Override public void setSession(app.spektralab.engine.SpektraLabEngine.Session value) { activity.session = value; }
    @Override public boolean getShowingOriginal() { return activity.showingOriginal; }
    @Override public void setShowingOriginal(boolean value) { activity.showingOriginal = value; }
    @Override public Square getSpotMark() { return activity.spotMark; }
    @Override public void setSpotMark(Square value) { activity.spotMark = value; }
    @Override public boolean getSpotOn() { return activity.spotOn; }
    @Override public void setSpotOn(boolean value) { activity.spotOn = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public boolean getUiReady() { return activity.uiReady; }
    @Override public void setUiReady(boolean value) { activity.uiReady = value; }
    @Override public android.widget.TextView addChoiceHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, java.util.function.Consumer<android.widget.TextView> a3) { return activity.addChoice(a0, a1, a2, a3); }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2) { activity.animateHeightTo(a0, a1, a2); }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2, int a3, java.lang.Runnable a4) { activity.animateHeightTo(a0, a1, a2, a3, a4); }
    @Override public void applyGateHostCall(java.lang.String a0) { activity.applyGate(a0); }
    @Override public void applyParamsHostCall() { activity.applyParams(); }
    @Override public boolean applyParamsHostCall(java.lang.String a0, int a1) { return activity.applyParams(a0, a1); }
    @Override public boolean applyParamsHostCall(java.lang.String a0, java.lang.String a1, int a2) { return activity.applyParams(a0, a1, a2); }
    @Override public void applyParamsHostCall(int a0) { activity.applyParams(a0); }
    @Override public void askTextHostCall(java.lang.String a0, java.lang.String a1, java.util.function.Consumer<java.lang.String> a2) { activity.askText(a0, a1, a2); }
    @Override public void askTextHostCall(java.lang.String a0, java.lang.String a1, boolean a2, java.util.function.Consumer<java.lang.String> a3) { activity.askText(a0, a1, a2, a3); }
    @Override public android.view.View bangForHostCall(java.util.function.Supplier<java.lang.String> a0) { return activity.bangFor(a0); }
    @Override public android.view.View cappedScrollHostCall(android.view.View a0, int a1) { return activity.cappedScroll(a0, a1); }
    @Override public android.widget.TextView choiceCellHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, java.util.function.Consumer<android.widget.TextView> a3) { return activity.choiceCell(a0, a1, a2, a3); }
    @Override public android.widget.LinearLayout choiceRowHostCall(java.lang.String a0, java.lang.String a1, java.util.function.Consumer<android.widget.TextView> a2) { return activity.choiceRow(a0, a1, a2); }
    @Override public void componentArrivalHostCall(android.view.View a0) { activity.componentArrival(a0); }
    @Override public float currentAspectHostCall() { return activity.currentAspect(); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public java.lang.String fNumLabelViaHost(float a0) { return activity.fNumLabel(a0); }
    @Override public int gateIndexOfViaHost(java.lang.String a0) { return activity.gateIndexOf(a0); }
    @Override public void gateLatitudeHostCall() { activity.gateLatitude(); }
    @Override public boolean hueFixUnsupportedViaHost(java.lang.String a0) { return activity.hueFixUnsupported(a0); }
    @Override public java.lang.String imprintDateHostCall() { return activity.imprintDate(); }
    @Override public boolean isFotuStockViaHost(java.lang.String a0) { return activity.isFotuStock(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public java.lang.String labelOfViaHost(java.lang.String[] a0, java.lang.String[] a1, java.lang.String a2) { return activity.labelOf(a0, a1, a2); }
    @Override public int naturalHeightViaHost(android.view.View a0, int a1) { return activity.naturalHeight(a0, a1); }
    @Override public void onEdgeChangedHostCall() { activity.onEdgeChanged(); }
    @Override public android.view.View pageHeadHostCall(java.lang.String a0) { return activity.pageHead(a0); }
    @Override public java.lang.String pfLabelHostCall() { return activity.pfLabel(); }
    @Override public java.util.List<android.view.View> printRowHostCall(java.lang.String a0) { return activity.printRow(a0); }
    @Override public android.widget.SeekBar rangeRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, float a2, float a3, float a4, java.util.function.Consumer<java.lang.Float> a5) { return activity.rangeRow(a0, a1, a2, a3, a4, a5); }
    @Override public android.widget.SeekBar rangeRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, float a3, float a4, float a5, java.util.function.Consumer<java.lang.Float> a6) { return activity.rangeRow(a0, a1, a2, a3, a4, a5, a6); }
    @Override public android.widget.SeekBar rangeRowHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, float a3, float a4, float a5, float a6, java.util.function.Consumer<java.lang.Float> a7) { return activity.rangeRow(a0, a1, a2, a3, a4, a5, a6, a7); }
    @Override public void refreshEdgeGroupsHostCall() { activity.refreshEdgeGroups(); }
    @Override public void refreshHueOwnMarkHostCall() { activity.refreshHueOwnMark(); }
    @Override public void refreshKnobHostCall() { activity.refreshKnob(); }
    @Override public void refreshLatitudeHostCall() { activity.refreshLatitude(); }
    @Override public void refreshPairRowsHostCall() { activity.refreshPairRows(); }
    @Override public void refreshPfRowsHostCall() { activity.refreshPfRows(); }
    @Override public void renderImageHostCall() { activity.renderImage(); }
    @Override public void renderImageHostCall(int a0) { activity.renderImage(a0); }
    @Override public void resolvePrintingHostCall() { activity.resolvePrinting(); }
    @Override public void setOriginalVisualHostCall(boolean a0) { activity.setOriginalVisual(a0); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3) { activity.showActionSheet(a0, a1, a2, a3); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4, java.lang.String a5) { activity.showActionSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4) { activity.showActionSheet(a0, a1, a2, a3, a4); }
    @Override public void showApertureSheetHostCall() { activity.showApertureSheet(); }
    @Override public void showChoiceMappingHostCall(java.lang.String a0, java.lang.String[] a1, java.lang.String[] a2, java.lang.String a3, java.util.function.Consumer<java.lang.String> a4) { activity.showChoiceMapping(a0, a1, a2, a3, a4); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.lang.String a2, java.util.function.Consumer<java.lang.String> a3) { activity.showChoiceSheet(a0, a1, a2, a3); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.util.List<java.lang.String> a2, java.lang.String a3, java.util.function.IntPredicate a4, java.util.function.Consumer<java.lang.String> a5) { activity.showChoiceSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showEdgePickerHostCall() { activity.showEdgePicker(); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void showPfPickerHostCall() { activity.showPfPicker(); }
    @Override public void syncSubRowActiveHostCall() { activity.syncSubRowActive(); }
    @Override public void toggleCellHostCall(android.widget.LinearLayout a0, java.lang.String a1, java.lang.String a2, Square a3, java.util.function.BooleanSupplier a4, java.util.function.Consumer<java.lang.Boolean> a5) { activity.toggleCell(a0, a1, a2, a3, a4, a5); }
    @Override public android.view.View toggleRowHostCall(java.lang.String a0, Square a1, java.util.function.BooleanSupplier a2, java.util.function.Consumer<java.lang.Boolean> a3) { return activity.toggleRow(a0, a1, a2, a3); }
    @Override public void turnByHostCall(int a0) { activity.turnBy(a0); }
    @Override public int uiToggleOffViaHost() { return activity.uiToggleOff(); }
    @Override public void runOnUiThreadHostCall(Runnable r) { activity.runOnUiThread(r); }
    @Override public android.content.Context context() { return activity; }
}
