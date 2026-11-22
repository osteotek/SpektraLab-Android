package app.spektralab;

/** Delegating implementation of SettingsDialogsHost. */
final class SettingsDialogsHostImpl implements app.spektralab.ui.panels.SettingsDialogsHost {
    private final MainActivity activity;
    SettingsDialogsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public android.view.animation.Interpolator getEASE_OUT() { return activity.EASE_OUT; }
    @Override public java.lang.String[] getORIENT_IDS() { return activity.ORIENT_IDS; }
    @Override public java.lang.String[] getORIENT_NAMES() { return activity.ORIENT_NAMES; }
    @Override public void setORIENT_NAMES(java.lang.String[] value) { activity.ORIENT_NAMES = value; }
    @Override public java.lang.String getREPO() { return activity.REPO; }
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
    @Override public boolean getDarkTheme() { return activity.darkTheme; }
    @Override public void setDarkTheme(boolean value) { activity.darkTheme = value; }
    @Override public app.spektralab.MainActivity.HistogramView getHistogram() { return activity.histogram; }
    @Override public void setHistogram(app.spektralab.MainActivity.HistogramView value) { activity.histogram = value; }
    @Override public boolean getHistogramOn() { return activity.histogramOn; }
    @Override public void setHistogramOn(boolean value) { activity.histogramOn = value; }
    @Override public app.spektralab.DeviceProbe getProbe() { return activity.probe; }
    @Override public void setProbe(app.spektralab.DeviceProbe value) { activity.probe = value; }
    @Override public boolean getRawAutoBright() { return activity.rawAutoBright; }
    @Override public void setRawAutoBright(boolean value) { activity.rawAutoBright = value; }
    @Override public android.widget.LinearLayout getSettingsBody() { return activity.settingsBody; }
    @Override public void setSettingsBody(android.widget.LinearLayout value) { activity.settingsBody = value; }
    @Override public android.widget.TextView getSettingsDone() { return activity.settingsDone; }
    @Override public void setSettingsDone(android.widget.TextView value) { activity.settingsDone = value; }
    @Override public java.lang.String getSettingsFilledSection() { return activity.settingsFilledSection; }
    @Override public void setSettingsFilledSection(java.lang.String value) { activity.settingsFilledSection = value; }
    @Override public boolean getSettingsOpen() { return activity.settingsOpen; }
    @Override public void setSettingsOpen(boolean value) { activity.settingsOpen = value; }
    @Override public android.view.View getSettingsScreen() { return activity.settingsScreen; }
    @Override public void setSettingsScreen(android.view.View value) { activity.settingsScreen = value; }
    @Override public android.widget.ScrollView getSettingsScroll() { return activity.settingsScroll; }
    @Override public void setSettingsScroll(android.widget.ScrollView value) { activity.settingsScroll = value; }
    @Override public java.lang.String getSettingsSection() { return activity.settingsSection; }
    @Override public void setSettingsSection(java.lang.String value) { activity.settingsSection = value; }
    @Override public android.widget.TextView getSettingsTitle() { return activity.settingsTitle; }
    @Override public void setSettingsTitle(android.widget.TextView value) { activity.settingsTitle = value; }
    @Override public android.graphics.Typeface getTfBody() { return activity.tfBody; }
    @Override public void setTfBody(android.graphics.Typeface value) { activity.tfBody = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public android.widget.FrameLayout getUiRoot() { return activity.uiRoot; }
    @Override public void setUiRoot(android.widget.FrameLayout value) { activity.uiRoot = value; }
    @Override public void applyOrientationPrefHostCall() { activity.applyOrientationPref(); }
    @Override public boolean arrivalAnimHostCall() { return activity.arrivalAnim(); }
    @Override public java.lang.String buildLineHostCall() { return activity.buildLine(); }
    @Override public boolean copyToClipboardHostCall(java.lang.String a0, java.lang.String a1) { return activity.copyToClipboard(a0, a1); }
    @Override public java.lang.String debugSummaryLineHostCall() { return activity.debugSummaryLine(); }
    @Override public java.lang.String deviceReportLineHostCall() { return activity.deviceReportLine(); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public java.lang.String hdrDetailLineHostCall() { return activity.hdrDetailLine(); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public android.view.View licenceBlockHostCall(java.lang.String a0, java.lang.String a1) { return activity.licenceBlock(a0, a1); }
    @Override public void loadAlbumHostCall() { activity.loadAlbum(); }
    @Override public int maxImportPixelsHostCall(boolean a0) { return activity.maxImportPixels(a0); }
    @Override public int maxImportPixelsHostCall() { return activity.maxImportPixels(); }
    @Override public android.widget.TextView mixedLabelHostCall(java.lang.String a0, float a1, int a2) { return activity.mixedLabel(a0, a1, a2); }
    @Override public java.lang.CharSequence mixedTextHostCall(java.lang.String a0) { return activity.mixedText(a0); }
    @Override public void openRepoHostCall() { activity.openRepo(); }
    @Override public void openUrlHostCall(java.lang.String a0) { activity.openUrl(a0); }
    @Override public java.lang.String orientationPrefHostCall() { return activity.orientationPref(); }
    @Override public java.lang.String orientationValueHostCall() { return activity.orientationValue(); }
    @Override public java.lang.String presetCountLineHostCall() { return activity.presetCountLine(); }
    @Override public app.spektralab.DeviceProbe probeNowHostCall() { return activity.probeNow(); }
    @Override public void refreshOriginalViewHostCall() { activity.refreshOriginalView(); }
    @Override public void relaunchForThemeHostCall() { activity.relaunchForTheme(); }
    @Override public void setArrivalAnimHostCall(boolean a0) { activity.setArrivalAnim(a0); }
    @Override public android.widget.LinearLayout settingsCheckRowHostCall(java.lang.String a0, java.lang.String a1, boolean a2, java.lang.Runnable a3) { return activity.settingsCheckRow(a0, a1, a2, a3); }
    @Override public void settingsInfoHostCall(java.lang.String a0, java.lang.String a1) { activity.settingsInfo(a0, a1); }
    @Override public android.graphics.drawable.Drawable settingsPressHostCall() { return activity.settingsPress(); }
    @Override public android.widget.LinearLayout settingsRowHostCall(java.lang.String a0, java.lang.String a1, java.lang.String a2, boolean a3, java.lang.Runnable a4) { return activity.settingsRow(a0, a1, a2, a3, a4); }
    @Override public android.widget.LinearLayout settingsSwitchHostCall(java.lang.String a0, java.lang.String a1, boolean a2, java.util.function.Consumer<java.lang.Boolean> a3) { return activity.settingsSwitch(a0, a1, a2, a3); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3) { activity.showActionSheet(a0, a1, a2, a3); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4, java.lang.String a5) { activity.showActionSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4) { activity.showActionSheet(a0, a1, a2, a3, a4); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.lang.String a2, java.util.function.Consumer<java.lang.String> a3) { activity.showChoiceSheet(a0, a1, a2, a3); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.util.List<java.lang.String> a2, java.lang.String a3, java.util.function.IntPredicate a4, java.util.function.Consumer<java.lang.String> a5) { activity.showChoiceSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showFeedbackGroupHostCall(boolean a0) { activity.showFeedbackGroup(a0); }
    @Override public void showHdrSheetHostCall() { activity.showHdrSheet(); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void showPixelCapSheetHostCall() { activity.showPixelCapSheet(); }
    @Override public void showPresetsPanelHostCall() { activity.showPresetsPanel(); }
    @Override public void syncRoomBarsHostCall() { activity.syncRoomBars(); }
    @Override public java.lang.String themeValueHostCall() { return activity.themeValue(); }
    @Override public void uploadRecentStepsHostCall() { activity.uploadRecentSteps(); }
    @Override public void checkForUpdatesHostCall(boolean show) { app.spektralab.UpdateCheck.check(activity, show); }
    @Override public void recreateHostCall() { activity.recreate(); }
    @Override public android.content.Context context() { return activity; }
}
