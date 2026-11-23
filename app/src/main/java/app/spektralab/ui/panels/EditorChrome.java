package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.UpdateCheck;
import app.spektralab.ui.*;

/** EditorChrome builders extracted from MainActivity (architecture Phase C). */
public final class EditorChrome {
    private EditorChrome() {}

    public static void addCanvasChrome(EditorChromeHost host, android.widget.FrameLayout stage) {
        LinearLayout top = new LinearLayout(host.context());
        host.setEditorTopBar(top);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(android.view.Gravity.TOP);
        top.setPadding(host.dpHostCall(16), host.dpHostCall(12), host.dpHostCall(16), host.dpHostCall(8));
        top.setBackground(new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                host.topWashHostCall()));
        top.setClickable(false);

        LinearLayout brand = new LinearLayout(host.context());
        brand.setOrientation(LinearLayout.VERTICAL);
        brand.setPadding(0, host.dpHostCall(2), 0, 0);
        LinearLayout wordRow = new LinearLayout(host.context());
        wordRow.setOrientation(LinearLayout.HORIZONTAL);
        wordRow.setGravity(android.view.Gravity.TOP);
        // `UI_PAPER` is the panel colour: white on the light theme (correct on
        // the navy canvas) and near-black on the dark one, where it vanished
        // into the black. The wordmark is always the canvas's *contrast* colour.
        TextView word = host.labelHostCall("SpektraLab", 18, host.uiOnCanvasViaHost(), host.getTfDisplay());
        word.setLetterSpacing(-0.045f);
        word.setSingleLine(true);
        wordRow.addView(word);
        // **The menu is a drawn glyph, not a text "≡".** Next to a real icon a
        // font character reads as a mistake; Material's `menu` path is the
        // designed 24 dp shape. It replaces the long-press on the wordmark,
        // which nothing on screen advertised.
        final android.widget.ImageView menuPill = new android.widget.ImageView(host.context());
        menuPill.setImageResource(host.context().getResources().getIdentifier(
                "ic_menu_white", "drawable", host.context().getPackageName()));
        menuPill.setAlpha(0.85f);
        menuPill.setPadding(host.dpHostCall(6), host.dpHostCall(6), host.dpHostCall(6), host.dpHostCall(6));
        menuPill.setContentDescription(tr("editor.settings"));
        menuPill.setOnClickListener(v -> host.showSettingsHostCall());
        // **Left of the wordmark, not between it and the version.** The menu
        // belongs at the edge of the bar, where every other app puts it;
        // between the name and its version number it reads as part of the name.
        final LinearLayout.LayoutParams menuLp = new LinearLayout.LayoutParams(host.dpHostCall(30), host.dpHostCall(30));
        menuLp.rightMargin = host.dpHostCall(10);
        menuLp.topMargin = host.dpHostCall(1);
        wordRow.addView(menuPill, 0, menuLp);
        TextView version = host.labelHostCall("v" + UpdateCheck.versionName(host.context()), 6.5f, host.uiOnCanvasViaHost(), host.getTfBody());
        version.setAlpha(0.7f);
        LinearLayout.LayoutParams versionLp = new LinearLayout.LayoutParams(-2, -2);
        versionLp.leftMargin = host.dpHostCall(4);
        versionLp.topMargin = 0;
        wordRow.addView(version, versionLp);
        brand.addView(wordRow);
        top.addView(brand, new LinearLayout.LayoutParams(0, -2, 1f));

        // **A drawn glyph, not a text arrow.** "↶" is whatever the device's font
        // decides it is -- different weight, different curve, sometimes a tofu --
        // and next to a real icon it reads as a mistake. Material's `undo` path
        // is a designed 24 dp shape with predictable proportions.
        host.setUndoPill(new android.widget.ImageView(host.context()));
        // No `R` class in this project -- resources are looked up by name.
        host.getUndoPill().setImageResource(host.context().getResources().getIdentifier(
                "ic_undo_white", "drawable", host.context().getPackageName()));
        host.getUndoPill().setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        host.getUndoPill().setPadding(host.dpHostCall(7), host.dpHostCall(4), host.dpHostCall(7), host.dpHostCall(4));
        host.getUndoPill().setVisibility(View.GONE);
        host.getUndoPill().setOnClickListener(v -> host.undoStepHostCall());
        LinearLayout.LayoutParams undoLp = new LinearLayout.LayoutParams(-2, host.dpHostCall(32));
        undoLp.rightMargin = host.dpHostCall(6);
        top.addView(host.getUndoPill(), undoLp);

