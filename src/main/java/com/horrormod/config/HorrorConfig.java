package com.horrormod.config;

import net.minecraftforge.common.config.Configuration;

public class HorrorConfig {

    // ── Sound settings ──────────────────────────────────────────────────
    public static int soundMinIntervalSeconds  = 30;
    public static int soundMaxIntervalSeconds  = 300;

    // ── Chunk tear settings ──────────────────────────────────────────────
    public static int chunkTearMinMinutes      = 15;
    public static int chunkTearMaxMinutes      = 45;
    public static int chunkTearRadius          = 64;
    /** 0 = permanent, otherwise number of in-game days before restoration */
    public static int chunkRestoreDays         = 5;

    // ── Illusion player settings ─────────────────────────────────────────
    public static int illusionMinDistance      = 16;
    public static int illusionMaxDistance      = 32;
    public static int illusionMinIntervalMin   = 10;
    public static int illusionMaxIntervalMin   = 30;

    // ── Herobrine settings ───────────────────────────────────────────────
    public static int herobrineMinDays         = 1;
    public static int herobrineMaxDays         = 3;
    public static int herobrineMinDistance     = 48;
    public static int herobrineMaxDistance     = 64;
    public static int herobrineMinStaySeconds  = 15;
    public static int herobrineMaxStaySeconds  = 30;

    // ── Settings manipulation ─────────────────────────────────────────────
    public static int settingsManipMinSeconds  = 60;
    public static int settingsManipMaxSeconds  = 180;
    public static int settingsEffectMinSeconds = 30;
    public static int settingsEffectMaxSeconds = 60;

    // ── The Watcher ───────────────────────────────────────────────────────
    public static int watcherSpawnMinSeconds   = 120;
    public static int watcherSpawnMaxSeconds   = 360;
    public static int watcherStayMinSeconds    = 10;
    public static int watcherStayMaxSeconds    = 15;

    // ── False effects ─────────────────────────────────────────────────────
    public static int falseEffectMinSeconds    = 90;
    public static int falseEffectMaxSeconds    = 240;

    // ── The Fracture boss ─────────────────────────────────────────────────
    public static int fractureMinDays          = 2;
    public static int fractureMaxDays          = 5;

    public static void init(Configuration cfg) {
        cfg.load();

        soundMinIntervalSeconds = cfg.getInt("soundMinInterval", "sound", 30, 10, 600,
            "Minimum interval between ambient horror sounds (seconds)");
        soundMaxIntervalSeconds = cfg.getInt("soundMaxInterval", "sound", 300, 30, 1800,
            "Maximum interval between ambient horror sounds (seconds)");

        chunkTearMinMinutes = cfg.getInt("chunkTearMin", "chunk", 15, 5, 120,
            "Minimum interval between chunk tears (minutes)");
        chunkTearMaxMinutes = cfg.getInt("chunkTearMax", "chunk", 45, 10, 240,
            "Maximum interval between chunk tears (minutes)");
        chunkTearRadius = cfg.getInt("chunkTearRadius", "chunk", 64, 16, 128,
            "Radius around player in which a chunk can be torn");
        chunkRestoreDays = cfg.getInt("chunkRestoreDays", "chunk", 5, 0, 30,
            "Days until a torn chunk restores (0 = never)");

        illusionMinDistance = cfg.getInt("illusionMinDist", "illusion", 16, 8, 64,
            "Minimum spawn distance of illusion players");
        illusionMaxDistance = cfg.getInt("illusionMaxDist", "illusion", 32, 16, 128,
            "Maximum spawn distance of illusion players");
        illusionMinIntervalMin = cfg.getInt("illusionMinInterval", "illusion", 10, 3, 60,
            "Minimum interval between illusion spawns (minutes)");
        illusionMaxIntervalMin = cfg.getInt("illusionMaxInterval", "illusion", 30, 5, 120,
            "Maximum interval between illusion spawns (minutes)");

        herobrineMinDays = cfg.getInt("herobrineMinDays", "herobrine", 1, 1, 10,
            "Minimum in-game days between Herobrine appearances");
        herobrineMaxDays = cfg.getInt("herobrineMaxDays", "herobrine", 3, 1, 20,
            "Maximum in-game days between Herobrine appearances");

        settingsManipMinSeconds = cfg.getInt("settingsManipMin", "settings", 60, 30, 600,
            "Minimum interval between settings manipulations (seconds)");
        settingsManipMaxSeconds = cfg.getInt("settingsManipMax", "settings", 180, 60, 1800,
            "Maximum interval between settings manipulations (seconds)");

        falseEffectMinSeconds = cfg.getInt("falseEffectMin", "effects", 90, 30, 600,
            "Minimum interval between false effects (seconds)");
        falseEffectMaxSeconds = cfg.getInt("falseEffectMax", "effects", 240, 60, 1800,
            "Maximum interval between false effects (seconds)");

        fractureMinDays = cfg.getInt("fractureMinDays", "fracture", 2, 1, 10,
            "Minimum in-game days between Fracture boss spawns");
        fractureMaxDays = cfg.getInt("fractureMaxDays", "fracture", 5, 2, 20,
            "Maximum in-game days between Fracture boss spawns");

        if (cfg.hasChanged()) cfg.save();
    }
}
