package party.dabble.redstonemod.block;

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
import party.dabble.redstonemod.init.ModBlocks;

public class BlockRedstonePasteWire_QuadruplePasted extends BlockRedstonePasteWire {
	public static final PropertyEnum PASTEDSIDES = PropertyEnum.create("pasted_sides", EnumPasting.class);

	public BlockRedstonePasteWire_QuadruplePasted(String unlocalisedName) {
		super(unlocalisedName, (byte)4);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(PASTEDSIDES, EnumPasting.DUNS)
				.withProperty(POWER, Integer.valueOf(0)));
	}

	@Override
	public EnumFacing[] getPastedSides(IBlockState state) {
		return ((EnumPasting)state.getValue(PASTEDSIDES)).sides;
	}

	@Override
	public EnumSet<EnumFacing> getPastedSidesSet(IBlockState state) {
		return EnumSet.copyOf(((EnumPasting)state.getValue(PASTEDSIDES)).sideSet);
	}

	@Override
	public boolean isPastedOnSide(EnumFacing side, IBlockState state) {
		return ((EnumPasting)state.getValue(PASTEDSIDES)).sideSet.contains(side);
	}

	@Override
	public IBlockState pasteAdditionalSide(EnumFacing side, IBlockState state, BlockPos pos, EntityPlayer player, World world) {
		EnumFacing[] pastedSides = ((EnumPasting)state.getValue(PASTEDSIDES)).sides;

		if (side != pastedSides[0] && side != pastedSides[1] && side != pastedSides[2] && side != pastedSides[3])
			return BlockRedstonePasteWire_QuintuplePasted.getStateFromSides(EnumSet.of(side, pastedSides));
		else
			return null;
	}

	static IBlockState getStateFromSides(EnumSet<EnumFacing> sides) {
		StringBuilder pasting = new StringBuilder(4);

		for (EnumFacing side : sides)
			pasting.append(side.getName().charAt(0));

		return ModBlocks.redstone_paste_quadruple_pasted.getDefaultState().withProperty(PASTEDSIDES, EnumPasting.valueOf(pasting.toString().toUpperCase()));
	}

	@Override
	protected EnumSet<EnumFacing> getValidPastedSides(IBlockState state, BlockPos pos, World world) {
		EnumSet<EnumFacing> validPastedSides = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing pastedSide : ((EnumPasting)state.getValue(PASTEDSIDES)).sides) {

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
		return ((EnumPasting)state.getValue(PASTEDSIDES)).ordinal();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { PASTEDSIDES, POWER });
	}

	private enum EnumPasting implements IStringSerializable {
		DUNS(EnumFacing.WEST, EnumFacing.EAST),
		DUNW(EnumFacing.SOUTH, EnumFacing.EAST),
		DUNE(EnumFacing.SOUTH, EnumFacing.WEST),
		DUSW(EnumFacing.NORTH, EnumFacing.EAST),
		DUSE(EnumFacing.NORTH, EnumFacing.WEST),
		DUWE(EnumFacing.NORTH, EnumFacing.SOUTH),
		DNSW(EnumFacing.UP, EnumFacing.EAST),
		DNSE(EnumFacing.UP, EnumFacing.WEST),
		DNWE(EnumFacing.UP, EnumFacing.SOUTH),
		DSWE(EnumFacing.UP, EnumFacing.NORTH),
		UNSW(EnumFacing.DOWN, EnumFacing.EAST),
		UNSE(EnumFacing.DOWN, EnumFacing.WEST),
		UNWE(EnumFacing.DOWN, EnumFacing.SOUTH),
		USWE(EnumFacing.DOWN, EnumFacing.NORTH),
		NSWE(EnumFacing.DOWN, EnumFacing.UP);

		private final EnumFacing[] sides;
		private final EnumSet<EnumFacing> sideSet;
		private final String name;

		private static final EnumPasting[] PASTING_LOOKUP = new EnumPasting[EnumPasting.values().length];

		private EnumPasting(EnumFacing missingSide1, EnumFacing missingSide2) {
			EnumFacing[] sides = new EnumFacing[4];
			EnumSet<EnumFacing> sideSet = EnumSet.noneOf(EnumFacing.class);
			StringBuilder name = new StringBuilder();

			byte i = 0;
			for (EnumFacing side : EnumFacing.VALUES) {

				if (side != missingSide1 && side != missingSide2) {
					sides[i++] = side;
					sideSet.add(side);
					name.append(side.getName()).append(", ");
				}
			}

			this.sides = sides;
			this.sideSet = sideSet;
			this.name = name.substring(0, name.length() - 2);
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
