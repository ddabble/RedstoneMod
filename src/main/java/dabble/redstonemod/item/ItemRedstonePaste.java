package dabble.redstonemod.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.block.BlockRedstonePasteWire_SinglePasted;

public class ItemRedstonePaste extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		BlockPos currentPos = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(side);

		if (!player.canPlayerEdit(currentPos, side, stack))
			return false;
		else {
			IBlockState state = world.getBlockState(currentPos);
			Block currentBlock = state.getBlock();
			if (currentBlock instanceof BlockRedstonePasteWire && BlockRedstonePasteWire.canPasteOnSideOfBlock(side, pos, world)) {
				IBlockState stateWithAdditionalPastedSide = ((BlockRedstonePasteWire) currentBlock).pasteAdditionalSide(side.getOpposite(), state, currentPos, player, world);
				if (stateWithAdditionalPastedSide != null) {
					--stack.stackSize;
					world.setBlockState(currentPos, stateWithAdditionalPastedSide, 2);
					return true;
				} else
					return false;
			}

			if (!world.canBlockBePlaced(currentBlock, currentPos, false, side, null, stack))
				return false;

			if (world.provider.getDimensionId() == -1) {

				if (BlockRedstonePasteWire.getFirstPasteableSide(side.getOpposite(), currentPos, world) != null) {
					--stack.stackSize;
					world.setBlockState(currentPos, Blocks.redstone_wire.getDefaultState());
					return true;
				} else
					return false;
			} else {
				EnumFacing firstPasteableSide = BlockRedstonePasteWire.getFirstPasteableSide(side.getOpposite(), currentPos, world);
				if (firstPasteableSide != null) {
					--stack.stackSize;
					world.setBlockState(currentPos, BlockRedstonePasteWire_SinglePasted.getStateFromSide(firstPasteableSide), 2);
					return true;
				} else
					return false;
			}
		}
	}
}
