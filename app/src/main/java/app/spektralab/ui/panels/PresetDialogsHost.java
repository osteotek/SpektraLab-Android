package app.spektralab.ui.panels;

/** Narrow Activity surface for the PresetDialogs builders. Implemented by PresetDialogsHostImpl. */
public interface PresetDialogsHost {
    int getUI_BLUE();
    void setUI_BLUE(int value);
    int getUI_INK();
    void setUI_INK(int value);
    int getUI_LINE();
    void setUI_LINE(int value);
    int getUI_MUTED();
    void setUI_MUTED(int value);
    int getUI_PAPER();
    void setUI_PAPER(int value);
    boolean getBatchExportRunning();
    void setBatchExportRunning(boolean value);
    java.util.List<app.spektralab.MainActivity.Frame> getFrames();
    android.graphics.Typeface getTfHan();
    void setTfHan(android.graphics.Typeface value);
    void applyDesktopBaselineHostCall();
    void applyPresetHostCall(org.json.JSONObject a0);
    android.view.View cappedScrollHostCall(android.view.View a0, int a1);
    app.spektralab.MainActivity.Look copyLookHostCall(app.spektralab.MainActivity.Look a0);
    int dpHostCall(float a0);
    void importPresetFromClipboardHostCall();
    android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3);
    org.json.JSONArray presetListHostCall();
    void rebuildFrameChipsHostCall();
    void removeFrameHostCall(int a0);
    void saveCurrentPresetHostCall();
    void saveLookIntoFramesHostCall();
    void saveSessionStateHostCall();
    void shareCurrentLookHostCall();
    void sharePresetHostCall(org.json.JSONObject a0);
    void showBottomSheetHostCall(android.app.Dialog a0, android.view.View a1);
    void showInAppToastHostCall(java.lang.CharSequence a0);
    void showMultiChoiceSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.Consumer<java.util.List<java.lang.Integer>> a3);
    void storePresetListHostCall(org.json.JSONArray a0);
    app.spektralab.ui.SheetDialogs.Host sheetHostHostCall();
    android.content.Context context();
}
