package app.spektralab.ui;

/** Activity state the light-table canvas reads. */
public interface LightDeskHost {
    java.util.List<app.spektralab.MainActivity.AlbumEntry> getAlbum();
    java.util.LinkedHashMap<String, android.graphics.Bitmap> getAlbumPrints();
    java.util.HashMap<String, Long> getArrived();
    boolean getHdrWindow();
    LightDesk getLightDesk();
    android.widget.TextView getLightGridHint();
    int getLightIndex();
    void setLightIndex(int value);
    android.view.View getLightTable();
    android.view.View getLightZoomBar();
    java.util.HashSet<String> getSoftAsked();
    java.util.LinkedHashMap<String, android.graphics.Bitmap[]> getSoftPrints();
    android.graphics.Typeface getTfBody();
    long albumPrintBytes();
    String appTag();
    long bitmapBytes(android.graphics.Bitmap b);
    android.graphics.Bitmap[] defocused(android.graphics.Bitmap b);
    boolean hdrPreviewPref();
    void hideInfo();
    void markFile(String what);
    long nowMs();
    void openFromTable(int index);
    void requestPrint(String key);
    void showInfoFor(int index);
    void showPrintActions(int index);
    long softPrintBytes();
    void wallCrop(int w, int h, int[] out);
    void runOnUiThread(Runnable r);
    void crossToEditor(android.view.View view);
}
