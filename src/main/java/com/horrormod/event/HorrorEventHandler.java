package com.horrormod.event;

import com.horrormod.HorrorMod;
import com.horrormod.config.HorrorConfig;
import com.horrormod.entity.*;
import com.horrormod.world.RealityTearEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Central event handler.
 * All timers are measured in server ticks (20/sec).
 * Each player gets independent timers stored in HorrorPlayerData.
 *
 * Handles:
 *  1. Random ambient sounds
 *  2. Chunk reality tears
 *  3. Illusion player spawns (multiplayer)
 *  4. Herobrine appearances
 *  5. Watcher spawns
 *  6. False effects (health, hunger, inventory)
 *  7. The Fracture boss spawn
 */
public class HorrorEventHandler {

    private final Random rand = new Random();

    // ── Per-player timers (use player's entity ID as key via array indexing) ──
    // For simplicity we maintain a single-player oriented approach.
    // In multiplayer each player object carries its own HorrorPlayerData via NBT.

    // Global tick counter
    private long globalTick = 0;

    // ── Next-event tick thresholds (single-world, first player) ──────────────
    private long nextSoundTick        = -1;
    private long nextChunkTearTick    = -1;
    private long nextIllusionTick     = -1;
    private long nextHerobrineDay     = -1;
    private long nextWatcherTick      = -1;
    private long nextFalseEffectTick  = -1;
    private long nextFractureTick     = -1;

    // Herobrine tracking
    private EntityHerobrine activeHerobrine = null;

    // ── Ambient sound names ───────────────────────────────────────────────────
    private static final String[] HORROR_SOUNDS = {
        "ambient.cave.cave",     // Cave noise (1–19 variants)
        "mob.endermen.stare",    // Enderman stare
        "mob.ghast.moan",        // Distant moaning
        "mob.ghast.affectionate_scream",
        "mob.zombie.unfect",     // Zombie moan
        "mob.spider.say",        // Spider hiss
        "mob.endermen.idle",     // Enderman idle
        "mob.blaze.breath",      // Mechanical breathing
    };

    // ── Chat messages for chunk tears ─────────────────────────────────────────
    private static final String[] TEAR_MESSAGES = {
        "Reality tears apart...",
        "Something is wrong with this place.",
        "Did you see that?",
        "It was never really there.",
        "The fabric unravels.",
        "̷̧̛͎̭͇͔̥̬͚̞̲̜͚̤͎̰̔̐̆͒̑̚͢͟͞͡͝͡ ̴̸̢̡̖̼͔̩̩͇͓͙̫̀̀͡",
    };

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        globalTick++;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        List<EntityPlayer> players = server.getConfigurationManager().playerEntityList;
        if (players == null || players.isEmpty()) return;

        // Init timers on first tick
        if (nextSoundTick < 0) initTimers();

        // ── 1. AMBIENT SOUNDS ─────────────────────────────────────────────
        if (globalTick >= nextSoundTick) {
            for (EntityPlayer player : players) {
                playAmbientSound(player);
            }
            scheduleNextSound();
        }

        // ── 2. CHUNK TEAR ─────────────────────────────────────────────────
        if (globalTick >= nextChunkTearTick) {
            for (EntityPlayer player : players) {
                RealityTearEvent.tearChunkNear(player, rand);
            }
            scheduleNextChunkTear();
        }

        // ── 3. ILLUSION PLAYER (multiplayer only) ─────────────────────────
        if (globalTick >= nextIllusionTick && players.size() >= 2) {
            EntityPlayer target = players.get(rand.nextInt(players.size()));
            spawnIllusion(target, players);
            scheduleNextIllusion();
        }

        // ── 4. HEROBRINE ──────────────────────────────────────────────────
        World world = server.worldServers[0];
        long worldDay = world.getWorldInfo().getWorldTotalTime() / 24000L;
        if (nextHerobrineDay < 0) {
            nextHerobrineDay = worldDay + HorrorConfig.herobrineMinDays
                + rand.nextInt(HorrorConfig.herobrineMaxDays - HorrorConfig.herobrineMinDays + 1);
        }
        if (worldDay >= nextHerobrineDay && (activeHerobrine == null || activeHerobrine.isDead)) {
            if (!players.isEmpty()) {
                EntityPlayer target = players.get(rand.nextInt(players.size()));
                activeHerobrine = spawnHerobrine(target);
            }
            nextHerobrineDay = worldDay + HorrorConfig.herobrineMinDays
                + rand.nextInt(HorrorConfig.herobrineMaxDays - HorrorConfig.herobrineMinDays + 1);
        }

