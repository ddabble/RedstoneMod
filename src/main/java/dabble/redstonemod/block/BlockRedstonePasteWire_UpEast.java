package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_UpEast extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_UpEast(String unlocalizedName) {
		super(EnumModel.NONE, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.UP;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.EAST;
	}
}
