package com.stevekung.lava_rain.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem
{
    @Redirect(method = "tryPlaceContainedLiquid(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockRayTraceResult;)Z", at = @At(value = "INVOKE", target = "net/minecraft/world/DimensionType.isUltrawarm()Z"))
    private boolean addWarm(DimensionType type, @Nullable PlayerEntity player, World worldIn, BlockPos posIn, @Nullable BlockRayTraceResult rayTrace)
    {
        return type.isUltrawarm() || NetherUtils.isNetherBiomes(worldIn.func_241828_r(), worldIn.getBiome(posIn));
    }
}