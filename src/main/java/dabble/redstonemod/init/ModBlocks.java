package dabble.redstonemod.init;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import dabble.redstonemod.Reference;
import dabble.redstonemod.block.BlockRedstonePasteWire;

public class ModBlocks {
	public static Block redstone_paste_wire;

	public static void init() {
		redstone_paste_wire = new BlockRedstonePasteWire().setHardness(0.0F).setStepSound(Block.soundTypeStone).setUnlocalizedName("redstone_paste_wire");
	}

	public static void register() {
		GameRegistry.registerBlock(redstone_paste_wire, redstone_paste_wire.getUnlocalizedName().substring(5));
	}

	public static void registerRenders() {
		registerRender(redstone_paste_wire);
	}

	public static void registerRender(Block block) {
		Item item = Item.getItemFromBlock(block);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}
}
