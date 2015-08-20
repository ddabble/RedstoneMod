package dabble.redstonemod.util;

import java.util.HashMap;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;

public class PowerLookup {
	private static HashMap<BlockPos, Byte> powerMap_Overworld = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> powerMap_Nether = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> powerMap_TheEnd = new HashMap<BlockPos, Byte>();

	public static byte getPower(BlockPos pos, World world) {
		Byte power = null;

		switch (world.provider.getDimensionId()) {
			case 0:
				power = powerMap_Overworld.get(pos);
				break;

			case -1:
				power = powerMap_Nether.get(pos);
				break;

			case 1:
				power = powerMap_TheEnd.get(pos);
				break;

			default:
				LogManager.getLogger().error("Could not find the dimension with the ID " + world.provider.getDimensionId()
						+ ".\nRedstone Paste's power system will as a result not function properly. Actually, not at all.");
		}

		return (power == null) ? 0 : power;
	}

	public static void putPower(BlockPos pos, byte power, World world) {

		if (power == 0) {
			removePower(pos, world);
			return;
		}

		switch (world.provider.getDimensionId()) {
			case 0:
				powerMap_Overworld.put(pos, power);
				break;

			case -1:
				powerMap_Nether.put(pos, power);
				break;

			case 1:
				powerMap_TheEnd.put(pos, power);
				break;
		}
	}

	public static void removePower(BlockPos pos, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				powerMap_Overworld.remove(pos);
				break;

			case -1:
				powerMap_Nether.remove(pos);
				break;

			case 1:
				powerMap_TheEnd.remove(pos);
				break;
		}
	}

	public static void clearPower() {
		int combinedSize = powerMap_Overworld.size() + powerMap_Nether.size() + powerMap_TheEnd.size();
		if (combinedSize > 0)
			LogManager.getLogger().info("Removing the power of " + combinedSize + " redstone paste blocks from memory.");

		powerMap_Overworld.clear();
		powerMap_Nether.clear();
		powerMap_TheEnd.clear();
	}
}
