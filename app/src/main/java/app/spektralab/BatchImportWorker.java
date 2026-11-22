package app.spektralab;

import static app.spektralab.AppLanguage.tr;
import android.net.Uri;
import java.util.List;

/** Worker extracted from MainActivity; delegates Android/Activity access to host. */
final class BatchImportWorker extends HostWorker {
    BatchImportWorker(MainActivity host) { super(host); }

    void run(final java.util.List<Uri> uris) {
        final int total = uris.size();
        UiDispatcher.post(() -> {
            if (host.statusText != null) host.statusText.setText(tr("export.adding") + total + tr("export.photos"));
        });
        new Thread(() -> {
            int added = 0, failed = 0;
            for (Uri u : uris) {
                final Uri target = host.persistOrCopy(u);
                if (target == null) { ++failed; continue; }
                if (host.addFrameOnly(target)) ++added; else ++failed;
            }
            final int a = added, f = failed;
            UiDispatcher.post(() -> {
                if (a > 0) {
                    host.saveSessionState();
                    host.rebuildFrameChips();
                }
                final String msg = tr("export.added") + a + tr("export.photos_2") + (f > 0 ? tr("export.skipped") + f + tr("export.photos_2") : "");
                if (host.statusText != null) host.statusText.setText(msg);
                host.showInAppToast(msg);
                if (a > 0 && host.currentFrame < 0 && !host.frames.isEmpty()) {
                    host.reopenFrame(host.frames.size() - a);
                }
            });
        }, "SpektraLab-batch-import").start();
      }
}
