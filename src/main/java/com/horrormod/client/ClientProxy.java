package com.horrormod.client;

import com.horrormod.CommonProxy;
import com.horrormod.HorrorMod;
import com.horrormod.entity.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.entity.RenderManager;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(
            EntityTheWatcher.class,
            new RenderTheWatcher()
        );
        RenderingRegistry.registerEntityRenderingHandler(
            EntityHerobrine.class,
            new RenderHerobrine()
        );
        RenderingRegistry.registerEntityRenderingHandler(
            EntityIllusionPlayer.class,
            new RenderIllusionPlayer()
        );
        RenderingRegistry.registerEntityRenderingHandler(
            EntityTheFracture.class,
            new RenderTheFracture()
        );

        // Register client-side tick handler for settings manipulation
        cpw.mods.fml.common.FMLCommonHandler.instance()
            .bus().register(new com.horrormod.client.SettingsManipulator());

        HorrorMod.LOG.info("[HorrorMod] Client renderers registered.");
    }
}
