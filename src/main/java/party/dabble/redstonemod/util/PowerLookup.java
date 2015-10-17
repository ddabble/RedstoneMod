package party.dabble.redstonemod.util;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PowerLookup {
	private static HashMap<BlockPos, Byte> powerMap_Overworld = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> powerMap_Nether = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> powerMap_TheEnd = new HashMap<BlockPos, Byte>();

	public static byte getPower(BlockPos pos, World world) {
		Byte power = getPowerMap(world).get(pos);
		return (power == null) ? 0 : power;
	}

	public static void putPower(BlockPos pos, byte power, World world) {
		power = (power < 0) ? 0 : ((power > 15) ? 15 : power);
		if (power == 0) {
			removePower(pos, world);
			return;
		}

		getPowerMap(world).put(pos, power);
	}

	public static void removePower(BlockPos pos, World world) {
		getPowerMap(world).remove(pos);
	}

	public static void clearPower(World world) {
		HashMap<BlockPos, Byte> powerMap = getPowerMap(world);
		if (powerMap.size() > 0) {
			LogManager.getLogger().info("Removing the power of " + powerMap.size() + " redstone paste blocks in "
					+ world.provider.getDimensionName() + " from memory.");
			powerMap.clear();
		}
	}

	public static void clearAllPower() {

		if (powerMap_Overworld.size() > 0) {
			LogManager.getLogger().info("Removing the power of " + powerMap_Overworld.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderSurface().getDimensionName() + " from memory.");
			powerMap_Overworld.clear();
		}

		if (powerMap_Nether.size() > 0) {
			LogManager.getLogger().info("Removing the power of " + powerMap_Nether.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderHell().getDimensionName() + " from memory.");
			powerMap_Nether.clear();
		}

		if (powerMap_TheEnd.size() > 0) {
			LogManager.getLogger().info("Removing the power of " + powerMap_TheEnd.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderEnd().getDimensionName() + " from memory.");
			powerMap_TheEnd.clear();
		}
	}

	private static long prevTime = System.nanoTime();

	private static HashMap<BlockPos, Byte> getPowerMap(World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				return powerMap_Overworld;

			case -1:
				return powerMap_Nether;

			case 1:
				return powerMap_TheEnd;

			default:
				if (System.nanoTime() - prevTime > 1e10) {
					LogManager.getLogger().error("Could not find the dimension with the ID " + world.provider.getDimensionId()
							+ ". Redstone Paste's power system will as a result not function properly. Actually, not at all.");
					prevTime = System.nanoTime();
				}

				return new HashMap<BlockPos, Byte>();
		}
	}
}
