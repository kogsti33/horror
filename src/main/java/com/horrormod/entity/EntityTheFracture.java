package com.horrormod.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * THE FRACTURE — the glitching, shard-like boss entity.
 * Breaks blocks to reach the player. Plays horrific sounds.
 * Does not die easily. Radiates wrongness.
 */
public class EntityTheFracture extends EntityLiving {

    private int soundTimer    = 0;
    private int blockBreakTimer = 0;

    public EntityTheFracture(World world) {
        super(world);
        this.isImmuneToFire = true;
        this.setSize(1.0f, 2.5f);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth)
            .setBaseValue(200.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed)
            .setBaseValue(0.28);
        this.getEntityAttribute(SharedMonsterAttributes.followRange)
            .setBaseValue(64.0);
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance)
            .setBaseValue(1.0); // Cannot be knocked back
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    protected void applyAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9, 32));
        this.tasks.addTask(3, new EntityAIWander(this, 0.6));
        this.tasks.addTask(4, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget(
            this, EntityPlayer.class, 0, true));
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (worldObj.isRemote) return;

        soundTimer++;
        blockBreakTimer++;

        // Play distorted sounds every 2-5 seconds
        if (soundTimer >= 40 + worldObj.rand.nextInt(60)) {
            soundTimer = 0;
            playFractureSound();
        }

        // Attempt to break blocks toward the player every 1 second
        if (blockBreakTimer >= 20) {
            blockBreakTimer = 0;
            tryBreakBlocksTowardPlayer();
        }

        // Glitch-teleport: rarely teleport short distance (3%)
        if (worldObj.rand.nextInt(100) < 3) {
            double tx = posX + (worldObj.rand.nextDouble() - 0.5) * 8;
            double tz = posZ + (worldObj.rand.nextDouble() - 0.5) * 8;
            spawnFractureParticles();
            this.setPosition(tx, posY, tz);
            spawnFractureParticles();
        }
    }

    private void playFractureSound() {
        String[] sounds = {
            "mob.blaze.breath",
            "ambient.cave.cave",
            "mob.endermen.scream",
            "mob.ghast.scream"
        };
        String sound = sounds[worldObj.rand.nextInt(sounds.length)];
        worldObj.playSoundEffect(posX, posY, posZ, sound,
            1.5f, 0.3f + worldObj.rand.nextFloat() * 0.4f);
    }

    private void tryBreakBlocksTowardPlayer() {
        EntityPlayer player = worldObj.getClosestPlayerToEntity(this, 32);
        if (player == null) return;

        // Move one step toward player and break any non-air block there
        double dx = player.posX - posX;
        double dz = player.posZ - posZ;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 2) return;

        dx /= len; dz /= len;

        // Check up to 4 blocks ahead
        for (int step = 1; step <= 4; step++) {
            int bx = MathHelper.floor_double(posX + dx * step);
            int by = MathHelper.floor_double(posY + 0.5);
            int bz = MathHelper.floor_double(posZ + dz * step);

            Block block = worldObj.getBlock(bx, by, bz);
            Block blockAbove = worldObj.getBlock(bx, by + 1, bz);

            // Don't break bedrock or air
            if (block != Blocks.air && block != Blocks.bedrock) {
                worldObj.setBlockToAir(bx, by, bz);
                worldObj.playSoundEffect(bx, by, bz, "dig.stone", 1.0f,
                    0.5f + worldObj.rand.nextFloat() * 0.5f);
            }
            if (blockAbove != Blocks.air && blockAbove != Blocks.bedrock) {
                worldObj.setBlockToAir(bx, by + 1, bz);
            }
        }
    }

    private void spawnFractureParticles() {
        for (int i = 0; i < 20; i++) {
            worldObj.spawnParticle("portal",
                posX + (worldObj.rand.nextDouble() - 0.5) * 2,
                posY + worldObj.rand.nextDouble() * 2.5,
                posZ + (worldObj.rand.nextDouble() - 0.5) * 2,
                0, 0.1, 0
            );
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // Fracture plays a scream when hit
        worldObj.playSoundEffect(posX, posY, posZ, "mob.endermen.hit", 1.0f,
            0.5f + worldObj.rand.nextFloat() * 0.3f);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void onDeathUpdate() {
        super.onDeathUpdate();
        // Massive portal burst on death
        if (deathTime == 1) {
            for (int i = 0; i < 80; i++) {
                worldObj.spawnParticle("portal",
                    posX + (worldObj.rand.nextDouble() - 0.5) * 4,
                    posY + worldObj.rand.nextDouble() * 2.5,
                    posZ + (worldObj.rand.nextDouble() - 0.5) * 4,
                    (worldObj.rand.nextDouble() - 0.5),
                    worldObj.rand.nextDouble(),
                    (worldObj.rand.nextDouble() - 0.5)
                );
            }
            worldObj.playSoundEffect(posX, posY, posZ, "mob.endermen.portal", 2.0f, 0.5f);
        }
    }

    @Override
    protected boolean canDespawn() { return false; }

    @Override
    protected String getLivingSound()  { return "mob.endermen.idle"; }
    @Override
    protected String getHurtSound()    { return "mob.endermen.hit"; }
    @Override
    protected String getDeathSound()   { return "mob.endermen.death"; }

    @Override
    protected int getExperiencePoints(EntityPlayer player) { return 100; }
}
