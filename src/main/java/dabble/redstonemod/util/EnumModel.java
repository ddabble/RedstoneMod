package dabble.redstonemod.util;

import java.util.ArrayList;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumModel implements IStringSerializable {
	D("none"),
	U("none-x180"),
	N("none-x270"),
	S("none-x90"),
	W("none-x90-y90"),
	E("none-x270-y90"),

	Dn("n"),
	Un("n-x180"),
	Nu("n-x270"),
	Su("n-x90"),
	Wu("n-x90-y90"),
	Eu("n-x270-y90"),

	Dw("w"),
	Uw("w-x180"),
	Nw("w-x270"),
	Sw("w-x90"),
	Wn("w-x90-y90"),
	En("w-x270-y90"),

	Dnw("n_w"),
	Unw("n_w-x180"),
	Nnw("n_w-x270"),
	Snw("n_w-x90"),
	Wnw("n_w-x90-y90"),
	Enw("n_w-x270-y90"),

	Dne("n_e"),
	Une("n_e-x180"),
	Nne("n_e-x270"),
	Sne("n_e-x90"),
	Wne("n_e-x90-y90"),
	Ene("n_e-x270-y90"),

	Dsw("s_w"),
	Usw("s_w-x180"),
	Nsw("s_w-x270"),
	Ssw("s_w-x90"),
	Wsw("s_w-x90-y90"),
	Esw("s_w-x270-y90"),

	Dse("s_e"),
	Use("s_e-x180"),
	Nse("s_e-x270"),
	Sse("s_e-x90"),
	Wse("s_e-x90-y90"),
	Ese("s_e-x270-y90"),

	Dnsw("n_s_w"),
	Unsw("n_s_w-x180"),
	Nnsw("n_s_w-x270"),
	Snsw("n_s_w-x90"),
	Wnsw("n_s_w-x90-y90"),
	Ensw("n_s_w-x270-y90"),

	Dnse("n_s_e"),
	Unse("n_s_e-x180"),
	Nnse("n_s_e-x270"),
	Snse("n_s_e-x90"),
	Wnse("n_s_e-x90-y90"),
	Ense("n_s_e-x270-y90"),

	Dnwe("n_w_e"),
	Unwe("n_w_e-x180"),
	Nnwe("n_w_e-x270"),
	Snwe("n_w_e-x90"),
	Wnwe("n_w_e-x90-y90"),
	Enwe("n_w_e-x270-y90"),

	Dswe("s_w_e"),
	Uswe("s_w_e-x180"),
	Nswe("s_w_e-x270"),
	Sswe("s_w_e-x90"),
	Wswe("s_w_e-x90-y90"),
	Eswe("s_w_e-x270-y90"),

	Dnsew("n_s_e_w"),
	Unsew("n_s_e_w-x180"),
	Nnsew("n_s_e_w-x270"),
	Snsew("n_s_e_w-x90"),
	Wnsew("n_s_e_w-x90-y90"),
	Ensew("n_s_e_w-x270-y90");

	private final String name;

	private EnumModel(String name) {
		this.name = name;
	}

	public String toString() {
		return this.getName();
	}

	public String getName() {
		return this.name;
	}

	public static EnumModel getModel(BlockRedstonePasteWire block, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections) {
		String pastedSide = block.getPastedSide().getName().substring(0, 1).toUpperCase();
		StringBuffer model = new StringBuffer(pastedSide);

		// switch (connectionDirections.size()) {
		// case 0:
		// switch (diagonalConnectionDirections.size()) {
		// case 0:
		//
		// }
		// break;
		// case 1:
		//
		// break;
		// case 2:
		//
		// break;
		// case 3:
		//
		// break;
		// case 4:
		//
		// break;
		// case 5:
		//
		// break;
		// case 6:
		//
		// break;
		// }

		return EnumModel.valueOf(model.toString());
	}
}
