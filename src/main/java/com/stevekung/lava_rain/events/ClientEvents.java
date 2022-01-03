package com.stevekung.lava_rain.events;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents
{
    @SubscribeEvent
    public void onFogDensity(FogDensity event)
    {
        PlayerEntity player = (PlayerEntity) event.getInfo().getRenderViewEntity();
        Optional<RegistryKey<Biome>> optional = player.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(player.world.getBiome(player.getPosition()));

        if (player.isCreative() || player.isSpectator())
        {
            return;
        }

        float rainSt = player.world.getRainStrength((float)event.getRenderPartialTicks());

        if (rainSt > 0.0F && (optional.get() == Biomes.BASALT_DELTAS || optional.get() == Biomes.SOUL_SAND_VALLEY))
        {
            event.setDensity(Math.max(Math.min(rainSt, 0.06F), 0.01F));
            event.setCanceled(true);
        }
    }
}