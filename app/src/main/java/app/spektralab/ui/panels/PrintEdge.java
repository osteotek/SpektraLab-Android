package app.spektralab.ui.panels;
import app.spektralab.ui.CappedScroll;
import app.spektralab.ui.Square;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.Probe;
import app.spektralab.ui.CappedScroll;

/** PrintEdge builders extracted from MainActivity (architecture Phase C). */
public final class PrintEdge {
    private PrintEdge() {}

    public static void showPrintGroup(PrintEdgeHost host, int index) {
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            final int wanted = index;
            host.runOnUiThreadHostCall(() -> showPrintGroup(host, wanted));
            return;
        }
        if (index < 0 || index >= host.getFOTU_GROUPS().length) index = 0;
        final boolean moved = index != host.getPrintGroup();
        host.setPrintGroup(index);
        if (host.getPrintBody() == null) return;
        try {
            buildPrintGroup(host, index);
        } catch (Throwable t) {
            host.getPrintRefreshers().clear();
            host.getPrintBody().removeAllViews();
            host.getPrintBody().addView(host.labelHostCall(tr("ui170.186") + t, 8f, host.getUI_INK(), host.getTfHan()));
            try {
                DebugSink.text("printpanel", "group " + index + ": " + t + "\n"
                        + android.util.Log.getStackTraceString(t));
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:19464", ignored); }
        }
        host.syncSubRowActiveHostCall();
        host.refreshKnobHostCall();
        if (moved) {
            final View box = host.getPrintBody().getParent() instanceof View
                    ? (View) host.getPrintBody().getParent() : null;
            final int cap = box instanceof CappedScroll
                    ? ((CappedScroll) box).cap() : Integer.MAX_VALUE;
            final int w = host.getPrintBody().getWidth() > 0 ? host.getPrintBody().getWidth()
                    : (box != null ? box.getWidth() : 0);
            final int was = box != null ? box.getHeight() : 0;
            final int want = w > 0 ? Math.min(host.naturalHeightViaHost(host.getPrintBody(), w), cap) : 0;
            host.componentArrivalHostCall(host.getPrintBody());
            if (box != null && want > 0 && was > 0 && want != was) {
                host.animateHeightToHostCall(box, was, want);
            }
        }
    }

