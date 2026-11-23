package app.spektralab;

/** Delegating implementation of EditorChromeHost. */
final class EditorChromeHostImpl implements app.spektralab.ui.panels.EditorChromeHost {
    private final MainActivity activity;
    EditorChromeHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public android.view.animation.Interpolator getEASE_OUT() { return activity.EASE_OUT; }
    @Override public int getMS_OUT() { return activity.MS_OUT; }
    @Override public int getTAB_BAR_DP() { return activity.TAB_BAR_DP; }
    @Override public int getUI_LINE() { return activity.UI_LINE; }
    @Override public void setUI_LINE(int value) { activity.UI_LINE = value; }
    @Override public int getUI_PAPER() { return activity.UI_PAPER; }
    @Override public void setUI_PAPER(int value) { activity.UI_PAPER = value; }
    @Override public int getActiveTabIndex() { return activity.activeTabIndex; }
    @Override public void setActiveTabIndex(int value) { activity.activeTabIndex = value; }
    @Override public android.widget.FrameLayout getCanvasStage() { return activity.canvasStage; }
    @Override public void setCanvasStage(android.widget.FrameLayout value) { activity.canvasStage = value; }
    @Override public android.widget.LinearLayout getControls() { return activity.controls; }
    @Override public void setControls(android.widget.LinearLayout value) { activity.controls = value; }
    @Override public app.spektralab.ui.CurveView getCurveView() { return activity.curveView; }
    @Override public void setCurveView(app.spektralab.ui.CurveView value) { activity.curveView = value; }
    @Override public android.view.View getEditorRoot() { return activity.editorRoot; }
    @Override public void setEditorRoot(android.view.View value) { activity.editorRoot = value; }
    @Override public android.view.View getEditorSourceBar() { return activity.editorSourceBar; }
    @Override public void setEditorSourceBar(android.view.View value) { activity.editorSourceBar = value; }
    @Override public android.view.View getEditorTopBar() { return activity.editorTopBar; }
    @Override public void setEditorTopBar(android.view.View value) { activity.editorTopBar = value; }
    @Override public android.view.View getFilmPaperRow() { return activity.filmPaperRow; }
    @Override public void setFilmPaperRow(android.view.View value) { activity.filmPaperRow = value; }
    @Override public android.widget.TextView getHdrBadge() { return activity.hdrBadge; }
    @Override public void setHdrBadge(android.widget.TextView value) { activity.hdrBadge = value; }
    @Override public app.spektralab.ui.HistogramView getHistogram() { return activity.histogram; }
    @Override public void setHistogram(app.spektralab.ui.HistogramView value) { activity.histogram = value; }
    @Override public boolean getHistogramOn() { return activity.histogramOn; }
    @Override public void setHistogramOn(boolean value) { activity.histogramOn = value; }
    @Override public boolean getLandscapeMode() { return activity.landscapeMode; }
    @Override public void setLandscapeMode(boolean value) { activity.landscapeMode = value; }
    @Override public android.graphics.Bitmap getLastRenderedBitmap() { return activity.lastRenderedBitmap; }
    @Override public void setLastRenderedBitmap(android.graphics.Bitmap value) { activity.lastRenderedBitmap = value; }
    @Override public android.widget.LinearLayout getLeftColumn() { return activity.leftColumn; }
    @Override public void setLeftColumn(android.widget.LinearLayout value) { activity.leftColumn = value; }
    @Override public android.widget.TextView getLiveBadge() { return activity.liveBadge; }
    @Override public void setLiveBadge(android.widget.TextView value) { activity.liveBadge = value; }
    @Override public boolean getPanelCollapsed() { return activity.panelCollapsed; }
    @Override public void setPanelCollapsed(boolean value) { activity.panelCollapsed = value; }
    @Override public android.widget.LinearLayout getPanelHolder() { return activity.panelHolder; }
    @Override public void setPanelHolder(android.widget.LinearLayout value) { activity.panelHolder = value; }
    @Override public android.widget.ScrollView getPanelScroll() { return activity.panelScroll; }
    @Override public void setPanelScroll(android.widget.ScrollView value) { activity.panelScroll = value; }
    @Override public android.view.View[] getPanels() { return activity.panels; }
    @Override public android.widget.LinearLayout getPaperDock() { return activity.paperDock; }
    @Override public void setPaperDock(android.widget.LinearLayout value) { activity.paperDock = value; }
    @Override public boolean getRawAutoBright() { return activity.rawAutoBright; }
    @Override public void setRawAutoBright(boolean value) { activity.rawAutoBright = value; }
    @Override public boolean getRotateGridOn() { return activity.rotateGridOn; }
    @Override public void setRotateGridOn(boolean value) { activity.rotateGridOn = value; }
    @Override public android.widget.TextView getSharePill() { return activity.sharePill; }
    @Override public void setSharePill(android.widget.TextView value) { activity.sharePill = value; }
    @Override public boolean getShelfCollapsed() { return activity.shelfCollapsed; }
    @Override public void setShelfCollapsed(boolean value) { activity.shelfCollapsed = value; }
    @Override public android.widget.FrameLayout getSubBar() { return activity.subBar; }
    @Override public void setSubBar(android.widget.FrameLayout value) { activity.subBar = value; }
    @Override public android.widget.FrameLayout getTabsRoot() { return activity.tabsRoot; }
    @Override public void setTabsRoot(android.widget.FrameLayout value) { activity.tabsRoot = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfBold() { return activity.tfBold; }
    @Override public void setTfBold(android.graphics.Typeface value) { activity.tfBold = value; }
    @Override public android.graphics.Typeface getTfDisplay() { return activity.tfDisplay; }
    @Override public void setTfDisplay(android.graphics.Typeface value) { activity.tfDisplay = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public android.widget.ImageView getUndoPill() { return activity.undoPill; }
    @Override public void setUndoPill(android.widget.ImageView value) { activity.undoPill = value; }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2) { activity.animateHeightTo(a0, a1, a2); }
    @Override public void animateHeightToHostCall(android.view.View a0, int a1, int a2, int a3, java.lang.Runnable a4) { activity.animateHeightTo(a0, a1, a2, a3, a4); }
    @Override public void applyControlsHeightHostCall(boolean a0) { activity.applyControlsHeight(a0); }
    @Override public void applyEditorChromeHostCall() { activity.applyEditorChrome(); }
    @Override public void applyFilmShelfModeHostCall() { activity.applyFilmShelfMode(); }
    @Override public void cancelHeightHostCall(android.view.View a0) { activity.cancelHeight(a0); }
    @Override public void cancelLineHostCall(android.view.View a0) { activity.cancelLine(a0); }
    @Override public void componentArrivalHostCall(android.view.View a0) { activity.componentArrival(a0); }
    @Override public int controlsWidthPxHostCall() { return activity.controlsWidthPx(); }
    @Override public android.view.View currentPanelHostCall() { return activity.currentPanel(); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public android.view.View editorRuleHostCall() { return activity.editorRule(); }
    @Override public boolean isLandscapeNowHostCall() { return activity.isLandscapeNow(); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public void markFileViaHost(java.lang.String a0) { activity.markFile(a0); }
    @Override public void moveTabUnderlineHostCall(int a0, boolean a1) { activity.moveTabUnderline(a0, a1); }
    @Override public int naturalHeightViaHost(android.view.View a0, int a1) { return activity.naturalHeight(a0, a1); }
    @Override public void refreshSubBarHostCall() { activity.refreshSubBar(); }
    @Override public void setTabHostCall(int a0) { activity.setTab(a0); }
    @Override public void shareLastExportHostCall() { activity.shareLastExport(); }
    @Override public void showExportDialogHostCall() { activity.showExportDialog(); }
    @Override public void showHdrSheetHostCall() { activity.showHdrSheet(); }
    @Override public void showSettingsHostCall() { activity.showSettings(); }
    @Override public void showSettingsHostCall(java.lang.String a0) { activity.showSettings(a0); }
    @Override public void startLivePreviewHostCall() { activity.startLivePreview(); }
    @Override public int[] topWashHostCall() { return activity.topWash(); }
    @Override public int uiOnCanvasViaHost() { return activity.uiOnCanvas(); }
    @Override public void undoStepHostCall() { activity.undoStep(); }
    @Override public void updateShelfVisibilityHostCall() { activity.updateShelfVisibility(); }
    @Override public android.content.Context context() { return activity; }
}
