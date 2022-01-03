package com.stevekung.lava_rain.effects;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.stevekungslib.utils.AbstractRegistryInitializer;

import net.minecraft.potion.Effect;

public class LREffects extends AbstractRegistryInitializer
{
    public static final Effect LAVA = LavaRainMod.COMMON.registerEffect("lava", new LavaEffect());
}