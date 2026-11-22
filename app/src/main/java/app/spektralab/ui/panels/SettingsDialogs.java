package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import android.view.View;
import android.widget.*;
import app.spektralab.DebugSink;
import app.spektralab.Diagnostics;
import app.spektralab.UpdateCheck;
import app.spektralab.AppLanguage;
import app.spektralab.DeviceReport;
import app.spektralab.SelfHeal;
import app.spektralab.engine.FotuFilm;

/** SettingsDialogs builders extracted from MainActivity (architecture Phase C). */
public final class SettingsDialogs {
    private SettingsDialogs() {}

    public static void showSettings(SettingsDialogsHost host, String section) {
        if (host.getUiRoot() == null) return;
        host.setSettingsSection(section);
        if (host.getSettingsScreen() == null) {
            host.setSettingsScreen(buildSettingsScreen(host));
            host.getUiRoot().addView(host.getSettingsScreen(), new android.widget.FrameLayout.LayoutParams(-1, -1));
        }
        if (!host.getSettingsOpen()) {
            host.setSettingsOpen(true);
            host.getSettingsScreen().setVisibility(View.VISIBLE);
            host.getSettingsScreen().setAlpha(0f);
            host.getSettingsScreen().animate().alpha(1f).setDuration(200)
                    .setInterpolator(host.getEASE_OUT()).start();
            host.syncRoomBarsHostCall();
        }
        fillSettings(host);
    }

    public static void closeSettings(SettingsDialogsHost host) {
        if (!host.getSettingsOpen()) return;
        host.setSettingsOpen(false);
        host.setSettingsSection(null);
        host.getSettingsScreen().animate().cancel();
        host.getSettingsScreen().animate().alpha(0f).setDuration(140)
                .withEndAction(() -> {
                    // **淡出途中又被打开时，这一下不能把它藏掉。** `showSettings`
                    // 会把同一个 view 的 alpha 反向拉回 1，但旧动画的末段回调仍
                    // 然排在那里 —— 少了这道判断，连按「完成」再按 ☰ 就是一页空白。
                    if (host.getSettingsScreen() != null && !host.getSettingsOpen())
                        host.getSettingsScreen().setVisibility(View.GONE);
                    // 系统栏等纸退干净再交还给房间，否则顶上那一条会比页面早 140 ms
                    // 变色。
                    host.syncRoomBarsHostCall();
                })
                .start();
    }

    public static View buildSettingsScreen(SettingsDialogsHost host) {
        final android.widget.FrameLayout page = new android.widget.FrameLayout(host.context());
        page.setBackgroundColor(host.getUI_PAPER());
        page.setClickable(true);          // 挡住下面那一层的手势

        final LinearLayout column = new LinearLayout(host.context());
        column.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout head = new LinearLayout(host.context());
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(host.dpHostCall(6), 0, host.dpHostCall(10), 0);

        final TextView back = host.labelHostCall("\u2039", 20, host.getUI_INK(), host.getTfBody());
        back.setGravity(android.view.Gravity.CENTER);
        back.setBackground(host.settingsPressHostCall());
        back.setContentDescription(tr("ui170.345"));
        back.setOnClickListener(v -> { if (host.getSettingsSection() == null) closeSettings(host); else showSettings(host, null); });
        head.addView(back, new LinearLayout.LayoutParams(host.dpHostCall(44), host.dpHostCall(48)));

        host.setSettingsTitle(host.mixedLabelHostCall(tr("editor.settings"), 13f, host.getUI_INK()));
        head.addView(host.getSettingsTitle(), new LinearLayout.LayoutParams(0, -2, 1f));

        host.setSettingsDone(host.labelHostCall(tr("ui170.146"), 9.5f, host.getUI_BLUE(), host.getTfHan()));
        host.getSettingsDone().setGravity(android.view.Gravity.CENTER);
        host.getSettingsDone().setBackground(host.settingsPressHostCall());
        host.getSettingsDone().setPadding(host.dpHostCall(12), 0, host.dpHostCall(12), host.dpHostCall(12));
        host.getSettingsDone().setOnClickListener(v -> closeSettings(host));
        head.addView(host.getSettingsDone(), new LinearLayout.LayoutParams(-2, host.dpHostCall(48)));
        column.addView(head, new LinearLayout.LayoutParams(-1, host.dpHostCall(52)));

        final View hair = new View(host.context());
        hair.setBackgroundColor(host.getUI_LINE());
        column.addView(hair, new LinearLayout.LayoutParams(-1, Math.max(1, Math.round(host.dpHostCall(0.6f)))));

        host.setSettingsScroll(new ScrollView(host.context()));
        host.getSettingsScroll().setVerticalScrollBarEnabled(false);
        host.getSettingsScroll().setFillViewport(true);
        host.setSettingsBody(new LinearLayout(host.context()));
        host.getSettingsBody().setOrientation(LinearLayout.VERTICAL);
        host.getSettingsBody().setPadding(0, 0, 0, host.dpHostCall(26));
        host.getSettingsScroll().addView(host.getSettingsBody(), new android.widget.FrameLayout.LayoutParams(-1, -2));
        column.addView(host.getSettingsScroll(), new LinearLayout.LayoutParams(-1, 0, 1f));

        page.addView(column, new android.widget.FrameLayout.LayoutParams(-1, -1));
        return page;
    }

