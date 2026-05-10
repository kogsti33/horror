package com.horrormod.client;

import com.horrormod.config.HorrorConfig;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

import java.util.Random;

/**
 * CLIENT-SIDE ONLY.
 *
 * Periodically warps:
 *  - Brightness (gamma) → 0 (moody)
 *  - Render distance → 2 chunks
 *  - FOV → slow sweep 30–110°
 *  - Sound volume → random spikes/drops
 *
 * After the effect window, all settings are restored to the player's originals.
 */
public class SettingsManipulator {

    private final Random rand = new Random();
    private final Minecraft mc = Minecraft.getMinecraft();

    // ── State machine ────────────────────────────────────────────────────
    private enum Phase { WAITING, ACTIVE, RESTORING }
    private Phase phase = Phase.WAITING;

    private long tickCounter   = 0;
    private long nextEventTick = -1;    // When the next manipulation starts
    private long effectEndTick = -1;    // When the active effect ends

    // ── Saved originals ───────────────────────────────────────────────────
    private float savedGamma;
    private int   savedRenderDist;
    private float savedFOV;
    private float savedMasterVolume;

    // FOV sweep
    private float fovDirection   = 1f;
    private float currentFovWarp = 70f;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        tickCounter++;

        if (nextEventTick < 0) scheduleNextEvent();

        switch (phase) {
            case WAITING:
                if (tickCounter >= nextEventTick) {
                    saveSettings();
                    phase = Phase.ACTIVE;
                    int durRange = HorrorConfig.settingsEffectMaxSeconds - HorrorConfig.settingsEffectMinSeconds;
                    effectEndTick = tickCounter + (HorrorConfig.settingsEffectMinSeconds + rand.nextInt(durRange + 1)) * 20L;
                    currentFovWarp = mc.gameSettings.fovSetting;
                }
                break;

            case ACTIVE:
                applyEffects();
                if (tickCounter >= effectEndTick) {
                    phase = Phase.RESTORING;
                }
                break;

            case RESTORING:
                restoreSettings();
                scheduleNextEvent();
                phase = Phase.WAITING;
                break;
        }
    }

    private void saveSettings() {
        GameSettings gs = mc.gameSettings;
        savedGamma        = gs.gammaSetting;
        savedRenderDist   = gs.renderDistanceChunks;
        savedFOV          = gs.fovSetting;
        savedMasterVolume = gs.getSoundLevel(
            net.minecraft.client.audio.SoundCategory.MASTER);
    }

    private void restoreSettings() {
        GameSettings gs = mc.gameSettings;
        gs.gammaSetting          = savedGamma;
        gs.renderDistanceChunks  = savedRenderDist;
        gs.fovSetting            = savedFOV;
        gs.setSoundLevel(net.minecraft.client.audio.SoundCategory.MASTER, savedMasterVolume);
        mc.renderGlobal.loadRenderers(); // Rebuild render distance
    }

    private void applyEffects() {
        GameSettings gs = mc.gameSettings;

        // 1. Darkness — gamma = 0 (full moody)
        gs.gammaSetting = 0f;

        // 2. Minimal render distance
        if (gs.renderDistanceChunks != 2) {
            gs.renderDistanceChunks = 2;
            mc.renderGlobal.loadRenderers();
        }

        // 3. FOV sweep: oscillate between 30 and 110
        currentFovWarp += fovDirection * 0.4f;
        if (currentFovWarp > 110f) { currentFovWarp = 110f; fovDirection = -1f; }
        if (currentFovWarp < 30f)  { currentFovWarp = 30f;  fovDirection =  1f; }
        gs.fovSetting = currentFovWarp;

        // 4. Volume spike/drop every 60 ticks
        if (tickCounter % 60 == 0) {
            float vol = rand.nextFloat(); // 0–1
            gs.setSoundLevel(net.minecraft.client.audio.SoundCategory.MASTER, vol);
        }
    }

    private void scheduleNextEvent() {
        int range = HorrorConfig.settingsManipMaxSeconds - HorrorConfig.settingsManipMinSeconds;
        nextEventTick = tickCounter
            + (HorrorConfig.settingsManipMinSeconds + rand.nextInt(range + 1)) * 20L;
    }
}
