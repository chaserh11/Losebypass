/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemBed
extends Item {
    public ItemBed() {
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }
        if (side != EnumFacing.UP) {
            return false;
        }
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        boolean flag = block.isReplaceable(worldIn, pos);
        if (!flag) {
            pos = pos.up();
        }
        int i = MathHelper.floor_double((double)(playerIn.rotationYaw * 4.0f / 360.0f) + 0.5) & 3;
        EnumFacing enumfacing = EnumFacing.getHorizontal(i);
        BlockPos blockpos = pos.offset(enumfacing);
        if (!playerIn.canPlayerEdit(pos, side, stack)) return false;
        if (!playerIn.canPlayerEdit(blockpos, side, stack)) return false;
        boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
        boolean flag2 = flag || worldIn.isAirBlock(pos);
        boolean flag3 = flag1 || worldIn.isAirBlock(blockpos);
        if (!flag2) return false;
        if (!flag3) return false;
        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down())) return false;
        if (!World.doesBlockHaveSolidTopSurface(worldIn, blockpos.down())) return false;
        IBlockState iblockstate1 = Blocks.bed.getDefaultState().withProperty(BlockBed.OCCUPIED, false).withProperty(BlockBed.FACING, enumfacing).withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);
        if (worldIn.setBlockState(pos, iblockstate1, 3)) {
            IBlockState iblockstate2 = iblockstate1.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
            worldIn.setBlockState(blockpos, iblockstate2, 3);
        }
        --stack.stackSize;
        return true;
    }
}

