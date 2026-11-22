package app.spektralab.ui;

import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;

/** Tactile control clicks (extracted from MainActivity). */
    public final class Ticker {
        public static final int LINE = 0, MONTH = 1, TRAVEL = 2, FINE = 3, CLOSE = 4;
        private static final float[] PITCH = {1.0f, 0.88f, 1.08f, 1.5f, 1.2f};
        private static final float[] DECAY = {1.0f, 1.2f, 0.8f, 0.7f, 0.85f};
        private static final float[] LEVEL = {1.0f, 1.1f, 0.5f, 0.55f, 0.4f};
        // Their 0.2 is a WebAudio mix level; on a phone speaker that is inaudible
        // under any ambient noise. The waveform is normalised, so this is the
        // only gain there is.
        private static final float MASTER = 0.85f;
        private static final long MIN_GAP_MS = 60;

        private static Ticker shared;
        private final android.media.SoundPool pool;
        private final int[] ids = new int[5];
        private long lastAt = -10000;
        private int pending = -1;

        public static Ticker of(android.content.Context c) {
            if (shared == null) shared = new Ticker(c.getApplicationContext());
            return shared;
        }

        private Ticker(android.content.Context c) {
            android.media.SoundPool p = null;
            try {
                // **No "touch sounds" gate.** This is the app's own control, and
                // gating it on a system switch meant silence on every ROM that
                // ships that switch off -- which is most of them.
                {
                    p = new android.media.SoundPool.Builder()
                            .setMaxStreams(4)
                            .setAudioAttributes(new android.media.AudioAttributes.Builder()
                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build())
                            .build();
                    final java.io.File dir = c.getCacheDir();
                    for (int i = 0; i < 5; ++i) {
                        final java.io.File f = new java.io.File(dir, "detent-" + i + "-v2.wav");
                        if (!f.isFile() || f.length() < 64) write(f, 44100, PITCH[i], DECAY[i]);
                        ids[i] = p.load(f.getAbsolutePath(), 1);
                    }
                }
            } catch (Throwable t) {
                p = null;
            }
            pool = p;
            // One line the device can prove it with, if a click is ever silent.
            try {
                if (p == null || ids[0] == 0) {
                    DebugSink.report("dial-sound", "the detent click did not load",
                            "pool=" + (p != null) + " id=" + ids[0] + " wav="
                                    + new java.io.File(c.getCacheDir(), "detent-0-v2.wav").length());
                }
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1161", ignored); }
        }

        private boolean armed;

        public void play(int kind) {
            if (pool == null || ids[kind] == 0) return;
            final long now = android.os.SystemClock.uptimeMillis();
            final long gap = now - lastAt;
            if (gap < MIN_GAP_MS) {
                // Deferred, not dropped: one more at the end of the window.
                if (pending < 0) {
                    pending = kind;
                    android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                    h.postDelayed(() -> {
                        final int k = pending;
                        pending = -1;
                        if (k >= 0) play(k);
                    }, MIN_GAP_MS - gap + 1);
                }
                return;
            }
            lastAt = now;
            try {
                final float rate = PITCH[kind] * (float) (1 + (Math.random() - 0.5) * 0.04);
                final int stream = pool.play(ids[kind], MASTER * LEVEL[kind],
                        MASTER * LEVEL[kind], 0, 0, Math.max(0.5f, Math.min(2f, rate)));
                if (!armed) {
                    armed = true;
                    try {
                        DebugSink.report("dial-sound", "first detent click",
                                "id=" + ids[kind] + " stream=" + stream + " vol=" + MASTER);
                    } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1193", ignored); }
                }
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1195", ignored); }
        }

        /** Their sample generator, one buffer per variant. */
        private static void write(java.io.File out, int rate, float pitch, float decay)
                throws java.io.IOException {
            final int n = Math.round(rate * 0.04f * Math.max(1f, decay));
            final float[] buf = new float[n];
            final double w = 2 * Math.PI * 4000 * pitch / rate;
            final double cos = Math.cos(w);
            final double sin = Math.sin(w) / 1.4142135623730951;
            final double b0 = (1 + cos) / 2, b1 = -(1 + cos), b2 = (1 + cos) / 2;
            final double a0 = 1 + sin, a1 = -2 * cos, a2 = 1 - sin;
            double x1 = 0, x2 = 0, y1 = 0, y2 = 0, peak = 0;
            final float[][] partials = {{5200f, 0.8f, 0.6f}, {7400f, 0.5f, 0.4f}};
            for (int i = 0; i < n; ++i) {
                final float t = i / (float) rate;
                final double x = Math.random() * 2 - 1;
                final double y = (b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2) / a0;
                x2 = x1; x1 = x; y2 = y1; y1 = y;
                double v = 0.5 * y * Math.exp(-t / (0.25 * decay / 1000.0)) * 3;
                for (float[] q : partials) {
                    v += q[2] * Math.sin(2 * Math.PI * q[0] * pitch * t)
                            * Math.exp(-t / (q[1] * decay / 1000.0));
                }
                buf[i] = (float) (v * Math.min(1, t / 1e-4));
                peak = (float) Math.max(peak, Math.abs(buf[i]));
            }
            final int fade = Math.max(1, Math.round(n * 0.005f));
            for (int i = 0; i < fade; ++i) buf[n - 1 - i] *= i / (float) fade;
            final float norm = peak == 0 ? 1f : (float) peak;
            final int bytes = n * 2;
            try (java.io.DataOutputStream o = new java.io.DataOutputStream(
                    new java.io.BufferedOutputStream(new java.io.FileOutputStream(out), 4096))) {
                o.writeBytes("RIFF"); o.writeInt(Integer.reverseBytes(36 + bytes));
                o.writeBytes("WAVEfmt "); o.writeInt(Integer.reverseBytes(16));
                o.writeShort(Short.reverseBytes((short) 1));
                o.writeShort(Short.reverseBytes((short) 1));
                o.writeInt(Integer.reverseBytes(rate));
                o.writeInt(Integer.reverseBytes(rate * 2));
                o.writeShort(Short.reverseBytes((short) 2));
                o.writeShort(Short.reverseBytes((short) 16));
                o.writeBytes("data"); o.writeInt(Integer.reverseBytes(bytes));
                for (float v : buf) {
                    o.writeShort(Short.reverseBytes((short) Math.round(v / norm * 32767f)));
                }
            }
        }
    }
