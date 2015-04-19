package dabble.redstonemod.init;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
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
	public static BlockRedstonePasteWire redstone_paste_wire_up, redstone_paste_wire_down, redstone_paste_wire_north, redstone_paste_wire_south, redstone_paste_wire_east, redstone_paste_wire_west,
			redstone_paste_wire_up_north, redstone_paste_wire_up_south, redstone_paste_wire_up_east, redstone_paste_wire_up_west,
			redstone_paste_wire_down_north, redstone_paste_wire_down_south, redstone_paste_wire_down_east, redstone_paste_wire_down_west,
			redstone_paste_wire_north_east, redstone_paste_wire_north_west, redstone_paste_wire_south_east, redstone_paste_wire_south_west;
	private static int down = EnumFacing.DOWN.getIndex() << 1,
			up = EnumFacing.UP.getIndex() << 1,
			north = EnumFacing.NORTH.getIndex() << 1,
			south = EnumFacing.SOUTH.getIndex() << 1,
			west = EnumFacing.WEST.getIndex() << 1,
			east = EnumFacing.EAST.getIndex() << 1;

	public static void init() {
		singleSideMap.put(0, redstone_paste_wire_down = new BlockRedstonePasteWire_Down("redstone_paste_wire_down"));
		singleSideMap.put(1, redstone_paste_wire_up = new BlockRedstonePasteWire_Up("redstone_paste_wire_up"));
		singleSideMap.put(2, redstone_paste_wire_north = new BlockRedstonePasteWire_North("redstone_paste_wire_north"));
		singleSideMap.put(3, redstone_paste_wire_south = new BlockRedstonePasteWire_South("redstone_paste_wire_south"));
		singleSideMap.put(4, redstone_paste_wire_west = new BlockRedstonePasteWire_West("redstone_paste_wire_west"));
		singleSideMap.put(5, redstone_paste_wire_east = new BlockRedstonePasteWire_East("redstone_paste_wire_east"));

		doubleSideMap.put(down + north + (down * north), redstone_paste_wire_down_north = new BlockRedstonePasteWire_DownNorth("redstone_paste_wire_down_north"));
		doubleSideMap.put(down + south + (down * south), redstone_paste_wire_down_south = new BlockRedstonePasteWire_DownSouth("redstone_paste_wire_down_south"));
		doubleSideMap.put(down + west + (down * west), redstone_paste_wire_down_west = new BlockRedstonePasteWire_DownWest("redstone_paste_wire_down_west"));
		doubleSideMap.put(down + east + (down * east), redstone_paste_wire_down_east = new BlockRedstonePasteWire_DownEast("redstone_paste_wire_down_east"));

		doubleSideMap.put(up + north + (up * north), redstone_paste_wire_up_north = new BlockRedstonePasteWire_UpNorth("redstone_paste_wire_up_north"));
		doubleSideMap.put(up + south + (up * south), redstone_paste_wire_up_south = new BlockRedstonePasteWire_UpSouth("redstone_paste_wire_up_south"));
		doubleSideMap.put(up + west + (up * west), redstone_paste_wire_up_west = new BlockRedstonePasteWire_UpWest("redstone_paste_wire_up_west"));
		doubleSideMap.put(up + east + (up * east), redstone_paste_wire_up_east = new BlockRedstonePasteWire_UpEast("redstone_paste_wire_up_east"));

		doubleSideMap.put(north + west + (north * west), redstone_paste_wire_north_west = new BlockRedstonePasteWire_NorthWest("redstone_paste_wire_north_west"));
		doubleSideMap.put(north + east + (north * east), redstone_paste_wire_north_east = new BlockRedstonePasteWire_NorthEast("redstone_paste_wire_north_east"));
		doubleSideMap.put(south + west + (south * west), redstone_paste_wire_south_west = new BlockRedstonePasteWire_SouthWest("redstone_paste_wire_south_west"));
		doubleSideMap.put(south + east + (south * east), redstone_paste_wire_south_east = new BlockRedstonePasteWire_SouthEast("redstone_paste_wire_south_east"));
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
