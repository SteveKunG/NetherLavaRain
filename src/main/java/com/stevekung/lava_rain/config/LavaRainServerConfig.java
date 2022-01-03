package com.stevekung.lava_rain.config;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class LavaRainServerConfig
{
    public static final ForgeConfigSpec.Builder GENERAL_BUILDER = new ForgeConfigSpec.Builder();
    public static final LavaRainServerConfig.General GENERAL = new LavaRainServerConfig.General(LavaRainServerConfig.GENERAL_BUILDER);

    public static class General
    {
        public final ForgeConfigSpec.BooleanValue netherThunder;

        private General(ForgeConfigSpec.Builder builder)
        {
            builder.comment("General settings")
            .push("general");

            this.netherThunder = builder
                    .translation("Nether Thunder")
                    .define("netherThunder", false);
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