package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.passive.AnimalEntity;

@Mixin(HoglinEntity.class)
public abstract class MixinHoglinEntity extends AnimalEntity
{
    private MixinHoglinEntity()
    {
        super(null, null);
    }

    @Inject(method = "func_234364_eK_()Z", at = @At("HEAD"), cancellable = true)
    private void func_234364_eK_(CallbackInfoReturnable info)
    {
        if (NetherUtils.isNetherBiomes(this.world.func_241828_r(), this.world.getBiome(this.getPosition())))
        {
            info.setReturnValue(false);
        }
    }
}