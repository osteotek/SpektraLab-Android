package app.spektralab;

/** Delegating implementation of SessionRestoreHost. */
final class SessionRestoreHostImpl implements app.spektralab.ui.panels.SessionRestoreHost {
    private final MainActivity activity;
    SessionRestoreHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public java.lang.String getKEY_SESSION() { return activity.KEY_SESSION; }
    @Override public java.lang.String getPREFS_SESSION() { return activity.PREFS_SESSION; }
    @Override public int getCurrentFrame() { return activity.currentFrame; }
    @Override public void setCurrentFrame(int value) { activity.currentFrame = value; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public java.util.List<app.spektralab.MainActivity.Frame> getFrames() { return activity.frames; }
    @Override public boolean getImportBudgetAccepted() { return activity.importBudgetAccepted; }
    @Override public void setImportBudgetAccepted(boolean value) { activity.importBudgetAccepted = value; }
    @Override public android.view.View getLightTable() { return activity.lightTable; }
    @Override public void setLightTable(android.view.View value) { activity.lightTable = value; }
    @Override public boolean getReopenToOriginal() { return activity.reopenToOriginal; }
    @Override public void setReopenToOriginal(boolean value) { activity.reopenToOriginal = value; }
    @Override public app.spektralab.engine.SpektraLabEngine.Session getSession() { return activity.session; }
    @Override public void setSession(app.spektralab.engine.SpektraLabEngine.Session value) { activity.session = value; }
    @Override public boolean getShelfCollapsed() { return activity.shelfCollapsed; }
    @Override public void setShelfCollapsed(boolean value) { activity.shelfCollapsed = value; }
    @Override public boolean getSuppressBudgetDialog() { return activity.suppressBudgetDialog; }
    @Override public void setSuppressBudgetDialog(boolean value) { activity.suppressBudgetDialog = value; }
    @Override public boolean canReadUriHostCall(android.net.Uri a0) { return activity.canReadUri(a0); }
    @Override public void loadImageHostCall(android.net.Uri a0) { activity.loadImage(a0); }
    @Override public void loadImageHostCall(android.net.Uri a0, int a1) { activity.loadImage(a0, a1); }
    @Override public app.spektralab.MainActivity.Look lookFromJsonViaHost(org.json.JSONObject a0) { return activity.lookFromJson(a0); }
    @Override public android.graphics.Bitmap readThumbHostCall(android.net.Uri a0) { return activity.readThumb(a0); }
    @Override public void rebuildFrameChipsHostCall() { activity.rebuildFrameChips(); }
    @Override public void refreshLightTableHostCall() { activity.refreshLightTable(); }
    @Override public void renderImageHostCall() { activity.renderImage(); }
    @Override public void renderImageHostCall(int a0) { activity.renderImage(a0); }
    @Override public void setShelfCollapsedHostCall(boolean a0) { activity.setShelfCollapsed(a0); }
    @Override public void showInAppToastHostCall(java.lang.CharSequence a0) { activity.showInAppToast(a0); }
    @Override public android.content.Context context() { return activity; }
}
