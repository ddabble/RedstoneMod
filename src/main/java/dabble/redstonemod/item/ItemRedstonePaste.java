package dabble.redstonemod.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.init.ModBlocks;

public class ItemRedstonePaste extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		boolean flag = world.getBlockState(pos).getBlock().isReplaceable(world, pos);
		BlockPos currentPos = flag ? pos : pos.offset(side);

		if (!player.canPlayerEdit(currentPos, side, stack))
			return false;
		else {
			Block currentBlock = world.getBlockState(currentPos).getBlock();

			if (currentBlock instanceof BlockRedstonePasteWire) {
				BlockRedstonePasteWire currentRedstonePaste = (BlockRedstonePasteWire) currentBlock;

				if (!currentRedstonePaste.isDoubleFaced && currentRedstonePaste.pastedSide != side) {
					int sideIndex = side.getOpposite().getIndex() << 1;
					int pastedSideIndex = currentRedstonePaste.pastedSide.getIndex() << 1;
					Block modBlocksBlock = ModBlocks.doubleSideMap.get(sideIndex + pastedSideIndex + (sideIndex * pastedSideIndex));
					world.setBlockState(currentPos, modBlocksBlock.getDefaultState());
					return true;
				} else
					return false;
			}

			if (!world.canBlockBePlaced(currentBlock, currentPos, false, side, null, stack))
				return false;
			else if (world.provider.getDimensionId() == -1) {

				if (BlockRedstonePasteWire.getFirstPasteableSide(world, currentPos, side) != null) {
					--stack.stackSize;
					world.setBlockState(currentPos, Blocks.redstone_wire.getDefaultState());
					return true;
				} else
					return false;
			} else {
				EnumFacing firstPasteableSide;

				if ((firstPasteableSide = BlockRedstonePasteWire.getFirstPasteableSide(world, currentPos, side)) != null) {
					--stack.stackSize;
					Block redstonePasteBlock = ModBlocks.singleSideMap.get(firstPasteableSide.getOpposite().getIndex());
					world.setBlockState(currentPos, redstonePasteBlock.getDefaultState());
					return true;
				} else
					return false;
			}
		}
	}
}
