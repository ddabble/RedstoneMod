package dabble.redstonemod.util;

import java.util.ArrayList;
import java.util.Comparator;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumModel implements IStringSerializable {
	NONE("none"),
	N("n"),
	W("w"),
	NW("n_w"),
	NE("n_e"),
	SW("s_w"),
	SE("s_e"),
	NSW("n_s_w"),
	NSE("n_s_e"),
	NWE("n_w_e"),
	SWE("s_w_e"),
	NSWE("n_s_w_e");

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
		StringBuffer model = new StringBuffer();
		EnumFacing pastedSide = block.getPastedSide();

		if (connectionDirections.size() > 0 && pastedSide != EnumFacing.DOWN) {

			for (byte i = 0; i < connectionDirections.size(); ++i) {
				EnumFacing currentSide = connectionDirections.get(i);
				EnumFacing normalizedSide = getNormalizedSide(currentSide, pastedSide);

				if (normalizedSide != currentSide)
					connectionDirections.set(i, normalizedSide);
			}

			connectionDirections.sort(new Comparator<EnumFacing>() {
				@Override
				public int compare(EnumFacing side1, EnumFacing side2) {
					return (int) Math.signum(side1.getIndex() - side2.getIndex());
				}
			});
		}

		for (EnumFacing side : connectionDirections) {
			char s = side.toString().charAt(0);
			model.append(s);
		}

		if (model.length() == 0)
			return NONE;

		switch (model.toString()) {
			case "ns":
				return EnumModel.N;
			case "s":
				return EnumModel.N;
			case "we":
				return EnumModel.W;
			case "e":
				return EnumModel.W;
		}

		return valueOf(model);
	}

	private static EnumFacing getNormalizedSide(EnumFacing currentSide, EnumFacing pastedSide) {

		switch (pastedSide) {
			case UP:
				if (currentSide.getAxis() == EnumFacing.Axis.X)
					currentSide = currentSide.getOpposite();
				break;
			case NORTH:
				if (currentSide.getAxis() == EnumFacing.Axis.Y)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.X);
				break;
			case SOUTH:
				if (currentSide.getAxis() == EnumFacing.Axis.X)
					currentSide = currentSide.getOpposite();
				else if (currentSide.getAxis() == EnumFacing.Axis.Y)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.X);
				break;
			case WEST:
				if (currentSide.getAxis() == EnumFacing.Axis.Y)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.X);
				else if (currentSide.getAxis() == EnumFacing.Axis.Z)
					currentSide = currentSide.rotateY();
				break;
			case EAST:
				if (currentSide.getAxis() == EnumFacing.Axis.Y)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.X);
				else if (currentSide.getAxis() == EnumFacing.Axis.Z)
					currentSide = currentSide.rotateYCCW();
				break;
			default:
				break;
		}

		return currentSide;
	}

	private static EnumModel valueOf(StringBuffer sb) {
		try {
			return EnumModel.valueOf(sb.toString().toUpperCase());
		} catch (Exception e) {
			return EnumModel.NONE;
		}
	}

	public static enum Rotation implements IStringSerializable {
		DOWN("none"),
		UP("x180"),
		NORTH("x270"),
		SOUTH("x90"),
		WEST("x90-y90"),
		EAST("x270-y90");
		private final String name;

		private Rotation(String name) {
			this.name = name;
		}

		public String toString() {
			return this.getName();
		}

		public String getName() {
			return this.name;
		}
	}
}
