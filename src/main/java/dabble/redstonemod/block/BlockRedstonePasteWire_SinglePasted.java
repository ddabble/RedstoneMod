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
				EnumFacing thirdSide = this.getThirdSideFromOpposites(side.getAxis(), pos, player, world);
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

	private EnumFacing getThirdSideFromOpposites(Axis axis, BlockPos pos, EntityPlayer player, World world) {
		EnumFacing facing = getFacingPastingDirection(axis, pos, player, world, false);

		if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
			return facing;

		if (axis == Axis.Y) {
			facing = getFacingPastingDirection(facing.getAxis(), pos, player, world, true);

			if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
				return facing;
		} else {

			if (facing.getAxis() == Axis.Y) {
				facing = getFacingPastingDirection(axis, pos, player, world, true);

				if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
					return facing;
			} else {
				facing = (player.rotationPitch > 0) ? EnumFacing.DOWN : EnumFacing.UP;

				if (canPasteOnSideOfBlock(facing.getOpposite(), pos.offset(facing), world))
					return facing;
			}
		}

		for (EnumFacing side : EnumFacing.VALUES) {

			if (side.getAxis() != axis && side != facing && canPasteOnSideOfBlock(side.getOpposite(), pos.offset(side), world))
				return side;
		}

		return null;
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
