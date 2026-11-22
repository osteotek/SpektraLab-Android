package app.spektralab.domain;

import java.util.Locale;

/** RAW container extensions shared by the picker, the importer and thumbnails. */
public final class RawFormats {
    private static final String[] RAW = {
            ".rw2", ".raw", ".dng", ".arw", ".nef", ".cr2", ".cr3", ".orf",
            ".raf", ".pef", ".srw", ".x3f", ".rwl", ".3fr", ".mrw", ".erf"
    };

    /** Containers we recognize but cannot decode; reported instead of guessed. */
    public static final String[] KNOWN_UNSUPPORTED = {".heic.raw", ".iiq", ".mos"};

    private RawFormats() {}

    public static boolean isRaw(String name) {
        if (name == null) return false;
        final String lower = name.toLowerCase(Locale.US);
        for (String ext : RAW) if (lower.endsWith(ext)) return true;
        return false;
    }

    /** The unsupported extension, or null when the name is not one of them. */
    public static String knownUnsupported(String name) {
        if (name == null) return null;
        final String lower = name.toLowerCase(Locale.US);
        for (String ext : KNOWN_UNSUPPORTED) if (lower.endsWith(ext)) return ext;
        return null;
    }
}
