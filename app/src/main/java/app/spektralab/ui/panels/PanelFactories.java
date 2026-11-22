package app.spektralab.ui.panels;
import app.spektralab.ui.HistogramView;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.MainActivity.Frame;
import app.spektralab.Look;
import app.spektralab.MainActivity.CappedScroll;
import app.spektralab.MainActivity.Knob;
import app.spektralab.MainActivity.LiveClip;
import app.spektralab.ui.*;

/** The seven 1.7.0 panel builders, extracted from MainActivity in
 * architecture Phase C. Each is a function of its narrow PanelHost. */
public final class PanelFactories {
    private PanelFactories() {}

    public static View buildFilmPanel(PanelHost host) {
        LinearLayout panel = new LinearLayout(host.context());
        panel.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout filmHead = host.sectionHeadHostCall(tr("film.film_paper"), host::resetFilmPanelHostCall);
        // **The "As Shot" switch, where the original app puts the same choice.**
        // On: the meter decides the film exposure. Off: the frame keeps the
        // exposure the camera recorded, which is the only way a lamp in a dark
        // room comes back dark instead of being lifted five stops onto Dmax.
        final TextView meterLabel = host.labelHostCall(tr("film.metering"), 8.5f, host.getUI_MUTED(), host.getTfHan());
        host.setMeterChip(host.labelHostCall("", 8.5f, host.getUI_INK(), host.getTfHan()));
        host.getMeterChip().setGravity(android.view.Gravity.CENTER);
        // **48dp, not 40x20.** The old chip was smaller than the minimum touch
        // target, so a tap that landed two pixels off did nothing -- which is
        // what tr("ui170.089") was.
        host.getMeterChip().setPadding(host.dpHostCall(10), host.dpHostCall(6), host.dpHostCall(10), host.dpHostCall(6));
        host.getMeterChip().setMinWidth(host.dpHostCall(48));
        host.getMeterChip().setMinHeight(host.dpHostCall(30));
        host.getMeterChip().setOnClickListener(v -> host.showMeterPickerHostCall());
        final LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(-2, -2);
        labelLp.rightMargin = host.dpHostCall(6);
        filmHead.addView(meterLabel, 1, labelLp);
        final LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, -2);
        chipLp.rightMargin = host.dpHostCall(10);
        filmHead.addView(host.getMeterChip(), 2, chipLp);
        host.refreshMeterChipHostCall();
        panel.addView(filmHead, new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        HorizontalScrollView brandScroll = new HorizontalScrollView(host.context());
        brandScroll.setHorizontalScrollBarEnabled(false);
        brandScroll.setFillViewport(false);
        host.setBrandRow(new LinearLayout(host.context()));
        host.getBrandRow().setOrientation(LinearLayout.HORIZONTAL);
        host.getBrandRow().setGravity(android.view.Gravity.CENTER_VERTICAL);
        brandScroll.addView(host.getBrandRow(), new android.widget.FrameLayout.LayoutParams(-2, host.dpHostCall(38)));
        panel.addView(brandScroll, new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        host.setFilmScroll(new HorizontalScrollView(host.context()));
        host.getFilmScroll().setHorizontalScrollBarEnabled(false);
        host.getFilmScroll().setClipToPadding(false);
        host.getFilmScroll().setFillViewport(false);
        host.setFilmCardStrip(new LinearLayout(host.context()));
        host.getFilmCardStrip().setOrientation(LinearLayout.HORIZONTAL);
        host.getFilmCardStrip().setGravity(android.view.Gravity.TOP);
        host.getFilmCardStrip().setPadding(0, host.dpHostCall(9), 0, host.dpHostCall(8));
        host.getFilmScroll().addView(host.getFilmCardStrip(), new android.widget.FrameLayout.LayoutParams(-2, -1));
        // **One host, two shapes.** Portrait holds the horizontal scroller,
        // landscape holds the same strip as a two-column grid; `applyFilmShelfMode`
        // hands the strip over when the phone turns.
        host.setFilmShelfHost(new LinearLayout(host.context()));
        host.getFilmShelfHost().setOrientation(LinearLayout.VERTICAL);
        host.getFilmShelfHost().addView(host.getFilmScroll(), new LinearLayout.LayoutParams(-1, -1));
        panel.addView(host.getFilmShelfHost(), new LinearLayout.LayoutParams(-1, host.dpHostCall(111)));

        // **One bar, and a list behind it.** The shelf of paper boxes was the
        // wrong shape for this row: two or three boxes fit, the rest needed a
        // sideways swipe, and what a paper row has to answer is only "which
        // sheet is in it". The bar states that on one line and opens the list
        // -- with the notes and the refusals -- on a tap.
        LinearLayout paperRow = new LinearLayout(host.context());
        paperRow.setOrientation(LinearLayout.HORIZONTAL);
        paperRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        final TextView paperCaption = host.labelHostCall(tr("film.paper_print_film"), 7.5f, host.getUI_MUTED(), host.getTfHan());
        paperCaption.setSingleLine(true);
        // The caption is the only way back to the full list, and a 10 dp line of
        // text is not a target: padding makes it 26 dp tall without growing the
        // row.
        paperCaption.setPadding(0, host.dpHostCall(8), 0, host.dpHostCall(8));
        // Long-press keeps the old list, notes and all. The strip carries only
        // the sheets this film can take; "why can't I use the silver paper" is a
        // question the list answers in one line.
        paperCaption.setOnLongClickListener(v -> { host.showPaperPickerHostCall(); return true; });
        LinearLayout.LayoutParams paperCapLp = new LinearLayout.LayoutParams(-2, -2);
        paperCapLp.rightMargin = host.dpHostCall(8);
        paperRow.addView(paperCaption, paperCapLp);

        host.setPaperStrip(new LinearLayout(host.context()));
        host.getPaperStrip().setOrientation(LinearLayout.HORIZONTAL);
        host.getPaperStrip().setGravity(android.view.Gravity.CENTER_VERTICAL);
        paperRow.addView(host.getPaperStrip(), new LinearLayout.LayoutParams(0, host.dpHostCall(35), 1f));
        host.setFilmPaperRow(paperRow);
        panel.addView(paperRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(35)));
        host.rebuildPaperCardsHostCall();
        return panel;
    }

