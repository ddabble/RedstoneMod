package party.dabble.redstonemod.util;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import party.dabble.redstonemod.block.BlockRedstonePasteWire;

public class ModelLookup {
	private static HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>> modelMap_Overworld = new HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>>();
	private static HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>> modelMap_Nether = new HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>>();
	private static HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>> modelMap_TheEnd = new HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>>();

	public static EnumMap<EnumFacing, EnumModel> getModel(BlockPos pos, World world) {
		EnumMap<EnumFacing, EnumModel> model = getModelMap(world).get(pos);
		if (model == null) {
			model = ((BlockRedstonePasteWire)world.getBlockState(pos).getBlock()).getModel(pos, world);
			putModel(pos, model, world);
		}

		return model;
	}

	public static void putModel(BlockPos pos, EnumMap<EnumFacing, EnumModel> model, World world) {
		getModelMap(world).put(pos, model);
	}

	public static void removeModel(BlockPos pos, World world) {
		getModelMap(world).remove(pos);
	}

	public static void clearModels(World world) {
		HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>> modelMap = getModelMap(world);
		if (modelMap.size() > 0) {
			LogManager.getLogger().info("Removing the models of " + modelMap.size() + " redstone paste blocks in "
					+ world.provider.getDimensionName() + " from memory.");
			modelMap.clear();
		}
	}

	public static void clearAllModels() {

		if (modelMap_Overworld.size() > 0) {
			LogManager.getLogger().info("Removing the models of " + modelMap_Overworld.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderSurface().getDimensionName() + " from memory.");
			modelMap_Overworld.clear();
		}

		if (modelMap_Nether.size() > 0) {
			LogManager.getLogger().info("Removing the models of " + modelMap_Nether.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderHell().getDimensionName() + " from memory.");
			modelMap_Nether.clear();
		}

		if (modelMap_TheEnd.size() > 0) {
			LogManager.getLogger().info("Removing the models of " + modelMap_TheEnd.size() + " redstone paste blocks in "
					+ new net.minecraft.world.WorldProviderEnd().getDimensionName() + " from memory.");
			modelMap_TheEnd.clear();
		}
	}

	private static long prevTime = System.nanoTime();

	private static HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>> getModelMap(World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				return modelMap_Overworld;

			case -1:
				return modelMap_Nether;

			case 1:
				return modelMap_TheEnd;

			default:
				if (System.nanoTime() - prevTime > 1e10) {
					LogManager.getLogger().error("Could not find the dimension with the ID " + world.provider.getDimensionId()
							+ ". Redstone Paste will as a result not render as efficiently.");
					prevTime = System.nanoTime();
				}

				return new HashMap<BlockPos, EnumMap<EnumFacing, EnumModel>>();
		}
	}

	public static boolean isModelPointingInDirection(EnumFacing direction, BlockPos pos, World world) {

		for (Entry<EnumFacing, EnumModel> face : getModel(pos, world).entrySet()) {

			if (face.getKey().getAxis() == direction.getAxis())
				continue;
			else if (face.getValue() == EnumModel.NONE || face.getValue().containsConnection(EnumModel.getNormalisedConnection(direction, face.getKey())))
				return true;
		}

		return false;
	}

	public static boolean isModelPointingInDirection(EnumFacing direction, EnumMap<EnumFacing, EnumModel> model) {

		for (Entry<EnumFacing, EnumModel> face : model.entrySet()) {

			if (face.getKey().getAxis() == direction.getAxis())
				continue;
			else if (face.getValue() == EnumModel.NONE || face.getValue().containsConnection(EnumModel.getNormalisedConnection(direction, face.getKey())))
				return true;
		}

		return false;
	}
}
