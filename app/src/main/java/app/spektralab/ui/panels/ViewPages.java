package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.MainActivity.Square;

/** ViewPages builders extracted from MainActivity (architecture Phase C). */
public final class ViewPages {
    private ViewPages() {}

    public static void buildWhiteBalancePage(ViewPagesHost host, LinearLayout page) {
        // **The decode half only exists for a RAW.** LibRaw applies the
        // balance while demosaicing, so a JPEG has no negative to re-develop:
        // the chips and both Kelvin sliders did nothing at all on `xxx.jpg`,
        // which reads as tr("ui170.147"). The box is kept so the page can grey
        // it out and say why once the file is known.
        host.setWbDecodeBox(new LinearLayout(host.context()));
        host.getWbDecodeBox().setOrientation(LinearLayout.VERTICAL);
        page.addView(host.getWbDecodeBox(), new LinearLayout.LayoutParams(-1, -2));
        host.getWbDecodeBox().addView(host.labelHostCall(tr("optics.raw_white_balance"), 8, host.getUI_INK(), host.getTfHan()),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(22)));
        final String[] names = {tr("optics.as_shot"), tr("optics.daylight"), tr("optics.cloudy"), tr("optics.shade"), tr("optics.tungsten"), tr("optics.fluorescent"), tr("optics.custom")};
        final LinearLayout row = new LinearLayout(host.context());
        row.setOrientation(LinearLayout.HORIZONTAL);
        final HorizontalScrollView scroll = new HorizontalScrollView(host.context());
        scroll.setHorizontalScrollBarEnabled(false);
        final TextView[] chips = new TextView[names.length];
        for (int i = 0; i < names.length; ++i) {
            final int preset = i;
            final TextView chip = host.labelHostCall(names[i], 8, i == 0 ? host.getUI_BLUE() : host.getUI_MUTED(), host.getTfBody());
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setBackground(host.outlineBoxHostCall(i == 0));
            chip.setOnClickListener(v -> {
                host.setWbPreset(preset);
                host.refreshWbChipsHostCall();
                host.redecodeWithWhiteBalanceHostCall();
            });
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(host.dpHostCall(58), host.dpHostCall(30));
            lp.rightMargin = host.dpHostCall(4);
            row.addView(chip, lp);
            chips[i] = chip;
        }
        host.setWbChips(chips);
        scroll.addView(row, new android.widget.FrameLayout.LayoutParams(-2, -1));
        host.getWbDecodeBox().addView(scroll, new LinearLayout.LayoutParams(-1, host.dpHostCall(36)));
        host.setWbDecodeNote(host.labelHostCall(tr("optics.these_controls_set_the_light")
                + tr("optics.changing_them_decodes_the_image"), 7.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getWbDecodeNote().setMaxLines(2);
        host.getWbDecodeBox().addView(host.getWbDecodeNote(), new LinearLayout.LayoutParams(-1, host.dpHostCall(32)));
        host.adjustRowHostCall(host.getWbDecodeBox(), tr("optics.raw_temperature"), 2000f, 12000f, () -> host.getWbKelvin(), v -> host.setWbKelvin(v), true,
                tr("optics.light_source_temperature_3200_k"));
        host.adjustRowHostCall(host.getWbDecodeBox(), tr("optics.raw_tint"), -150f, 150f, () -> host.getWbTint(), v -> host.setWbTint(v), true,
                tr("optics.green_magenta_balance_increase_to"));
    }

    public static void buildTonePage(ViewPagesHost host, LinearLayout page) {
        // **Two cells, not three.** 直方图 moved to the settings sheet -- it is
        // a display preference, not a parameter of the picture, and it was
        // taking a third of the page's first row. 复位 left with it: the 重置当前页
        // in the heading already means exactly that.
        //
        // What is left is 曲线, which opens as a screen of its own (Lightroom
        // gives it a tool and a 完成, not six rows in the middle of 亮度), and
        // 原图提亮.
        final LinearLayout toneGrid = new LinearLayout(host.context());
        toneGrid.setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(toneGrid, tr("optics.curves"), tr("ui170.148"), tv -> host.openViewToolHostCall(4));

        // **How the canvas reads the decode, not what the engine gets.**
        // Off: the numbers exactly as LibRaw handed them, through sRGB only.
        // On: the median goes to sRGB mid-grey (capped where p99.5 would clip)
        // and the encoded shadows are lifted. That is how every raw was shown
        // from 1.5.1 through 1.6.6a, after an import that came back at 2/255
        // looked like a black rectangle -- but it also hides the picture's own
        // exposure, and it ran 2.54 EV above Lightroom on the reference DNG.
        final Square levelMark = new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost());
        host.toggleCellHostCall(toneGrid, tr("optics.original_brightness"), tr("meter.auto"), levelMark, () -> host.getRawAutoBright(), on -> {
            host.setRawAutoBright(on);
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit()
                    .putBoolean("raw_auto_bright", on).apply();
            host.refreshOriginalViewHostCall();
        });
        page.addView(toneGrid, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        // **Lightroom's six, in Lightroom's order.** 曝光度 / 对比度 / 高光 /
        // 阴影 / 白色色阶 / 黑色色阶 is the panel a user already has in their
        // hands on the other side of the screen. These rows were in the order
        // they were written -- 亮度 between 对比 and 高光, and the two ends
        // last -- so a hand that knows Lightroom kept landing on the wrong row.
        //
        // 相机曝光 stays first and is not a Lightroom row at all: it is the
        // exposure the *engine* applies before the film, which the other engine
        // has no slot for (and greys out there). 亮度 is not a Lightroom slider
        // either; it stays at the end, because the engine has it and taking a
        // control away is not an improvement.
        host.setViewCameraEvBox(host.adjustRowHostCall(page, tr("optics.camera_exposure"), -4f, 4f, () -> host.getCameraEv(), v -> host.setCameraEv(v), false));
        host.setViewCameraEvNote(host.labelHostCall("", 7.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getViewCameraEvNote().setMaxLines(2);
        host.attachBangHostCall(host.getViewCameraEvBox(), () -> host.getViewCameraEvNote() == null
                ? "" : host.getViewCameraEvNote().getText().toString());
        final LinearLayout expBox = host.adjustRowHostCall(page, tr("ui170.149"), -3f, 3f,
                () -> host.getViewExposure(), v -> host.setViewExposure(v), false);
        host.setViewExposureNote(host.labelHostCall("", 7.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getViewExposureNote().setMaxLines(2);
        host.attachBangHostCall(expBox, () -> host.getViewExposureNote() == null
                ? "" : host.getViewExposureNote().getText().toString());
        host.adjustRowHostCall(page, tr("ui170.150"), -100f, 100f, () -> host.getViewContrast(), v -> host.setViewContrast(v), false);
        host.adjustRowHostCall(page, tr("optics.highlights"), -100f, 100f, () -> host.getViewHighlights(), v -> host.setViewHighlights(v), false);
        host.adjustRowHostCall(page, tr("optics.shade"), -100f, 100f, () -> host.getViewShadows(), v -> host.setViewShadows(v), false);
        host.adjustRowHostCall(page, tr("ui170.151"), 0f, 50f, () -> host.getViewWhite(), v -> host.setViewWhite(v), false,
                tr("ui170.152"));
        host.adjustRowHostCall(page, tr("ui170.153"), 0f, 50f, () -> host.getViewBlack(), v -> host.setViewBlack(v), false,
                tr("ui170.154"));
        host.adjustRowHostCall(page, tr("optics.brightness"), -100f, 100f, () -> host.getViewBrightness(), v -> host.setViewBrightness(v), false,
                tr("ui170.155"));
    }

    public static void buildColourPage(ViewPagesHost host, LinearLayout page) {
        // **Lightroom's 颜色, top to bottom.** A row of bordered doors at the
        // top -- HSL and 分级, the two buttons Lightroom puts there -- then the
        // white balance, then 色温 / 色调 / 自然饱和度 / 饱和度.
        //
        // **HSL is not on this page any more.** Eight bands times three sliders
        // is a screen, and Lightroom opens it as one: the 混色 screen shows the
        // eight hue dots and the three rows for the band you picked, with a 完成
        // to come back. Keeping a copy of those rows here as well means the same
        // parameter has two homes and one of them is always stale.
        final LinearLayout doors = new LinearLayout(host.context());
        doors.setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(doors, "HSL", host.getHSL_BANDS()[host.getHslBand()], tv -> host.openViewToolHostCall(6));
        host.choiceCellHostCall(doors, tr("optics.grading"), tr("ui170.156"), tv -> host.openViewToolHostCall(5));
        page.addView(doors, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        buildWhiteBalancePage(host, page);
        page.addView(host.labelHostCall(tr("optics.color_2"), 8, host.getUI_INK(), host.getTfHan()),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(24)));
        final TextView wbNote = host.labelHostCall(
                tr("optics.the_controls_above_re_decode")
                + tr("optics.including_its_highlights"), 7.5f, host.getUI_MUTED(), host.getTfHan());
        wbNote.setMaxLines(2);
        page.addView(wbNote, new LinearLayout.LayoutParams(-1, host.dpHostCall(32)));
        host.adjustRowHostCall(page, tr("optics.temperature"), -100f, 100f, () -> host.getViewTemperature(), v -> host.setViewTemperature(v), false,
                tr("optics.warm_or_cool_the_finished"));
        host.adjustRowHostCall(page, tr("optics.tint"), -100f, 100f, () -> host.getViewTint(), v -> host.setViewTint(v), false,
                tr("optics.adjust_the_finished_print_s"));
        host.adjustRowHostCall(page, tr("optics.vibrance"), -100f, 100f, () -> host.getViewVibrance(), v -> host.setViewVibrance(v), false,
                tr("optics.boost_muted_colors_in_darker"));
        host.adjustRowHostCall(page, tr("optics.saturation"), -100f, 100f, () -> host.getViewSaturation(), v -> host.setViewSaturation(v), false);
    }

    public static void buildDetailPage(ViewPagesHost host, LinearLayout page) {
        page.addView(host.labelHostCall(tr("ui170.157"), 8, host.getUI_INK(), host.getTfHan()),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(24)));
        host.adjustRowHostCall(page, tr("ui170.158"), 0f, 150f, () -> host.getViewSharpen(), v -> host.setViewSharpen(v), false,
                tr("ui170.159"));
        host.adjustRowHostCall(page, tr("ui170.160"), 0.5f, 3f, () -> host.getViewSharpenRadius(),
                v -> host.setViewSharpenRadius(v), false,
                tr("ui170.161"));
        host.adjustRowHostCall(page, tr("ui170.162"), 0f, 100f, () -> host.getViewSharpenMask(), v -> host.setViewSharpenMask(v), false,
                tr("ui170.163"));
        page.addView(host.labelHostCall(tr("ui170.164")
                + tr("ui170.165"),
                7.5f, host.getUI_MUTED(), host.getTfHan()), new LinearLayout.LayoutParams(-1, host.dpHostCall(44)));
    }

    public static void buildGradingPage(ViewPagesHost host, LinearLayout page) {
        host.toolHeaderHostCall(page, tr("optics.grading"));
        // Four zones, one wheel -- Lightroom's shape: pick the tonal range,
        // then drag a dot. Hue is the angle, saturation the distance from grey.
        // **Lightroom's order and Lightroom's words.** 阴影 -> 中间调 -> 高光
        // -> 全部, which is the order the panel lists them in; tr("optics.global") was this
        // app's own name for the last one.
        final String[] zones = {tr("optics.shade"), tr("optics.midtones"), tr("optics.highlights"), tr("meter.all")};
        // Chip index -> the colour-balance zone it edits.
        final int[] zoneIds = {1, 2, 3, 0};
        final LinearLayout zoneRow = new LinearLayout(host.context());
        zoneRow.setOrientation(LinearLayout.HORIZONTAL);
        final TextView[] zoneChips = new TextView[zones.length];
        for (int i = 0; i < zones.length; ++i) {
            final int zone = zoneIds[i];
            final TextView chip = host.labelHostCall(zones[i], 8, zone == host.getCbZone() ? host.getUI_BLUE() : host.getUI_MUTED(), host.getTfBody());
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setOnClickListener(v -> {
                host.setCbZone(zone);
                for (int k = 0; k < zoneChips.length; ++k)
                    zoneChips[k].setTextColor(zoneIds[k] == host.getCbZone() ? host.getUI_BLUE() : host.getUI_MUTED());
                if (host.getColorWheel() != null) host.getColorWheel().invalidate();
                host.setCbReadout(null);
                host.refreshViewPanelHostCall();
            });
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, host.dpHostCall(26), 1f);
            lp.rightMargin = host.dpHostCall(4);
            zoneRow.addView(chip, lp);
            zoneChips[i] = chip;
        }
        page.addView(zoneRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(30)));

        host.setColorWheel(host.newColorWheel());
        page.addView(host.getColorWheel(), new LinearLayout.LayoutParams(-1, host.dpHostCall(196)));

        host.setCbReadout(host.labelHostCall("", 8, host.getUI_MUTED(), host.getTfBody()));
        host.getCbReadout().setGravity(android.view.Gravity.CENTER);
        page.addView(host.getCbReadout(), new LinearLayout.LayoutParams(-1, host.dpHostCall(20)));
        host.refreshCbReadoutHostCall();

        host.adjustRowHostCall(page, tr("optics.luminance"), -1f, 1f, () -> host.getCbLum()[host.getCbZone()], v -> host.getCbLum()[host.getCbZone()] = v, false,
                tr("optics.lighten_or_darken_this_tonal"));
    }

    public static void buildEffectsPage(ViewPagesHost host, LinearLayout page) {
        final String[] tabs = {tr("optics.effects"), tr("ui170.166")};
        final LinearLayout tabRow = new LinearLayout(host.context());
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        host.setEffectChips(new TextView[tabs.length]);
        for (int i = 0; i < tabs.length; ++i) {
            final int tab = i;
            final TextView chip = host.labelHostCall(tabs[i], 9, host.getUI_MUTED(), host.getTfHan());
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setOnClickListener(v -> host.showEffectTabHostCall(tab));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, host.dpHostCall(26), 1f);
            lp.rightMargin = host.dpHostCall(2);
            tabRow.addView(chip, lp);
            host.getEffectChips()[i] = chip;
        }
        page.addView(tabRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(28)));

        host.setEffectPages(new LinearLayout[tabs.length]);
        for (int i = 0; i < tabs.length; ++i) {
            final LinearLayout body = new LinearLayout(host.context());
            body.setOrientation(LinearLayout.VERTICAL);
            body.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            page.addView(body, new LinearLayout.LayoutParams(-1, -2));
            host.getEffectPages()[i] = body;
        }
        host.adjustRowHostCall(host.getEffectPages()[0], tr("ui170.167"), -100f, 100f, () -> host.getViewTexture(), v -> host.setViewTexture(v), false,
                tr("ui170.168"));
        host.adjustRowHostCall(host.getEffectPages()[0], tr("ui170.169"), -100f, 100f, () -> host.getViewVignette(), v -> host.setViewVignette(v), false,
                tr("ui170.170"));
        host.fillGlowControlsHostCall(host.getEffectPages()[1]);
        host.showEffectTabHostCall(0);
    }

