package app.spektralab.ui.panels;

import java.io.InputStream;

import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;

/** Content-URI and stream utilities extracted from MainActivity. */
public final class UriFiles {
    private UriFiles() {}

    public static String getFileName(android.content.Context context, android.net.Uri uri) {
    String result = null;
    if (uri.getScheme().equals("content")) {
        try (android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        }
    }
    if (result == null) {
        result = uri.getPath();
        // A provider can answer a `content://` URI whose path is null; the
        // crash then lands in `lastIndexOf` and reads as an import failure
        // with no file at all.
        if (result == null) return null;
        int cut = result.lastIndexOf('/');
        if (cut != -1) result = result.substring(cut + 1);
    }
    return result;
    }

    public static String getFilePathFromUri(android.content.Context context, android.net.Uri uri) {
    // For content:// URIs, we need to copy to a temp file
    if (uri.getScheme().equals("content")) {
        try {
            String fileName = getFileName(context, uri);
            // **`filesDir`, not `cacheDir`.** The cache directory is the one
            // place the system is free to delete from under us, and the path
            // this returns is kept in `rawFilePath` and re-read by every full
            // export. A copy cut short reports `RAW unpack: Input/output
            // error (-100009)`, which reads like a corrupt file -- and the
            // file is fine. `filesDir/import` is the directory `privateCopy`
            // already uses for the same reason.
            java.io.File dir = new java.io.File(context.getFilesDir(), "import");
            if (!dir.exists() && !dir.mkdirs()) dir = context.getFilesDir();
            java.io.File tempFile = new java.io.File(dir, fileName);
            // What the provider says the size is, so a short copy is caught
            // here rather than in libraw's unpack().
            long expected = -1;
            try (android.database.Cursor c = context.getContentResolver().query(uri,
                    new String[]{android.provider.OpenableColumns.SIZE}, null, null, null)) {
                if (c != null && c.moveToFirst()) expected = c.getLong(0);
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30175", ignored); }
            // **Already here, and whole: use it.** Every full export and every
            // fotufilm render asks for this path; copying the file again on
            // each of them is work nobody asked for.
            // **Only a *short* copy is a broken copy.** A provider's
            // `OpenableColumns.SIZE` can under-report: a Xiaomi gallery
            // answered 5,248,554 for a file that reads 5,248,586 -- and the
            // old `!= expected` test called that "copy short", deleted a
            // perfectly good copy twice, and left `fotufilm` with no path
            // ("\u8bfb\u4e0d\u5230\u539f\u56fe\u6587\u4ef6\uff1b\u8bf7\u91cd\u65b0\u5bfc\u5165\u8fd9\u4e00\u5f20",
            // SL-261118-06CA). The canvas never noticed: it decodes the
            // stream directly, and this door is only the second engine's.
            if (expected > 0 && tempFile.isFile() && tempFile.length() >= expected) {
                return tempFile.getAbsolutePath();
            }
            // **Two callers can resolve the same grant at the same time.** The
            // canvas' import and the second engine's develop both land here,
            // and they used to write this path directly: one truncated the
            // other's bytes, and the engine then failed on a half-written
            // JPEG -- a 1 in two milliseconds, right after the tap. Each copy
            // gets its own name and the rename is atomic, so a reader only
            // ever sees a whole file.
            // **A short copy gets one more open.** Some providers answer the
            // first stream for a file they are still staging from the cloud
            // and hand out the whole thing the second time -- and the price
            // of asking again is one `openInputStream`, against a photograph
            // the user cannot render until they re-import it by hand.
            java.io.File part = null;
            for (int attempt = 0; attempt < 2; ++attempt) {
                part = new java.io.File(dir, fileName + "." + System.nanoTime() + ".part");
                try (InputStream in = context.getContentResolver().openInputStream(uri);
                     java.io.OutputStream out = new java.io.FileOutputStream(part)) {
                    if (in == null) { part.delete(); part = null; return null; }
                    final byte[] buf = new byte[64 * 1024];
                    copyStream(in, out, buf);
                } catch (Exception e) {
                    part.delete();
                    part = null;
                    try { DebugSink.text("import", "copy failed: " + e); }
                    catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30214", ignored); }
                    return null;
                }
                if (expected <= 0 || part.length() >= expected) {
                    if (expected > 0 && part.length() > expected) {
                        // Worth knowing: it means the provider's SIZE is not
                        // the file's length, which is what killed fotufilm.
                        try {
                            DebugSink.text("import", "copy longer than reported: "
                                    + part.length() + " > " + expected + " for " + fileName);
                        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30224", ignored); }
                    }
                    break;
                }
                try { DebugSink.text("import", "copy short: " + part.length() + " of "
                        + expected + " for " + fileName + " (attempt " + (attempt + 1) + ")"); }
                catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30230", ignored); }
                part.delete();
                part = null;
            }
            if (part == null) return null;
            if (!part.renameTo(tempFile)) {
                // Same directory, so a rename crosses no mount point and this
                // should not happen -- but a `.part` name must never escape:
                // the second engine decides by extension whether it can read
                // the file, and would re-encode a perfectly good JPEG.
                try (java.io.FileInputStream in = new java.io.FileInputStream(part);
                     java.io.FileOutputStream out = new java.io.FileOutputStream(tempFile)) {
                    byte[] buf = new byte[64 * 1024];
                    copyStream(in, out, buf);
                } catch (Exception e) {
                    part.delete();
                    return null;
                }
                part.delete();
            }
            try { DebugSink.text("import", "copied " + tempFile.length() + " -> "
                    + tempFile.getAbsolutePath()); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30251", ignored); }
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            try { DebugSink.text("import", "copy failed: " + e); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:30254", ignored); }
            return null;
        }
    } else if (uri.getScheme().equals("file")) {
        return uri.getPath();
    }
    return null;
    }

    public static boolean canReadUri(android.content.Context context, android.net.Uri uri) {
    if (uri == null) return false;
    try (InputStream in = context.getContentResolver().openInputStream(uri)) {
        return in != null;
    } catch (Throwable t) {
        return false;
    }
    }

    public static long copyStream(java.io.InputStream in, java.io.OutputStream out, byte[] buf) throws java.io.IOException {
    long total = 0;
    int idle = 0;
    while (true) {
        final int n = in.read(buf);
        if (n < 0) break;
        if (n == 0) {
            if (++idle > 64) break;
            try { Thread.sleep(2); } catch (InterruptedException ignored) { }
            continue;
        }
        idle = 0;
        out.write(buf, 0, n);
        total += n;
    }
    return total;
    }
}
