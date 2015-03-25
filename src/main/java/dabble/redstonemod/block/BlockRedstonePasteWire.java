package dabble.redstonemod.block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dabble.redstonemod.init.ModBlocks;
import dabble.redstonemod.init.ModItems;

public class BlockRedstonePasteWire extends Block {
	public static final PropertyEnum DOWN = PropertyEnum.create("down", BlockRedstonePasteWire.VerticalAttachPosition.class);
	public static final PropertyEnum EAST = PropertyEnum.create("east", BlockRedstonePasteWire.AttachPosition.class);
	public static final PropertyEnum NORTH = PropertyEnum.create("north", BlockRedstonePasteWire.AttachPosition.class);
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	public static final PropertyEnum SOUTH = PropertyEnum.create("south", BlockRedstonePasteWire.AttachPosition.class);
	public static final PropertyEnum UP = PropertyEnum.create("up", BlockRedstonePasteWire.VerticalAttachPosition.class);
	public static final PropertyEnum WEST = PropertyEnum.create("west", BlockRedstonePasteWire.AttachPosition.class);
	private boolean canProvidePower = true;
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();

	public BlockRedstonePasteWire() {
		super(Material.rock);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(DOWN, BlockRedstonePasteWire.VerticalAttachPosition.NONE)
				.withProperty(EAST, BlockRedstonePasteWire.AttachPosition.NONE)
				.withProperty(NORTH, BlockRedstonePasteWire.AttachPosition.NONE)
				.withProperty(POWER, Integer.valueOf(0))
				.withProperty(SOUTH, BlockRedstonePasteWire.AttachPosition.NONE)
				.withProperty(UP, BlockRedstonePasteWire.VerticalAttachPosition.NONE)
				.withProperty(WEST, BlockRedstonePasteWire.AttachPosition.NONE));
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = state.withProperty(DOWN, this.getVerticalAttachPosition(worldIn, pos, EnumFacing.DOWN));
		state = state.withProperty(EAST, this.getAttachPosition(worldIn, pos, EnumFacing.EAST));
		state = state.withProperty(NORTH, this.getAttachPosition(worldIn, pos, EnumFacing.NORTH));
		state = state.withProperty(SOUTH, this.getAttachPosition(worldIn, pos, EnumFacing.SOUTH));
		state = state.withProperty(UP, this.getVerticalAttachPosition(worldIn, pos, EnumFacing.UP));
		state = state.withProperty(WEST, this.getAttachPosition(worldIn, pos, EnumFacing.WEST));

