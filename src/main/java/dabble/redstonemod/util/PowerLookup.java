package dabble.redstonemod.util;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class PowerLookup {
	private static HashMap<BlockPos, Byte> blockMap_Overworld = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> blockMap_Nether = new HashMap<BlockPos, Byte>();
	private static HashMap<BlockPos, Byte> blockMap_TheEnd = new HashMap<BlockPos, Byte>();

	private static ArrayList<BlockPos> blocksNeedingUpdate_Overworld = new ArrayList<BlockPos>();
	private static ArrayList<BlockPos> blocksNeedingUpdate_Nether = new ArrayList<BlockPos>();
	private static ArrayList<BlockPos> blocksNeedingUpdate_TheEnd = new ArrayList<BlockPos>();

	public static byte getPower(BlockPos pos, World world) {
		Byte power;

		switch (world.provider.getDimensionId()) {
			case 0:
				power = blockMap_Overworld.get(pos);
				break;
			case -1:
				power = blockMap_Nether.get(pos);
				break;
			case 1:
				power = blockMap_TheEnd.get(pos);
				break;
			default:
				System.out.println("Could not find the dimension with the ID " + world.provider.getDimensionId() + ".\nTerminating.");
				throw new IllegalStateException();
		}

		return (power == null) ? 0 : power;
	}

	public static void putPower(BlockPos pos, byte power, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				blockMap_Overworld.put(pos, power);
				break;
			case -1:
				blockMap_Nether.put(pos, power);
				break;
			case 1:
				blockMap_TheEnd.put(pos, power);
				break;
		}
	}

	public static void removePower(BlockPos pos, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				/*
				 * When Minecraft unloads chunks from the overworld, it actually doesn't, for some weird reason.
				 * So if the block gets removed here it will never get re-added when the chunk "re-loads",
				 * because the chunk was never really unloaded and thusly it will also never get re-loaded ._.
				 */
				// blockMap_Overworld.remove(pos);
				break;
			case -1:
				blockMap_Nether.remove(pos);
				break;
			case 1:
				blockMap_TheEnd.remove(pos);
				break;
		}
	}

	public static void clearPower(World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				blockMap_Overworld.clear();
				break;
			case -1:
				blockMap_Nether.clear();
				break;
			case 1:
				blockMap_TheEnd.clear();
				break;
		}
	}

	public static ArrayList<BlockPos> getBlocksNeedingUpdate(World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				return blocksNeedingUpdate_Overworld;
			case -1:
				return blocksNeedingUpdate_Nether;
			case 1:
				return blocksNeedingUpdate_TheEnd;
			default:
				return null;
		}
	}

	public static void addBlockNeedingUpdate(BlockPos pos, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				blocksNeedingUpdate_Overworld.add(pos);
				break;
			case -1:
				blocksNeedingUpdate_Nether.add(pos);
				break;
			case 1:
				blocksNeedingUpdate_TheEnd.add(pos);
				break;
		}
	}

	public static void clearBlocksNeedingUpdate(World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				blocksNeedingUpdate_Overworld.clear();
				break;
			case -1:
				blocksNeedingUpdate_Nether.clear();
				break;
			case 1:
				blocksNeedingUpdate_TheEnd.clear();
				break;
		}
	}
}
