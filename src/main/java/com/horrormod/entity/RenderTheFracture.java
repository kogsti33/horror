package com.horrormod.entity;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renders The Fracture — a glitchy, corrupted figure.
 * Uses a distorted texture and random scale flicker.
 */
public class RenderTheFracture extends RenderLiving {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("horrormod", "textures/entity/fracture.png");

    public RenderTheFracture() {
        super(new ModelBiped(0, 0, 64, 64), 0.5f);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z,
                         float yaw, float partialTick) {
        GL11.glPushMatrix();

        // Glitch: random minor scale distortion
        float scaleX = 1.0f + (float)(Math.random() * 0.06 - 0.03);
        float scaleY = 1.0f + (float)(Math.random() * 0.1  - 0.05);
        GL11.glScalef(scaleX, scaleY, scaleX);

        // Slight red tint pulsing
        float pulse = 0.7f + 0.3f * (float)Math.abs(Math.sin(
            System.currentTimeMillis() * 0.003));
        GL11.glColor4f(1.0f, pulse * 0.5f, pulse * 0.5f, 1.0f);

        super.doRender(entity, x, y, z, yaw, partialTick);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glPopMatrix();
    }

    @Override
    protected boolean canRenderName(Entity entity) {
        return false;
    }
}
