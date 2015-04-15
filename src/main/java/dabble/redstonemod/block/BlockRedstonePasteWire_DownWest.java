package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_DownWest extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_DownWest(String unlocalizedName) {
		super(EnumModel.D, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.WEST;
	}
}
