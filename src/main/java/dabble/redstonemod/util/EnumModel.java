package dabble.redstonemod.util;

import java.util.ArrayList;
import java.util.Comparator;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

public enum EnumModel implements IStringSerializable {
	NONE("none"),
	ns("ns"),
	we("we"),
	nw("nw"),
	ne("ne"),
	sw("sw"),
	se("se"),
	nsw("nsw"),
	nse("nse"),
	nwe("nwe"),
	swe("swe"),
	nswe("nswe"),

	nsNns("nsNns"),
	weEns("weEns"),
	neNns("neNns"),
	neEns("neEns"),
	nseNns("nseNns"),
	nseSns("nseSns"),
	nseEns("nseEns"),
	nsweNns("nsweNns"),

	nsNnsSns("nsNnsSns"),
	weEnsEns("weEnsEns"),
	neNnsEns("neNnsEns"),
	nseNnsSns("nseNnsSns"),
	nseNnsEns("nseNnsEns"),
	nseSnsEns("nseSnsEns"),
	nsweNnsSns("nsweNnsSns"),
	nsweNnsEns("nsweNnsEns"),

	nseNnsSnsEns("nseNnsSnsEns"),
	nsweNnsSnsEns("nsweNnsSnsEns"),

	nsweNnsSnsWnsEns("nsweNnsSnsWnsEns");

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
		EnumFacing pastedSide = block.pastedSide;

		// TODO Remove this when finished (?)
		if (connectionDirections.size() > 0 && pastedSide != EnumFacing.DOWN) {

			for (byte i = 0; i < connectionDirections.size(); ++i) {
				EnumFacing currentSide = connectionDirections.get(i);
				EnumFacing normalisedSide = getNormalisedSide(currentSide, pastedSide);

				if (normalisedSide != currentSide)
					connectionDirections.set(i, normalisedSide);
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

		if (model.length() == 1)
			switch (model.toString()) {
				case "n":
					return ns;
				case "s":
					return ns;
				case "w":
					return we;
				case "e":
					return we;
			}

		return valueOf(model);
	}

	private static EnumFacing getNormalisedSide(EnumFacing currentSide, EnumFacing pastedSide) {

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
			return valueOf(sb.toString());
		} catch (Exception e) {
			return NONE;
		}
	}
}
