package dabble.redstonemod.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import dabble.redstonemod.block.BlockRedstonePasteWire;

public class EventHookContainer {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void boundingBoxHandler(DrawBlockHighlightEvent event) {

		EntityPlayer player = event.player;
		if (player == null)
			return;

		MovingObjectPosition target = event.target;
		if (target == null)
			return;

		BlockPos pos = target.getBlockPos();
		Block block = player.worldObj.getBlockState(pos).getBlock();
		if (!(block instanceof BlockRedstonePasteWire))
			return;

		if (target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return;

		AxisAlignedBB boundingBox = null;
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		EnumFacing side = getSideLookingAt(target.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ()), player.cameraPitch, player.cameraYaw, target.sideHit);

		switch (side) {
			case DOWN:
				boundingBox = new AxisAlignedBB(x + 0, y + 0, z + 0, x + 1, y + 1 / 16f, z + 1);
				break;
			case UP:
				break;
			case NORTH:
				break;
			case SOUTH:
				break;
			case WEST:
				break;
			case EAST:
				break;
			default:
				return;
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(0, 0, 0, 0.4f);
		GL11.glLineWidth(2);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		float partialTicks = event.partialTicks;
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		drawOutlinedBoundingBox(tessellator, worldRenderer, boundingBox.offset(-d0, -d1, -d2));

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		event.setCanceled(true);
	}

	private EnumFacing getSideLookingAt(Vec3 hitVec, float pitch, float yaw, EnumFacing sideHit) {

		return EnumFacing.DOWN;
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
