package app.spektralab.ui.panels;

/** Narrow Activity surface for the LiveClip builders. Implemented by LiveClipHostImpl. */
public interface LiveClipHost {
    app.spektralab.engine.SpektraLabEngine getEngine();
    void setEngine(app.spektralab.engine.SpektraLabEngine value);
    java.lang.String getFilmStock();
    void setFilmStock(java.lang.String value);
    java.util.List<app.spektralab.MainActivity.Frame> getFrames();
    long getLiveClipPtsUs();
    void setLiveClipPtsUs(long value);
    java.lang.String getPrintStock();
    void setPrintStock(java.lang.String value);
    app.spektralab.engine.SpektraLabEngine.Session getSession();
    void setSession(app.spektralab.engine.SpektraLabEngine.Session value);
    android.widget.TextView getStatusText();
    void setStatusText(android.widget.TextView value);
    boolean applyParamsToHostCall(app.spektralab.engine.SpektraLabEngine.Session a0, java.lang.String a1, java.lang.String a2, int a3, java.lang.String a4);
    boolean applyParamsToHostCall(app.spektralab.engine.SpektraLabEngine.Session a0, java.lang.String a1, java.lang.String a2, int a3, java.lang.String a4, boolean a5);
    boolean drainVideoViaHost(app.spektralab.MainActivity.ClipWriter a0, android.media.MediaCodec.BufferInfo a1, boolean a2);
    java.util.List<app.spektralab.MainActivity.Frame> framesHostCall();
    android.graphics.Bitmap liveTailFrameViaHost(android.graphics.Bitmap a0, int a1, int a2);
    long nowMsViaHost();
    app.spektralab.MainActivity.ClipWriter openClipWriterHostCall(int a0, int a1, float a2, int[] a3, long a4, android.media.MediaExtractor a5, int a6, android.media.MediaCodec.BufferInfo a7);
    void queueFrameViaHost(app.spektralab.MainActivity.ClipWriter a0, int[] a1, long a2, android.media.MediaCodec.BufferInfo a3);
    void reportLiveHostCall(java.lang.String a0, java.lang.String a1);
    void runOnUiThreadHostCall(Runnable r);
    android.content.Context context();
}
