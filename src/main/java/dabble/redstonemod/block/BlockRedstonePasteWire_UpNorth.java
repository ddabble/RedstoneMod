package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_UpNorth extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_UpNorth(String unlocalizedName) {
		super(EnumModel.U, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.UP;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.NORTH;
	}
}
