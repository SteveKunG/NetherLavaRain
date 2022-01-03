package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;

@Mixin(Difficulty.class)
public abstract class MixinDifficulty
{
    @Shadow
    private String translationKey;

    @Shadow
    private int id;

    @Overwrite
    public static Difficulty byId(int id)
    {
        return Difficulty.values()[id % Difficulty.values().length];
    }

    @Overwrite
    public ITextComponent getDisplayName()
    {
        if (this.id < 4)
        {
            return new TranslationTextComponent("options.difficulty." + this.translationKey);
        }
        else
        {
            IFormattableTextComponent component = new TranslationTextComponent("options.difficulty." + this.translationKey);

            if (this.id == LavaRainMod.NETHER_RAIN.getId())
            {
                IFormattableTextComponent com = component.deepCopy();
                com.setStyle(com.getStyle().setColor(Color.fromHex("#FF4500")));
                component = com;
            }
            return component;
        }
    }

    @Overwrite
    public Difficulty getNextDifficulty()
    {
        return Difficulty.values()[(this.id + 1) % Difficulty.values().length];
    }
}