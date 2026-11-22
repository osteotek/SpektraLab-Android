package app.spektralab.ui.panels;
import app.spektralab.ui.RatioMark;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.Probe;
import app.spektralab.MainActivity.CropOverlay;
import app.spektralab.MainActivity.RotateScale;

/** ComposePages builders extracted from MainActivity (architecture Phase C). */
public final class ComposePages {
    private ComposePages() {}

    public static void showComposePage(ComposePagesHost host, int page) {
        host.setComposeSubPage(page);
        if (host.getComposeBody() == null) return;
        host.getComposeCropTab().setTextColor(page == 0 ? host.getUI_INK() : host.getUI_MUTED());
        host.getComposeRotateTab().setTextColor(page == 1 ? host.getUI_INK() : host.getUI_MUTED());
        host.underlineViaHost(host.getComposeCropTab(), page == 0);
        host.underlineViaHost(host.getComposeRotateTab(), page == 1);
        if (host.getCropScroll() != null) host.setCropScrollX(host.getCropScroll().getScrollX());
        host.getComposeBody().removeAllViews();
        if (page == 0) buildCropPage(host); else buildRotatePage(host);
        host.syncRotateGridHostCall();
        if (host.getCropOverlay() != null) {
            // **Only while 构图 → 裁切 is the page in front.** The page is
            // built during `onCreate`, long before `setTab` runs, so keying on
            // `page == 0` alone left the frame drawn over every other page --
            // which is exactly what tr("ui170.202") was.
            final boolean show = host.getActiveTabIndex() == 4 && page == 0;
            host.getCropOverlay().setVisibility(show ? View.VISIBLE : View.GONE);
            if (show) {
                host.getCropOverlay().sync(host.getPendingCropX(), host.getPendingCropY(), host.getPendingCropW(), host.getPendingCropH(),
                        host.cropAspectForSelectionHostCall());
            }
        }
    }

