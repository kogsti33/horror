package com.horrormod.entity;

import com.horrormod.HorrorMod;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityTheWatcher extends EntityLiving {

    /** Ticks the watcher has been alive */
    private int ticksAlive = 0;
    /** How many ticks before it disappears (10-15 seconds) */
    private int lifespanTicks;
    /** Whether the watcher has played its vanish effect */
    private boolean vanished = false;

    public EntityTheWatcher(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.noClip = false;
        // Lifespan 10–15 seconds
        this.lifespanTicks = 200 + world.rand.nextInt(100);
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

        // Look at nearest player
        EntityPlayer nearest = worldObj.getClosestPlayerToEntity(this, 64);
        if (nearest != null) {
            // Face the player
            double dx = nearest.posX - posX;
            double dz = nearest.posZ - posZ;
            rotationYaw = (float)(Math.toDegrees(Math.atan2(-dx, dz)));
            rotationPitch = 0;

            double dist = getDistanceToEntity(nearest);

            // Disappear when player gets too close (< 5 blocks)
            if (dist < 5.0 && !vanished) {
                vanishWithEffects();
                return;
            }
        }

        // Disappear after lifespan
        if (ticksAlive >= lifespanTicks && !vanished) {
            vanishWithEffects();
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // Disappear when hit
        if (!vanished) {
            vanishWithEffects();
        }
        return false;
    }

    private void vanishWithEffects() {
        vanished = true;
        // Spawn smoke particles on server side (clients will see via packet)
        for (int i = 0; i < 20; i++) {
            worldObj.spawnParticle("smoke",
                posX + (worldObj.rand.nextDouble() - 0.5),
                posY + worldObj.rand.nextDouble() * 2,
                posZ + (worldObj.rand.nextDouble() - 0.5),
                0, 0.1, 0
            );
        }
        worldObj.playSoundEffect(posX, posY, posZ, "mob.blaze.death", 0.5f,
            0.5f + worldObj.rand.nextFloat() * 0.5f);
        setDead();
    }

    @Override
    protected boolean canDespawn() {
        return false; // Managed manually
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected String getLivingSound()  { return null; }
    @Override
    protected String getHurtSound()    { return null; }
    @Override
    protected String getDeathSound()   { return null; }
    @Override
    protected int getExperiencePoints(EntityPlayer player) { return 0; }
}
