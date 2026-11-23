package app.spektralab;

/** Delegating implementation of LightTableHost. */
final class LightTableHostImpl implements app.spektralab.ui.panels.LightTableHost {
    private final MainActivity activity;
    LightTableHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public float getLT_ARC_H() { return activity.LT_ARC_H; }
    @Override public int getLT_INK() { return activity.LT_INK; }
    @Override public int getLT_KLEIN() { return activity.LT_KLEIN; }
    @Override public int getLT_MUTED() { return activity.LT_MUTED; }
    @Override public int getLT_PAPER() { return activity.LT_PAPER; }
    @Override public int getLT_WALL() { return activity.LT_WALL; }
    @Override public java.lang.String[] getLT_ZOOM_NAMES() { return activity.LT_ZOOM_NAMES; }
    @Override public void setLT_ZOOM_NAMES(java.lang.String[] value) { activity.LT_ZOOM_NAMES = value; }
    @Override public int getNEG_AMBER() { return activity.NEG_AMBER; }
    @Override public float getNEG_SAFE_BOTTOM_DP() { return activity.NEG_SAFE_BOTTOM_DP; }
    @Override public float getNEG_SAFE_TOP_DP() { return activity.NEG_SAFE_TOP_DP; }
    @Override public java.util.List<app.spektralab.MainActivity.AlbumEntry> getAlbum() { return activity.album; }
    @Override public android.widget.LinearLayout getControls() { return activity.controls; }
    @Override public void setControls(android.widget.LinearLayout value) { activity.controls = value; }
    @Override public boolean getCrossing() { return activity.crossing; }
    @Override public void setCrossing(boolean value) { activity.crossing = value; }
    @Override public java.util.List<app.spektralab.MainActivity.Frame> getFrames() { return activity.frames; }
    @Override public app.spektralab.ui.LightBox getLightBoxBg() { return activity.lightBoxBg; }
    @Override public void setLightBoxBg(app.spektralab.ui.LightBox value) { activity.lightBoxBg = value; }
    @Override public app.spektralab.ui.LightDesk getLightDesk() { return activity.lightDesk; }
    @Override public void setLightDesk(app.spektralab.ui.LightDesk value) { activity.lightDesk = value; }
    @Override public android.widget.TextView getLightEmpty1() { return activity.lightEmpty1; }
    @Override public void setLightEmpty1(android.widget.TextView value) { activity.lightEmpty1 = value; }
    @Override public android.widget.TextView getLightEmpty2() { return activity.lightEmpty2; }
    @Override public void setLightEmpty2(android.widget.TextView value) { activity.lightEmpty2 = value; }
    @Override public android.view.View getLightEmptyBlock() { return activity.lightEmptyBlock; }
    @Override public void setLightEmptyBlock(android.view.View value) { activity.lightEmptyBlock = value; }
    @Override public android.widget.TextView getLightFilmValue() { return activity.lightFilmValue; }
    @Override public void setLightFilmValue(android.widget.TextView value) { activity.lightFilmValue = value; }
    @Override public android.view.View getLightGo() { return activity.lightGo; }
    @Override public void setLightGo(android.view.View value) { activity.lightGo = value; }
    @Override public android.widget.TextView getLightGridHint() { return activity.lightGridHint; }
    @Override public void setLightGridHint(android.widget.TextView value) { activity.lightGridHint = value; }
    @Override public int getLightIndex() { return activity.lightIndex; }
    @Override public void setLightIndex(int value) { activity.lightIndex = value; }
    @Override public android.widget.LinearLayout getLightInfo() { return activity.lightInfo; }
    @Override public void setLightInfo(android.widget.LinearLayout value) { activity.lightInfo = value; }
    @Override public android.widget.TextView getLightInfoFile() { return activity.lightInfoFile; }
    @Override public void setLightInfoFile(android.widget.TextView value) { activity.lightInfoFile = value; }
    @Override public android.widget.TextView getLightInfoMeta() { return activity.lightInfoMeta; }
    @Override public void setLightInfoMeta(android.widget.TextView value) { activity.lightInfoMeta = value; }
    @Override public android.widget.TextView getLightLoupe() { return activity.lightLoupe; }
    @Override public void setLightLoupe(android.widget.TextView value) { activity.lightLoupe = value; }
    @Override public android.widget.TextView getLightPaperValue() { return activity.lightPaperValue; }
    @Override public void setLightPaperValue(android.widget.TextView value) { activity.lightPaperValue = value; }
    @Override public app.spektralab.ui.RockerSwitch getLightRocker() { return activity.lightRocker; }
    @Override public void setLightRocker(app.spektralab.ui.RockerSwitch value) { activity.lightRocker = value; }
    @Override public android.view.View getLightTopRight() { return activity.lightTopRight; }
    @Override public void setLightTopRight(android.view.View value) { activity.lightTopRight = value; }
    @Override public android.view.View getLightZoomBar() { return activity.lightZoomBar; }
    @Override public void setLightZoomBar(android.view.View value) { activity.lightZoomBar = value; }
    @Override public android.widget.TextView getLightZoomBtn() { return activity.lightZoomBtn; }
    @Override public void setLightZoomBtn(android.widget.TextView value) { activity.lightZoomBtn = value; }
    @Override public app.spektralab.LightTableCanvas getLtWeb() { return activity.ltWeb; }
    @Override public void setLtWeb(app.spektralab.LightTableCanvas value) { activity.ltWeb = value; }
    @Override public android.widget.TextView getNegAdd() { return activity.negAdd; }
    @Override public void setNegAdd(android.widget.TextView value) { activity.negAdd = value; }
    @Override public android.widget.LinearLayout getNegBar() { return activity.negBar; }
    @Override public void setNegBar(android.widget.LinearLayout value) { activity.negBar = value; }
    @Override public android.graphics.drawable.GradientDrawable getNegBarBg() { return activity.negBarBg; }
    @Override public void setNegBarBg(android.graphics.drawable.GradientDrawable value) { activity.negBarBg = value; }
    @Override public app.spektralab.NegativeDesk getNegDesk() { return activity.negDesk; }
    @Override public void setNegDesk(app.spektralab.NegativeDesk value) { activity.negDesk = value; }
    @Override public android.widget.TextView getNegFold() { return activity.negFold; }
    @Override public void setNegFold(android.widget.TextView value) { activity.negFold = value; }
    @Override public float getNegLitT() { return activity.negLitT; }
    @Override public void setNegLitT(float value) { activity.negLitT = value; }
    @Override public boolean getNegMode() { return activity.negMode; }
    @Override public void setNegMode(boolean value) { activity.negMode = value; }
    @Override public android.widget.TextView getNegModeBtn() { return activity.negModeBtn; }
    @Override public void setNegModeBtn(android.widget.TextView value) { activity.negModeBtn = value; }
    @Override public android.widget.TextView getNegReopen() { return activity.negReopen; }
    @Override public void setNegReopen(android.widget.TextView value) { activity.negReopen = value; }
    @Override public android.graphics.drawable.GradientDrawable getNegReopenBg() { return activity.negReopenBg; }
    @Override public void setNegReopenBg(android.graphics.drawable.GradientDrawable value) { activity.negReopenBg = value; }
    @Override public android.widget.HorizontalScrollView getNegStrip() { return activity.negStrip; }
    @Override public void setNegStrip(android.widget.HorizontalScrollView value) { activity.negStrip = value; }
    @Override public boolean getNegStripOpen() { return activity.negStripOpen; }
    @Override public void setNegStripOpen(boolean value) { activity.negStripOpen = value; }
    @Override public android.widget.LinearLayout getNegStripRow() { return activity.negStripRow; }
    @Override public void setNegStripRow(android.widget.LinearLayout value) { activity.negStripRow = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public void applyLitHudHostCall() { activity.applyLitHud(); }
    @Override public void applyNegHudHostCall() { activity.applyNegHud(); }
    @Override public void crossToEditorHostCall(android.view.View a0) { activity.crossToEditor(a0); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public android.widget.TextView infoCardHostCall(android.widget.LinearLayout a0, java.lang.String a1, boolean a2) { return activity.infoCard(a0, a1, a2); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public android.graphics.drawable.GradientDrawable ltCardHostCall(int a0, int a1, int a2) { return activity.ltCard(a0, a1, a2); }
    @Override public void morphConsoleLabelHostCall(long a0) { activity.morphConsoleLabel(a0); }
    @Override public void raiseNegBarHostCall(long a0) { activity.raiseNegBar(a0); }
    @Override public void refreshLoupeButtonHostCall() { activity.refreshLoupeButton(); }
    @Override public void refreshZoomBarHostCall() { activity.refreshZoomBar(); }
    @Override public void requestNegSharpHostCall(java.lang.String a0, int a1) { activity.requestNegSharp(a0, a1); }
    @Override public void setNegStripOpenHostCall(boolean a0) { activity.setNegStripOpen(a0); }
    @Override public void showImportMenuHostCall() { activity.showImportMenu(); }
    @Override public void showNegativeAtHostCall(int a0) { activity.showNegativeAt(a0); }
    @Override public void showSettingsHostCall() { activity.showSettings(); }
    @Override public void showSettingsHostCall(java.lang.String a0) { activity.showSettings(a0); }
    @Override public void syncNegStripColorsHostCall() { activity.syncNegStripColors(); }
    @Override public void toggleLightsHostCall(android.view.View a0) { activity.toggleLights(a0); }
    @Override public android.view.animation.Interpolator xfEaseViaHost() { return activity.xfEase(); }
    @Override public android.content.Context context() { return activity; }
    @Override public app.spektralab.ui.LightDeskHost lightDeskHost() { return new LightDeskHostImpl(activity); }
}
