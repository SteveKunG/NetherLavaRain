package com.stevekung.lava_rain.config;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class LavaRainClientConfig
{
    public static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();
    public static final LavaRainClientConfig.General GENERAL = new LavaRainClientConfig.General(LavaRainClientConfig.GENERAL_BUILDER);

    public static class General
    {
        public final ForgeConfigSpec.ConfigValue<Double> rainOpacity;
        public final ForgeConfigSpec.BooleanValue fastRain;

        private General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General settings")
            .push("general");

            this.rainOpacity = builder
                    .translation("Rain Opacity")
                    .define("rainOpacity", 0.5D);
            this.fastRain = builder
                    .translation("Fast Rain render")
                    .define("fastRain", false);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(ModConfig.Loading event)
    {
        LavaRainMod.LOGGER.info("Loaded config file {}", event.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event)
    {
        LavaRainMod.LOGGER.info("BestKunG config just got changed on the file system");
    }
}