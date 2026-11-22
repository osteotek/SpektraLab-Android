package app.spektralab.domain;

/** Shared engine defaults; one source of truth for UI, export and presets. */
public final class Defaults {
    public static final String PRINT_STOCK = "kodak_portra_endura";
    public static final String MONO_PRINT_STOCK = "ilford_multigrade_iv_rc";
    public static final String METER = "protect_highlights";
    public static final String ORIGINAL_ID = "original";
    public static final String WORKING_SPACE = "ProPhoto RGB";

    private Defaults() {}

    /** The fallback used everywhere a Look may not name a paper stock. */
    public static String effectivePrintStock(String stock) {
        return stock == null ? PRINT_STOCK : stock;
    }
}
