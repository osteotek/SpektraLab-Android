package app.spektralab.testing;

import app.spektralab.domain.AppPreferences;

import java.util.HashMap;
import java.util.Map;

/** In-memory preferences double for JVM tests. */
public final class FakeAppPreferences implements AppPreferences {
    private final Map<String, Object> values = new HashMap<>();

    @Override public boolean getBoolean(String key, boolean fallback) {
        Object value = values.get(key);
        return value instanceof Boolean ? (Boolean) value : fallback;
    }

    @Override public void putBoolean(String key, boolean value) { values.put(key, value); }
}
