package com.stevekung.lava_rain.utils;

import java.util.Optional;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class NetherUtils
{
    public static boolean isNetherBiomes(DynamicRegistries reg, Biome biome)
    {
        Optional<RegistryKey<Biome>> optional = reg.getRegistry(Registry.BIOME_KEY).getOptionalKey(biome);
        return optional.get() == Biomes.THE_END || optional.get() == Biomes.CRIMSON_FOREST || optional.get() == Biomes.WARPED_FOREST || optional.get() == Biomes.NETHER_WASTES || optional.get() == Biomes.SOUL_SAND_VALLEY || optional.get() == Biomes.BASALT_DELTAS;
    }
}