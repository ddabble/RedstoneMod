package dabble.redstonemod.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import dabble.redstonemod.block.BlockRedstonePasteWire;

public enum EnumModel {
	NONE(0, 1, 0, 1),
	NS(1, 2, 0, 1),
	WE(2, 3, 0, 1),
	NW(3, 4, 0, 1),
	NE(4, 5, 0, 1),
	SW(5, 6, 0, 1),
	SE(6, 7, 0, 1),
	NSW(7, 8, 0, 1),
	NSE(8, 9, 0, 1),
	NWE(9, 10, 0, 1),
	SWE(0, 1, 1, 2),
	NSWE(1, 2, 1, 2);

	private final double minU;
	private final double maxU;
	private final double minV;
	private final double maxV;

	private EnumModel(double minU, double maxU, double minV, double maxV) {
		this.minU = minU / 10;
		this.maxU = maxU / 10;
		this.minV = minV / 2;
		this.maxV = maxV / 2;
	}

	public double getMinU() {
		return this.minU;
	}

	public double getMaxU() {
		return this.maxU;
	}

	public double getMinV() {
		return this.minV;
	}

	public double getMaxV() {
		return this.maxV;
	}

	public static EnumMap<EnumFacing, EnumModel> getModel(BlockRedstonePasteWire wire, ArrayList<EnumFacing> connectionDirections, ArrayList<EnumFacing[]> diagonalConnectionDirections, ArrayList<EnumFacing> blockDirections, IBlockAccess worldIn, BlockPos pos) {
		EnumMap<EnumFacing, ArrayList<EnumFacing>> faces = new EnumMap<EnumFacing, ArrayList<EnumFacing>>(EnumFacing.class);
		EnumMap<EnumFacing, EnumModel> model = new EnumMap<EnumFacing, EnumModel>(EnumFacing.class);
		EnumFacing pastedSide = wire.pastedSide;
		EnumFacing pastedSide2 = wire.pastedSide2;

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

			if (face.getValue().get(0) == null) {

				if (faces.size() == 1) {
					model.put(currentSide, EnumModel.NONE);
					return model;
				}

				ArrayList<EnumFacing> value = new ArrayList<EnumFacing>();
				value.add(EnumFacing.NORTH);
				value.add(EnumFacing.SOUTH);
				model.put(currentSide, valueOf(value));
			} else
				model.put(currentSide, valueOf(face.getValue()));
		}

		return model;
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

	private static EnumModel valueOf(ArrayList<EnumFacing> model) {
		StringBuffer sb = new StringBuffer();

		for (EnumFacing direction : model)
			sb.append(Character.toUpperCase(direction.toString().charAt(0)));

		try {
			return valueOf(sb.toString());
		} catch (Exception e) {
			System.out.println("Could not find the enum with the value of " + sb + ".");
			return NONE;
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
