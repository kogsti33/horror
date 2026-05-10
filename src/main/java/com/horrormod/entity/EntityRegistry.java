package com.horrormod.entity;

import com.horrormod.HorrorMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityRegistry {

    private static int nextId = 200; // Start above vanilla IDs

    public static void registerEntities() {
        // The Watcher — silent observer
        EntityRegistry.registerModEntity(
            EntityTheWatcher.class,
            "TheWatcher",
            nextId++,
            HorrorMod.instance,
            80, 3, true
        );

        // Herobrine
        EntityRegistry.registerModEntity(
            EntityHerobrine.class,
            "Herobrine",
            nextId++,
            HorrorMod.instance,
            80, 3, true
        );

        // Illusion Player
        EntityRegistry.registerModEntity(
            EntityIllusionPlayer.class,
            "IllusionPlayer",
            nextId++,
            HorrorMod.instance,
            80, 3, true
        );

        // The Fracture boss
        EntityRegistry.registerModEntity(
            EntityTheFracture.class,
            "TheFracture",
            nextId++,
            HorrorMod.instance,
            160, 3, true
        );

        HorrorMod.LOG.info("[HorrorMod] Entities registered.");
    }
}
