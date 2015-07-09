package dabble.redstonemod.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import dabble.redstonemod.block.BlockRedstonePasteWire;

public class EventHookContainer {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderDebugText(net.minecraftforge.client.event.RenderGameOverlayEvent event) {

		if (event.type == net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.DEBUG) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
				Block block = mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
				if (block instanceof BlockRedstonePasteWire) {
					ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
					String text = ((BlockRedstonePasteWire) block).getDebugInfo();
					int x = res.getScaledWidth() - mc.fontRendererObj.getStringWidth(text) - 2;
					mc.fontRendererObj.drawString(text, x, 137, 0xe0e0e0);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void boundingBoxHandler(DrawBlockHighlightEvent event) {
		MovingObjectPosition target = event.target;
		if (target.typeOfHit != MovingObjectType.BLOCK)
			return;

		EntityPlayer player = event.player;
		World worldIn = player.worldObj;
		BlockPos pos = target.getBlockPos();
		Block block = worldIn.getBlockState(pos).getBlock();
		if (!(block instanceof BlockRedstonePasteWire))
			return;

		AxisAlignedBB boundingBox = null;
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		switch (target.sideHit) {
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
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0, 0, 0, 0.4f);
		GL11.glLineWidth(2);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

		drawOutlinedBoundingBox(tessellator, worldRenderer, boundingBox.offset(-d0, -d1, -d2));

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
