package app.spektralab.ui.panels;

import static app.spektralab.AppLanguage.tr;
import app.spektralab.engine.FotuFilm;
import app.spektralab.ui.Cover;
import java.util.Map;

/** Film-box palette and lettering (extracted from MainActivity). */
public final class FilmCoverStyles {
    private FilmCoverStyles() {}

    public static Cover create(String id, String caption,
            Map<String,String> brandMap, Map<String,String> techMap) {
    final Cover v = new Cover();
    v.body = 0xFFF2EDE3;
    v.band = 0xFFF5C518;
    v.ink = 0xFF171717;
    v.bandInk = 0xFFDA291C;
    v.accent = 0xFFDA291C;
    if (id == null) id = "";

    // **The name is not brand-specific.** This used to be parsed at the
    // bottom of the method, after every palette branch -- so the fotufilm
    // branch returned first and every second-engine card drew a coloured
    // band over a blank pane: a film box with no name on it, which reads
    // as a broken card. Colours belong to brands; the name is just the
    // caption minus the kind (tr("film.slide")) and minus the brand the band
    // already shows.
    String m = caption == null ? "" : caption;
    final int dot = m.indexOf(" · ");
    if (dot > 0) m = m.substring(0, dot);
    for (String b : new String[]{"Kodak ", "Fujifilm ", "Ilford ", "CineStill "}) {
        if (m.startsWith(b)) { m = m.substring(b.length()); break; }
    }
    v.model = m;

    // **A number in a film's name is only a speed if it is one.** "1443"
    // (Aerochrome) and "5222" (Double-X) are catalogue codes, and printing
    // them beside the wordmark as an ISO would be a lie on the box.
    final java.util.regex.Matcher mt =
            java.util.regex.Pattern.compile("(\\d{2,4})\\s*(D|T)?").matcher(m);
    while (mt.find()) {
        if (plausibleIso(mt.group(1)))
            v.iso = mt.group(1) + (mt.group(2) == null ? "" : mt.group(2));
    }

    if (id.startsWith(FotuFilm.PREFIX)) {
        // A different engine's film, drawn the same way: a band, a
        // wordmark, the stock's own colour. The wordmark is the one its
        // catalogue names, not a guess from the id.
        final String brand = brandMap.get(id);
        final String tech = techMap.get(id);
        // The band already says the brand; do not say it twice -- and only
        // at a word boundary, so "Agfa" does not turn "Agfachrome" into
        // "chrome RSX II 100".
        if (brand != null && v.model.startsWith(brand + " "))
            v.model = v.model.substring(brand.length() + 1);
        v.wordmark = brand == null ? "FOTUFILM" : brand.toUpperCase(java.util.Locale.US);
        v.tech = tech == null ? "FOTUFILM" : tech;
        v.band = 0xFF2B2B2B;
        v.bandInk = 0xFFF2EDE3;
        if ("Fujifilm".equals(brand)) {
            v.band = 0xFF007A3D;
        } else if ("Kodak".equals(brand)) {
            v.band = 0xFFE8B90C;
            v.bandInk = 0xFF171717;
        } else if ("Ilford".equals(brand)) {
            v.body = 0xFF141414;
            v.ink = 0xFFF4F2EC;
            v.band = 0xFF141414;
            v.bandInk = 0xFFF4F2EC;
        } else if ("CineStill".equals(brand)) {
            v.band = 0xFFC8102E;
        } else if ("Agfa".equals(brand)) {
            v.band = 0xFFE87722;
        } else if ("Lomography".equals(brand)) {
            v.band = 0xFF6D2C91;
        } else if ("Harman".equals(brand)) {
            v.band = 0xFF0B5FA5;
        } else if ("AR-5".equals(brand)) {
            v.band = 0xFF00837A;
        }
        return v;
    }

    if (id.startsWith("fujifilm")) {
        v.wordmark = "FUJIFILM";
        v.body = 0xFFF4F2EC;
        v.band = 0xFF007A3D;
        v.bandInk = 0xFFF4F2EC;
        v.ink = 0xFF0B6B38;
        v.accent = 0xFF6D2C91;
    } else if (id.startsWith("ilford")) {
        v.wordmark = "ILFORD";
        v.body = 0xFF141414;
        v.band = 0xFF141414;
        v.bandInk = 0xFFF4F2EC;
        v.ink = 0xFFF4F2EC;
        v.accent = 0xFFF4F2EC;
        v.tech = "BLACK & WHITE FILM";
    } else if (id.startsWith("cinestill")) {
        v.wordmark = "CINESTILL";
        v.body = 0xFFC8102E;
        v.band = 0xFF101010;
        v.bandInk = 0xFFF7F4EF;
        v.ink = 0xFFF7F4EF;
        v.accent = 0xFF2AA3DC;
        v.tech = "CINE FILM · 36 EXP";
    }

    if (id.equals("kodak_gold_200")) {
        v.body = 0xFFE6007E; v.ink = 0xFFFFFFFF;
    } else if (id.equals("kodak_colorplus_200")) {
        v.body = 0xFFEE2E24; v.ink = 0xFFFFFFFF;
    } else if (id.equals("kodak_ultramax_400")) {
        v.body = 0xFF0B5FA5; v.ink = 0xFFFFFFFF;
    } else if (id.equals("kodak_ektar_100")) {
        v.body = 0xFFF2A900; v.ink = 0xFF171717;
    } else if (id.startsWith("kodak_portra")) {
        v.band = 0xFFE8B90C;
    } else if (id.equals("kodak_tri_x_400") || id.equals("kodak_tmax_100")) {
        v.body = 0xFFF2F0EA; v.band = 0xFF141414;
        v.bandInk = 0xFFF2F0EA; v.ink = 0xFF141414;
        v.tech = "BLACK & WHITE FILM";
    } else if (id.startsWith("kodak_vision3")) {
        v.body = 0xFF1B1B1B; v.ink = 0xFFF4F2EC;
        v.tech = "CINE FILM · 400 FT";
        if (id.endsWith("50d")) v.band = 0xFFF2C300;
        else if (id.endsWith("250d")) v.band = 0xFFE07A24;
        else if (id.endsWith("200t")) v.band = 0xFFD64545;
        else v.band = 0xFFC8202F;
    } else if (id.equals("kodak_verita_200d")) {
        v.body = 0xFF1F6E43; v.ink = 0xFFF4F2EC;
    } else if (id.equals("kodak_ektachrome_100")) {
        v.body = 0xFF101010; v.band = 0xFFF2C300;
        v.bandInk = 0xFF171717; v.ink = 0xFFF4F2EC;
        v.tech = "COLOUR REVERSAL";
    } else if (id.equals("kodak_kodachrome_64")) {
        v.body = 0xFFC8102E; v.ink = 0xFFF7F4EF;
        v.tech = "COLOUR REVERSAL";
    } else if (id.equals("fujifilm_c200")) {
        v.body = 0xFF009A44; v.band = 0xFF6D2C91;
        v.bandInk = 0xFFFFFFFF; v.ink = 0xFFFFFFFF;
    } else if (id.equals("fujifilm_xtra_400")) {
        v.body = 0xFF007A3D; v.ink = 0xFFFFFFFF; v.bandInk = 0xFFFFFFFF;
    } else if (id.equals("fujifilm_pro_400h")) {
        v.body = 0xFF007A3D; v.band = 0xFFF4F2EC;
        v.bandInk = 0xFF007A3D; v.ink = 0xFFFFFFFF;
    } else if (id.equals("fujifilm_neopan_acros_100_ii")) {
        v.body = 0xFFF4F2EC; v.band = 0xFF141414;
        v.bandInk = 0xFFF4F2EC; v.ink = 0xFF141414;
        v.tech = "BLACK & WHITE FILM";
    } else if (id.equals("fujifilm_velvia_100")) {
        v.body = 0xFF101010; v.band = 0xFFC8102E;
        v.bandInk = 0xFFF4F2EC; v.ink = 0xFFF4F2EC;
        v.tech = "COLOUR REVERSAL";
    } else if (id.equals("fujifilm_provia_100f")) {
        v.body = 0xFFF4F2EC; v.band = 0xFF007A3D;
        v.ink = 0xFF0B6B38; v.tech = "COLOUR REVERSAL";
    }

    return v;
    }

    private static boolean plausibleIso(String digits) {
        switch (digits) {
            case "25": case "50": case "64": case "100": case "125": case "160":
            case "200": case "250": case "320": case "400": case "500": case "800":
            case "1000": case "1600": case "3200": case "6400": case "12800":
                return true;
            default:
                return false;
        }
    }
}
