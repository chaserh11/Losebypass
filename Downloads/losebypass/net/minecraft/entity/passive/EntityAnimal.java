/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityAnimal
extends EntityAgeable
implements IAnimals {
    protected Block spawnableBlock = Blocks.grass;
    private int inLove;
    private EntityPlayer playerInLove;

    public EntityAnimal(World worldIn) {
        super(worldIn);
    }

    @Override
    protected void updateAITasks() {
        if (this.getGrowingAge() != 0) {
            this.inLove = 0;
        }
        super.updateAITasks();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.getGrowingAge() != 0) {
            this.inLove = 0;
        }
        if (this.inLove <= 0) return;
        --this.inLove;
        if (this.inLove % 10 != 0) return;
        double d0 = this.rand.nextGaussian() * 0.02;
        double d1 = this.rand.nextGaussian() * 0.02;
        double d2 = this.rand.nextGaussian() * 0.02;
        this.worldObj.spawnParticle(EnumParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0f) - (double)this.width, this.posY + 0.5 + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0f) - (double)this.width, d0, d1, d2, new int[0]);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.inLove = 0;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        if (this.worldObj.getBlockState(pos.down()).getBlock() == Blocks.grass) {
            return 10.0f;
        }
        float f = this.worldObj.getLightBrightness(pos) - 0.5f;
        return f;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("InLove", this.inLove);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.inLove = tagCompund.getInteger("InLove");
    }

    @Override
    public boolean getCanSpawnHere() {
        int k;
        int j;
        int i = MathHelper.floor_double(this.posX);
        BlockPos blockpos = new BlockPos(i, j = MathHelper.floor_double(this.getEntityBoundingBox().minY), k = MathHelper.floor_double(this.posZ));
        if (this.worldObj.getBlockState(blockpos.down()).getBlock() != this.spawnableBlock) return false;
        if (this.worldObj.getLight(blockpos) <= 8) return false;
        if (!super.getCanSpawnHere()) return false;
        return true;
    }

    @Override
    public int getTalkInterval() {
        return 120;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 1 + this.worldObj.rand.nextInt(3);
    }

    public boolean isBreedingItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() != Items.wheat) return false;
        return true;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();
        if (itemstack == null) return super.interact(player);
        if (this.isBreedingItem(itemstack) && this.getGrowingAge() == 0 && this.inLove <= 0) {
            this.consumeItemFromStack(player, itemstack);
            this.setInLove(player);
            return true;
        }
        if (!this.isChild()) return super.interact(player);
        if (!this.isBreedingItem(itemstack)) return super.interact(player);
        this.consumeItemFromStack(player, itemstack);
        this.func_175501_a((int)((float)(-this.getGrowingAge() / 20) * 0.1f), true);
        return true;
    }

    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
        if (player.capabilities.isCreativeMode) return;
        --stack.stackSize;
        if (stack.stackSize > 0) return;
        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
    }

    public void setInLove(EntityPlayer player) {
        this.inLove = 600;
        this.playerInLove = player;
        this.worldObj.setEntityState(this, (byte)18);
    }

    public EntityPlayer getPlayerInLove() {
        return this.playerInLove;
    }

    public boolean isInLove() {
        if (this.inLove <= 0) return false;
        return true;
    }

    public void resetInLove() {
        this.inLove = 0;
    }

    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        }
        if (otherAnimal.getClass() != this.getClass()) {
            return false;
        }
        if (!this.isInLove()) return false;
        if (!otherAnimal.isInLove()) return false;
        return true;
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id != 18) {
            super.handleStatusUpdate(id);
            return;
        }
        int i = 0;
        while (i < 7) {
            double d0 = this.rand.nextGaussian() * 0.02;
            double d1 = this.rand.nextGaussian() * 0.02;
            double d2 = this.rand.nextGaussian() * 0.02;
            this.worldObj.spawnParticle(EnumParticleTypes.HEART, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0f) - (double)this.width, this.posY + 0.5 + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0f) - (double)this.width, d0, d1, d2, new int[0]);
            ++i;
        }
    }
}

