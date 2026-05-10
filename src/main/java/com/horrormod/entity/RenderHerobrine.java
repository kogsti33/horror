package com.horrormod.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders Herobrine — standard biped with the herobrine.png texture (white eyes).
 * Emits a faint ambient glow-like tint.
 */
public class RenderHerobrine extends RenderLiving {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("horrormod", "textures/entity/herobrine.png");

    public RenderHerobrine() {
        super(new ModelBiped(), 0.0f);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z,
                         float yaw, float partialTick) {
        // Very slight blue-white tint to make the eyes pop
        GL11.glColor4f(0.95f, 0.95f, 1.0f, 1.0f);
        super.doRender(entity, x, y, z, yaw, partialTick);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    protected boolean canRenderName(Entity entity) {
        return false;
    }
}
