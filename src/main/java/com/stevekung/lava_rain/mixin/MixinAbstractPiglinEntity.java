package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;

@Mixin(AbstractPiglinEntity.class)
public abstract class MixinAbstractPiglinEntity extends MonsterEntity
{
    private MixinAbstractPiglinEntity()
    {
        super(null, null);
    }

    @Inject(method = "func_242336_eL()Z", at = @At("HEAD"), cancellable = true)
    private void func_242336_eL(CallbackInfoReturnable info)
    {
        if (NetherUtils.isNetherBiomes(this.world.func_241828_r(), this.world.getBiome(this.getPosition())))
        {
            info.setReturnValue(false);
        }
    }
}