    public static void buildCropPage(ComposePagesHost host) {
        final HorizontalScrollView scroll = new HorizontalScrollView(host.context());
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipToPadding(false);
        final LinearLayout strip = new LinearLayout(host.context());
        strip.setOrientation(LinearLayout.HORIZONTAL);
        // **Room for the border on the bottom edge.** The cards are 94 dp and
        // the strip used to be 94 dp tall with a 10 dp top pad, so the bottom
        // of every card -- and with it the selected card's outline -- was
        // clipped away. The strip is as tall as its children plus both pads.
        strip.setPadding(0, host.dpHostCall(8), 0, host.dpHostCall(6));

        for (int i = -1; i < host.getEDGE_LABELS().length; ++i) {
            final int index = i;
            final boolean original = i < 0;
            final boolean active = host.getCropRatioIndex() == index;
            final float ratio = original ? host.originalAspectHostCall() : (float) host.getEDGE_RATIOS()[i];

            final LinearLayout card = new LinearLayout(host.context());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            card.setBackground(host.cardBorderHostCall(active));
            card.setPadding(host.dpHostCall(3), host.dpHostCall(6), host.dpHostCall(3), host.dpHostCall(4));

            card.addView(new RatioMark(host.context(), ratio, active ? host.getUI_BLUE() : host.getUI_INK()),
                    new LinearLayout.LayoutParams(-1, host.dpHostCall(44)));

            final TextView caption = host.labelHostCall(original ? tr("compose.original") : host.getEDGE_RATIO_LABELS()[i],
                    8.5f, active ? host.getUI_INK() : host.getUI_MUTED(), host.getTfBody());
            caption.setGravity(android.view.Gravity.CENTER);
            card.addView(caption, new LinearLayout.LayoutParams(-1, -2));

            final TextView gate = host.labelHostCall(original ? tr("compose.full_frame") : host.getEDGE_LABELS()[i], 6.5f, host.getUI_MUTED(), host.getTfHan());
            gate.setGravity(android.view.Gravity.CENTER);
            card.addView(gate, new LinearLayout.LayoutParams(-1, -2));

            card.setOnClickListener(v -> host.applyCropRatioHostCall(index));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(host.dpHostCall(76), host.dpHostCall(94));
            lp.rightMargin = host.dpHostCall(6);
            strip.addView(card, lp);
        }
        for (int i = 0; i < host.getPORTRAIT_RATIOS().length; ++i) {
            final int index = host.getEDGE_LABELS().length + i;
            final boolean active = host.getCropRatioIndex() == index;
            final float ratio = host.getPORTRAIT_RATIOS()[i];

            final LinearLayout card = new LinearLayout(host.context());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            card.setBackground(host.cardBorderHostCall(active));
            card.setPadding(host.dpHostCall(3), host.dpHostCall(6), host.dpHostCall(3), host.dpHostCall(4));
            card.addView(new RatioMark(host.context(), ratio, active ? host.getUI_BLUE() : host.getUI_INK()),
                    new LinearLayout.LayoutParams(-1, host.dpHostCall(44)));
            final TextView caption = host.labelHostCall(host.getPORTRAIT_LABELS()[i], 8.5f,
                    active ? host.getUI_INK() : host.getUI_MUTED(), host.getTfBody());
            caption.setGravity(android.view.Gravity.CENTER);
            card.addView(caption, new LinearLayout.LayoutParams(-1, -2));
            final TextView gate = host.labelHostCall(tr("compose.portrait"), 6.5f, host.getUI_MUTED(), host.getTfHan());
            gate.setGravity(android.view.Gravity.CENTER);
            card.addView(gate, new LinearLayout.LayoutParams(-1, -2));

            card.setOnClickListener(v -> host.applyCropRatioHostCall(index));
            final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(host.dpHostCall(76), host.dpHostCall(94));
            lp.rightMargin = host.dpHostCall(6);
            strip.addView(card, lp);
        }
        scroll.addView(strip, new android.widget.FrameLayout.LayoutParams(-2, host.dpHostCall(108)));
        host.setCropScroll(scroll);
        host.getComposeBody().addView(scroll, new LinearLayout.LayoutParams(-1, host.dpHostCall(108)));
        // **Restore the scroll after layout, not before it.** `post` can run
        // while the new strip is still unmeasured; `scrollTo` then clamps
        // against a zero content width and the strip lands back on the first
        // card. Picking one of the portrait ratios at the end of the strip
        // therefore snapped to the start on every tap -- the tr("ui170.203") on the last
        // few cards. `OnPreDraw` runs after measure/layout and before the
        // frame is drawn, which is the first moment `scrollTo` means anything.
        scroll.getViewTreeObserver().addOnPreDrawListener(
                new android.view.ViewTreeObserver.OnPreDrawListener() {
                    @Override public boolean onPreDraw() {
                        scroll.getViewTreeObserver().removeOnPreDrawListener(this);
                        scroll.scrollTo(host.getCropScrollX(), 0);
                        Probe.log("crop", "scroll " + host.getCropScrollX() + " -> " + scroll.getScrollX());
                        return true;
                    }
                });

        final LinearLayout footRow = new LinearLayout(host.context());
        footRow.setOrientation(LinearLayout.HORIZONTAL);
        footRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        final TextView foot = host.labelHostCall(host.cropSummaryHostCall(), 8.5f, host.getUI_MUTED(), host.getTfHan());
        footRow.addView(foot, new LinearLayout.LayoutParams(0, -2, 1f));

        // **Nothing happens to the picture until 应用.** 取消 throws the
        // proposal away; the frame goes back to the whole canvas.
        final TextView cancel = host.labelHostCall(tr("editor.cancel"), 9, host.getUI_MUTED(), host.getTfHan());
        cancel.setGravity(android.view.Gravity.CENTER);
        cancel.setBackground(host.cardBorderHostCall(false));
        cancel.setOnClickListener(v -> {
            host.setPendingCropX(0f); host.setPendingCropY(0f);
            host.setPendingCropW(1f); host.setPendingCropH(1f);
            host.setCropRatioIndex(-1);
            showComposePage(host, 0);
        });
        final LinearLayout.LayoutParams cancelLp = new LinearLayout.LayoutParams(host.dpHostCall(58), host.dpHostCall(30));
        cancelLp.rightMargin = host.dpHostCall(6);
        footRow.addView(cancel, cancelLp);

        final TextView apply = host.labelHostCall(tr("compose.apply_crop"), 9, host.getUI_BLUE(), host.getTfHan());
        apply.setGravity(android.view.Gravity.CENTER);
        apply.setBackground(host.cardBorderHostCall(true));
        apply.setOnClickListener(v -> host.commitPendingCropHostCall());
        footRow.addView(apply, new LinearLayout.LayoutParams(host.dpHostCall(84), host.dpHostCall(30)));

        host.getComposeBody().addView(footRow, new LinearLayout.LayoutParams(-1, host.dpHostCall(38)));
    }

