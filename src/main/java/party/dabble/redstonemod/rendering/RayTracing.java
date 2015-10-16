package party.dabble.redstonemod.rendering;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import party.dabble.redstonemod.util.EnumModel;
import party.dabble.redstonemod.util.ModelLookup;

public class RayTracing {
	/** Make sure this equals Minecraft.getMinecraft().playerController.getBlockReachDistance() when in creative mode. */
	private static final float BLOCK_REACH_DISTANCE = 5;
	public static EntityPlayer removingPlayer = null;

	public static MovingObjectPosition rayTraceFromPlayer(EntityPlayer player) {
		Vec3 eyePos = getPositionEyes(player, 1);
		Vec3 lookVec = player.getLook(1);
		Vec3 endVec = eyePos.addVector(lookVec.xCoord * BLOCK_REACH_DISTANCE, lookVec.yCoord * BLOCK_REACH_DISTANCE, lookVec.zCoord * BLOCK_REACH_DISTANCE);
		return player.worldObj.rayTraceBlocks(eyePos, endVec, false, false, true);
	}

	private static Vec3 getPositionEyes(EntityPlayer player, float partialTicks) {
		if (partialTicks == 1.0F)
			return new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		else {
			double d0 = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
			double d1 = player.prevPosY + (player.posY - player.prevPosY) * partialTicks + player.getEyeHeight();
			double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
			return new Vec3(d0, d1, d2);
		}
	}

	public static MovingObjectPosition collisionRayTrace(Vec3 start, Vec3 end, BlockPos pos, World world, MovingObjectPosition superRayTrace, EnumSet<EnumFacing> pastedSides) {
		EnumMap<EnumFacing, EnumModel> model = ModelLookup.getModel(pos, world);
		MovingObjectPosition nearestFaceEdgeMOP = getNearestFaceEdgeMOP(superRayTrace, model, pos, world, pastedSides);

		if (nearestFaceEdgeMOP != null)
			return nearestFaceEdgeMOP;

		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		EnumMap<EnumFacing, Vec3> hitVecs = new EnumMap<EnumFacing, Vec3>(EnumFacing.class);

		Vec3 hitVec = (model.containsKey(EnumFacing.DOWN) && end.yCoord <= start.yCoord) ? getVecInsideXZBounds(start.getIntermediateWithYValue(end, 1 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.DOWN, hitVec);

		hitVec = (model.containsKey(EnumFacing.UP) && end.yCoord >= start.yCoord) ? getVecInsideXZBounds(start.getIntermediateWithYValue(end, 15 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.UP, hitVec);

		hitVec = (model.containsKey(EnumFacing.NORTH) && end.zCoord <= start.zCoord) ? getVecInsideXYBounds(start.getIntermediateWithZValue(end, 1 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.NORTH, hitVec);

		hitVec = (model.containsKey(EnumFacing.SOUTH) && end.zCoord >= start.zCoord) ? getVecInsideXYBounds(start.getIntermediateWithZValue(end, 15 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.SOUTH, hitVec);

		hitVec = (model.containsKey(EnumFacing.WEST) && end.xCoord <= start.xCoord) ? getVecInsideYZBounds(start.getIntermediateWithXValue(end, 1 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.WEST, hitVec);

		hitVec = (model.containsKey(EnumFacing.EAST) && end.xCoord >= start.xCoord) ? getVecInsideYZBounds(start.getIntermediateWithXValue(end, 15 / 16.0), model) : null;
		if (hitVec != null)
			hitVecs.put(EnumFacing.EAST, hitVec);

		hitVec = null;
		EnumFacing sideLookingAt = null;

		if (hitVecs.size() == 0)
			return null;
		else if (hitVecs.size() == 1) {
			Entry<EnumFacing, Vec3> vec = hitVecs.entrySet().iterator().next();
			hitVec = vec.getValue();
			sideLookingAt = vec.getKey();
		} else {

			for (Entry<EnumFacing, Vec3> vec : hitVecs.entrySet()) {

				if (hitVec == null) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				} else if (start.squareDistanceTo(vec.getValue()) < start.squareDistanceTo(hitVec)) {
					hitVec = vec.getValue();
					sideLookingAt = vec.getKey();
				}
			}
		}

		if (hitVec == null
				|| world.isRemote && Minecraft.getMinecraft().thePlayer.isSneaking() && !pastedSides.contains(sideLookingAt)
				|| removingPlayer != null && removingPlayer.isSneaking() && !pastedSides.contains(sideLookingAt))
			return null;

		MovingObjectPosition redstonePasteHit = new MovingObjectPosition(hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), sideLookingAt.getOpposite(), pos);
		redstonePasteHit.hitInfo = sideLookingAt;
		return redstonePasteHit;
	}

	private static MovingObjectPosition getNearestFaceEdgeMOP(MovingObjectPosition nearestMOP, EnumMap<EnumFacing, EnumModel> model, BlockPos pos, World world, EnumSet<EnumFacing> pastedSides) {
		if (nearestMOP == null)
			return null;

		Vec3 hitVec = nearestMOP.hitVec.subtract(pos.getX(), pos.getY(), pos.getZ());
		Axis sideHitAxis = nearestMOP.sideHit.getAxis();
		EnumFacing sideLookingAt = null;
		byte facesHit = 0;

		if (sideHitAxis != Axis.X && hitVec.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) {
			sideLookingAt = EnumFacing.WEST;
			++facesHit;
		}

		if (sideHitAxis != Axis.X && hitVec.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST)) {
			sideLookingAt = EnumFacing.EAST;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) {
			sideLookingAt = EnumFacing.DOWN;
			++facesHit;
		}

		if (sideHitAxis != Axis.Y && hitVec.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP)) {
			sideLookingAt = EnumFacing.UP;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) {
			sideLookingAt = EnumFacing.NORTH;
			++facesHit;
		}

		if (sideHitAxis != Axis.Z && hitVec.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH)) {
			sideLookingAt = EnumFacing.SOUTH;
			++facesHit;
		}

		if (facesHit == 0)
			return null;
		else if (facesHit > 1) {

			switch (sideHitAxis) {
				case X:
					if (hitVec.yCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.yCoord && hitVec.zCoord >= hitVec.yCoord)
						sideLookingAt = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.yCoord && hitVec.zCoord >= 1 - hitVec.yCoord)
						sideLookingAt = EnumFacing.UP;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.zCoord && hitVec.yCoord >= hitVec.zCoord)
						sideLookingAt = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.zCoord && hitVec.yCoord >= 1 - hitVec.zCoord)
						sideLookingAt = EnumFacing.SOUTH;

