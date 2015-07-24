package dabble.redstonemod.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCompressedPowered;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneRepeater;
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
import dabble.redstonemod.init.ModItems;
import dabble.redstonemod.renderer.RedstonePasteRenderer;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import dabble.redstonemod.util.EnumModel;
import dabble.redstonemod.util.PowerLookup;

public abstract class BlockRedstonePasteWire extends Block implements ITileEntityProvider {
	private boolean canProvidePower = true;
	private final byte numberOfPastedSides;

	public abstract EnumFacing[] getPastedSides(IBlockState state);

	public abstract IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state);

	abstract EnumSet<EnumFacing> getValidPastedSides(IBlockState state, BlockPos pos, World world);

	public BlockRedstonePasteWire(String unlocalisedName, byte numberOfPastedSides) {
		super(Material.circuits);
		this.setHardness(0);
		this.setStepSound(Block.soundTypeStone);
		this.setUnlocalizedName(unlocalisedName);
		this.numberOfPastedSides = numberOfPastedSides;
	}

	// Will remove once I figure out how to customise the rendering of non-tileEntities
	@Deprecated
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	// Same goes with this
	@Deprecated
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityRedstonePaste();
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public boolean canProvidePower() {
		// TODO: Look into what changing this does
		return this.canProvidePower;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public int getRenderType() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, BlockPos pos) {
		return ModItems.redstone_paste;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ModItems.redstone_paste;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	public EnumMap<EnumFacing, EnumModel> getModel(BlockPos pos, World world) {
		ArrayList<EnumFacing> connectionDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing> blockDirections = new ArrayList<EnumFacing>();
		ArrayList<EnumFacing[]> diagonalConnectionDirections = new ArrayList<EnumFacing[]>();

		for (EnumFacing direction : EnumFacing.VALUES)
			checkBlockInDirection(direction, connectionDirections, blockDirections, pos, world);

		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.DOWN, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, pos, world);

		checkBlockInDirection(EnumFacing.UP, EnumFacing.NORTH, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.SOUTH, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.UP, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, pos, world);

		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.NORTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.WEST, diagonalConnectionDirections, blockDirections, pos, world);
		checkBlockInDirection(EnumFacing.SOUTH, EnumFacing.EAST, diagonalConnectionDirections, blockDirections, pos, world);

		return EnumModel.getModel(this, connectionDirections, diagonalConnectionDirections, blockDirections, pos, world);
	}

	private void checkBlockInDirection(EnumFacing side, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing> blockDirections, BlockPos pos, World world) {
		AttachState state = getAttachState(side, pos, world);

		if (state != AttachState.NONE)

			// TODO: Separate these
			if (state == AttachState.CONNECT || state == AttachState.PASTE)
				connectionDirections.add(side);
			else
				blockDirections.add(side);
	}

	private AttachState getAttachState(EnumFacing side, BlockPos pos, World world) {
		BlockPos neighbourPos = pos.offset(side);
		IBlockState neighbourState = world.getBlockState(neighbourPos);
		Block neighbourBlock = neighbourState.getBlock();

		if (neighbourBlock == Blocks.air)
			return AttachState.NONE;
		else if (neighbourBlock instanceof BlockRedstonePasteWire)
			return AttachState.PASTE;
		else if (canPasteOnSideOfBlock(side.getOpposite(), neighbourPos, world))
			return AttachState.BLOCK;
		else if (Blocks.unpowered_repeater.isAssociated(neighbourBlock)) {
			EnumFacing direction = (EnumFacing) neighbourState.getValue(BlockRedstoneRepeater.FACING);
			if (direction == side || direction.getOpposite() == side)
				return AttachState.CONNECT;
		} else if (neighbourBlock.canConnectRedstone(world, pos, side) && !(neighbourBlock == Blocks.redstone_wire))
			return AttachState.CONNECT;

		return AttachState.NONE;
	}

	private void checkBlockInDirection(EnumFacing side1, EnumFacing side2, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections, BlockPos pos, World world) {

		if (blockDirections.contains(side1) && blockDirections.contains(side2))
			return;

		if (getAttachState(side1, side2, pos, world) != AttachState.NONE)
			diagonalConnectionDirections.add(new EnumFacing[] { side1, side2 });
	}

	private AttachState getAttachState(EnumFacing side1, EnumFacing side2, BlockPos pos, World world) {
		if (world.getBlockState(pos.offset(side1).offset(side2)).getBlock() instanceof BlockRedstonePasteWire)
			return AttachState.PASTE;
		else
			return AttachState.NONE;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		EnumMap<EnumFacing, EnumModel> model = getModel(pos, world);

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
		EnumFacing sideLookingAt = null;

		if (hitVecs.size() == 0)
			return null;
		else if (hitVecs.size() == 1) {
			Entry<EnumFacing, Vec3> vec = hitVecs.entrySet().iterator().next();
			hitVec = vec.getValue();
			sideLookingAt = vec.getKey();
		} else
			for (Entry<EnumFacing, Vec3> vec : hitVecs.entrySet())

				if (hitVec == null) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				} else if (start.squareDistanceTo(vec.getValue()) < start.squareDistanceTo(hitVec)) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				}

		if (hitVec == null)
			return null;

		MovingObjectPosition redstonePasteHit = new MovingObjectPosition(hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), sideLookingAt.getOpposite(), pos);
		redstonePasteHit.hitInfo = sideLookingAt;
		return redstonePasteHit;
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

		EnumFacing sideLookingAt = null;
		byte facesHit = 0;

		if (sideHitAxis != Axis.X && hitVec.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) {
			sideLookingAt = EnumFacing.WEST;
			++facesHit;
		}

		if (sideHitAxis != Axis.X && hitVec.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST)) {
			sideLookingAt = EnumFacing.EAST;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) {
			sideLookingAt = EnumFacing.DOWN;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP)) {
			sideLookingAt = EnumFacing.UP;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) {
			sideLookingAt = EnumFacing.NORTH;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH)) {
			sideLookingAt = EnumFacing.SOUTH;
			++facesHit;
		}

		if (facesHit == 0)
			return null;

		if (facesHit > 1)
			switch (sideHitAxis) {
				case X:
					if (hitVec.yCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.yCoord && hitVec.zCoord >= hitVec.yCoord)
						sideLookingAt = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.yCoord && hitVec.zCoord >= 1 - hitVec.yCoord)
						sideLookingAt = EnumFacing.UP;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.zCoord && hitVec.yCoord >= hitVec.zCoord)
						sideLookingAt = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.zCoord && hitVec.yCoord >= 1 - hitVec.zCoord)
						sideLookingAt = EnumFacing.SOUTH;

					break;

				case Y:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.xCoord && hitVec.zCoord >= hitVec.xCoord)
						sideLookingAt = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.xCoord && hitVec.zCoord >= 1 - hitVec.xCoord)
						sideLookingAt = EnumFacing.EAST;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.zCoord && hitVec.xCoord >= hitVec.zCoord)
						sideLookingAt = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.zCoord && hitVec.xCoord >= 1 - hitVec.zCoord)
						sideLookingAt = EnumFacing.SOUTH;

					break;

				case Z:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.xCoord && hitVec.yCoord >= hitVec.xCoord)
						sideLookingAt = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.xCoord && hitVec.yCoord >= 1 - hitVec.xCoord)
						sideLookingAt = EnumFacing.EAST;
					else if (hitVec.yCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.yCoord && hitVec.xCoord >= hitVec.yCoord)
						sideLookingAt = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.yCoord && hitVec.xCoord >= 1 - hitVec.yCoord)
						sideLookingAt = EnumFacing.UP;

					break;
			}

		MovingObjectPosition redstonePasteHit = new MovingObjectPosition(nearestMOP.hitVec, nearestMOP.sideHit, pos);
		redstonePasteHit.hitInfo = sideLookingAt;
		return redstonePasteHit;
	}

	public static EnumFacing getFirstPasteableSide(EnumFacing startSide, BlockPos pos, World world) {

		if (canPasteOnSideOfBlock(startSide.getOpposite(), pos.offset(startSide), world))
			return startSide;

		for (EnumFacing currentSide : EnumFacing.VALUES)

			if (currentSide != startSide && canPasteOnSideOfBlock(currentSide.getOpposite(), pos.offset(currentSide), world))
				return currentSide;

		return null;
	}

	protected static boolean canPasteOnSideOfBlock(EnumFacing side, BlockPos pos, World world) {
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

		if (block == Blocks.hopper)
			return side == EnumFacing.UP;

		if (block instanceof BlockSnow)
			return ((Integer) block.getActualState(state, world, pos).getValue(BlockSnow.LAYERS)) >= 8;

		if (block instanceof BlockFarmland)
			return side == EnumFacing.DOWN;

		return false;
	}

	protected void updateSurroundingBlocks(BlockPos pos, World world, boolean shouldOnlyAffectRedstonePaste) {

		for (EnumFacing direction : EnumFacing.VALUES)
			this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, direction);

		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.DOWN, EnumFacing.NORTH);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.DOWN, EnumFacing.SOUTH);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.DOWN, EnumFacing.WEST);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.DOWN, EnumFacing.EAST);

		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.UP, EnumFacing.NORTH);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.UP, EnumFacing.SOUTH);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.UP, EnumFacing.WEST);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.UP, EnumFacing.EAST);

		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.NORTH, EnumFacing.WEST);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.NORTH, EnumFacing.EAST);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.SOUTH, EnumFacing.WEST);
		this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, EnumFacing.SOUTH, EnumFacing.EAST);

		if (!shouldOnlyAffectRedstonePaste)

			for (EnumFacing direction : EnumFacing.VALUES)
				this.updateBlockInDirection(pos, world, shouldOnlyAffectRedstonePaste, direction, direction);
	}

	private void updateBlockInDirection(BlockPos pos, World world, boolean shouldOnlyAffectRedstonePaste, EnumFacing... direction) {

		if (direction.length == 2)
			pos = pos.offset(direction[0]).offset(direction[1]);
		else
			pos = pos.offset(direction[0]);

		if (shouldOnlyAffectRedstonePaste) {

			if (world.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire)
				this.calculateCurrentChanges(pos, world, shouldOnlyAffectRedstonePaste);
		} else
			world.notifyBlockOfStateChange(pos, this);
	}

	public boolean calculateCurrentChanges(BlockPos pos, World world, boolean shouldOnlyAffectRedstonePaste) {
		byte signalStrength = PowerLookup.getPower(pos, world);
		int newSignalStrength = signalStrength;
		this.canProvidePower = false;
		int indirectSignalStrength = world.isBlockIndirectlyGettingPowered(pos);
		this.canProvidePower = true;

		if (indirectSignalStrength > 0 && indirectSignalStrength > newSignalStrength - 1)
			newSignalStrength = indirectSignalStrength;

		int k = 0;

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos neighbourBlockPos = pos.offset(facing);
			boolean yOffset = facing.getAxis() == Axis.Y;

			if (!yOffset)
				k = this.getMaxSignalStrength(k, neighbourBlockPos, world);

			if (world.getBlockState(neighbourBlockPos).getBlock().isNormalCube() && !world.getBlockState(pos.up()).getBlock().isNormalCube()) {

				if (!yOffset)
					k = this.getMaxSignalStrength(k, neighbourBlockPos.up(), world);
			} else if (!world.getBlockState(neighbourBlockPos).getBlock().isNormalCube() && !yOffset)
				k = this.getMaxSignalStrength(k, neighbourBlockPos.down(), world);
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
			this.updateSurroundingBlocks(pos, world, shouldOnlyAffectRedstonePaste);
			return true;
		} else
			return false;
	}

	private int getMaxSignalStrength(int strengthIn, BlockPos pos, World world) {

		if (world.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire) {
			byte signalStrength = PowerLookup.getPower(pos, world);
			return Math.max(signalStrength, strengthIn);
		} else
			return strengthIn;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {

		if (!world.isRemote) {

			if (!this.calculateCurrentChanges(pos, world, false))
				this.updateSurroundingBlocks(pos, world, false);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);

		if (!world.isRemote) {
			PowerLookup.removePower(pos, world);
			this.updateSurroundingBlocks(pos, world, false);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block triggerBlock) {

		if (!world.isRemote) {
			EnumSet<EnumFacing> validPastedSides = this.getValidPastedSides(state, pos, world);
			if (validPastedSides.size() < this.numberOfPastedSides) {
				IBlockState newState = null;

				switch (validPastedSides.size()) {
					case 1:
						newState = BlockRedstonePasteWire_SinglePasted.getStateFromSide(validPastedSides.iterator().next());
						break;

					case 2:
						newState = BlockRedstonePasteWire_DoublePasted.getStateFromSides(validPastedSides);
						break;

					case 3:
						if (validPastedSides.contains(EnumFacing.DOWN))
							newState = BlockRedstonePasteWire_TriplePasted_OnGround.getStateFromSides(validPastedSides);
						else
							newState = BlockRedstonePasteWire_TriplePasted_OnWalls.getStateFromSides(validPastedSides);

						break;

					case 4:
						newState = BlockRedstonePasteWire_QuadruplePasted.getStateFromSides(validPastedSides);
						break;

					case 0:
						this.dropBlockAsItem(world, pos, state, 0);
						world.setBlockToAir(pos);
						return;
				}

				world.setBlockState(pos, newState);
			}

			this.calculateCurrentChanges(pos, world, false);
		}
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
			byte signalStrength = PowerLookup.getPower(pos, (World) world);
			if (signalStrength == 0)
				return 0;
			else if (side == EnumFacing.UP)
				return signalStrength;
			else {
				EnumSet<EnumFacing> sidesProvidingPowerTo = EnumSet.noneOf(EnumFacing.class);

				for (EnumFacing horizontalSide : EnumFacing.HORIZONTALS)

					if (this.canProvidePowerToSide(world, pos, horizontalSide))
						sidesProvidingPowerTo.add(horizontalSide);

				if (side.getAxis().isHorizontal() && sidesProvidingPowerTo.isEmpty() || world.getBlockState(pos.offset(side.getOpposite())).getBlock() instanceof BlockRedstoneDiode)
					return signalStrength;
				else if (sidesProvidingPowerTo.contains(side) && !sidesProvidingPowerTo.contains(side.rotateYCCW()) && !sidesProvidingPowerTo.contains(side.rotateY()))
					return signalStrength;
				else
					return 0;
			}
		}
	}

	private boolean canProvidePowerToSide(IBlockAccess world, BlockPos pos, EnumFacing side) {
		BlockPos neighbourBlockPos = pos.offset(side);
		IBlockState neighbourBlockState = world.getBlockState(neighbourBlockPos);
		Block neighbourBlock = neighbourBlockState.getBlock();
		boolean isNeighbourBlockNormalCube = neighbourBlock.isNormalCube();
		boolean isAboveBlockNormalCube = world.getBlockState(pos.up()).getBlock().isNormalCube();
		return (!isAboveBlockNormalCube && isNeighbourBlockNormalCube && canRedstonePasteConnect(world, neighbourBlockPos.up(), null)) ? true :
				(canRedstonePasteConnect(world, neighbourBlockPos, side) ? true :
						(neighbourBlock == Blocks.powered_repeater && neighbourBlockState.getValue(BlockRedstoneDiode.FACING) == side ? true :
								!isNeighbourBlockNormalCube && canRedstonePasteConnect(world, neighbourBlockPos.down(), null)));
	}

	private static boolean canRedstonePasteConnect(IBlockAccess world, BlockPos pos, EnumFacing side) {
		IBlockState state = world.getBlockState(pos);

		if (state.getBlock() instanceof BlockRedstonePasteWire)
			return true;
		else if (Blocks.unpowered_repeater.isAssociated(state.getBlock())) {
			EnumFacing direction = (EnumFacing) state.getValue(BlockRedstoneRepeater.FACING);
			return direction == side || direction.getOpposite() == side;
		} else
			return state.getBlock().canConnectRedstone(world, pos, side) && state.getBlock() != Blocks.redstone_wire;
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

	public String getDebugInfo(BlockPos pos, World world) {
		return "power: " + PowerLookup.getPower(pos, world);
	}

	private static enum AttachState {
		NONE, PASTE, CONNECT, BLOCK;
	}
}
