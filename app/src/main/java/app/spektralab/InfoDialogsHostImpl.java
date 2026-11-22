package app.spektralab;

/** Delegating implementation of InfoDialogsHost. */
final class InfoDialogsHostImpl implements app.spektralab.ui.panels.InfoDialogsHost {
    private final MainActivity activity;
    InfoDialogsHostImpl(MainActivity activity) { this.activity = activity; }

    @Override public int getUI_INK() { return activity.UI_INK; }
    @Override public void setUI_INK(int value) { activity.UI_INK = value; }
    @Override public int getUI_LINE() { return activity.UI_LINE; }
    @Override public void setUI_LINE(int value) { activity.UI_LINE = value; }
    @Override public int getUI_PAPER() { return activity.UI_PAPER; }
    @Override public void setUI_PAPER(int value) { activity.UI_PAPER = value; }
    @Override public java.util.List<app.spektralab.MainActivity.AlbumEntry> getAlbum() { return activity.album; }
    @Override public java.util.List<java.lang.String> getFilmIds() { return activity.filmIds; }
    @Override public java.lang.String getFilmStock() { return activity.filmStock; }
    @Override public void setFilmStock(java.lang.String value) { activity.filmStock = value; }
    @Override public android.widget.TextView getLightFilmValue() { return activity.lightFilmValue; }
    @Override public void setLightFilmValue(android.widget.TextView value) { activity.lightFilmValue = value; }
    @Override public android.widget.LinearLayout getLightInfo() { return activity.lightInfo; }
    @Override public void setLightInfo(android.widget.LinearLayout value) { activity.lightInfo = value; }
    @Override public android.widget.TextView getLightInfoFile() { return activity.lightInfoFile; }
    @Override public void setLightInfoFile(android.widget.TextView value) { activity.lightInfoFile = value; }
    @Override public java.lang.String getLightInfoKey() { return activity.lightInfoKey; }
    @Override public void setLightInfoKey(java.lang.String value) { activity.lightInfoKey = value; }
    @Override public android.widget.TextView getLightInfoMeta() { return activity.lightInfoMeta; }
    @Override public void setLightInfoMeta(android.widget.TextView value) { activity.lightInfoMeta = value; }
    @Override public android.widget.TextView getLightPaperValue() { return activity.lightPaperValue; }
    @Override public void setLightPaperValue(android.widget.TextView value) { activity.lightPaperValue = value; }
    @Override public android.animation.ValueAnimator getNoteAnim() { return activity.noteAnim; }
    @Override public void setNoteAnim(android.animation.ValueAnimator value) { activity.noteAnim = value; }
    @Override public android.widget.PopupWindow getNotePopup() { return activity.notePopup; }
    @Override public void setNotePopup(android.widget.PopupWindow value) { activity.notePopup = value; }
    @Override public boolean getShowingOriginal() { return activity.showingOriginal; }
    @Override public void setShowingOriginal(boolean value) { activity.showingOriginal = value; }
    @Override public android.graphics.Typeface getTfHan() { return activity.tfHan; }
    @Override public void setTfHan(android.graphics.Typeface value) { activity.tfHan = value; }
    @Override public boolean cardShowsStockViaHost(java.lang.String a0, java.lang.String a1) { return activity.cardShowsStock(a0, a1); }
    @Override public void componentArrivalHostCall(android.view.View a0) { activity.componentArrival(a0); }
    @Override public int[] dimsOfHostCall(java.lang.String a0) { return activity.dimsOf(a0); }
    @Override public int dpHostCall(float a0) { return activity.dp(a0); }
    @Override public java.lang.String idFromPrettyHostCall(java.lang.String a0, boolean a1) { return activity.idFromPretty(a0, a1); }
    @Override public boolean isFotuStockViaHost(java.lang.String a0) { return activity.isFotuStock(a0); }
    @Override public android.widget.TextView labelHostCall(java.lang.String a0, float a1, int a2, android.graphics.Typeface a3) { return activity.label(a0, a1, a2, a3); }
    @Override public void markFileViaHost(java.lang.String a0) { activity.markFile(a0); }
    @Override public java.lang.String ratioViaHost(java.lang.String a0) { return activity.ratio(a0); }
    @Override public void selectFotuStockHostCall(java.lang.String a0) { activity.selectFotuStock(a0); }
    @Override public void selectSpkStockHostCall(java.lang.String a0, int a1) { activity.selectSpkStock(a0, a1); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.lang.String a2, java.util.function.Consumer<java.lang.String> a3) { activity.showChoiceSheet(a0, a1, a2, a3); }
    @Override public void showChoiceSheetHostCall(java.lang.String a0, java.util.List<java.lang.String> a1, java.util.List<java.lang.String> a2, java.lang.String a3, java.util.function.IntPredicate a4, java.util.function.Consumer<java.lang.String> a5) { activity.showChoiceSheet(a0, a1, a2, a3, a4, a5); }
    @Override public void typeNoteHostCall(android.widget.TextView a0, java.lang.String a1) { activity.typeNote(a0, a1); }
    @Override public android.content.Context context() { return activity; }
}
