package party.dabble.redstonemod.block;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRedstoneDiode;
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
import party.dabble.redstonemod.init.ModItems;
import party.dabble.redstonemod.rendering.RayTracing;
import party.dabble.redstonemod.rendering.RedstonePasteRenderer;
import party.dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import party.dabble.redstonemod.util.EnumFacing2;
import party.dabble.redstonemod.util.EnumModel;
import party.dabble.redstonemod.util.ModelLookup;
import party.dabble.redstonemod.util.PowerLookup;

public abstract class BlockRedstonePasteWire extends Block implements ITileEntityProvider {
	/**
	 * Only for debugging, like displaying power on the debug screen, and making the game display every possible block state in a debug world.
	 */
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	public static boolean isDebugWorld = false;
	private final EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides = new EnumMap<EnumFacing, EnumSet<EnumFacing>>(EnumFacing.class);
	private final EnumSet<EnumFacing> recheckDirections = EnumSet.noneOf(EnumFacing.class);
	private final byte numberOfPastedSides;

	public abstract EnumFacing[] getPastedSides(IBlockState state);

	public abstract EnumSet<EnumFacing> getPastedSidesSet(IBlockState state);

	public abstract boolean isPastedOnSide(EnumFacing side, IBlockState state);

	public abstract IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state, BlockPos pos, EntityPlayer player, World world);

	protected abstract EnumSet<EnumFacing> getValidPastedSides(IBlockState state, BlockPos pos, World world);

	public BlockRedstonePasteWire(String unlocalisedName, byte numberOfPastedSides) {
		super(Material.circuits);
		this.setUnlocalizedName(unlocalisedName);
		this.setHardness(0);
		this.setStepSound(Block.soundTypeStone);
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
		return state.withProperty(POWER, Integer.valueOf(PowerLookup.getPower(pos, (World)world)));
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
		return RayTracing.collisionRayTrace(start, end, pos, world, super.collisionRayTrace(world, pos, start, end), this.getPastedSidesSet(world.getBlockState(pos)));
	}

	public static EnumFacing getFirstPasteableSide(EnumFacing sideLookingAt, BlockPos pos, EntityPlayer player, World world) {

		if (canPasteOnSideOfBlock(sideLookingAt.getOpposite(), pos.offset(sideLookingAt), world))
			return sideLookingAt;

		EnumFacing facing = getFacingPastingDirection(sideLookingAt.getAxis(), pos.offset(sideLookingAt), player, world, false);

		if (facing != sideLookingAt && canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
			return facing;

		if (facing.getAxis() == Axis.Y) {
			facing = getFacingPastingDirection(sideLookingAt.getAxis(), pos.offset(sideLookingAt), player, world, true);

			if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
				return facing;
		}

		if (sideLookingAt.getAxis() != Axis.Y) {

			facing = (player.rotationPitch > 0) ? EnumFacing.DOWN : EnumFacing.UP;

			if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
				return facing;
		}

		for (EnumFacing side : EnumFacing.VALUES) {

			if (side != sideLookingAt && side != facing && canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world))
				return side;
		}

		return null;
	}

	public static boolean canPasteOnSideOfBlock(EnumFacing side, BlockPos pos, World world) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block.isSolidFullCube())
			return true;

		if (block.getMaterial() == Material.circuits)
			return false;

		// TODO: Review the glowstone mechanics
		if (block == Blocks.glowstone)
			return true;

		if (block instanceof BlockSlab) {
			state = block.getActualState(state, world, pos);
			return (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP && side == EnumFacing.UP)
					|| (state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN)
					|| block.isFullBlock();
		}

		if (block instanceof BlockStairs) {
			state = block.getActualState(state, world, pos);
			boolean flipped = state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP;

			if (side.getHorizontalIndex() < 0)
				return (side == EnumFacing.UP) ? flipped : !flipped;

			BlockStairs.EnumShape shape = (BlockStairs.EnumShape)state.getValue(BlockStairs.SHAPE);
			EnumFacing facing = (EnumFacing)state.getValue(BlockStairs.FACING);
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
			return ((Integer)block.getActualState(state, world, pos).getValue(BlockSnow.LAYERS)) >= 8;

		if (block instanceof BlockFarmland)
			return side == EnumFacing.DOWN;

		return false;
	}

	/**
	 * Returns the side that the player is most likely expecting to be pasted.
	 * <p>
	 * If a horizontal direction is returned, it is guaranteed to be the direction the player is looking the most toward while still not being on the same axis as the side clicked.
	 */
	protected static EnumFacing getFacingPastingDirection(Axis clickedAxis, BlockPos clickedPos, EntityPlayer player, World world, boolean onlyHorizontals) {

		if (!onlyHorizontals && clickedAxis != Axis.Y && Math.abs(player.posX - (clickedPos.getX() + 0.5)) < 1.5 && Math.abs(player.posZ - (clickedPos.getZ() + 0.5)) < 1.5) {
			double eyePosY = player.posY + player.getEyeHeight();

			if (eyePosY - clickedPos.getY() > 1.5)
				return EnumFacing.DOWN;
			else if (clickedPos.getY() - eyePosY > 0)
				return EnumFacing.UP;
		}

		EnumFacing horizontalFacing = player.getHorizontalFacing();
		if (horizontalFacing.getAxis() != clickedAxis)
			return horizontalFacing;

		float yaw = player.rotationYaw / 90;
		EnumFacing otherHorizontalFacing = EnumFacing.fromAngle(Math.floor(yaw) * 90);
		if (otherHorizontalFacing != horizontalFacing)
			return otherHorizontalFacing;
		else
			return EnumFacing.fromAngle(Math.ceil(yaw) * 90);
	}

	private boolean updateModel(BlockPos pos, World world) {
		EnumMap<EnumFacing, EnumModel> model = this.getModel(pos, world);
		if (!model.equals(ModelLookup.getModel(pos, world))) {
			ModelLookup.putModel(pos, model, world);
			return true;
		} else
			return false;
	}

	public EnumMap<EnumFacing, EnumModel> getModel(BlockPos pos, World world) {
		IBlockState state = world.getBlockState(pos);

		for (EnumFacing2 direction : EnumFacing2.VALUES)
			this.checkBlockInDirection(direction, state, pos, world);

		for (EnumFacing direction : EnumFacing.VALUES)
			this.checkBlockInDirection(direction, state, pos, world);

		for (EnumFacing direction : this.recheckDirections)
			this.recheckDirection(direction, state, pos, world);

		EnumMap<EnumFacing, EnumModel> model = EnumModel.getModelFromExternalConnections(this.connectionSides, this.getPastedSidesSet(state));
		this.connectionSides.clear();
		this.recheckDirections.clear();
		return model;
	}

	private void checkBlockInDirection(EnumFacing2 direction, IBlockState state, BlockPos pos, World world) {
		IBlockState neighbourState = world.getBlockState(direction.offsetBlockPos(pos));
		if (neighbourState.getBlock() instanceof BlockRedstonePasteWire) {
			BlockRedstonePasteWire neighbourBlock = (BlockRedstonePasteWire)neighbourState.getBlock();

			if (!world.getBlockState(pos.offset(direction.facing1)).getBlock().isSolidFullCube()) {
				boolean isPastedOpposite = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing1, neighbourState);

				if ((!isPastedOpposite && this.isPastedOnSide(direction.facing2, state)) || neighbourBlock.isPastedOnSide(direction.facing1.getOpposite(), neighbourState))
					this.addConnection(direction.facing2, direction.facing1, state, pos, world, false);
			} else if (!world.getBlockState(pos.offset(direction.facing2)).getBlock().isSolidFullCube()) {
				boolean isPastedOpposite = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing2, neighbourState);

				if ((!isPastedOpposite && this.isPastedOnSide(direction.facing1, state)) || neighbourBlock.isPastedOnSide(direction.facing2.getOpposite(), neighbourState))
					this.addConnection(direction.facing1, direction.facing2, state, pos, world, false);
			}
		}
	}

	private void checkBlockInDirection(EnumFacing direction, IBlockState state, BlockPos pos, World world) {
		BlockPos neighbourPos = pos.offset(direction);
		IBlockState neighbourState = world.getBlockState(neighbourPos);
		Block neighbourBlock = neighbourState.getBlock();
		if (neighbourBlock instanceof BlockRedstonePasteWire) {
			EnumSet<EnumFacing> pastedSides = this.getPastedSidesSet(state);
			EnumSet<EnumFacing> neighbourPastedSides = ((BlockRedstonePasteWire)neighbourBlock).getPastedSidesSet(neighbourState);
			boolean hasConnected = false;

			for (EnumFacing side : neighbourPastedSides) {

				if (pastedSides.contains(side)) {
					this.addConnection(side, direction, state, pos, world, true);
					hasConnected = true;
				}
			}

			if (!hasConnected) {

				for (EnumFacing side : EnumFacing.VALUES) {

					if (pastedSides.contains(side)) {
						boolean isPastedOpposite = neighbourPastedSides.size() == 1 && side == neighbourPastedSides.iterator().next().getOpposite();

						if (!isPastedOpposite && canPasteOnSideOfBlock(side.getOpposite(), neighbourPos.offset(side), world)) {
							this.addConnection(side, direction, state, pos, world, true);
							return;
						}
					} else if (neighbourPastedSides.contains(side)) {
						boolean isPastedOpposite = pastedSides.size() == 1 && side == pastedSides.iterator().next().getOpposite();

						if (!isPastedOpposite && canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world)) {
							this.addConnection(side, direction, state, pos, world, false);
							return;
						}
					}
				}
			}

			return;
		} else if (neighbourBlock == Blocks.redstone_wire) {

			if (direction.getAxis() != Axis.Y && isRedstoneWirePoweringDirection(direction.getOpposite(), neighbourPos, world))
				this.addConnection(EnumFacing.DOWN, direction, state, pos, world, false);

			return;
		}

		if (!neighbourBlock.canProvidePower())
			return;

		if (neighbourBlock instanceof BlockRedstoneDiode) {

			if (direction.getAxis() == ((EnumFacing)neighbourState.getValue(BlockRedstoneDiode.FACING)).getAxis())
				this.addConnection(EnumFacing.DOWN, direction, state, pos, world, false);

			return;
		} else if (this.numberOfPastedSides == 1 && this.isPastedOnSide(direction.getOpposite(), state)) {
			// TODO: Find other uses of attachedSide's value (if it's not null)
			EnumFacing attachedSide = getAttachedSideOfBlockProvidingPower(neighbourState, neighbourBlock);
			if (attachedSide != null)
				this.recheckDirections.add(direction);

			return;
		}

		for (EnumFacing pastedSide : this.getPastedSides(state)) {

			if (pastedSide.getAxis() != direction.getAxis())
				this.addConnection(pastedSide, direction, state, pos, world, true);
		}
	}

	private static boolean isRedstoneWirePoweringDirection(EnumFacing direction, BlockPos pos, World world) {
		direction = direction.rotateYCCW();
		BlockPos leftPos = pos.offset(direction);

		if (canRedstoneConnectToBlock(direction, leftPos, world) || canRedstoneConnectToBlock(null, leftPos.up(), world))
			return false;
		else if (!world.getBlockState(leftPos).getBlock().isSolidFullCube() && canRedstoneConnectToBlock(null, leftPos.down(), world))
			return false;

		direction = direction.getOpposite();
		BlockPos rightPos = pos.offset(direction);

		if (canRedstoneConnectToBlock(direction, rightPos, world) || canRedstoneConnectToBlock(null, rightPos.up(), world))
			return false;
		else if (!world.getBlockState(rightPos).getBlock().isSolidFullCube() && canRedstoneConnectToBlock(null, rightPos.down(), world))
			return false;

		return true;
	}

	private static boolean canRedstoneConnectToBlock(EnumFacing side, BlockPos pos, IBlockAccess world) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block == Blocks.redstone_wire)
			return true;
		else if (side != null && block instanceof BlockRedstoneDiode)
			return ((EnumFacing)state.getValue(BlockRedstoneDiode.FACING)).getAxis() == side.getAxis();
		else
			return block.canConnectRedstone(world, pos, side) && block != Blocks.redstone_wire;
	}

	private static EnumFacing getAttachedSideOfBlockProvidingPower(IBlockState state, Block block) {
		EnumFacing side = null;

		if (block instanceof BlockRedstoneTorch)
			side = (EnumFacing)state.getValue(BlockRedstoneTorch.FACING);
		else if (block instanceof BlockLever)
			side = ((EnumOrientation)state.getValue(BlockLever.FACING)).getFacing();
		else if (block instanceof BlockButton)
			side = (EnumFacing)state.getValue(BlockButton.FACING);
		else if (block instanceof BlockBasePressurePlate)
			side = EnumFacing.DOWN;
		else if (block instanceof BlockRailDetector) {

			switch ((EnumRailDirection)state.getValue(BlockRailDetector.SHAPE)) {
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
			side = (EnumFacing)state.getValue(BlockTripWireHook.FACING);

		return (side != null) ? side.getOpposite() : null;
	}

	private void recheckDirection(EnumFacing direction, IBlockState state, BlockPos pos, World world) {

		for (EnumFacing side : this.connectionSides.keySet()) {

			if (side.getAxis() != direction.getAxis()) {
				this.addConnection(side, direction, state, pos, world, true);
				return;
			}
		}
	}

	private void addConnection(EnumFacing side, EnumFacing connection, IBlockState state, BlockPos pos, World world, boolean isGuaranteedPasted) {

		if (!isGuaranteedPasted) {
			boolean isPastedOpposite = this.numberOfPastedSides == 1 && this.isPastedOnSide(side.getOpposite(), state);

			if (isPastedOpposite || !canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world))
				return;
		}

		if (this.connectionSides.containsKey(side))
			this.connectionSides.get(side).add(connection);
		else
			this.connectionSides.put(side, EnumSet.of(connection));
	}

	public boolean updatePower(BlockPos pos, World world) {
		byte power = PowerLookup.getPower(pos, world);
		byte strongestNeighbouringPower = (byte)(isBlockIndirectlyGettingPowered(pos, world) + 1);

		for (EnumFacing direction : EnumFacing.VALUES) {

			if (strongestNeighbouringPower > 14)
				break;

			BlockPos neighbourPos = pos.offset(direction);
			Block block = world.getBlockState(neighbourPos).getBlock();
			byte neighbourPower = 0;

			if (block instanceof BlockRedstonePasteWire)
				neighbourPower = PowerLookup.getPower(neighbourPos, world);
			else if (block == Blocks.redstone_wire)
				neighbourPower = (byte)block.isProvidingWeakPower(world, neighbourPos, world.getBlockState(neighbourPos), direction);

			if (neighbourPower > strongestNeighbouringPower)
				strongestNeighbouringPower = neighbourPower;
		}

		for (EnumFacing2 direction : EnumFacing2.VALUES) {

			if (strongestNeighbouringPower > 14)
				break;

			if (this.isConnectedToBlockInDirection(direction, pos, world)) {
				byte neighbourPower = PowerLookup.getPower(direction.offsetBlockPos(pos), world);

				if (neighbourPower > strongestNeighbouringPower)
					strongestNeighbouringPower = neighbourPower;
			}
		}

		if (power != --strongestNeighbouringPower) {
			PowerLookup.putPower(pos, strongestNeighbouringPower, world);
			return true;
		} else
			return false;
	}

	private static int isBlockIndirectlyGettingPowered(BlockPos pos, World world) {
		IBlockState state = world.getBlockState(pos);
		int strongestPower = 0;

		for (EnumFacing direction : EnumFacing.VALUES)
			strongestPower = Math.max(strongestPower, getPowerFromDirection(direction, state, pos, world));

		return Math.min(strongestPower, 15);
	}

	private static int getPowerFromDirection(EnumFacing direction, IBlockState state, BlockPos pos, World world) {
		BlockPos neighbourPos = pos.offset(direction);
		IBlockState neighbourState = world.getBlockState(neighbourPos);
		Block neighbourBlock = neighbourState.getBlock();
		if (neighbourBlock instanceof BlockRedstonePasteWire || neighbourBlock == Blocks.redstone_wire)
			return 0;

		if (neighbourBlock.shouldCheckWeakPower(world, neighbourPos, direction))
			return getPowerFromNormalCube(neighbourPos, world);
		else if (neighbourBlock instanceof BlockRedstoneDiode)
			return (ModelLookup.getModel(pos, world).containsKey(EnumFacing.DOWN)) ? neighbourBlock.isProvidingWeakPower(world, neighbourPos, neighbourState, direction) : 0;
		else if (neighbourBlock.isFullCube() || ModelLookup.isModelPointingInDirection(direction, pos, world))
			return neighbourBlock.isProvidingWeakPower(world, neighbourPos, neighbourState, direction);
		else
			return 0;
	}

	private static int getPowerFromNormalCube(BlockPos pos, World world) {
		int strongestPower = 0;

		for (EnumFacing direction : EnumFacing.VALUES) {
			BlockPos neighbourPos = pos.offset(direction);
			IBlockState state = world.getBlockState(neighbourPos);
			Block block = state.getBlock();
			if (block instanceof BlockRedstonePasteWire || block == Blocks.redstone_wire)
				continue;

			strongestPower = Math.max(strongestPower, block.isProvidingStrongPower(world, neighbourPos, state, direction));
		}

		return strongestPower;
	}

	private boolean isConnectedToBlockInDirection(EnumFacing2 direction, BlockPos pos, World world) {
		IBlockState state = world.getBlockState(pos);
		IBlockState neighbourState = world.getBlockState(direction.offsetBlockPos(pos));
		if (neighbourState.getBlock() instanceof BlockRedstonePasteWire) {
			BlockRedstonePasteWire neighbourBlock = (BlockRedstonePasteWire)neighbourState.getBlock();

			if (!world.getBlockState(pos.offset(direction.facing1)).getBlock().isSolidFullCube()) {
				boolean isPastedOpposite = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing1, neighbourState);

				return (!isPastedOpposite && this.isPastedOnSide(direction.facing2, state)) || neighbourBlock.isPastedOnSide(direction.facing1.getOpposite(), neighbourState);
			} else if (!world.getBlockState(pos.offset(direction.facing2)).getBlock().isSolidFullCube()) {
				boolean isPastedOpposite = neighbourBlock.numberOfPastedSides == 1 && neighbourBlock.isPastedOnSide(direction.facing2, neighbourState);

				return (!isPastedOpposite && this.isPastedOnSide(direction.facing1, state)) || neighbourBlock.isPastedOnSide(direction.facing2.getOpposite(), neighbourState);
			}
		}

		return false;
	}

	public void updateSurroundingBlocks(BlockPos pos, World world) {

		if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), EnumSet.allOf(EnumFacing.class)).isCanceled())
			return;

		for (EnumFacing direction : EnumFacing.VALUES) {
			BlockPos neighbourPos = pos.offset(direction);
			// IBlockState state = world.getBlockState(neighbourPos);
			// state.getBlock().onNeighborBlockChange(world, neighbourPos, state, this);
			world.notifyBlockOfStateChange(neighbourPos, this);

			// neighbourPos = neighbourPos.offset(direction);
			// state = world.getBlockState(neighbourPos);
			// state.getBlock().onNeighborBlockChange(world, neighbourPos, state, this);
			world.notifyBlockOfStateChange(neighbourPos.offset(direction), this);
		}

		for (EnumFacing2 direction : EnumFacing2.VALUES) {
			// BlockPos neighbourPos = direction.offsetBlockPos(pos);
			// IBlockState state = world.getBlockState(neighbourPos);
			// state.getBlock().onNeighborBlockChange(world, neighbourPos, state, this);
			world.notifyBlockOfStateChange(direction.offsetBlockPos(pos), this);
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {

		if (!world.isRemote) {
			this.updateModel(pos, world);
			this.updatePower(pos, world);
			this.updateSurroundingBlocks(pos, world);
		}
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {

		if (!world.isRemote && player.isSneaking()) {
			RayTracing.removingPlayer = player;
			MovingObjectPosition blockLookingAt = RayTracing.rayTraceFromPlayer(player);
			RayTracing.removingPlayer = null;
			if (blockLookingAt != null) {
				IBlockState state = world.getBlockState(pos);
				EnumSet<EnumFacing> pastedSides = this.getPastedSidesSet(state);
				if (!pastedSides.contains(blockLookingAt.hitInfo))
					return false;
				else {
					pastedSides.remove(blockLookingAt.hitInfo);

					switch (pastedSides.size()) {
						case 0:
							return world.setBlockToAir(pos);

						case 1:
							state = BlockRedstonePasteWire_SinglePasted.getStateFromSide(pastedSides.iterator().next());
							break;

						case 2:
							Iterator<EnumFacing> sideIterator = pastedSides.iterator();
							if (sideIterator.next() == sideIterator.next().getOpposite())
								return world.setBlockToAir(pos);

							state = BlockRedstonePasteWire_DoublePasted.getStateFromSides(pastedSides);
							break;

						case 3:
							if (pastedSides.contains(EnumFacing.DOWN))
								state = BlockRedstonePasteWire_TriplePasted_OnGround.getStateFromSides(pastedSides);
							else
								state = BlockRedstonePasteWire_TriplePasted_OnWalls.getStateFromSides(pastedSides);

							break;

						case 4:
							state = BlockRedstonePasteWire_QuadruplePasted.getStateFromSides(pastedSides);
							break;
					}

					world.setBlockState(pos, state, 2);
					BlockRedstonePasteWire block = (BlockRedstonePasteWire)state.getBlock();
					block.updateModel(pos, world);
					block.updatePower(pos, world);
					block.updateSurroundingBlocks(pos, world);
					return false;
				}
			}
		}

		return world.setBlockToAir(pos);
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
			EnumSet<EnumFacing> validPastedSides = this.getValidPastedSides(state, pos, world);
			if (validPastedSides.size() < this.numberOfPastedSides) {

				switch (validPastedSides.size()) {
					case 1:
						state = BlockRedstonePasteWire_SinglePasted.getStateFromSide(validPastedSides.iterator().next());
						break;

					case 2:
						state = BlockRedstonePasteWire_DoublePasted.getStateFromSides(validPastedSides);
						break;

					case 3:
						if (validPastedSides.contains(EnumFacing.DOWN))
							state = BlockRedstonePasteWire_TriplePasted_OnGround.getStateFromSides(validPastedSides);
						else
							state = BlockRedstonePasteWire_TriplePasted_OnWalls.getStateFromSides(validPastedSides);

						break;

					case 4:
						state = BlockRedstonePasteWire_QuadruplePasted.getStateFromSides(validPastedSides);
						break;

					case 0:
						this.dropBlockAsItem(world, pos, state, 0);
						world.setBlockToAir(pos);
						return;
				}

				world.setBlockState(pos, state, 2);
				BlockRedstonePasteWire block = (BlockRedstonePasteWire)state.getBlock();
				block.updateModel(pos, world);
				block.updatePower(pos, world);
				block.updateSurroundingBlocks(pos, world);
			} else {

				if (this.updateModel(pos, world) | this.updatePower(pos, world))
					this.updateSurroundingBlocks(pos, world);
			}
		}
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing direction) {
		direction = direction.getOpposite();
		byte power = PowerLookup.getPower(pos, (World)world);
		EnumMap<EnumFacing, EnumModel> model = ModelLookup.getModel(pos, (World)world);

		if (model.containsKey(direction) || ModelLookup.isModelPointingInDirection(direction, model))
			return power;

		return 0;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing direction) {
		direction = direction.getOpposite();
		byte power = PowerLookup.getPower(pos, (World)world);
		if (power == 0)
			return 0;

		EnumMap<EnumFacing, EnumModel> model = ModelLookup.getModel(pos, (World)world);
		if (model.containsKey(direction))
			return power;

		BlockPos originPos = pos.offset(direction);
		IBlockState askerState = world.getBlockState(originPos);
		Block blockAsking = askerState.getBlock();
		if (blockAsking instanceof BlockRedstoneDiode)
			return (model.containsKey(EnumFacing.DOWN)) ? power : 0;
		else if (blockAsking == Blocks.redstone_wire) {
			EnumModel downModel = model.get(EnumFacing.DOWN);
			return (downModel != null && (downModel == EnumModel.NONE || downModel.containsConnection(direction))) ? power - 1 : 0;
		} else
			return (ModelLookup.isModelPointingInDirection(direction, model)) ? power : 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing direction) {

		if (direction == null)
			return false;

		// This is so stupid
		if (world instanceof net.minecraft.world.ChunkCache)
			world = ((net.minecraft.world.ChunkCache)world).getTileEntity(pos).getWorld();

		return ModelLookup.getModel(pos, (World)world).containsKey(EnumFacing.DOWN) && isRedstoneWirePoweringDirection(direction, pos.offset(direction.getOpposite()), (World)world);
	}

	// TODO: Look into colorMultiplier() when fixing the breaking particles; it affects their colour
	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {

		if (world.getBlockState(pos).getBlock() != this)
			return super.colorMultiplier(world, pos, renderPass);
		else if (isDebugWorld)
			return RedstonePasteRenderer.calculateColour(((Integer)world.getBlockState(pos).getValue(POWER)).byteValue());
		else
			return RedstonePasteRenderer.calculateColour(PowerLookup.getPower(pos, (World)world));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		byte power = (isDebugWorld) ? ((Integer)world.getBlockState(pos).getValue(POWER)).byteValue() : PowerLookup.getPower(pos, world);
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
