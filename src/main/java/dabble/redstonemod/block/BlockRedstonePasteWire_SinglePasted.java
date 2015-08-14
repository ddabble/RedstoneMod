package dabble.redstonemod.block;

import java.util.EnumSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;
import dabble.redstonemod.init.ModBlocks;

public class BlockRedstonePasteWire_SinglePasted extends BlockRedstonePasteWire {
	public static final PropertyDirection PASTEDSIDE = PropertyDirection.create("pasted_side");

	public BlockRedstonePasteWire_SinglePasted(String unlocalisedName) {
		super(unlocalisedName, (byte) 1);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(PASTEDSIDE, EnumFacing.DOWN)
				.withProperty(POWER, Integer.valueOf(0)));
	}

	@Override
	public EnumFacing[] getPastedSides(IBlockState state) {
		return new EnumFacing[] { (EnumFacing) state.getValue(PASTEDSIDE) };
	}

	@Override
	public EnumSet<EnumFacing> getPastedSidesSet(IBlockState state) {
		return EnumSet.of((EnumFacing) state.getValue(PASTEDSIDE));
	}

	@Override
	public boolean isPastedOnSide(EnumFacing side, IBlockState state) {
		return side == (EnumFacing) state.getValue(PASTEDSIDE);
	}

	@Override
	public IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state, BlockPos pos, EntityPlayer player, World world) {
		EnumFacing pastedSide = (EnumFacing) state.getValue(PASTEDSIDE);
		if (side != pastedSide) {

			if (side != pastedSide.getOpposite())
				return BlockRedstonePasteWire_DoublePasted.getStateFromSides(EnumSet.of(side, pastedSide));
			else {
				EnumFacing thirdSide = this.getThirdSideFromOpposites(side, pastedSide, pos, player, world);
				if (thirdSide != null) {

					if (side == EnumFacing.DOWN || pastedSide == EnumFacing.DOWN || thirdSide == EnumFacing.DOWN)
						return BlockRedstonePasteWire_TriplePasted_OnGround.getStateFromSides(EnumSet.of(side, pastedSide, thirdSide));
					else
						return BlockRedstonePasteWire_TriplePasted_OnWalls.getStateFromSides(EnumSet.of(side, pastedSide, thirdSide));
				}
			}
		}

		return null;
	}

	// TODO: Pls make me pretty, thx.
	private EnumFacing getThirdSideFromOpposites(EnumFacing side, EnumFacing pastedSide, BlockPos currentPos, EntityPlayer player, World world) {
		EnumFacing facingPastingDirection = this.getFacingPastingDirection(pastedSide, currentPos, player, world);

		if (canPasteOnSideOfBlock(facingPastingDirection.getOpposite(), currentPos.offset(facingPastingDirection), world))
			return facingPastingDirection;
		else {

			if (pastedSide.getAxis() == Axis.Y) {
				float yaw = player.rotationYaw / 90;
				EnumFacing facing = EnumFacing.fromAngle(Math.floor(yaw) * 90);

				if (facing != facingPastingDirection && canPasteOnSideOfBlock(facing.getOpposite(), currentPos.offset(facing), world))
					return facing;

				facing = EnumFacing.fromAngle(Math.ceil(yaw) * 90);

				if (facing != facingPastingDirection && canPasteOnSideOfBlock(facing.getOpposite(), currentPos.offset(facing), world))
					return facing;
			} else {
				double eyePosY = player.posY + player.getEyeHeight();

				if (eyePosY > currentPos.getY() + 0.5 && canPasteOnSideOfBlock(EnumFacing.UP, currentPos.down(), world))
					return EnumFacing.DOWN;
				else if (canPasteOnSideOfBlock(EnumFacing.DOWN, currentPos.up(), world))
					return EnumFacing.UP;
			}

			for (EnumFacing facing : EnumFacing.VALUES) {

				if (facing.getAxis() != pastedSide.getAxis() && canPasteOnSideOfBlock(facing.getOpposite(), currentPos.offset(facing), world))
					return facing;
			}
		}

		return null;
	}

	private EnumFacing getFacingPastingDirection(EnumFacing pastedSide, BlockPos currentPos, EntityPlayer player, World world) {

		if (Math.abs(player.posX - currentPos.getX()) < 2 && Math.abs(player.posZ - currentPos.getZ()) < 2 && pastedSide.getAxis() != Axis.Y) {
			double eyePosY = player.posY + player.getEyeHeight();

			if (eyePosY - currentPos.getY() > 2)
				return EnumFacing.DOWN;
			else if (currentPos.getY() - eyePosY > 0)
				return EnumFacing.UP;
		}

		EnumFacing horizontalFacing = player.getHorizontalFacing();
		if (horizontalFacing.getAxis() != pastedSide.getAxis())
			return horizontalFacing;

		if (pastedSide.getAxis() == Axis.X) {

			if (player.posZ > currentPos.getZ())
				return EnumFacing.NORTH;
			else
				return EnumFacing.SOUTH;
		} else {

			if (player.posX > currentPos.getX())
				return EnumFacing.WEST;
			else
				return EnumFacing.EAST;
		}
	}

	public static IBlockState getStateFromSide(EnumFacing side) {
		return ModBlocks.redstone_paste_single_pasted.getDefaultState().withProperty(BlockRedstonePasteWire_SinglePasted.PASTEDSIDE, side);
	}

	@Override
	EnumSet<EnumFacing> getValidPastedSides(IBlockState state, BlockPos pos, World world) {
		EnumFacing pastedSide = (EnumFacing) state.getValue(PASTEDSIDE);

		if (canPasteOnSideOfBlock(pastedSide.getOpposite(), pos.offset(pastedSide), world))
			return EnumSet.of(pastedSide);
		else
			return EnumSet.noneOf(EnumFacing.class);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(PASTEDSIDE, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing) state.getValue(PASTEDSIDE)).getIndex();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { PASTEDSIDE, POWER });
	}
}
