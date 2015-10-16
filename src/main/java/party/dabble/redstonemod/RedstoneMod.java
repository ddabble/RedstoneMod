package party.dabble.redstonemod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import party.dabble.redstonemod.event.EventHookContainer;
import party.dabble.redstonemod.init.ModBlocks;
import party.dabble.redstonemod.init.ModItems;
import party.dabble.redstonemod.init.ModRecipes;
import party.dabble.redstonemod.proxy.CommonProxy;
import party.dabble.redstonemod.tileentity.TileEntityRedstonePaste;

@Mod(modid = RedstoneMod.ID, name = RedstoneMod.NAME, version = RedstoneMod.VERSION)
public class RedstoneMod {
	public static final String ID = "redstonemod";
	public static final String NAME = "Redstone Mod";
	public static final String VERSION = "1.0";
	public static final String SERVER_PROXY_CLASS = "party.dabble.redstonemod.proxy.CommonProxy";
	public static final String CLIENT_PROXY_CLASS = "party.dabble.redstonemod.proxy.ClientProxy";

	@Instance(ID)
	static RedstoneMod instance = new RedstoneMod();

	@SidedProxy(clientSide = CLIENT_PROXY_CLASS, serverSide = SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// config = new RedstoneModConfig(event.getSuggestedConfigurationFile());

		ModBlocks.init();
		ModBlocks.register();
		ModItems.init();
		ModItems.register();
		ModRecipes.register();

		GameRegistry.registerTileEntity(TileEntityRedstonePaste.class, "tileEntityRedstonePaste");
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
