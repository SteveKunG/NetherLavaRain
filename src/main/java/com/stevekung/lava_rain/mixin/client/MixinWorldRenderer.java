package com.stevekung.lava_rain.mixin.client;

import java.util.Optional;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.stevekung.lava_rain.config.LavaRainClientConfig;
import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.Heightmap;

@SuppressWarnings("deprecation")
@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer
{
    @Shadow
    private Minecraft mc;

    @Shadow
    private int ticks;

    @Shadow
    private int rainSoundTime;

    @Shadow
    private float[] rainSizeX;

    @Shadow
    private float[] rainSizeZ;

    @Shadow
    private static ResourceLocation RAIN_TEXTURES;

    @Shadow
    private static ResourceLocation SNOW_TEXTURES;

    private static final ResourceLocation LAVA_RAIN_TEXTURES = new ResourceLocation("lava_rain:textures/environment/lava_rain.png");
    private static final ResourceLocation ASH_RAIN_TEXTURES = new ResourceLocation("lava_rain:textures/environment/ash_rain.png");
    private static final ResourceLocation WARPED_RAIN_TEXTURES = new ResourceLocation("lava_rain:textures/environment/warped_rain.png");
    private static final ResourceLocation SOUL_RAIN_TEXTURES = new ResourceLocation("lava_rain:textures/environment/soul_rain.png");

    @Inject(method = "renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true, at = @At("HEAD"))
    private void renderRainSnow(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn, CallbackInfo info)
    {
        this.renderLavaRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        this.renderEndLavaRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        this.renderWarpedRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        this.renderSoulRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        this.renderAshRain(lightmapIn, partialTicks, xIn, yIn, zIn);

        int l = this.isFancyRainRender() ? 10 : 5;
        int i = MathHelper.floor(xIn);
        int k = MathHelper.floor(zIn);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for (int j1 = k - l; j1 <= k + l; ++j1)
        {
            for (int k1 = i - l; k1 <= i + l; ++k1)
            {
                blockpos$mutable.setPos(k1, 0, j1);

                if (NetherUtils.isNetherBiomes(this.mc.world.func_241828_r(), this.mc.world.getBiome(blockpos$mutable)))
                {
                    info.cancel();
                }
            }
        }
    }

    @Inject(method = "addRainParticles(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", cancellable = true, at = @At("HEAD"))
    private void addRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo info)
    {
        float f = this.mc.world.getRainStrength(1.0F) / (Minecraft.isFancyGraphicsEnabled() ? 1.0F : 2.0F);

        if (!(f <= 0.0F))
        {
            Random random = new Random(this.ticks * 312987231L);
            IWorldReader iworldreader = this.mc.world;
            BlockPos blockpos = new BlockPos(activeRenderInfoIn.getProjectedView());
            BlockPos blockpos1 = null;
            int i = (int)(100.0F * f * f) / (this.mc.gameSettings.particles == ParticleStatus.DECREASED ? 2 : 1);

            for (int j = 0; j < i; ++j)
            {
                int k = random.nextInt(21) - 10;
                int l = random.nextInt(21) - 10;
                BlockPos blockpos2 = iworldreader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos.add(k, 0, l)).down();

                if (NetherUtils.isNetherBiomes(this.mc.world.func_241828_r(), iworldreader.getBiome(blockpos2)) && blockpos2.getY() > 0 && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10)
                {
                    blockpos1 = blockpos2;

                    if (this.mc.gameSettings.particles == ParticleStatus.MINIMAL)
                    {
                        break;
                    }

                    double d0 = random.nextDouble();
                    double d1 = random.nextDouble();
                    BlockState blockstate = iworldreader.getBlockState(blockpos2);
                    FluidState fluidstate = iworldreader.getFluidState(blockpos2);
                    VoxelShape voxelshape = blockstate.getCollisionShape(iworldreader, blockpos2);
                    double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                    double d3 = fluidstate.getActualHeight(iworldreader, blockpos2);
                    double d4 = Math.max(d2, d3);
                    this.mc.world.addParticle(ParticleTypes.SMOKE, blockpos2.getX() + d0, blockpos2.getY() + d4, blockpos2.getZ() + d1, 0.0D, 0.0D, 0.0D);
                    info.cancel();
                }
            }

            if (blockpos1 != null && random.nextInt(3) < this.rainSoundTime++)
            {
                this.rainSoundTime = 0;

                if (blockpos1.getY() > blockpos.getY() + 1 && iworldreader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos).getY() > MathHelper.floor(blockpos.getY()))
                {
                    this.mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
                }
                else
                {
                    this.mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

    private void renderLavaRain(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn)
    {
        float f = this.mc.world.getRainStrength(partialTicks);

        if (!(f <= 0.0F)) {
            lightmapIn.enableLightmap();
            World world = this.mc.world;
            int i = MathHelper.floor(xIn);
            int j = MathHelper.floor(yIn);
            int k = MathHelper.floor(zIn);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (this.isFancyRainRender()) {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            int i1 = -1;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for(int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = this.rainSizeX[l1] * 0.5D;
                    double d1 = this.rainSizeZ[l1] * 0.5D;
                    blockpos$mutable.setPos(k1, 0, j1);
                    Optional<RegistryKey<Biome>> optional2 = this.mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(this.mc.world.getBiome(blockpos$mutable));

                    if (optional2.get() == Biomes.CRIMSON_FOREST || optional2.get() == Biomes.NETHER_WASTES) {
                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);

                            if (i1 != 0) {
                                if (i1 >= 0) {
                                    tessellator.draw();
                                }
                                i1 = 0;
                                this.mc.getTextureManager().bindTexture(LAVA_RAIN_TEXTURES);
                                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                            float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                            double d2 = k1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - xIn;
                            double d4 = j1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - zIn;
                            float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / l;
                            float f5 = (float)(((1.0F - f4 * f4) * LavaRainClientConfig.GENERAL.rainOpacity.get() + LavaRainClientConfig.GENERAL.rainOpacity.get()) * f);
                            blockpos$mutable.setPos(k1, l2, j1);
                            int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        }
                    }
                }
            }

            if (i1 >= 0) {
                tessellator.draw();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableAlphaTest();
            lightmapIn.disableLightmap();
        }
    }

    private void renderEndLavaRain(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn)
    {
        lightmapIn.enableLightmap();
        World world = this.mc.world;
        int i = MathHelper.floor(xIn);
        int j = MathHelper.floor(yIn);
        int k = MathHelper.floor(zIn);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.enableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        int l = 5;
        if (this.isFancyRainRender()) {
            l = 10;
        }

        RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
        int i1 = -1;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        for(int j1 = k - l; j1 <= k + l; ++j1) {
            for(int k1 = i - l; k1 <= i + l; ++k1) {
                int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                double d0 = this.rainSizeX[l1] * 0.5D;
                double d1 = this.rainSizeZ[l1] * 0.5D;
                blockpos$mutable.setPos(k1, 0, j1);
                Optional<RegistryKey<Biome>> optional = this.mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(this.mc.world.getBiome(blockpos$mutable));

                if (optional.get() == Biomes.THE_END) {
                    int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                    int j2 = j - l;
                    int k2 = j + l;
                    if (j2 < i2) {
                        j2 = i2;
                    }

                    if (k2 < i2) {
                        k2 = i2;
                    }

                    int l2 = i2;
                    if (i2 < j) {
                        l2 = j;
                    }

                    if (j2 != k2) {
                        Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);

                        if (i1 != 0) {
                            if (i1 >= 0) {
                                tessellator.draw();
                            }
                            i1 = 0;
                            this.mc.getTextureManager().bindTexture(LAVA_RAIN_TEXTURES);
                            bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                        }

                        int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                        float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                        double d2 = k1 + 0.25F - xIn;
                        double d4 = j1 + 0.25F - zIn;
                        float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / l;
                        float f5 = ((1.0F - f4 * f4) * 0.25F + 0.25F) * 1.0F;
                        blockpos$mutable.setPos(k1, l2, j1);
                        int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
                        bufferbuilder.pos(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        bufferbuilder.pos(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        bufferbuilder.pos(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        bufferbuilder.pos(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                    }
                }
            }
        }

        if (i1 >= 0) {
            tessellator.draw();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableAlphaTest();
        lightmapIn.disableLightmap();
    }

    private void renderWarpedRain(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn)
    {
        float f = this.mc.world.getRainStrength(partialTicks);
        if (!(f <= 0.0F)) {
            lightmapIn.enableLightmap();
            World world = this.mc.world;
            int i = MathHelper.floor(xIn);
            int j = MathHelper.floor(yIn);
            int k = MathHelper.floor(zIn);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (this.isFancyRainRender()) {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            int i1 = -1;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for(int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = this.rainSizeX[l1] * 0.5D;
                    double d1 = this.rainSizeZ[l1] * 0.5D;
                    blockpos$mutable.setPos(k1, 0, j1);
                    Optional<RegistryKey<Biome>> optional = this.mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(this.mc.world.getBiome(blockpos$mutable));

                    if (optional.get() == Biomes.WARPED_FOREST) {
                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);

                            if (i1 != 0) {
                                if (i1 >= 0) {
                                    tessellator.draw();
                                }
                                i1 = 0;
                                this.mc.getTextureManager().bindTexture(WARPED_RAIN_TEXTURES);
                                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                            float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                            double d2 = k1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - xIn;
                            double d4 = j1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - zIn;
                            float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / l;
                            float f5 = (float)(((1.0F - f4 * f4) * LavaRainClientConfig.GENERAL.rainOpacity.get() + LavaRainClientConfig.GENERAL.rainOpacity.get()) * f);
                            blockpos$mutable.setPos(k1, l2, j1);
                            int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        }
                    }
                }
            }

            if (i1 >= 0) {
                tessellator.draw();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableAlphaTest();
            lightmapIn.disableLightmap();
        }
    }

    private void renderSoulRain(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn)
    {
        float f = this.mc.world.getRainStrength(partialTicks);
        if (!(f <= 0.0F)) {
            lightmapIn.enableLightmap();
            World world = this.mc.world;
            int i = MathHelper.floor(xIn);
            int j = MathHelper.floor(yIn);
            int k = MathHelper.floor(zIn);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (this.isFancyRainRender()) {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            int i1 = -1;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for(int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = this.rainSizeX[l1] * 0.5D;
                    double d1 = this.rainSizeZ[l1] * 0.5D;
                    blockpos$mutable.setPos(k1, 0, j1);
                    Optional<RegistryKey<Biome>> optional = this.mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(this.mc.world.getBiome(blockpos$mutable));

                    if (optional.get() == Biomes.SOUL_SAND_VALLEY) {
                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);

                            if (i1 != 0) {
                                if (i1 >= 0) {
                                    tessellator.draw();
                                }
                                i1 = 0;
                                this.mc.getTextureManager().bindTexture(SOUL_RAIN_TEXTURES);
                                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                            float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                            double d2 = k1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - xIn;
                            double d4 = j1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - zIn;
                            float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / l;
                            float f5 = (float)(((1.0F - f4 * f4) * LavaRainClientConfig.GENERAL.rainOpacity.get() + LavaRainClientConfig.GENERAL.rainOpacity.get()) * f);
                            blockpos$mutable.setPos(k1, l2, j1);
                            int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        }
                    }
                }
            }

            if (i1 >= 0) {
                tessellator.draw();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableAlphaTest();
            lightmapIn.disableLightmap();
        }
    }

    private void renderAshRain(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn)
    {
        float f = this.mc.world.getRainStrength(partialTicks);
        if (!(f <= 0.0F)) {
            lightmapIn.enableLightmap();
            World world = this.mc.world;
            int i = MathHelper.floor(xIn);
            int j = MathHelper.floor(yIn);
            int k = MathHelper.floor(zIn);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (this.isFancyRainRender()) {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            int i1 = -1;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for(int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = this.rainSizeX[l1] * 0.5D;
                    double d1 = this.rainSizeZ[l1] * 0.5D;
                    blockpos$mutable.setPos(k1, 0, j1);
                    Optional<RegistryKey<Biome>> optional = this.mc.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(this.mc.world.getBiome(blockpos$mutable));

                    if (optional.get() == Biomes.BASALT_DELTAS) {
                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            Random random = new Random(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761);

                            if (i1 != 0) {
                                if (i1 >= 0) {
                                    tessellator.draw();
                                }
                                i1 = 0;
                                this.mc.getTextureManager().bindTexture(ASH_RAIN_TEXTURES);
                                bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                            }

                            int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                            float f3 = -(i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                            double d2 = k1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - xIn;
                            double d4 = j1 + LavaRainClientConfig.GENERAL.rainOpacity.get() - zIn;
                            float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / l;
                            float f5 = (float)(((1.0F - f4 * f4) * LavaRainClientConfig.GENERAL.rainOpacity.get() + LavaRainClientConfig.GENERAL.rainOpacity.get()) * f);
                            blockpos$mutable.setPos(k1, l2, j1);
                            int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, k2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, k2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn + d0 + 0.5D, j2 - yIn, j1 - zIn + d1 + 0.5D).tex(1.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                            bufferbuilder.pos(k1 - xIn - d0 + 0.5D, j2 - yIn, j1 - zIn - d1 + 0.5D).tex(0.0F, k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                        }
                    }
                }
            }

            if (i1 >= 0) {
                tessellator.draw();
            }

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.disableAlphaTest();
            lightmapIn.disableLightmap();
        }
    }

    private boolean isFancyRainRender()
    {
        return Minecraft.isFancyGraphicsEnabled() && !LavaRainClientConfig.GENERAL.fastRain.get();
    }
}