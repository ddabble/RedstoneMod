package party.dabble.redstonemod.util;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public enum EnumFacing2 {
	DOWN_NORTH(0, EnumFacing.DOWN, EnumFacing.NORTH),
	DOWN_SOUTH(1, EnumFacing.DOWN, EnumFacing.SOUTH),
	DOWN_WEST(2, EnumFacing.DOWN, EnumFacing.WEST),
	DOWN_EAST(3, EnumFacing.DOWN, EnumFacing.EAST),

	UP_NORTH(4, EnumFacing.UP, EnumFacing.NORTH),
	UP_SOUTH(5, EnumFacing.UP, EnumFacing.SOUTH),
	UP_WEST(6, EnumFacing.UP, EnumFacing.WEST),
	UP_EAST(7, EnumFacing.UP, EnumFacing.EAST),

	NORTH_WEST(8, EnumFacing.NORTH, EnumFacing.WEST),
	NORTH_EAST(9, EnumFacing.NORTH, EnumFacing.EAST),
	SOUTH_WEST(10, EnumFacing.SOUTH, EnumFacing.WEST),
	SOUTH_EAST(11, EnumFacing.SOUTH, EnumFacing.EAST);

	private final int index;
	public final EnumFacing facing1;
	public final EnumFacing facing2;

	public static final EnumFacing2[] VALUES = new EnumFacing2[12];

	private EnumFacing2(int index, EnumFacing facing1, EnumFacing facing2) {
		this.index = index;
		this.facing1 = facing1;
		this.facing2 = facing2;
	}

	public BlockPos offsetBlockPos(BlockPos pos) {
		return new BlockPos(pos.getX() + this.facing1.getFrontOffsetX() + this.facing2.getFrontOffsetX(),
				pos.getY() + this.facing1.getFrontOffsetY() + this.facing2.getFrontOffsetY(),
				pos.getZ() + this.facing1.getFrontOffsetZ() + this.facing2.getFrontOffsetZ());
	}

	static {

		for (EnumFacing2 direction : EnumFacing2.values())
			VALUES[direction.index] = direction;
	}
}
