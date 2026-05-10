package com.horrormod.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityHerobrine extends EntityLiving {

    private int ticksAlive = 0;
    /** Lifespan in ticks (15-30 seconds) */
    private int lifespanTicks;
    private boolean vanished = false;

    /** The player this Herobrine is targeting/watching */
    private EntityPlayer targetPlayer;

    public EntityHerobrine(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.noClip = true; // Clips through blocks — supernatural
        this.lifespanTicks = 300 + world.rand.nextInt(300); // 15-30 sec
    }

    public void setTargetPlayer(EntityPlayer player) {
        this.targetPlayer = player;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (worldObj.isRemote) return;

        ticksAlive++;

        if (targetPlayer == null || targetPlayer.isDead) {
            setDead();
            return;
        }

        // Always face the target player
        double dx = targetPlayer.posX - posX;
        double dz = targetPlayer.posZ - posZ;
        rotationYaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
        rotationPitch = 0;

        // Check if player is LOOKING AWAY — use look vector check
        boolean playerLookingAt = isPlayerLookingAt(targetPlayer);

        // Disappear if player is looking at it or if lifespan expired
        if (playerLookingAt || ticksAlive >= lifespanTicks) {
            vanishWithPortalEffects();
        }
    }

    /**
     * Returns true if the player's view vector roughly points toward this entity.
     * Threshold: within ~15 degrees of the entity centre.
     */
    private boolean isPlayerLookingAt(EntityPlayer player) {
        // Direction player is looking
        float yaw = player.rotationYaw;
        float pitch = player.rotationPitch;
        double lookX = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double lookY = -Math.sin(Math.toRadians(pitch));
        double lookZ =  Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

        // Vector from player to this entity
        double toX = posX - player.posX;
        double toY = (posY + height / 2) - (player.posY + player.getEyeHeight());
        double toZ = posZ - player.posZ;
        double len = Math.sqrt(toX*toX + toY*toY + toZ*toZ);
        if (len < 0.001) return true;
        toX /= len; toY /= len; toZ /= len;

        double dot = lookX*toX + lookY*toY + lookZ*toZ;
        // dot > cos(20°) ≈ 0.94 means player is looking roughly at the entity
        return dot > 0.94;
    }

    private void vanishWithPortalEffects() {
        if (vanished) return;
        vanished = true;

        for (int i = 0; i < 30; i++) {
            worldObj.spawnParticle("portal",
                posX + (worldObj.rand.nextDouble() - 0.5) * 2,
                posY + worldObj.rand.nextDouble() * 2,
                posZ + (worldObj.rand.nextDouble() - 0.5) * 2,
                (worldObj.rand.nextDouble() - 0.5) * 0.5,
                worldObj.rand.nextDouble() * 0.5,
                (worldObj.rand.nextDouble() - 0.5) * 0.5
            );
        }
        // cave_noise sound
        int caveNum = 1 + worldObj.rand.nextInt(19);
        worldObj.playSoundEffect(posX, posY, posZ,
            "ambient.cave.cave", 1.0f, 0.5f + worldObj.rand.nextFloat() * 0.5f);
        setDead();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false; // Immune to all damage
    }

    @Override
    protected boolean canDespawn() { return false; }

    @Override
    public boolean canBePushed() { return false; }

    @Override
    protected String getLivingSound()  { return null; }
    @Override
    protected String getHurtSound()    { return null; }
    @Override
    protected String getDeathSound()   { return null; }
    @Override
    protected int getExperiencePoints(EntityPlayer player) { return 0; }
}
