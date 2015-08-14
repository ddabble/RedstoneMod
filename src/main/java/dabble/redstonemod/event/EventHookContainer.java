package dabble.redstonemod.event;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.tileentity.TileEntityRedstonePaste;
import dabble.redstonemod.util.ModelLookup;
import dabble.redstonemod.util.PowerLookup;

public class EventHookContainer {

	@SubscribeEvent
	public void chunkLoadHandler(ChunkEvent.Load event) {
		/*
		 * For use when I figure out how to customise the rendering of non-tileEntities
		 * 
		 * for (ExtendedBlockStorage arr : event.getChunk().getBlockStorageArray()) {
		 * if (arr != null) {
		 * for (short i = 0; i < 4096; ++i) {
		 * if (((IBlockState) Block.BLOCK_STATE_IDS.getByValue(arr.getData()[i])).getBlock() instanceof BlockRedstonePasteWire) {
		 * // System.out.println("x:" + (i) + ", y:" + (i >> 8) + ", z:" + (i));
		 * System.out.println(Minecraft.getMinecraft().theWorld);
		 * }
		 * }
		 * }
		 * }
		 */

		if (event.world.isRemote)
			return;

		BlockRedstonePasteWire.isDebugWorld = event.world.getWorldInfo().getTerrainType() == WorldType.DEBUG_WORLD;

		@SuppressWarnings("unchecked")
		Map<BlockPos, TileEntity> tileEntityMap = event.getChunk().getTileEntityMap();

		for (Entry<BlockPos, TileEntity> tileEntity : tileEntityMap.entrySet()) {

			if (tileEntity.getValue() instanceof TileEntityRedstonePaste) {
				BlockRedstonePasteWire block = (BlockRedstonePasteWire) tileEntity.getValue().getBlockType();

				if (block.updatePower(tileEntity.getKey(), event.world))
					block.updateSurroundingBlocks(tileEntity.getKey(), event.world);
			}
		}
	}

	@SubscribeEvent
	public void worldUnloadHandler(WorldEvent.Unload event) {

		// Apparently this is only true when quitting to the title screen
		if (!event.world.isRemote && event.world.provider.getDimensionId() == 0) {
			ModelLookup.clearModels();
			PowerLookup.clearPower();
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void boundingBoxHandler(DrawBlockHighlightEvent event) {
		MovingObjectPosition target = event.target;
		if (target.typeOfHit != MovingObjectType.BLOCK)
			return;

		EntityPlayer player = event.player;
		BlockPos pos = target.getBlockPos();
		if (!(player.worldObj.getBlockState(pos).getBlock() instanceof BlockRedstonePasteWire))
			return;

		AxisAlignedBB boundingBox = null;
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		switch ((EnumFacing) target.hitInfo) {
			case DOWN:
				boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1 / 16.0, z + 1);
				break;

			case UP:
				boundingBox = new AxisAlignedBB(x, y + 15 / 16.0, z, x + 1, y + 1, z + 1);
				break;

			case NORTH:
				boundingBox = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1 / 16.0);
				break;

			case SOUTH:
				boundingBox = new AxisAlignedBB(x, y, z + 15 / 16.0, x + 1, y + 1, z + 1);
				break;

			case WEST:
				boundingBox = new AxisAlignedBB(x, y, z, x + 1 / 16.0, y + 1, z + 1);
				break;

			case EAST:
				boundingBox = new AxisAlignedBB(x + 15 / 16.0, y, z, x + 1, y + 1, z + 1);
				break;
		}

		Tessellator tessellator = Tessellator.getInstance();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0, 0, 0, 0.4f);
		GL11.glLineWidth(2);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;
		double magicNum = 0.0020000000949949026;

		drawOutlinedBoundingBox(tessellator, tessellator.getWorldRenderer(), boundingBox.expand(magicNum, magicNum, magicNum).offset(-d0, -d1, -d2));

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		event.setCanceled(true);
	}

	private void drawOutlinedBoundingBox(Tessellator tessellator, WorldRenderer worldRenderer, AxisAlignedBB boundingBox) {
		worldRenderer.startDrawing(3);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		tessellator.draw();
		worldRenderer.startDrawing(3);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		tessellator.draw();
		worldRenderer.startDrawing(1);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		worldRenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		tessellator.draw();
	}
}
