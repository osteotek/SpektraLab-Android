package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import app.spektralab.NegativeDesk;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.ui.*;

/** LightTable builders extracted from MainActivity (architecture Phase C). */
public final class LightTable {
    private LightTable() {}

    public static View buildLightTable(LightTableHost host) {
        final android.widget.FrameLayout root = new android.widget.FrameLayout(host.context());
        root.setBackgroundColor(host.getLT_WALL());
        root.setClickable(true);

        host.setLightBoxBg(new app.spektralab.ui.LightBox(host.context()));
        root.addView(host.getLightBoxBg(), new android.widget.FrameLayout.LayoutParams(-1, -1));
        host.setLightDesk(new app.spektralab.ui.LightDesk(host.context(), host.lightDeskHost()));
        root.addView(host.getLightDesk(), new android.widget.FrameLayout.LayoutParams(-1, -1));
        host.getLightDesk().zoomChanged = host::refreshZoomBarHostCall;
        host.getLightDesk().litChanged = host::applyLitHudHostCall;

        // **The second view of the same album.** The wall shows the work; this
        // shows the *negative* of one frame, on the same lamp. It is a sibling
        // and not a mode inside `LightDesk` because the two share nothing but
        // the bitmap: one is a wall of seventy-two cells, the other is a single
        // sheet of film with a recurrence of its own for zoom and pan.
        host.setNegDesk(new NegativeDesk(host.context()));
        host.getNegDesk().setFaces(host.getTfHan());
        // **安全区.** 上面让给顶栏（照片墙 / 设置），下面让给照片栏 + 控制台：新放上
        // 去的、整理出来的、双击放大的，都落在这一段里，不压在按钮底下。
        host.getNegDesk().setSafeInsets(host.dpHostCall(host.getNEG_SAFE_TOP_DP()), host.dpHostCall(host.getNEG_SAFE_BOTTOM_DP()));
        host.getNegDesk().setOnLayoutChanged(packed -> host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE)
                .edit().putString("neg_layout", packed).apply());
        host.getNegDesk().setVisibility(View.GONE);
        host.getNegDesk().setOnSwipe(dir -> host.showNegativeAtHostCall((host.getLightIndex() < 0 ? 0 : host.getLightIndex()) + dir));
        // **One lamp for the two rooms.** The console's rocker throws both; the
        // desk only reports how far its own light has got.
        host.getNegDesk().setOnLightToggle(on -> {
            if (host.getLightDesk() != null) host.getLightDesk().setLit(on);
            if (host.getLightRocker() != null) host.getLightRocker().setThrown(on);
        });
        host.getNegDesk().setOnLit(t -> {
            host.setNegLitT(t);
            host.applyNegHudHostCall();
        });
        // 台面上有谁、选中谁：照片栏的琥珀点和琥珀框跟着它
        host.getNegDesk().setOnDeskChanged(host::syncNegStripColorsHostCall);
        // 整理 ⇄ 复原：控制台左边那一块的字
        host.getNegDesk().setOnArrangeState(can -> host.morphConsoleLabelHostCall(0L));
        // 画得比位图大了：去要一张清楚的
        host.getNegDesk().setOnWantSharp(host::requestNegSharpHostCall);
        // **红光里没有照片栏，也没有控制台。** 「拖到底部移出」要一个干净的落点：
        // 照片栏（136–198 dp）和控制台（10–128 dp）正好压在最下面 —— 两样一起让开。
        host.getNegDesk().setOnRemoveMode(on -> {
            if (host.getLightZoomBar() != null) {
                host.getLightZoomBar().animate().cancel();
                host.getLightZoomBar().animate().alpha(on ? 0f : 1f)
                        .translationY(on ? host.dpHostCall(30) : 0f)
                        .setStartDelay(0).setDuration(on ? 150 : 220)
                        .setInterpolator(host.xfEaseViaHost()).start();
            }
            if (host.getNegBar() == null) return;
            host.getNegBar().animate().cancel();
            if (host.getNegReopen() != null) {
                host.getNegReopen().animate().cancel();
                host.getNegReopen().animate().alpha(on ? 0f : 1f).setStartDelay(0).setDuration(140).start();
            }
            if (on) {
                host.getNegBar().animate().alpha(0f).translationY(host.dpHostCall(44)).setStartDelay(0)
                        .setDuration(150).start();
            } else if (host.getNegStripOpen()) {
                host.raiseNegBarHostCall(0L);
            } else {
                host.getNegBar().setAlpha(1f);
                host.getNegBar().setTranslationY(0f);
            }
        });
        // ★ 单张观片台已移除：negDesk 只留实例（接口还在），不进视图树。
        host.getNegDesk().setVisibility(View.GONE);

