package com.horrormod.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders the illusion as the skin of the mimicked player.
 * Falls back to Steve if skin not cached.
 */
public class RenderIllusionPlayer extends RenderLiving {

    private static final ResourceLocation STEVE =
        new ResourceLocation("textures/entity/steve.png");

    public RenderIllusionPlayer() {
        super(new ModelBiped(), 0.0f);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        if (entity instanceof EntityIllusionPlayer) {
            String playerName = ((EntityIllusionPlayer) entity).getMimickedPlayerName();
            if (playerName != null && !playerName.isEmpty()) {
                // Use Minecraft's skin download system via the SkinManager
                net.minecraft.client.network.NetworkPlayerInfo info =
                    Minecraft.getMinecraft().getNetHandler() == null ? null :
                    Minecraft.getMinecraft().getNetHandler()
                        .getPlayerInfo(playerName);
                if (info != null) {
                    ResourceLocation loc = info.getLocationSkin();
                    if (loc != null) return loc;
                }
            }
        }
        return STEVE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z,
                         float yaw, float partialTick) {
        // Semi-transparent ghost look
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 0.75f);
        super.doRender(entity, x, y, z, yaw, partialTick);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    protected boolean canRenderName(Entity entity) {
        return false; // No name tag — that's the horror
    }
}
