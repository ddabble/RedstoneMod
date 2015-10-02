package dabble.redstonemod.util;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public enum EnumModel {
	NONE(0, 1, 0, 1),

	NS(1, 2, 0, 1, EnumFacing.NORTH, EnumFacing.SOUTH),
	WE(2, 3, 0, 1, EnumFacing.WEST, EnumFacing.EAST),

	NW(3, 4, 0, 1, EnumFacing.NORTH, EnumFacing.WEST),
	NE(4, 5, 0, 1, EnumFacing.NORTH, EnumFacing.EAST),
	SW(5, 6, 0, 1, EnumFacing.SOUTH, EnumFacing.WEST),
	SE(6, 7, 0, 1, EnumFacing.SOUTH, EnumFacing.EAST),

	NSW(7, 8, 0, 1, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST),
	NSE(8, 9, 0, 1, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST),
	NWE(9, 10, 0, 1, EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.EAST),
	SWE(0, 1, 1, 2, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST),

	NSWE(1, 2, 1, 2, EnumFacing.HORIZONTALS);

	private final double minU;
	private final double maxU;
	private final double minV;
	private final double maxV;
	private final EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

	private EnumModel(double minU, double maxU, double minV, double maxV, EnumFacing... connections) {
		this.minU = minU / 10;
		this.maxU = maxU / 10;
		this.minV = minV / 2;
		this.maxV = maxV / 2;

		for (EnumFacing direction : connections)
			this.connections.add(direction);
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

	public boolean containsConnection(EnumFacing connection) {
		return this.connections.contains(connection);
	}

	public static EnumMap<EnumFacing, EnumModel> getModelFromExternalConnections(EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides, EnumSet<EnumFacing> pastedSides) {
		EnumMap<EnumFacing, EnumModel> model = new EnumMap<EnumFacing, EnumModel>(EnumFacing.class);
		addInternalConnections(connectionSides, pastedSides);

		for (Entry<EnumFacing, EnumSet<EnumFacing>> side : connectionSides.entrySet())
			model.put(side.getKey(), getModelFromConnections(side.getValue(), side.getKey()));

		return model;
	}

	private static void addInternalConnections(EnumMap<EnumFacing, EnumSet<EnumFacing>> connectionSides, EnumSet<EnumFacing> pastedSides) {

		for (EnumFacing pastedSide : pastedSides) {
			EnumSet<EnumFacing> pastedSideConnections = null;

			if (!connectionSides.containsKey(pastedSide))
				pastedSideConnections = EnumSet.noneOf(EnumFacing.class);

			for (EnumFacing borderingSide : getBorderingFacings(pastedSide)) {

				if (pastedSides.contains(borderingSide)) {

					if (pastedSideConnections == null)
						connectionSides.get(pastedSide).add(borderingSide);
					else
						pastedSideConnections.add(borderingSide);
				} else if (connectionSides.containsKey(borderingSide)) {
					connectionSides.get(borderingSide).add(pastedSide);

					if (pastedSideConnections == null)
						connectionSides.get(pastedSide).add(borderingSide);
					else
						pastedSideConnections.add(borderingSide);
				}
			}

			if (pastedSideConnections != null)
				connectionSides.put(pastedSide, pastedSideConnections);
		}
	}

	private static EnumFacing[] getBorderingFacings(EnumFacing facing) {

		switch (facing.getAxis()) {
			case X:
				return new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH };

			case Y:
				return new EnumFacing[] { EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST };

			case Z:
				return new EnumFacing[] { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.WEST, EnumFacing.EAST };

			default:
				return null;
		}
	}

	private static EnumModel getModelFromConnections(EnumSet<EnumFacing> connections, EnumFacing side) {

		if (connections.isEmpty())
			return NONE;

		connections = getNormalisedConnections(connections, side);
		Iterator<EnumFacing> connectionIterator = connections.iterator();

		switch (connections.size()) {
			case 1:
				if (connectionIterator.next().getAxis() == Axis.Z)
					return NS;
				else
					return WE;

			case 2:
				if (connectionIterator.next() == EnumFacing.NORTH) {

					if (connectionIterator.next() == EnumFacing.WEST)
						return NW;
					else
						return NE;
				} else {

					if (connectionIterator.next() == EnumFacing.WEST)
						return SW;
					else
						return SE;
				}

			case 3:
				if (connectionIterator.next() == EnumFacing.SOUTH)
					return SWE;
				else if (connectionIterator.next() == EnumFacing.WEST)
					return NWE;
				else if (connectionIterator.next() == EnumFacing.WEST)
					return NSW;
				else
					return NSE;

			case 4:
				return NSWE;

			default:
				return null;
		}
	}

	private static EnumSet<EnumFacing> getNormalisedConnections(EnumSet<EnumFacing> connections, EnumFacing side) {

		if (connections.size() == 2) {
			Iterator<EnumFacing> connectionIterator = connections.iterator();

			if (connectionIterator.next() == connectionIterator.next().getOpposite())
				return EnumSet.of(getNormalisedConnection(connections.iterator().next(), side));
		}

		EnumSet<EnumFacing> normalisedConnections = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing connection : connections)
			normalisedConnections.add(getNormalisedConnection(connection, side));

		return normalisedConnections;
	}

	public static EnumFacing getNormalisedConnection(EnumFacing connection, EnumFacing side) {

		switch (side) {
			case DOWN:
				return connection;

			case UP:
				if (connection.getAxis() == Axis.Z)
					return connection.getOpposite();
				else
					break;

			case NORTH:
				break;

			case SOUTH:
				if (connection.getAxis() == Axis.X)
					return connection.getOpposite();
				else
					break;

			case WEST:
				if (connection.getAxis() == Axis.Z)
					return connection.rotateY();
				else
					break;

			case EAST:
				if (connection.getAxis() == Axis.Z)
					return connection.rotateYCCW();
				else
					break;
		}

		return rotateX(connection);
	}

	private static EnumFacing rotateX(EnumFacing facing) {

		switch (facing) {
			case DOWN:
				return EnumFacing.SOUTH;

			case UP:
				return EnumFacing.NORTH;

			case NORTH:
				return EnumFacing.DOWN;

			case SOUTH:
				return EnumFacing.UP;

			default:
				return facing;
		}
	}
}
