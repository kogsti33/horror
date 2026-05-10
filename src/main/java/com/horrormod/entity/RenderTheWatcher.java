package com.horrormod.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders TheWatcher as a distorted player-like figure with a washed-out skin.
 */
public class RenderTheWatcher extends RenderLiving {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("horrormod", "textures/entity/watcher.png");

    public RenderTheWatcher() {
        super(new ModelBiped(), 0.0f); // No shadow
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z,
                         float yaw, float partialTick) {
        // Subtle flicker effect
        if (entity.worldObj.rand.nextInt(10) == 0) {
            GL11.glColor4f(1f, 1f, 1f, 0.85f);
        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }
        super.doRender(entity, x, y, z, yaw, partialTick);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    protected boolean canRenderName(Entity entity) {
        return false; // No name tag
    }
}
