package dabble.redstonemod.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import dabble.redstonemod.Reference;
import dabble.redstonemod.item.ItemRedstonePaste;

public class ModItems {

	public static Item redstone_paste;

	public static void init() {
		redstone_paste = new ItemRedstonePaste().setUnlocalizedName("redstone_paste").setCreativeTab(CreativeTabs.tabRedstone);
	}

	public static void register() {
		GameRegistry.registerItem(redstone_paste, redstone_paste.getUnlocalizedName().substring(5)); // "tile.redstone_paste" <-- Skips "tile."
	}

	public static void registerRenders() {
		registerRender(redstone_paste);
	}

	public static void registerRender(Item item) {
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
}