    public static void buildRotatePage(ComposePagesHost host) {
        // **The gesture is on the picture; the scale is the readout.** This
        // page used to make the finger turn a dial while the picture sat still,
        // which is the wrong way round: 修偏 is done by watching the horizon.
        final RotateScale arc = host.newRotateScale();
        arc.setAngle(host.getGeometryRotation());
        host.setRotateArc(arc);
        android.widget.FrameLayout arcBox = new android.widget.FrameLayout(host.context());
        arcBox.addView(arc, new android.widget.FrameLayout.LayoutParams(-1, -1));
        final TextView gridChip = host.labelHostCall("", 8f, host.getUI_BLUE(), host.getTfHan());
        final Runnable paintChip = () -> gridChip.setText(host.getRotateGridOn() ? tr("compose.guides_on") : tr("compose.guides_off"));
        paintChip.run();
        gridChip.setPadding(host.dpHostCall(8), host.dpHostCall(4), host.dpHostCall(8), host.dpHostCall(4));
        gridChip.setOnClickListener(v -> {
            host.setRotateGridOn(!host.getRotateGridOn());
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit()
                    .putBoolean("rotate_grid", host.getRotateGridOn()).apply();
            paintChip.run();
            host.syncRotateGridHostCall();
        });
        android.widget.FrameLayout.LayoutParams chipLp = new android.widget.FrameLayout.LayoutParams(
                -2, -2, android.view.Gravity.TOP | android.view.Gravity.END);
        chipLp.topMargin = 0;
        chipLp.rightMargin = host.dpHostCall(6);
        arcBox.addView(gridChip, chipLp);
        host.getComposeBody().addView(arcBox, new LinearLayout.LayoutParams(-1, host.dpHostCall(70)));
        final TextView tip = host.labelHostCall(tr("compose.drag_the_dial_or_image"),
                7.5f, host.getUI_MUTED(), host.getTfHan());
        tip.setGravity(android.view.Gravity.CENTER);
        host.getComposeBody().addView(tip, new LinearLayout.LayoutParams(-1, host.dpHostCall(14)));

        final LinearLayout row = new LinearLayout(host.context());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, host.dpHostCall(12), 0, 0);

        final LinearLayout.LayoutParams cell = new LinearLayout.LayoutParams(0, -2, 1f);
        row.addView(host.circleActionHostCall("↺", tr("compose.left_90"), () -> host.turnByHostCall(-1)), cell);
        row.addView(host.circleActionHostCall("↻", tr("compose.right_90"), () -> host.turnByHostCall(1)),
                new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(host.circleActionHostCall("⇆", tr("compose.flip_horizontally"), () -> {
            host.setGeometryFlipH(!host.getGeometryFlipH());
            showComposePage(host, 1);
            host.commitComposeHostCall();
        }), new LinearLayout.LayoutParams(0, -2, 1f));
        row.addView(host.circleActionHostCall("⇅", tr("compose.flip_vertically"), () -> {
            host.setGeometryFlipV(!host.getGeometryFlipV());
            showComposePage(host, 1);
            host.commitComposeHostCall();
        }), new LinearLayout.LayoutParams(0, -2, 1f));
        host.getComposeBody().addView(row, new LinearLayout.LayoutParams(-1, -2));

        // No footnote under the buttons: the tip above the scale already says
        // what the gesture does, and this page is height-sensitive. (The old
        // one here still said tr("ui170.205") three pages after the dial was removed.)
    }
}
