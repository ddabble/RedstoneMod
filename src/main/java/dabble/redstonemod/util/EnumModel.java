package dabble.redstonemod.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import dabble.redstonemod.block.BlockRedstonePasteWire;

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

	// nsNns("nsNns"),
	// nsNnw("nsNnw"),
	// nsNne("nsNne"),
	// nsNnsw("nsNnsw"),
	// nsNnse("nsNnse"),
	// nsNswe("nsNswe"),
	// nsNnswe("nsNnswe"),
	//
	// nsSns("nsSns"),
	// nsSnw("nsSnw"),
	// nsSne("nsSne"),
	// nsSnsw("nsSnsw"),
	// nsSnse("nsSnse"),
	// nsSswe("nsSswe"),
	// nsSnswe("nsSnswe"),
	//
	// weEns("weEns"),
	// weEnw("weEnw"),
	// weEne("weEne"),
	// weEnsw("weEnsw"),
	// weEnse("weEnse"),
	// weEswe("weEswe"),
	// weEnswe("weEnswe"),
	//
	// weWns("weWns"),
	// weWnw("weWnw"),
	// weWne("weWne"),
	// weWnsw("weWnsw"),
	// weWnse("weWnse"),
	// weWswe("weWswe"),
	// weWnswe("weWnswe"),
	//
	// nwNns("nwNns"),
	// nwNnw("nwNnw"),
	// nwNne("nwNne"),
	// nwNnsw("nwNnsw"),
	// nwNnse("nwNnse"),
	// nwNswe("nwNswe"),
	// nwNnswe("nwNnswe"),
	//
	// nwWns("nwWns"),
	// nwWnw("nwWnw"),
	// nwWne("nwWne"),
	// nwWnsw("nwWnsw"),
	// nwWnse("nwWnse"),
	// nwWswe("nwWswe"),
	// nwWnswe("nwWnswe"),
	//
	// neNns("neNns"),
	// neNnw("neNnw"),
	// neNne("neNne"),
	// neNnsw("neNnsw"),
	// neNnse("neNnse"),
	// neNswe("neNswe"),
	// neNnswe("neNnswe"),
	//
	// neEns("neEns"),
	// neEnw("neEnw"),
	// neEne("neEne"),
	// neEnsw("neEnsw"),
	// neEnse("neEnse"),
	// neEswe("neEswe"),
	// neEnswe("neEnswe"),
	//
	// swSns("swSns"),
	// swSnw("swSnw"),
	// swSne("swSne"),
	// swSnsw("swSnsw"),
	// swSnse("swSnse"),
	// swSswe("swSswe"),
	// swSnswe("swSnswe"),
	//
	// swWns("swWns"),
	// swWnw("swWnw"),
	// swWne("swWne"),
	// swWnsw("swWnsw"),
	// swWnse("swWnse"),
	// swWswe("swWswe"),
	// swWnswe("swWnswe"),
	//
	// seSns("seSns"),
	// seSnw("seSnw"),
	// seSne("seSne"),
	// seSnsw("seSnsw"),
	// seSnse("seSnse"),
	// seSswe("seSswe"),
	// seSnswe("seSnswe"),
	//
	// seEns("seEns"),
	// seEnw("seEnw"),
	// seEne("seEne"),
	// seEnsw("seEnsw"),
	// seEnse("seEnse"),
	// seEswe("seEswe"),
	// seEnswe("seEnswe"),
	//
	// nseNns("nseNns"),
	//
	// nseSns("nseSns"),
	//
	// nseEns("nseEns"),
	//
	// nsweNns("nsweNns"),
	//
	// nsNnsSns("nsNnsSns"),
	// weEnsEns("weEnsEns"),
	// neNnsEns("neNnsEns"),
	// nseNnsSns("nseNnsSns"),
	// nseNnsEns("nseNnsEns"),
	// nseSnsEns("nseSnsEns"),
	// nsweNnsSns("nsweNnsSns"),
	// nsweNnsEns("nsweNnsEns"),
	//
	// nseNnsSnsEns("nseNnsSnsEns"),
	// nsweNnsSnsEns("nsweNnsSnsEns"),
	//
	// nsweNnsSnsWnsEns("nsweNnsSnsWnsEns"),

	INVALID("invalid");

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

	public static EnumModel getModel(BlockRedstonePasteWire wire, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections, IBlockAccess worldIn, BlockPos pos) {
		EnumMap<EnumFacing, ArrayList<EnumFacing>> faces = new EnumMap<EnumFacing, ArrayList<EnumFacing>>(EnumFacing.class);
		EnumMap<EnumFacing, ArrayList<EnumFacing>> normalisedFaces = new EnumMap<EnumFacing, ArrayList<EnumFacing>>(EnumFacing.class);
		EnumFacing pastedSide = wire.pastedSide;
		EnumFacing pastedSide2 = wire.pastedSide2;
		StringBuffer model = new StringBuffer();

		for (EnumFacing blockSide : blockDirections) {
			ArrayList<EnumFacing> connections = new ArrayList<EnumFacing>();

			for (EnumFacing side : connectionDirections) {

				if (side == blockSide.getOpposite())
					continue;

				Block block = worldIn.getBlockState(pos.offset(side)).getBlock();

				// TODO Move this to checkBlockInDirection in BlockRedstonePasteWire
				if (block instanceof BlockRedstonePasteWire && (((BlockRedstonePasteWire) block).pastedSide == blockSide || ((BlockRedstonePasteWire) block).pastedSide2 == blockSide))
					connections.add(side);
				else if (blockSide == pastedSide || blockSide == pastedSide2)
					connections.add(side);
			}

			for (EnumFacing[] side : diagonalConnectionDirections) {

				// Sorts out any sides that are not directly next to the current blockSide
				if (side[0] != blockSide && side[1] != blockSide)
					continue;

				BlockRedstonePasteWire diagonalBlock = (BlockRedstonePasteWire) worldIn.getBlockState(pos.offset(side[0]).offset(side[1])).getBlock();
				EnumFacing otherSide = ((blockSide == side[0]) ? side[1] : side[0]);

				if (!connections.contains(otherSide) && (diagonalBlock.pastedSide == otherSide.getOpposite() || diagonalBlock.pastedSide2 == otherSide.getOpposite()
						|| diagonalBlock.pastedSide == pastedSide || diagonalBlock.pastedSide == pastedSide2
						|| diagonalBlock.pastedSide2 == pastedSide || diagonalBlock.pastedSide2 == pastedSide2))
					connections.add(otherSide);
			}

			if (connections.size() == 0) {

				if (blockSide == pastedSide || blockSide == pastedSide2) {
					connections.add(null);
					faces.put(blockSide, connections);
				}

				continue;
			} else
				normaliseAndSort(connections, blockSide);

			if (connections.size() == 1) {

				switch (connections.get(0)) {
					case NORTH:
						connections.add(EnumFacing.SOUTH);
						break;
					case SOUTH:
						connections.add(0, EnumFacing.NORTH);
						break;
					case WEST:
						connections.add(EnumFacing.EAST);
						break;
					case EAST:
						connections.add(0, EnumFacing.WEST);
						break;
					default:
						break;
				}
			}

			faces.put(blockSide, connections);
		}

		for (Entry<EnumFacing, ArrayList<EnumFacing>> face : faces.entrySet()) {
			EnumFacing currentSide = face.getKey();
			EnumFacing normalisedSide = asdf(currentSide, pastedSide);

			if (face.getValue().get(0) == null) {

				if (faces.size() == 1)
					return NONE;

				ArrayList<EnumFacing> value = new ArrayList<EnumFacing>();
				value.add(EnumFacing.NORTH);
				value.add(EnumFacing.SOUTH);
				normalisedFaces.put(normalisedSide, value);
			} else
				normalisedFaces.put(normalisedSide, face.getValue());
		}

		byte i = 0;
		for (Entry<EnumFacing, ArrayList<EnumFacing>> face : normalisedFaces.entrySet()) {

			if (i != 0)
				model.append(Character.toUpperCase(face.getKey().toString().charAt(0)));

			for (EnumFacing side : face.getValue())
				model.append(side.toString().charAt(0));

			++i;
		}

		return valueOf(model);
	}

	private static void normaliseAndSort(ArrayList<EnumFacing> connectionSides, EnumFacing pastedSide) {

		// TODO Remove this when finished (?)
		if (connectionSides.size() > 0 && pastedSide != EnumFacing.DOWN) {

			for (byte i = 0; i < connectionSides.size(); ++i) {
				EnumFacing currentSide = connectionSides.get(i);
				EnumFacing normalisedSide = getNormalisedSide(currentSide, pastedSide);

				if (normalisedSide != currentSide)
					connectionSides.set(i, normalisedSide);
			}

			connectionSides.sort(new Comparator<EnumFacing>() {
				@Override
				public int compare(EnumFacing side1, EnumFacing side2) {
					return (int) Math.signum(side1.getIndex() - side2.getIndex());
				}
			});
		}
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

	private static EnumFacing asdf(EnumFacing currentSide, EnumFacing pastedSide) {

		switch (pastedSide) {
			case UP:
				if (currentSide.getAxis() != EnumFacing.Axis.X)
					currentSide = currentSide.getOpposite();
				break;
			case NORTH:
				if (currentSide.getAxis() != EnumFacing.Axis.X)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.X);
				break;
			case SOUTH:
				if (currentSide.getAxis() != EnumFacing.Axis.X)
					currentSide = rotateXCCW(currentSide);
				break;
			case WEST:
				if (currentSide.getAxis() != EnumFacing.Axis.Z)
					currentSide = rotateZCCW(currentSide);
				break;
			case EAST:
				if (currentSide.getAxis() != EnumFacing.Axis.Z)
					currentSide = currentSide.rotateAround(EnumFacing.Axis.Z);
				break;
			default:
				break;
		}

		return currentSide;
	}

	private static EnumFacing rotateXCCW(EnumFacing side) {

		switch (SwitchPlane.FACING_LOOKUP[side.ordinal()]) {
			case 1:
				return EnumFacing.UP;
			case 2:
			case 4:
			default:
				throw new IllegalStateException("Unable to get X-rotated facing of " + side);
			case 3:
				return EnumFacing.DOWN;
			case 5:
				return EnumFacing.SOUTH;
			case 6:
				return EnumFacing.NORTH;
		}
	}

	private static EnumFacing rotateZCCW(EnumFacing side) {

		switch (SwitchPlane.FACING_LOOKUP[side.ordinal()]) {
			case 2:
				return EnumFacing.UP;
			case 3:
			default:
				throw new IllegalStateException("Unable to get Z-rotated facing of " + side);
			case 4:
				return EnumFacing.DOWN;
			case 5:
				return EnumFacing.WEST;
			case 6:
				return EnumFacing.EAST;
		}
	}

	private static EnumModel valueOf(StringBuffer sb) {
		try {
			return valueOf(sb.toString());
		} catch (Exception e) {
			return INVALID;
		}
	}

	static final class SwitchPlane {
		static final int[] FACING_LOOKUP;

		static {
			FACING_LOOKUP = new int[EnumFacing.values().length];

			try {
				FACING_LOOKUP[EnumFacing.NORTH.ordinal()] = 1;
			} catch (NoSuchFieldError var9) {
				;
			}

			try {
				FACING_LOOKUP[EnumFacing.EAST.ordinal()] = 2;
			} catch (NoSuchFieldError var8) {
				;
			}

			try {
				FACING_LOOKUP[EnumFacing.SOUTH.ordinal()] = 3;
			} catch (NoSuchFieldError var7) {
				;
			}

			try {
				FACING_LOOKUP[EnumFacing.WEST.ordinal()] = 4;
			} catch (NoSuchFieldError var6) {
				;
			}

			try {
				FACING_LOOKUP[EnumFacing.UP.ordinal()] = 5;
			} catch (NoSuchFieldError var5) {
				;
			}

			try {
				FACING_LOOKUP[EnumFacing.DOWN.ordinal()] = 6;
			} catch (NoSuchFieldError var4) {
				;
			}
		}
	}
}
