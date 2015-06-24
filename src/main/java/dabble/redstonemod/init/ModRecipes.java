package dabble.redstonemod.init;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModRecipes {

	public static void register() {
		GameRegistry.addShapelessRecipe(new ItemStack(ModItems.redstone_paste, 8), Items.water_bucket, Items.redstone, Items.redstone, Items.redstone, Items.redstone, Items.redstone, Items.redstone, Items.redstone, Items.redstone);
		// TODO: Make redstone paste smelt like sponge
		GameRegistry.addSmelting(new ItemStack(ModItems.redstone_paste), new ItemStack(Items.redstone), 0);
	}
}
