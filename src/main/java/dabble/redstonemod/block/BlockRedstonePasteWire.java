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
import dabble.redstonemod.renderer.RedstonePasteRenderer;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import dabble.redstonemod.util.EnumModel;
import dabble.redstonemod.util.PowerLookup;

public abstract class BlockRedstonePasteWire extends Block implements ITileEntityProvider {
	private boolean canProvidePower = true;
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();
	public EnumFacing pastedSide;
	public EnumFacing pastedSide2;
	public boolean isDoubleFaced = true;

	public BlockRedstonePasteWire(String unlocalisedName, EnumFacing pastedSide, EnumFacing pastedSide2) {
		super(Material.circuits);
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
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRedstonePaste();
	}

	// Same goes with this
	@Deprecated
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	public EnumMap<EnumFacing, EnumModel> getModel(World world, BlockPos pos) {
		ArrayList<EnumFacing> connectionDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing> blockDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing[]> diagonalConnectionDirections = new ArrayList<EnumFacing[]>();

		checkBlockInDirection(EnumFacing.DOWN, connectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.UP, connectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.NORTH, connectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.SOUTH, connectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.WEST, connectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.EAST, connectionDirections, blockDirections, world, pos);

		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, world, pos);

		checkBlockInDirection(EnumFacing.UP, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, world, pos);

		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, world, pos);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, world, pos);

		return EnumModel.getModel(this, connectionDirections, diagonalConnectionDirections, blockDirections, world, pos);
	}

	private void checkBlockInDirection(EnumFacing side, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing> blockDirections, World world, BlockPos pos) {
		AttachState state = getAttachState(world, pos, side);

		if (state != AttachState.NONE)

			// TODO: Separate these
			if (state == AttachState.CONNECT || state == AttachState.PASTE)
				connectionDirections.add(side);
			else
				blockDirections.add(side);
	}

	private AttachState getAttachState(World world, BlockPos pos, EnumFacing side) {
		BlockPos neighborPos = pos.offset(side);
		IBlockState neighborState = world.getBlockState(neighborPos);
		Block neighborBlock = neighborState.getBlock();

		if (neighborBlock instanceof BlockAir)
			return AttachState.NONE;
		else if (neighborBlock instanceof BlockRedstonePasteWire)
			return AttachState.PASTE;
		else if (canPasteOnSide(world, neighborPos, side.getOpposite()))
			return AttachState.BLOCK;
		else if (Blocks.unpowered_repeater.isAssociated(neighborBlock)) {
			EnumFacing direction = (EnumFacing) neighborState.getValue(BlockRedstoneRepeater.FACING);
			if (direction == side || direction.getOpposite() == side)
				return AttachState.CONNECT;
		} else if (neighborBlock.canConnectRedstone(world, pos, side) && !(neighborBlock instanceof BlockRedstoneWire))
			return AttachState.CONNECT;

		return AttachState.NONE;
	}

	private void checkBlockInDirection(EnumFacing side1, EnumFacing side2, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections, World world, BlockPos pos) {

		if (blockDirections.contains(side1) && blockDirections.contains(side2))
			return;

		if (getAttachState(world, pos, side1, side2) != AttachState.NONE)
			diagonalConnectionDirections.add(new EnumFacing[] { side1, side2 });
	}

	private AttachState getAttachState(World world, BlockPos pos, EnumFacing side1, EnumFacing side2) {
		if (world.getBlockState(pos.offset(side1).offset(side2)).getBlock() instanceof BlockRedstonePasteWire)
			return AttachState.PASTE;
		else
			return AttachState.NONE;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		EnumMap<EnumFacing, EnumModel> model = getModel(world, pos);

		MovingObjectPosition nearestFaceEdgeMOP = getNearestFaceEdgeMOP(world, pos, start.addVector(pos.getX(), pos.getY(), pos.getZ()), end.addVector(pos.getX(), pos.getY(), pos.getZ()), model);
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

	private MovingObjectPosition getNearestFaceEdgeMOP(World world, BlockPos pos, Vec3 start, Vec3 end, EnumMap<EnumFacing, EnumModel> model) {
		MovingObjectPosition nearestMOP = super.collisionRayTrace(world, pos, start, end);
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
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public int getRenderType() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
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
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return canPasteOnSide(world, pos.down(), EnumFacing.UP)
				|| canPasteOnSide(world, pos.south(), EnumFacing.NORTH)
				|| canPasteOnSide(world, pos.north(), EnumFacing.SOUTH)
				|| canPasteOnSide(world, pos.east(), EnumFacing.WEST)
				|| canPasteOnSide(world, pos.west(), EnumFacing.EAST)
				|| canPasteOnSide(world, pos.up(), EnumFacing.DOWN);
	}

	public static EnumFacing getFirstPasteableSide(World world, BlockPos pos, EnumFacing startSide) {

		if (canPasteOnSide(world, pos.offset(startSide.getOpposite()), startSide))
			return startSide;

		for (int i = startSide.getIndex() + 1; i % 6 != startSide.getIndex(); ++i) {
			EnumFacing currentSide = EnumFacing.getFront(i);

			if (canPasteOnSide(world, pos.offset(currentSide.getOpposite()), currentSide))
				return currentSide;
		}

		return null;
	}

	private static boolean canPasteOnSide(IBlockAccess world, BlockPos pos, EnumFacing side) {
		// TODO: Double check what should happen in all the different cases
		Block block = world.getBlockState(pos).getBlock();

		if (block.isNormalCube())
			return true;

		if (block.getMaterial() == Material.circuits)
			return false;

		if (block == Blocks.glowstone)
			return true;

		IBlockState state = world.getBlockState(pos);

		if (block instanceof BlockSlab) {
			state = block.getActualState(state, world, pos);
			return (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP && side == EnumFacing.UP)
					|| (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN)
					|| block.isFullBlock();
		}

		if (block instanceof BlockCompressedPowered)
			return true;

		if (block instanceof BlockStairs) {
			state = block.getActualState(state, world, pos);
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
			return ((Integer) block.getActualState(state, world, pos).getValue(BlockSnow.LAYERS)) >= 8;

		if (block instanceof BlockFarmland)
			return side == EnumFacing.DOWN;

		return false;
	}

	private void updateSurroundingRedstonePaste(World world, BlockPos pos) {
		this.calculateCurrentChanges(world, pos);
		ArrayList<BlockPos> blocksNeedingUpdate = Lists.newArrayList(this.blocksNeedingUpdate);
		this.blocksNeedingUpdate.clear();

		for (BlockPos neighborBlockPos : blocksNeedingUpdate)
			world.notifyNeighborsOfStateChange(neighborBlockPos, this);
	}

	public void updateLoadedRedstonePaste(World world, BlockPos pos) {

		if (this.calculateCurrentChanges(world, pos)) {
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.DOWN);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.UP);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.NORTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.SOUTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.WEST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.EAST);

			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.DOWN, EnumFacing.NORTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.DOWN, EnumFacing.SOUTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.DOWN, EnumFacing.WEST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.DOWN, EnumFacing.EAST);

			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.UP, EnumFacing.NORTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.UP, EnumFacing.SOUTH);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.UP, EnumFacing.WEST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.UP, EnumFacing.EAST);

			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.NORTH, EnumFacing.WEST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.NORTH, EnumFacing.EAST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.SOUTH, EnumFacing.WEST);
			this.notifyRedstonePasteOfCurrentChange(world, pos, EnumFacing.SOUTH, EnumFacing.EAST);
		}
	}

	private void notifyRedstonePasteOfCurrentChange(World world, BlockPos pos, EnumFacing... sides) {

		if (sides.length == 2)
			pos = pos.offset(sides[0]).offset(sides[1]);
		else
			pos = pos.offset(sides[0]);

		if (world.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire)
			updateLoadedRedstonePaste(world, pos);
	}

	private boolean calculateCurrentChanges(World world, BlockPos pos) {
		byte signalStrength = PowerLookup.getPower(pos, world);
		int newSignalStrength = signalStrength;
		this.canProvidePower = false;
		int indirectSignalStrength = world.isBlockIndirectlyGettingPowered(pos);
		this.canProvidePower = true;

		if (indirectSignalStrength > 0 && indirectSignalStrength > newSignalStrength - 1)
			newSignalStrength = indirectSignalStrength;

		int k = 0;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos neighborBlockPos = pos.offset(facing);
			boolean yOffset = facing.getAxis() == Axis.Y;

			if (!yOffset)
				k = this.getMaxSignalStrength(world, neighborBlockPos, k);

			if (world.getBlockState(neighborBlockPos).getBlock().isNormalCube() && !world.getBlockState(pos.up()).getBlock().isNormalCube()) {

				if (!yOffset)
					k = this.getMaxSignalStrength(world, neighborBlockPos.up(), k);
			} else if (!world.getBlockState(neighborBlockPos).getBlock().isNormalCube() && !yOffset)
				k = this.getMaxSignalStrength(world, neighborBlockPos.down(), k);
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
			PowerLookup.putPower(pos, (byte) newSignalStrength, world);

			this.blocksNeedingUpdate.add(pos);

			for (byte i = 0; i < EnumFacing.VALUES.length; ++i) {
				EnumFacing direction = EnumFacing.VALUES[i];
				this.blocksNeedingUpdate.add(pos.offset(direction));
			}

			return true;
		}

		return false;
	}

	private int getMaxSignalStrength(World world, BlockPos pos, int strengthIn) {

		if (world.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire) {
			byte signalStrength = PowerLookup.getPower(pos, world);
			return Math.max(signalStrength, strengthIn);
		} else
			return strengthIn;
	}

	private void notifyWireNeighborsOfStateChange(World world, BlockPos pos) {

		if (world.getBlockState(pos).getBlock() == this) {
			world.notifyNeighborsOfStateChange(pos, this);
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (byte j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {

		if (!world.isRemote) {
			this.updateSurroundingRedstonePaste(world, pos);
			Iterator<?> iterator = EnumFacing.Plane.VERTICAL.iterator();
			EnumFacing enumfacing;

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				this.notifyWireNeighborsOfStateChange(world, pos.offset(enumfacing));
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing = (EnumFacing) iterator.next();
				BlockPos blockpos1 = pos.offset(enumfacing);

				if (world.getBlockState(blockpos1).getBlock().isNormalCube())
					this.notifyWireNeighborsOfStateChange(world, blockpos1.up());
				else
					this.notifyWireNeighborsOfStateChange(world, blockpos1.down());
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);

		if (!world.isRemote) {
			EnumFacing[] aenumfacing = EnumFacing.values();
			int i = aenumfacing.length;

			for (byte j = 0; j < i; ++j) {
				EnumFacing enumfacing = aenumfacing[j];
				world.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
			}

			this.updateSurroundingRedstonePaste(world, pos);
			Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();
			EnumFacing enumfacing1;

			while (iterator.hasNext()) {
				enumfacing1 = (EnumFacing) iterator.next();
				this.notifyWireNeighborsOfStateChange(world, pos.offset(enumfacing1));
			}

			iterator = EnumFacing.Plane.HORIZONTAL.iterator();

			while (iterator.hasNext()) {
				enumfacing1 = (EnumFacing) iterator.next();
				BlockPos blockpos1 = pos.offset(enumfacing1);

				if (world.getBlockState(blockpos1).getBlock().isNormalCube())
					this.notifyWireNeighborsOfStateChange(world, blockpos1.up());
				else
					this.notifyWireNeighborsOfStateChange(world, blockpos1.down());
			}
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {

		if (!world.isRemote) {

			if (this.canPlaceBlockAt(world, pos)) {

				if (this.isDoubleFaced)

					if (!canPasteOnSide(world, pos.offset(this.pastedSide), this.pastedSide.getOpposite())) {
						Block modBlocksBlock = ModBlocks.singleSideMap.get(pastedSide2.getIndex());
						world.setBlockState(pos, modBlocksBlock.getDefaultState());
					} else if (!canPasteOnSide(world, pos.offset(this.pastedSide2), this.pastedSide2.getOpposite())) {
						Block modBlocksBlock = ModBlocks.singleSideMap.get(pastedSide.getIndex());
						world.setBlockState(pos, modBlocksBlock.getDefaultState());
					}

				this.updateSurroundingRedstonePaste(world, pos);
			} else {
				this.dropBlockAsItem(world, pos, state, 0);
				world.setBlockToAir(pos);
			}
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.redstone_paste;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		return !this.canProvidePower ? 0 : this.isProvidingWeakPower(world, pos, state, side);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {

		if (!this.canProvidePower)
			return 0;
		else {
			byte i = PowerLookup.getPower(pos, (World) world);

			if (i == 0)
				return 0;
			else if (side == EnumFacing.UP)
				return i;
			else {
				EnumSet<EnumFacing> enumset = EnumSet.noneOf(EnumFacing.class);
				Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

				while (iterator.hasNext()) {
					EnumFacing enumfacing1 = (EnumFacing) iterator.next();

					if (this.func_176339_d(world, pos, enumfacing1))
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

	private boolean func_176339_d(IBlockAccess world, BlockPos pos, EnumFacing side) {
		BlockPos blockpos1 = pos.offset(side);
		IBlockState iblockstate = world.getBlockState(blockpos1);
		Block block = iblockstate.getBlock();
		boolean flag = block.isNormalCube();
		boolean flag1 = world.getBlockState(pos.up()).getBlock().isNormalCube();
		return !flag1 && flag && canRedstonePasteConnect(world, blockpos1.up(), null) ? true : (canRedstonePasteConnect(world, blockpos1, side) ? true : (block == Blocks.powered_repeater &&
				iblockstate.getValue(BlockRedstoneDiode.FACING) == side ? true : !flag && canRedstonePasteConnect(world, blockpos1.down(), null)));
	}

	protected static boolean canRedstonePasteConnect(IBlockAccess world, BlockPos pos, EnumFacing side) {
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
	public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
		return world.getBlockState(pos).getBlock() != this ? super.colorMultiplier(world, pos, renderPass) : RedstonePasteRenderer.colorMultiplier(PowerLookup.getPower(pos, (World) world));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		byte i = PowerLookup.getPower(pos, world);

		if (i != 0) {
			double d0 = pos.getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
			double d1 = pos.getY() + 1 / 16.0;
			double d2 = pos.getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.2;
			float f = i / 15f;
			float f1 = f * 0.6f + 0.4f;
			float f2 = Math.max(0, f * f * 0.7f - 0.5f);
			float f3 = Math.max(0, f * f * 0.6f - 0.7f);
			world.spawnParticle(EnumParticleTypes.REDSTONE, d0, d1, d2, f1, f2, f3, new int[0]);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, BlockPos pos) {
		return ModItems.redstone_paste;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	public String[] getDebugInfo(BlockPos pos, World world) {
		return new String[] { ((this.isDoubleFaced) ? "pasted sides: " : "pasted side: ")
				+ this.pastedSide.getName() + ((this.isDoubleFaced) ? ", " + this.pastedSide2.getName() : ""),
				"power: " + PowerLookup.getPower(pos, world) };
	}

	private static enum AttachState {
		NONE, PASTE, CONNECT, BLOCK;
	}
}
