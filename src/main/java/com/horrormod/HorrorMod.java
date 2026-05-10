package com.horrormod;

import com.horrormod.config.HorrorConfig;
import com.horrormod.entity.EntityRegistry;
import com.horrormod.event.HorrorEventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
    modid = HorrorMod.MODID,
    name = HorrorMod.NAME,
    version = HorrorMod.VERSION
)
public class HorrorMod {

    public static final String MODID   = "horrormod";
    public static final String NAME    = "Reality Fracture";
    public static final String VERSION = "1.0.0";

    public static final Logger LOG = LogManager.getLogger(NAME);

    @SidedProxy(
        clientSide = "com.horrormod.client.ClientProxy",
        serverSide = "com.horrormod.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static HorrorMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        HorrorConfig.init(new Configuration(configFile));
        LOG.info("[HorrorMod] Pre-initialization complete.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EntityRegistry.registerEntities();
        MinecraftForge.EVENT_BUS.register(new HorrorEventHandler());
        proxy.registerRenderers();
        LOG.info("[HorrorMod] Initialization complete. Fear has been registered.");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOG.info("[HorrorMod] Post-initialization complete. They are watching.");
    }
}
