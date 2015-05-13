package dabble.redstonemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import dabble.redstonemod.init.ModBlocks;
import dabble.redstonemod.init.ModItems;
import dabble.redstonemod.init.ModRecipes;
import dabble.redstonemod.proxy.CommonProxy;
import dabble.redstonemod.renderer.EventHookContainer;
import dabble.redstonemod.renderer.RedstonePasteRenderer;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;

@Mod(modid = RedstoneMod.MOD_ID, name = RedstoneMod.MOD_NAME, version = RedstoneMod.VERSION)
public class RedstoneMod {
	public static final String MOD_ID = "redstonemod";
	public static final String MOD_NAME = "Redstone Mod";
	public static final String VERSION = "1.0";
	public static final String CLIENT_PROXY_CLASS = "dabble.redstonemod.proxy.ClientProxy";
	public static final String SERVER_PROXY_CLASS = "dabble.redstonemod.proxy.CommonProxy";

	@SidedProxy(clientSide = RedstoneMod.CLIENT_PROXY_CLASS, serverSide = RedstoneMod.SERVER_PROXY_CLASS)
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

		GameRegistry.registerTileEntity(TileEntityRedstonePaste.class, "tileEntityRedstonePaste");
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedstonePaste.class, new RedstonePasteRenderer());
		MinecraftForge.EVENT_BUS.register(new EventHookContainer());
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
