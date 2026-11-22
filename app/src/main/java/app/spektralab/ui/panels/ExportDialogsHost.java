package app.spektralab.ui.panels;

/** Narrow Activity surface for the ExportDialogs builders. Implemented by ExportDialogsHostImpl. */
public interface ExportDialogsHost {
    int getLIVE_CLIP_FULL_EDGE();
    int getLIVE_CLIP_MAX_EDGE();
    java.lang.String getPAPER_SCAN();
    boolean getBatchExportAbort();
    void setBatchExportAbort(boolean value);
    boolean getBatchExportRunning();
    void setBatchExportRunning(boolean value);
    app.spektralab.engine.SpektraLabEngine getEngine();
    void setEngine(app.spektralab.engine.SpektraLabEngine value);
    int getExportHeals();
    void setExportHeals(int value);
    java.lang.String getFilmStock();
    void setFilmStock(java.lang.String value);
    java.util.List<app.spektralab.MainActivity.Frame> getFrames();
    java.lang.String getPrintStock();
    void setPrintStock(java.lang.String value);
    app.spektralab.DeviceProbe getProbe();
    void setProbe(app.spektralab.DeviceProbe value);
    app.spektralab.engine.SpektraLabEngine.Session getSession();
    void setSession(app.spektralab.engine.SpektraLabEngine.Session value);
    int getSourceHeight();
    void setSourceHeight(int value);
    int getSourceWidth();
    void setSourceWidth(int value);
    long availableMemoryBytesHostCall();
    void beginExportHostCall(java.lang.String[] a0);
    int debugBandCapHostCall();
    int debugPixelCapMpHostCall();
    int debugStorageLimitMibHostCall();
    boolean isFotuStockViaHost(java.lang.String a0);
    java.lang.String liveClipForCurrentFrameHostCall();
    int liveClipLongEdgeHostCall(java.lang.String a0);
    int maxImportPixelsHostCall(boolean a0);
    int maxImportPixelsHostCall();
    void setDebugBandCapHostCall(int a0);
    void setDebugPixelCapMpHostCall(int a0);
    void setDebugStorageLimitMibHostCall(int a0);
    void showAboutHostCall();
    void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3);
    void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4, java.lang.String a5);
    void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4);
    void showInAppToastHostCall(java.lang.CharSequence a0);
    void startBatchExportHostCall(java.lang.String a0, java.lang.String a1);
    android.content.Context context();
}
