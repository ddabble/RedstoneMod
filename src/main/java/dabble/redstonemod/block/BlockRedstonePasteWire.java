package dabble.redstonemod.block;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCompressedPowered;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
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
import dabble.redstonemod.util.EnumFacing2;
import dabble.redstonemod.util.EnumModel;
import dabble.redstonemod.util.ModelLookup;
import dabble.redstonemod.util.PowerLookup;

public abstract class BlockRedstonePasteWire extends Block implements ITileEntityProvider {
	// Only here for debugging, like displaying the block's power on the debug screen, and making the game display every possible block state in a debug world.
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	public static boolean isDebugWorld = false;
	private boolean canProvidePower = true;
	private final byte numberOfPastedSides;

	public abstract EnumFacing[] getPastedSides(IBlockState state);

	public abstract EnumSet<EnumFacing> getPastedSidesSet(IBlockState state);

	public abstract boolean isPastedOnSide(EnumFacing side, IBlockState state);

	public abstract IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state, BlockPos pos, EntityPlayer player, World world);

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
	public boolean isAssociatedBlock(Block otherBlock) {
		return otherBlock instanceof BlockRedstonePasteWire;
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
		// // TODO: Look into what changing this does
		// return this.canProvidePower;
		// Currently like this to prevent redstone connecting to redstone paste
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public int getRenderType() {
		return -1;
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
	public int quantityDropped(Random random) {
		return this.numberOfPastedSides;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.withProperty(POWER, Integer.valueOf(PowerLookup.getPower(pos, (World) world)));
	}

	public EnumMap<EnumFacing, EnumModel> getModel(BlockPos pos, World world) {
		EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides = new EnumMap<EnumFacing, EnumSet<EnumFacing>>(EnumFacing.class);
		IBlockState state = world.getBlockState(pos);

		for (EnumFacing direction : EnumFacing.VALUES)
			this.checkBlockInDirection(direction, connectionSides, state, pos, world);

		for (EnumFacing2 direction : EnumFacing2.VALUES)
			this.checkBlockInDirection(direction, connectionSides, state, pos, world);

		return EnumModel.getModelFromExternalConnections(connectionSides, this.getPastedSidesSet(state));
	}

	private void checkBlockInDirection(EnumFacing direction, EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides, IBlockState state, BlockPos pos, World world) {
		BlockPos neighbourPos = pos.offset(direction);
		IBlockState neighbourState = world.getBlockState(neighbourPos);
		Block neighbourBlock = neighbourState.getBlock();

		if (neighbourBlock instanceof BlockRedstonePasteWire) {
			EnumSet<EnumFacing> pastedSides = this.getPastedSidesSet(state);
			EnumSet<EnumFacing> neighbourPastedSides = ((BlockRedstonePasteWire) neighbourBlock).getPastedSidesSet(neighbourState);
			boolean hasConnected = false;

			for (EnumFacing side : neighbourPastedSides) {

				if (pastedSides.contains(side)) {
					this.addConnection(side, direction, connectionSides, state, pos, world, true);
					hasConnected = true;
				}
			}

			if (!hasConnected) {

				for (EnumFacing side : EnumFacing.VALUES) {

					if (pastedSides.contains(side)) {
						boolean isOppositeSide = neighbourPastedSides.size() == 1 && side == neighbourPastedSides.iterator().next().getOpposite();

						if (!isOppositeSide && canPasteOnSideOfBlock(side.getOpposite(), neighbourPos.offset(side), world)) {
							this.addConnection(side, direction, connectionSides, state, pos, world, true);
							return;
						}
					} else if (neighbourPastedSides.contains(side)) {
						boolean isOppositeSide = pastedSides.size() == 1 && side == pastedSides.iterator().next().getOpposite();

						if (!isOppositeSide && canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world)) {
							this.addConnection(side, direction, connectionSides, state, pos, world, false);
							return;
						}
					}
				}
			}

			return;
		}
		// TODO: Maybe switch places with this and the redstone_wire one, in case canProvidePower() returns false in BlockRedstoneWire..
		// (Only necessary if this method is going to be called while calculating redstone power.)
		else if (!neighbourBlock.canProvidePower())
			return;
		else if (neighbourBlock == Blocks.redstone_wire) {

			if (isRedstoneWirePoweringDirection(direction.getOpposite(), neighbourPos, world))
				this.addConnection(EnumFacing.DOWN, direction, connectionSides, state, pos, world, false);

			return;
		} else if (Blocks.unpowered_repeater.isAssociated(neighbourBlock)) {

			if (direction.getAxis() == ((EnumFacing) neighbourState.getValue(BlockDirectional.FACING)).getAxis())
				this.addConnection(EnumFacing.DOWN, direction, connectionSides, state, pos, world, false);

			return;
		} else if (neighbourBlock instanceof BlockCompressedPowered) {

			for (EnumFacing side : this.getPastedSides(state)) {

				if (side != direction.getOpposite())
					this.addConnection(side, direction, connectionSides, state, pos, world, true);
			}

			return;
		} else {
			EnumFacing attachedSide = this.getAttachedSideOfBlockProvidingPower(neighbourState, neighbourBlock);

			if (attachedSide != null && this.isPastedOnSide(attachedSide, state)) {
				this.addConnection(attachedSide, direction, connectionSides, state, pos, world, true);
				return;
			}
		}

		this.addConnection(this.getPastedSides(state)[0], direction, connectionSides, state, pos, world, true);
	}

	private static boolean isRedstoneWirePoweringDirection(EnumFacing direction, BlockPos pos, World world) {
		BlockPos leftPos = pos.offset(direction.rotateYCCW());

		if (isRedstoneWire(leftPos, world) || isRedstoneWire(leftPos.down(), world) || isRedstoneWire(leftPos.up(), world))
			return false;

		BlockPos rightPos = pos.offset(direction.rotateY());

		if (isRedstoneWire(rightPos, world) || isRedstoneWire(rightPos.down(), world) || isRedstoneWire(rightPos.up(), world))
			return false;

		if (isRedstoneWire(pos.offset(direction).down(), world))
			return false;

		return true;
	}

	private static boolean isRedstoneWire(BlockPos pos, World world) {
		return world.getBlockState(pos).getBlock() == Blocks.redstone_wire;
	}

	private EnumFacing getAttachedSideOfBlockProvidingPower(IBlockState state, Block block) {

		if (block instanceof BlockRedstoneTorch)
			return (EnumFacing) state.getValue(BlockRedstoneTorch.FACING);
		else if (block instanceof BlockLever)
			return ((EnumOrientation) state.getValue(BlockLever.FACING)).getFacing();
		else if (block instanceof BlockButton)
			return (EnumFacing) state.getValue(BlockButton.FACING);
		else if (block instanceof BlockRailDetector) {

			switch ((EnumRailDirection) state.getValue(BlockRailDetector.SHAPE)) {
				case ASCENDING_EAST:
					return EnumFacing.EAST;

				case ASCENDING_WEST:
					return EnumFacing.WEST;

				case ASCENDING_NORTH:
					return EnumFacing.NORTH;

				case ASCENDING_SOUTH:
					return EnumFacing.SOUTH;

				default:
					return EnumFacing.DOWN;
			}
		} else if (block instanceof BlockTripWireHook)
			return (EnumFacing) state.getValue(BlockTripWireHook.FACING);

		return null;
	}

	private void checkBlockInDirection(EnumFacing2 direction, EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides, IBlockState state, BlockPos pos, World world) {
		IBlockState neighbourState = world.getBlockState(direction.offsetBlockPos(pos));

		if (neighbourState.getBlock() instanceof BlockRedstonePasteWire) {
			BlockRedstonePasteWire neighbourBlock = (BlockRedstonePasteWire) neighbourState.getBlock();

			if (!world.getBlockState(pos.offset(direction.facing1)).getBlock().isSolidFullCube()) {
				boolean isOppositeSide = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing1, neighbourState);

				if ((!isOppositeSide && this.isPastedOnSide(direction.facing2, state)) || neighbourBlock.isPastedOnSide(direction.facing1.getOpposite(), neighbourState))
					this.addConnection(direction.facing2, direction.facing1, connectionSides, state, pos, world, false);
			} else if (!world.getBlockState(pos.offset(direction.facing2)).getBlock().isSolidFullCube()) {
				boolean isOppositeSide = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing2, neighbourState);

				if ((!isOppositeSide && this.isPastedOnSide(direction.facing1, state)) || neighbourBlock.isPastedOnSide(direction.facing2.getOpposite(), neighbourState))
					this.addConnection(direction.facing1, direction.facing2, connectionSides, state, pos, world, false);
			}
		}
	}

	private void addConnection(EnumFacing side, EnumFacing connection, EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides, IBlockState state, BlockPos pos, World world, boolean isGuaranteedPasted) {

		if (!isGuaranteedPasted) {
			boolean isOppositeSide = this.numberOfPastedSides == 1 && this.isPastedOnSide(side.getOpposite(), state);

			if (isOppositeSide || !canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world))
				return;
		}

		if (connectionSides.containsKey(side))
			connectionSides.get(side).add(connection);
		else
			connectionSides.put(side, EnumSet.of(connection));
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
		} else {

			for (Entry<EnumFacing, Vec3> vec : hitVecs.entrySet()) {

				if (hitVec == null) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				} else if (start.squareDistanceTo(vec.getValue()) < start.squareDistanceTo(hitVec)) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				}
			}
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

		if (facesHit > 1) {

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
		}

		MovingObjectPosition redstonePasteHit = new MovingObjectPosition(nearestMOP.hitVec, nearestMOP.sideHit, pos);
		redstonePasteHit.hitInfo = sideLookingAt;
		return redstonePasteHit;
	}

	public static EnumFacing getFirstPasteableSide(EnumFacing startSide, BlockPos pos, World world) {

		if (canPasteOnSideOfBlock(startSide.getOpposite(), pos.offset(startSide), world))
			return startSide;

		// TODO: Try pasting on the approximated side the player is looking at before doing this
		for (EnumFacing currentSide : EnumFacing.VALUES) {

			if (currentSide != startSide && canPasteOnSideOfBlock(currentSide.getOpposite(), pos.offset(currentSide), world))
				return currentSide;
		}

		return null;
	}

	public static boolean canPasteOnSideOfBlock(EnumFacing side, BlockPos pos, World world) {
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

	public void updateSurroundingBlocks(BlockPos pos, World world) {

		for (EnumFacing direction : EnumFacing.VALUES) {
			BlockPos neighbourPos = pos.offset(direction);
			world.notifyBlockOfStateChange(neighbourPos, this);
			world.notifyBlockOfStateChange(neighbourPos.offset(direction), this);
		}

		for (EnumFacing2 direction : EnumFacing2.VALUES)
			world.notifyBlockOfStateChange(direction.offsetBlockPos(pos), this);
	}

	public boolean updatePower(BlockPos pos, World world) {
		byte power = PowerLookup.getPower(pos, world);
		int newPower = power;
		this.canProvidePower = false;
		int indirectPower = world.isBlockIndirectlyGettingPowered(pos);
		this.canProvidePower = true;

		if (indirectPower > 0 && indirectPower > newPower - 1)
			newPower = indirectPower;

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

		if (k > newPower)
			newPower = k - 1;
		else if (newPower > 0)
			--newPower;
		else
			newPower = 0;

		if (indirectPower > newPower - 1)
			newPower = indirectPower;

		if (power != newPower) {
			PowerLookup.putPower(pos, (byte) newPower, world);
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
			this.updatePower(pos, world);
			this.updateSurroundingBlocks(pos, world);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);

		if (!world.isRemote) {
			ModelLookup.removeModel(pos, world);
			PowerLookup.removePower(pos, world);
			this.updateSurroundingBlocks(pos, world);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block triggerBlock) {

		if (!world.isRemote) {
			BlockRedstonePasteWire block = this;
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

				world.setBlockState(pos, newState, 2);
				block = (BlockRedstonePasteWire) newState.getBlock();
			}

			if (block.updatePower(pos, world))
				block.updateSurroundingBlocks(pos, world);

			ModelLookup.putModel(pos, block.getModel(pos, world), world);
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
			byte power = PowerLookup.getPower(pos, (World) world);
			if (power == 0)
				return 0;
			else if (side == EnumFacing.UP)
				return power;
			else {
				EnumSet<EnumFacing> sidesProvidingPowerTo = EnumSet.noneOf(EnumFacing.class);

				for (EnumFacing horizontalSide : EnumFacing.HORIZONTALS) {

					if (this.canProvidePowerToSide(world, pos, horizontalSide))
						sidesProvidingPowerTo.add(horizontalSide);
				}

				if (side.getAxis().isHorizontal() && sidesProvidingPowerTo.isEmpty() || world.getBlockState(pos.offset(side.getOpposite())).getBlock() instanceof BlockRedstoneDiode)
					return power;
				else if (sidesProvidingPowerTo.contains(side) && !sidesProvidingPowerTo.contains(side.rotateYCCW()) && !sidesProvidingPowerTo.contains(side.rotateY()))
					return power;
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

		if (world.getBlockState(pos).getBlock() != this)
			return super.colorMultiplier(world, pos, renderPass);
		else if (isDebugWorld)
			return RedstonePasteRenderer.calculateColour((byte) (int) (Integer) world.getBlockState(pos).getValue(POWER));
		else
			return RedstonePasteRenderer.calculateColour(PowerLookup.getPower(pos, (World) world));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		byte power = (isDebugWorld) ? (byte) (int) (Integer) world.getBlockState(pos).getValue(POWER) : PowerLookup.getPower(pos, (World) world);
		if (power != 0) {
			float powerPercentage = power / 15f;
			float red = powerPercentage * 0.6f + 0.4f;
			float green = Math.max(0, powerPercentage * powerPercentage * 0.7f - 0.5f);
			float blue = Math.max(0, powerPercentage * powerPercentage * 0.6f - 0.7f);

			for (EnumFacing side : this.getPastedSides(state)) {
				double x = (side.getAxis() != Axis.X) ? pos.getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.2
						: (side.getAxisDirection() == AxisDirection.NEGATIVE) ? pos.getX() + 1 / 16.0 : pos.getX() + 15 / 16.0;

				double y = (side.getAxis() != Axis.Y) ? pos.getY() + 0.5 + (rand.nextFloat() - 0.5) * 0.2
						: (side.getAxisDirection() == AxisDirection.NEGATIVE) ? pos.getY() + 1 / 16.0 : pos.getY() + 15 / 16.0;

				double z = (side.getAxis() != Axis.Z) ? pos.getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.2
						: (side.getAxisDirection() == AxisDirection.NEGATIVE) ? pos.getZ() + 1 / 16.0 : pos.getZ() + 15 / 16.0;

				world.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, red, green, blue);
			}
		}
	}
}
