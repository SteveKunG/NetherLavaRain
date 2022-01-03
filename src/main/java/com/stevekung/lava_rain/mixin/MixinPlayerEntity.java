package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity
{
    private final PlayerEntity that = (PlayerEntity) (Object) this;

    @Shadow
    protected abstract void spawnShoulderEntities();

    private MixinPlayerEntity()
    {
        super(null, null);
    }

    @Override
    @Overwrite
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttack(this.that, source, amount))
        {
            return false;
        }
        if (this.isInvulnerableTo(source))
        {
            return false;
        } else if (this.that.abilities.disableDamage && !source.canHarmInCreative())
        {
            return false;
        } else {
            this.idleTime = 0;
            if (this.getShouldBeDead()) {
                return false;
            } else {
                this.spawnShoulderEntities();
                if (source.isDifficultyScaled()) {
                    if (this.world.getDifficulty() == Difficulty.PEACEFUL) {
                        amount = 0.0F;
                    }

                    if (this.world.getDifficulty() == Difficulty.EASY) {
                        amount = Math.min(amount / 2.0F + 1.0F, amount);
                    }

                    if (this.world.getDifficulty() == Difficulty.HARD || this.world.getDifficulty() == LavaRainMod.NETHER_RAIN) {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                return amount == 0.0F ? false : super.attackEntityFrom(source, amount);
            }
        }
    }
}