        // 观片台：原生 Canvas —— 逐层照抄 `assets/lt` 那一版网页（同一个台面、
        // 同一块磨砂导光板、同一张带真镂空齿孔的底片、同一套辉光与暗角）。
        // negDesk 退场：从树里摘掉之后任何 setVisibility 都叫不回来。
        // ★ 单张观片台（Canvas 版 + 网页版）都已移除：ltWeb 不创建、不进树。

        // **The same strip as 冲印.** A plus, the frames, a chevron -- one shape
        // for "the photographs I am working with", so it does not have to be
        // learned twice. Its three states are the lamp's: obsidian in the dark
        // room, paper under the light, and the current frame always ringed in
        // the same amber (the brand is the one thing the lamp does not change).
        host.setNegStripRow(new LinearLayout(host.context()));
        host.getNegStripRow().setOrientation(LinearLayout.HORIZONTAL);
        host.getNegStripRow().setGravity(android.view.Gravity.CENTER_VERTICAL);
        host.setNegStrip(new android.widget.HorizontalScrollView(host.context()));
        host.getNegStrip().setHorizontalScrollBarEnabled(false);
        host.getNegStrip().setOverScrollMode(View.OVER_SCROLL_NEVER);
        host.getNegStrip().addView(host.getNegStripRow());
        host.setNegAdd(host.labelHostCall("＋", 17, host.getNEG_AMBER(), host.getTfHan()));
        host.getNegAdd().setGravity(android.view.Gravity.CENTER);
        host.getNegAdd().setOnClickListener(v -> host.showImportMenuHostCall());
        host.setNegFold(host.labelHostCall("‹", 15, host.getNEG_AMBER(), host.getTfHan()));
        host.getNegFold().setGravity(android.view.Gravity.CENTER);
        host.getNegFold().setOnClickListener(v -> host.setNegStripOpenHostCall(false));
        host.setNegBarBg(host.ltCardHostCall(0xFF121212, 0, 0));
        host.setNegBar(new LinearLayout(host.context()));
        host.getNegBar().setOrientation(LinearLayout.HORIZONTAL);
        host.getNegBar().setGravity(android.view.Gravity.CENTER_VERTICAL);
        host.getNegBar().setPadding(host.dpHostCall(10), 0, host.dpHostCall(4), 0);
        host.getNegBar().setBackground(host.getNegBarBg());
        host.getNegBar().addView(host.getNegAdd(), new LinearLayout.LayoutParams(host.dpHostCall(40), host.dpHostCall(48)));
        host.getNegBar().addView(host.getNegStrip(), new LinearLayout.LayoutParams(0, host.dpHostCall(48), 1f));
        host.getNegBar().addView(host.getNegFold(), new LinearLayout.LayoutParams(host.dpHostCall(28), host.dpHostCall(48)));
        host.getNegBar().setVisibility(View.GONE);
        final android.widget.FrameLayout.LayoutParams negBarLp =
                new android.widget.FrameLayout.LayoutParams(-1, host.dpHostCall(62));
        negBarLp.gravity = android.view.Gravity.BOTTOM;
        // 控制台占 10–128 dp：照片栏落在它上面，中间留 8 dp。
        negBarLp.bottomMargin = host.dpHostCall(136);
        // ★ 单张观片台已移除：照片栏不进树。
        // root.addView(negBar, negBarLp);