					break;

				case Y:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.zCoord <= 1 - hitVec.xCoord && hitVec.zCoord >= hitVec.xCoord)
						sideLookingAt = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.zCoord <= hitVec.xCoord && hitVec.zCoord >= 1 - hitVec.xCoord)
						sideLookingAt = EnumFacing.EAST;
					else if (hitVec.zCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.zCoord && hitVec.xCoord >= hitVec.zCoord)
						sideLookingAt = EnumFacing.NORTH;
					else if (hitVec.zCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.zCoord && hitVec.xCoord >= 1 - hitVec.zCoord)
						sideLookingAt = EnumFacing.SOUTH;

					break;

				case Z:
					if (hitVec.xCoord <= 1 / 16.0 && hitVec.yCoord <= 1 - hitVec.xCoord && hitVec.yCoord >= hitVec.xCoord)
						sideLookingAt = EnumFacing.WEST;
					else if (hitVec.xCoord >= 15 / 16.0 && hitVec.yCoord <= hitVec.xCoord && hitVec.yCoord >= 1 - hitVec.xCoord)
						sideLookingAt = EnumFacing.EAST;
					else if (hitVec.yCoord <= 1 / 16.0 && hitVec.xCoord <= 1 - hitVec.yCoord && hitVec.xCoord >= hitVec.yCoord)
						sideLookingAt = EnumFacing.DOWN;
					else if (hitVec.yCoord >= 15 / 16.0 && hitVec.xCoord <= hitVec.yCoord && hitVec.xCoord >= 1 - hitVec.yCoord)
						sideLookingAt = EnumFacing.UP;

					break;
			}
		}

		if (world.isRemote && Minecraft.getMinecraft().thePlayer.isSneaking() && !pastedSides.contains(sideLookingAt)
				|| removingPlayer != null && removingPlayer.isSneaking() && !pastedSides.contains(sideLookingAt))
			return null;

		MovingObjectPosition redstonePasteHit = new MovingObjectPosition(nearestMOP.hitVec, nearestMOP.sideHit, pos);
		redstonePasteHit.hitInfo = sideLookingAt;
		return redstonePasteHit;
	}

	/**
	 * Returns the provided vector if it is within the X and Z bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private static Vec3 getVecInsideXZBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.xCoord < 0 || point.xCoord > 1 || point.zCoord < 0 || point.zCoord > 1)
			return null;

		return (!(point.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) && !(point.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST))
				&& !(point.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) && !(point.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH))) ? point : null;
	}

	/**
	 * Returns the provided vector if it is within the X and Y bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private static Vec3 getVecInsideXYBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.xCoord < 0 || point.xCoord > 1 || point.yCoord < 0 || point.yCoord > 1)
			return null;

		return (!(point.xCoord <= 1 / 16.0 && model.containsKey(EnumFacing.WEST)) && !(point.xCoord >= 15 / 16.0 && model.containsKey(EnumFacing.EAST))
				&& !(point.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) && !(point.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP))) ? point : null;
	}

	/**
	 * Returns the provided vector if it is within the Y and Z bounds of the block and if it's not being covered by another rendered face, or null if that's not the case.
	 */
	private static Vec3 getVecInsideYZBounds(Vec3 point, EnumMap<EnumFacing, EnumModel> model) {

		if (point == null || point.yCoord < 0 || point.yCoord > 1 || point.zCoord < 0 || point.zCoord > 1)
			return null;

		return (!(point.yCoord <= 1 / 16.0 && model.containsKey(EnumFacing.DOWN)) && !(point.yCoord >= 15 / 16.0 && model.containsKey(EnumFacing.UP))
				&& !(point.zCoord <= 1 / 16.0 && model.containsKey(EnumFacing.NORTH)) && !(point.zCoord >= 15 / 16.0 && model.containsKey(EnumFacing.SOUTH))) ? point : null;
	}
}
