package dabble.redstonemod.proxy;

import dabble.redstonemod.init.ModItems;
import dabble.redstonemod.renderer.RedstonePasteRenderer;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenders() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedstonePaste.class, new RedstonePasteRenderer());
		ModItems.registerRenders();
	}
}
