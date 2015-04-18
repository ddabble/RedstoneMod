package dabble.redstonemod.block;

import net.minecraft.util.EnumFacing;
import dabble.redstonemod.util.EnumModel;

public class BlockRedstonePasteWire_SouthWest extends BlockRedstonePasteWire {

	public BlockRedstonePasteWire_SouthWest(String unlocalizedName) {
		super(EnumModel.NONE, unlocalizedName);
	}

	@Override
	public EnumFacing getPastedSide() {
		return EnumFacing.SOUTH;
	}

	@Override
	public EnumFacing getPastedSide2() {
		return EnumFacing.WEST;
	}
}
