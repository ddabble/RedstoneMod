package dabble.redstonemod.block;

import java.util.EnumSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import dabble.redstonemod.init.ModBlocks;

public class BlockRedstonePasteWire_SinglePasted extends BlockRedstonePasteWire {
	public static final PropertyDirection PASTEDSIDE = PropertyDirection.create("pasted_side");

	public BlockRedstonePasteWire_SinglePasted(String unlocalisedName) {
		super(unlocalisedName, (byte) 1);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(PASTEDSIDE, EnumFacing.DOWN));
	}

	public EnumFacing[] getPastedSides(IBlockState state) {
		return new EnumFacing[] { (EnumFacing) state.getValue(PASTEDSIDE) };
	}

	@Override
	public IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state) {
		EnumFacing pastedSide = (EnumFacing) state.getValue(PASTEDSIDE);
		if (pastedSide != side)
			return BlockRedstonePasteWire_DoublePasted.getStateFromSides(EnumSet.of(side, pastedSide));
		else
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
		return new BlockState(this, new IProperty[] { PASTEDSIDE });
	}
}
