package com.stevekung.lava_rain.proxy;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.lava_rain.effects.LREffects;
import com.stevekung.lava_rain.entity.LREntities;
import com.stevekung.lava_rain.events.CommonEvents;
import com.stevekung.stevekungslib.utils.CommonUtils;

import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CommonProxy
{
    public void init()
    {
        CommonUtils.registerModEventBus(this);
        CommonUtils.addModListener(this::commonSetup);
        CommonUtils.addModListener(this::clientRegistries);
    }

    public void commonSetup(FMLCommonSetupEvent event)
    {
        CommonUtils.registerEventHandler(new CommonEvents());
    }

    public void clientRegistries(FMLClientSetupEvent event)
    {

    }

    @SubscribeEvent
    public void registerEffects(RegistryEvent.Register<Effect> event)
    {
        new LREffects().init(event, LavaRainMod.LOGGER);
    }

    @SubscribeEvent
    public void registerEntity(RegistryEvent.Register<EntityType<?>> event)
    {
        new LREntities().init(event, LavaRainMod.LOGGER);
    }
}