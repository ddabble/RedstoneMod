package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_DownEast extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_DownEast(String unlocalizedName) {
		super(EnumModel.NONE, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.EAST;
	}
}
