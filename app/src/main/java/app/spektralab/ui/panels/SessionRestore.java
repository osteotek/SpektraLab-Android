package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.net.Uri;
import app.spektralab.MainActivity.Frame;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.ui.*;

/** SessionRestore builders extracted from MainActivity (architecture Phase C). */
public final class SessionRestore {
    private SessionRestore() {}

    public static boolean restoreSessionState(SessionRestoreHost host) {
        String raw = null;
        try {
            raw = host.context().getSharedPreferences(host.getPREFS_SESSION(), android.content.Context.MODE_PRIVATE).getString(host.getKEY_SESSION(), null);
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:2549", ignored); }
        if (raw == null || raw.isEmpty()) return false;
        try {
            final org.json.JSONObject root = new org.json.JSONObject(raw);
            final org.json.JSONArray arr = root.optJSONArray("frames");
            if (arr == null || arr.length() == 0) return false;
            host.getFrames().clear();
            for (int i = 0; i < arr.length(); ++i) {
                final org.json.JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                final String u = o.optString("uri", "");
                if (u.isEmpty()) continue;
                final Uri uri = Uri.parse(u);
                final Frame frame = new Frame(uri, o.optBoolean("raw", false), o.optString("name", ""));
                frame.look = host.lookFromJsonViaHost(o.optJSONObject("look"));
                // **Legacy sessions open on their print.** A frame saved before
                // this flag existed had no way to say "I was looking at the
                // source"; defaulting to true would bring every old edit back
                // unrendered and read as lost work.
                frame.original = o.optBoolean("orig", false);
                // **The clip, if it is still there.** The file lives in
                // `filesDir`, which survives the process but not "clear data";
                // a missing one just means this frame exports as a still.
                final String lc = o.optString("live", "");
                if (!lc.isEmpty() && new java.io.File(lc).isFile()) {
                    frame.liveClip = lc;
                    frame.liveTsUs = o.optLong("lts", 0L);
                }
                frame.thumb = host.readThumbHostCall(uri);
                host.getFrames().add(frame);
            }
            if (host.getFrames().isEmpty()) return false;
            final int saved = root.optInt("current", -1);
            final String savedUri = (saved >= 0 && saved < host.getFrames().size())
                    ? host.getFrames().get(saved).uri.toString() : "";
            // **A frame whose grant expired can never be opened again.** Left in
            // the strip it re-ran `loadImage`, hit the same dialog, and kept the
            // dead chip for the next launch; dropped here it costs one toast and
            // the start is clean from then on.
            int dropped = 0;
            for (int i = host.getFrames().size() - 1; i >= 0; --i) {
                final Uri frameUri = host.getFrames().get(i).uri;
                // A cold start may carry a file:// URI from an ACTION_VIEW
                // import. ContentResolver.openInputStream cannot always reopen
                // one after the grant ends, but the shared-storage file itself
                // is still there - keep that frame instead of dropping it.
                final boolean readable = host.canReadUriHostCall(frameUri)
                        || ("file".equals(frameUri.getScheme())
                            && frameUri.getPath() != null
                            && new java.io.File(frameUri.getPath()).canRead());
                if (!readable) {
                    host.getFrames().remove(i);
                    ++dropped;
                }
            }
            if (dropped > 0) {
                try {
                    DebugSink.text("session", "dropped " + dropped + " unreadable frames");
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:2598", ignored); }
                host.showInAppToastHostCall(dropped + tr("frame.photos_were_removed_from_the"));
            }
            if (host.getFrames().isEmpty()) return false;
            // The saved index was an index into the old list. The frame itself
            // is what has to come back, so it is looked up again by URI.
            int pick = -1;
            for (int i = 0; i < host.getFrames().size(); ++i) {
                if (host.getFrames().get(i).uri.toString().equals(savedUri)) { pick = i; break; }
            }
            host.setCurrentFrame(pick >= 0 ? pick : host.getFrames().size() - 1);
            host.setShelfCollapsed(root.optBoolean("collapsed", false));
            host.rebuildFrameChipsHostCall();
            host.setShelfCollapsedHostCall(host.getShelfCollapsed());
            try {
                DebugSink.text("session", "restored " + host.getFrames().size() + " frames, current=" + host.getCurrentFrame());
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:2614", ignored); }
            // The photograph is decoded off the shelf by the same road a chip
            // takes. If the file is gone (moved, or a picker grant expired) the
            // import reports it and the strip keeps the rest.
            //
            // `suppressBudgetDialog`: the user answered that question on the
            // first import, and a restore is not a new decision. Without this
            // every return from another app re-asked "this is 50 MP, shrink it?"
            host.setImportBudgetAccepted(true);
            host.setSuppressBudgetDialog(true);
            // **Nothing is decoded while the app is on the desk.** The frame is
            // opened when 去冲印 is pressed -- which also means a cold start no
            // longer spends two seconds developing a photograph nobody asked to
            // see yet.
            if (host.getLightTable() != null) {
                host.refreshLightTableHostCall();
                return true;
            }
            // **Bring the frame back the way it was left.** A frame that was
            // parked on 原图 must not come back developed just because it still
            // carries a look -- that is the same jump the chips had.
            final boolean restoredOriginal = host.getFrames().get(host.getCurrentFrame()).original;
            host.setReopenToOriginal(restoredOriginal);
            host.loadImageHostCall(host.getFrames().get(host.getCurrentFrame()).uri, host.getCurrentFrame());
            // **A frame that had a film gets its print back.** The decode is
            // the whole of `loadImage`; without this an edit that was safely
            // on disk still came back as the original, because nothing ever
            // asked for a render. The strip already says "this is the
            // photograph you were working on" -- the canvas has to agree.
            if (host.getFilmStock() != null && !restoredOriginal) host.renderImageHostCall();
            return true;
        } catch (Throwable t) {
            try { DebugSink.text("session", "restore failed: " + t); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:2646", ignored); }
            return false;
        }
    }
}