    public static void buildHslPage(ViewPagesHost host, LinearLayout page) {
        host.toolHeaderHostCall(page, tr("ui170.171"));
        final LinearLayout row = new LinearLayout(host.context());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        host.setHslSwatches(new View[host.getHSL_BANDS().length]);
        for (int i = 0; i < host.getHSL_BANDS().length; ++i) {
            final int band = i;
            final android.widget.FrameLayout cell = new android.widget.FrameLayout(host.context());
            final View dot = new View(host.context());
            dot.setOnClickListener(v -> {
                host.setHslBand(band);
                host.refreshViewPanelHostCall();
            });
            cell.addView(dot, new android.widget.FrameLayout.LayoutParams(
                    host.dpHostCall(24), host.dpHostCall(24), android.view.Gravity.CENTER));
            cell.setOnClickListener(v -> {
                host.setHslBand(band);
                host.refreshViewPanelHostCall();
            });
            row.addView(cell, new LinearLayout.LayoutParams(0, host.dpHostCall(40), 1f));
            host.getHslSwatches()[i] = dot;
        }
        page.addView(row, new LinearLayout.LayoutParams(-1, host.dpHostCall(40)));
        page.addView(host.labelHostCall(tr("ui170.172"),
                7.5f, host.getUI_MUTED(), host.getTfHan()), new LinearLayout.LayoutParams(-1, host.dpHostCall(26)));
        host.adjustRowHostCall(page, tr("ui170.173"), -40f, 40f, () -> host.getHslHue()[host.getHslBand()], v -> host.getHslHue()[host.getHslBand()] = v, false,
                tr("ui170.174"));
        host.adjustRowHostCall(page, tr("optics.saturation"), -100f, 100f, () -> host.getHslSat()[host.getHslBand()], v -> host.getHslSat()[host.getHslBand()] = v, false,
                tr("ui170.175"));
        host.adjustRowHostCall(page, tr("ui170.176"), -100f, 100f, () -> host.getHslLum()[host.getHslBand()], v -> host.getHslLum()[host.getHslBand()] = v, false,
                tr("ui170.177"));
        host.getViewRefreshers().add(host::refreshHslSwatchesHostCall);
        host.refreshHslSwatchesHostCall();
    }

