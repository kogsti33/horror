package com.horrormod.world;

import com.horrormod.HorrorMod;
import com.horrormod.config.HorrorConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles the "Reality Tear" — deleting an entire chunk near a player.
 *
 * Tear records are stored (chunkX, chunkZ → world-day-of-tear) so that
 * if HorrorConfig.chunkRestoreDays > 0, the chunk can be regenerated later.
 */
public class RealityTearEvent {

    /** Map of (chunkX << 32 | chunkZ) → game-day when chunk was torn */
    private static final Map<Long, Long> tornChunks = new HashMap<>();

    private static final String[] TEAR_MESSAGES = {
        EnumChatFormatting.DARK_RED + "Reality tears apart...",
        EnumChatFormatting.DARK_GRAY + "Something was never really there.",
        EnumChatFormatting.GRAY + "The fabric unravels.",
        EnumChatFormatting.DARK_RED + "It crumbles into nothing.",
        EnumChatFormatting.OBFUSCATED + "§r" + EnumChatFormatting.DARK_GRAY + "̴̵̷̸̡̢̧̨̛̖̗̘̙̜̝̞̟̠...",
        EnumChatFormatting.DARK_PURPLE + "Do you feel that? Something is missing.",
    };

    /**
     * Selects a random chunk within the configured radius and erases it.
     */
    public static void tearChunkNear(EntityPlayer player, Random rand) {
        World world = player.worldObj;

        // Pick random offset in chunk units (radius / 16 chunks)
        int chunkRadius = HorrorConfig.chunkTearRadius / 16;
        int offsetX = (rand.nextInt(chunkRadius * 2 + 1) - chunkRadius);
        int offsetZ = (rand.nextInt(chunkRadius * 2 + 1) - chunkRadius);

        int playerChunkX = (int) Math.floor(player.posX / 16.0);
        int playerChunkZ = (int) Math.floor(player.posZ / 16.0);

        int targetChunkX = playerChunkX + offsetX;
        int targetChunkZ = playerChunkZ + offsetZ;

        // Don't delete the chunk the player is standing in
        if (targetChunkX == playerChunkX && targetChunkZ == playerChunkZ) {
            targetChunkX += (rand.nextBoolean() ? 1 : -1);
        }

        eraseChunk(world, targetChunkX, targetChunkZ, rand);

        // Record tear time
        long key = chunkKey(targetChunkX, targetChunkZ);
        long currentDay = world.getWorldInfo().getWorldTotalTime() / 24000L;
        tornChunks.put(key, currentDay);

        // Send scary message to player
        String msg = TEAR_MESSAGES[rand.nextInt(TEAR_MESSAGES.length)];
        player.addChatMessage(new ChatComponentText(msg));

        // Play a loud rupture sound at chunk centre
        double cx = (targetChunkX * 16) + 8;
        double cz = (targetChunkZ * 16) + 8;
        world.playSoundEffect(cx, player.posY, cz, "mob.endermen.portal", 3.0f,
            0.3f + rand.nextFloat() * 0.3f);
        world.playSoundEffect(cx, player.posY, cz, "random.explode", 2.0f,
            0.3f + rand.nextFloat() * 0.2f);

        HorrorMod.LOG.info("[HorrorMod] Reality tear at chunk ({}, {})", targetChunkX, targetChunkZ);
    }

    /**
     * Iterates over all 16×256×16 blocks in a chunk and sets them to air.
     * Also spawns smoke particles at every 4th block for visual effect.
     */
    private static void eraseChunk(World world, int chunkX, int chunkZ, Random rand) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    int bx = baseX + x;
                    int bz = baseZ + z;

                    if (!world.getBlock(bx, y, bz).isAir(world, bx, y, bz)) {
                        world.setBlockToAir(bx, y, bz);

                        // Spawn particles every 4th block to avoid lag
                        if (y % 4 == 0 && rand.nextInt(3) == 0) {
                            world.spawnParticle("largesmoke",
                                bx + 0.5, y + 0.5, bz + 0.5,
                                (rand.nextDouble() - 0.5) * 0.2,
                                rand.nextDouble() * 0.1,
                                (rand.nextDouble() - 0.5) * 0.2
                            );
                        }
                    }
                }
            }
        }

        // Forcibly update the chunk so clients re-render
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setChunkModified();
        }
    }

    /**
     * Call this periodically (e.g. once per in-game day) to restore old torn chunks.
     * Uses world.recreateChunk to regenerate using the world seed.
     */
    public static void checkAndRestoreChunks(World world) {
        if (HorrorConfig.chunkRestoreDays <= 0) return;

        long currentDay = world.getWorldInfo().getWorldTotalTime() / 24000L;

        tornChunks.entrySet().removeIf(entry -> {
            long tearDay = entry.getValue();
            if (currentDay - tearDay >= HorrorConfig.chunkRestoreDays) {
                long key = entry.getKey();
                int cx = (int)(key >> 32);
                int cz = (int)(key & 0xFFFFFFFFL);

                // Force chunk to regenerate from the world generator
                world.theChunkProviderServer.loadChunk(cx, cz);
                HorrorMod.LOG.info("[HorrorMod] Restored chunk ({}, {})", cx, cz);
                return true; // Remove from map
            }
            return false;
        });
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}
