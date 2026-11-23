package app.spektralab.ui.panels;

import app.spektralab.MainActivity.Look;

/** The neutral Look a fresh import starts from (extracted from MainActivity). */
public final class LookDefaults {
    private LookDefaults() {}

    public static Look create(boolean isRawFile, float formatMm) {
    final Look look = new Look();
    look.filmStock = null;
    look.printStock = "kodak_portra_endura";
    look.printExposure = 1.0f;
    // A raw is scene-referred and has no exposure decision of its own; a
    // camera JPEG has already been metered once, by the camera, and
    // metering it again is how "import this photo" turned into a grey wash.
    look.meterMethod = isRawFile ? "protect_highlights" : "";
    look.grainAmount = 1.0f;
    look.halationAmount = 1.0f;
    look.glareAmount = 1.0f;
    look.highlightRoom = 1.0f;
    look.shadowRoom = 0.0f;
    look.hlManual = false;
    look.hlKnee = 2.0f;
    look.hlRolloff = 2.0f;
    look.hlRoom = 2.4f;
    look.halationBoost = 0.0f;
    look.halationScatter = 1.0f;
    look.scatterRadius = 0.0f;
    look.lensBlur = 0.0f;
    look.viewVignette = 0.0f;
    look.hslHue = new float[8];
    look.hslSat = new float[8];
    look.hslLum = new float[8];
    look.viewSharpen = 0.0f;
    look.viewSharpenRadius = 1.0f;
    look.viewSharpenMask = 0.0f;
    look.viewTexture = 0.0f;
    look.cameraEv = 0.0f;
    look.wbPreset = 0;
    look.wbKelvin = 5500f;
    look.wbTint = 0.0f;
    look.wbMul = null;
    look.geometryTurns = 0;
    look.geometryRotation = 0.0f;
    look.geometryCropW = 1.0f;
    look.geometryCropH = 1.0f;
    look.geometryCropX = 0.0f;
    look.geometryCropY = 0.0f;
    look.cropRatioIndex = -1;
    look.geometryFlipH = false;
    look.geometryFlipV = false;
    look.yFilter = 0.0f;
    look.mFilter = 0.0f;
    look.filterScale = 1.0f;
    look.preflash = 0.0f;
    look.edgeFog = 1.0f;
    look.edgeLeaks = 0.0f;
    look.frameNumber = 0.0f;
    look.pfKind = "none";
    look.pfMargin = 3.0f;
    look.pfFx = 0.0f;
    look.pfFy = 0.0f;
    look.halationOn = 1f;
    look.grainOn = 1f;
    look.couplersOn = 1f;
    look.antihalationRemoved = 0f;
    look.scannerWhite = 0f;
    look.scannerBlack = 0f;
    look.edrOn = 1f;
    look.glareActive = 1f;
    look.scannerLensBlur = 0f;
    look.halationHeadroom = 4f;
    look.densityGamma = 1f;
    look.digitalIntermediate = false;
    look.diBlueComp = false;
    look.outputCctf = true;
    look.hueCorrection = 1f;
    look.shadowRolloff = 2f;
    look.maxLift = 4f;
    look.cameraFilter = "";
    look.sceneNorm = "power";
    look.edgeMode = "strip";
    look.edgeGate = "auto";
    look.edgeHoles = "white";
    look.edgeCarrier = "none";
    look.datePlacement = "frame";
    look.dateCorner = "br";
    look.dateStyle = "lcd";
    look.dateInsetX = 3f;
    look.dateInsetY = 2.4f;
    look.edgeTextOn = true;
    look.edgeFNumber = 0f;
    look.dateSize = 1f;
    look.dateEv = 5.5f;
    look.dataText = "";
    look.dateTextB = "";
    look.cameraSeed = 1;
    look.frameSeed = 1;
    look.filmFormatMm = formatMm;
    look.overscanOn = false;
    look.overscanFormat = "135";
    look.dateOn = false;
    // fotufilm's controls are per frame as well.
    //
    // **`new Look()` holds 0 for every one of them, and 0 is not neutral for
    // the multipliers.** `fotuSlots` writes a multiplier whenever it differs
    // from 1, so a zeroed look asked the engine for `mul:30..32=0` (no
    // grain), `mul:58/59=0` + `mulrange:66-449=0` (no couplers) and
    // `physmul:halationSigmaMM_*=0` (no halation) -- three of the model's
    // physical stages switched off on every freshly imported frame, and the
    // panel's 重置 turned them back on. That is why the grain only appeared
    // after opening 冲印 and pressing reset.
    look.fotuSaturation = 1f;
    look.fotuGrain = 1f;
    look.fotuGrainSize = 1f;
    look.fotuGrainColour = 1f;
    look.fotuGrainSoft = 1f;
    look.fotuGrainR = 1f;
    look.fotuGrainG = 1f;
    look.fotuGrainB = 1f;
    look.fotuCouplers = 1f;
    look.fotuHalation = 1f;
    look.fotuCouplerReach = 1f;
    look.fotuCouplerRG = 1f;
    look.fotuCouplerGB = 1f;
    look.fotuCouplerSelf = 1f;
    look.fotuMottle = 0f;
    look.fotuEncodedGrade = 0f;
    look.fotuCondenser = 0f;
    look.fotuPrintLight = "";
    look.fotuPrinter = 0f;
    look.fotuPrinterLamp = 3200f;
    look.fotuPrinterM = 0.4f;
    look.fotuPrinterY = 0.5f;
    look.fotuPrinterEv = 0f;
    look.fotuPrinterR = 25f;
    look.fotuPrinterG = 25f;
    look.fotuPrinterB = 25f;
    look.fotuLocalTone = 1f;
    look.fotuDigitalRef = "";
    look.fotuFringeRadius = 100f;
    look.fotuBlackPoint = 0f;
    look.fotuWhitePoint = 1f;
    look.fotuPrinterPreflash = 0f;
    return look;
    }
}
