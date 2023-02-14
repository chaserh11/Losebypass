/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRailBase
extends Block {
    protected final boolean isPowered;

    public static boolean isRailBlock(World worldIn, BlockPos pos) {
        return BlockRailBase.isRailBlock(worldIn.getBlockState(pos));
    }

    public static boolean isRailBlock(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.rail) return true;
        if (block == Blocks.golden_rail) return true;
        if (block == Blocks.detector_rail) return true;
        if (block == Blocks.activator_rail) return true;
        return false;
    }

    protected BlockRailBase(boolean isPowered) {
        super(Material.circuits);
        this.isPowered = isPowered;
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
        this.setCreativeTab(CreativeTabs.tabTransport);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.collisionRayTrace(worldIn, pos, start, end);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        EnumRailDirection blockrailbase$enumraildirection;
        IBlockState iblockstate = worldIn.getBlockState(pos);
        EnumRailDirection enumRailDirection = blockrailbase$enumraildirection = iblockstate.getBlock() == this ? iblockstate.getValue(this.getShapeProperty()) : null;
        if (blockrailbase$enumraildirection != null && blockrailbase$enumraildirection.isAscending()) {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.625f, 1.0f);
            return;
        }
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return World.doesBlockHaveSolidTopSurface(worldIn, pos.down());
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) return;
        state = this.func_176564_a(worldIn, pos, state, true);
        if (!this.isPowered) return;
        this.onNeighborBlockChange(worldIn, pos, state, this);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
        if (worldIn.isRemote) return;
        EnumRailDirection blockrailbase$enumraildirection = state.getValue(this.getShapeProperty());
        boolean flag = false;
        if (!World.doesBlockHaveSolidTopSurface(worldIn, pos.down())) {
            flag = true;
        }
        if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_EAST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.east())) {
            flag = true;
        } else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_WEST && !World.doesBlockHaveSolidTopSurface(worldIn, pos.west())) {
            flag = true;
        } else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_NORTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.north())) {
            flag = true;
        } else if (blockrailbase$enumraildirection == EnumRailDirection.ASCENDING_SOUTH && !World.doesBlockHaveSolidTopSurface(worldIn, pos.south())) {
            flag = true;
        }
        if (flag) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
            return;
        }
        this.onNeighborChangedInternal(worldIn, pos, state, neighborBlock);
    }

    protected void onNeighborChangedInternal(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
    }

    protected IBlockState func_176564_a(World worldIn, BlockPos p_176564_2_, IBlockState p_176564_3_, boolean p_176564_4_) {
        IBlockState iBlockState;
        if (worldIn.isRemote) {
            iBlockState = p_176564_3_;
            return iBlockState;
        }
        iBlockState = new Rail(worldIn, p_176564_2_, p_176564_3_).func_180364_a(worldIn.isBlockPowered(p_176564_2_), p_176564_4_).getBlockState();
        return iBlockState;
    }

    @Override
    public int getMobilityFlag() {
        return 0;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (state.getValue(this.getShapeProperty()).isAscending()) {
            worldIn.notifyNeighborsOfStateChange(pos.up(), this);
        }
        if (!this.isPowered) return;
        worldIn.notifyNeighborsOfStateChange(pos, this);
        worldIn.notifyNeighborsOfStateChange(pos.down(), this);
    }

    public abstract IProperty<EnumRailDirection> getShapeProperty();

    public class Rail {
        private final World world;
        private final BlockPos pos;
        private final BlockRailBase block;
        private IBlockState state;
        private final boolean isPowered;
        private final List<BlockPos> field_150657_g = Lists.newArrayList();

        public Rail(World worldIn, BlockPos pos, IBlockState state) {
            this.world = worldIn;
            this.pos = pos;
            this.state = state;
            this.block = (BlockRailBase)state.getBlock();
            EnumRailDirection blockrailbase$enumraildirection = state.getValue(BlockRailBase.this.getShapeProperty());
            this.isPowered = this.block.isPowered;
            this.func_180360_a(blockrailbase$enumraildirection);
        }

        private void func_180360_a(EnumRailDirection p_180360_1_) {
            this.field_150657_g.clear();
            switch (1.$SwitchMap$net$minecraft$block$BlockRailBase$EnumRailDirection[p_180360_1_.ordinal()]) {
                case 1: {
                    this.field_150657_g.add(this.pos.north());
                    this.field_150657_g.add(this.pos.south());
                    return;
                }
                case 2: {
                    this.field_150657_g.add(this.pos.west());
                    this.field_150657_g.add(this.pos.east());
                    return;
                }
                case 3: {
                    this.field_150657_g.add(this.pos.west());
                    this.field_150657_g.add(this.pos.east().up());
                    return;
                }
                case 4: {
                    this.field_150657_g.add(this.pos.west().up());
                    this.field_150657_g.add(this.pos.east());
                    return;
                }
                case 5: {
                    this.field_150657_g.add(this.pos.north().up());
                    this.field_150657_g.add(this.pos.south());
                    return;
                }
                case 6: {
                    this.field_150657_g.add(this.pos.north());
                    this.field_150657_g.add(this.pos.south().up());
                    return;
                }
                case 7: {
                    this.field_150657_g.add(this.pos.east());
                    this.field_150657_g.add(this.pos.south());
                    return;
                }
                case 8: {
                    this.field_150657_g.add(this.pos.west());
                    this.field_150657_g.add(this.pos.south());
                    return;
                }
                case 9: {
                    this.field_150657_g.add(this.pos.west());
                    this.field_150657_g.add(this.pos.north());
                    return;
                }
                case 10: {
                    this.field_150657_g.add(this.pos.east());
                    this.field_150657_g.add(this.pos.north());
                    return;
                }
            }
        }

        private void func_150651_b() {
            int i = 0;
            while (i < this.field_150657_g.size()) {
                Rail blockrailbase$rail = this.findRailAt(this.field_150657_g.get(i));
                if (blockrailbase$rail != null && blockrailbase$rail.func_150653_a(this)) {
                    this.field_150657_g.set(i, blockrailbase$rail.pos);
                } else {
                    this.field_150657_g.remove(i--);
                }
                ++i;
            }
        }

        private boolean hasRailAt(BlockPos pos) {
            if (BlockRailBase.isRailBlock(this.world, pos)) return true;
            if (BlockRailBase.isRailBlock(this.world, pos.up())) return true;
            if (BlockRailBase.isRailBlock(this.world, pos.down())) return true;
            return false;
        }

        private Rail findRailAt(BlockPos pos) {
            IBlockState iblockstate = this.world.getBlockState(pos);
            if (BlockRailBase.isRailBlock(iblockstate)) {
                BlockRailBase blockRailBase = BlockRailBase.this;
                blockRailBase.getClass();
                return blockRailBase.new Rail(this.world, pos, iblockstate);
            }
            BlockPos lvt_2_1_ = pos.up();
            iblockstate = this.world.getBlockState(lvt_2_1_);
            if (BlockRailBase.isRailBlock(iblockstate)) {
                BlockRailBase blockRailBase = BlockRailBase.this;
                blockRailBase.getClass();
                return blockRailBase.new Rail(this.world, lvt_2_1_, iblockstate);
            }
            lvt_2_1_ = pos.down();
            iblockstate = this.world.getBlockState(lvt_2_1_);
            if (!BlockRailBase.isRailBlock(iblockstate)) return null;
            BlockRailBase blockRailBase = BlockRailBase.this;
            blockRailBase.getClass();
            Rail rail = blockRailBase.new Rail(this.world, lvt_2_1_, iblockstate);
            return rail;
        }

        private boolean func_150653_a(Rail p_150653_1_) {
            return this.func_180363_c(p_150653_1_.pos);
        }

        private boolean func_180363_c(BlockPos p_180363_1_) {
            int i = 0;
            while (i < this.field_150657_g.size()) {
                BlockPos blockpos = this.field_150657_g.get(i);
                if (blockpos.getX() == p_180363_1_.getX() && blockpos.getZ() == p_180363_1_.getZ()) {
                    return true;
                }
                ++i;
            }
            return false;
        }

        protected int countAdjacentRails() {
            int i = 0;
            Iterator<EnumFacing> iterator = EnumFacing.Plane.HORIZONTAL.iterator();
            while (iterator.hasNext()) {
                EnumFacing enumfacing = iterator.next();
                if (!this.hasRailAt(this.pos.offset(enumfacing))) continue;
                ++i;
            }
            return i;
        }

        private boolean func_150649_b(Rail rail) {
            if (this.func_150653_a(rail)) return true;
            if (this.field_150657_g.size() != 2) return true;
            return false;
        }

        private void func_150645_c(Rail p_150645_1_) {
            this.field_150657_g.add(p_150645_1_.pos);
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.func_180363_c(blockpos);
            boolean flag1 = this.func_180363_c(blockpos1);
            boolean flag2 = this.func_180363_c(blockpos2);
            boolean flag3 = this.func_180363_c(blockpos3);
            EnumRailDirection blockrailbase$enumraildirection = null;
            if (flag || flag1) {
                blockrailbase$enumraildirection = EnumRailDirection.NORTH_SOUTH;
            }
            if (flag2 || flag3) {
                blockrailbase$enumraildirection = EnumRailDirection.EAST_WEST;
            }
            if (!this.isPowered) {
                if (flag1 && flag3 && !flag && !flag2) {
                    blockrailbase$enumraildirection = EnumRailDirection.SOUTH_EAST;
                }
                if (flag1 && flag2 && !flag && !flag3) {
                    blockrailbase$enumraildirection = EnumRailDirection.SOUTH_WEST;
                }
                if (flag && flag2 && !flag1 && !flag3) {
                    blockrailbase$enumraildirection = EnumRailDirection.NORTH_WEST;
                }
                if (flag && flag3 && !flag1 && !flag2) {
                    blockrailbase$enumraildirection = EnumRailDirection.NORTH_EAST;
                }
            }
            if (blockrailbase$enumraildirection == EnumRailDirection.NORTH_SOUTH) {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos1.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (blockrailbase$enumraildirection == EnumRailDirection.EAST_WEST) {
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (blockrailbase$enumraildirection == null) {
                blockrailbase$enumraildirection = EnumRailDirection.NORTH_SOUTH;
            }
            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
            this.world.setBlockState(this.pos, this.state, 3);
        }

        private boolean func_180361_d(BlockPos p_180361_1_) {
            Rail blockrailbase$rail = this.findRailAt(p_180361_1_);
            if (blockrailbase$rail == null) {
                return false;
            }
            blockrailbase$rail.func_150651_b();
            return blockrailbase$rail.func_150649_b(this);
        }

        public Rail func_180364_a(boolean p_180364_1_, boolean p_180364_2_) {
            BlockPos blockpos = this.pos.north();
            BlockPos blockpos1 = this.pos.south();
            BlockPos blockpos2 = this.pos.west();
            BlockPos blockpos3 = this.pos.east();
            boolean flag = this.func_180361_d(blockpos);
            boolean flag1 = this.func_180361_d(blockpos1);
            boolean flag2 = this.func_180361_d(blockpos2);
            boolean flag3 = this.func_180361_d(blockpos3);
            EnumRailDirection blockrailbase$enumraildirection = null;
            if ((flag || flag1) && !flag2 && !flag3) {
                blockrailbase$enumraildirection = EnumRailDirection.NORTH_SOUTH;
            }
            if ((flag2 || flag3) && !flag && !flag1) {
                blockrailbase$enumraildirection = EnumRailDirection.EAST_WEST;
            }
            if (!this.isPowered) {
                if (flag1 && flag3 && !flag && !flag2) {
                    blockrailbase$enumraildirection = EnumRailDirection.SOUTH_EAST;
                }
                if (flag1 && flag2 && !flag && !flag3) {
                    blockrailbase$enumraildirection = EnumRailDirection.SOUTH_WEST;
                }
                if (flag && flag2 && !flag1 && !flag3) {
                    blockrailbase$enumraildirection = EnumRailDirection.NORTH_WEST;
                }
                if (flag && flag3 && !flag1 && !flag2) {
                    blockrailbase$enumraildirection = EnumRailDirection.NORTH_EAST;
                }
            }
            if (blockrailbase$enumraildirection == null) {
                if (flag || flag1) {
                    blockrailbase$enumraildirection = EnumRailDirection.NORTH_SOUTH;
                }
                if (flag2 || flag3) {
                    blockrailbase$enumraildirection = EnumRailDirection.EAST_WEST;
                }
                if (!this.isPowered) {
                    if (p_180364_1_) {
                        if (flag1 && flag3) {
                            blockrailbase$enumraildirection = EnumRailDirection.SOUTH_EAST;
                        }
                        if (flag2 && flag1) {
                            blockrailbase$enumraildirection = EnumRailDirection.SOUTH_WEST;
                        }
                        if (flag3 && flag) {
                            blockrailbase$enumraildirection = EnumRailDirection.NORTH_EAST;
                        }
                        if (flag && flag2) {
                            blockrailbase$enumraildirection = EnumRailDirection.NORTH_WEST;
                        }
                    } else {
                        if (flag && flag2) {
                            blockrailbase$enumraildirection = EnumRailDirection.NORTH_WEST;
                        }
                        if (flag3 && flag) {
                            blockrailbase$enumraildirection = EnumRailDirection.NORTH_EAST;
                        }
                        if (flag2 && flag1) {
                            blockrailbase$enumraildirection = EnumRailDirection.SOUTH_WEST;
                        }
                        if (flag1 && flag3) {
                            blockrailbase$enumraildirection = EnumRailDirection.SOUTH_EAST;
                        }
                    }
                }
            }
            if (blockrailbase$enumraildirection == EnumRailDirection.NORTH_SOUTH) {
                if (BlockRailBase.isRailBlock(this.world, blockpos.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_NORTH;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos1.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_SOUTH;
                }
            }
            if (blockrailbase$enumraildirection == EnumRailDirection.EAST_WEST) {
                if (BlockRailBase.isRailBlock(this.world, blockpos3.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_EAST;
                }
                if (BlockRailBase.isRailBlock(this.world, blockpos2.up())) {
                    blockrailbase$enumraildirection = EnumRailDirection.ASCENDING_WEST;
                }
            }
            if (blockrailbase$enumraildirection == null) {
                blockrailbase$enumraildirection = EnumRailDirection.NORTH_SOUTH;
            }
            this.func_180360_a(blockrailbase$enumraildirection);
            this.state = this.state.withProperty(this.block.getShapeProperty(), blockrailbase$enumraildirection);
            if (!p_180364_2_) {
                if (this.world.getBlockState(this.pos) == this.state) return this;
            }
            this.world.setBlockState(this.pos, this.state, 3);
            int i = 0;
            while (i < this.field_150657_g.size()) {
                Rail blockrailbase$rail = this.findRailAt(this.field_150657_g.get(i));
                if (blockrailbase$rail != null) {
                    blockrailbase$rail.func_150651_b();
                    if (blockrailbase$rail.func_150649_b(this)) {
                        blockrailbase$rail.func_150645_c(this);
                    }
                }
                ++i;
            }
            return this;
        }

        public IBlockState getBlockState() {
            return this.state;
        }
    }

    public static enum EnumRailDirection implements IStringSerializable
    {
        NORTH_SOUTH(0, "north_south"),
        EAST_WEST(1, "east_west"),
        ASCENDING_EAST(2, "ascending_east"),
        ASCENDING_WEST(3, "ascending_west"),
        ASCENDING_NORTH(4, "ascending_north"),
        ASCENDING_SOUTH(5, "ascending_south"),
        SOUTH_EAST(6, "south_east"),
        SOUTH_WEST(7, "south_west"),
        NORTH_WEST(8, "north_west"),
        NORTH_EAST(9, "north_east");

        private static final EnumRailDirection[] META_LOOKUP;
        private final int meta;
        private final String name;

        private EnumRailDirection(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public boolean isAscending() {
            if (this == ASCENDING_NORTH) return true;
            if (this == ASCENDING_EAST) return true;
            if (this == ASCENDING_SOUTH) return true;
            if (this == ASCENDING_WEST) return true;
            return false;
        }

        public static EnumRailDirection byMetadata(int meta) {
            if (meta >= 0) {
                if (meta < META_LOOKUP.length) return META_LOOKUP[meta];
            }
            meta = 0;
            return META_LOOKUP[meta];
        }

        @Override
        public String getName() {
            return this.name;
        }

        static {
            META_LOOKUP = new EnumRailDirection[EnumRailDirection.values().length];
            EnumRailDirection[] enumRailDirectionArray = EnumRailDirection.values();
            int n = enumRailDirectionArray.length;
            int n2 = 0;
            while (n2 < n) {
                EnumRailDirection blockrailbase$enumraildirection;
                EnumRailDirection.META_LOOKUP[blockrailbase$enumraildirection.getMetadata()] = blockrailbase$enumraildirection = enumRailDirectionArray[n2];
                ++n2;
            }
        }
    }
}

