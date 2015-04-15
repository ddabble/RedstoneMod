package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_UpWest extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_UpWest(String unlocalizedName) {
		super(EnumModel.U, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.UP;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.WEST;
	}
}
