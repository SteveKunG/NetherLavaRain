package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.feature.DungeonsFeature;

@Mixin(DungeonsFeature.class)
public abstract class MixinDungeonsFeature
{
    @Redirect(method = "generate", at = @At(value = "INVOKE", target = "net/minecraft/world/ISeedReader.setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 2))
    private boolean redirectMossy(ISeedReader p_241855_1_, BlockPos pos, BlockState newState, int flags)
    {
        return p_241855_1_.setBlockState(pos, Blocks.GILDED_BLACKSTONE.getDefaultState(), flags);
    }

    @Redirect(method = "generate", at = @At(value = "INVOKE", target = "net/minecraft/world/ISeedReader.setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 3))
    private boolean redirectCobble(ISeedReader p_241855_1_, BlockPos pos, BlockState newState, int flags)
    {
        return p_241855_1_.setBlockState(pos, Blocks.BLACKSTONE.getDefaultState(), flags);
    }
}