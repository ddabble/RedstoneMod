package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_Up extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_Up(String unlocalizedName) {
		super(EnumModel.U, unlocalizedName);
	}

	@Override
	public boolean isSingleFaced() {
		return true;
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.UP;
	}
}
