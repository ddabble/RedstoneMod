package dabble.redstonemod.proxy;

import dabble.redstonemod.init.ModBlocks;
import dabble.redstonemod.init.ModItems;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenders() {
		// This is for rendering entities and so forth later on
		ModBlocks.registerRenders();
		ModItems.registerRenders();
	}
}