    public static void fillSettings(SettingsDialogsHost host) {
        if (host.getSettingsBody() == null) return;
        final boolean root = host.getSettingsSection() == null;
        final boolean moved = !String.valueOf(host.getSettingsSection()).equals(host.getSettingsFilledSection());
        final int keepY = host.getSettingsScroll() == null ? 0 : host.getSettingsScroll().getScrollY();
        host.setSettingsFilledSection(String.valueOf(host.getSettingsSection()));
        host.getSettingsTitle().setText(host.mixedTextHostCall(root ? tr("editor.settings")
                : "theme".equals(host.getSettingsSection()) ? tr("ui170.346")
                : "about".equals(host.getSettingsSection()) ? tr("ui170.347")
                : "licence".equals(host.getSettingsSection()) ? tr("ui170.348") : tr("editor.settings")));
        host.getSettingsDone().setVisibility(root ? View.VISIBLE : View.GONE);
        host.getSettingsBody().removeAllViews();

        if (root) fillSettingsRoot(host);
        else if ("theme".equals(host.getSettingsSection())) fillSettingsTheme(host);
        else if ("about".equals(host.getSettingsSection())) fillSettingsAbout(host);
        else fillSettingsLicence(host);

        final TextView foot = host.mixedLabelHostCall("SpektraLab Android \u00b7 "
                + UpdateCheck.versionName(host.context()) + " (" + UpdateCheck.versionCode(host.context()) + ")", 7.5f, host.getUI_MUTED());
        foot.setGravity(android.view.Gravity.CENTER);
        foot.setPadding(0, host.dpHostCall(28), 0, host.dpHostCall(4));
        host.getSettingsBody().addView(foot);

        // **换页回到顶部，改一行不动。** 在「驱动报告」上按一下就把页面弹回最上
        // 面，是那种用户会以为是 bug 的小动作。
        host.getSettingsScroll().scrollTo(0, moved ? 0 : keepY);
        host.getSettingsBody().animate().cancel();
        if (moved) {
            host.getSettingsBody().setAlpha(0.35f);
            host.getSettingsBody().setTranslationY(host.dpHostCall(8));
            host.getSettingsBody().animate().alpha(1f).translationY(0f)
                    .setDuration(180).setInterpolator(host.getEASE_OUT()).start();
        } else {
            host.getSettingsBody().setAlpha(1f);
            host.getSettingsBody().setTranslationY(0f);
        }
    }