        // **收起之后的那一颗钉子.** The bar leaves, but the way back cannot:
        // a screen with no controls and no door is a screen you have to guess
        // your way out of. It sits where the chevron was.
        host.setNegReopenBg(host.ltCardHostCall(0xFF121212, host.getNEG_AMBER(), host.dpHostCall(1)));
        host.setNegReopen(host.labelHostCall("›", 13, host.getNEG_AMBER(), host.getTfHan()));
        host.getNegReopen().setGravity(android.view.Gravity.CENTER);
        host.getNegReopen().setBackground(host.getNegReopenBg());
        host.getNegReopen().setVisibility(View.GONE);
        host.getNegReopen().setOnClickListener(v -> host.setNegStripOpenHostCall(true));
        final android.widget.FrameLayout.LayoutParams nrLp =
                new android.widget.FrameLayout.LayoutParams(host.dpHostCall(46), host.dpHostCall(30),
                        android.view.Gravity.BOTTOM | android.view.Gravity.END);
        nrLp.rightMargin = host.dpHostCall(16);
        nrLp.bottomMargin = host.dpHostCall(140);
        root.addView(host.getNegReopen(), nrLp);

        final TextView settings = host.labelHostCall(tr("editor.settings"), 9, host.getLT_KLEIN(), host.getTfHan());
        settings.setGravity(android.view.Gravity.CENTER);
        settings.setPadding(host.dpHostCall(13), host.dpHostCall(8), host.dpHostCall(13), host.dpHostCall(8));
        settings.setBackground(host.ltCardHostCall(host.getLT_PAPER(), host.getLT_KLEIN(), host.dpHostCall(1.2f)));
        settings.setOnClickListener(v -> host.showSettingsHostCall());

        final LinearLayout topRight = new LinearLayout(host.context());
        host.setLightTopRight(topRight);
        topRight.setOrientation(LinearLayout.HORIZONTAL);
        topRight.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topRight.addView(settings);
        // **照片墙 / 观片台**, left of 设置, same card language as the rest.
        // 单张观片台已移除：右上角不再有「观片台 / 照片墙」那颗双态按钮。
        host.setNegModeBtn(null);

        // **放大镜在控制台上**（与照片墙同一块），右上角不再另放一颗：一个房间
        // 一套按钮，换房间只换它们管的是谁。
        final android.widget.FrameLayout.LayoutParams setLp =
                new android.widget.FrameLayout.LayoutParams(-2, -2,
                        android.view.Gravity.TOP | android.view.Gravity.END);
        setLp.setMargins(0, host.dpHostCall(24), host.dpHostCall(24), 0);
        root.addView(topRight, setLp);

        // ---- the one gesture the sheet has
        host.setLightGridHint(host.labelHostCall(tr("ui170.062"), 7.5f, 0xB3FFFFFF, host.getTfHan()));
        host.getLightGridHint().setLetterSpacing(0.06f);
        host.getLightGridHint().setAlpha(0.85f);
        final android.widget.FrameLayout.LayoutParams hintLp =
                new android.widget.FrameLayout.LayoutParams(-2, -2,
                        android.view.Gravity.TOP | android.view.Gravity.START);
        hintLp.setMargins(host.dpHostCall(24), host.dpHostCall(28), 0, 0);
        root.addView(host.getLightGridHint(), hintLp);

        // ---- nothing exported yet
        final LinearLayout empty = new LinearLayout(host.context());
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(android.view.Gravity.CENTER);
        final TextView e1 = host.labelHostCall(tr("ui170.063"), 10.5f, host.getLT_PAPER(), host.getTfHan());
        host.setLightEmpty1(e1);
        e1.setGravity(android.view.Gravity.CENTER);
        empty.addView(e1);
        final TextView e2 = host.labelHostCall(tr("ui170.064"), 8.5f, 0x99FFFFFF, host.getTfHan());
        host.setLightEmpty2(e2);
        e2.setGravity(android.view.Gravity.CENTER);
        e2.setPadding(0, host.dpHostCall(8), 0, 0);
        empty.addView(e2);
        // **The empty wall needs its own way out.** 去冲印 lives inside
        // `lightZoomBar`, and `applyAlbum` hides that whole console when the
        // album is empty -- so a first launch showed a sentence telling the user
        // to go and develop something, with no way to do it. Same control, same
        // paint (a solid Klein block at 10 sp), so it reads as the one that
        // appears once there is a wall to look at.
        final TextView goEmpty = host.labelHostCall(tr("ui170.065"), 10, host.getLT_PAPER(), host.getTfHan());
        goEmpty.setGravity(android.view.Gravity.CENTER);
        goEmpty.setBackground(host.ltCardHostCall(host.getLT_KLEIN(), 0, 0));
        goEmpty.setOnClickListener(v -> host.crossToEditorHostCall(goEmpty));
        final LinearLayout.LayoutParams goEmptyLp =
                new LinearLayout.LayoutParams(host.dpHostCall(124), host.dpHostCall(36));
        goEmptyLp.topMargin = host.dpHostCall(20);
        goEmptyLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        empty.addView(goEmpty, goEmptyLp);
        empty.setVisibility(View.GONE);
        root.addView(empty, new android.widget.FrameLayout.LayoutParams(-1, -1));
        host.setLightEmptyBlock(empty);