    public static void buildPrintGroup(PrintEdgeHost host, int index) {
        host.getPrintRefreshers().clear();
        host.getPrintBody().removeAllViews();
        host.getLatGated().clear();
        switch (index) {
            case 0:   // 影调
                // **Three controls, one row.** They were three full-width rows
                // -- 46 + 48 + 46 dp of the page's 250 for a button, a switch and
                // a picker that each need a third of the width. Side by side, in
                // square boxes, is how Lightroom stacks a page's switches.
                final LinearLayout toneCells = new LinearLayout(host.context());
                toneCells.setOrientation(LinearLayout.HORIZONTAL);
                host.choiceCellHostCall(toneCells, tr("print.color_timing"), tr("print.rebalance_print"), tv -> host.resolvePrintingHostCall());
                host.setEdrMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
                host.toggleCellHostCall(toneCells, tr("print.dynamic_range"), "EDR", host.getEdrMark(), () -> host.getEdrOn() > 0.5f, on -> {
                    host.setEdrOn(on ? 1f : 0f);
                    if (host.getUiReady() && host.getSession() != null) {
                        host.setShowingOriginal(false);
                        host.setOriginalVisualHostCall(false);
                        host.renderImageHostCall();
                    }
                });
                host.choiceCellHostCall(toneCells, tr("print.protection_basis"),
                        host.labelOfViaHost(host.getNORM_LABELS(), host.getNORM_IDS(), host.getSceneNorm()), tv ->
                        host.showChoiceMappingHostCall(tr("print.basis_for_highlight_and_shadow"), host.getNORM_LABELS(), host.getNORM_IDS(),
                                host.getSceneNorm(), id -> {
                                    host.setSceneNorm(id);
                                    tv.setText(host.labelOfViaHost(host.getNORM_LABELS(), host.getNORM_IDS(), id));
                                    host.applyParamsHostCall();
                                    if (host.getUiReady() && host.getSession() != null) {
                                        host.setShowingOriginal(false);
                                        host.setOriginalVisualHostCall(false);
                                        host.renderImageHostCall();
                                    }
                                }));
                host.setSpotMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
                host.toggleCellHostCall(toneCells, tr("ui170.187"), tr("ui170.188"), host.getSpotMark(), () -> host.getSpotOn(), on -> {
                    host.setSpotOn(on);
                    host.applyParamsHostCall();
                    if (host.getUiReady() && host.getSession() != null) {
                        host.setShowingOriginal(false);
                        host.setOriginalVisualHostCall(false);
                        host.renderImageHostCall();
                    }
                });
                host.getPrintBody().addView(toneCells, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

                // **相纸宽容度：这是唯一能真正tr("ui170.189")相纸那条 7 档线的控件。**
                // 它不是把纸的曲线拉直（物理上做不到），而是给每个大尺度区域
                // 它自己那一份曝光 —— 暗房里的遮挡和加深，自动做在负片的纸曝光
                // 上。高光那边多给一点光把它从肩部拉回来，暗部那边少给一点把它
                // 从趾部推出去；照片内部的细节（base 跟不上的那一层）一点没动。
                host.getPrintBody().addView(host.choiceRowHostCall(tr("ui170.190"),
                        host.getLATITUDE_LABELS()[Math.max(0, Math.min(4, host.getPrintLatitude()))] + "  ›",
                        tv -> host.showChoiceMappingHostCall(tr("ui170.191"),
                                host.getLATITUDE_LABELS(), new String[]{"0", "1", "2", "3", "4"},
                                String.valueOf(host.getPrintLatitude()), id -> {
                                    host.setPrintLatitude(Math.max(0, Math.min(4, Integer.parseInt(id))));
                                    host.refreshLatitudeHostCall();
                                    tv.setText(host.getLATITUDE_LABELS()[host.getPrintLatitude()] + "  ›");
                                    host.gateLatitudeHostCall();
                                    Probe.log("toggle", "printLatitude " + host.getPrintLatitude());
                                    if (host.getUiReady() && host.getSession() != null) {
                                        host.setShowingOriginal(false);
                                        host.setOriginalVisualHostCall(false);
                                        host.renderImageHostCall();
                                    }
                                })),
                        new LinearLayout.LayoutParams(-1, -2));
                host.printRowHostCall(tr("print.knob.enlarger_exposure_right_is_darker"));
                host.printRowHostCall(tr("print.knob.highlight_protection"));
                host.printRowHostCall(tr("print.knob.shadow_protection"));
                host.printRowHostCall(tr("ui170.138"));
                host.printRowHostCall(tr("ui170.139"));
                host.printRowHostCall(tr("ui170.140"));
                host.printRowHostCall(tr("ui170.141"));
                host.printRowHostCall(tr("ui170.181"));
                host.printRowHostCall(tr("ui170.182"));
                host.printRowHostCall(tr("ui170.183"));
                host.printRowHostCall(tr("ui170.184"));
                host.printRowHostCall(tr("print.knob.curve_softness"));
                host.printRowHostCall(tr("print.knob.maximum_shadow_lift"));
                host.printRowHostCall(tr("print.knob.density_curve"));
                host.printRowHostCall(tr("print.knob.pre_flash"));
                break;
            case 1:   // 色彩
                host.setHueOwnMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
                host.getPrintBody().addView(host.toggleRowHostCall(tr("ui170.192"), host.getHueOwnMark(),
                        () -> host.hueFixUnsupportedViaHost(host.getFilmStock()) || host.getHueCorrection() < 0.5f,
                        on -> {
                            if (host.hueFixUnsupportedViaHost(host.getFilmStock())) {
                                host.refreshHueOwnMarkHostCall();
                                host.showActionSheetHostCall(tr("print.hue_correction_unavailable_for_this"),
                                        tr("print.this_stock_s_spectral_calibration")
                                        + tr("print.correction_would_shift_the_hues")
                                        + tr("print.choose_a_different_film_to"),
                                        new String[]{tr("print.ok")}, which -> { });
                                return;
                            }
                            host.setHueCorrection(on ? 0f : 1f);
                            if (host.getUiReady() && host.getSession() != null) {
                                host.setShowingOriginal(false);
                                host.setOriginalVisualHostCall(false);
                                host.renderImageHostCall();
                            }
                        }));
                host.printRowHostCall(tr("print.knob.interlayer_inhibition"));
                host.printRowHostCall(tr("ui170.185"));
                break;
            case 2:   // 胶片
                host.printRowHostCall(tr("print.knob.digital_intermediate"));
                host.printRowHostCall(tr("print.knob.output_encoding"));
                host.printRowHostCall(tr("print.knob.scan_whites"));
                host.printRowHostCall(tr("print.knob.scan_blacks"));
                break;
            case 3:   // 颗粒
                host.printRowHostCall(tr("print.knob.grain"));
                host.printRowHostCall(tr("print.knob.enable_grain"));
                break;
            case 4:   // 光晕
                host.printRowHostCall(tr("print.knob.halation"));
                host.printRowHostCall(tr("print.knob.enable_halation"));
                host.printRowHostCall(tr("print.knob.halation_boost"));
                host.printRowHostCall(tr("print.knob.halation_headroom"));
                host.printRowHostCall(tr("print.knob.remove_anti_halation"));
                host.printRowHostCall(tr("print.knob.emulsion_scatter"));
                host.printRowHostCall(tr("print.knob.scatter_radius"));
                break;
            case 5:   // 镜头
                host.printRowHostCall(tr("print.knob.lens_blur"));
                host.printRowHostCall(tr("print.knob.scanner_blur"));
                host.addChoiceHostCall(host.getPrintBody(), tr("print.lens_filter"),
                        host.labelOfViaHost(host.getFILTER_LABELS(), host.getFILTER_IDS(), host.getCameraFilter()), tv ->
                        host.showChoiceMappingHostCall(tr("print.lens_filter_wratten"), host.getFILTER_LABELS(), host.getFILTER_IDS(),
                                host.getCameraFilter(), id -> {
                                    host.setCameraFilter(id);
                                    tv.setText(host.labelOfViaHost(host.getFILTER_LABELS(), host.getFILTER_IDS(), id));
                                    host.applyParamsHostCall();
                                    if (host.getUiReady() && host.getSession() != null) {
                                        host.setShowingOriginal(false);
                                        host.setOriginalVisualHostCall(false);
                                        host.renderImageHostCall();
                                    }
                                }));
                break;
            default:  // 相纸
                host.printRowHostCall(tr("print.knob.yellow_filter"));
                host.printRowHostCall(tr("print.knob.magenta_filter"));
                host.printRowHostCall(tr("print.knob.filter_strength"));
                host.printRowHostCall(tr("print.knob.glare"));
                host.printRowHostCall(tr("print.knob.enable_glare"));
                // **What the canvas is drawn in.** Not a look parameter: it never
                // reaches a file, and two exports of the same frame in different
                // canvas spaces are the same bytes.
                host.addChoiceHostCall(host.getPrintBody(), tr("print.preview_gamut"), host.getPreviewSpace(), tv -> {
                    final java.util.List<String> labels = java.util.Arrays.asList(host.getSPACES());
                    host.showChoiceSheetHostCall(tr("print.preview_gamut_display_only_exports"),
                            labels, host.getPreviewSpace(), picked -> {
                                final int i = labels.indexOf(picked);
                                if (i >= 0) host.setPreviewSpace(host.getSPACES()[i]);
                                tv.setText(host.getPreviewSpace());
                                if (host.getUiReady() && host.getSession() != null) {
                                    host.applyParamsHostCall();
                                    host.renderImageHostCall();
                                }
                            });
                });
                break;
        }
        // **After the switch, not inside one branch.** `showPrintGroup` returns
        // here on every path, so the gate always runs on the page that was just
        // built -- and on the rows that exist now, not on the ones the previous
        // page left in `latGated`.
        host.gateLatitudeHostCall();
        // The arrival animation owns the rows' alpha (`enterPanel` writes 1 on
        // every row), so put the dim back once the frame trace has finished.
        // The colour and the disabled state are already in place and do not
        // need this.
        if (host.getPrintBody() != null) host.getPrintBody().postDelayed(host::gateLatitudeHostCall, 900);
    }

    public static void buildEdgePages(PrintEdgeHost host) {
        // **`onEdgeChanged`, not a copy of it.** This hook rebuilt the frame
        // through the session, which a fotufilm frame does not have -- so on that
        // engine every drawn control (片边文字, 片基雾, 张数, the date's four)
        // moved its number and changed nothing on the canvas. `onEdgeChanged`
        // owns both answers: recompose where the border is drawn, render where
        // the engine develops it.
        final java.util.function.Consumer<Void> redraw = v -> host.onEdgeChangedHostCall();

        host.setEdgePage(new LinearLayout(host.context()));
        host.getEdgePage().setOrientation(LinearLayout.VERTICAL);
        host.getEdgePage().addView(host.pageHeadHostCall(tr("border.film_border_2")), new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));
        host.setEdgeFotuNote(host.labelHostCall(tr("ui170.241")
                + tr("ui170.242"), 7.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getEdgeFotuNote().setPadding(host.dpHostCall(2), 0, host.dpHostCall(2), host.dpHostCall(6));
        host.getEdgeFotuNote().setVisibility(View.GONE);
        host.getEdgePage().addView(host.getEdgeFotuNote(), new LinearLayout.LayoutParams(-1, -2));
        host.setOverscanMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.setOverscanRow(host.toggleRowHostCall(tr("border.enable_film_border"), host.getOverscanMark(), () -> host.getOverscanOn(), on -> {
            if (on && host.getEdgeIds().isEmpty()) {
                host.getOverscanMark().setLit(false);
                // **Point at the way out.** A 16:9 frame has no gate at all, and
                // this switch used to answer "does not correspond to any gate"
                // while the picker that could fix it was dimmed a few rows down.
                host.showInAppToastHostCall(tr("border.choose_a_film_gate_first"));
                return;
            }
            host.setOverscanOn(on);
            if (on && host.isFotuStockViaHost(host.getFilmStock())) {
                // **Nothing can refuse the shape here.** The first engine's
                // overscan rejects a frame that is not its gate's shape (E-1305)
                // and the UI heals the gate from that error; a drawn strip has
                // nothing to refuse with, so the crop happens up front -- the
                // same cut the gate picker makes.
                final double want = host.getEDGE_RATIOS()[host.gateIndexOfViaHost(host.getOverscanFormat())];
                final float cur = host.currentAspectHostCall();
                if (cur <= 0f || Math.abs(cur / (float) want - 1f) > 0.045f) {
                    host.applyGateHostCall(host.getOverscanFormat());
                    return;
                }
            }
            host.refreshEdgeGroupsHostCall();
            host.onEdgeChangedHostCall();
        }));
        host.getEdgePage().addView(host.getOverscanRow(), new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));
        // **The field, not a local that shadows it.** `refreshEdgeGroups` dims
        // `edgeInner`, and a local here left the field null -- so every switch
        // it was meant to grey stayed lit and enabled, which is the opposite of
        // what the page says it does.
        host.setEdgeInner(new LinearLayout(host.context()));
        host.getEdgeInner().setOrientation(LinearLayout.VERTICAL);

        // **The two rows that *decide* the shape live outside the dimmed block.**
        // `refreshEdgeGroups` disables `edgeInner` while the rebate is off, and
        // these two used to be inside it -- so a 1.777:1 frame could not be
        // given a gate at all: the 开启片边 switch refused ("no gate fits"),
        // and the picker one row below was greyed out and untappable. The only
        // way through was to crop in 构图 first. Now the entries stay live and
        // choosing a gate crops the picture and turns the rebate on.
        LinearLayout gateRow = new LinearLayout(host.context());
        gateRow.setOrientation(LinearLayout.HORIZONTAL);
        {
            final int at = host.getEdgeIds().indexOf(host.getOverscanFormat());
            final String current = at >= 0 && at < host.getEdgeLabels().size() ? host.getEdgeLabels().get(at) : host.getOverscanFormat();
            host.choiceCellHostCall(gateRow, tr("border.film_gate_format"), current, tv -> {
                host.setEdgeValue(tv);
                host.showEdgePickerHostCall();
            });
        }
        // **方向, where the user looks for it.** A rebate is cut by a gate and a
        // gate's shape is fixed -- the film runs along the picture's short edge
        // -- so "turn the rebate" can only mean "turn the picture". That is one
        // quarter turn, and `commitCompose` then re-picks the gates for the new
        // shape. The rebate follows; the canvas shows both.
        host.choiceCellHostCall(gateRow, tr("border.border_orientation"), tr("border.rotate_90"), tv -> {
            host.turnByHostCall(1);
            host.showInAppToastHostCall(tr("border.rotated_90_the_border_follows"));
        });
        host.getEdgePage().addView(gateRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        // **半格对：一条片上两格。** 引擎的 `overscan_pair` 只认 `135_half`
        // 的片门，而且要求画面正好是 37:24（两格 18 mm 夹着 19 mm 走片），
        // 所以打开它先选片门、再把照片裁成那个比例。第二引擎没有这个概念
        // （它的片边是画出来的），所以那一侧整行禁用。
        host.setPairMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.setPairRow(host.toggleRowHostCall(tr("ui170.243"), host.getPairMark(), () -> host.getOverscanPair(), on -> {
            if (host.isFotuStockViaHost(host.getFilmStock())) {
                host.getPairMark().setLit(false);
                host.showInAppToastHostCall(tr("ui170.244"));
                return;
            }
            host.setOverscanPair(on);
            host.getPairMark().setLit(on);
            if (on) {
                host.setOverscanFormat("135_half");
                host.setFilmFormatMm(host.getEDGE_MM()[1]);
                host.setFormatIndex(1);
            }
            host.applyGateHostCall(host.getOverscanFormat());
            host.refreshPairRowsHostCall();
        }));
        host.getEdgePage().addView(host.getPairRow(), new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));

        host.setEdgeTextMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.getEdgeInner().addView(host.toggleRowHostCall(tr("border.edge_markings_brand_frame_number"), host.getEdgeTextMark(), () -> host.getEdgeTextOn(), on -> {
            host.setEdgeTextOn(on);
            redraw.accept(null);
        }), new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));
        host.setEdgeFogBar(host.rangeRowHostCall(host.getEdgeInner(), tr("border.base_fog"), tr("border.haze_in_the_film_base"),
                0.0f, 4.0f, host.getEdgeFog(), 0.1f, v -> host.setEdgeFog(v)));
        host.setEdgeLeaksBar(host.rangeRowHostCall(host.getEdgeInner(), tr("border.light_leak"), tr("border.light_leaking_into_the_roll"),
                0.0f, 4.0f, host.getEdgeLeaks(), 0.1f, v -> host.setEdgeLeaks(v)));
        host.setFrameNoBar(host.rangeRowHostCall(host.getEdgeInner(), tr("border.frame_number"), tr("ui170.245"),
                0.0f, 99.0f, host.getFrameNumber(), 1f, v -> host.setFrameNumber(v)));
        // **The aperture: a stop list, not a number line.** f/1.2 -> f/1.4 is
        // a hair at the left of a 0..64 dial and half its travel is past any
        // lens anyone owns -- and the value used to be sent as "%.0f", so
        // f/1.4 shipped as f/1 (the 取整 in the report). A tap opens the
        // standard stops plus a typed field.
        LinearLayout fNumRow = new LinearLayout(host.context());
        fNumRow.setOrientation(LinearLayout.HORIZONTAL);
        fNumRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fNumRow.addView(host.labelHostCall(tr("border.aperture"), 8, host.getUI_INK(), host.getTfHan()), new LinearLayout.LayoutParams(-2, -2));
        fNumRow.addView(host.bangForHostCall(() -> tr("border.recorded_f_number_affects_the")));
        fNumRow.addView(new View(host.context()), new LinearLayout.LayoutParams(0, -2, 1f));
        host.setEdgeFNumValue(host.labelHostCall(host.fNumLabelViaHost(host.getEdgeFNumber()), 8, host.getUI_BLUE(), host.getTfBody()));
        host.getEdgeFNumValue().setFontFeatureSettings("tnum");    // live number, not typed
        host.getEdgeFNumValue().setGravity(android.view.Gravity.END);
        host.getEdgeFNumValue().setPadding(host.dpHostCall(12), host.dpHostCall(10), host.dpHostCall(2), host.dpHostCall(10));
        fNumRow.addView(host.getEdgeFNumValue(), new LinearLayout.LayoutParams(-2, -2));
        fNumRow.setOnClickListener(v -> host.showApertureSheetHostCall());
        host.getEdgeInner().addView(fNumRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(40)));

