package dabble.redstonemod.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.init.ModBlocks;

public class ItemRedstonePaste extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
		BlockPos currentPos = flag ? pos : pos.offset(side);

		if (!playerIn.canPlayerEdit(currentPos, side, stack))
			return false;
		else {
			Block currentBlock = worldIn.getBlockState(currentPos).getBlock();

			if (currentBlock instanceof BlockRedstonePasteWire) {
				BlockRedstonePasteWire currentRedstonePaste = (BlockRedstonePasteWire) currentBlock;

				if (currentRedstonePaste.isSingleFaced() && currentRedstonePaste.getPastedSide() != side) {
					int sideIndex = side.getOpposite().getIndex();
					int pastedSideIndex = currentRedstonePaste.getPastedSide().getIndex();
					Block modBlocksBlock = ModBlocks.doubleSideMap.get(sideIndex + pastedSideIndex + (sideIndex * pastedSideIndex));
					worldIn.setBlockState(currentPos, modBlocksBlock.getDefaultState());
					return true;
				} else
					return false;
			}

			if (!worldIn.canBlockBePlaced(currentBlock, currentPos, false, side, null, stack))
				return false;
			else {
				EnumFacing firstPasteableSide;

				if ((firstPasteableSide = ModBlocks.redstone_paste_wire_down.canPlaceBlockAt(worldIn, currentPos, side)) != null) {
					--stack.stackSize;
					Block modBlocksBlock = ModBlocks.singleSideMap.get(firstPasteableSide.getOpposite().getIndex());
					worldIn.setBlockState(currentPos, modBlocksBlock.getDefaultState());
					return true;
				} else
					return false;
			}
		}
	}
}