        // ---- what this frame is: EXIF (typed), then the film and paper cards
        host.setLightInfo(new LinearLayout(host.context()));
        host.getLightInfo().setOrientation(LinearLayout.VERTICAL);
        host.getLightInfo().setAlpha(0f);
        host.getLightInfo().setVisibility(View.INVISIBLE);
        // **One white block.** A dark wall needs a place for the small type to
        // be read on, and an outline is not enough when the wall behind can be
        // any photograph: the caption gets a solid sheet of paper.
        host.getLightInfo().setBackground(host.ltCardHostCall(host.getLT_PAPER(), 0, 0));
        host.getLightInfo().setPadding(host.dpHostCall(13), host.dpHostCall(10), host.dpHostCall(15), host.dpHostCall(11));
        host.setLightInfoMeta(host.labelHostCall("", 8.5f, host.getLT_INK(), host.getTfBody()));
        host.getLightInfoMeta().setLetterSpacing(0.03f);
        host.getLightInfo().addView(host.getLightInfoMeta());
        host.setLightInfoFile(host.labelHostCall("", 7.5f, host.getLT_MUTED(), host.getTfHan()));
        host.getLightInfoFile().setPadding(0, host.dpHostCall(5), 0, host.dpHostCall(10));
        host.getLightInfo().addView(host.getLightInfoFile());
        final LinearLayout cards = new LinearLayout(host.context());
        cards.setOrientation(LinearLayout.HORIZONTAL);
        // **Words, plainly.** The stock is a caption, not a shelf: two lines of
        // text read at arm's length, and nothing here competes with the picture
        // for attention.
        host.setLightFilmValue(host.infoCardHostCall(cards, tr("navigation.film"), true));
        host.setLightPaperValue(host.infoCardHostCall(cards, tr("ui170.066"), false));
        host.getLightInfo().addView(cards);
        final android.widget.FrameLayout.LayoutParams infoLp =
                new android.widget.FrameLayout.LayoutParams(-2, -2,
                        android.view.Gravity.START | android.view.Gravity.BOTTOM);
        // **Bottom-aligned with 去冲印.** Same 24 dp the button keeps, so the
        // caption and the action share one baseline; the block is narrower than
        // the gap the button leaves, so they never touch.
        infoLp.setMargins(host.dpHostCall(24), 0, host.dpHostCall(24), Math.round(host.dpHostCall(host.getLT_ARC_H() + 20f)));
        root.addView(host.getLightInfo(), infoLp);

        // ---- the console: two accent blocks, a switch between them
        //
        // **One language on the bottom line.** 去冲印 has always been a solid
        // accent block; the size and the loupe are the same block now, so the
        // three read as one row of actions instead of three inventions. The lamp
        // keeps its metal, because a switch is not an action.
        final android.widget.FrameLayout console = new android.widget.FrameLayout(host.context());

