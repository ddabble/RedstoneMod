package party.dabble.redstonemod.util;

import java.util.EnumMap;
import java.util.HashMap;

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
		EnumMap<EnumFacing, EnumModel> model = null;

		switch (world.provider.getDimensionId()) {
			case 0:
				model = modelMap_Overworld.get(pos);
				break;

			case -1:
				model = modelMap_Nether.get(pos);
				break;

			case 1:
				model = modelMap_TheEnd.get(pos);
				break;

			default:
				LogManager.getLogger().error("Could not find the dimension with the ID " + world.provider.getDimensionId()
						+ ".\nRedstone Paste will as a result not render at all. Ooh, invisible Redstone Paste o.O");
				return new EnumMap<EnumFacing, EnumModel>(EnumFacing.class);
		}

		if (model == null) {
			model = ((BlockRedstonePasteWire)world.getBlockState(pos).getBlock()).getModel(pos, world);
			putModel(pos, model, world);
		}

		return model;
	}

	public static void putModel(BlockPos pos, EnumMap<EnumFacing, EnumModel> model, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				modelMap_Overworld.put(pos, model);
				break;

			case -1:
				modelMap_Nether.put(pos, model);
				break;

			case 1:
				modelMap_TheEnd.put(pos, model);
				break;
		}
	}

	public static void removeModel(BlockPos pos, World world) {

		switch (world.provider.getDimensionId()) {
			case 0:
				modelMap_Overworld.remove(pos);
				break;

			case -1:
				modelMap_Nether.remove(pos);
				break;

			case 1:
				modelMap_TheEnd.remove(pos);
				break;
		}
	}

	public static void clearModels() {
		int combinedSize = modelMap_Overworld.size() + modelMap_Nether.size() + modelMap_TheEnd.size();
		if (combinedSize > 0)
			LogManager.getLogger().info("Removing the models of " + combinedSize + " redstone paste blocks from memory.");

		modelMap_Overworld.clear();
		modelMap_Nether.clear();
		modelMap_TheEnd.clear();
	}
}
