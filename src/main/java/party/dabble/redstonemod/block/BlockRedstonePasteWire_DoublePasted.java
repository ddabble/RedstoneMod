package party.dabble.redstonemod.block;

import java.util.EnumSet;
import java.util.Iterator;

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

public class BlockRedstonePasteWire_DoublePasted extends BlockRedstonePasteWire {
	public static final PropertyEnum PASTEDSIDES = PropertyEnum.create("pasted_sides", EnumPasting.class);

	public BlockRedstonePasteWire_DoublePasted(String unlocalisedName) {
		super(unlocalisedName, (byte)2);
		this.setDefaultState(this.blockState.getBaseState()
				.withProperty(PASTEDSIDES, EnumPasting.DN)
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
		if (side != pastedSides[0] && side != pastedSides[1]) {

			if (side == EnumFacing.DOWN || pastedSides[0] == EnumFacing.DOWN || pastedSides[1] == EnumFacing.DOWN)
				return BlockRedstonePasteWire_TriplePasted_OnGround.getStateFromSides(EnumSet.of(side, pastedSides));
			else
				return BlockRedstonePasteWire_TriplePasted_OnWalls.getStateFromSides(EnumSet.of(side, pastedSides));
		} else
			return null;
	}

	static IBlockState getStateFromSides(EnumSet<EnumFacing> sides) {
		Iterator<EnumFacing> sideIterator = sides.iterator();

		if (sideIterator.next() == sideIterator.next().getOpposite())
			return BlockRedstonePasteWire_SinglePasted.getStateFromSide(sides.iterator().next());

		StringBuilder pasting = new StringBuilder(2);

		for (EnumFacing side : sides)
			pasting.append(side.getName().charAt(0));

		return ModBlocks.redstone_paste_double_pasted.getDefaultState().withProperty(PASTEDSIDES, EnumPasting.valueOf(pasting.toString().toUpperCase()));
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
		DN(EnumFacing.DOWN, EnumFacing.NORTH),
		DS(EnumFacing.DOWN, EnumFacing.SOUTH),
		DW(EnumFacing.DOWN, EnumFacing.WEST),
		DE(EnumFacing.DOWN, EnumFacing.EAST),
		UN(EnumFacing.UP, EnumFacing.NORTH),
		US(EnumFacing.UP, EnumFacing.SOUTH),
		UW(EnumFacing.UP, EnumFacing.WEST),
		UE(EnumFacing.UP, EnumFacing.EAST),
		NW(EnumFacing.NORTH, EnumFacing.WEST),
		NE(EnumFacing.NORTH, EnumFacing.EAST),
		SW(EnumFacing.SOUTH, EnumFacing.WEST),
		SE(EnumFacing.SOUTH, EnumFacing.EAST);

		private final EnumFacing[] sides;
		private final EnumSet<EnumFacing> sideSet;
		private final String name;

		private static final EnumPasting[] PASTING_LOOKUP = new EnumPasting[EnumPasting.values().length];

		private EnumPasting(EnumFacing side1, EnumFacing side2) {
			this.sides = new EnumFacing[] { side1, side2 };
			this.sideSet = EnumSet.of(side1, side2);
			this.name = side1.getName() + ", " + side2.getName();
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
