package app.spektralab.ui;

/** The paper-stock bar (extracted from MainActivity). */
    public final class PaperBarView extends android.view.View {
    private final PaperBarHost host;
        private final android.graphics.Paint paint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF full = new android.graphics.RectF();
        private final int body, band, bandInk, ink;
        private final String wordmark, name, note, process, tag;
        private final boolean pickable;

        public PaperBarView(PaperCover v, boolean pickable, PaperBarHost host,
            android.content.Context c) {
            super(c);
            this.host = host;
            body = v.body;
            band = v.band;
            bandInk = v.bandInk;
            ink = v.ink;
            wordmark = v.wordmark;
            name = v.name;
            process = v.process;
            note = v.note;
            tag = v.tag == null ? "" : v.tag;
            this.pickable = pickable;
            // Every rebuild of the strip is a new bar, so the entrance plays
            // whenever the sheet changes and on the first layout.
            post(this::startIntro);
        }

        // --- the entrance ---------------------------------------------------
        //
        // Three blocks -- the sheet, the maker's end panel, the stamp -- each
        // growing in from one of the four edges, chosen at random when the
        // intro starts. The text is typed once its block is mostly in.
        private float introT = 1f;                       // 1 = settled
        private int[] dirs = {0, 1, 2};
        private android.animation.ValueAnimator introAnim;
        private final android.graphics.RectF bodyRect = new android.graphics.RectF();
        private final android.graphics.RectF bandRect = new android.graphics.RectF();
        private final android.graphics.RectF stampRect = new android.graphics.RectF();
        private static final int MS_BAR = 780;

        public void startIntro() {
            if (introAnim != null) { introAnim.cancel(); introAnim = null; }
            // Settled is `introT = 1`: every block at its full size, the text
            // typed out and the arrow in. 0 would be the other end -- three
            // zero-area blocks and no text, which is a blank card, not a still
            // one.
            if (!host.arrivalAnim()) {
                introT = 1f;
                invalidate();
                return;
            }
            // **Three different edges, every time.** `nextInt(4)` three times can
            // land on the same edge twice (or three times), and three blocks
            // growing from the left reads as one wipe from the left -- which is
            // exactly tr("ui170.123"). A shuffle cannot repeat.
            final java.util.Random r = new java.util.Random(System.nanoTime()
                    ^ (long) System.identityHashCode(this));
            final int[] deck = {0, 1, 2, 3};
            for (int i = deck.length - 1; i > 0; --i) {
                final int j = r.nextInt(i + 1);
                final int tmp = deck[i]; deck[i] = deck[j]; deck[j] = tmp;
            }
            dirs = new int[]{deck[0], deck[1], deck[2]};
            host.markFile("paper dirs=" + dirs[0] + dirs[1] + dirs[2]
                    + " (0 left, 1 right, 2 top, 3 bottom)");
            introT = 0f;
            final android.animation.ValueAnimator a =
                    android.animation.ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(MS_BAR);
            a.setInterpolator(host.easeOut());
            a.addUpdateListener(an -> { introT = (Float) an.getAnimatedValue(); invalidate(); });
            a.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator x) {
                    introT = 1f;
                    introAnim = null;
                    invalidate();
                }
            });
            introAnim = a;
            a.start();
        }

        /** A block's own share of the run: 0 = not started, 1 = landed. */
        private float blockP(int i) {
            return Math.max(0f, Math.min(1f, (introT - i * 0.13f) / 0.55f));
        }

        /** The rect `l,t,r,b` grown out of the edge `dir` by `p`. */
        private void grown(android.graphics.RectF out, float l, float t, float r, float b,
                           int dir, float p) {
            final float k = Math.max(0f, Math.min(1f, p));
            switch (dir) {
                case 0:  out.set(l, t, l + (r - l) * k, b); break;    // in from the left
                case 1:  out.set(r - (r - l) * k, t, r, b); break;    // in from the right
                case 2:  out.set(l, t, r, t + (b - t) * k); break;    // in from the top
                default: out.set(l, b - (b - t) * k, r, b); break;    // in from the bottom
            }
        }

        /** How many characters of `s` are typed once `introT` passes `at`. */
        private int typed(String s, float at) {
            if (s == null || s.isEmpty()) return 0;
            final float k = Math.max(0f, Math.min(1f, (introT - at) / 0.45f));
            return Math.round(s.length() * k);
        }

        /** Where the word the typing head is inside starts. */
        private static int wordStart(String s, int n) {
            int i = Math.min(n, s.length());
            while (i > 0 && s.charAt(i - 1) != ' ') i--;
            return i;
        }

        /**
         * Draw the first `n` characters of `s`, with the word under the typing
         * head knocked out of a block -- the same read as the panel's labels.
         */
        private void typedText(android.graphics.Canvas c, String s, float x, float baseline,
                               int n, boolean invert, int block, int onBlock) {
            if (s == null || s.isEmpty()) return;
            final int k = Math.max(0, Math.min(n, s.length()));
            if (k == 0) return;
            final int head = invert ? wordStart(s, k) : k;
            final int was = paint.getColor();
            if (head > 0) c.drawText(s.substring(0, head), x, baseline, paint);
            if (head < k) {
                final float fx = x + paint.measureText(s.substring(0, head));
                final float tx = fx + paint.measureText(s.substring(head, k));
                paint.setColor(block);
                c.drawRect(fx, baseline + paint.ascent(), tx, baseline + paint.descent(), paint);
                paint.setColor(onBlock);
                c.drawText(s.substring(head, k), fx, baseline, paint);
                paint.setColor(was);
            }
        }

        /**
         * The card's caption face.
         *
         * <p>It used to be the platform's `sans-serif-condensed`, which is
         * whatever the OEM shipped: on one phone the wordmark measures 4% wider
         * than on the next and the fitted text ends somewhere else. Inter's real
         * 700 instance, squeezed by the same 0.88 on every device, is the one
         * thing this caption can be.
         */
        private void condense(android.graphics.Paint p) {
            p.setTypeface(host.boldFace() != null ? host.boldFace() : host.bodyFace());
            p.setTextScaleX(0.88f);
        }

        /** As much of `text` as fits, with an ellipsis: a canvas has no ellipsize. */
        private String fit(String text, float room) {
            if (text == null) return "";
            if (paint.measureText(text) <= room) return text;
            int n = text.length();
            while (n > 1 && paint.measureText(text.substring(0, n) + "…") > room) n--;
            return text.substring(0, n) + "…";
        }

        @Override protected void onDraw(android.graphics.Canvas c) {
            final int w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            final float d = getResources().getDisplayMetrics().density;

            // The maker's block: the end panel of a box, not a colour chip.
            final float blockW = Math.min(104f * d, Math.max(74f * d, w * 0.235f));
            final float top = h * 0.41f;
            final float bottom = h * 0.80f;

            final boolean intro = introT < 0.999f;
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setShader(null);
            grown(bodyRect, 0, 0, w, h, dirs[0], intro ? blockP(0) : 1f);
            paint.setColor(body);
            c.drawRect(bodyRect, paint);
            grown(bandRect, 0, 0, blockW, h, dirs[1], intro ? blockP(1) : 1f);
            paint.setColor(band);
            c.drawRect(bandRect, paint);

            // --- the block: who made it, then what it is for --------------
            condense(paint);
            paint.setColor(bandInk);
            paint.setTextAlign(android.graphics.Paint.Align.LEFT);
            final float bx = 9f * d;
            final float blockRoom = Math.max(20f * d, blockW - bx - 6f * d);
            // Clipped to its own block: text cannot show outside the panel that
            // is still growing in.
            c.save();
            c.clipRect(bandRect);
            if (!wordmark.isEmpty()) {
                paint.setTypeface(host.faceFor(wordmark));
                paint.setTextScaleX(0.88f);
                paint.setTextSize(7.0f * d);
                final String wm = fit(wordmark, blockRoom);
                typedText(c, wm, bx, top, typed(wm, 0.34f), intro, bandInk, band);
                paint.setTypeface(host.faceFor(process));
                paint.setTextScaleX(1f);
                paint.setTextSize(5.2f * d);
                paint.setAlpha(0xB8);
                final String pr = fit(process, blockRoom);
                typedText(c, pr, bx, bottom, typed(pr, 0.40f), false, bandInk, band);
                paint.setAlpha(0xFF);
            } else {
                paint.setTypeface(host.faceFor(process));
                paint.setTextScaleX(1f);
                paint.setTextSize(6.4f * d);
                final String pr = fit(process, blockRoom);
                typedText(c, pr, bx, h * 0.5f + 2.3f * d, typed(pr, 0.34f), false, bandInk, band);
            }
            c.restore();

            // --- the sheet itself: name, character, arrow -----------------
            final float x0 = blockW + 10f * d;
            final float right = w - 9f * d - (pickable ? 14f * d : 0f);
            // The stamp is measured before the name so the name can be fitted to
            // what is left -- the same reason every other line here is fitted.
            float tagW = 0f;
            if (!tag.isEmpty()) {
                paint.setTypeface(host.boldFace() != null ? host.boldFace() : host.bodyFace());
                paint.setTextScaleX(1f);
                paint.setTextSize(6.2f * d);
                tagW = paint.measureText(tag) + 9f * d;
            }
            final float room = Math.max(24f * d, right - x0 - (tagW > 0f ? tagW + 6f * d : 0f));

            condense(paint);
            paint.setColor(ink);
            // **Its own face, spelled out.** Every other line in this bar sets a
            // typeface before it draws; the sheet's name did not, so it came out
            // in whatever the paint still held from the line above -- on a card
            // without a maker block, the ROM's own font.
            paint.setTypeface(host.bodyFace());
            paint.setTextScaleX(1f);
            paint.setTypeface(host.faceFor(name));
            paint.setTextScaleX(1f);
            paint.setTextSize(10.5f * d);
            final String nm = fit(name, room);
            c.save();
            c.clipRect(bodyRect);
            typedText(c, nm, x0, top + 0.8f * d, typed(nm, 0.42f), intro, ink, body);
            c.restore();

            if (!note.isEmpty()) {
                paint.setTypeface(host.hanFace());
                paint.setTextSize(7.0f * d);
                paint.setAlpha(pickable ? 0x99 : 0x66);
                final String nt = fit(note, room);
                c.save();
                c.clipRect(bodyRect);
                typedText(c, nt, x0, bottom, typed(nt, 0.52f), false, ink, body);
                c.restore();
                paint.setAlpha(0xFF);
            }

            // --- the stamp -------------------------------------------------
            // Rectangular and knocked out of the medium's own band, so it reads
            // as pressed into the box rather than as a badge laid on top. The
            // band already carries the maker's colour; the stamp reuses it.
            if (tagW > 0f) {
                final float th = 12.2f * d;
                final float tl = right - tagW;
                final float tt = top - 9.4f * d;
                paint.setStyle(android.graphics.Paint.Style.FILL);
                paint.setShader(null);
                paint.setAlpha(0xFF);
                grown(stampRect, tl, tt, right, tt + th, dirs[2], intro ? blockP(2) : 1f);
                paint.setColor(band);
                c.drawRect(stampRect, paint);
                paint.setColor(bandInk);
                paint.setTypeface(host.boldFace() != null ? host.boldFace() : host.bodyFace());
                paint.setTextScaleX(1f);
                paint.setTextSize(6.2f * d);
                paint.setTextAlign(android.graphics.Paint.Align.CENTER);
                c.save();
                c.clipRect(stampRect);
                c.drawText(tag, tl + tagW / 2f, tt + th * 0.745f, paint);
                c.restore();
                paint.setTextAlign(android.graphics.Paint.Align.LEFT);
            }

            if (pickable) {
                paint.setTypeface(host.bodyFace());
                paint.setTextScaleX(1f);
                paint.setTextAlign(android.graphics.Paint.Align.RIGHT);
                paint.setColor(host.blue());
                paint.setTextSize(11f * d);
                paint.setAlpha(Math.round(255f * Math.max(0f, Math.min(1f,
                        (introT - 0.72f) / 0.28f))));
                c.drawText("↓", w - 8f * d, h * 0.5f + 3.8f * d, paint);
                paint.setAlpha(0xFF);
            }

            // One hairline, square: the bar has to keep an edge on paper colour.
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(0x33000000);
            full.set(0.5f, 0.5f, w - 0.5f, h - 0.5f);
            c.drawRect(full, paint);
        }
    }