        host.setEdgeShapeRow(new LinearLayout(host.context()));
        host.getEdgeShapeRow().setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(host.getEdgeShapeRow(), tr("border.border_style"), host.labelOfViaHost(host.getMODE_LABELS(), host.getMODE_IDS(), host.getEdgeMode()), tv ->
                host.showChoiceMappingHostCall(tr("border.border_style"), host.getMODE_LABELS(), host.getMODE_IDS(), host.getEdgeMode(), id -> {
                    host.setEdgeMode(id);
                    tv.setText(host.labelOfViaHost(host.getMODE_LABELS(), host.getMODE_IDS(), id));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(host.getEdgeShapeRow(), tr("border.gate_shape"), host.labelOfViaHost(host.getGATE_LABELS(), host.getGATE_IDS(), host.getEdgeGate()), tv ->
                host.showChoiceMappingHostCall(tr("border.gate_shape"), host.getGATE_LABELS(), host.getGATE_IDS(), host.getEdgeGate(), id -> {
                    host.setEdgeGate(id);
                    tv.setText(host.labelOfViaHost(host.getGATE_LABELS(), host.getGATE_IDS(), id));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(host.getEdgeShapeRow(), tr("border.sprocket_holes"), host.labelOfViaHost(host.getHOLES_LABELS(), host.getHOLES_IDS(), host.getEdgeHoles()), tv ->
                host.showChoiceMappingHostCall(tr("border.what_appears_through_the_sprocket"), host.getHOLES_LABELS(), host.getHOLES_IDS(), host.getEdgeHoles(), id -> {
                    host.setEdgeHoles(id);
                    tv.setText(host.labelOfViaHost(host.getHOLES_LABELS(), host.getHOLES_IDS(), id));
                    redraw.accept(null);
                }));
        host.getEdgeInner().addView(host.getEdgeShapeRow(), new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        LinearLayout edgeSeedRow = new LinearLayout(host.context());
        edgeSeedRow.setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(edgeSeedRow, tr("border.film_holder"), host.labelOfViaHost(host.getCARRIER_LABELS(), host.getCARRIER_IDS(), host.getEdgeCarrier()), tv ->
                host.showChoiceMappingHostCall(tr("border.what_appears_alongside_the_film"), host.getCARRIER_LABELS(), host.getCARRIER_IDS(), host.getEdgeCarrier(), id -> {
                    host.setEdgeCarrier(id);
                    tv.setText(host.labelOfViaHost(host.getCARRIER_LABELS(), host.getCARRIER_IDS(), id));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(edgeSeedRow, tr("border.border_seed"), String.valueOf(host.getCameraSeed()), tv ->
                host.askTextHostCall(tr("border.random_seed_for_the_film"), String.valueOf(host.getCameraSeed()), true, v -> {
                    try { host.setCameraSeed(Math.max(0, Integer.parseInt(v))); } catch (Exception ignored) { return; }
                    tv.setText(String.valueOf(host.getCameraSeed()));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(edgeSeedRow, tr("border.frame_seed"), String.valueOf(host.getFrameSeed()), tv ->
                host.askTextHostCall(tr("border.random_seed_for_the_frame"), String.valueOf(host.getFrameSeed()), true, v -> {
                    try { host.setFrameSeed(Math.max(0, Integer.parseInt(v))); } catch (Exception ignored) { return; }
                    tv.setText(String.valueOf(host.getFrameSeed()));
                    redraw.accept(null);
                }));
        LinearLayout.LayoutParams edgeSeedLp = new LinearLayout.LayoutParams(-1, host.dpHostCall(46));
        edgeSeedLp.topMargin = host.dpHostCall(6);
        host.getEdgeInner().addView(edgeSeedRow, edgeSeedLp);

        // **后半格自己的宽容度。** 两格并排时它们可以是两个场景：一格对着
        // 窗户、一格背着光，一条片上各自印对。`位置` 是第二段沿着片长开始的
        // 地方 —— 0.514 就是 18 mm 画面 + 1 mm 间隙，共 37 mm。
        host.setPairBox(new LinearLayout(host.context()));
        host.getPairBox().setOrientation(LinearLayout.VERTICAL);
        host.setPairToneMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.getPairBox().addView(host.toggleRowHostCall(tr("ui170.246"), host.getPairToneMark(), () -> host.getPairTone(), on -> {
            host.setPairTone(on);
            host.getPairToneMark().setLit(on);
            host.onEdgeChangedHostCall();
        }), new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));
        host.setPairSplitBar(host.rangeRowHostCall(host.getPairBox(), tr("ui170.247"),
                tr("ui170.248"),
                0.30f, 0.70f, host.getPairSplit(), 0.002f, v -> host.setPairSplit(v)));
        host.setPairHighKneeBar(host.rangeRowHostCall(host.getPairBox(), tr("ui170.249"),
                tr("ui170.250"),
                -6.0f, 6.0f, host.getPairHighKnee(), 0.1f, v -> host.setPairHighKnee(v)));
        host.setPairHighRoomBar(host.rangeRowHostCall(host.getPairBox(), tr("ui170.251"),
                tr("ui170.252"),
                0.0f, 8.0f, host.getPairHighRoom(), 0.1f, v -> host.setPairHighRoom(v)));
        host.setPairShadowKneeBar(host.rangeRowHostCall(host.getPairBox(), tr("ui170.253"),
                tr("ui170.254"), -6.0f, 6.0f, host.getPairShadowKnee(), 0.1f, v -> host.setPairShadowKnee(v)));
        host.setPairShadowRoomBar(host.rangeRowHostCall(host.getPairBox(), tr("ui170.255"),
                tr("ui170.256"),
                0.0f, 8.0f, host.getPairShadowRoom(), 0.1f, v -> host.setPairShadowRoom(v)));
        host.getEdgeInner().addView(host.getPairBox(), new LinearLayout.LayoutParams(-1, -2));

        host.setEdgeInner(host.getEdgeInner());
        // **240, not 340.** 片门格式 and 片边方向 moved out of the scroll and
        // into the page, so the block keeps its old total height instead of
        // pushing the tab bar off the bottom of a 640 dp screen.
        host.getEdgePage().addView(host.cappedScrollHostCall(host.getEdgeInner(), 240), new LinearLayout.LayoutParams(-1, -2));
        host.getEdgeRoot().addView(host.getEdgePage(), new LinearLayout.LayoutParams(-1, -2));

        host.setDatePage(new LinearLayout(host.context()));
        host.getDatePage().setOrientation(LinearLayout.VERTICAL);
        host.getDatePage().addView(host.pageHeadHostCall(tr("border.date_stamp_2")), new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));
        host.setDateMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.setDateRow(host.toggleRowHostCall(tr("border.enable_date_stamp"), host.getDateMark(), () -> host.getDateOn(), on -> {
            host.setDateOn(on);
            if (host.getDateOn() && host.getDateText() == null) host.setDateText(host.imprintDateHostCall());
            host.refreshEdgeGroupsHostCall();
            host.onEdgeChangedHostCall();
        }));
        host.getDatePage().addView(host.getDateRow(), new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));
        host.setDateInner(new LinearLayout(host.context()));
        host.getDateInner().setOrientation(LinearLayout.VERTICAL);
        // 0..200 mm: 30 was the schema's old cap and it stopped the date a
        // third of the way along a 6x12 / 6x17 rebate.
        host.setDateInsetXBar(host.rangeRowHostCall(host.getDateInner(), tr("border.horizontal_inset"), tr("border.distance_from_the_sides_of"),
                0.0f, 200.0f, host.getDateInsetX(), v -> host.setDateInsetX(v)));
        host.setDateInsetYBar(host.rangeRowHostCall(host.getDateInner(), tr("border.vertical_inset"), tr("border.distance_from_the_top_or"),
                0.0f, 200.0f, host.getDateInsetY(), v -> host.setDateInsetY(v)));
        // 8.0: the engine clamps at 8 now, because "make the date bigger" was
        // the one adjustment the old 3.0 ceiling refused.
        host.setDateSizeBar(host.rangeRowHostCall(host.getDateInner(), tr("border.date_size"), tr("border.relative_character_height_1_0"),
                0.4f, 8.0f, host.getDateSize(), v -> host.setDateSize(v)));
        host.setDateEvBar(host.rangeRowHostCall(host.getDateInner(), tr("border.date_brightness"), tr("border.red_light_exposure_above_18"),
                -2.0f, 8.0f, host.getDateEv(), v -> host.setDateEv(v)));
        LinearLayout dateGrid1 = new LinearLayout(host.context());
        dateGrid1.setOrientation(LinearLayout.HORIZONTAL);
        // The one field the app used to decide for the user: it read the date
        // out of EXIF and gave no way to write a different one.
        host.choiceCellHostCall(dateGrid1, tr("border.date_text"), host.getDateText() == null || host.getDateText().isEmpty() ? host.imprintDateHostCall() : host.getDateText(),
                tv -> host.askTextHostCall(tr("border.text_stamped_on_the_image"),
                        host.getDateText() == null || host.getDateText().isEmpty() ? host.imprintDateHostCall() : host.getDateText(), v -> {
                            host.setDateText(v.isEmpty() ? null : v);
                            tv.setText(host.getDateText() == null ? host.imprintDateHostCall() : host.getDateText());
                            redraw.accept(null);
                        }));
        host.choiceCellHostCall(dateGrid1, tr("border.date_placement"), host.labelOfViaHost(host.getPLACE_LABELS(), host.getPLACE_IDS(), host.getDatePlacement()), tv ->
                host.showChoiceMappingHostCall(tr("border.where_to_place_the_date"), host.getPLACE_LABELS(), host.getPLACE_IDS(), host.getDatePlacement(), id -> {
                    host.setDatePlacement(id);
                    tv.setText(host.labelOfViaHost(host.getPLACE_LABELS(), host.getPLACE_IDS(), id));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(dateGrid1, tr("border.date_corner"), host.labelOfViaHost(host.getCORNER_LABELS(), host.getCORNER_IDS(), host.getDateCorner()), tv ->
                host.showChoiceMappingHostCall(tr("border.which_corner_to_stamp"), host.getCORNER_LABELS(), host.getCORNER_IDS(), host.getDateCorner(), id -> {
                    host.setDateCorner(id);
                    tv.setText(host.labelOfViaHost(host.getCORNER_LABELS(), host.getCORNER_IDS(), id));
                    redraw.accept(null);
                }));
        host.getDateInner().addView(dateGrid1, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        LinearLayout dateGrid2 = new LinearLayout(host.context());
        dateGrid2.setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(dateGrid2, tr("border.date_style"), host.labelOfViaHost(host.getSTYLE_LABELS(), host.getSTYLE_IDS(), host.getDateStyle()), tv ->
                host.showChoiceMappingHostCall(tr("border.type_of_lettering_for_the"), host.getSTYLE_LABELS(), host.getSTYLE_IDS(), host.getDateStyle(), id -> {
                    host.setDateStyle(id);
                    tv.setText(host.labelOfViaHost(host.getSTYLE_LABELS(), host.getSTYLE_IDS(), id));
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(dateGrid2, tr("border.shooting_data"), host.getDataText().isEmpty() ? tr("border.empty") : host.getDataText(), tv ->
                host.askTextHostCall(tr("border.shooting_data_to_print_alongside"), host.getDataText(), v -> {
                    host.setDataText(v);
                    tv.setText(v.isEmpty() ? tr("border.empty") : v);
                    redraw.accept(null);
                }));
        host.choiceCellHostCall(dateGrid2, tr("border.second_frame_date"), host.getDateTextB().isEmpty() ? tr("border.empty") : host.getDateTextB(), tv ->
                host.askTextHostCall(tr("border.date_for_the_second_frame"), host.getDateTextB(), v -> {
                    host.setDateTextB(v);
                    tv.setText(v.isEmpty() ? tr("border.empty") : v);
                    redraw.accept(null);
                }));
        LinearLayout.LayoutParams dateGrid2Lp = new LinearLayout.LayoutParams(-1, host.dpHostCall(46));
        dateGrid2Lp.topMargin = host.dpHostCall(6);
        host.getDateInner().addView(dateGrid2, dateGrid2Lp);

        TextView dateTip = host.labelHostCall(
                tr("border.for_an_in_image_stamp"),
                7.5f, host.getUI_MUTED(), host.getTfHan());
        dateTip.setLineSpacing(host.dpHostCall(1), 1f);
        host.getDateInner().addView(dateTip, new LinearLayout.LayoutParams(-1, -2));
        host.setDateInner(host.getDateInner());
        host.getDatePage().addView(host.cappedScrollHostCall(host.getDateInner(), 340), new LinearLayout.LayoutParams(-1, -2));
        host.getEdgeRoot().addView(host.getDatePage(), new LinearLayout.LayoutParams(-1, -2));
        buildPrintFramePage(host);
        host.getEdgeRoot().addView(host.getPfPage(), new LinearLayout.LayoutParams(-1, -2));
    }

    public static void buildPrintFramePage(PrintEdgeHost host) {
        host.setPfPage(new LinearLayout(host.context()));
        host.getPfPage().setOrientation(LinearLayout.VERTICAL);
        host.getPfPage().addView(host.pageHeadHostCall(tr("ui170.228")), new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));

        TextView note = host.labelHostCall(tr("ui170.229")
                + tr("ui170.230")
                + tr("ui170.231")
                + tr("ui170.232"),
                7.5f, host.getUI_MUTED(), host.getTfHan());
        note.setPadding(host.dpHostCall(2), 0, host.dpHostCall(2), host.dpHostCall(6));
        host.getPfPage().addView(note, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout row = new LinearLayout(host.context());
        row.setOrientation(LinearLayout.HORIZONTAL);
        host.choiceCellHostCall(row, tr("ui170.233"), host.pfLabelHostCall(), tv -> { host.setPfValue(tv); host.showPfPickerHostCall(); });
        host.getPfPage().addView(row, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));

        // **The body scrolls, like the other two sub-pages.** 片边 and 日期水印
        // both sit inside a `cappedScroll`; this page was the only one whose
        // rows hung straight off the page, so its natural height was whatever
        // the device made of three rows and a note -- and the page is a child of
        // the panel, which is `wrap_content` inside a column where the canvas is
        // the only weight. A page that measures tall therefore takes the canvas
        // with it: 边框 → 印刷边框 covered the whole window, tab bar and all.
        // Capped at 240 dp the same rows scroll, and the photograph keeps its
        // half of the screen whatever the font scale does.
        LinearLayout body = new LinearLayout(host.context());
        body.setOrientation(LinearLayout.VERTICAL);

        host.setPfMarginBar(host.rangeRowHostCall(body, tr("ui170.234"), tr("ui170.235"),
                0.5f, 20f, host.getPfMargin(), 0.5f, v -> host.setPfMargin(v)));
        host.setPfFxBar(host.rangeRowHostCall(body, tr("ui170.236"), tr("ui170.237"),
                -1f, 1f, host.getPfFx(), 0.01f, v -> host.setPfFx(v)));
        host.setPfFyBar(host.rangeRowHostCall(body, tr("ui170.238"), tr("ui170.239"),
                -1f, 1f, host.getPfFy(), 0.01f, v -> host.setPfFy(v)));

        TextView only = host.labelHostCall(tr("ui170.240"),
                7.5f, host.getUI_MUTED(), host.getTfHan());
        only.setPadding(host.dpHostCall(2), host.dpHostCall(8), host.dpHostCall(2), host.dpHostCall(4));
        body.addView(only, new LinearLayout.LayoutParams(-1, -2));
        host.getPfPage().addView(host.cappedScrollHostCall(body, 240), new LinearLayout.LayoutParams(-1, -2));
        host.refreshPfRowsHostCall();
    }
}
