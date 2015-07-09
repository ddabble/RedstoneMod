package dabble.redstonemod.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCompressedPowered;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dabble.redstonemod.init.ModBlocks;
import dabble.redstonemod.init.ModItems;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import dabble.redstonemod.util.EnumModel;

public abstract class BlockRedstonePasteWire extends Block implements ITileEntityProvider {
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	private boolean canProvidePower = true;
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();
	public EnumFacing pastedSide;
	public EnumFacing pastedSide2;
	public boolean isDoubleFaced = true;

	public BlockRedstonePasteWire(String unlocalisedName, EnumFacing pastedSide, EnumFacing pastedSide2) {
		super(Material.circuits);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(POWER, Integer.valueOf(0)));
		this.setHardness(0);
		this.setStepSound(Block.soundTypeStone);
		this.setUnlocalizedName(unlocalisedName);
		this.pastedSide = pastedSide;
		this.pastedSide2 = pastedSide2;
	}

	public BlockRedstonePasteWire(String unlocalisedName, EnumFacing pastedSide) {
		this(unlocalisedName, pastedSide, null);
		this.isDoubleFaced = false;
	}

	// Will remove once I figure out how to customise the rendering of non-tileEntities
	@Deprecated
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityRedstonePaste();
	}

	// Same goes with this
	@Deprecated
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public EnumMap<EnumFacing, EnumModel> getModel(IBlockAccess worldIn, BlockPos pos) {
		ArrayList<EnumFacing> connectionDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing> blockDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing[]> diagonalConnectionDirections = new ArrayList<EnumFacing[]>();

		checkBlockInDirection(EnumFacing.DOWN, connectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.UP, connectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.NORTH, connectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.SOUTH, connectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.WEST, connectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.EAST, connectionDirections, blockDirections, worldIn, pos);

		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, worldIn, pos);

		checkBlockInDirection(EnumFacing.UP, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, worldIn, pos);

		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, worldIn, pos);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, worldIn, pos);

		return EnumModel.getModel(this, connectionDirections, diagonalConnectionDirections, blockDirections, worldIn, pos);
	}

	private void checkBlockInDirection(EnumFacing side, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing> blockDirections, IBlockAccess worldIn, BlockPos pos) {
		AttachState state = getAttachState(worldIn, pos, side);

		if (state != AttachState.NONE)

			// TODO: Separate these
			if (state == AttachState.CONNECT || state == AttachState.PASTE)
				connectionDirections.add(side);
			else
				blockDirections.add(side);
	}

	private AttachState getAttachState(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		BlockPos neighborPos = pos.offset(side);
		IBlockState neighborState = worldIn.getBlockState(neighborPos);
		Block neighborBlock = neighborState.getBlock();

		if (neighborBlock instanceof BlockAir)
			return AttachState.NONE;
		else if (neighborBlock instanceof BlockRedstonePasteWire)
			return AttachState.PASTE;
		else if (canPasteOnSide(worldIn, neighborPos, side.getOpposite()))
			return AttachState.BLOCK;
		else if (Blocks.unpowered_repeater.isAssociated(neighborBlock)) {
			EnumFacing direction = (EnumFacing) neighborState.getValue(BlockRedstoneRepeater.FACING);
			if (direction == side || direction.getOpposite() == side)
				return AttachState.CONNECT;
		} else if (neighborBlock.canConnectRedstone(worldIn, pos, side) && !(neighborBlock instanceof BlockRedstoneWire))
			return AttachState.CONNECT;

		return AttachState.NONE;
	}

	private void checkBlockInDirection(EnumFacing side1, EnumFacing side2, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections, IBlockAccess worldIn, BlockPos pos) {

		if (blockDirections.contains(side1) && blockDirections.contains(side2))
			return;

		if (getAttachState(worldIn, pos, side1, side2) != AttachState.NONE)
			diagonalConnectionDirections.add(new EnumFacing[] { side1, side2 });
	}

	private AttachState getAttachState(IBlockAccess worldIn, BlockPos pos, EnumFacing side1, EnumFacing side2) {
		BlockPos neighborPos = pos.offset(side1).offset(side2);
		Block neighborBlock = worldIn.getBlockState(neighborPos).getBlock();

		if (neighborBlock instanceof BlockAir)
			return AttachState.NONE;
		else if (neighborBlock instanceof BlockRedstonePasteWire)
			return AttachState.CONNECT;

		return AttachState.NONE;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World worldIn, BlockPos pos, Vec3 start, Vec3 end) {
		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		EnumMap<EnumFacing, EnumModel> model = getModel(worldIn, pos);

		MovingObjectPosition nearestFaceEdgeMOP = getNearestFaceEdgeMOP(worldIn, pos, start.addVector(pos.getX(), pos.getY(), pos.getZ()), end.addVector(pos.getX(), pos.getY(), pos.getZ()), model);
		if (nearestFaceEdgeMOP != null)
			return nearestFaceEdgeMOP;

		EnumMap<EnumFacing, Vec3> hitVecs = new EnumMap<EnumFacing, Vec3>(EnumFacing.class);

		Vec3 hitVecDown = (model.containsKey(EnumFacing.DOWN)) ? getVecInsideXZBounds(start.getIntermediateWithYValue(end, 1 / 16.0), model) : null;
		if (hitVecDown != null)
			hitVecs.put(EnumFacing.DOWN, hitVecDown);

		Vec3 hitVecUp = (model.containsKey(EnumFacing.UP)) ? getVecInsideXZBounds(start.getIntermediateWithYValue(end, 15 / 16.0), model) : null;
		if (hitVecUp != null)
			hitVecs.put(EnumFacing.UP, hitVecUp);

		Vec3 hitVecNorth = (model.containsKey(EnumFacing.NORTH)) ? getVecInsideXYBounds(start.getIntermediateWithZValue(end, 1 / 16.0), model) : null;
		if (hitVecNorth != null)
			hitVecs.put(EnumFacing.NORTH, hitVecNorth);

		Vec3 hitVecSouth = (model.containsKey(EnumFacing.SOUTH)) ? getVecInsideXYBounds(start.getIntermediateWithZValue(end, 15 / 16.0), model) : null;
		if (hitVecSouth != null)
			hitVecs.put(EnumFacing.SOUTH, hitVecSouth);

		Vec3 hitVecWest = (model.containsKey(EnumFacing.WEST)) ? getVecInsideYZBounds(start.getIntermediateWithXValue(end, 1 / 16.0), model) : null;
		if (hitVecWest != null)
			hitVecs.put(EnumFacing.WEST, hitVecWest);

		Vec3 hitVecEast = (model.containsKey(EnumFacing.EAST)) ? getVecInsideYZBounds(start.getIntermediateWithXValue(end, 15 / 16.0), model) : null;
		if (hitVecEast != null)
			hitVecs.put(EnumFacing.EAST, hitVecEast);

		Vec3 hitVec = null;
		EnumFacing facing = null;

		if (hitVecs.size() == 0)
			return null;
		else if (hitVecs.size() == 1) {
			Entry<EnumFacing, Vec3> vec = hitVecs.entrySet().iterator().next();
			hitVec = vec.getValue();
			facing = vec.getKey();
		} else
			for (Entry<EnumFacing, Vec3> vec : hitVecs.entrySet())

				if (hitVec == null) {
					hitVec = vec.getValue();
					facing = vec.getKey();
				} else if (start.squareDistanceTo(vec.getValue()) < start.squareDistanceTo(hitVec)) {
					hitVec = vec.getValue();
					facing = vec.getKey();
				}

		if (hitVec == null)
			return null;

		return new MovingObjectPosition(hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), facing, pos);
	}

	/**
	 * Returns the provided vector if it is within the X and Z bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private Vec3 getVecInsideXZBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.xCoord < 0 || point.xCoord > 1 || point.zCoord < 0 || point.zCoord > 1)
			return null;

		return (!(point.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) && !(point.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST))
				&& !(point.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) && !(point.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH))) ? point : null;
	}

	/**
	 * Returns the provided vector if it is within the X and Y bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private Vec3 getVecInsideXYBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.xCoord < 0 || point.xCoord > 1 || point.yCoord < 0 || point.yCoord > 1)
			return null;

		return (!(point.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) && !(point.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST))
				&& !(point.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) && !(point.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP))) ? point : null;
	}

	/**
	 * Returns the provided vector if it is within the Y and Z bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private Vec3 getVecInsideYZBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.yCoord < 0 || point.yCoord > 1 || point.zCoord < 0 || point.zCoord > 1)
			return null;

		return (!(point.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) && !(point.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP))
				&& !(point.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) && !(point.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH))) ? point : null;
	}

	private MovingObjectPosition getNearestFaceEdgeMOP(World worldIn, BlockPos pos, Vec3 start, Vec3 end, EnumMap<EnumFacing, EnumModel> model) {
		MovingObjectPosition nearestMOP = super.collisionRayTrace(worldIn, pos, start, end);
		if (nearestMOP == null)
			return null;

		Vec3 hitVec = nearestMOP.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
		Axis sideHitAxis = nearestMOP.sideHit.getAxis();

		EnumFacing facing = null;
		byte facesHit = 0;

		if (sideHitAxis != Axis.X && hitVec.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) {
			facing = EnumFacing.WEST;
			++facesHit;
		}

		if (sideHitAxis != Axis.X && hitVec.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST)) {
			facing = EnumFacing.EAST;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) {
			facing = EnumFacing.DOWN;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP)) {
			facing = EnumFacing.UP;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) {
			facing = EnumFacing.NORTH;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH)) {
			facing = EnumFacing.SOUTH;
			++facesHit;
		}

		if (facesHit == 0)
			return null;

		if (facesHit > 1)
			switch (sideHitAxis) {
				case X:
					if (hitVec.yCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.yCoord && hitVec.zCoord >= hitVec.yCoord)
						facing = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.yCoord && hitVec.zCoord >= 1 - hitVec.yCoord)
						facing = EnumFacing.UP;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.zCoord && hitVec.yCoord >= hitVec.zCoord)
						facing = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.zCoord && hitVec.yCoord >= 1 - hitVec.zCoord)
						facing = EnumFacing.SOUTH;
					break;
				case Y:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.xCoord && hitVec.zCoord >= hitVec.xCoord)
						facing = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.xCoord && hitVec.zCoord >= 1 - hitVec.xCoord)
						facing = EnumFacing.EAST;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.zCoord && hitVec.xCoord >= hitVec.zCoord)
						facing = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.zCoord && hitVec.xCoord >= 1 - hitVec.zCoord)
						facing = EnumFacing.SOUTH;
					break;
				case Z:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.xCoord && hitVec.yCoord >= hitVec.xCoord)
						facing = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.xCoord && hitVec.yCoord >= 1 - hitVec.xCoord)
						facing = EnumFacing.EAST;
					else if (hitVec.yCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.yCoord && hitVec.xCoord >= hitVec.yCoord)
						facing = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.yCoord && hitVec.xCoord >= 1 - hitVec.yCoord)
						facing = EnumFacing.UP;
					break;
			}

		return new MovingObjectPosition(nearestMOP.hitVec, facing, pos);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public int getRenderType() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
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
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return canPasteOnSide(worldIn, pos.down(), EnumFacing.UP)
				|| canPasteOnSide(worldIn, pos.south(), EnumFacing.NORTH)
				|| canPasteOnSide(worldIn, pos.north(), EnumFacing.SOUTH)
				|| canPasteOnSide(worldIn, pos.east(), EnumFacing.WEST)
				|| canPasteOnSide(worldIn, pos.west(), EnumFacing.EAST)
				|| canPasteOnSide(worldIn, pos.up(), EnumFacing.DOWN);
	}

	public static EnumFacing getFirstPasteableSide(World worldIn, BlockPos pos, EnumFacing startSide) {

		if (canPasteOnSide(worldIn, pos.offset(startSide.getOpposite()), startSide))
			return startSide;

		for (int i = startSide.getIndex() + 1; i % 6 != startSide.getIndex(); ++i) {
			EnumFacing currentSide = EnumFacing.getFront(i);

			if (canPasteOnSide(worldIn, pos.offset(currentSide.getOpposite()), currentSide))
				return currentSide;
		}

		return null;
	}

	private static boolean canPasteOnSide(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		// TODO: Double check what should happen in all the different cases
		Block block = worldIn.getBlockState(pos).getBlock();

		if (block.isNormalCube())
			return true;

		if (block.getMaterial() == Material.circuits)
			return false;

		if (block == Blocks.glowstone)
			return true;

		IBlockState state = worldIn.getBlockState(pos);

		if (block instanceof BlockSlab) {
			state = block.getActualState(state, worldIn, pos);
			return (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP && side == EnumFacing.UP)
					|| (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN)
					|| block.isFullBlock();
		}

		if (block instanceof BlockCompressedPowered)
			return true;

		if (block instanceof BlockStairs) {
			state = block.getActualState(state, worldIn, pos);
			boolean flipped = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;

			if (side.getHorizontalIndex() < 0)
				return (side == EnumFacing.UP) ? flipped : !flipped;

			BlockStairs.EnumShape shape = (BlockStairs.EnumShape) state.getValue(BlockStairs.SHAPE);
			EnumFacing facing = (EnumFacing) state.getValue(BlockStairs.FACING);

			if (facing == side)
				return true;

			if (flipped) {

				if (shape == BlockStairs.EnumShape.INNER_LEFT)
					return side == facing.rotateYCCW();

				if (shape == BlockStairs.EnumShape.INNER_RIGHT)
					return side == facing.rotateY();
			} else {

				if (shape == BlockStairs.EnumShape.INNER_LEFT)
					return side == facing.rotateY();

				if (shape == BlockStairs.EnumShape.INNER_RIGHT)
					return side == facing.rotateYCCW();
			}

			return false;
		}

		if (block instanceof BlockHopper)
			return side == EnumFacing.UP;

		if (block instanceof BlockSnow)
			return ((Integer) block.getActualState(state, worldIn, pos).getValue(BlockSnow.LAYERS)) >= 8;

		if (block instanceof BlockFarmland)
			return side == EnumFacing.DOWN;

		return false;
	}

	private IBlockState updateSurroundingRedstone(World worldIn, BlockPos pos, IBlockState state) {
		state = this.calculateCurrentChanges(worldIn, pos, state);
		ArrayList<BlockPos> blocksNeedingUpdate = Lists.newArrayList(this.blocksNeedingUpdate);
		this.blocksNeedingUpdate.clear();

		for (BlockPos neighborBlockPos : blocksNeedingUpdate)
			worldIn.notifyNeighborsOfStateChange(neighborBlockPos, this);

		return state;
	}

	private IBlockState calculateCurrentChanges(World worldIn, BlockPos pos, IBlockState state) {
		IBlockState stateIn = state;
		int signalStrength = ((Integer) state.getValue(POWER)).intValue();
		int newSignalStrength = signalStrength;
		this.canProvidePower = false;
		int indirectSignalStrength = worldIn.isBlockIndirectlyGettingPowered(pos);
		this.canProvidePower = true;

		if (indirectSignalStrength > 0 && indirectSignalStrength > newSignalStrength - 1)
			newSignalStrength = indirectSignalStrength;

		int k = 0;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos neighborBlockPos = pos.offset(facing);
			boolean yOffset = facing.getAxis() == Axis.Y;

			if (!yOffset)
				k = this.getMaxSignalStrength(worldIn.getBlockState(neighborBlockPos), k);

			if (worldIn.getBlockState(neighborBlockPos).getBlock().isNormalCube() && !worldIn.getBlockState(pos.up()).getBlock().isNormalCube()) {

				if (!yOffset)
					k = this.getMaxSignalStrength(worldIn.getBlockState(neighborBlockPos.up()), k);
			} else if (!worldIn.getBlockState(neighborBlockPos).getBlock().isNormalCube() && !yOffset)
				k = this.getMaxSignalStrength(worldIn.getBlockState(neighborBlockPos.down()), k);
		}

		if (k > newSignalStrength)
			newSignalStrength = k - 1;
		else if (newSignalStrength > 0)
			--newSignalStrength;
		else
			newSignalStrength = 0;

		if (indirectSignalStrength > newSignalStrength - 1)
			newSignalStrength = indirectSignalStrength;

		if (signalStrength != newSignalStrength) {
			state = state.withProperty(POWER, Integer.valueOf(newSignalStrength));

			if (worldIn.getBlockState(pos) == stateIn)
				worldIn.setBlockState(pos, state, 2);

			this.blocksNeedingUpdate.add(pos);

			for (byte i = 0; i < EnumFacing.VALUES.length; ++i) {
				EnumFacing direction = EnumFacing.VALUES[i];
				this.blocksNeedingUpdate.add(pos.offset(direction));
			}
		}

		return state;
	}

	private int getMaxSignalStrength(IBlockState state, int strengthIn) {

		if (state.getBlock() != this)
			return strengthIn;
		else {
			int signalStrength = ((Integer) state.getValue(POWER)).intValue();
			return Math.max(signalStrength, strengthIn);
		}
	}

	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {

		if (worldIn.getBlockState(pos).getBlock() == this) {
			worldIn.notifyNeighborsOfStateChange(pos, this);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (byte j = 0; j < i; ++j) {
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

				if (worldIn.getBlockState(blockpos1).getBlock().isNormalCube())
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.up());
				else
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.down());
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);

		if (!worldIn.isRemote) {
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (byte j = 0; j < i; ++j) {
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

				if (worldIn.getBlockState(blockpos1).getBlock().isNormalCube())
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.up());
				else
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos1.down());
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {

		if (!worldIn.isRemote) {

			if (this.canPlaceBlockAt(worldIn, pos)) {

				if (this.isDoubleFaced)

					if (!canPasteOnSide(worldIn, pos.offset(this.pastedSide), this.pastedSide.getOpposite())) {
						Block modBlocksBlock = ModBlocks.singleSideMap.get(pastedSide2.getIndex());
						worldIn.setBlockState(pos, modBlocksBlock.getDefaultState());
					} else if (!canPasteOnSide(worldIn, pos.offset(this.pastedSide2), this.pastedSide2.getOpposite())) {
						Block modBlocksBlock = ModBlocks.singleSideMap.get(pastedSide.getIndex());
						worldIn.setBlockState(pos, modBlocksBlock.getDefaultState());
					}

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

		if (!this.canProvidePower)
			return 0;
		else {
			int i = ((Integer) state.getValue(POWER)).intValue();

			if (i == 0)
				return 0;
			else if (side == EnumFacing.UP)
				return i;
			else {
				EnumSet<EnumFacing> enumset = EnumSet.noneOf(EnumFacing.class);
				Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

				while (iterator.hasNext()) {
					EnumFacing enumfacing1 = (EnumFacing) iterator.next();

					if (this.func_176339_d(worldIn, pos, enumfacing1))
						enumset.add(enumfacing1);
				}

				if (side.getAxis().isHorizontal() && enumset.isEmpty())
					return i;
				else if (enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY()))
					return i;
				else
					return 0;
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

		if (state.getBlock() instanceof BlockRedstonePasteWire)
			return true;
		else if (Blocks.unpowered_repeater.isAssociated(state.getBlock())) {
			EnumFacing direction = (EnumFacing) state.getValue(BlockRedstoneRepeater.FACING);
			return direction == side || direction.getOpposite() == side;
		} else
			return state.getBlock().canConnectRedstone(world, pos, side) && state.getBlock() != Blocks.redstone_wire;
	}

	@Override
	public boolean canProvidePower() {
		return !this.canProvidePower;
	}

	// TODO: Look into this when fixing the breaking particles; it affects their colour
	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		return iblockstate.getBlock() != this ? super.colorMultiplier(worldIn, pos, renderPass) : this.colorMultiplier(((Integer) iblockstate.getValue(POWER)).intValue());
	}

	@SideOnly(Side.CLIENT)
	private int colorMultiplier(int powerLevel) {
		float f = (float) powerLevel / 15f;
		float f1 = f * 0.6f + 0.4f;

		if (powerLevel == 0)
			f1 = 0.3f;

		float f2 = f * f * 0.7f - 0.5f;
		float f3 = f * f * 0.6f - 0.7f;

		if (f2 < 0)
			f2 = 0;

		if (f3 < 0)
			f3 = 0;

		int j = MathHelper.clamp_int((int) (f1 * 255), 0, 255);
		int k = MathHelper.clamp_int((int) (f2 * 255), 0, 255);
		int l = MathHelper.clamp_int((int) (f3 * 255), 0, 255);
		return -16777216 | j << 16 | k << 8 | l;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		int i = ((Integer) state.getValue(POWER)).intValue();

		if (i != 0) {
			double d0 = pos.getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
			double d1 = pos.getY() + 1 / 16.0;
			double d2 = pos.getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
			float f = i / 15f;
			float f1 = f * 0.6f + 0.4f;
			float f2 = Math.max(0, f * f * 0.7f - 0.5f);
			float f3 = Math.max(0, f * f * 0.6f - 0.7f);
			worldIn.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, f1, f2, f3, new int[0]);
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
		return new BlockState(this, new IProperty[] { POWER });
	}

	public String getDebugInfo() {
		return ((this.isDoubleFaced) ? "pasted sides: " : "pasted side: ")
				+ this.pastedSide.getName() + ((this.isDoubleFaced) ? ", " + this.pastedSide2.getName() : "");
	}

	private static enum AttachState {
		NONE, PASTE, CONNECT, BLOCK;
	}
}
