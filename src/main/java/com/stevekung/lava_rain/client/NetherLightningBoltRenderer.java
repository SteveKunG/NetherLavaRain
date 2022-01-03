package com.stevekung.lava_rain.client;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.stevekung.lava_rain.entity.NetherLightningBoltEntity;
import com.stevekung.stevekungslib.utils.ColorUtils;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NetherLightningBoltRenderer extends EntityRenderer<NetherLightningBoltEntity>
{
    public NetherLightningBoltRenderer(EntityRendererManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    @Override
    public void render(NetherLightningBoltEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        float[] afloat = new float[8];
        float[] afloat1 = new float[8];
        float f = 0.0F;
        float f1 = 0.0F;
        Random random = new Random(entityIn.boltVertex);

        for(int i = 7; i >= 0; --i) {
            afloat[i] = f;
            afloat1[i] = f1;
            f += random.nextInt(11) - 5;
            f1 += random.nextInt(11) - 5;
        }

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLightning());
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();

        for(int j = 0; j < 4; ++j) {
            Random random1 = new Random(entityIn.boltVertex);

            for(int k = 0; k < 3; ++k) {
                int l = 7;
                int i1 = 0;
                if (k > 0) {
                    l = 7 - k;
                }

                if (k > 0) {
                    i1 = l - 2;
                }

                float f2 = afloat[l] - f;
                float f3 = afloat1[l] - f1;

                for(int j1 = l; j1 >= i1; --j1) {
                    float f4 = f2;
                    float f5 = f3;
                    if (k == 0) {
                        f2 += random1.nextInt(11) - 5;
                        f3 += random1.nextInt(11) - 5;
                    } else {
                        f2 += random1.nextInt(31) - 15;
                        f3 += random1.nextInt(31) - 15;
                    }

                    float f10 = 0.1F + j * 0.2F;
                    if (k == 0) {
                        f10 = (float)(f10 * (j1 * 0.1D + 1.0D));
                    }

                    float f11 = 0.1F + j * 0.2F;
                    if (k == 0) {
                        f11 *= (j1 - 1) * 0.1F + 1.0F;
                    }

                    ColorUtils rgb = new ColorUtils(255, 77, 0);
                    func_229116_a_(matrix4f, ivertexbuilder, f2, f3, j1, f4, f5, rgb.floatRed(), rgb.floatGreen(), rgb.floatBlue(), f10, f11, false, false, true, false);
                    func_229116_a_(matrix4f, ivertexbuilder, f2, f3, j1, f4, f5, rgb.floatRed(), rgb.floatGreen(), rgb.floatBlue(), f10, f11, true, false, true, true);
                    func_229116_a_(matrix4f, ivertexbuilder, f2, f3, j1, f4, f5, rgb.floatRed(), rgb.floatGreen(), rgb.floatBlue(), f10, f11, true, true, false, true);
                    func_229116_a_(matrix4f, ivertexbuilder, f2, f3, j1, f4, f5, rgb.floatRed(), rgb.floatGreen(), rgb.floatBlue(), f10, f11, false, true, false, false);
                }
            }
        }

    }

    private static void func_229116_a_(Matrix4f p_229116_0_, IVertexBuilder p_229116_1_, float p_229116_2_, float p_229116_3_, int p_229116_4_, float p_229116_5_, float p_229116_6_, float p_229116_7_, float p_229116_8_, float p_229116_9_, float p_229116_10_, float p_229116_11_, boolean p_229116_12_, boolean p_229116_13_, boolean p_229116_14_, boolean p_229116_15_)
    {
        p_229116_1_.pos(p_229116_0_, p_229116_2_ + (p_229116_12_ ? p_229116_11_ : -p_229116_11_), p_229116_4_ * 16, p_229116_3_ + (p_229116_13_ ? p_229116_11_ : -p_229116_11_)).color(p_229116_7_, p_229116_8_, p_229116_9_, 0.3F).endVertex();
        p_229116_1_.pos(p_229116_0_, p_229116_5_ + (p_229116_12_ ? p_229116_10_ : -p_229116_10_), (p_229116_4_ + 1) * 16, p_229116_6_ + (p_229116_13_ ? p_229116_10_ : -p_229116_10_)).color(p_229116_7_, p_229116_8_, p_229116_9_, 0.3F).endVertex();
        p_229116_1_.pos(p_229116_0_, p_229116_5_ + (p_229116_14_ ? p_229116_10_ : -p_229116_10_), (p_229116_4_ + 1) * 16, p_229116_6_ + (p_229116_15_ ? p_229116_10_ : -p_229116_10_)).color(p_229116_7_, p_229116_8_, p_229116_9_, 0.3F).endVertex();
        p_229116_1_.pos(p_229116_0_, p_229116_2_ + (p_229116_14_ ? p_229116_11_ : -p_229116_11_), p_229116_4_ * 16, p_229116_3_ + (p_229116_15_ ? p_229116_11_ : -p_229116_11_)).color(p_229116_7_, p_229116_8_, p_229116_9_, 0.3F).endVertex();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ResourceLocation getEntityTexture(NetherLightningBoltEntity entity)
    {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}