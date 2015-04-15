package dabble.redstonemod.init;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.block.BlockRedstonePasteWire_Down;
import dabble.redstonemod.block.BlockRedstonePasteWire_DownEast;
import dabble.redstonemod.block.BlockRedstonePasteWire_DownNorth;
import dabble.redstonemod.block.BlockRedstonePasteWire_DownSouth;
import dabble.redstonemod.block.BlockRedstonePasteWire_DownWest;
import dabble.redstonemod.block.BlockRedstonePasteWire_East;
import dabble.redstonemod.block.BlockRedstonePasteWire_North;
import dabble.redstonemod.block.BlockRedstonePasteWire_NorthEast;
import dabble.redstonemod.block.BlockRedstonePasteWire_NorthWest;
import dabble.redstonemod.block.BlockRedstonePasteWire_South;
import dabble.redstonemod.block.BlockRedstonePasteWire_SouthEast;
import dabble.redstonemod.block.BlockRedstonePasteWire_SouthWest;
import dabble.redstonemod.block.BlockRedstonePasteWire_Up;
import dabble.redstonemod.block.BlockRedstonePasteWire_UpEast;
import dabble.redstonemod.block.BlockRedstonePasteWire_UpNorth;
import dabble.redstonemod.block.BlockRedstonePasteWire_UpSouth;
import dabble.redstonemod.block.BlockRedstonePasteWire_UpWest;
import dabble.redstonemod.block.BlockRedstonePasteWire_West;

public class ModBlocks {
	public static HashMap<Integer, BlockRedstonePasteWire> singleSideMap = new HashMap<Integer, BlockRedstonePasteWire>(6);
	public static HashMap<Integer, BlockRedstonePasteWire> doubleSideMap = new HashMap<Integer, BlockRedstonePasteWire>(12);
	public static BlockRedstonePasteWire redstone_paste_wire_up, redstone_paste_wire_down, redstone_paste_wire_north, redstone_paste_wire_south, redstone_paste_wire_east, redstone_paste_wire_west;
	public static BlockRedstonePasteWire redstone_paste_wire_up_north, redstone_paste_wire_up_south, redstone_paste_wire_up_east, redstone_paste_wire_up_west;
	public static BlockRedstonePasteWire redstone_paste_wire_down_north, redstone_paste_wire_down_south, redstone_paste_wire_down_east, redstone_paste_wire_down_west;
	public static BlockRedstonePasteWire redstone_paste_wire_north_east, redstone_paste_wire_north_west, redstone_paste_wire_south_east, redstone_paste_wire_south_west;

	public static void init() {
		singleSideMap.put(0, redstone_paste_wire_down = new BlockRedstonePasteWire_Down("redstone_paste_wire_down"));
		singleSideMap.put(1, redstone_paste_wire_up = new BlockRedstonePasteWire_Up("redstone_paste_wire_up"));
		singleSideMap.put(2, redstone_paste_wire_north = new BlockRedstonePasteWire_North("redstone_paste_wire_north"));
		singleSideMap.put(3, redstone_paste_wire_south = new BlockRedstonePasteWire_South("redstone_paste_wire_south"));
		singleSideMap.put(4, redstone_paste_wire_west = new BlockRedstonePasteWire_West("redstone_paste_wire_west"));
		singleSideMap.put(5, redstone_paste_wire_east = new BlockRedstonePasteWire_East("redstone_paste_wire_east"));

		doubleSideMap.put(2 + 0, redstone_paste_wire_down_north = new BlockRedstonePasteWire_DownNorth("redstone_paste_wire_down_north"));
		doubleSideMap.put(3 + 0, redstone_paste_wire_down_south = new BlockRedstonePasteWire_DownSouth("redstone_paste_wire_down_south"));
		doubleSideMap.put(4 + 0, redstone_paste_wire_down_west = new BlockRedstonePasteWire_DownWest("redstone_paste_wire_down_west"));
		doubleSideMap.put(5 + 0, redstone_paste_wire_down_east = new BlockRedstonePasteWire_DownEast("redstone_paste_wire_down_east"));

		doubleSideMap.put(3 + 2, redstone_paste_wire_up_north = new BlockRedstonePasteWire_UpNorth("redstone_paste_wire_up_north"));
		doubleSideMap.put(4 + 3, redstone_paste_wire_up_south = new BlockRedstonePasteWire_UpSouth("redstone_paste_wire_up_south"));
		doubleSideMap.put(5 + 4, redstone_paste_wire_up_west = new BlockRedstonePasteWire_UpWest("redstone_paste_wire_up_west"));
		doubleSideMap.put(6 + 5, redstone_paste_wire_up_east = new BlockRedstonePasteWire_UpEast("redstone_paste_wire_up_east"));

		doubleSideMap.put(6 + 8, redstone_paste_wire_north_west = new BlockRedstonePasteWire_NorthWest("redstone_paste_wire_north_west"));
		doubleSideMap.put(7 + 10, redstone_paste_wire_north_east = new BlockRedstonePasteWire_NorthEast("redstone_paste_wire_north_east"));
		doubleSideMap.put(7 + 12, redstone_paste_wire_south_west = new BlockRedstonePasteWire_SouthWest("redstone_paste_wire_south_west"));
		doubleSideMap.put(8 + 15, redstone_paste_wire_south_east = new BlockRedstonePasteWire_SouthEast("redstone_paste_wire_south_east"));
	}

	public static void register() {
		registerBlock(redstone_paste_wire_down);
		registerBlock(redstone_paste_wire_up);
		registerBlock(redstone_paste_wire_north);
		registerBlock(redstone_paste_wire_south);
		registerBlock(redstone_paste_wire_west);
		registerBlock(redstone_paste_wire_east);

		registerBlock(redstone_paste_wire_down_north);
		registerBlock(redstone_paste_wire_down_south);
		registerBlock(redstone_paste_wire_down_west);
		registerBlock(redstone_paste_wire_down_east);

		registerBlock(redstone_paste_wire_up_north);
		registerBlock(redstone_paste_wire_up_south);
		registerBlock(redstone_paste_wire_up_west);
		registerBlock(redstone_paste_wire_up_east);

		registerBlock(redstone_paste_wire_north_west);
		registerBlock(redstone_paste_wire_north_east);
		registerBlock(redstone_paste_wire_south_west);
		registerBlock(redstone_paste_wire_south_east);
	}

	private static void registerBlock(Block block) {
		GameRegistry.registerBlock(block, block.getUnlocalizedName().substring(5));
	}

	// public static void registerRenders() {
	// registerRender(redstone_paste_wire);
	// }
	//
	// public static void registerRender(Block block) {
	// Item item = Item.getItemFromBlock(block);
	// Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(Reference.MOD_ID + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	// }
}
