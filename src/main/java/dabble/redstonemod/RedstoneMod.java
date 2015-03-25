package dabble.redstonemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import dabble.redstonemod.init.ModBlocks;
import dabble.redstonemod.init.ModItems;
import dabble.redstonemod.init.ModRecipes;
import dabble.redstonemod.proxy.CommonProxy;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class RedstoneMod {

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	// @Instance(Reference.NAME)
	// static RedstoneMod instance;

	// @SidedProxy(modId = Reference.MODID, clientSide = "dabble.redstonemod.client.ClientProxy", serverSide = "dabble.redstonemod.server.ServerProxy")
	// public static CommonProxy proxy;

	// public static RedstoneModConfig config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// config = new RedstoneModConfig(event.getSuggestedConfigurationFile());

		// proxy.registerEventHandlers();

		ModBlocks.init();
		ModBlocks.register();
		ModItems.init();
		ModItems.register();
		ModRecipes.register();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// System.out.println(config.whatToPrint);

		proxy.registerRenders();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}
}