		return state;
	}

	private BlockRedstonePasteWire.AttachPosition getAttachPosition(IBlockAccess worldIn, BlockPos pos, EnumFacing direction) {

		BlockPos blockPos = pos.offset(direction);
		Block block = worldIn.getBlockState(blockPos).getBlock();

		if (!canRedstoneConnect(worldIn, blockPos, direction) && (block.isSolidFullCube() || !canRedstoneConnect(worldIn, blockPos.down(), null))) {
			Block block1 = worldIn.getBlockState(pos.up()).getBlock();
			return (!block1.isSolidFullCube() && block.isSolidFullCube() && canRedstoneConnect(worldIn, blockPos.up(), null)) ?
					BlockRedstonePasteWire.AttachPosition.DIAGONAL :
					((worldIn.getBlockState(blockPos).getBlock().isSolidFullCube()) ? BlockRedstonePasteWire.AttachPosition.WALL : BlockRedstonePasteWire.AttachPosition.NONE);
		} else {
			return BlockRedstonePasteWire.AttachPosition.SIDE;
		}
	}

	private BlockRedstonePasteWire.VerticalAttachPosition getVerticalAttachPosition(IBlockAccess worldIn, BlockPos pos, EnumFacing direction) {

		if (direction == EnumFacing.UP && worldIn.getBlockState(pos.up()).getBlock().isSolidFullCube())
			return VerticalAttachPosition.CEILING;

		return canRedstoneConnect(worldIn, pos.offset(direction), null) ? VerticalAttachPosition.SIDE : VerticalAttachPosition.NONE;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

	// @Override
	// @SideOnly(Side.CLIENT)
	// public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
	// return new AxisAlignedBB((double) pos.getX() + 1, (double) pos.getY(), (double) pos.getZ(), (double) pos.getX() + 1 - 0.0625, (double) pos.getY() + 1, (double)
	// pos.getZ() + 1);
	// }

	public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos)
	{
		// float f = 0.1875F;
		//
		// switch (BlockLever.SwitchEnumFacing.ORIENTATION_LOOKUP[((BlockLever.EnumOrientation)worldIn.getBlockState(pos).getValue(FACING)).ordinal()])
		// {
		// case 1:
		// this.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
		// break;
		// case 2:
		// this.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
		// break;
		// case 3:
		// this.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
		// break;
		// case 4:
		// this.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
		// break;
		// case 5:
		// case 6:
		// f = 0.25F;
		// this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
		// break;
		// case 7:
		// case 8:
		// f = 0.25F;
		// this.setBlockBounds(0.5F - f, 0.4F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
		// }
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		return iblockstate.getBlock() != this ? super.colorMultiplier(worldIn, pos, renderPass) : this.colorMultiplier(((Integer) iblockstate.getValue(POWER)).intValue());
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) || worldIn.getBlockState(pos.down()).getBlock() == Blocks.glowstone ||
				doesBlockHaveSolidBottomSurface(worldIn, pos.up()) ||
				worldIn.isSideSolid(pos.west(), EnumFacing.EAST) ||
				worldIn.isSideSolid(pos.east(), EnumFacing.WEST) ||
				worldIn.isSideSolid(pos.north(), EnumFacing.SOUTH) ||
				worldIn.isSideSolid(pos.south(), EnumFacing.NORTH);
	}

	private static boolean doesBlockHaveSolidBottomSurface(IBlockAccess worldIn, BlockPos pos) {
		Block block = worldIn.getBlockState(pos).getBlock();
		return block.isSideSolid(worldIn, pos, EnumFacing.DOWN);
	}

	// public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	// {
	// IBlockState state = this.getActualState(world.getBlockState(pos), world, pos);
	//
	// if (this instanceof BlockSlab)
	// {
	// return isFullBlock() || (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP && side == EnumFacing.UP);
	// }
	// else if (this instanceof BlockFarmland)
	// {
	// return (side != EnumFacing.DOWN && side != EnumFacing.UP);
	// }
	// else if (this instanceof BlockStairs)
	// {
	// boolean flipped = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;
	// BlockStairs.EnumShape shape = (BlockStairs.EnumShape)state.getValue(BlockStairs.SHAPE);
	// EnumFacing facing = (EnumFacing)state.getValue(BlockStairs.FACING);
	// if (side == EnumFacing.UP) return flipped;
	// if (facing == side) return true;
	// if (flipped)
	// {
	// if (shape == BlockStairs.EnumShape.INNER_LEFT ) return side == facing.rotateYCCW();
	// if (shape == BlockStairs.EnumShape.INNER_RIGHT) return side == facing.rotateY();
	// }
	// else
	// {
	// if (shape == BlockStairs.EnumShape.INNER_LEFT ) return side == facing.rotateY();
	// if (shape == BlockStairs.EnumShape.INNER_RIGHT) return side == facing.rotateYCCW();
	// }
	// return false;
	// }
	// else if (this instanceof BlockSnow)
	// {
	// return ((Integer)state.getValue(BlockSnow.LAYERS)) >= 8;
	// }
	// else if (this instanceof BlockHopper && side == EnumFacing.UP)
	// {
	// return true;
	// }
	// else if (this instanceof BlockCompressedPowered)
	// {
	// return true;
	// }
	// return isNormalCube(world, pos);
	// }

	private IBlockState updateSurroundingRedstone(World worldIn, BlockPos pos, IBlockState state) {
		state = this.calculateCurrentChanges(worldIn, pos, pos, state);
		ArrayList<BlockPos> arraylist = Lists.newArrayList(this.blocksNeedingUpdate);
		this.blocksNeedingUpdate.clear();
		Iterator<BlockPos> iterator = arraylist.iterator();

		while (iterator.hasNext()) {
			BlockPos blockpos1 = (BlockPos) iterator.next();
			worldIn.notifyNeighborsOfStateChange(blockpos1, this);
		}

		return state;
	}

	private IBlockState calculateCurrentChanges(World worldIn, BlockPos pos1, BlockPos pos2, IBlockState state) {
		IBlockState iblockstate1 = state;
		int currentPowerLevel = ((Integer) state.getValue(POWER)).intValue();
		byte strength = 0;
		int newPowerLevel = this.getMaxCurrentStrength(worldIn, pos2, strength);
		this.canProvidePower = false;
		int indirectPowerLevel = worldIn.isBlockIndirectlyGettingPowered(pos1);
		this.canProvidePower = true;

		if (indirectPowerLevel > 0 && indirectPowerLevel > newPowerLevel - 1) {
			newPowerLevel = indirectPowerLevel;
		}

		int k = 0;
		Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

		while (iterator.hasNext()) {
			EnumFacing enumfacing = (EnumFacing) iterator.next();
			BlockPos blockpos2 = pos1.offset(enumfacing);
			boolean flag = blockpos2.getX() != pos2.getX() || blockpos2.getZ() != pos2.getZ();

			if (flag) {
				k = this.getMaxCurrentStrength(worldIn, blockpos2, k);
			}

			if (worldIn.getBlockState(blockpos2).getBlock().isNormalCube() && !worldIn.getBlockState(pos1.up()).getBlock().isNormalCube()) {

				if (flag && pos1.getY() >= pos2.getY()) {
					k = this.getMaxCurrentStrength(worldIn, blockpos2.up(), k);
				}
			} else if (!worldIn.getBlockState(blockpos2).getBlock().isNormalCube() && flag && pos1.getY() <= pos2.getY()) {
				k = this.getMaxCurrentStrength(worldIn, blockpos2.down(), k);
			}
		}

		if (k > newPowerLevel) {
			newPowerLevel = k - 1;
		} else if (newPowerLevel > 0) {
			--newPowerLevel;
		} else {
			newPowerLevel = 0;
		}

		if (indirectPowerLevel > newPowerLevel - 1) {
			newPowerLevel = indirectPowerLevel;
		}

		if (currentPowerLevel != newPowerLevel) {
			state = state.withProperty(POWER, Integer.valueOf(newPowerLevel));

			if (worldIn.getBlockState(pos1) == iblockstate1) {
				worldIn.setBlockState(pos1, state, 2);
			}

			this.blocksNeedingUpdate.add(pos1);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i1 = aenumfacing.length;

			for (int i = 0; i < i1; ++i) {
				EnumFacing enumfacing1 = aenumfacing[i];
				this.blocksNeedingUpdate.add(pos1.offset(enumfacing1));
			}
		}

		return state;
	}

	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {

		if (worldIn.getBlockState(pos).getBlock() == this) {
			worldIn.notifyNeighborsOfStateChange(pos, this);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}
		}
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {

		if (!worldIn.isRemote) {
			this.updateSurroundingRedstone(worldIn, pos, state);
			Iterator<?> iterator = EnumFacing.Plane.VERTICAL.iterator();
			EnumFacing enumfacing;

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(enumfacing));
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				BlockPos blockpos1 = pos.offset(enumfacing);

				if (worldIn.getBlockState(blockpos1).getBlock().isNormalCube()) {
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.up());
				} else {
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.down());
				}
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);

		if (!worldIn.isRemote) {
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (int j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}

			this.updateSurroundingRedstone(worldIn, pos, state);
			Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();
			EnumFacing enumfacing1;

			while (iterator.hasNext()) {
				enumfacing1 = (EnumFacing) iterator.next();
				this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(enumfacing1));
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing1 = (EnumFacing) iterator.next();
				BlockPos blockpos1 = pos.offset(enumfacing1);

				if (worldIn.getBlockState(blockpos1).getBlock().isNormalCube()) {
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.up());
				} else {
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.down());
				}
			}
		}
	}

	private int getMaxCurrentStrength(World worldIn, BlockPos pos, int strength) {

		if (worldIn.getBlockState(pos).getBlock() != this) {
			return strength;
		} else {
			int j = ((Integer) worldIn.getBlockState(pos).getValue(POWER)).intValue();
			return j > strength ? j : strength;
		}
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {

		if (!worldIn.isRemote) {

			if (this.canPlaceBlockAt(worldIn, pos)) {
				this.updateSurroundingRedstone(worldIn, pos, state);
			} else {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.redstone_paste;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
		return !this.canProvidePower ? 0 : this.isProvidingWeakPower(worldIn, pos, state, side);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {

		if (!this.canProvidePower) {
			return 0;
		} else {
			int i = ((Integer) state.getValue(POWER)).intValue();

			if (i == 0) {
				return 0;
			} else if (side == EnumFacing.UP) {
				return i;
			} else {
				EnumSet<EnumFacing> enumset = EnumSet.noneOf(EnumFacing.class);
				Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

				while (iterator.hasNext()) {
					EnumFacing enumfacing1 = (EnumFacing) iterator.next();

					if (this.func_176339_d(worldIn, pos, enumfacing1)) {
						enumset.add(enumfacing1);
					}
				}

				if (side.getAxis().isHorizontal() && enumset.isEmpty()) {
					return i;
				} else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY())) {
					return i;
				} else {
					return 0;
				}
			}
		}
	}

	private boolean func_176339_d(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		BlockPos blockpos1 = pos.offset(side);
		IBlockState iblockstate = worldIn.getBlockState(blockpos1);
		Block block = iblockstate.getBlock();
		boolean flag = block.isNormalCube();
		boolean flag1 = worldIn.getBlockState(pos.up()).getBlock().isNormalCube();
		return !flag1 && flag && canRedstoneConnect(worldIn, blockpos1.up(), null) ? true : (canRedstoneConnect(worldIn, blockpos1, side) ? true : (block == Blocks.powered_repeater &&
				iblockstate.getValue(BlockRedstoneDiode.FACING) == side ? true : !flag && canRedstoneConnect(worldIn, blockpos1.down(), null)));
	}

	protected static boolean canRedstoneConnect(IBlockAccess world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() == ModBlocks.redstone_paste_wire) {
			return true;
		} else if (Blocks.unpowered_repeater.isAssociated(state.getBlock())) {
			EnumFacing direction = (EnumFacing) state.getValue(BlockRedstoneRepeater.FACING);
			return direction == side || direction.getOpposite() == side;
		} else {
			return state.getBlock().canConnectRedstone(world, pos, side) && state.getBlock() != Blocks.redstone_wire;
		}

		// TODO
		// return false;
	}

	@Override
	public boolean canProvidePower() {
		return !this.canProvidePower;
	}

	@SideOnly(Side.CLIENT)
	private int colorMultiplier(int powerLevel) {
		float f = (float) powerLevel / 15.0F;
		float f1 = f * 0.6F + 0.4F;

		if (powerLevel == 0) {
			f1 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f3 = f * f * 0.6F - 0.7F;

		if (f2 < 0.0F) {
			f2 = 0.0F;
		}

		if (f3 < 0.0F) {
			f3 = 0.0F;
		}

		int j = MathHelper.clamp_int((int) (f1 * 255.0F), 0, 255);
		int k = MathHelper.clamp_int((int) (f2 * 255.0F), 0, 255);
		int l = MathHelper.clamp_int((int) (f3 * 255.0F), 0, 255);
		return -16777216 | j << 16 | k << 8 | l;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int i = ((Integer) state.getValue(POWER)).intValue();

		if (i != 0) {
			double d0 = (double) pos.getX() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			double d1 = (double) ((float) pos.getY() + 0.0625F);
			double d2 = (double) pos.getZ() + 0.5D + ((double) rand.nextFloat() - 0.5D) * 0.2D;
			float f = (float) i / 15.0F;
			float f1 = f * 0.6F + 0.4F;
			float f2 = Math.max(0.0F, f * f * 0.7F - 0.5F);
			float f3 = Math.max(0.0F, f * f * 0.6F - 0.7F);
			worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, (double) f1, (double) f2, (double) f3, new int[0]);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World worldIn, BlockPos pos) {
		return ModItems.redstone_paste;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(POWER, Integer.valueOf(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Integer) state.getValue(POWER)).intValue();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { DOWN, EAST, NORTH, POWER, SOUTH, UP, WEST });
	}

	static enum AttachPosition implements IStringSerializable {
		DIAGONAL("diagonal"),
		NONE("none"),
		SIDE("side"),
		WALL("wall");
		private final String name;

		private AttachPosition(String name) {
			this.name = name;
		}

		public String toString() {
			return this.getName();
		}

		public String getName() {
			return this.name;
		}
	}

	static enum VerticalAttachPosition implements IStringSerializable {
		CEILING("ceiling"),
		NONE("none"),
		SIDE("side");
		private final String name;

		private VerticalAttachPosition(String name) {
			this.name = name;
		}

		public String toString() {
			return this.getName();
		}

		public String getName() {
			return this.name;
		}
	}

	static enum Model implements IStringSerializable {
		MODEL1("ceiling"),
		MODEL2("none"),
		MODEL3("side");
		private final String name;

		private Model(String name) {
			this.name = name;
		}

		public String toString() {
			return this.getName();
		}

		public String getName() {
			return this.name;
		}
	}
}
