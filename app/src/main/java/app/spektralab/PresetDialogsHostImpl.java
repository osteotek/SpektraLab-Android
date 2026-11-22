package app.spektralab;

/** Delegating implementation of PresetDialogsHost. */
final class PresetDialogsHostImpl implements app.spektralab.ui.panels.PresetDialogsHost {
    private final MainActivity activity;
    PresetDialogsHostImpl(MainActivity activity) { this.activity = activity; }

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
    @Override public java.util.List<app.spektralab.MainActivity.Frame> getFrames() { return activity.frames; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public void applyDesktopBaselineHostCall() { activity.applyDesktopBaseline(); }
    @Override public void applyPresetHostCall(org.json.JSONObject a0) { activity.applyPreset(a0); }
    @Override public android.view.View cappedScrollHostCall(android.view.View a0, int a1) { return activity.cappedScroll(a0, a1); }
    @Override public app.spektralab.MainActivity.Look copyLookHostCall(app.spektralab.MainActivity.Look a0) { return activity.copyLook(a0); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public void importPresetFromClipboardHostCall() { activity.importPresetFromClipboard(); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public org.json.JSONArray presetListHostCall() { return activity.presetList(); }
    @Override public void rebuildFrameChipsHostCall() { activity.rebuildFrameChips(); }
    @Override public void removeFrameHostCall(int a0) { activity.removeFrame(a0); }
    @Override public void saveCurrentPresetHostCall() { activity.saveCurrentPreset(); }
    @Override public void saveLookIntoFramesHostCall() { activity.saveLookIntoFrames(); }
    @Override public void saveSessionStateHostCall() { activity.saveSessionState(); }
    @Override public void shareCurrentLookHostCall() { activity.shareCurrentLook(); }
    @Override public void sharePresetHostCall(org.json.JSONObject a0) { activity.sharePreset(a0); }
    @Override public void showBottomSheetHostCall(android.app.Dialog a0, android.view.View a1) { activity.showBottomSheet(a0, a1); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void showMultiChoiceSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.Consumer<java.util.List<java.lang.Integer>> a3) { activity.showMultiChoiceSheet(a0, a1, a2, a3); }
    @Override public void storePresetListHostCall(org.json.JSONArray a0) { activity.storePresetList(a0); }
    @Override public app.spektralab.ui.SheetDialogs.Host sheetHostHostCall() { return activity; }
    @Override public android.content.Context context() { return activity; }
}
