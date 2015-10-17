package party.dabble.redstonemod.world;

import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import party.dabble.redstonemod.block.BlockRedstonePasteWire;
import party.dabble.redstonemod.util.ModelLookup;
import party.dabble.redstonemod.util.PowerLookup;

public class WorldLoadingHelper {

	public static void handleChunkLoading(ChunkEvent.Load event) {

		if (event.world.isRemote)
			return;

		/*
		 * Necessary for initially filling the memory with redstone paste blocks' power when entering a dimension,
		 * before any manual block changes cause the same.
		 */
		for (ExtendedBlockStorage ebs : event.getChunk().getBlockStorageArray()) {
			if (ebs == null || ebs.isEmpty())
				continue;

			for (short i = 0; i < 0x1000; ++i) {
				BlockPos pos = event.getChunk().getChunkCoordIntPair().getBlock(i & 0x000F, (i >> 8) + ebs.getYLocation(), i >> 4 & 0x000F);

				if (event.world.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire) {
					BlockRedstonePasteWire block = (BlockRedstonePasteWire)event.world.getBlockState(pos).getBlock();
					if (block.updatePower(pos, event.world))
						block.updateSurroundingBlocks(pos, event.world);
				}
			}
		}
	}

	public static void handleWorldLoading(WorldEvent.Load event) {
		BlockRedstonePasteWire.isDebugWorld = event.world.getWorldInfo().getTerrainType() == WorldType.DEBUG_WORLD;
	}

	public static void handleWorldUnloading(WorldEvent.Unload event) {

		if (event.world.isRemote) {
			ModelLookup.clearModels(event.world);

			/*
			 * Necessary for preventing a redstone paste block's power being removed when the player leaves the Overworld for another dimension.
			 * This is because, when in singleplayer, it will never get re-added when the player returns,
			 * due to Minecraft not actually unloading chunks in the Overworld, despite this event being fired.
			 */
			if (net.minecraft.server.MinecraftServer.getServer().isDedicatedServer() && event.world.provider.getDimensionId() == 0)
				PowerLookup.clearPower(event.world);
		}

		/* Apparently this is only true when quitting to the title screen */
		if (!event.world.isRemote && event.world.provider.getDimensionId() == 0) {
			ModelLookup.clearAllModels();
			PowerLookup.clearAllPower();
		}
	}

	public static void handleClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		ModelLookup.clearAllModels();
		PowerLookup.clearAllPower();
	}

	/*
	 * to End
	 * client: unloading 0
	 * from End
	 * client: unloading 1
	 * server: unloading 1
	 * 
	 * to Nether
	 * client: unloading 0
	 * from Nether
	 * client: unloading -1
	 */

	/*
	 * disconnecting in End
	 * client: unloading 1
	 * server: unloading 1
	 * disconnecting in Overworld
	 * client: unloading 0
	 * disconnecting in Nether
	 * client: unloading -1
	 */

	/*
	 * quitting in End
	 * client: unloading 1
	 * server: unloading 0, -1, 1
	 * quitting in Overworld
	 * client: unloading 0
	 * server: unloading 0, -1, 1
	 * quitting in Nether
	 * client: unloading -1
	 * server: unloading 0, -1, 1
	 */
}
