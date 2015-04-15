package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_NorthWest extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_NorthWest(String unlocalizedName) {
		super(EnumModel.N, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.NORTH;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.WEST;
	}
}
