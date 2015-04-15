package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_South extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_South(String unlocalizedName) {
		super(EnumModel.S, unlocalizedName);
	}

	@Override
	public boolean isSingleFaced() {
		return true;
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.SOUTH;
	}
}
