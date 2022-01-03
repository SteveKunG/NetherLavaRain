package com.stevekung.lava_rain;

import com.stevekung.lava_rain.config.LavaRainClientConfig;
import com.stevekung.lava_rain.config.LavaRainServerConfig;
import com.stevekung.lava_rain.proxy.ClientProxy;
import com.stevekung.lava_rain.proxy.CommonProxy;
import com.stevekung.lava_rain.utils.EnumHelper;
import com.stevekung.stevekungslib.utils.CommonRegistryUtils;
import com.stevekung.stevekungslib.utils.CommonUtils;
import com.stevekung.stevekungslib.utils.LoggerBase;

import net.minecraft.item.Food;
import net.minecraft.state.BooleanProperty;
import net.minecraft.world.Difficulty;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(LavaRainMod.MOD_ID)
public class LavaRainMod
{
    public static final String MOD_ID = "lava_rain";
    public static final CommonRegistryUtils COMMON = new CommonRegistryUtils(MOD_ID);
    public static final LoggerBase LOGGER = new LoggerBase("LavaRain");
    public static final Difficulty NETHER_RAIN = EnumHelper.addDifficulty("NETHER_RAIN", 4, "nether_rain");
    public static final BooleanProperty LAVA = BooleanProperty.create("lava");
    public static final Food WART_BLOCK = new Food.Builder().hunger(4).saturation(0.5F).build();
    public static CommonProxy PROXY;

    public LavaRainMod()
    {
        CommonUtils.registerConfig(ModConfig.Type.CLIENT, LavaRainClientConfig.GENERAL_BUILDER);
        CommonUtils.registerConfig(ModConfig.Type.COMMON, LavaRainServerConfig.GENERAL_BUILDER);
        CommonUtils.registerModEventBus(LavaRainClientConfig.class);
        CommonUtils.registerModEventBus(LavaRainServerConfig.class);
        PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        PROXY.init();
        COMMON.registerAll();
    }
}