        host.setSharePill(host.labelHostCall(tr("editor.share"), 11, host.uiOnCanvasViaHost(), host.getTfHan()));
        host.getSharePill().setGravity(android.view.Gravity.CENTER);
        host.getSharePill().setShadowLayer(host.dpHostCall(3), 0, host.dpHostCall(1), 0xCC000000);
        host.getSharePill().setBackground(null);
        host.getSharePill().setPadding(host.dpHostCall(8), 0, host.dpHostCall(8), 0);
        host.getSharePill().setVisibility(View.GONE);
        host.getSharePill().setOnClickListener(v -> host.shareLastExportHostCall());
        LinearLayout.LayoutParams shareLp = new LinearLayout.LayoutParams(-2, host.dpHostCall(32));
        shareLp.rightMargin = host.dpHostCall(6);
        top.addView(host.getSharePill(), shareLp);

        // Material "file upload", for the same reason as the undo glyph: the
        // tray-and-arrow I drew by hand had a thin stroke and a lopsided arrow.
        final android.widget.ImageView export = new android.widget.ImageView(host.context());
        export.setImageResource(host.context().getResources().getIdentifier(
                "ic_export_white", "drawable", host.context().getPackageName()));
        export.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        export.setOnClickListener(v -> host.showExportDialogHostCall());
        top.addView(export, new LinearLayout.LayoutParams(host.dpHostCall(26), host.dpHostCall(26)));

        stage.addView(top, new android.widget.FrameLayout.LayoutParams(-1, host.dpHostCall(64), android.view.Gravity.TOP));

