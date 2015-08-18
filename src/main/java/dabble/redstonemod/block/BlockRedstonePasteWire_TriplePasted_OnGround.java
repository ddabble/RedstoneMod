package dabble.redstonemod.block;

import java.util.EnumSet;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import dabble.redstonemod.init.ModBlocks;

public class BlockRedstonePasteWire_TriplePasted_OnGround extends BlockRedstonePasteWire {
	public static final PropertyEnum PASTEDSIDES = PropertyEnum.create("pasted_sides", EnumPasting.class);

	public BlockRedstonePasteWire_TriplePasted_OnGround(String unlocalisedName) {
		super(unlocalisedName, (byte) 3);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(PASTEDSIDES, EnumPasting.DUN)
				.withProperty(POWER, Integer.valueOf(0)));
	}

	@Override
	public EnumFacing[] getPastedSides(IBlockState state) {
		return ((EnumPasting) state.getValue(PASTEDSIDES)).sides;
	}

	@Override
	public EnumSet<EnumFacing> getPastedSidesSet(IBlockState state) {
		return EnumSet.copyOf(((EnumPasting) state.getValue(PASTEDSIDES)).sideSet);
	}

	@Override
	public boolean isPastedOnSide(EnumFacing side, IBlockState state) {
		return ((EnumPasting) state.getValue(PASTEDSIDES)).sideSet.contains(side);
	}

	@Override
	public IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state, BlockPos pos, EntityPlayer player, World world) {
		EnumFacing[] pastedSides = ((EnumPasting) state.getValue(PASTEDSIDES)).sides;

		if (side != pastedSides[0] && side != pastedSides[1] && side != pastedSides[2])
			return BlockRedstonePasteWire_QuadruplePasted.getStateFromSides(EnumSet.of(side, pastedSides));
		else
			return null;
	}

	static IBlockState getStateFromSides(EnumSet<EnumFacing> sides) {
		StringBuilder pasting = new StringBuilder(3);

		for (EnumFacing side : sides)
			pasting.append(side.getName().charAt(0));

		return ModBlocks.redstone_paste_triple_pasted_on_ground.getDefaultState().withProperty(PASTEDSIDES, EnumPasting.valueOf(pasting.toString().toUpperCase()));
	}

	@Override
	EnumSet<EnumFacing> getValidPastedSides(IBlockState state, BlockPos pos, World world) {
		EnumSet<EnumFacing> validPastedSides = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing pastedSide : ((EnumPasting) state.getValue(PASTEDSIDES)).sides) {

			if (canPasteOnSideOfBlock(pastedSide.getOpposite(), pos.offset(pastedSide), world))
				validPastedSides.add(pastedSide);
		}

		return validPastedSides;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(PASTEDSIDES, EnumPasting.getPasting(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumPasting) state.getValue(PASTEDSIDES)).ordinal();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { PASTEDSIDES, POWER });
	}

	private enum EnumPasting implements IStringSerializable {
		DUN(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH),
		DUS(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.SOUTH),
		DUW(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST),
		DUE(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.EAST),
		DNS(EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH),
		DNW(EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.WEST),
		DNE(EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.EAST),
		DSW(EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.WEST),
		DSE(EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.EAST),
		DWE(EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.EAST);

		private final EnumFacing[] sides;
		private final EnumSet<EnumFacing> sideSet;
		private final String name;

		private static final EnumPasting[] PASTING_LOOKUP = new EnumPasting[EnumPasting.values().length];

		private EnumPasting(EnumFacing side1, EnumFacing side2, EnumFacing side3) {
			this.sides = new EnumFacing[] { side1, side2, side3 };
			this.sideSet = EnumSet.of(side1, side2, side3);
			this.name = side1.getName() + ", " + side2.getName() + ", " + side3.getName();
		}

		@Override
		public String toString() {
			return this.getName();
		}

		@Override
		public String getName() {
			return this.name;
		}

		private static EnumPasting getPasting(int ordinal) {
			return PASTING_LOOKUP[ordinal];
		}

		static {
			for (EnumPasting pasting : EnumPasting.values())
				PASTING_LOOKUP[pasting.ordinal()] = pasting;
		}
	}
}
