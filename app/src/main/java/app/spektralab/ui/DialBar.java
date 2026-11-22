package app.spektralab.ui;

import app.spektralab.Diagnostics;

/** The film-parameter dial (extracted from MainActivity). */
    public final class DialBar extends android.widget.SeekBar {
    private final boolean animArrival;
    private final android.view.animation.Interpolator ease;
    private final int blue, ink, muted;
        /**
         * **A lock of the page's own.** `setEnabled` is re-asserted by
         * `setOpticsUnlocked` on every render and every state change
         * (`setEnabledDeepExcept(..., unlocked, null)`), so a row greyed out by
         * the latitude tier came back live the moment the picture finished: the
         * label stayed grey -- nothing else writes a text colour -- while the
         * thumb moved. This field is written by `gateEnabled` and read by
         * `onTouchEvent`, and by nothing else.
         */
        public boolean latLocked;
        /** Notches across the range, and how many of them make a major tick. */
        private static final int kSteps = 96, kMajor = 4, kClickEvery = 2;
        /** Screen pitch of one notch, and the unit their formulas are written in. */
        private static final float kPitchDp = 12f, kUnitDp = 14f;
        private static final long kHoldMs = 360;
        /** Their spring, per millisecond, and their release projection. */
        private static final float OMEGA = 0.026f, ZETA = 0.7f, PROJECT_MS = 150f;

        /** Set by the activity once: prints the value over everything, on the
         *  anchor's centre, with the dial's own spring and fade. */
        public interface Readout { void show(android.view.View anchor, String text, float pop, float alpha); }
        public static Readout readout;

        private static android.os.Vibrator vibrator;

        private final float d;
        private final int thumbSlop;
        private final android.graphics.Paint paint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF rect = new android.graphics.RectF();
        private final android.os.Handler handler = new android.os.Handler(
                android.os.Looper.getMainLooper());

        private android.widget.SeekBar.OnSeekBarChangeListener listener;
        private java.util.function.IntFunction<String> format;
        private Ticker ticker;

        /**
         * The entrance: notches arriving from both ends inwards, one by one,
         * meeting under the pointer, then one burst of light.
         *
         * <p>`introT < 0` is idle. Everything below is a per-circle rank and a
         * short hot window, so a notch that has just landed is visibly taller
         * and bluer than the ones already settled -- that is the tr("ui170.024"),
         * and without it the arrival is a wave with no beat.
         */
        private float introT = -1f;
        private float glowA;
        private android.animation.ValueAnimator introAnim;
        private android.graphics.Shader glowShader;
        private android.graphics.Shader haloShader;
        private float haloR = -1f;
        /** Which entrance owns the pending start; an older one is a no-op. */
        private int introGen;

        static final int MS_TICK = 760, MS_GLOW = 620, MS_START = 100;
        /** How long a notch stays hot after it lands, as a fraction of the run. */
        static final float kHot = 0.14f;

        /** Replay the entrance. Called when the page carrying this bar arrives. */
        public void startIntro() {
            if (introAnim != null) { introAnim.cancel(); introAnim = null; }
            // **Settled is `introT < 0`.** With the entrance off this is not
            // "skip the start": every notch is drawn at its full height and
            // nothing glows exactly when the entrance is idle, so this is the
            // finished bar. Starting one anyway would leave the page animating
            // with the switch saying otherwise.
            final int gen = ++introGen;
            if (!animArrival) {
                introT = -1f;
                glowA = 0f;
                invalidate();
                return;
            }
            introT = 0f;
            glowA = 0f;
            // The row it sits in is still fading at t = 0; a beat that starts
            // under a half-transparent row is wasted. The generation check keeps
            // a second arrival from leaving two entrances running at once.
            handler.postDelayed(() -> { if (gen == introGen) runIntro(); }, MS_START);
        }

        private void runIntro() {
            if (introT < 0f) return;
            final android.animation.ValueAnimator a =
                    android.animation.ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(MS_TICK);
            a.setInterpolator(new android.view.animation.LinearInterpolator());
            a.addUpdateListener(an -> { introT = (Float) an.getAnimatedValue(); invalidate(); });
            a.addListener(new android.animation.AnimatorListenerAdapter() {
                private boolean cancelled;
                @Override public void onAnimationCancel(android.animation.Animator x) {
                    cancelled = true;
                }
                @Override public void onAnimationEnd(android.animation.Animator x) {
                    if (cancelled) { introT = -1f; glowA = 0f; invalidate(); return; }
                    // Every notch is in: the bar takes a breath of light and
                    // then behaves like a bar.
                    final android.animation.ValueAnimator g =
                            android.animation.ValueAnimator.ofFloat(1f, 0f);
                    g.setDuration(MS_GLOW);
                    g.setInterpolator(ease);
                    g.addUpdateListener(an -> {
                        final float p = (Float) an.getAnimatedValue();
                        // **An exponential tail, not a linear ramp.** With
                        // `1 - p` and an ease-out clock the light was 87 % gone
                        // after 100 ms and the rest was invisible, which read as
                        // the glow being switched off. `exp(-3.4 p)` keeps a
                        // long, even fall; the 0.033 floor is subtracted so the
                        // last frame is exactly zero and there is no step.
                        glowA = (float) Math.max(0.0, (Math.exp(-3.4 * p) - 0.033) / 0.967);
                        // **Let the last notch come back down.** Freezing
                        // `introT` at 1 held the pair under the pointer at full
                        // `pop` for the whole burst -- 95 % taller and 55 %
                        // wider, held steady, which is the slab that was
                        // reported as "the centre tick turns into a rectangle".
                        introT = 1f + 0.30f * p;
                        invalidate();
                    });
                    g.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(android.animation.Animator y) {
                            introT = -1f;
                            glowA = 0f;
                            invalidate();
                        }
                    });
                    g.start();
                }
            });
            introAnim = a;
            a.start();
        }

        /** Where the marker is drawn (springs), and which notch it is on. */
        private float pos;
        private float vel;                 // notches per millisecond
        private int target;
        private boolean selfDriven;
        private float downX, downY, posAtDown;
        private boolean tracking, dragging, focused, movedWhileDown, touching;
        private long lastUpMs, lastHapticAt, lastMoveAt;
        private int lastMajor, resetProgress = -1;
        private Runnable onReset;
        private float valueAlpha, pop = 1f, popVel;
        private long lastFrameAt;

        private final Runnable reveal = new Runnable() {
            @Override public void run() {
                if (!tracking) return;
                focused = true;
                haptic(true);
                if (ticker != null) ticker.play(Ticker.LINE);
                startAnim();
                invalidate();
            }
        };

        private final Runnable anim = new Runnable() {
            @Override public void run() {
                final long now = android.os.SystemClock.uptimeMillis();
                final float dtMs = Math.min(8f, Math.max(1f, now - lastFrameAt));
                lastFrameAt = now;
                boolean more = false;

                // Their spring: the marker chases the notch it is on.
                if (Math.abs(pos - target) > 0.004f || Math.abs(vel) > 0.0004f) {
                    vel += (OMEGA * OMEGA * (target - pos) - 2 * ZETA * OMEGA * vel) * dtMs;
                    pos += vel * dtMs;
                    more = true;
                } else if (pos != target) {
                    pos = target;
                    vel = 0f;
                }

                final float want = (touching || focused) ? 1f : 0f;
                if (Math.abs(valueAlpha - want) > 0.01f) {
                    valueAlpha += (want - valueAlpha) * Math.min(1f, dtMs * 0.014f);
                    more = true;
                } else {
                    valueAlpha = want;
                }
                final float dtS = dtMs / 1000f;
                popVel += (900f * (1f - pop) - 26f * popVel) * dtS;
                pop += popVel * dtS;
                if (Math.abs(pop - 1f) > 0.004f || Math.abs(popVel) > 0.02f) more = true;

                publish();
                invalidate();
                if (more) postOnAnimation(this);
            }
        };

        public DialBar(android.content.Context c, boolean animArrival,
            android.view.animation.Interpolator ease,
            int blue, int ink, int muted) {
            super(c);
        this.animArrival = animArrival; this.ease = ease;
        this.blue = blue; this.ink = ink; this.muted = muted;
            d = c.getResources().getDisplayMetrics().density;
            thumbSlop = (int) (26 * d);
            setThumb(null);
            setBackground(null);
            setForeground(null);
            setSplitTrack(false);
            setFocusable(false);
            setFocusableInTouchMode(false);
            setClickable(false);
            setLongClickable(false);
            setSelected(false);
            if (android.os.Build.VERSION.SDK_INT >= 26) setDefaultFocusHighlightEnabled(false);
            if (vibrator == null) {
                try {
                    vibrator = (android.os.Vibrator) c.getApplicationContext()
                            .getSystemService(android.content.Context.VIBRATOR_SERVICE);
                } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1486", ignored); }
            }
            try {
                ticker = Ticker.of(c);
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1490", ignored); }
        }

        public void setValueFormat(java.util.function.IntFunction<String> f) { format = f; }

        @Override public void setOnSeekBarChangeListener(android.widget.SeekBar.OnSeekBarChangeListener l) {
            listener = l;
            super.setOnSeekBarChangeListener(l);
        }

        public void setResetTarget(int progress, Runnable action) {
            resetProgress = progress;
            onReset = action;
        }

        public boolean isDragging() { return dragging; }

        @Override public synchronized void setProgress(int progress) {
            super.setProgress(progress);
            if (!selfDriven) {
                final float p = progress * kSteps / (float) Math.max(1, getMax());
                pos = p;
                target = Math.round(p);
                vel = 0f;
            }
            invalidate();
        }

        private float pitch() { return kPitchDp * d; }

        private float pointerX() { return getWidth() * 0.5f; }

        private void startAnim() {
            lastFrameAt = android.os.SystemClock.uptimeMillis();
            postOnAnimation(anim);
        }

        /** Hand the current text to the floating readout, if there is one. */
        private void publish() {
            if (readout == null || format == null) return;
            final String text = format.apply(Math.round(target * getMax() / (float) kSteps));
            try {
                readout.show(this, text, pop, valueAlpha);
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1533", ignored); }
        }

        // --- input ------------------------------------------------------------

        @Override public boolean onTouchEvent(android.view.MotionEvent ev) {
            if (latLocked || !isEnabled()) return false;
            switch (ev.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN: {
                    downX = ev.getX();
                    downY = ev.getY();
                    // **Anywhere on the bar starts a drag.** This used to refuse
                    // a touch that was not within `thumbSlop` of the pointer --
                    // the dial's own centre -- so the marks either side of it,
                    // which are what the eye reads as the thing to grab, did
                    // nothing. The drag is *relative* (the value moves by what
                    // the finger moves), so a touch at the far edge does not
                    // yank the number to that edge.
                    final boolean onPointer = Math.abs(downX - pointerX()) <= thumbSlop;
                    final long now = android.os.SystemClock.uptimeMillis();
                    // The double tap that puts a value back stays on the pointer:
                    // letting it fire anywhere would turn a quick second drag
                    // from the edge into a silent reset.
                    if (onPointer && resetProgress >= 0 && now - lastUpMs < 300) {
                        setProgress(resetProgress);
                        if (onReset != null) onReset.run();
                        lastUpMs = 0;
                        return true;
                    }
                    touching = true;
                    tracking = true;
                    dragging = false;
                    movedWhileDown = false;
                    focused = false;
                    posAtDown = target;
                    lastMoveAt = now;
                    setPressed(true);
                    // **Not yet.** Claiming the gesture on DOWN would swallow a
                    // vertical scroll that merely started on the bar, and every
                    // row of this panel sits inside a scroll. The claim happens
                    // in MOVE, once the direction is known.
                    handler.removeCallbacks(reveal);
                    handler.postDelayed(reveal, kHoldMs);
                    startAnim();
                    return true;
                }
                case android.view.MotionEvent.ACTION_MOVE: {
                    if (!tracking) return false;
                    if (!dragging) {
                        final float adx = Math.abs(ev.getX() - downX);
                        final float ady = Math.abs(ev.getY() - downY);
                        // **A vertical flick must never move a value.** This used
                        // to give way only when `ady > adx`, i.e. the drag started
                        // on the first frame where the sideways travel beat the
                        // downward one by a single pixel. Scrolling the panel with
                        // 6-8 dp of sideways noise therefore stepped the dial, and
                        // the pitch is 12 dp a step, so that noise *is* a step:
                        // tr("ui170.025"). Two conditions now --
                        // enough sideways travel to mean it, and clearly more
                        // sideways than vertical.
                        if (adx < 8f * d || adx < ady * 1.5f) return true;
                        dragging = true;
                        // **Re-anchor.** The travel that qualified the gesture
                        // must not also move the value, or the dial jumps a step
                        // the instant it decides the finger meant it.
                        downX = ev.getX();
                        posAtDown = target;
                        if (getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        if (listener != null) listener.onStartTrackingTouch(this);
                    }
                    movedWhileDown = true;
                    final float want = posAtDown - (ev.getX() - downX) / pitch();
                    final int notch = Math.max(0, Math.min(kSteps, Math.round(want)));
                    if (notch != target) {
                        target = notch;
                        final long now = android.os.SystemClock.uptimeMillis();
                        final long dt = Math.max(1, now - lastMoveAt);
                        lastMoveAt = now;
                        selfDriven = true;
                        setProgress(Math.round(notch * getMax() / (float) kSteps));
                        selfDriven = false;
                        popVel = 1.8f;
                        final int dent = notch / kClickEvery;
                        if (dent != lastMajor) {
                            lastMajor = dent;
                            haptic(false);
                            if (ticker != null) ticker.play(focused ? Ticker.FINE : Ticker.LINE);
                        }
                    }
                    startAnim();
                    return true;
                }
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL: {
                    if (!tracking) return false;
                    handler.removeCallbacks(reveal);
                    tracking = false;
                    touching = false;
                    setPressed(false);
                    lastUpMs = android.os.SystemClock.uptimeMillis();
                    if (dragging) {
                        dragging = false;
                        if (listener != null) listener.onStopTrackingTouch(this);
                    }
                    // Their release: throw 150 ms of velocity, then spring.
                    final float projected = target + vel * PROJECT_MS;
                    final int landed = Math.max(0, Math.min(kSteps, Math.round(projected)));
                    if (landed != target) {
                        target = landed;
                        selfDriven = true;
                        setProgress(Math.round(landed * getMax() / (float) kSteps));
                        selfDriven = false;
                    }
                    if (movedWhileDown && ticker != null) ticker.play(Ticker.CLOSE);
                    focused = false;
                    // **Nothing may differ between a dial that was dragged and
                    // one that was not.** A focused view draws the theme's focus
                    // highlight -- an accent-coloured box around the row -- and
                    // that is what stayed behind.
                    if (isFocused()) clearFocus();
                    setSelected(false);
                    startAnim();
                    return true;
                }
                default:
                    return true;
            }
        }

        /** One tick of the motor: the system's own click, which is the only
         *  effect tuned for a linear actuator. Length is what made a hand-rolled
         *  pulse feel loose. */
        private void haptic(boolean strong) {
            final long now = android.os.SystemClock.uptimeMillis();
            if (now - lastHapticAt < (strong ? 90 : 65)) return;
            lastHapticAt = now;
            try {
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        vibrator.vibrate(android.os.VibrationEffect.createPredefined(
                                strong ? android.os.VibrationEffect.EFFECT_HEAVY_CLICK
                                       : android.os.VibrationEffect.EFFECT_CLICK));
                    } else if (android.os.Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(
                                strong ? 9 : 5, strong ? 200 : 150));
                    } else {
                        vibrator.vibrate(strong ? 9 : 5);
                    }
                    return;
                }
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1685", ignored); }
            try {
                performHapticFeedback(strong
                        ? android.view.HapticFeedbackConstants.LONG_PRESS
                        : android.view.HapticFeedbackConstants.CLOCK_TICK);
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:1690", ignored); }
        }

        // --- drawing ----------------------------------------------------------

        @Override protected void onDraw(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0 || getMax() <= 0) return;
            final float pitch = pitch();
            final float cx = pointerX();
            final float unit = kUnitDp * d;
            final float midY = h * 0.5f;

            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setShader(null);
            paint.setAlpha(0xFF);

            final int first = Math.max(0, (int) Math.floor(pos - cx / pitch) - 1);
            final int last = Math.min(kSteps, (int) Math.ceil(pos + cx / pitch) + 1);
            final boolean intro = introT >= 0f;
            final float half = Math.max(1f, w * 0.5f);

            // **The burst is every notch lighting up, not one wash across the
            // bar.** Each notch carries its own halo, sized to itself and as
            // bright as it is hot, so the light falls off notch by notch to the
            // left and right -- which is what a bar of small lamps looks like.
            // It is also cheaper: one shader, moved by the canvas.
            if (glowA > 0.004f) {
                if (haloShader == null || Math.abs(haloR - 24f * d) > 0.5f) {
                    haloR = 24f * d;
                    final int ink = blue & 0x00FFFFFF;
                    haloShader = new android.graphics.RadialGradient(0f, 0f, haloR,
                            new int[]{0x9A000000 | ink, 0x44000000 | ink, 0x00000000 | ink},
                            new float[]{0f, 0.5f, 1f},
                            android.graphics.Shader.TileMode.CLAMP);
                }
                paint.setStyle(android.graphics.Paint.Style.FILL);
                paint.setShader(haloShader);
                for (int i = first; i <= last; ++i) {
                    final float x = cx + (i - pos) * pitch;
                    if (x < -pitch || x > w + pitch) continue;
                    final float n = Math.abs(x - cx) / unit;
                    final float f = (float) Math.pow(Math.max(0f, 1f - Math.min(1f, n)), 1.5);
                    final float g = (float) Math.exp(-(n / 2.2f) * (n / 2.2f));
                    final float hot0 = Math.min(1f, 0.60f * f + 0.60f * g);
                    final float a = glowA * (0.10f + 0.90f * hot0);
                    if (a < 0.03f) continue;            // the tail is not worth a draw
                    final float m0 = Math.max(i % kMajor == 0 ? 1f : 0f, f);
                    final float th = Math.min(((6f + 3f * m0) + 1.5f + 4.5f * g + 10f * f) * d,
                            h - 2f * d);
                    final float sc = Math.max(0.35f, th * 0.62f) / haloR;
                    c.save();
                    c.translate(x, midY);
                    c.scale(sc, sc);
                    paint.setAlpha(Math.round(255f * a));
                    c.drawCircle(0f, 0f, haloR, paint);
                    c.restore();
                }
                paint.setShader(null);
                paint.setAlpha(0xFF);
            }
            for (int i = first; i <= last; ++i) {
                final float x = cx + (i - pos) * pitch;
                if (x < -pitch || x > w + pitch) continue;
                // **The entrance runs from the edges to the pointer.** A notch's
                // rank is its distance from the centre over half the bar, so the
                // outermost pair arrives first and the pair under the pointer
                // arrives last -- both sides at once, which is what makes it
                // read as two hands closing rather than a wipe.
                float pop = 0f;
                if (intro) {
                    final float rank = Math.min(1f, Math.abs(x - cx) / half);
                    final float tOn = 1f - rank;
                    if (introT < tOn) continue;
                    pop = Math.max(0f, 1f - (introT - tOn) / kHot);
                }
                final float n = Math.abs(x - cx) / unit;       // in 14 dp notches
                final float f = (float) Math.pow(Math.max(0f, 1f - Math.min(1f, n)), 1.5);
                final float g = (float) Math.exp(-(n / 2.2f) * (n / 2.2f));
                final boolean major = i % kMajor == 0;
                // **The notch under the pointer is the accent whatever its
                // index.** Major-ness used to be decided by `i % 4`, so a value
                // that happened to land on a multiple of four drew a full-height
                // tick at 100 % while every other value drew a minor one at
                // 72 % -- which is exactly "0 is brighter than everything else".
                final float m = Math.max(major ? 1f : 0f, f);
                // A notch that has only just landed is taller, wider and
                // bluer; `pop` falls to zero inside `kHot`, so the beat is
                // visible without anything being left lit afterwards.
                // **Never past the bar.** A notch taller than the view is cut
                // flat at the top and bottom, and a flat-topped notch is a
                // rectangle -- which is exactly what the one under the pointer
                // turned into while it was landing: 25 dp of tick in a 32 dp
                // bar, multiplied by 1.95, clipped to a slab.
                final float tickH = Math.min(
                        ((6f + 3f * m) + 1.5f + 4.5f * g + 10f * f) * d
                                * (1f + 0.50f * pop),
                        h - 2f * d);
                final float tickW = ((0.9f + 0.5f * m) + 1.2f * f) * d
                        * (1f + 0.28f * pop);
                final float lit = Math.max(
                        (0.72f + 0.28f * m) * Math.min(1f, 0.14f + 0.50f * f + 0.30f * g),
                        0.30f + 0.70f * pop);
                final float hot = Math.max(Math.min(1f, 0.60f * f + 0.60f * g), pop);
                paint.setColor(blend(muted, blue, hot));
                paint.setAlpha(Math.round(lit * 255f));
                rect.set(x - tickW, midY - tickH * 0.5f, x + tickW, midY + tickH * 0.5f);
                c.drawRoundRect(rect, tickW, tickW, paint);
            }
            paint.setAlpha(0xFF);

            if (!focused || format == null) return;
            paint.setTypeface(android.graphics.Typeface.create(
                    "sans-serif-condensed", android.graphics.Typeface.NORMAL));
            paint.setTextAlign(android.graphics.Paint.Align.CENTER);
            paint.setTextSize(8.5f * d);
            final float baseline = h - 2f * d;
            float lastRight = -1e9f;
            for (int i = first; i <= last; i += kMajor) {
                final float x = cx + (i - pos) * pitch;
                if (Math.abs(x - cx) > unit * 3.4f) continue;
                final String text = format.apply(Math.round(i * getMax() / (float) kSteps));
                if (text == null || text.isEmpty()) continue;
                final float tw = paint.measureText(text);
                if (x - tw * 0.5f < lastRight + 2.5f * d) continue;
                paint.setColor(Math.abs(x - cx) < pitch * kMajor * 0.5f ? ink : muted);
                c.drawText(text, x, baseline, paint);
                lastRight = x + tw * 0.5f;
            }
        }

        private static int blend(int from, int to, float t) {
            final float k = Math.max(0f, Math.min(1f, t));
            final int r = Math.round(((from >> 16) & 0xFF)
                    + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * k);
            final int g = Math.round(((from >> 8) & 0xFF)
                    + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * k);
            final int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * k);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
    }
