package app.spektralab.ui;

import app.spektralab.AppLanguage;

/** Shared text lookup for every panel host; the other adapters stay host-specific. */
public interface PanelHost {
    default String text(String key) { return AppLanguage.tr(key); }
}
