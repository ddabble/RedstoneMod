package party.dabble.redstonemod.event;

import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import party.dabble.redstonemod.rendering.BlockHighlightRenderer;
import party.dabble.redstonemod.world.LoadingHelper;

public class EventHookContainer {

	@SubscribeEvent
	public void chunkLoadHandler(ChunkEvent.Load event) {
		LoadingHelper.handleChunkLoading(event);
	}

	@SubscribeEvent
	public void worldLoadHandler(WorldEvent.Load event) {
		LoadingHelper.handleWorldLoading(event);
	}

	@SubscribeEvent
	public void worldUnloadHandler(WorldEvent.Unload event) {
		LoadingHelper.handleWorldUnloading(event);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void blockHighlightHandler(DrawBlockHighlightEvent event) {
		BlockHighlightRenderer.handleBlockHighlighting(event);
	}
}
