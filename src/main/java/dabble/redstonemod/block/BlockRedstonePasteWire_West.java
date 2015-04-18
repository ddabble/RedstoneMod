package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_West extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_West(String unlocalizedName) {
		super(EnumModel.NONE, unlocalizedName);
	}

	@Override
	public boolean isSingleFaced() {
		return true;
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.WEST;
	}
}
