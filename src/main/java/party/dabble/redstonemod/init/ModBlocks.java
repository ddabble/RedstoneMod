package party.dabble.redstonemod.init;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_DoublePasted;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_QuadruplePasted;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_QuintuplePasted;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_SinglePasted;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_TriplePasted_OnGround;
import party.dabble.redstonemod.block.BlockRedstonePasteWire_TriplePasted_OnWalls;

public class ModBlocks {
	public static BlockRedstonePasteWire_SinglePasted redstone_paste_single_pasted;
	public static BlockRedstonePasteWire_DoublePasted redstone_paste_double_pasted;
	public static BlockRedstonePasteWire_TriplePasted_OnGround redstone_paste_triple_pasted_on_ground;
	public static BlockRedstonePasteWire_TriplePasted_OnWalls redstone_paste_triple_pasted_on_walls;
	public static BlockRedstonePasteWire_QuadruplePasted redstone_paste_quadruple_pasted;
	public static BlockRedstonePasteWire_QuintuplePasted redstone_paste_quintuple_pasted;

	public static void init() {
		redstone_paste_single_pasted = new BlockRedstonePasteWire_SinglePasted("redstone_paste_single_pasted");
		redstone_paste_double_pasted = new BlockRedstonePasteWire_DoublePasted("redstone_paste_double_pasted");
		redstone_paste_triple_pasted_on_ground = new BlockRedstonePasteWire_TriplePasted_OnGround("redstone_paste_triple_pasted_on_ground");
		redstone_paste_triple_pasted_on_walls = new BlockRedstonePasteWire_TriplePasted_OnWalls("redstone_paste_triple_pasted_on_walls");
		redstone_paste_quadruple_pasted = new BlockRedstonePasteWire_QuadruplePasted("redstone_paste_quadruple_pasted");
		redstone_paste_quintuple_pasted = new BlockRedstonePasteWire_QuintuplePasted("redstone_paste_quintuple_pasted");
	}

	public static void register() {
		registerBlock(redstone_paste_single_pasted);
		registerBlock(redstone_paste_double_pasted);
		registerBlock(redstone_paste_triple_pasted_on_ground);
		registerBlock(redstone_paste_triple_pasted_on_walls);
		registerBlock(redstone_paste_quadruple_pasted);
		registerBlock(redstone_paste_quintuple_pasted);
	}

	private static void registerBlock(Block block) {
		GameRegistry.registerBlock(block, block.getUnlocalizedName().substring(5));
	}
}