    public static void fillSettingsRoot(SettingsDialogsHost host) {
        settingsGroup(host, "外观 \u00b7 APPEARANCE");
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.350"), null, host.themeValueHostCall(), true,
                () -> showSettings(host, "theme")));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.349"), null,
                AppLanguage.current().name, true, () -> showLanguageSheet(host)));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.351"), tr("ui170.352"), host.orientationValueHostCall(), true,
                () -> showOrientationSheet(host)));
        settingsRule(host);
        settingsAdd(host, host.settingsSwitchHostCall(tr("ui170.353"), tr("ui170.354"), host.arrivalAnimHostCall(),
                host::setArrivalAnimHostCall));

        settingsGroup(host, "预览 \u00b7 PREVIEW");
        settingsAdd(host, host.settingsSwitchHostCall(tr("optics.histogram"), tr("ui170.355"), host.getHistogramOn(), on -> {
            host.setHistogramOn(on);
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit()
                    .putBoolean("histogram", on).apply();
            if (host.getHistogram() != null) host.getHistogram().setVisibility(on ? View.VISIBLE : View.GONE);
        }));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.356"), host.hdrDetailLineHostCall(), null, true,
                host::showHdrSheetHostCall));
        settingsRule(host);
        settingsAdd(host, host.settingsSwitchHostCall(tr("optics.original_brightness"), tr("ui170.357"),
                host.getRawAutoBright(), on -> {
                    host.setRawAutoBright(on);
                    host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit()
                            .putBoolean("raw_auto_bright", on).apply();
                    host.refreshOriginalViewHostCall();
                }));

        settingsGroup(host, "工作区 \u00b7 WORKSPACE");
        settingsAdd(host, host.settingsRowHostCall(tr("preset.preset"), host.presetCountLineHostCall(), null, true, host::showPresetsPanelHostCall));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.358"), tr("ui170.359"), null, true, () -> {
            host.loadAlbumHostCall();
            host.showInAppToastHostCall(tr("ui170.360"));
        }));

        settingsGroup(host, "关于 \u00b7 ABOUT");
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.361"), null,
                UpdateCheck.versionName(host.context()) + "\uff08" + UpdateCheck.versionCode(host.context()) + "\uff09",
                true, () -> showSettings(host, "about")));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("settings.check_for_updates"), null, null, true,
                () -> host.checkForUpdatesHostCall(true)));
        settingsRule(host);
        settingsAdd(host, host.settingsSwitchHostCall(tr("ui170.362"), host.deviceReportLineHostCall(), DeviceReport.enabled(host.context()),
                on -> DeviceReport.setEnabled(host.context(), on)));
        settingsRule(host);
        // **上传步骤与仓库都归「关于与诊断」。** 一个是出问题才用，一个看一次就
        // 够；根页只留每天会碰的那几行，这页才不会变成第二条动作单。
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.363"), tr("ui170.364"), null, true,
                () -> host.showFeedbackGroupHostCall(false)));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.348"), tr("ui170.365"), null, true,
                () -> showSettings(host, "licence")));
    }

    public static void fillSettingsTheme(SettingsDialogsHost host) {
        final boolean dark = host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).getBoolean("dark", false);
        final boolean pending = dark != host.getDarkTheme();
        final TextView note = host.mixedLabelHostCall(pending
                ? tr("ui170.366")
                : tr("ui170.367"),
                8.5f, host.getUI_MUTED());
        note.setPadding(host.dpHostCall(16), host.dpHostCall(16), host.dpHostCall(16), host.dpHostCall(2));
        note.setLineSpacing(host.dpHostCall(2), 1f);
        host.getSettingsBody().addView(note);

        settingsGroup(host, "配色 \u00b7 PALETTE");
        settingsAdd(host, host.settingsCheckRowHostCall(tr("ui170.368"), "纸白面板 \u00b7 克莱因蓝", !dark, () -> {
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit().putBoolean("dark", false).apply();
            fillSettings(host);
        }));
        settingsRule(host);
        settingsAdd(host, host.settingsCheckRowHostCall("暗黑 \u00b7 黑橙", "近黑面板 \u00b7 橙色强调", dark, () -> {
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit().putBoolean("dark", true).apply();
            fillSettings(host);
        }));

        if (pending) {
            settingsGroup(host, "生效 \u00b7 APPLY");
            settingsAdd(host, host.settingsRowHostCall(tr("ui170.369"), tr("ui170.370"), null, true,
                    host::relaunchForThemeHostCall));
        }
    }

    public static void fillSettingsAbout(SettingsDialogsHost host) {
        host.probeNowHostCall();
        settingsGroup(host, "本机 \u00b7 DEVICE");
        host.settingsInfoHostCall("GPU", host.getProbe().describe());
        host.settingsInfoHostCall(tr("ui170.371"), String.format(java.util.Locale.US, "%.1f MP \uff08内存上限\uff09",
                host.maxImportPixelsHostCall(false) / 1e6));
        host.settingsInfoHostCall(tr("ui170.372"), host.buildLineHostCall());
        // **Two engines, two lines.** The shelf merges both catalogues, so a page
        // that named only the Vulkan one read as if the other did not exist.
        host.settingsInfoHostCall(tr("ui170.373"), FotuFilm.buildInfo(host.context()));
        // **Only when something was repaired.** A permanent tr("ui170.374") row is
        // noise; a row that appears is a signal, and it is the first thing to
        // ask for when a report says the picture came out strange.
        final String healed = SelfHeal.summary();
        if (!"nothing repaired".equals(healed)) {
            host.settingsInfoHostCall(tr("ui170.375"), healed);
        }

        settingsGroup(host, "维护 \u00b7 SUPPORT");
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.376"), host.debugSummaryLineHostCall(), null, true,
                host::showPixelCapSheetHostCall));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.377"), DebugSink.REF + " \u00b7 点一下复制", null, true, () -> {
            host.copyToClipboardHostCall(tr("ui170.378"), DebugSink.REF);
            host.showInAppToastHostCall(tr("ui170.379") + DebugSink.REF);
        }));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.380"), tr("ui170.381"), null, true,
                host::uploadRecentStepsHostCall));
    }

    public static void fillSettingsLicence(SettingsDialogsHost host) {
        host.getSettingsBody().addView(host.licenceBlockHostCall(tr("ui170.382"),
                "spektrafilm \u2014 Andrea Volpato。胶片过程模型、实测彩色 profile 与放大 LUT，"
                + tr("ui170.383")));
        host.getSettingsBody().addView(host.licenceBlockHostCall(tr("ui170.384"),
                "SpektraLab \u2014 Hanze Qiu。把上面的引擎重写成 C++ / Metal 并做出 macOS 应用，"
                + tr("ui170.385")));
        host.getSettingsBody().addView(host.licenceBlockHostCall("第二引擎 \u2014 Fotufilm",
                "Fotufilm \u2014 MUAStudio Inc.（fotufilm.com）。引擎代码以 Apache-2.0 分发；"
                + tr("ui170.386")
                + tr("ui170.387")));
        host.getSettingsBody().addView(host.licenceBlockHostCall(tr("ui170.388"),
                tr("ui170.389")
                + tr("ui170.390")));
        host.getSettingsBody().addView(host.licenceBlockHostCall(tr("ui170.391"),
                tr("ui170.392")));
        settingsGroup(host, "全文 \u00b7 LICENCE");
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.393"), "GPL-3.0-or-later、CC BY-SA 4.0、Apache-2.0",
                null, true, host::openRepoHostCall));
        settingsRule(host);
        settingsAdd(host, host.settingsRowHostCall(tr("ui170.394"), tr("ui170.395"),
                null, true, () -> host.openUrlHostCall(host.getREPO() + "/blob/master/AI-NOTICE.md")));
    }

    public static void showLanguageSheet(SettingsDialogsHost host) {
        final java.util.List<AppLanguage.Language> languages = AppLanguage.available();
        final java.util.List<String> labels = new java.util.ArrayList<>();
        for (AppLanguage.Language language : languages) labels.add(language.name);
        host.showChoiceSheetHostCall(tr("ui170.349"), labels, AppLanguage.current().name, picked -> {
            for (AppLanguage.Language language : languages) {
                if (language.name.equals(picked)) {
                    AppLanguage.select(host.context(), language);
                    host.recreateHostCall();
                    return;
                }
            }
        });
    }

    public static void showOrientationSheet(SettingsDialogsHost host) {
        final String cur = host.orientationPrefHostCall();
        int active = 0;
        for (int i = 0; i < host.getORIENT_IDS().length; ++i) if (host.getORIENT_IDS()[i].equals(cur)) active = i;
        final String note = "横屏：照片在左，参数在右一栏，页签在栏顶。\n"
                + tr("ui170.399");
        final String[] names = new String[host.getORIENT_NAMES().length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = i == active ? host.getORIENT_NAMES()[i] + "  \u00b7 现在" : host.getORIENT_NAMES()[i];
        }
        host.showActionSheetHostCall(tr("ui170.351"), note, names, which -> {
            if (which < 0 || which >= host.getORIENT_IDS().length) return;
            host.context().getSharedPreferences("ui", android.content.Context.MODE_PRIVATE).edit()
                    .putString("orientation", host.getORIENT_IDS()[which]).apply();
            host.applyOrientationPrefHostCall();
            if (host.getSettingsOpen()) fillSettings(host);
        });
    }

    public static void showAbout(SettingsDialogsHost host) {

    }

    public static void settingsAdd(SettingsDialogsHost host, View row) {
        host.getSettingsBody().addView(row, new LinearLayout.LayoutParams(-1, -2));
    }

    public static void settingsRule(SettingsDialogsHost host) {
        final View v = new View(host.context());
        v.setBackgroundColor(host.getUI_LINE());
        final LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(-1, Math.max(1, Math.round(host.dpHostCall(0.6f))));
        lp.leftMargin = host.dpHostCall(16);
        host.getSettingsBody().addView(v, lp);
    }

    public static void settingsGroup(SettingsDialogsHost host, String title) {
        final TextView t = host.mixedLabelHostCall(title, 8.5f, host.getUI_MUTED());
        t.setLetterSpacing(0.10f);
        t.setPadding(host.dpHostCall(16), host.dpHostCall(26), host.dpHostCall(16), host.dpHostCall(7));
        host.getSettingsBody().addView(t, new LinearLayout.LayoutParams(-1, -2));
    }
}
