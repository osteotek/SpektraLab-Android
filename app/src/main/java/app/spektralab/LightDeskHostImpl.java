package app.spektralab;

/** Delegating implementation of LightDeskHost. */
final class LightDeskHostImpl implements app.spektralab.ui.LightDeskHost {
    private final MainActivity activity;
    LightDeskHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.util.List<MainActivity.AlbumEntry> getAlbum() { return activity.album; }
    @Override public java.util.LinkedHashMap<String, android.graphics.Bitmap> getAlbumPrints() { return activity.albumPrints; }
    @Override public java.util.HashMap<String, Long> getArrived() { return activity.arrived; }
    @Override public boolean getHdrWindow() { return activity.hdrWindow; }
    @Override public app.spektralab.ui.LightDesk getLightDesk() { return activity.lightDesk; }
    @Override public android.widget.TextView getLightGridHint() { return activity.lightGridHint; }
    @Override public int getLightIndex() { return activity.lightIndex; }
    @Override public void setLightIndex(int value) { activity.lightIndex = value; }
    @Override public android.view.View getLightTable() { return activity.lightTable; }
    @Override public android.view.View getLightZoomBar() { return activity.lightZoomBar; }
    @Override public java.util.HashSet<String> getSoftAsked() { return activity.softAsked; }
    @Override public java.util.LinkedHashMap<String, android.graphics.Bitmap[]> getSoftPrints() { return activity.softPrints; }
    @Override public android.graphics.Typeface getTfBody() { return MainActivity.tfBody; }
    @Override public long albumPrintBytes() { return activity.albumPrintBytes(); }
    @Override public String appTag() { return activity.appTag(); }
    @Override public long bitmapBytes(android.graphics.Bitmap b) { return MainActivity.bitmapBytes(b); }
    @Override public android.graphics.Bitmap[] defocused(android.graphics.Bitmap b) { return MainActivity.defocused(b); }
    @Override public boolean hdrPreviewPref() { return activity.hdrPreviewPref(); }
    @Override public void hideInfo() { activity.hideInfo(); }
    @Override public void markFile(String what) { MainActivity.markFile(what); }
    @Override public long nowMs() { return MainActivity.nowMs(); }
    @Override public void openFromTable(int index) { activity.openFromTable(index); }
    @Override public void requestPrint(String key) { activity.requestPrint(key); }
    @Override public void showInfoFor(int index) { activity.showInfoFor(index); }
    @Override public void showPrintActions(int index) { activity.showPrintActions(index); }
    @Override public long softPrintBytes() { return activity.softPrintBytes(); }
    @Override public void wallCrop(int w, int h, int[] out) { MainActivity.wallCrop(w, h, out); }
    @Override public void runOnUiThread(Runnable r) { activity.runOnUiThread(r); }
    @Override public void crossToEditor(android.view.View view) { activity.crossToEditor(view); }
}
