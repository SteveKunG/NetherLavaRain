package com.stevekung.lava_rain.entity;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.stevekungslib.utils.AbstractRegistryInitializer;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

public class LREntities extends AbstractRegistryInitializer
{
    public static final EntityType<NetherLightningBoltEntity> NETHER_LIGHTNING_BOLT = LavaRainMod.COMMON.registerEntityType("nether_lightning_bolt", EntityType.Builder.create(NetherLightningBoltEntity::new, EntityClassification.MISC).disableSerialization().size(0.0F, 0.0F).trackingRange(16).func_233608_b_(Integer.MAX_VALUE));
}