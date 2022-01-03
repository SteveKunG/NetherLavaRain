package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.world.IWorldReader;

@Mixin(LavaFluid.class)
public abstract class MixinLavaFluid extends FlowingFluid
{
    @Override
    @Overwrite
    public int getSlopeFindDistance(IWorldReader worldIn) {
        return 4;
    }

    @Override
    @Overwrite
    public int getLevelDecreasePerBlock(IWorldReader worldIn) {
        return 1;
    }

    @Override
    @Overwrite
    public int getTickRate(IWorldReader p_205569_1_) {
        return 10;
    }
}