    public static void buildCurvesPage(ViewPagesHost host, LinearLayout page) {
        host.toolHeaderHostCall(page, tr("optics.curves"));
        final String[] channels = {"RGB", tr("optics.brightness"), "R", "G", "B"};
        final LinearLayout row = new LinearLayout(host.context());
        row.setOrientation(LinearLayout.HORIZONTAL);
        final TextView[] chips = new TextView[channels.length];
        for (int i = 0; i < channels.length; ++i) {
            final int ch = i;
            final TextView chip = host.labelHostCall(channels[i], 8, i == 0 ? host.getUI_BLUE() : host.getUI_MUTED(), host.getTfBody());
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setOnClickListener(v -> {
                host.setCurveChannel(ch);
                for (int k = 0; k < chips.length; ++k)
                    chips[k].setTextColor(k == ch ? host.getUI_BLUE() : host.getUI_MUTED());
                if (host.getCurveView() != null) host.getCurveView().invalidate();
            });
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, host.dpHostCall(26), 1f);
            lp.rightMargin = host.dpHostCall(4);
            row.addView(chip, lp);
            chips[i] = chip;
        }
        page.addView(row, new LinearLayout.LayoutParams(-1, host.dpHostCall(28)));
        host.setCurveView(host.newCurveView());
        page.addView(host.getCurveView(), new LinearLayout.LayoutParams(-1, host.dpHostCall(150)));
        page.addView(host.labelHostCall(tr("optics.drag_the_control_points_or"), 7.5f, host.getUI_MUTED(), host.getTfHan()),
                new LinearLayout.LayoutParams(-1, host.dpHostCall(18)));
    }

    public static void showViewPage(ViewPagesHost host, int page) {
        if (page < 0 || page >= host.getVIEW_PAGE_NAMES().length) page = 0;
        host.setToolPage(-1);
        // The sub-page being left: its height is where the block grows from, so
        // 亮度 -> 颜色 does not resize the photograph in one step either.
        final int fromH = host.getViewScrolls()[host.getViewPage()] != null
                && host.getViewScrolls()[host.getViewPage()].getVisibility() == View.VISIBLE
                ? host.getViewScrolls()[host.getViewPage()].getHeight() : 0;
        host.setViewPage(page);
        for (int i = 0; i < host.getViewPages().length; ++i) {
            final boolean on = i == page;
            if (host.getViewPages()[i] != null) host.getViewPages()[i].setVisibility(on ? View.VISIBLE : View.GONE);
            if (host.getViewCatChips() != null) host.tintTextHostCall(host.getViewCatChips()[i], on ? host.getUI_BLUE() : host.getUI_MUTED(), host.getMS_SOFT());
            // The sub-pages are stacked in one vertical panel with only one of
            // them visible, so the *scroll* is what changes height -- and the
            // scroll is what has to move. 26 dp, not a page's 40: a sub-page is
            // a smaller thing than a page, and the motion says so.
            if (host.getViewScrolls()[i] == null) continue;
            if (on) {
                // **`again = true`: the guard at the top of `enterPanel` must
                // not decide this.** All six scrolls are built visible and only
                // their inner page is `GONE`, so on the first visit the incoming
                // scroll already looks "shown", the whole arrival was skipped,
                // and the swap was a cut: no height, no notches, no typing.
                host.enterPanelHostCall(host.getViewScrolls()[i], fromH, true);
                // A sub-page is a smaller thing than a page, so it rises a
                // shorter distance -- 12 dp against a page's 40.
                final View sc = host.getViewScrolls()[i];
                sc.setTranslationY(host.dpHostCall(12));
                sc.animate().translationY(0f)
                        .setDuration(host.getMS_SOFT())
                        .setInterpolator(host.getEASE_OUT())
                        .start();
            } else {
                host.hideAtOnceHostCall(host.getViewScrolls()[i]);
            }
        }
        host.syncSubRowActiveHostCall();
        host.markFileViaHost("view page=" + page + " fromH=" + fromH
                + " h=" + (host.getViewScrolls()[page] == null ? -1 : host.getViewScrolls()[page].getHeight()));
    }
}
