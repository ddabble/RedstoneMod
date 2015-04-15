package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_East extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_East(String unlocalizedName) {
		super(EnumModel.E, unlocalizedName);
	}

	@Override
	public boolean isSingleFaced() {
		return true;
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.EAST;
	}
}
