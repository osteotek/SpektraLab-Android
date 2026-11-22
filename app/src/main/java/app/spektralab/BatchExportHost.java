package app.spektralab;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.spektralab.engine.SpektraLabEngine;

import java.util.List;
import java.util.function.BooleanSupplier;

/** The narrow Activity surface BatchExportWorker needs. */
interface BatchExportHost {
    int LIVE_LONG_EDGE = 800;
    TextView statusText();
    TextView abortBatchPill();
    TextView sharePill();
    ProgressBar exportProgressBar();
    boolean batchExportRunning();
    void setBatchExportRunning(boolean value);
    boolean batchExportAbort();
    void setBatchExportAbort(boolean value);
    int currentFrame();
    List<MainActivity.Frame> frames();
    String filmStock();
    SpektraLabEngine engine();
    SpektraLabEngine.Session session();
    void closeSessionSafely();
    void saveLookIntoFrames();
    void applyLook(MainActivity.Look look);
    void stopPrefetch();
    void uiWait(Runnable r, long ms);
    boolean pollUntil(BooleanSupplier cond, long ms);
    void reopenFrame(int index);
    void reopenFrameForced(int index);
    void exportImage(String format, String tier, Runnable onDone);
    void renderImage(int longEdge);
    void showInAppToast(CharSequence message);
}
