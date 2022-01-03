package com.stevekung.lava_rain.proxy;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.lava_rain.client.NetherLightningBoltRenderer;
import com.stevekung.lava_rain.entity.LREntities;
import com.stevekung.lava_rain.events.ClientEvents;
import com.stevekung.stevekungslib.utils.CommonUtils;

import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.world.biome.BiomeColors;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public void commonSetup(FMLCommonSetupEvent event)
    {
        super.commonSetup(event);
        CommonUtils.registerEventHandler(new ClientEvents());
    }

    @Override
    public void clientRegistries(FMLClientSetupEvent event)
    {
        RenderTypeLookup.setRenderLayer(Blocks.CAULDRON, RenderType.getTranslucent());
        RenderingRegistry.registerEntityRenderingHandler(LREntities.NETHER_LIGHTNING_BOLT, NetherLightningBoltRenderer::new);
    }

    @SubscribeEvent
    public void onRegisterBlockColor(ColorHandlerEvent.Block event)
    {
        event.getBlockColors().register((state, reader, pos, tintIndex) -> reader != null && pos != null && !state.get(LavaRainMod.LAVA) ? BiomeColors.getWaterColor(reader, pos) : -1, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
    }
}