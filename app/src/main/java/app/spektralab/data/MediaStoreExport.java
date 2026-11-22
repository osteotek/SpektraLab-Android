package app.spektralab.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import app.spektralab.Diagnostics;
import app.spektralab.SpkError;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Writes a finished export into MediaStore, with the album-write fallbacks. */
public final class MediaStoreExport {
    private final ContentResolver resolver;

    public MediaStoreExport(Context context) {
        resolver = context.getContentResolver();
    }

    public Uri write(File tmp, String baseName, String ext, String mime) throws IOException {
        Throwable lastError = null;
        for (int attempt = 0; attempt < 2; ++attempt) {
            final String name = attempt == 0 ? baseName
                    : "SpektraLab_" + System.currentTimeMillis() + "_r" + ext;
            try {
                final Uri uri = writeTo(tmp, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        Environment.DIRECTORY_PICTURES, name, mime);
                if (uri != null) return uri;
            } catch (Throwable t) { lastError = t; }
        }
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                final Uri uri = writeTo(tmp, MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        Environment.DIRECTORY_DOWNLOADS,
                        "SpektraLab_" + System.currentTimeMillis() + ext, mime);
                if (uri != null) return uri;
            } catch (Throwable t) { lastError = t; }
        }
        throw new IOException(SpkError.ALBUM_WRITE_PREFIX, lastError);
    }

    /** One insert/copy/publish attempt; null means the provider refused the insert. */
    private Uri writeTo(File tmp, Uri collection, String directory, String name, String mime)
            throws Throwable {
        final ContentValues v = new ContentValues();
        v.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        v.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        v.put(MediaStore.MediaColumns.RELATIVE_PATH, directory + "/SpektraLab");
        v.put(MediaStore.MediaColumns.IS_PENDING, 1);
        final Uri uri = resolver.insert(collection, v);
        if (uri == null) return null;
        try {
            copyInto(tmp, uri);
            v.clear();
            v.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, v, null, null);
            return uri;
        } catch (Throwable t) {
            try { resolver.delete(uri, null, null); }
            catch (Throwable e) { Diagnostics.ignored("MediaStoreExport:delete", e); }
            throw t;
        }
    }

    private void copyInto(File tmp, Uri uri) throws IOException {
        try (InputStream in = new FileInputStream(tmp);
             OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("openOutputStream returned null");
            final byte[] buf = new byte[262144];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }
}
