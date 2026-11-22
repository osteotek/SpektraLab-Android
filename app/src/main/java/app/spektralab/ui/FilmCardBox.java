package app.spektralab.ui;

/** Extracted from MainActivity (custom-view refactor). */
    public final class FilmCardBox extends android.widget.FrameLayout {
    public interface DualMark {
        void draw(android.graphics.Canvas c, int w, int h);
    }
        private boolean dualOn;
    private final long softMs;
    private final android.view.animation.Interpolator ease;
    private final DualMark dual;
        /**
         * The scale this card rests at -- 1.05 when it is the loaded film.
         *
         * <p>**A press has to be relative to this, not absolute.** The press
         * used to scale to a literal 0.97 and back to a literal 1.00; on a card
         * that is now held at 1.05 by the selection, pressing would have shrunk
         * it *below* its neighbours and releasing would have quietly dropped it
         * back to 1.00, undoing the selection size.
         */
        private float rest = 1f;
        public FilmCardBox(android.content.Context c, long softMs,
            android.view.animation.Interpolator ease, DualMark dual) {
        super(c);
        this.softMs = softMs; this.ease = ease; this.dual = dual;
    }
    public void setDual(boolean v) { dualOn = v; }

        /** Land on a new resting scale. Called by `updateFilmSelection`. */
        public void settle(float want) {
            rest = want;
            if (isPressed() || Math.abs(getScaleX() - want) <= 0.001f) return;
            animate().cancel();
            animate().scaleX(want).scaleY(want)
                    .setDuration(softMs)
                    .setInterpolator(ease)
                    .start();
        }

        /**
         * **Press-in, not press-out.** 100 ms down to 0.97 and 140 ms back --
         * the same vocabulary as every other pressable surface here, and short
         * enough that flicking through the shelf never sees it start.
         * `View` already calls this from its own touch handling, so nothing
         * else has to know the card exists.
         *
         * <p>Guarded on `isPressed() == pressed` because a scroll or a
         * `removeAllViews` re-sends the state the card is already in, and
         * restarting the animation there would make the shelf twitch under a
         * finger that is only scrolling past.
         */
        @Override public void setPressed(boolean pressed) {
            if (isPressed() == pressed) return;
            super.setPressed(pressed);
            animate().cancel();
            // 6 %, not 3 %: at 3 % a finger on a 86 dp card could not see the
            // card move at all. Press-in is quick and flat so the tap feels
            // answered, release eases back over 190 ms so it can be watched.
            final float to = pressed ? rest * 0.94f : rest;
            animate().scaleX(to).scaleY(to)
                    .setDuration(pressed ? 110 : 190)
                    .setInterpolator(ease)
                    .start();
        }
        @Override protected void dispatchDraw(android.graphics.Canvas c) {
            super.dispatchDraw(c);
            if (dualOn) dual.draw(c, getWidth(), getHeight());
        }
    }