        // ---- 缩放, bottom left: one tap, one step, all the way round
        host.setLightZoomBtn(host.labelHostCall(tr("ui170.054"), 10, host.getLT_PAPER(), host.getTfHan()));
        host.getLightZoomBtn().setGravity(android.view.Gravity.CENTER);
        host.getLightZoomBtn().setBackground(host.ltCardHostCall(host.getLT_KLEIN(), 0, 0));
        host.getLightZoomBtn().setOnClickListener(v -> {
            // **观片台上它是「整理 / 复原」**：排成网格，再按一次每张飞回去。
            if (host.getNegMode()) {
                if (host.getNegDesk() != null) host.getNegDesk().toggleArrange();
                return;
            }
            if (host.getLightDesk() == null) return;
            host.getLightDesk().zoomTo((Math.max(0, host.getLightDesk().zoomStep()) + 1) % host.getLT_ZOOM_NAMES().length);
        });
        final android.widget.FrameLayout.LayoutParams zLp =
                new android.widget.FrameLayout.LayoutParams(Math.round(host.dpHostCall(68f)),
                        Math.round(host.dpHostCall(36f)),
                        android.view.Gravity.BOTTOM | android.view.Gravity.START);
        zLp.leftMargin = host.dpHostCall(14f);
        console.addView(host.getLightZoomBtn(), zLp);

        // ---- 放大镜, bottom right: the same block, inverted while it is on
        host.setLightLoupe(host.labelHostCall(tr("ui170.067"), 10, host.getLT_PAPER(), host.getTfHan()));
        host.getLightLoupe().setGravity(android.view.Gravity.CENTER);
        host.getLightLoupe().setBackground(host.ltCardHostCall(host.getLT_KLEIN(), 0, 0));
        host.getLightLoupe().setOnClickListener(v -> {
            if (host.getNegMode()) {
                if (host.getNegDesk() != null) host.getNegDesk().setLens(!host.getNegDesk().isLensOn());
            } else {
                if (host.getLightDesk() == null) return;
                host.getLightDesk().setTool(!host.getLightDesk().isToolOn());
            }
            host.refreshLoupeButtonHostCall();
        });
        final android.widget.FrameLayout.LayoutParams lLp =
                new android.widget.FrameLayout.LayoutParams(Math.round(host.dpHostCall(68f)),
                        Math.round(host.dpHostCall(36f)),
                        android.view.Gravity.BOTTOM | android.view.Gravity.END);
        lLp.rightMargin = host.dpHostCall(14f);
        console.addView(host.getLightLoupe(), lLp);
        host.getLightLoupe().setVisibility(android.view.View.GONE);

        host.setLightRocker(new app.spektralab.ui.RockerSwitch(host.context()));
        final android.widget.FrameLayout.LayoutParams rkLp =
                new android.widget.FrameLayout.LayoutParams(Math.round(host.dpHostCall(48f)),
                        Math.round(host.dpHostCall(72f)),
                        android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        rkLp.bottomMargin = host.dpHostCall(46f);
        console.addView(host.getLightRocker(), rkLp);
        host.getLightRocker().setOnClickListener(v -> host.toggleLightsHostCall(host.getLightRocker()));

        final TextView go = host.labelHostCall(tr("ui170.065"), 10, host.getLT_PAPER(), host.getTfHan());
        host.setLightGo(go);
        go.setGravity(android.view.Gravity.CENTER);
        go.setBackground(host.ltCardHostCall(host.getLT_KLEIN(), 0, 0));
        // **The crossing, not a cut.** 去冲印 hands the stage to the editor and
        // lets the editor build itself; the picker is the button in the middle
        // of the new page, so nothing is thrown in front of the arrival.
        go.setOnClickListener(v -> host.crossToEditorHostCall(go));
        final android.widget.FrameLayout.LayoutParams goLp =
                new android.widget.FrameLayout.LayoutParams(host.dpHostCall(104), host.dpHostCall(36),
                        android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        console.addView(go, goLp);

        final android.widget.FrameLayout.LayoutParams barLp =
                new android.widget.FrameLayout.LayoutParams(-1, Math.round(host.dpHostCall(host.getLT_ARC_H())),
                        android.view.Gravity.BOTTOM);
        barLp.setMargins(0, 0, 0, host.dpHostCall(10));
        root.addView(console, barLp);
        host.setLightZoomBar(console);
        // **Catch up with the desk.** It outlives this view, so the light can
        // already be on and the wall already on a step when this is built.
        host.getLightRocker().setThrown(host.getLightDesk().isLit(), false);
        host.refreshZoomBarHostCall();

        return root;
    }
}
