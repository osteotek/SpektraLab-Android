package app.spektralab;

/** Delegating implementation of LiveClipHost. */
final class LiveClipHostImpl implements app.spektralab.ui.panels.LiveClipHost {
    private final MainActivity activity;
    LiveClipHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public java.util.List<app.spektralab.MainActivity.Frame> getFrames() { return activity.frames; }
    @Override public long getLiveClipPtsUs() { return activity.liveClipPtsUs; }
    @Override public void setLiveClipPtsUs(long value) { activity.liveClipPtsUs = value; }
    @Override public java.lang.String getPrintStock() { return activity.printStock; }
    @Override public void setPrintStock(java.lang.String value) { activity.printStock = value; }
    @Override public app.spektralab.engine.SpektraLabEngine.Session getSession() { return activity.session; }
    @Override public void setSession(app.spektralab.engine.SpektraLabEngine.Session value) { activity.session = value; }
    @Override public android.widget.TextView getStatusText() { return activity.statusText; }
    @Override public void setStatusText(android.widget.TextView value) { activity.statusText = value; }
    @Override public boolean applyParamsToHostCall(app.spektralab.engine.SpektraLabEngine.Session a0, java.lang.String a1, java.lang.String a2, int a3, java.lang.String a4) { return activity.applyParamsTo(a0, a1, a2, a3, a4); }
    @Override public boolean applyParamsToHostCall(app.spektralab.engine.SpektraLabEngine.Session a0, java.lang.String a1, java.lang.String a2, int a3, java.lang.String a4, boolean a5) { return activity.applyParamsTo(a0, a1, a2, a3, a4, a5); }
    @Override public boolean drainVideoViaHost(app.spektralab.MainActivity.ClipWriter a0, android.media.MediaCodec.BufferInfo a1, boolean a2) { return activity.drainVideo(a0, a1, a2); }
    @Override public java.util.List<app.spektralab.MainActivity.Frame> framesHostCall() { return activity.frames(); }
    @Override public android.graphics.Bitmap liveTailFrameViaHost(android.graphics.Bitmap a0, int a1, int a2) { return activity.liveTailFrame(a0, a1, a2); }
    @Override public long nowMsViaHost() { return activity.nowMs(); }
    @Override public app.spektralab.MainActivity.ClipWriter openClipWriterHostCall(int a0, int a1, float a2, int[] a3, long a4, android.media.MediaExtractor a5, int a6, android.media.MediaCodec.BufferInfo a7) { return activity.openClipWriter(a0, a1, a2, a3, a4, a5, a6, a7); }
    @Override public void queueFrameViaHost(app.spektralab.MainActivity.ClipWriter a0, int[] a1, long a2, android.media.MediaCodec.BufferInfo a3) { activity.queueFrame(a0, a1, a2, a3); }
    @Override public void reportLiveHostCall(java.lang.String a0, java.lang.String a1) { activity.reportLive(a0, a1); }
    @Override public void runOnUiThreadHostCall(Runnable r) { activity.runOnUiThread(r); }
    @Override public android.content.Context context() { return activity; }
}
