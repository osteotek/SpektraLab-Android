package app.spektralab;

/** Delegating implementation of ExportDialogsHost. */
final class ExportDialogsHostImpl implements app.spektralab.ui.panels.ExportDialogsHost {
    private final MainActivity activity;
    ExportDialogsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public int getLIVE_CLIP_FULL_EDGE() { return activity.LIVE_CLIP_FULL_EDGE; }
    @Override public int getLIVE_CLIP_MAX_EDGE() { return activity.LIVE_CLIP_MAX_EDGE; }
    @Override public java.lang.String getPAPER_SCAN() { return activity.PAPER_SCAN; }
    @Override public boolean getBatchExportAbort() { return activity.batchExportAbort; }
    @Override public void setBatchExportAbort(boolean value) { activity.batchExportAbort = value; }
    @Override public boolean getBatchExportRunning() { return activity.batchExportRunning; }
    @Override public void setBatchExportRunning(boolean value) { activity.batchExportRunning = value; }
    @Override public app.spektralab.engine.SpektraLabEngine getEngine() { return activity.engine; }
    @Override public void setEngine(app.spektralab.engine.SpektraLabEngine value) { activity.engine = value; }
    @Override public int getExportHeals() { return activity.exportHeals; }
    @Override public void setExportHeals(int value) { activity.exportHeals = value; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public java.util.List<app.spektralab.MainActivity.Frame> getFrames() { return activity.frames; }
    @Override public java.lang.String getPrintStock() { return activity.printStock; }
    @Override public void setPrintStock(java.lang.String value) { activity.printStock = value; }
    @Override public app.spektralab.DeviceProbe getProbe() { return activity.probe; }
    @Override public void setProbe(app.spektralab.DeviceProbe value) { activity.probe = value; }
    @Override public app.spektralab.engine.SpektraLabEngine.Session getSession() { return activity.session; }
    @Override public void setSession(app.spektralab.engine.SpektraLabEngine.Session value) { activity.session = value; }
    @Override public int getSourceHeight() { return activity.sourceHeight; }
    @Override public void setSourceHeight(int value) { activity.sourceHeight = value; }
    @Override public int getSourceWidth() { return activity.sourceWidth; }
    @Override public void setSourceWidth(int value) { activity.sourceWidth = value; }
    @Override public long availableMemoryBytesHostCall() { return activity.availableMemoryBytes(); }
    @Override public void beginExportHostCall(java.lang.String[] a0) { activity.beginExport(a0); }
    @Override public int debugBandCapHostCall() { return activity.debugBandCap(); }
    @Override public int debugPixelCapMpHostCall() { return activity.debugPixelCapMp(); }
    @Override public int debugStorageLimitMibHostCall() { return activity.debugStorageLimitMib(); }
    @Override public boolean isFotuStockViaHost(java.lang.String a0) { return activity.isFotuStock(a0); }
    @Override public java.lang.String liveClipForCurrentFrameHostCall() { return activity.liveClipForCurrentFrame(); }
    @Override public int liveClipLongEdgeHostCall(java.lang.String a0) { return activity.liveClipLongEdge(a0); }
    @Override public int maxImportPixelsHostCall(boolean a0) { return activity.maxImportPixels(a0); }
    @Override public int maxImportPixelsHostCall() { return activity.maxImportPixels(); }
    @Override public void setDebugBandCapHostCall(int a0) { activity.setDebugBandCap(a0); }
    @Override public void setDebugPixelCapMpHostCall(int a0) { activity.setDebugPixelCapMp(a0); }
    @Override public void setDebugStorageLimitMibHostCall(int a0) { activity.setDebugStorageLimitMib(a0); }
    @Override public void showAboutHostCall() { activity.showAbout(); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3) { activity.showActionSheet(a0, a1, a2, a3); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4, java.lang.String a5) { activity.showActionSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void showActionSheetHostCall(java.lang.String a0, java.lang.String a1, java.lang.String[] a2, java.util.function.IntConsumer a3, java.lang.Runnable a4) { activity.showActionSheet(a0, a1, a2, a3, a4); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public void startBatchExportHostCall(java.lang.String a0, java.lang.String a1) { activity.startBatchExport(a0, a1); }
    @Override public android.content.Context context() { return activity; }
}