        // **The histogram lives on the photograph, not in a panel.** It is the
        // one readout that is about *this* frame, so it sits where the frame is
        // and updates with it; behind it a 30% wash keeps a bright sky from
        // swallowing the traces.
        // **Whether the histogram shows is a preference now.** It is the one
        // readout that is about *this* frame, so it sits on the frame; but a
        // plate over the top-left corner of a photograph is not always
        // wanted, and the switch is in 光学 -> 亮度.
        host.setHistogramOn(host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).getBoolean("histogram", true));
        host.setRotateGridOn(host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).getBoolean("rotate_grid", true));
        host.setRawAutoBright(host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE)
                .getBoolean("raw_auto_bright", false));
        host.setHistogram(new HistogramView(host.context()));
        // The curve page draws the same bins as a backdrop; without this it
        // would sit on the distribution from the last time *it* was laid out.
        host.getHistogram().setOnChanged(() -> { if (host.getCurveView() != null) host.getCurveView().invalidate(); });
        final android.widget.FrameLayout.LayoutParams hlp =
                new android.widget.FrameLayout.LayoutParams(host.dpHostCall(148), host.dpHostCall(54),
                        android.view.Gravity.TOP | android.view.Gravity.START);
        hlp.leftMargin = host.dpHostCall(14);
        hlp.topMargin = host.dpHostCall(62);
        stage.addView(host.getHistogram(), hlp);
        host.getHistogram().setVisibility(host.getHistogramOn() ? View.VISIBLE : View.GONE);
        // **Mirror of the histogram, on the right.** The pill says the frame has
        // a clip and is the only control the feature needs: tap it (or rest a
        // finger on the picture) and the clip plays, developed.
        host.setLiveBadge(host.labelHostCall("\u5b9e\u51b5", 7.5f, host.uiOnCanvasViaHost(), host.getTfBold()));
        host.getLiveBadge().setLetterSpacing(0.10f);
        host.getLiveBadge().setSingleLine(true);
        host.getLiveBadge().setGravity(android.view.Gravity.CENTER);
        host.getLiveBadge().setPadding(host.dpHostCall(10), host.dpHostCall(5), host.dpHostCall(10), host.dpHostCall(5));
        final android.graphics.drawable.GradientDrawable badgeBg =
                new android.graphics.drawable.GradientDrawable();
        badgeBg.setColor(0xCC141414);
        badgeBg.setCornerRadius(host.dpHostCall(3));
        badgeBg.setStroke(Math.max(1, host.dpHostCall(1)), 0x59F6F3EA);
        host.getLiveBadge().setBackground(badgeBg);
        host.getLiveBadge().setContentDescription("\u5b9e\u51b5\u7167\u7247\uff1a\u70b9\u4e00\u4e0b\u770b\u52a8\u56fe");
        host.getLiveBadge().setOnClickListener(v -> host.startLivePreviewHostCall());
        final android.widget.FrameLayout.LayoutParams blp =
                new android.widget.FrameLayout.LayoutParams(-2, -2,
                        android.view.Gravity.TOP | android.view.Gravity.END);
        blp.rightMargin = host.dpHostCall(14);
        blp.topMargin = host.dpHostCall(62);
        stage.addView(host.getLiveBadge(), blp);
        host.getLiveBadge().setVisibility(android.view.View.GONE);
        // **The same pill, one notch lower.** HDR 预览把画布换成半浮点副本（纸白
        // 可以高过 SDR 白），画面上却没有一个字说这件事 —— 和实况一样，它得自己
        // 说出来。点一下开档位表，和设置里那一行是同一个入口。
        host.setHdrBadge(host.labelHostCall("HDR", 7.5f, host.uiOnCanvasViaHost(), host.getTfBold()));
        host.getHdrBadge().setLetterSpacing(0.10f);
        host.getHdrBadge().setSingleLine(true);
        host.getHdrBadge().setGravity(android.view.Gravity.CENTER);
        host.getHdrBadge().setPadding(host.dpHostCall(10), host.dpHostCall(5), host.dpHostCall(10), host.dpHostCall(5));
        final android.graphics.drawable.GradientDrawable hdrBg =
                new android.graphics.drawable.GradientDrawable();
        hdrBg.setColor(0xCC141414);
        hdrBg.setCornerRadius(host.dpHostCall(3));
        hdrBg.setStroke(Math.max(1, host.dpHostCall(1)), 0x59F6F3EA);
        host.getHdrBadge().setBackground(hdrBg);
        host.getHdrBadge().setContentDescription(tr("ui170.086"));
        host.getHdrBadge().setOnClickListener(v -> host.showHdrSheetHostCall());
        final android.widget.FrameLayout.LayoutParams hlp2 =
                new android.widget.FrameLayout.LayoutParams(-2, -2,
                        android.view.Gravity.TOP | android.view.Gravity.END);
        hlp2.rightMargin = host.dpHostCall(14);
        hlp2.topMargin = host.dpHostCall(90);
        stage.addView(host.getHdrBadge(), hlp2);
        host.getHdrBadge().setVisibility(android.view.View.GONE);
        if (host.getLastRenderedBitmap() != null) host.getHistogram().setBitmap(host.getLastRenderedBitmap());
    }

    public static void applyEditorLayout(EditorChromeHost host) {
        if (!(host.getEditorRoot() instanceof LinearLayout) || host.getLeftColumn() == null || host.getControls() == null
                || host.getCanvasStage() == null || host.getEditorSourceBar() == null || host.getPanelHolder() == null
                || host.getTabsRoot() == null || host.getSubBar() == null) return;
        final LinearLayout app = (LinearLayout) host.getEditorRoot();
        final boolean land = host.isLandscapeNowHostCall();
        host.setLandscapeMode(land);
        try {
            host.markFileViaHost("layout " + (land ? "landscape" : "portrait")
                    + " " + host.context().getResources().getDisplayMetrics().widthPixels
                    + "x" + host.context().getResources().getDisplayMetrics().heightPixels
                    + " rail=" + host.controlsWidthPxHostCall() + " cols=" + (land ? 2 : 1));
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:13486", ignored); }

        app.removeAllViews();
        host.getLeftColumn().removeAllViews();
        host.getControls().removeAllViews();
        if (host.getPanelScroll() != null) host.getPanelScroll().removeAllViews();
        // **Not `paperDock.removeAllViews()`.** The dock owns a hairline as well,
        // and emptying it would take that with it the first time the phone
        // turned back to portrait. Only the paper row travels.
        if (host.getFilmPaperRow() != null && host.getFilmPaperRow().getParent() instanceof android.view.ViewGroup) {
            ((android.view.ViewGroup) host.getFilmPaperRow().getParent()).removeView(host.getFilmPaperRow());
        }

        host.getLeftColumn().addView(host.getCanvasStage(), new LinearLayout.LayoutParams(-1, 0, 1f));
        host.getLeftColumn().addView(host.getEditorSourceBar(), new LinearLayout.LayoutParams(-1, host.dpHostCall(62)));

        if (land) {
            app.setOrientation(LinearLayout.HORIZONTAL);
            app.addView(host.getLeftColumn(), new LinearLayout.LayoutParams(0, -1, 1f));
            app.addView(host.editorRuleHostCall(), new LinearLayout.LayoutParams(Math.max(1, host.dpHostCall(0.6f)), -1));

            // **页签在右栏顶上。** 竖屏时它在屏幕最底（拇指够得到）；横屏时
            // 屏幕最底离右栏的正文最远，一列工具的第一个东西应该是它的目录。
            if (host.getPanelScroll() == null) {
                host.setPanelScroll(new android.widget.ScrollView(host.context()));
                host.getPanelScroll().setFillViewport(false);
                host.getPanelScroll().setVerticalScrollBarEnabled(true);
            }
            host.getPanelScroll().addView(host.getPanelHolder(), new LinearLayout.LayoutParams(-1, -2));
            if (host.getPaperDock() == null) {
                host.setPaperDock(new LinearLayout(host.context()));
                host.getPaperDock().setOrientation(LinearLayout.VERTICAL);
                host.getPaperDock().setBackgroundColor(host.getUI_PAPER());
                final View rule = new View(host.context());
                rule.setBackgroundColor(host.getUI_LINE());
                host.getPaperDock().addView(rule, new LinearLayout.LayoutParams(-1, Math.max(1, host.dpHostCall(0.6f))));
            }
            if (host.getFilmPaperRow() != null) {
                host.getPaperDock().addView(host.getFilmPaperRow(), new LinearLayout.LayoutParams(-1, host.dpHostCall(35)));
            }

            host.getControls().addView(host.getTabsRoot(), new LinearLayout.LayoutParams(-1, host.dpHostCall(host.getTAB_BAR_DP())));
            host.getControls().addView(host.getSubBar(), new LinearLayout.LayoutParams(-1, -2));
            host.getControls().addView(host.getPanelScroll(), new LinearLayout.LayoutParams(-1, 0, 1f));
            host.getControls().addView(host.getPaperDock(), new LinearLayout.LayoutParams(-1, -2));
            app.addView(host.getControls(), new LinearLayout.LayoutParams(host.controlsWidthPxHostCall(), -1));
        } else {
            app.setOrientation(LinearLayout.VERTICAL);
            app.addView(host.getLeftColumn(), new LinearLayout.LayoutParams(-1, 0, 1f));
            if (host.getFilmPaperRow() != null && host.getPanels().length > 0 && host.getPanels()[0] instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) host.getPanels()[0]).addView(host.getFilmPaperRow(),
                        new LinearLayout.LayoutParams(-1, host.dpHostCall(35)));
            }
            host.getControls().addView(host.getPanelHolder(), new LinearLayout.LayoutParams(-1, -2));
            host.getControls().addView(host.getSubBar(), new LinearLayout.LayoutParams(-1, -2));
            host.getControls().addView(host.getTabsRoot(), new LinearLayout.LayoutParams(-1, host.dpHostCall(host.getTAB_BAR_DP())));
            app.addView(host.getControls(), new LinearLayout.LayoutParams(-1, -2));
        }

        // 折叠只在竖屏有意义：横屏的右栏是定高的，收起面板也换不回画面。
        host.setPanelCollapsed(false);
        host.cancelHeightHostCall(host.getPanelHolder());
        host.getPanelHolder().animate().cancel();
        host.getPanelHolder().setVisibility(View.VISIBLE);
        host.getPanelHolder().setAlpha(1f);
        host.getPanelHolder().setTranslationY(0f);
        final android.view.ViewGroup.LayoutParams hl = host.getPanelHolder().getLayoutParams();
        if (hl != null) {
            hl.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            host.getPanelHolder().setLayoutParams(hl);
        }

        // **每个面板都还给自己的内容。** 上一次 `enterPanel` 留下的固定高度
        // 是竖屏的尺寸；换成两栏之后，横屏那面网格会被它按 222 dp 剪掉。
        for (int i = 0; i < host.getPanelHolder().getChildCount(); ++i) {
            final View pv = host.getPanelHolder().getChildAt(i);
            final android.view.ViewGroup.LayoutParams plp = pv.getLayoutParams();
            if (plp != null) {
                plp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                pv.setLayoutParams(plp);
            }
        }

        // 横屏的舞台矮，顶栏让出 12 dp 给照片。
        if (host.getEditorTopBar() != null && host.getEditorTopBar().getLayoutParams() != null) {
            final android.view.ViewGroup.LayoutParams tl = host.getEditorTopBar().getLayoutParams();
            tl.height = host.dpHostCall(land ? 52 : 64);
            host.getEditorTopBar().setLayoutParams(tl);
        }

        host.applyFilmShelfModeHostCall();
        // 照片栏可能是收起的（`shelfCollapsed`）：重装一遍才知道它该多高。
        host.updateShelfVisibilityHostCall();
        host.moveTabUnderlineHostCall(host.getActiveTabIndex(), false);
        host.refreshSubBarHostCall();
        host.applyEditorChromeHostCall();
        app.requestLayout();
    }

    public static void setPanelCollapsed(EditorChromeHost host, boolean collapsed) {
        // 横屏没有「收起」这回事：右栏是定高的，面板收起来也换不回画面。
        if (host.getLandscapeMode()) {
            host.setPanelCollapsed(false);
            if (host.getPanelHolder() != null) {
                host.getPanelHolder().setVisibility(View.VISIBLE);
                host.getPanelHolder().setAlpha(1f);
                host.getPanelHolder().setTranslationY(0f);
            }
            return;
        }
        // `controls` is what says the column is laid out at all. The holder's
        // own height is 0 on the way *out* of a collapse, and that is exactly
        // the case that still has a fold to show: 0 -> the panel's height.
        final boolean animate = host.getPanelCollapsed() != collapsed
                && host.getControls() != null && host.getControls().getHeight() > 0;
        host.setPanelCollapsed(collapsed);
        // **The strip cannot survive a fold.** `controls` is pinned to the bar's
        // own height while collapsed, so a strip left on screen would push the
        // bar itself out of the column.
        if (collapsed && host.getSubBar() != null) {
            host.cancelHeightHostCall(host.getSubBar());
            host.getSubBar().animate().cancel();
            host.getSubBar().setAlpha(1f);
            host.getSubBar().setVisibility(View.GONE);
            final android.view.ViewGroup.LayoutParams sl = host.getSubBar().getLayoutParams();
            sl.height = -2;
            host.getSubBar().setLayoutParams(sl);
        }
        if (host.getPanelHolder() == null) { host.applyControlsHeightHostCall(collapsed); return; }
        host.cancelHeightHostCall(host.getPanelHolder());
        host.getPanelHolder().animate().cancel();
        if (!animate) {
            host.getPanelHolder().setVisibility(collapsed ? View.GONE : View.VISIBLE);
            host.getPanelHolder().setAlpha(1f);
            host.getPanelHolder().setTranslationY(0f);
            final android.view.ViewGroup.LayoutParams hl = host.getPanelHolder().getLayoutParams();
            hl.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            host.getPanelHolder().setLayoutParams(hl);
            host.applyControlsHeightHostCall(collapsed);
            return;
        }
        // The state this transition is heading for; the completion callback
        // refuses to run if a later tap has already changed its mind.
        final int wanted = collapsed ? 1 : 0;
        if (collapsed) {
            // **Only on the way down.** This used to sit above the `!animate`
            // early return, so it ran on every `setPanelCollapsed(false)` as
            // well -- and `onTabClicked` calls exactly that one line after
            // `setTab(index)`, which cancelled the run `setTab` had just
            // started. That was tr("ui170.265").
            host.cancelLineHostCall(null);
            // The rows sink and fade while the space closes: folding a fully
            // opaque panel would cut its top rows off one by one, which reads
            // as a window being dragged over them rather than a panel leaving.
            host.getPanelHolder().animate().alpha(0f).translationY(host.dpHostCall(24))
                    .setDuration(host.getMS_OUT() + 40)
                    .setInterpolator(host.getEASE_OUT())
                    .start();
            host.animateHeightToHostCall(host.getPanelHolder(), host.getPanelHolder().getHeight(), 0, 0, () -> {
                if ((host.getPanelCollapsed() ? 1 : 0) != wanted) return;
                host.getPanelHolder().setVisibility(View.GONE);
                host.getPanelHolder().setAlpha(1f);
                host.getPanelHolder().setTranslationY(0f);
                final android.view.ViewGroup.LayoutParams hl = host.getPanelHolder().getLayoutParams();
                hl.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                host.getPanelHolder().setLayoutParams(hl);
                host.applyControlsHeightHostCall(true);
            });
        } else {
            // **Only if the fold took it.** `onTabClicked` calls `setTab` and
            // then this, and the tab has already put the right strip up with its
            // own height travel -- re-deciding it here cancelled that travel and
            // snapped 46 dp into the canvas in one frame.
            if (host.getSubBar() != null && host.getSubBar().getVisibility() != View.VISIBLE) host.refreshSubBarHostCall();
            // Hand `controls` back to the layout *before* the holder has any
            // height, so nothing is clipped on the way up.
            host.applyControlsHeightHostCall(false);
            host.getPanelHolder().setVisibility(View.VISIBLE);
            // **The holder itself is not faded in.** Its children each cascade
            // from alpha 0 already; fading the container as well would square
            // the two curves and leave every row almost invisible for the first
            // fifth of a second -- the cascade would be there and unseen.
            host.getPanelHolder().setAlpha(1f);
            host.getPanelHolder().setTranslationY(0f);
            final android.view.ViewGroup.LayoutParams hl = host.getPanelHolder().getLayoutParams();
            hl.height = 0;
            host.getPanelHolder().setLayoutParams(hl);
            final View shown = host.currentPanelHostCall();
            final int w = host.getControls() != null ? host.getControls().getWidth() : 0;
            final int inner = host.getPanelHolder().getWidth()
                    - host.getPanelHolder().getPaddingLeft() - host.getPanelHolder().getPaddingRight();
            final int to = w > 0 ? host.naturalHeightViaHost(host.getPanelHolder(), w) : 0;
            // The components animate themselves again on the way up.
            if (shown != null) host.componentArrivalHostCall(shown);
            if (to <= 0) {
                // Never laid out: no fold to show, just be there.
                hl.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
                host.getPanelHolder().setLayoutParams(hl);
                host.getPanelHolder().setAlpha(1f);
                return;
            }
            host.animateHeightToHostCall(host.getPanelHolder(), 0, to,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT, null);
        }
    }
}
