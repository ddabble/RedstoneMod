package party.dabble.redstonemod.proxy;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import party.dabble.redstonemod.init.ModItems;
import party.dabble.redstonemod.rendering.RedstonePasteRenderer;
import party.dabble.redstonemod.tileentity.TileEntityRedstonePaste;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenders() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedstonePaste.class, new RedstonePasteRenderer());
		ModItems.registerRenders();
	}
}
