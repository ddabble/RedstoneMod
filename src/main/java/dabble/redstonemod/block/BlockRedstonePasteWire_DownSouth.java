package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_DownSouth extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_DownSouth(String unlocalizedName) {
		super(EnumModel.D, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.SOUTH;
	}
}
