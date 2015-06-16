package dabble.redstonemod.renderer;

import java.util.EnumMap;

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
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import dabble.redstonemod.util.EnumModel;

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

		World worldIn = player.worldObj;
		BlockPos pos = target.getBlockPos();
		Block block = worldIn.getBlockState(pos).getBlock();
		if (!(block instanceof BlockRedstonePasteWire))
			return;

		if (target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return;

		EnumFacing faceLookingAt = getFaceLookingAt(player, pos, target, ((BlockRedstonePasteWire) block).getModel(worldIn, pos));
		if (faceLookingAt == null) {
			event.setCanceled(true);
			return;
		}

		AxisAlignedBB boundingBox = null;
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();

		switch (faceLookingAt) {
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

	private EnumFacing getFaceLookingAt(EntityPlayer player, BlockPos pos, MovingObjectPosition target, EnumMap<EnumFacing, EnumModel> model) {
		EnumFacing sideHit = target.sideHit;
		EnumFacing faceLookingAt = getFaceEdgeLookingAt(sideHit, target.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ()), model);

		if (faceLookingAt != null)
			return faceLookingAt;

		double yaw = player.rotationYaw;
		double[] coords = getLookingAtCoords(player, pos, Math.toRadians(player.rotationPitch), Math.toRadians(yaw - 90 * Math.round(yaw / 90)));

		EnumFacing facing = player.getHorizontalFacing();

		if (coords[0] < 1 / 16.0)
			faceLookingAt = EnumFacing.fromAngle(yaw + ((facing.getAxis() == EnumFacing.Axis.X) ? -1 : 1) * facing.getAxisDirection().getOffset() * 90);
		else if (coords[0] > 15 / 16.0)
			faceLookingAt = EnumFacing.fromAngle(yaw + ((facing.getAxis() == EnumFacing.Axis.X) ? 1 : -1) * facing.getAxisDirection().getOffset() * 90);
		else if (coords[1] < 1 / 16.0)
			faceLookingAt = EnumFacing.DOWN;
		else if (coords[1] > 15 / 16.0)
			faceLookingAt = EnumFacing.UP;
		else
			faceLookingAt = facing;

		return (model.containsKey(faceLookingAt)) ? faceLookingAt : null;
	}

	private EnumFacing getFaceEdgeLookingAt(EnumFacing sideHit, Vec3 hitVec, EnumMap<EnumFacing, EnumModel> model) {
		double[] coords = new double[2];
		switch (sideHit) {
			case DOWN:
				coords[0] = 1 - hitVec.zCoord;
				coords[1] = hitVec.xCoord;
				break;
			case UP:
				coords[0] = hitVec.zCoord;
				coords[1] = 1 - hitVec.xCoord;
				break;
			case NORTH:
				coords[0] = 1 - hitVec.xCoord;
				coords[1] = hitVec.yCoord;
				break;
			case SOUTH:
				coords[0] = hitVec.xCoord;
				coords[1] = 1 - hitVec.yCoord;
				break;
			case WEST:
				coords[0] = 1 - hitVec.yCoord;
				coords[1] = hitVec.zCoord;
				break;
			case EAST:
				coords[0] = hitVec.yCoord;
				coords[1] = 1 - hitVec.zCoord;
				break;
		}

		EnumFacing faceLookingAt = null;

		if (coords[0] < 1 / 16.0)
			faceLookingAt = sideHit.rotateAround(EnumFacing.getFront(sideHit.getIndex() + 4).getAxis());
		else if (coords[0] > 15 / 16.0)
			faceLookingAt = EnumModel.rotateCCWAround(sideHit, EnumFacing.getFront(sideHit.getIndex() + 4).getAxis());
		else if (coords[1] < 1 / 16.0)
			faceLookingAt = sideHit.rotateAround(EnumFacing.getFront(sideHit.getIndex() + 2).getAxis());
		else if (coords[1] > 15 / 16.0)
			faceLookingAt = EnumModel.rotateCCWAround(sideHit, EnumFacing.getFront(sideHit.getIndex() + 2).getAxis());

		return (model.containsKey(faceLookingAt)) ? faceLookingAt : null;
	}

	private double[] getLookingAtCoords(EntityPlayer player, BlockPos pos, double pitch, double yaw) {
		double eyeY = player.posY + player.getEyeHeight() - pos.getY();
		double eyeXZ = 0;
		int blockXZ = 0;
		double distanceToBlock = 0;
		byte positiveLeftSide = 0;

		switch (player.getHorizontalFacing()) {
			case NORTH:
				eyeXZ = player.posX;
				blockXZ = pos.getX();
				distanceToBlock = player.posZ - (pos.getZ() + 1 / 16.0);
				positiveLeftSide = 1;
				break;
			case SOUTH:
				eyeXZ = player.posX;
				blockXZ = pos.getX();
				distanceToBlock = pos.getZ() + 15 / 16.0 - player.posZ;
				positiveLeftSide = -1;
				break;
			case WEST:
				eyeXZ = player.posZ;
				blockXZ = pos.getZ();
				distanceToBlock = player.posX - (pos.getX() + 1 / 16.0);
				positiveLeftSide = -1;
				break;
			case EAST:
				eyeXZ = player.posZ;
				blockXZ = pos.getZ();
				distanceToBlock = pos.getX() + 15 / 16.0 - player.posX;
				positiveLeftSide = 1;
				break;
			default:
				break;
		}

		double lookingAtY = getLookingAtY(eyeY, distanceToBlock, pitch, yaw);
		double lookingAtXZ;

		if (lookingAtY > 1 / 16.0 && lookingAtY < 15 / 16.0)
			lookingAtXZ = getLookingAtVerticalXZ(eyeXZ, blockXZ, distanceToBlock, positiveLeftSide, yaw);
		else
			lookingAtXZ = getLookingAtHorizontalXZ(eyeY, eyeXZ, blockXZ, positiveLeftSide, pitch, yaw);

		return new double[] { lookingAtXZ, lookingAtY };
	}

	private double getLookingAtY(double eyeY, double distanceToBlock, double pitch, double yaw) {
		double horizontalDistanceToPointLookingAt = distanceToBlock / Math.cos(yaw);
		double yDifferenceOnBlockHit = horizontalDistanceToPointLookingAt * Math.tan(pitch);
		return eyeY - yDifferenceOnBlockHit;
	}

	private double getLookingAtVerticalXZ(double eyeXZ, int blockXZ, double distanceToBlock, byte positiveLeftSide, double yaw) {
		double horizontalDistanceToPointLookingAt = distanceToBlock / Math.cos(yaw);
		double xzDifferenceOnBlockHit = Math.sqrt(horizontalDistanceToPointLookingAt * horizontalDistanceToPointLookingAt - distanceToBlock * distanceToBlock);
		return eyeXZ + positiveLeftSide * Math.signum(yaw) * xzDifferenceOnBlockHit - blockXZ;
	}

	private double getLookingAtHorizontalXZ(double eyeY, double eyeXZ, int blockXZ, byte positiveLeftSide, double pitch, double yaw) {

		if (pitch < 0)
			eyeY = 1 - eyeY;

		double horizontalDistanceToPointLookingAt = eyeY * Math.tan(Math.PI / 2 - Math.abs(pitch));
		double xzDifferenceOnBlockHit = horizontalDistanceToPointLookingAt * Math.abs(Math.sin(yaw));
		return eyeXZ + positiveLeftSide * Math.signum(yaw) * xzDifferenceOnBlockHit - blockXZ;
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
