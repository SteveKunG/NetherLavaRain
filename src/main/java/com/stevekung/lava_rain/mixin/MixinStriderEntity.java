package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.StriderEntity;

@Mixin(StriderEntity.class)
public abstract class MixinStriderEntity extends AnimalEntity
{
    private MixinStriderEntity()
    {
        super(null, null);
    }

    @Override
    @Overwrite
    public boolean isWaterSensitive()
    {
        return !NetherUtils.isNetherBiomes(this.world.func_241828_r(), this.world.getBiome(this.getPosition()));
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "net/minecraft/entity/passive/StriderEntity.func_234319_t_(Z)V"))
    private void redirectWarm(StriderEntity entity, boolean warm)
    {
        entity.func_234319_t_(warm && !(this.world.isRaining() && this.world.isRainingAt(this.getPosition().up()) && NetherUtils.isNetherBiomes(this.world.func_241828_r(), this.world.getBiome(this.getPosition()))));
    }
}