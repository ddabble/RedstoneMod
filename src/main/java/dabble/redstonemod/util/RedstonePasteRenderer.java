package dabble.redstonemod.util;

import dabble.redstonemod.block.BlockRedstonePasteWire;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RedstonePasteRenderer extends TileEntitySpecialRenderer {
	private static final ResourceLocation TEXTURE = new ResourceLocation("redstonemod:textures/blocks/redstone_paste_cross.png");

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f, int wat) {
		World worldIn = tileEntity.getWorld();
		BlockPos pos = tileEntity.getPos();
		Block block = tileEntity.getBlockType();
		StringBuffer model = ((BlockRedstonePasteWire) block).getModel(worldIn, pos);

		int colour = colorMultiplier(((Integer) worldIn.getBlockState(pos).getValue(BlockRedstonePasteWire.POWER)).intValue());
		int red = colour >> 16 & 255;
		int green = colour >> 8 & 255;
		int blue = colour & 255;

		this.bindTexture(TEXTURE);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();

		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();

		GlStateManager.translate(x, y, z);
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA(red, green, blue, 255);

		worldRenderer.addVertexWithUV(1, 1, 0, 1, 1);
		worldRenderer.addVertexWithUV(1, 0, 0, 1, 0);
		worldRenderer.addVertexWithUV(0, 0, 0, 0, 0);
		worldRenderer.addVertexWithUV(0, 1, 0, 0, 1);

		tessellator.draw();

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	private int colorMultiplier(int powerLevel) {
		float f = (float) powerLevel / 15.0F;
		float f1 = f * 0.6F + 0.4F;

		if (powerLevel == 0) {
			f1 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f3 = f * f * 0.6F - 0.7F;

		if (f2 < 0.0F) {
			f2 = 0.0F;
		}

		if (f3 < 0.0F) {
			f3 = 0.0F;
		}

		int j = MathHelper.clamp_int((int) (f1 * 255.0F), 0, 255);
		int k = MathHelper.clamp_int((int) (f2 * 255.0F), 0, 255);
		int l = MathHelper.clamp_int((int) (f3 * 255.0F), 0, 255);
		return -16777216 | j << 16 | k << 8 | l;
	}
}
