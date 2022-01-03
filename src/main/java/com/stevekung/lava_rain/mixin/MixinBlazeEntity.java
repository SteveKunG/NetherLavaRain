package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.MonsterEntity;

@Mixin(BlazeEntity.class)
public abstract class MixinBlazeEntity extends MonsterEntity
{
    private MixinBlazeEntity()
    {
        super(null, null);
    }

    @Override
    @Overwrite
    public boolean isWaterSensitive()
    {
        return !NetherUtils.isNetherBiomes(this.world.func_241828_r(), this.world.getBiome(this.getPosition()));
    }
}