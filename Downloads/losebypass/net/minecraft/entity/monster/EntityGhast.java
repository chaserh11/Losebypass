/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityGhast
extends EntityFlying
implements IMob {
    private int explosionStrength = 1;

    public EntityGhast(World worldIn) {
        super(worldIn);
        this.setSize(4.0f, 4.0f);
        this.isImmuneToFire = true;
        this.experienceValue = 5;
        this.moveHelper = new GhastMoveHelper(this);
        this.tasks.addTask(5, new AIRandomFly(this));
        this.tasks.addTask(7, new AILookAround(this));
        this.tasks.addTask(7, new AIFireballAttack(this));
        this.targetTasks.addTask(1, new EntityAIFindEntityNearestPlayer(this));
    }

    public boolean isAttacking() {
        if (this.dataWatcher.getWatchableObjectByte(16) == 0) return false;
        return true;
    }

    public void setAttacking(boolean p_175454_1_) {
        this.dataWatcher.updateObject(16, (byte)(p_175454_1_ ? 1 : 0));
    }

    public int getFireballStrength() {
        return this.explosionStrength;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.worldObj.isRemote) return;
        if (this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL) return;
        this.setDead();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (!"fireball".equals(source.getDamageType())) return super.attackEntityFrom(source, amount);
        if (!(source.getEntity() instanceof EntityPlayer)) return super.attackEntityFrom(source, amount);
        super.attackEntityFrom(source, 1000.0f);
        ((EntityPlayer)source.getEntity()).triggerAchievement(AchievementList.ghast);
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (byte)0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(100.0);
    }

    @Override
    protected String getLivingSound() {
        return "mob.ghast.moan";
    }

    @Override
    protected String getHurtSound() {
        return "mob.ghast.scream";
    }

    @Override
    protected String getDeathSound() {
        return "mob.ghast.death";
    }

    @Override
    protected Item getDropItem() {
        return Items.gunpowder;
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
        int i = this.rand.nextInt(2) + this.rand.nextInt(1 + p_70628_2_);
        for (int j = 0; j < i; ++j) {
            this.dropItem(Items.ghast_tear, 1);
        }
        i = this.rand.nextInt(3) + this.rand.nextInt(1 + p_70628_2_);
        int k = 0;
        while (k < i) {
            this.dropItem(Items.gunpowder, 1);
            ++k;
        }
    }

    @Override
    protected float getSoundVolume() {
        return 10.0f;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (this.rand.nextInt(20) != 0) return false;
        if (!super.getCanSpawnHere()) return false;
        if (this.worldObj.getDifficulty() == EnumDifficulty.PEACEFUL) return false;
        return true;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("ExplosionPower", this.explosionStrength);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        if (!tagCompund.hasKey("ExplosionPower", 99)) return;
        this.explosionStrength = tagCompund.getInteger("ExplosionPower");
    }

    @Override
    public float getEyeHeight() {
        return 2.6f;
    }

    static class GhastMoveHelper
    extends EntityMoveHelper {
        private EntityGhast parentEntity;
        private int courseChangeCooldown;

        public GhastMoveHelper(EntityGhast p_i45838_1_) {
            super(p_i45838_1_);
            this.parentEntity = p_i45838_1_;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (!this.update) return;
            double d0 = this.posX - this.parentEntity.posX;
            double d1 = this.posY - this.parentEntity.posY;
            double d2 = this.posZ - this.parentEntity.posZ;
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (this.courseChangeCooldown-- > 0) return;
            this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
            if (this.isNotColliding(this.posX, this.posY, this.posZ, d3 = (double)MathHelper.sqrt_double(d3))) {
                this.parentEntity.motionX += d0 / d3 * 0.1;
                this.parentEntity.motionY += d1 / d3 * 0.1;
                this.parentEntity.motionZ += d2 / d3 * 0.1;
                return;
            }
            this.update = false;
        }

        private boolean isNotColliding(double p_179926_1_, double p_179926_3_, double p_179926_5_, double p_179926_7_) {
            double d0 = (p_179926_1_ - this.parentEntity.posX) / p_179926_7_;
            double d1 = (p_179926_3_ - this.parentEntity.posY) / p_179926_7_;
            double d2 = (p_179926_5_ - this.parentEntity.posZ) / p_179926_7_;
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();
            int i = 1;
            while ((double)i < p_179926_7_) {
                if (!this.parentEntity.worldObj.getCollidingBoundingBoxes(this.parentEntity, axisalignedbb = axisalignedbb.offset(d0, d1, d2)).isEmpty()) {
                    return false;
                }
                ++i;
            }
            return true;
        }
    }

    static class AIRandomFly
    extends EntityAIBase {
        private EntityGhast parentEntity;

        public AIRandomFly(EntityGhast p_i45836_1_) {
            this.parentEntity = p_i45836_1_;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            double d2;
            double d1;
            EntityMoveHelper entitymovehelper = this.parentEntity.getMoveHelper();
            if (!entitymovehelper.isUpdating()) {
                return true;
            }
            double d0 = entitymovehelper.getX() - this.parentEntity.posX;
            double d3 = d0 * d0 + (d1 = entitymovehelper.getY() - this.parentEntity.posY) * d1 + (d2 = entitymovehelper.getZ() - this.parentEntity.posZ) * d2;
            if (d3 < 1.0) return true;
            if (d3 > 3600.0) return true;
            return false;
        }

        @Override
        public boolean continueExecuting() {
            return false;
        }

        @Override
        public void startExecuting() {
            Random random = this.parentEntity.getRNG();
            double d0 = this.parentEntity.posX + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double d1 = this.parentEntity.posY + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double d2 = this.parentEntity.posZ + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            this.parentEntity.getMoveHelper().setMoveTo(d0, d1, d2, 1.0);
        }
    }

    static class AILookAround
    extends EntityAIBase {
        private EntityGhast parentEntity;

        public AILookAround(EntityGhast p_i45839_1_) {
            this.parentEntity = p_i45839_1_;
            this.setMutexBits(2);
        }

        @Override
        public boolean shouldExecute() {
            return true;
        }

        @Override
        public void updateTask() {
            if (this.parentEntity.getAttackTarget() == null) {
                this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw = -((float)MathHelper.func_181159_b(this.parentEntity.motionX, this.parentEntity.motionZ)) * 180.0f / (float)Math.PI;
                return;
            }
            EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
            double d0 = 64.0;
            if (!(entitylivingbase.getDistanceSqToEntity(this.parentEntity) < d0 * d0)) return;
            double d1 = entitylivingbase.posX - this.parentEntity.posX;
            double d2 = entitylivingbase.posZ - this.parentEntity.posZ;
            this.parentEntity.renderYawOffset = this.parentEntity.rotationYaw = -((float)MathHelper.func_181159_b(d1, d2)) * 180.0f / (float)Math.PI;
        }
    }

    static class AIFireballAttack
    extends EntityAIBase {
        private EntityGhast parentEntity;
        public int attackTimer;

        public AIFireballAttack(EntityGhast p_i45837_1_) {
            this.parentEntity = p_i45837_1_;
        }

        @Override
        public boolean shouldExecute() {
            if (this.parentEntity.getAttackTarget() == null) return false;
            return true;
        }

        @Override
        public void startExecuting() {
            this.attackTimer = 0;
        }

        @Override
        public void resetTask() {
            this.parentEntity.setAttacking(false);
        }

        @Override
        public void updateTask() {
            EntityLivingBase entitylivingbase = this.parentEntity.getAttackTarget();
            double d0 = 64.0;
            if (entitylivingbase.getDistanceSqToEntity(this.parentEntity) < d0 * d0 && this.parentEntity.canEntityBeSeen(entitylivingbase)) {
                World world = this.parentEntity.worldObj;
                ++this.attackTimer;
                if (this.attackTimer == 10) {
                    world.playAuxSFXAtEntity(null, 1007, new BlockPos(this.parentEntity), 0);
                }
                if (this.attackTimer == 20) {
                    double d1 = 4.0;
                    Vec3 vec3 = this.parentEntity.getLook(1.0f);
                    double d2 = entitylivingbase.posX - (this.parentEntity.posX + vec3.xCoord * d1);
                    double d3 = entitylivingbase.getEntityBoundingBox().minY + (double)(entitylivingbase.height / 2.0f) - (0.5 + this.parentEntity.posY + (double)(this.parentEntity.height / 2.0f));
                    double d4 = entitylivingbase.posZ - (this.parentEntity.posZ + vec3.zCoord * d1);
                    world.playAuxSFXAtEntity(null, 1008, new BlockPos(this.parentEntity), 0);
                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.parentEntity, d2, d3, d4);
                    entitylargefireball.explosionPower = this.parentEntity.getFireballStrength();
                    entitylargefireball.posX = this.parentEntity.posX + vec3.xCoord * d1;
                    entitylargefireball.posY = this.parentEntity.posY + (double)(this.parentEntity.height / 2.0f) + 0.5;
                    entitylargefireball.posZ = this.parentEntity.posZ + vec3.zCoord * d1;
                    world.spawnEntityInWorld(entitylargefireball);
                    this.attackTimer = -40;
                }
            } else if (this.attackTimer > 0) {
                --this.attackTimer;
            }
            this.parentEntity.setAttacking(this.attackTimer > 10);
        }
    }
}
