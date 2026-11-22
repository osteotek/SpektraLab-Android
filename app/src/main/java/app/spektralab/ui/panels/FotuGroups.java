package app.spektralab.ui.panels;
import app.spektralab.ui.Square;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.MainActivity.CappedScroll;

/** FotuGroups builders extracted from MainActivity (architecture Phase C). */
public final class FotuGroups {
    private FotuGroups() {}

    public static void showFotuGroup(FotuGroupsHost host, int index) {
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            final int wanted = index;
            host.runOnUiThreadHostCall(() -> showFotuGroup(host, wanted));
            return;
        }
        final boolean moved = index != host.getFotuGroupShown();
        host.setFotuGroup(index);
        host.setFotuGroupShown(index);
        if (host.getFotuBody() == null) return;
        // 影调 -> 色彩 is a different number of rows, so the block's height moves
        // here too and the photograph above would jump with it. The height that
        // moves is read further down, off the box that is actually visible.
        host.syncSubRowActiveHostCall();
        if (host.getFotuGroupTabs() != null) {
            for (int i = 0; i < host.getFotuGroupTabs().length; ++i) {
                // A colour change is an `invalidate`, not a measure, so these
                // travel with the height instead of snapping ahead of it.
                host.tintTextHostCall(host.getFotuGroupTabs()[i], i == index ? host.getUI_INK() : host.getUI_MUTED(), host.getMS_SOFT());
                host.underlineViaHost(host.getFotuGroupTabs()[i], i == index);
            }
        }
        host.getFotuRefreshers().clear();
        host.getFotuBody().removeAllViews();
        // The push row only exists on one page; a stale reference would have
        // `refreshFotuPush` writing into a detached label.
        host.setFotuPushValue(null);
        try {
            if (index == 0) buildFotuToneGroup(host);
            else if (index == 1) buildFotuColourGroup(host);
            else if (index == 2) buildFotuFilmGroup(host);
            else if (index == 3) buildFotuGrainGroup(host);
            else if (index == 4) buildFotuHalationGroup(host);
            else if (index == 5) buildFotuLensGroup(host);
            else buildFotuPaperGroup(host);
        } catch (Throwable t) {
            // **A page that cannot be built is a line of text, not a crash.**
            // This runs on the launch path, where an exception takes the whole
            // activity with it -- and the report then never leaves the device.
            host.getFotuRefreshers().clear();
            host.getFotuBody().removeAllViews();
            host.getFotuBody().addView(host.labelHostCall(tr("ui170.186") + t, 8f, host.getUI_INK(), host.getTfHan()));
            try {
                DebugSink.text("fotupanel", "group " + index + ": " + t + "\n"
                        + android.util.Log.getStackTraceString(t));
            } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:33721", ignored); }
        }
        // **The body was rebuilt in place -- same view, new children.** So this
        // is one of the two callers that has to force the entrance: `fotuBody`
        // is never hidden, and the "already on screen" guard would skip it.
        if (moved) {
            host.getFotuBody().animate().cancel();
            host.getFotuBody().setAlpha(1f);
            host.getFotuBody().setTranslationY(0f);
            host.componentArrivalHostCall(host.getFotuBody());
            // **The height that visibly changes is the scroll's.** `fotuBody`
            // lives inside a `CappedScroll`, so `enterPanel(fotuBody, ...)` was
            // travelling a height nobody can see: the box stayed pinned at its
            // cap and 影调 -> 色彩 arrived with no height animation at all.
            // Both ends are capped first, then the box travels between them, and
            // the canvas above breathes like it does on every other page.
            final View box = host.getFotuBody().getParent() instanceof View
                    ? (View) host.getFotuBody().getParent() : null;
            final int cap = box instanceof CappedScroll
                    ? ((CappedScroll) box).cap() : Integer.MAX_VALUE;
            final int w = host.getFotuBody().getWidth() > 0 ? host.getFotuBody().getWidth()
                    : (box != null ? box.getWidth() : 0);
            final int was = box != null ? box.getHeight() : 0;
            final int want = w > 0 ? Math.min(host.naturalHeightViaHost(host.getFotuBody(), w), cap) : 0;
            if (box != null && want > 0 && was > 0 && want != was) {
                host.animateHeightToHostCall(box, was, want);
            }
        }
        host.refreshFotuPushHostCall();
    }

    public static void buildFotuToneGroup(FotuGroupsHost host) {
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.exposure"), -2f, 2f, () -> host.getFotuEv(), v -> host.setFotuEv(v),
                tr("ui170.478")
                + tr("ui170.479"));
        // **Only the screen medium has one.** The styles are how a
        // negative becomes a *picture* rather than a print, and the host
        // consults them only when it is building the screen tables -- on
        // paper they change nothing, so offering them there is a switch
        // that does not move.
        if (host.fotuScreenMediumHostCall()) {
            host.fotuChoiceRowHostCall(host.getFotuBody(), tr("ui170.480"), tr("ui170.481"),
                    new String[]{"", "graded-print", "reference-exposure"},
                    new String[]{tr("ui170.482"), tr("ui170.483"), tr("ui170.484")},
                    () -> host.getFotuDigitalRef() == null ? "" : host.getFotuDigitalRef(), v -> host.setFotuDigitalRef(v));
        }
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.highlights"), -1f, 1f, () -> host.getFotuHighlights(), v -> host.setFotuHighlights(v), tr("ui170.485"));
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.shade"), -1f, 1f, () -> host.getFotuShadows(), v -> host.setFotuShadows(v), tr("ui170.486"));
        // It is the *region* switch for the two sliders below it: with
        // both at zero the host never reads it, which is why it sat at
        // the top of the panel doing nothing.
        // **The mark is made here, not held from some earlier build.** It used
        // to be a field assigned once, back when this page was one flat column;
        // the pages rebuild, so the field has to follow the square that is
        // actually in the hierarchy. Leaving it null is an NPE inside
        // `toggleRow` on the launch path -- the app did not open.
        host.setFotuLocalToneMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.getFotuBody().addView(host.toggleRowHostCall(tr("ui170.487"), host.getFotuLocalToneMark(), () -> host.getFotuLocalTone() > 0.5f, on -> {
            host.setFotuLocalTone(on ? 1f : 0f);
            host.renderImageHostCall();
        }));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.488"), 0f, 1f, () -> host.getFotuShadowDetail(), v -> host.setFotuShadowDetail(v),
                tr("ui170.489"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.490"), 0f, 1f, () -> host.getFotuHighlightDetail(), v -> host.setFotuHighlightDetail(v),
                tr("ui170.491"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.492"), 0f, 1f, () -> host.getFotuPrintCorrection(), v -> host.setFotuPrintCorrection(v),
                tr("ui170.493"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.494"), 0f, 0.5f, () -> host.getFotuBlackPoint(), v -> host.setFotuBlackPoint(v),
                tr("ui170.495"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.496"), 0.5f, 1f, () -> host.getFotuWhitePoint(), v -> host.setFotuWhitePoint(v),
                tr("ui170.497"));
    }

    public static void buildFotuColourGroup(FotuGroupsHost host) {
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.saturation"), 0f, 2f, () -> host.getFotuSaturation(), v -> host.setFotuSaturation(v), tr("ui170.498"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.499"), -1f, 1f, () -> host.getFotuVibrance(), v -> host.setFotuVibrance(v), tr("ui170.500"));
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.temperature"), -1f, 1f, () -> host.getFotuWarmth(), v -> host.setFotuWarmth(v), tr("ui170.501"));
        host.fotuRowHostCall(host.getFotuBody(), tr("optics.tint"), -1f, 1f, () -> host.getFotuTint(), v -> host.setFotuTint(v), tr("ui170.502"));
        host.fotuRowHostCall(host.getFotuBody(), tr("print.knob.interlayer_inhibition"), 0f, 2f, () -> host.getFotuCouplers(), v -> host.setFotuCouplers(v),
                tr("ui170.503"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.504"), -1f, 1f, () -> host.getFotuGradeLift(), v -> host.setFotuGradeLift(v), null);
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.505"), -1f, 1f, () -> host.getFotuGradeGain(), v -> host.setFotuGradeGain(v), null);
        // Meaningful once lift or gain is away from zero.
        host.fotuChoiceRowHostCall(host.getFotuBody(), tr("ui170.506"), tr("ui170.507"),
                new String[]{"", "encoded"}, new String[]{tr("ui170.508"), tr("ui170.509")},
                () -> host.getFotuEncodedGrade() > 0.5f ? "encoded" : "", v -> host.setFotuEncodedGrade(v.isEmpty() ? 0f : 1f));
    }

    public static void buildFotuFilmGroup(FotuGroupsHost host) {
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.510"), 0f, 0.25f, () -> host.getFotuPreflash(), v -> host.setFotuPreflash(v), tr("ui170.511"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.512"), 0f, 20f, () -> host.getFotuExpired(), v -> host.setFotuExpired(v), tr("ui170.513"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.514"), 0f, 1f, () -> host.getFotuBleach(), v -> host.setFotuBleach(v), tr("ui170.515"));

        final LinearLayout pushRow = new LinearLayout(host.context());
        pushRow.setOrientation(LinearLayout.HORIZONTAL);
        pushRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        pushRow.setPadding(0, host.dpHostCall(6), 0, host.dpHostCall(6));
        pushRow.addView(host.labelHostCall(tr("ui170.467"), 9, host.getUI_INK(), host.getTfHan()), new LinearLayout.LayoutParams(0, -2, 1f));
        host.setFotuPushValue(host.labelHostCall("", 9, host.getUI_BLUE(), host.getTfBody()));
        pushRow.addView(host.getFotuPushValue());
        pushRow.addView(host.labelHostCall(" ↓", 10, host.getUI_BLUE(), host.getTfBody()));
        pushRow.setClickable(true);
        pushRow.setOnClickListener(v -> host.showFotuPushPickerHostCall());
        host.getFotuBody().addView(pushRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(34)));
    }

    public static void buildFotuGrainGroup(FotuGroupsHost host) {
        host.setFotuFilmGrainMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.getFotuBody().addView(host.toggleRowHostCall(tr("ui170.516"), host.getFotuFilmGrainMark(),
                () -> host.getFotuFilmGrain(), on -> {
                    host.setFotuFilmGrain(on);
                    host.refreshGrainGatesHostCall();
                    host.renderImageHostCall();
                }));
        host.getGrainGated().clear();
        host.setFotuGrainNote(host.labelHostCall("", 7.5f, host.getUI_MUTED(), host.getTfHan()));
        host.getFotuGrainNote().setPadding(host.dpHostCall(2), host.dpHostCall(2), host.dpHostCall(2), host.dpHostCall(8));
        host.getFotuBody().addView(host.getFotuGrainNote(), new LinearLayout.LayoutParams(-1, -2));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.517"), 0f, 2f, () -> host.getFotuGrain(), v -> host.setFotuGrain(v),
                tr("ui170.518"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.519"), 0.5f, 2f, () -> host.getFotuGrainSize(), v -> host.setFotuGrainSize(v),
                tr("ui170.520"));
        // **The five that only exist under the tile model.** 彩色颗粒, 扫描柔度 and
        // the three layer shares are slots the host reads while it samples a laid
        // out tile bank; on the default clump model they used to slide and change
        // no pixel at all. They are gated now, not merely annotated.
        host.gateGrainHostCall(host.fotuRowHostCall(host.getFotuBody(), tr("ui170.521"), 0f, 1f, () -> host.getFotuGrainColour(), v -> host.setFotuGrainColour(v),
                tr("ui170.522")));
        host.gateGrainHostCall(host.fotuRowHostCall(host.getFotuBody(), tr("ui170.523"), 0.5f, 2f, () -> host.getFotuGrainSoft(), v -> host.setFotuGrainSoft(v),
                tr("ui170.524")));
        host.gateGrainHostCall(host.fotuRowHostCall(host.getFotuBody(), tr("ui170.525"), 0f, 2f, () -> host.getFotuGrainR(), v -> host.setFotuGrainR(v),
                tr("ui170.526")));
        host.gateGrainHostCall(host.fotuRowHostCall(host.getFotuBody(), tr("ui170.527"), 0f, 2f, () -> host.getFotuGrainG(), v -> host.setFotuGrainG(v),
                tr("ui170.528")));
        host.gateGrainHostCall(host.fotuRowHostCall(host.getFotuBody(), tr("ui170.529"), 0f, 2f, () -> host.getFotuGrainB(), v -> host.setFotuGrainB(v),
                tr("ui170.530")));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.531"), 0f, 0.9f, () -> host.getFotuMottle(), v -> host.setFotuMottle(v),
                tr("ui170.532"));
        host.refreshGrainGatesHostCall();
    }

    public static void buildFotuHalationGroup(FotuGroupsHost host) {
        host.fotuRowHostCall(host.getFotuBody(), tr("print.knob.halation"), 0f, 4f, () -> host.getFotuHalation(), v -> host.setFotuHalation(v),
                tr("ui170.533"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.534"), 0f, 1f, () -> host.getFotuHalationColour(), v -> host.setFotuHalationColour(v),
                tr("ui170.535"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.536"), 0f, 3f, () -> host.getFotuCouplerReach(), v -> host.setFotuCouplerReach(v),
                tr("ui170.537"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.538"), 0f, 3f, () -> host.getFotuCouplerRG(), v -> host.setFotuCouplerRG(v),
                tr("ui170.539"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.540"), 0f, 3f, () -> host.getFotuCouplerGB(), v -> host.setFotuCouplerGB(v),
                tr("ui170.541"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.542"), 0f, 3f, () -> host.getFotuCouplerSelf(), v -> host.setFotuCouplerSelf(v),
                tr("ui170.543"));
    }

    public static void buildFotuLensGroup(FotuGroupsHost host) {
        host.fotuRowHostCall(host.getFotuBody(), tr("print.knob.glare"), 0f, 1f, () -> host.getFotuFlare(), v -> host.setFotuFlare(v), tr("ui170.544"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.545"), 0f, 1f, () -> host.getFotuFringe(), v -> host.setFotuFringe(v),
                tr("ui170.546"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.547"), 20f, 300f, () -> host.getFotuFringeRadius(), v -> host.setFotuFringeRadius(v),
                tr("ui170.548"));
        host.fotuChoiceRowHostCall(host.getFotuBody(), tr("ui170.549"), tr("ui170.550"),
                new String[]{"", "condenser"}, new String[]{tr("ui170.551"), tr("ui170.552")},
                () -> host.getFotuCondenser() > 0.5f ? "condenser" : "", v -> host.setFotuCondenser(v.isEmpty() ? 0f : 1f));
    }

    public static void buildFotuPaperGroup(FotuGroupsHost host) {
        // **Two switches, one row.** Same rule as the other engine's 影调 page:
        // two of them stacked cost 96 dp of a 250 dp page to say "on" twice.
        final LinearLayout paperCells = new LinearLayout(host.context());
        paperCells.setOrientation(LinearLayout.HORIZONTAL);
        host.setFotuRebateMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.toggleCellHostCall(paperCells, tr("border.film_border"), tr("ui170.553"), host.getFotuRebateMark(), () -> host.getOverscanOn(), on -> {
            host.setOverscanOn(on);
            if (host.getOverscanMark() != null) host.getOverscanMark().setLit(on);
            // **The picture takes the gate's shape.** The host lays the frame in
            // the gate and refuses a shape that is not the gate's (its own
            // E-1305); `applyGate` is the cut that makes it one.
            if (on) host.applyGateHostCall(host.getOverscanFormat()); else host.renderFotuHostCall();
        });
        host.setFotuPrinterMark(new Square(host.context(), host.getUI_BLUE(), host.uiToggleOffViaHost()));
        host.toggleCellHostCall(paperCells, tr("ui170.554"), tr("ui170.555"), host.getFotuPrinterMark(),
                () -> host.getFotuPrinter() > 0.5f, on -> {
                    host.setFotuPrinter(on ? 1f : 0f);
                    host.renderImageHostCall();
                });
        host.getFotuBody().addView(paperCells, new LinearLayout.LayoutParams(-1, host.dpHostCall(46)));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.556"), 2800f, 3600f, () -> host.getFotuPrinterLamp(), v -> host.setFotuPrinterLamp(v),
                tr("ui170.557"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.558"), 0f, 1.2f, () -> host.getFotuPrinterM(), v -> host.setFotuPrinterM(v),
                tr("ui170.559"));
        host.fotuRowHostCall(host.getFotuBody(), tr("print.knob.yellow_filter"), 0f, 1.2f, () -> host.getFotuPrinterY(), v -> host.setFotuPrinterY(v),
                tr("ui170.560"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.561"), -6f, 6f, () -> host.getFotuPrinterEv(), v -> host.setFotuPrinterEv(v),
                tr("ui170.562"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.563"), 1f, 50f, () -> host.getFotuPrinterR(), v -> host.setFotuPrinterR(v),
                tr("ui170.564"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.565"), 1f, 50f, () -> host.getFotuPrinterG(), v -> host.setFotuPrinterG(v),
                tr("ui170.566"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.567"), 1f, 50f, () -> host.getFotuPrinterB(), v -> host.setFotuPrinterB(v),
                tr("ui170.568"));
        host.fotuRowHostCall(host.getFotuBody(), tr("ui170.569"), 0f, 0.1f, () -> host.getFotuPrinterPreflash(), v -> host.setFotuPrinterPreflash(v),
                tr("ui170.570"));
        host.fotuChoiceRowHostCall(host.getFotuBody(), tr("ui170.571"), tr("ui170.572"),
                new String[]{"", "d65", "tungsten"}, new String[]{tr("ui170.573"), tr("ui170.574"), tr("ui170.575")},
                () -> host.getFotuPrintLight(), v -> host.setFotuPrintLight(v));
    }
}
