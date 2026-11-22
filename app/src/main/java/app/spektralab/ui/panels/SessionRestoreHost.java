package app.spektralab.ui.panels;

/** Narrow Activity surface for the SessionRestore builders. Implemented by SessionRestoreHostImpl. */
public interface SessionRestoreHost {
    java.lang.String getKEY_SESSION();
    java.lang.String getPREFS_SESSION();
    int getCurrentFrame();
    void setCurrentFrame(int value);
    java.lang.String getFilmStock();
    void setFilmStock(java.lang.String value);
    java.util.List<app.spektralab.MainActivity.Frame> getFrames();
    boolean getImportBudgetAccepted();
    void setImportBudgetAccepted(boolean value);
    android.view.View getLightTable();
    void setLightTable(android.view.View value);
    boolean getReopenToOriginal();
    void setReopenToOriginal(boolean value);
    app.spektralab.engine.SpektraLabEngine.Session getSession();
    void setSession(app.spektralab.engine.SpektraLabEngine.Session value);
    boolean getShelfCollapsed();
    void setShelfCollapsed(boolean value);
    boolean getSuppressBudgetDialog();
    void setSuppressBudgetDialog(boolean value);
    boolean canReadUriHostCall(android.net.Uri a0);
    void loadImageHostCall(android.net.Uri a0);
    void loadImageHostCall(android.net.Uri a0, int a1);
    app.spektralab.MainActivity.Look lookFromJsonViaHost(org.json.JSONObject a0);
    android.graphics.Bitmap readThumbHostCall(android.net.Uri a0);
    void rebuildFrameChipsHostCall();
    void refreshLightTableHostCall();
    void renderImageHostCall();
    void renderImageHostCall(int a0);
    void setShelfCollapsedHostCall(boolean a0);
    void showInAppToastHostCall(java.lang.CharSequence a0);
    android.content.Context context();
}
