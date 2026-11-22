package app.spektralab;

import app.spektralab.MainActivity.Look;
import org.json.JSONArray;
import org.json.JSONObject;

/** Pure JSON mapping for {@link Look}, extracted from MainActivity.
 *
 * <p>This is the full session representation (presets, sharing, export
 * metadata); {@link LookCodec} keeps the compact render-only schema. Neither
 * class touches Android views or Activity state.
 */
final class LookJson {
    private LookJson() {}

    private static float[] readBands(org.json.JSONArray a) {
        final float[] v = new float[8];
        if (a == null) return v;
        for (int i = 0; i < 8; ++i) v[i] = (float) a.optDouble(i, 0.0);
        return v;
    }

    private static org.json.JSONArray arrOf(float[] v) {
        final org.json.JSONArray a = new org.json.JSONArray();
        try {
            for (int i = 0; i < 8; ++i) a.put(v == null || i >= v.length ? 0.0 : (double) v[i]);
        } catch (org.json.JSONException ignored) { }
        return a;
    }

    private static void putNum(org.json.JSONObject o, String key, double v) {
        final boolean bad = Double.isNaN(v) || Double.isInfinite(v);
        try {
            o.put(key, bad ? 0.0 : v);
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:24245", ignored); }
        if (bad) {
            try { DebugSink.text("look-bad", key + "=" + v + " -> 0"); } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:24247", ignored); }
        }
    }

    public static org.json.JSONObject toJson(Look l) {
        final org.json.JSONObject o = new org.json.JSONObject();
        try {
            o.put("film", l.filmStock == null ? "" : l.filmStock);
            o.put("print", l.printStock == null ? "" : l.printStock);
            putNum(o, "exp", l.printExposure);
            o.put("mtr", l.meterMethod == null ? "" : l.meterMethod);
            putNum(o, "grain", l.grainAmount);
            putNum(o, "halation", l.halationAmount);
            putNum(o, "glare", l.glareAmount);
            putNum(o, "highlight", l.highlightRoom);
            o.put("hmig", 1);            // see the one-time lift in `lookFromJson`
            putNum(o, "shadow", l.shadowRoom);
            o.put("hlm", l.hlManual ? 1 : 0);
            putNum(o, "hlk", l.hlKnee);
            putNum(o, "hlro", l.hlRolloff);
            putNum(o, "hlr", l.hlRoom);
            o.put("lat", l.printLatitude);
            putNum(o, "boost", l.halationBoost);
            putNum(o, "scatter", l.halationScatter);
            putNum(o, "scatterr", l.scatterRadius);
            putNum(o, "lens", l.lensBlur);
            putNum(o, "vT", l.viewTemperature);
            putNum(o, "vTn", l.viewTint);
            putNum(o, "vE", l.viewExposure);
            putNum(o, "vC", l.viewContrast);
            putNum(o, "vB", l.viewBrightness);
            putNum(o, "vS", l.viewSaturation);
            putNum(o, "vHi", l.viewHighlights);
            putNum(o, "vSh", l.viewShadows);
            putNum(o, "vBk", l.viewBlack);
            putNum(o, "vWh", l.viewWhite);
            putNum(o, "vV", l.viewVignette);
            putNum(o, "cEv", l.cameraEv);
            putNum(o, "vVb", l.viewVibrance);
            // HSL and 细节 are new; absent means zero, which is what a look
            // saved by 1.6.5 wanted.
            o.put("hslH", arrOf(l.hslHue));
            o.put("hslS", arrOf(l.hslSat));
            o.put("hslL", arrOf(l.hslLum));
            putNum(o, "shp", l.viewSharpen);
            putNum(o, "shpR", l.viewSharpenRadius);
            putNum(o, "shpM", l.viewSharpenMask);
            putNum(o, "tex", l.viewTexture);
            o.put("wb", l.wbPreset);
            putNum(o, "wbK", l.wbKelvin);
            putNum(o, "wbT", l.wbTint);
            if (l.wbMul != null && l.wbMul.length == 4) {
                final org.json.JSONArray wbArr = new org.json.JSONArray();
                for (float v : l.wbMul) wbArr.put((double) v);
                o.put("wbm", wbArr);
            }
            o.put("gturn", l.geometryTurns);
            putNum(o, "grot", l.geometryRotation);
            putNum(o, "gcw", l.geometryCropW);
            putNum(o, "gch", l.geometryCropH);
            putNum(o, "gcx", l.geometryCropX);
            putNum(o, "gcy", l.geometryCropY);
            o.put("gcri", l.cropRatioIndex);
            o.put("gfh", l.geometryFlipH);
            o.put("gfv", l.geometryFlipV);
            putNum(o, "yf", l.yFilter);
            putNum(o, "mf", l.mFilter);
            putNum(o, "fscale", l.filterScale);
            putNum(o, "preflash", l.preflash);
            putNum(o, "fog", l.edgeFog);
            putNum(o, "leaks", l.edgeLeaks);
            putNum(o, "frameno", l.frameNumber);
            o.put("pfk", l.pfKind == null ? "none" : l.pfKind);
            o.put("pfc", l.pfChosen ? 1 : 0);
            putNum(o, "pfm", l.pfMargin);
            putNum(o, "pffx", l.pfFx);
            putNum(o, "pffy", l.pfFy);
            putNum(o, "halon", l.halationOn);
            putNum(o, "grainon", l.grainOn);
            putNum(o, "coupon", l.couplersOn);
            putNum(o, "ahrem", l.antihalationRemoved);
            putNum(o, "scw", l.scannerWhite);
            putNum(o, "scb", l.scannerBlack);
            putNum(o, "edr", l.edrOn);
            putNum(o, "gla", l.glareActive);
            putNum(o, "slb", l.scannerLensBlur);
            putNum(o, "hhe", l.halationHeadroom);
            putNum(o, "dcg", l.densityGamma);
            o.put("dint", l.digitalIntermediate);
            o.put("dibc", l.diBlueComp);
            o.put("occtf", l.outputCctf);
            o.put("hcor", l.hueCorrection);
            putNum(o, "rolloff", l.shadowRolloff);
            for (int i = 0; i < 4; ++i) {
                o.put("cbh" + i, (double) l.cbHue[i]);
                o.put("cbs" + i, (double) l.cbSat[i]);
                o.put("cbl" + i, (double) l.cbLum[i]);
            }
            for (int i = 0; i < 5; ++i) {
                final float[] flat = l.curves[i];
                if (flat == null) continue;
                final org.json.JSONArray arr = new org.json.JSONArray();
                for (int k = 0; k < flat.length; ++k) arr.put((double) flat[k]);
                o.put("cv" + i, arr);
            }
            putNum(o, "lift", l.maxLift);
            o.put("cfilter", l.cameraFilter == null ? "" : l.cameraFilter);
            o.put("snorm", l.sceneNorm == null ? "power" : l.sceneNorm);
            o.put("emode", l.edgeMode == null ? "strip" : l.edgeMode);
            o.put("egate", l.edgeGate == null ? "auto" : l.edgeGate);
            o.put("eholes", l.edgeHoles == null ? "white" : l.edgeHoles);
            o.put("ecarrier", l.edgeCarrier == null ? "none" : l.edgeCarrier);
            o.put("dplace", l.datePlacement == null ? "frame" : l.datePlacement);
            o.put("dcorn", l.dateCorner == null ? "br" : l.dateCorner);
            o.put("dstyle", l.dateStyle == null ? "lcd" : l.dateStyle);
            putNum(o, "dinsx", l.dateInsetX);
            putNum(o, "dinsy", l.dateInsetY);
            o.put("etext", l.edgeTextOn);
            putNum(o, "efnum", l.edgeFNumber);
            putNum(o, "dsize", l.dateSize);
            putNum(o, "dev", l.dateEv);
            o.put("dtext", l.dataText == null ? "" : l.dataText);
            o.put("dtextb", l.dateTextB == null ? "" : l.dateTextB);
            putNum(o, "cseed", l.cameraSeed);
            putNum(o, "fseed", l.frameSeed);
            putNum(o, "mm", l.filmFormatMm);
            o.put("overscan", l.overscanOn);
            o.put("format", l.overscanFormat == null ? "135" : l.overscanFormat);
            o.put("pair", l.overscanPair);
            putNum(o, "psplit", l.pairSplit);
            o.put("pton", l.pairTone);
            putNum(o, "pkh", l.pairHighKnee);
            putNum(o, "phr", l.pairHighRoom);
            putNum(o, "psk", l.pairShadowKnee);
            putNum(o, "psr", l.pairShadowRoom);
            o.put("lmig", 1);      // see the unit change in `lookFromJson`
            o.put("date", l.dateOn);
            // **fotufilm's own keys, all prefixed `ft`.** `mm` was taken by
            // `filmFormatMm` and `mm` for the meter method once collided with
            // it (SL-261106-8B0D, `auto_exposure_method = "24"`); a prefix that
            // belongs to neither engine keeps these out of that argument.
            putNum(o, "ftev", l.fotuEv);
            putNum(o, "fthi", l.fotuHighlights);
            putNum(o, "ftsh", l.fotuShadows);
            putNum(o, "ftsa", l.fotuSaturation);
            putNum(o, "ftvi", l.fotuVibrance);
            putNum(o, "ftwa", l.fotuWarmth);
            putNum(o, "ftti", l.fotuTint);
            putNum(o, "ftfl", l.fotuFlare);
            putNum(o, "ftli", l.fotuGradeLift);
            putNum(o, "ftga", l.fotuGradeGain);
            putNum(o, "ftxp", l.fotuExpired);
            putNum(o, "ftbl", l.fotuBleach);
            putNum(o, "ftpu", l.fotuPush);
            putNum(o, "ftgr", l.fotuGrain);
            // Stamped once: a look written before this had no way to say "these
            // multipliers were never set", and their zeros are not choices.
            o.put("fmi2", 1);
            o.put("ftfg", l.fotuFilmGrain ? 1 : 0);
            putNum(o, "ftgs", l.fotuGrainSize);
            putNum(o, "ftgc", l.fotuGrainColour);
            putNum(o, "ftgso", l.fotuGrainSoft);
            putNum(o, "ftsd", l.fotuShadowDetail);
            putNum(o, "fthd", l.fotuHighlightDetail);
            putNum(o, "ftpc", l.fotuPrintCorrection);
            putNum(o, "ftlr", l.fotuGrainR);
            putNum(o, "ftlg", l.fotuGrainG);
            putNum(o, "ftlb", l.fotuGrainB);
            putNum(o, "ftpf", l.fotuPreflash);
            putNum(o, "ftfr", l.fotuFringe);
            putNum(o, "ftha", l.fotuHalation);
            putNum(o, "fthc", l.fotuHalationColour);
            putNum(o, "ftcp", l.fotuCouplers);
            putNum(o, "ftcr", l.fotuCouplerReach);
            putNum(o, "ftcrg", l.fotuCouplerRG);
            putNum(o, "ftcgb", l.fotuCouplerGB);
            putNum(o, "ftcs", l.fotuCouplerSelf);
            putNum(o, "ftmt", l.fotuMottle);
            putNum(o, "fteg", l.fotuEncodedGrade);
            putNum(o, "ftcn", l.fotuCondenser);
            o.put("ftpl", l.fotuPrintLight == null ? "" : l.fotuPrintLight);
            putNum(o, "ftpo", l.fotuPrinter);
            putNum(o, "ftpk", l.fotuPrinterLamp);
            putNum(o, "ftpm", l.fotuPrinterM);
            putNum(o, "ftpy", l.fotuPrinterY);
            putNum(o, "ftpx", l.fotuPrinterEv);
            putNum(o, "ftpr", l.fotuPrinterR);
            putNum(o, "ftpg", l.fotuPrinterG);
            putNum(o, "ftpb", l.fotuPrinterB);
            putNum(o, "ftlt", l.fotuLocalTone);
            o.put("ftdr", l.fotuDigitalRef == null ? "" : l.fotuDigitalRef);
            putNum(o, "ftfrr", l.fotuFringeRadius);
            putNum(o, "ftbp", l.fotuBlackPoint);
            putNum(o, "ftwp", l.fotuWhitePoint);
            putNum(o, "ftppf", l.fotuPrinterPreflash);
        } catch (Throwable ignored) { Diagnostics.ignored("MainActivity:2239", ignored); }
        return o;
    }

    public static Look fromJson(org.json.JSONObject o) {
        final Look l = new Look();
        if (o == null) return l;
        final String film = o.optString("film", "");
        l.filmStock = film.isEmpty() ? null : film;
        final String paper = o.optString("print", "");
        l.printStock = paper.isEmpty() ? "kodak_portra_endura" : paper;
        l.printExposure = (float) o.optDouble("exp", 1.0);
        // `mm` is the name; an older state has only the boolean, and `true`
        // there meant exactly this default.
        // **Not `mm`** -- that key already belonged to `filmFormatMm`, and the
        // two writes landed in the same object: the format (24.0) overwrote the
        // meter name, and the next render sent `auto_exposure_method: "24"`,
        // which the engine refuses. `mtr` is this field's own key.
        l.meterMethod = LookCodec.sanitizeMeter(o.has("mtr") ? o.optString("mtr", "protect_highlights")
                : (o.optBoolean("meter", true) ? "protect_highlights" : ""));
        l.grainAmount = (float) o.optDouble("grain", 1.0);
        l.halationAmount = (float) o.optDouble("halation", 1.0);
        l.glareAmount = (float) o.optDouble("glare", 1.0);
        l.highlightRoom = (float) o.optDouble("highlight", 0.0);
        l.shadowRoom = (float) o.optDouble("shadow", 0.0);
        l.hlManual = o.optInt("hlm", 0) != 0;
        l.hlKnee = (float) o.optDouble("hlk", 2.0);
        l.hlRolloff = (float) o.optDouble("hlro", 2.0);
        l.hlRoom = (float) o.optDouble("hlr", 2.4);
        l.printLatitude = Math.max(0, Math.min(4, o.optInt("lat", 0)));
        l.halationBoost = (float) o.optDouble("boost", 0.0);
        l.halationScatter = (float) o.optDouble("scatter", 1.0);
        l.scatterRadius = (float) o.optDouble("scatterr", 0.0);
        l.lensBlur = (float) o.optDouble("lens", 0.0);
        l.viewTemperature = (float) o.optDouble("vT", 0.0);
        l.viewTint = (float) o.optDouble("vTn", 0.0);
        l.viewExposure = (float) o.optDouble("vE", 0.0);
        l.viewContrast = (float) o.optDouble("vC", 0.0);
        l.viewBrightness = (float) o.optDouble("vB", 0.0);
        l.viewSaturation = (float) o.optDouble("vS", 0.0);
        l.viewHighlights = (float) o.optDouble("vHi", 0.0);
        l.viewShadows = (float) o.optDouble("vSh", 0.0);
        l.viewBlack = (float) o.optDouble("vBk", 0.0);
        l.viewWhite = (float) o.optDouble("vWh", 0.0);
        l.viewVignette = (float) o.optDouble("vV", 0.0);
        l.cameraEv = (float) o.optDouble("cEv", 0.0);
        l.viewVibrance = (float) o.optDouble("vVb", 0.0);
        l.hslHue = readBands(o.optJSONArray("hslH"));
        l.hslSat = readBands(o.optJSONArray("hslS"));
        l.hslLum = readBands(o.optJSONArray("hslL"));
        l.viewSharpen = (float) o.optDouble("shp", 0.0);
        l.viewSharpenRadius = (float) o.optDouble("shpR", 1.0);
        l.viewSharpenMask = (float) o.optDouble("shpM", 0.0);
        l.viewTexture = (float) o.optDouble("tex", 0.0);
        l.wbPreset = o.optInt("wb", 0);
        l.wbKelvin = (float) o.optDouble("wbK", 5500.0);
        l.wbTint = (float) o.optDouble("wbT", 0.0);
        final org.json.JSONArray wbArr = o.optJSONArray("wbm");
        if (wbArr != null && wbArr.length() == 4) {
            l.wbMul = new float[4];
            for (int i = 0; i < 4; ++i) l.wbMul[i] = (float) wbArr.optDouble(i, 1.0);
        }
        l.geometryTurns = o.optInt("gturn", 0);
        l.geometryRotation = (float) o.optDouble("grot", 0.0);
        l.geometryCropW = (float) o.optDouble("gcw", 1.0);
        l.geometryCropH = (float) o.optDouble("gch", 1.0);
        l.geometryCropX = (float) o.optDouble("gcx", 0.0);
        l.geometryCropY = (float) o.optDouble("gcy", 0.0);
        l.cropRatioIndex = o.optInt("gcri", -1);
        l.geometryFlipH = o.optBoolean("gfh", false);
        l.geometryFlipV = o.optBoolean("gfv", false);
        l.yFilter = (float) o.optDouble("yf", 0.0);
        l.mFilter = (float) o.optDouble("mf", 0.0);
        l.filterScale = (float) o.optDouble("fscale", 1.0);
        l.preflash = (float) o.optDouble("preflash", 0.0);
        l.edgeFog = (float) o.optDouble("fog", 1.0);
        l.edgeLeaks = (float) o.optDouble("leaks", 0.0);
        l.frameNumber = (float) o.optDouble("frameno", 0.0);
        l.pfKind = o.optString("pfk", "none");
        l.pfChosen = o.optInt("pfc", 0) != 0;
        l.pfMargin = (float) o.optDouble("pfm", 3.0);
        l.pfFx = (float) o.optDouble("pffx", 0.0);
        l.pfFy = (float) o.optDouble("pffy", 0.0);
        l.halationOn = (float) o.optDouble("halon", 1.0);
        l.grainOn = (float) o.optDouble("grainon", 1.0);
        l.couplersOn = (float) o.optDouble("coupon", 1.0);
        l.antihalationRemoved = (float) o.optDouble("ahrem", 0.0);
        l.scannerWhite = (float) o.optDouble("scw", 0.0);
        l.scannerBlack = (float) o.optDouble("scb", 0.0);
        l.edrOn = (float) o.optDouble("edr", 0.0);
        // **Every look saved before 1.6.3v carries 0/0 because that was the default,
        // not because anyone chose it** -- and 0/0 is exactly the state users call
        // "the highlights cannot be pulled back". Lift it once, then honour whatever
        // the user does afterwards (the marker keeps the lift from repeating).
        if (!o.has("hmig") && l.highlightRoom <= 0.01f && l.edrOn <= 0.5f) {
            l.highlightRoom = 1.0f;
            l.edrOn = 1f;
        }
        // **Two sliders changed what their number means.**
        //
        // 预闪 used to be sent as 4 % of its slider, and 散射半径 as 1 + 11x; both
        // now send the engine's own number (0..1 and 1..64), so a look saved
        // under the old arithmetic is converted once. Without this a stored
        // 0.5 preflash loads as a flash 25x brighter than the one it showed.
        if (!o.has("lmig")) {
            l.preflash *= 0.04f / 0.2f;
            l.scatterRadius *= 11.0f / 63.0f;
        }
        l.glareActive = (float) o.optDouble("gla", 1.0);
        l.scannerLensBlur = (float) o.optDouble("slb", 0.0);
        l.halationHeadroom = (float) o.optDouble("hhe", 4.0);
        l.densityGamma = (float) o.optDouble("dcg", 1.0);
        l.digitalIntermediate = o.optBoolean("dint", false);
        l.diBlueComp = o.optBoolean("dibc", false);
        l.outputCctf = o.optBoolean("occtf", true);
        l.hueCorrection = (float) o.optDouble("hcor", 1.0);
        // **1.5.4a shipped two wrong defaults for a few hours.** It sent
        // `halation_headroom_ev` 0 (engine default 4) and `output_cctf_encoding`
        // false (engine default true); either alone darkens every frame, and
        // together they made the print look broken. No UI could reach those
        // fields then, so a stored pair of exactly 0 + false can only be that
        // build's fingerprint -- lift it to what the engine actually uses.
        if (l.halationHeadroom <= 0f && !l.outputCctf) {
            l.halationHeadroom = 4f;
            l.outputCctf = true;
        }
        l.shadowRolloff = (float) o.optDouble("rolloff", 2.0);
        for (int i = 0; i < 4; ++i) {
            l.cbHue[i] = (float) o.optDouble("cbh" + i, 0.0);
            l.cbSat[i] = (float) o.optDouble("cbs" + i, 0.0);
            l.cbLum[i] = (float) o.optDouble("cbl" + i, 0.0);
        }
        for (int i = 0; i < 5; ++i) {
            final org.json.JSONArray arr = o.optJSONArray("cv" + i);
            if (arr == null) continue;
            final float[] flat = new float[arr.length()];
            for (int k = 0; k < arr.length(); ++k) flat[k] = (float) arr.optDouble(k, 0.0);
            l.curves[i] = flat;
        }
        l.maxLift = (float) o.optDouble("lift", 4.0);
        l.cameraFilter = o.optString("cfilter", "");
        l.sceneNorm = o.optString("snorm", "power");
        l.edgeMode = o.optString("emode", "strip");
        l.edgeGate = o.optString("egate", "auto");
        l.edgeHoles = o.optString("eholes", "white");
        l.edgeCarrier = o.optString("ecarrier", "none");
        l.datePlacement = o.optString("dplace", "frame");
        l.dateCorner = o.optString("dcorn", "br");
        l.dateStyle = o.optString("dstyle", "lcd");
        l.dateInsetX = (float) o.optDouble("dinsx", 3.0);
        l.dateInsetY = (float) o.optDouble("dinsy", 2.4);
        l.edgeTextOn = o.optBoolean("etext", true);
        l.edgeFNumber = (float) o.optDouble("efnum", 0.0);
        l.dateSize = (float) o.optDouble("dsize", 1.0);
        // **The desktop's own migration.** Its date base moved 3.5 -> 5.5
        // (`FilmEdge.swift`: `storedBaseEV` / `glowBaseEV`), and a frame whose
        // stored value is the old default is moved rather than left behind --
        // otherwise a look saved before the change prints a dimmer date than a
        // Mac frame with the same settings.
        l.dateEv = (float) o.optDouble("dev", 5.5);
        if (Math.abs(l.dateEv - 3.5f) < 1e-4f) l.dateEv = 5.5f;
        l.dataText = o.optString("dtext", "");
        l.dateTextB = o.optString("dtextb", "");
        l.cameraSeed = o.optInt("cseed", 1);
        l.frameSeed = o.optInt("fseed", 1);
        l.filmFormatMm = (float) o.optDouble("mm", 36.0);
        l.overscanOn = o.optBoolean("overscan", false);
        final String format = o.optString("format", "135");
        l.overscanFormat = format.isEmpty() ? "135" : format;
        l.overscanPair = o.optBoolean("pair", false);
        l.pairSplit = (float) o.optDouble("psplit", 0.514);
        l.pairTone = o.optBoolean("pton", false);
        l.pairHighKnee = (float) o.optDouble("pkh", 1.0);
        l.pairHighRoom = (float) o.optDouble("phr", 2.0);
        l.pairShadowKnee = (float) o.optDouble("psk", -1.0);
        l.pairShadowRoom = (float) o.optDouble("psr", 0.0);
        l.dateOn = o.optBoolean("date", false);
        l.fotuEv = (float) o.optDouble("ftev", 0.0);
        l.fotuHighlights = (float) o.optDouble("fthi", 0.0);
        l.fotuShadows = (float) o.optDouble("ftsh", 0.0);
        l.fotuSaturation = (float) o.optDouble("ftsa", 1.0);
        l.fotuVibrance = (float) o.optDouble("ftvi", 0.0);
        l.fotuWarmth = (float) o.optDouble("ftwa", 0.0);
        l.fotuTint = (float) o.optDouble("ftti", 0.0);
        l.fotuFlare = (float) o.optDouble("ftfl", 0.0);
        l.fotuGradeLift = (float) o.optDouble("ftli", 0.0);
        l.fotuGradeGain = (float) o.optDouble("ftga", 0.0);
        l.fotuExpired = (float) o.optDouble("ftxp", 0.0);
        l.fotuBleach = (float) o.optDouble("ftbl", 0.0);
        l.fotuPush = (float) o.optDouble("ftpu", 0.0);
        // 1 leaves the film's own calibration alone; it is the fallback for a look saved
        // before these three existed.
        l.fotuGrain = (float) o.optDouble("ftgr", 1.0);
        l.fotuFilmGrain = o.optInt("ftfg", 0) != 0;
        l.fotuGrainSize = (float) o.optDouble("ftgs", 1.0);
        l.fotuGrainColour = (float) o.optDouble("ftgc", 1.0);
        l.fotuGrainSoft = (float) o.optDouble("ftgso", 1.0);
        l.fotuShadowDetail = (float) o.optDouble("ftsd", 0.0);
        l.fotuHighlightDetail = (float) o.optDouble("fthd", 0.0);
        l.fotuPrintCorrection = (float) o.optDouble("ftpc", 0.0);
        l.fotuGrainR = (float) o.optDouble("ftlr", 1.0);
        l.fotuGrainG = (float) o.optDouble("ftlg", 1.0);
        l.fotuGrainB = (float) o.optDouble("ftlb", 1.0);
        l.fotuPreflash = (float) o.optDouble("ftpf", 0.0);
        l.fotuFringe = (float) o.optDouble("ftfr", 0.0);
        l.fotuHalation = (float) o.optDouble("ftha", 1.0);
        l.fotuHalationColour = (float) o.optDouble("fthc", 0.0);
        l.fotuCouplers = (float) o.optDouble("ftcp", 1.0);
        l.fotuCouplerReach = (float) o.optDouble("ftcr", 1.0);
        l.fotuCouplerRG = (float) o.optDouble("ftcrg", 1.0);
        l.fotuCouplerGB = (float) o.optDouble("ftcgb", 1.0);
        l.fotuCouplerSelf = (float) o.optDouble("ftcs", 1.0);
        l.fotuMottle = (float) o.optDouble("ftmt", 0.0);
        l.fotuEncodedGrade = (float) o.optDouble("fteg", 0.0);
        l.fotuCondenser = (float) o.optDouble("ftcn", 0.0);
        l.fotuPrintLight = o.optString("ftpl", "");
        l.fotuPrinter = (float) o.optDouble("ftpo", 0.0);
        l.fotuPrinterLamp = (float) o.optDouble("ftpk", 3200.0);
        l.fotuPrinterM = (float) o.optDouble("ftpm", 0.4);
        l.fotuPrinterY = (float) o.optDouble("ftpy", 0.5);
        l.fotuPrinterEv = (float) o.optDouble("ftpx", 0.0);
        l.fotuPrinterR = (float) o.optDouble("ftpr", 25.0);
        l.fotuPrinterG = (float) o.optDouble("ftpg", 25.0);
        l.fotuPrinterB = (float) o.optDouble("ftpb", 25.0);
        l.fotuLocalTone = (float) o.optDouble("ftlt", 1.0);
        l.fotuDigitalRef = o.optString("ftdr", "");
        if (l.fotuLocalTone <= 0f) l.fotuLocalTone = 0f; else l.fotuLocalTone = 1f;
        if (l.fotuPrinterLamp < 2800f) l.fotuPrinterLamp = 2800f;
        if (l.fotuPrinterLamp > 3600f) l.fotuPrinterLamp = 3600f;
        l.fotuFringeRadius = (float) o.optDouble("ftfrr", 100.0);
        l.fotuBlackPoint = (float) o.optDouble("ftbp", 0.0);
        l.fotuWhitePoint = (float) o.optDouble("ftwp", 1.0);
        l.fotuPrinterPreflash = (float) o.optDouble("ftppf", 0.0);
        // **A zero in all three is the old bug, not a choice.** Every look written
        // before this build carries 0 for the grain, coupler and halation
        // multipliers, because `defaultLook` never initialised them and the slots
        // writer treats anything that differs from 1 as a request -- so those
        // frames asked for `mul:30..32=0` (no grain), `mul:58/59=0` (no couplers)
        // and `physmul:halationSigmaMM_*=0` (no halation). Raise them once; a user
        // who really wants any of them off can still pull it down afterwards,
        // and then `fmi2` keeps the choice.
        if (!o.has("fmi2")
                && l.fotuGrain <= 1e-4f && l.fotuCouplers <= 1e-4f && l.fotuHalation <= 1e-4f) {
            l.fotuGrain = 1f;
            l.fotuCouplers = 1f;
            l.fotuHalation = 1f;
        }
        return l;
    }
}
