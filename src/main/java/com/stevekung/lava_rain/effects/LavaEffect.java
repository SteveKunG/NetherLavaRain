package com.stevekung.lava_rain.effects;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.world.server.ServerWorld;

public class LavaEffect extends Effect
{
    public LavaEffect()
    {
        super(EffectType.HARMFUL, 14981690);
    }

    @Override
    public void performEffect(LivingEntity entityLivingBaseIn, int amplifier)
    {
        entityLivingBaseIn.setFire(10);

        if (!entityLivingBaseIn.world.isRemote)
        {
            for (int i = 0; i < entityLivingBaseIn.getRNG().nextInt(1) + 1; ++i)
            {
                ((ServerWorld)entityLivingBaseIn.world).spawnParticle(ParticleTypes.LAVA, entityLivingBaseIn.getPosX(), entityLivingBaseIn.getPosYHeight(0.6666666666666666D), entityLivingBaseIn.getPosZ(), 1, (double)(entityLivingBaseIn.getWidth() / 4.0F), (double)(entityLivingBaseIn.getHeight() / 4.0F), (double)(entityLivingBaseIn.getWidth() / 4.0F), 0.01D);
            }
        }
    }

    @Override
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier, double health)
    {
        this.performEffect(entityLivingBaseIn, amplifier);
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        return true;
    }
}