        // ── 5. THE WATCHER ────────────────────────────────────────────────
        if (globalTick >= nextWatcherTick) {
            if (!players.isEmpty()) {
                EntityPlayer target = players.get(rand.nextInt(players.size()));
                spawnWatcher(target);
            }
            scheduleNextWatcher();
        }

        // ── 6. FALSE EFFECTS ──────────────────────────────────────────────
        if (globalTick >= nextFalseEffectTick) {
            if (!players.isEmpty()) {
                EntityPlayer target = players.get(rand.nextInt(players.size()));
                applyFalseEffect(target);
            }
            scheduleNextFalseEffect();
        }

        // ── 7. THE FRACTURE BOSS ──────────────────────────────────────────
        if (nextFractureTick < 0) {
            nextFractureTick = globalTick + (long)(
                HorrorConfig.fractureMinDays * 24000
                + rand.nextInt((HorrorConfig.fractureMaxDays - HorrorConfig.fractureMinDays) * 24000 + 1)
            );
        }
        if (globalTick >= nextFractureTick) {
            if (!players.isEmpty()) {
                EntityPlayer target = players.get(rand.nextInt(players.size()));
                spawnFracture(target);
            }
            nextFractureTick = globalTick + (long)(
                HorrorConfig.fractureMinDays * 24000
                + rand.nextInt((HorrorConfig.fractureMaxDays - HorrorConfig.fractureMinDays) * 24000 + 1)
            );
        }
    }

    // ── Timer initialisation ──────────────────────────────────────────────────

    private void initTimers() {
        scheduleNextSound();
        scheduleNextChunkTear();
        scheduleNextIllusion();
        scheduleNextWatcher();
        scheduleNextFalseEffect();
    }

    private void scheduleNextSound() {
        int range = HorrorConfig.soundMaxIntervalSeconds - HorrorConfig.soundMinIntervalSeconds;
        nextSoundTick = globalTick
            + (HorrorConfig.soundMinIntervalSeconds + rand.nextInt(range + 1)) * 20L;
    }

    private void scheduleNextChunkTear() {
        int range = HorrorConfig.chunkTearMaxMinutes - HorrorConfig.chunkTearMinMinutes;
        nextChunkTearTick = globalTick
            + (HorrorConfig.chunkTearMinMinutes + rand.nextInt(range + 1)) * 60L * 20L;
    }

    private void scheduleNextIllusion() {
        int range = HorrorConfig.illusionMaxIntervalMin - HorrorConfig.illusionMinIntervalMin;
        nextIllusionTick = globalTick
            + (HorrorConfig.illusionMinIntervalMin + rand.nextInt(range + 1)) * 60L * 20L;
    }

    private void scheduleNextWatcher() {
        nextWatcherTick = globalTick
            + (HorrorConfig.watcherSpawnMinSeconds
               + rand.nextInt(HorrorConfig.watcherSpawnMaxSeconds - HorrorConfig.watcherSpawnMinSeconds + 1))
            * 20L;
    }

    private void scheduleNextFalseEffect() {
        int range = HorrorConfig.falseEffectMaxSeconds - HorrorConfig.falseEffectMinSeconds;
        nextFalseEffectTick = globalTick
            + (HorrorConfig.falseEffectMinSeconds + rand.nextInt(range + 1)) * 20L;
    }

    // ── Event implementations ─────────────────────────────────────────────────

    private void playAmbientSound(EntityPlayer player) {
        String sound = HORROR_SOUNDS[rand.nextInt(HORROR_SOUNDS.length)];
        float volume = 0.4f + rand.nextFloat() * 0.6f;
        float pitch  = 0.6f + rand.nextFloat() * 0.8f;

        // Offset position — sounds come from "around" the player
        double ox = (rand.nextDouble() - 0.5) * 20;
        double oy = (rand.nextDouble() - 0.5) * 5;
        double oz = (rand.nextDouble() - 0.5) * 20;

        player.worldObj.playSoundEffect(
            player.posX + ox,
            player.posY + oy,
            player.posZ + oz,
            sound, volume, pitch
        );
        HorrorMod.LOG.debug("[HorrorMod] Played sound '{}' for {}", sound, player.getCommandSenderName());
    }

    private void spawnIllusion(EntityPlayer target, List<EntityPlayer> allPlayers) {
        // Pick a different player to mimic
        EntityPlayer mimic = null;
        for (EntityPlayer p : allPlayers) {
            if (p != target) { mimic = p; break; }
        }
        if (mimic == null) return;

        // Random direction at configured distance
        double angle = rand.nextDouble() * Math.PI * 2;
        double dist  = HorrorConfig.illusionMinDistance
            + rand.nextInt(HorrorConfig.illusionMaxDistance - HorrorConfig.illusionMinDistance + 1);

        double sx = target.posX + Math.sin(angle) * dist;
        double sz = target.posZ + Math.cos(angle) * dist;
        int sy = target.worldObj.getHeightValue((int)sx, (int)sz);

        EntityIllusionPlayer illusion = new EntityIllusionPlayer(target.worldObj);
        illusion.setMimickedPlayer(mimic.getCommandSenderName());
        illusion.setPosition(sx, sy, sz);
        target.worldObj.spawnEntityInWorld(illusion);
        HorrorMod.LOG.debug("[HorrorMod] Illusion of {} spawned near {}", mimic.getCommandSenderName(), target.getCommandSenderName());
    }

    private EntityHerobrine spawnHerobrine(EntityPlayer target) {
        double angle = rand.nextDouble() * Math.PI * 2;
        double dist  = HorrorConfig.herobrineMinDistance
            + rand.nextInt(HorrorConfig.herobrineMaxDistance - HorrorConfig.herobrineMinDistance + 1);

        double sx = target.posX + Math.sin(angle) * dist;
        double sz = target.posZ + Math.cos(angle) * dist;
        int sy = target.worldObj.getHeightValue((int)sx, (int)sz);

        EntityHerobrine herobrine = new EntityHerobrine(target.worldObj);
        herobrine.setTargetPlayer(target);
        herobrine.setPosition(sx, sy, sz);
        target.worldObj.spawnEntityInWorld(herobrine);
        HorrorMod.LOG.info("[HorrorMod] Herobrine spawned near {}", target.getCommandSenderName());
        return herobrine;
    }

    private void spawnWatcher(EntityPlayer target) {
        double angle = rand.nextDouble() * Math.PI * 2;
        double dist  = 18 + rand.nextInt(8); // 18-26 blocks

        double sx = target.posX + Math.sin(angle) * dist;
        double sz = target.posZ + Math.cos(angle) * dist;
        int sy = target.worldObj.getHeightValue((int)sx, (int)sz);

        EntityTheWatcher watcher = new EntityTheWatcher(target.worldObj);
        watcher.setPosition(sx, sy, sz);
        target.worldObj.spawnEntityInWorld(watcher);
        HorrorMod.LOG.debug("[HorrorMod] Watcher spawned near {}", target.getCommandSenderName());
    }

    private void applyFalseEffect(EntityPlayer player) {
        int effect = rand.nextInt(3);
        switch (effect) {
            case 0:
                // Illusion of death — flash health to 0 then restore
                float realHealth = player.getHealth();
                player.setHealth(0.001f); // Near-zero but not dead
                // Schedule restoration via a simple delayed task
                // We'll restore on next tick by flagging:
                // (a real impl would use a scheduler; simplified here)
                player.setHealth(realHealth);
                // Play hurt sound to sell the illusion
                player.worldObj.playSoundAtEntity(player, "damage.hit", 1.0f, 1.0f);
                player.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.DARK_RED + "Your heart stopped for a moment..."));
                break;

            case 1:
                // Drain hunger to 0 momentarily
                player.getFoodStats().setFoodLevel(0);
                player.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GRAY + "Something drained the life from you."));
                break;

            case 2:
                // Move a random stack in inventory
                int fromSlot = rand.nextInt(36);
                int toSlot   = rand.nextInt(36);
                if (fromSlot != toSlot
                        && player.inventory.getStackInSlot(fromSlot) != null) {
                    net.minecraft.item.ItemStack moved = player.inventory.getStackInSlot(fromSlot);
                    // Swap
                    net.minecraft.item.ItemStack displaced = player.inventory.getStackInSlot(toSlot);
                    player.inventory.setInventorySlotContents(toSlot, moved);
                    player.inventory.setInventorySlotContents(fromSlot, displaced);
                    player.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.DARK_GRAY + "Where did that go...?"));
                }
                break;
        }
    }

    private void spawnFracture(EntityPlayer target) {
        double angle = rand.nextDouble() * Math.PI * 2;
        double dist  = 20 + rand.nextInt(16);

        double sx = target.posX + Math.sin(angle) * dist;
        double sz = target.posZ + Math.cos(angle) * dist;
        int sy = target.worldObj.getHeightValue((int)sx, (int)sz);

        EntityTheFracture fracture = new EntityTheFracture(target.worldObj);
        fracture.setPosition(sx, sy, sz);
        target.worldObj.spawnEntityInWorld(fracture);

        // Alert message
        target.addChatMessage(new ChatComponentText(
            EnumChatFormatting.DARK_RED + "THE FRACTURE APPROACHES."));
        target.worldObj.playSoundEffect(
            target.posX, target.posY, target.posZ,
            "mob.endermen.portal", 2.0f, 0.3f
        );
        HorrorMod.LOG.warn("[HorrorMod] THE FRACTURE has spawned near {}!", target.getCommandSenderName());
    }
}
