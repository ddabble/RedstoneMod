package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_North extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_North(String unlocalizedName) {
		super(EnumModel.NONE, unlocalizedName);
	}

	@Override
	public boolean isSingleFaced() {
		return true;
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.NORTH;
	}
}
