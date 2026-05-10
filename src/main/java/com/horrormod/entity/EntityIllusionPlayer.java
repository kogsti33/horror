package com.horrormod.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * A ghostly, silent copy of a real player.
 * Stands motionless, watches, and vanishes when approached.
 * No name tag. No sounds. Only dread.
 */
public class EntityIllusionPlayer extends EntityLiving {

    /** The player whose skin we're mimicking */
    private String mimickedPlayerName = "";
    private int ticksAlive = 0;
    /** Lifespan: 30-90 seconds */
    private int lifespanTicks;
    private boolean vanished = false;

    public EntityIllusionPlayer(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.noClip = true;
        this.lifespanTicks = 600 + world.rand.nextInt(1200);
    }

    /** Call this right after spawning to set which player's look to copy */
    public void setMimickedPlayer(String playerName) {
        this.mimickedPlayerName = playerName;
    }

    public String getMimickedPlayerName() {
        return mimickedPlayerName;
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
        // DataWatcher index 10 holds the player name used for skin lookup
        this.dataWatcher.addObject(10, "");
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (worldObj.isRemote) return;

        ticksAlive++;

        // Sync player name to data watcher for renderer
        if (!mimickedPlayerName.equals(this.dataWatcher.getWatchableObjectString(10))) {
            this.dataWatcher.updateObject(10, mimickedPlayerName);
        }

        // Find nearest player
        EntityPlayer nearest = worldObj.getClosestPlayerToEntity(this, 64);
        if (nearest == null) {
            if (ticksAlive > lifespanTicks) vanishWithEffects();
            return;
        }

        // Always face the nearest player — eerie stare
        double dx = nearest.posX - posX;
        double dz = nearest.posZ - posZ;
        rotationYaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
        rotationPitch = 0;

        double dist = getDistanceToEntity(nearest);

        // Play a quiet whisper every ~5 seconds
        if (ticksAlive % 100 == 0 && dist < 20) {
            worldObj.playSoundEffect(posX, posY, posZ,
                "ambient.cave.cave", 0.15f,
                1.8f + worldObj.rand.nextFloat() * 0.4f);
        }

        // Vanish when player gets within 8 blocks
        if (dist < 8.0) {
            vanishWithEffects();
            return;
        }

        if (ticksAlive >= lifespanTicks) {
            vanishWithEffects();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!vanished) vanishWithEffects();
        return false;
    }

    private void vanishWithEffects() {
        if (vanished) return;
        vanished = true;

        // Smoke + static-buzz sound
        for (int i = 0; i < 25; i++) {
            worldObj.spawnParticle("smoke",
                posX + (worldObj.rand.nextDouble() - 0.5),
                posY + worldObj.rand.nextDouble() * 2,
                posZ + (worldObj.rand.nextDouble() - 0.5),
                0, 0.05, 0
            );
        }
        worldObj.playSoundEffect(posX, posY, posZ, "mob.endermen.stare", 0.6f,
            0.3f + worldObj.rand.nextFloat() * 0.3f);
        setDead();
    }

    // No name tag, no sounds
    @Override
    public boolean getAlwaysRenderNameTag() { return false; }

    @Override
    public String getCommandSenderName() { return ""; }

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