    public static LinearLayout buildViewPanel(PanelHost host) {
        final LinearLayout panel = new LinearLayout(host.context());
        host.setViewRoot(panel);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.addView(host.sectionHeadHostCall(tr("optics.optics"), host::resetViewPanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));
        // **Say why, instead of letting a live page do nothing.**
        //
        // The optics layer is applied to the engine's *output* -- `grade_result`
        // on export, `copyArgbRange` on the canvas -- so with no film there is
        // nothing for it to sit on: the sliders moved and the picture did not.
        // A dead page with no explanation is what made a user ask for a "no
        // film mode"; he read the silence as a missing feature. This line is the
        // cheap half of that answer.
        host.setViewLockNote(host.labelHostCall(tr("ui170.144"),
                8.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getViewLockNote().setPadding(host.dpHostCall(2), host.dpHostCall(4), host.dpHostCall(2), host.dpHostCall(8));
        panel.addView(host.getViewLockNote(), new LinearLayout.LayoutParams(-1, -2));

        // The six sub-pages live in the strip above the tool bar now.
        host.setViewCatChips(null);

        for (int i = 0; i < host.getViewPages().length; ++i) {
            host.getViewPages()[i] = new LinearLayout(host.context());
            host.getViewPages()[i].setOrientation(LinearLayout.VERTICAL);
            host.getViewPages()[i].setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            final CappedScroll scroll = new CappedScroll(host.context(), host.dpHostCall(280));
            scroll.addView(host.getViewPages()[i], new android.widget.FrameLayout.LayoutParams(-1, -2));
            panel.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
            // The scroll has to hide with its page, so the page list is the
            // one that gets toggled and the scroll is kept beside it.
            host.getViewScrolls()[i] = scroll;
        }
        host.buildTonePageHostCall(host.getViewPages()[0]);
        host.buildColourPageHostCall(host.getViewPages()[1]);
        host.buildEffectsPageHostCall(host.getViewPages()[2]);
        host.buildDetailPageHostCall(host.getViewPages()[3]);
        host.buildCurvesPageHostCall(host.getViewPages()[4]);
        host.buildGradingPageHostCall(host.getViewPages()[5]);
        host.buildHslPageHostCall(host.getViewPages()[6]);

        for (int i = 0; i < host.getCurvePoints().length; ++i) {
            host.getCurvePoints()[i] = new java.util.ArrayList<>();
            host.getCurvePoints()[i].add(new float[]{0f, 0f});
            host.getCurvePoints()[i].add(new float[]{1f, 1f});
        }
        // **Locked from the first frame.** An import lands with no film, so the
        // page has nothing to act on; leaving it bright until the first render
        // would teach the opposite of what the note says.
        host.refreshViewLockHostCall();
        return panel;
    }

    public static LinearLayout buildOpticsPanel(PanelHost host) {
        LinearLayout panel = new LinearLayout(host.context());
        // **The field the lock reads.** Without this line `setOpticsUnlocked`
        // hit `if (opticsPanel == null) return;` every single time and the page
        // was never dimmed -- which is why tr("ui170.179") survived a
        // version that was supposed to fix it.
        host.setOpticsPanel(panel);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.addView(host.sectionHeadHostCall(tr("print.print_settings"), host::resetOpticsPanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        // **One page per stage, and the same seven the second engine uses.**
        // This page used to be a single slider over a strip of twenty-two chips:
        // the parameter you wanted was a horizontal scroll away, the one you
        // were on was named in a readout above the bar instead of in the row you
        // were touching, and nothing said which stage of the chain any of them
        // belonged to. The knobs below are registered first -- they are the
        // page's vocabulary -- and `showPrintGroup` builds the rows from them.
        host.getKnobs().clear();

        // **Log travel.** 0.25 / 1.0 / 4.0 are equal stops, and 1.0 is the
        // neutral: on a linear bar it sat at 20 %, which is the tr("ui170.180") half
        // only. With a ratio scale the default is the middle of the track.
        host.knobHostCall(tr("print.knob.enlarger_exposure_right_is_darker"), "E",  0.25f, 4.0f,   v -> host.setPrintExposure(v),   () -> host.getPrintExposure(), false, true);
        host.knobHostCall(tr("print.knob.highlight_protection"),   "H",  0.0f,  3.0f,   v -> host.setHighlightRoom(v),   () -> host.getHighlightRoom(), true);
        host.knobHostCall(tr("print.knob.shadow_protection"),   "S",  0.0f,  3.0f,   v -> host.setShadowRoom(v),      () -> host.getShadowRoom(), true);
        // **The same shoulder, by hand.** These are not a new curve -- they are
        // the three numbers the fit solves (`latitude_fit`), exposed so a frame
        // the histogram reads badly can be told where the highlights are. The
        // competitor's 「高光提升」 trio is exactly this: threshold, range,
        // strength. Ours acts before the film, so the halo follows it too.
        host.knobHostCall(tr("ui170.138"), "M", 0.0f, 1.0f, v -> host.setHlManual(v > 0.5f), () -> host.getHlManual() ? 1f : 0f, true);
        // **Touching one takes the curve over.** A slider that reads a number
        // the renderer is ignoring is a lie; these three flip the mode the
        // moment they are moved, and the switch above is the way back to auto.
        host.knobHostCall(tr("ui170.139"), "T", 0.0f,  6.0f,   v -> { host.setHlKnee(v); host.setHlManual(true); },    () -> host.getHlKnee(), true);
        host.knobHostCall(tr("ui170.140"), "R", 1.0f,  4.0f,   v -> { host.setHlRolloff(v); host.setHlManual(true); }, () -> host.getHlRolloff(), true);
        host.knobHostCall(tr("ui170.141"), "K", 0.0f,  6.0f,   v -> { host.setHlRoom(v); host.setHlManual(true); },    () -> host.getHlRoom(), true);
                // The darkroom's own two moves, as numbers: where on the print,
                // how big, and how many stops. They live in the view layer, so
                // they cost a re-read of the frame, not a re-develop.
                host.knobHostCall(tr("ui170.181"), "LX", 0.0f, 1.0f, v -> host.setSpotX(v), () -> host.getSpotX(), false);
                host.knobHostCall(tr("ui170.182"), "LY", 0.0f, 1.0f, v -> host.setSpotY(v), () -> host.getSpotY(), false);
                host.knobHostCall(tr("ui170.183"),   "LR", 0.05f, 0.60f, v -> host.setSpotR(v), () -> host.getSpotR(), false);
                host.knobHostCall(tr("ui170.184"),   "LE", -2.0f, 2.0f, v -> host.setSpotEv(v), () -> host.getSpotEv(), false);
        host.knobHostCall(tr("print.knob.grain"),       "G",  0.0f,  2.0f,   v -> host.setGrainAmount(v),     () -> host.getGrainAmount(), true);
        host.knobHostCall(tr("print.knob.halation"),       "○",  0.0f,  4.0f,   v -> host.setHalationAmount(v),  () -> host.getHalationAmount(), true);
        host.knobHostCall(tr("print.knob.halation_boost"),   "+",  0.0f,  8.0f,   v -> host.setHalationBoost(v),   () -> host.getHalationBoost(), true);
        host.knobHostCall(tr("print.knob.emulsion_scatter"),   "~",  0.0f,  1.0f,   v -> host.setHalationScatter(v), () -> host.getHalationScatter(), true);
        host.knobHostCall(tr("print.knob.scatter_radius"),   "◍",  0.0f,  1.0f,   v -> host.setScatterRadius(v),  () -> host.getScatterRadius(), true);
        host.knobHostCall(tr("print.knob.glare"),       "✳",  0.0f,  30.0f,  v -> host.setGlareAmount(v),     () -> host.getGlareAmount());
        host.knobHostCall(tr("print.knob.lens_blur"),   "◌",  0.0f,  200.0f, v -> host.setLensBlur(v),        () -> host.getLensBlur(), true);
        host.knobHostCall(tr("print.knob.yellow_filter"),     "Y",  -1.0f, 1.0f,   v -> host.setYFilter(v),         () -> host.getYFilter());
        host.knobHostCall(tr("print.knob.magenta_filter"),   "M",  -1.0f, 1.0f,   v -> host.setMFilter(v),         () -> host.getMFilter());
        host.knobHostCall(tr("print.knob.filter_strength"),   "C",  1.0f,  60.0f,  v -> host.setFilterScale(v),     () -> host.getFilterScale());

        host.knobHostCall(tr("print.knob.pre_flash"),       "⚡", 0.0f,  1.0f,   v -> host.setPreflash(v),        () -> host.getPreflash());
        host.knobHostCall(tr("print.knob.curve_softness"),   "∿",  1.0f,  4.0f,   v -> host.setShadowRolloff(v),   () -> host.getShadowRolloff(), true);
        host.knobHostCall(tr("print.knob.maximum_shadow_lift"),   "L",  0.25f, 12.0f,  v -> host.setMaxLift(v),         () -> host.getMaxLift(), true);
        // The switches, on a 0/1 slider: the panel has one slider by design,
        // and a second kind of control would be a second thing to explain.
        host.knobHostCall(tr("print.knob.enable_halation"),   "◐",  0.0f,  1.0f,   v -> host.setHalationOn(v),       () -> host.getHalationOn(), true);
        host.knobHostCall(tr("print.knob.remove_anti_halation"),   "⊘",  0.0f,  1.0f,   v -> host.setAntihalationRemoved(v), () -> host.getAntihalationRemoved(), true);
        host.knobHostCall(tr("print.knob.enable_grain"),   "g",  0.0f,  1.0f,   v -> host.setGrainOn(v),          () -> host.getGrainOn(), true);
        host.knobHostCall(tr("print.knob.interlayer_inhibition"),   "⧉",  0.0f,  2.0f,   v -> host.setCouplersOn(v),       () -> host.getCouplersOn(), true);
        host.knobHostCall(tr("print.knob.scan_whites"),     "W",  0.0f,  1.0f,   v -> host.setScannerWhite(v),     () -> host.getScannerWhite());
        host.knobHostCall(tr("print.knob.scan_blacks"),     "B",  0.0f,  1.0f,   v -> host.setScannerBlack(v),     () -> host.getScannerBlack());
        // Six more that the schema has had all along and the panel never
        // showed. Booleans ride a 0..1 knob because the knob is the panel's
        // one control; the readout says "0.00" / "1.00" and the hint says
        // which end is which.
        host.knobHostCall(tr("print.knob.enable_glare"),   "☀",  0.0f,  1.0f,   v -> host.setGlareActive(v),      () -> host.getGlareActive());
        host.knobHostCall(tr("print.knob.scanner_blur"), "S",  0.0f, 20.0f,   v -> host.setScannerLensBlur(v),  () -> host.getScannerLensBlur());
        host.knobHostCall(tr("print.knob.halation_headroom"),   "R",  0.0f,  8.0f,   v -> host.setHalationHeadroom(v), () -> host.getHalationHeadroom(), true);
        host.knobHostCall(tr("print.knob.density_curve"),   "γ",  0.2f,  4.0f,   v -> host.setDensityGamma(v),     () -> host.getDensityGamma(), true);
        host.knobHostCall(tr("ui170.185"), "I", 0.0f, 1.0f, v -> host.setDiBlueComp(v > 0.5f),
             () -> host.getDiBlueComp() ? 1f : 0f, true);
        host.knobHostCall(tr("print.knob.digital_intermediate"), "I",  0.0f,  1.0f,   v -> host.setDigitalIntermediate(v > 0.5f),
                                                                    () -> host.getDigitalIntermediate() ? 1f : 0f);
        host.knobHostCall(tr("print.knob.output_encoding"),   "E",  0.0f,  1.0f,   v -> host.setOutputCctf(v > 0.5f),
                                                                    () -> host.getOutputCctf() ? 1f : 0f);


        // The six controls that are not knobs are rows on the page they belong
        // to now: 配光 / 保护基准 / EDR with the tone, 胶片色相 with the colour,
        // 镜头滤镜 with the lens, 预览色域 with the sheet.
        host.setPrintBody(new LinearLayout(host.context()));
        host.getPrintBody().setOrientation(LinearLayout.VERTICAL);
        // **The same 250 dp cap the other engine's print panel has**, so a frame
        // that changes engines does not move the photograph.
        final CappedScroll printScroll = new CappedScroll(host.context(), host.dpHostCall(250));
        printScroll.addView(host.getPrintBody(), new android.widget.FrameLayout.LayoutParams(-1, -2));
        panel.addView(printScroll, new LinearLayout.LayoutParams(-1, -2));
        host.showPrintGroupHostCall(0);
        return panel;
    }

    public static LinearLayout buildComposePanel(PanelHost host) {
        LinearLayout panel = new LinearLayout(host.context());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.addView(host.sectionHeadHostCall(tr("compose.composition"), host::resetComposePanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        LinearLayout sub = new LinearLayout(host.context());
        sub.setOrientation(LinearLayout.HORIZONTAL);
        host.setComposeCropTab(host.composeTabViewHostCall(tr("compose.crop"), 0));
        host.setComposeRotateTab(host.composeTabViewHostCall(tr("compose.rotate"), 1));
        sub.addView(host.getComposeCropTab(), new LinearLayout.LayoutParams(0, host.dpHostCall(32), 1f));
        sub.addView(host.getComposeRotateTab(), new LinearLayout.LayoutParams(0, host.dpHostCall(32), 1f));
        panel.addView(sub, new LinearLayout.LayoutParams(-1, host.dpHostCall(32)));

        host.setComposeBody(new LinearLayout(host.context()));
        host.getComposeBody().setOrientation(LinearLayout.VERTICAL);
        panel.addView(host.getComposeBody(), new LinearLayout.LayoutParams(-1, -2));
        host.showComposePageHostCall(0);      // the page is built hidden; it must still have content
        return panel;
    }

    public static LinearLayout buildFormatPanel(PanelHost host) {
        LinearLayout panel = new LinearLayout(host.context());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.addView(host.sectionHeadHostCall(tr("border.frame_scale"), host::resetFormatPanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));
        HorizontalScrollView scroll = new HorizontalScrollView(host.context());
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        host.setFormatCardStrip(new LinearLayout(host.context()));
        host.getFormatCardStrip().setOrientation(LinearLayout.HORIZONTAL);
        host.getFormatCardStrip().setPadding(0, host.dpHostCall(10), 0, 0);
        host.setFormatScroll(scroll);
        scroll.addView(host.getFormatCardStrip(), new android.widget.FrameLayout.LayoutParams(-2, host.dpHostCall(127)));
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, host.dpHostCall(127)));

        LinearLayout foot = new LinearLayout(host.context());
        foot.setOrientation(LinearLayout.HORIZONTAL);
        foot.setGravity(android.view.Gravity.CENTER_VERTICAL);
        host.setFormatFootLabel(host.labelHostCall(tr("border.physical_film_size_crop_and"), 8.5f, host.getUI_MUTED(), host.getTfHan()));
        foot.addView(host.getFormatFootLabel(), new LinearLayout.LayoutParams(0, -2, 1f));
        host.setFormatFootValue(host.labelHostCall("36 mm", 9, host.getUI_BLUE(), host.getTfBody()));
        host.getFormatFootValue().setFontFeatureSettings("tnum");   // live number, not typed
        foot.addView(host.getFormatFootValue());
        panel.addView(foot, new LinearLayout.LayoutParams(-1, host.dpHostCall(28)));

        host.rebuildFormatCardsHostCall();
        return panel;
    }

    public static LinearLayout buildEdgePanel(PanelHost host) {
        host.setEdgeRoot(new LinearLayout(host.context()));
        host.getEdgeRoot().setOrientation(LinearLayout.VERTICAL);

        // Two big cards. A card is a switch, a one-line summary, and a door
        // into its own page -- not fifteen controls at once.
        LinearLayout home = new LinearLayout(host.context());
        home.setOrientation(LinearLayout.VERTICAL);
        home.addView(host.sectionHeadHostCall(tr("border.frame_imprint"), host::resetEdgePanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        HorizontalScrollView cardScroll = new HorizontalScrollView(host.context());
        cardScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout cardStrip = new LinearLayout(host.context());
        cardStrip.setOrientation(LinearLayout.HORIZONTAL);
        cardStrip.addView(host.bigCardHostCall(tr("border.film_border"), "REBATE", host::edgeSummaryHostCall, () -> host.openEdgePageHostCall(0)),
                host.cardLpHostCall());
        cardStrip.addView(host.bigCardHostCall(tr("border.date_stamp"), "DATE BACK", host::dateSummaryHostCall, () -> host.openEdgePageHostCall(1)),
                host.cardLpHostCall());
        cardStrip.addView(host.bigCardHostCall(tr("ui170.226"), "PRINT FRAME", host::pfSummaryHostCall, () -> host.openEdgePageHostCall(2)),
                host.cardLpHostCall());
        cardScroll.addView(cardStrip, new android.widget.FrameLayout.LayoutParams(-2, -2));
        home.addView(cardScroll, new LinearLayout.LayoutParams(-1, -2));

        host.getEdgeRoot().addView(home, new LinearLayout.LayoutParams(-1, -2));
        host.buildEdgePagesHostCall();
        host.refreshEdgeGroupsHostCall();
        return host.getEdgeRoot();
    }

    public static View buildFotuPanel(PanelHost host) {
        final LinearLayout panel = new LinearLayout(host.context());
        panel.setOrientation(LinearLayout.VERTICAL);
        try {
            buildFotuPanelInto(host, panel);
        } catch (Throwable t) {
            // Same rule as the pages: this is `onCreate`, and an exception here
            // is an app that will not open.
            panel.removeAllViews();
            panel.addView(host.labelHostCall(tr("ui170.472") + t, 8f, host.getUI_INK(), host.getTfHan()),
                    new LinearLayout.LayoutParams(-1, host.dpHostCall(34)));
            try {
                DebugSink.text("fotupanel", "build: " + t + "\n"
                        + android.util.Log.getStackTraceString(t));
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:33630", ignored); }
        }
        return panel;
    }

    public static void buildFotuPanelInto(PanelHost host, LinearLayout panel) {
        panel.addView(host.sectionHeadHostCall(tr("ui170.473"), host::resetFotuPanelHostCall),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        // The seven pages are the strip above the tool bar now, not a row of
        // words inside this panel: they were sitting between the heading and the
        // sliders, i.e. in the middle of the page they switch away from.
        host.setFotuGroupTabs(null);

        host.setFotuBody(new LinearLayout(host.context()));
        host.getFotuBody().setOrientation(LinearLayout.VERTICAL);
        // **The cap is what the eight long pages have in common, and it is why
        // 影调 -> 色彩 does not change height: both want more room than the box
        // gives, so both sit at the cap.** The pages that *do* differ are the
        // short ones -- 胶片 is three rows (156 dp), 镜头 four (208 dp) -- and
        // those were the jumps nobody could see: the animation was travelling
        // `fotuBody`'s own height, which is hidden behind this cap. Raising the
        // cap buys nothing (光晕 at 314 dp against its own 312 is two pixels) and
        // costs the photograph 60 dp of height, so it stays at 250.
        final CappedScroll scroll = new CappedScroll(host.context(), host.dpHostCall(250));
        scroll.addView(host.getFotuBody(), new android.widget.FrameLayout.LayoutParams(-1, -2));
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, -2));

        final TextView note = host.labelHostCall(tr("ui170.474")
                + tr("ui170.475"), 7.5f, host.getUI_MUTED(), host.getTfHan());
        note.setPadding(0, host.dpHostCall(4), 0, host.dpHostCall(6));
        panel.addView(note);

        host.showFotuGroupHostCall(0);
    }
}
