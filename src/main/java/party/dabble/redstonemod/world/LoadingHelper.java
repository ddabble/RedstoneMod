package party.dabble.redstonemod.world;

import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import party.dabble.redstonemod.block.BlockRedstonePasteWire;
import party.dabble.redstonemod.util.ModelLookup;
import party.dabble.redstonemod.util.PowerLookup;

public class LoadingHelper {

	public static void handleChunkLoading(ChunkEvent.Load event) {

		if (event.world.isRemote)
			return;

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

		// Apparently this is only true when quitting to the title screen
		if (!event.world.isRemote && event.world.provider.getDimensionId() == 0) {
			ModelLookup.clearModels();
			PowerLookup.clearPower();
		}
	}
}
