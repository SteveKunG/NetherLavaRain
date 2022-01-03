package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;

@Mixin(DifficultyInstance.class)
public abstract class MixinDifficultyInstance
{
    @Overwrite
    private float calculateAdditionalDifficulty(Difficulty difficulty, long worldTime, long chunkInhabitedTime, float moonPhaseFactor)
    {
        if (difficulty == Difficulty.PEACEFUL)
        {
            return 0.0F;
        }
        else
        {
            boolean flag = difficulty == Difficulty.HARD || difficulty == LavaRainMod.NETHER_RAIN;
            float f = 0.75F;
            float f1 = MathHelper.clamp((worldTime + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
            f = f + f1;
            float f2 = 0.0F;
            f2 = f2 + MathHelper.clamp(chunkInhabitedTime / 3600000.0F, 0.0F, 1.0F) * (flag ? 1.0F : 0.75F);
            f2 = f2 + MathHelper.clamp(moonPhaseFactor * 0.25F, 0.0F, f1);

            if (difficulty == Difficulty.EASY)
            {
                f2 *= 0.5F;
            }

            f = f + f2;
            return difficulty.getId() * f;
        }
    }
}