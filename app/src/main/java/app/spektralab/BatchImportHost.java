package app.spektralab;

import android.net.Uri;
import android.widget.TextView;

import java.util.List;

/** The narrow Activity surface BatchImportWorker needs. */
interface BatchImportHost {
    TextView statusText();
    List<MainActivity.Frame> frames();
    int currentFrame();
    Uri persistOrCopy(Uri uri);
    boolean addFrameOnly(Uri uri);
    void saveSessionState();
    void rebuildFrameChips();
    void showInAppToast(CharSequence message);
    void reopenFrame(int index);
}
