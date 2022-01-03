package com.stevekung.lava_rain.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stevekung.lava_rain.entity.LREntities;
import com.stevekung.lava_rain.entity.NetherLightningBoltEntity;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SSpawnObjectPacket;

@Mixin(ClientPlayNetHandler.class)
public abstract class MixinClientPlayNetHandler
{
    @Shadow
    private ClientWorld world;

    @Inject(method = "handleSpawnObject(Lnet/minecraft/network/play/server/SSpawnObjectPacket;)V", at = @At("RETURN"))
    private void handleLightningBoltSpawn(SSpawnObjectPacket packet, CallbackInfo info)
    {
        if (packet.getType() == LREntities.NETHER_LIGHTNING_BOLT)
        {
            double d0 = packet.getX();
            double d1 = packet.getY();
            double d2 = packet.getZ();
            Entity entity = new NetherLightningBoltEntity(LREntities.NETHER_LIGHTNING_BOLT, this.world);

            if (entity != null)
            {
                int i = packet.getEntityID();
                entity.setPacketCoordinates(d0, d1, d2);
                entity.moveForced(d0, d1, d2);
                entity.rotationPitch = packet.getPitch() * 360 / 256.0F;
                entity.rotationYaw = packet.getYaw() * 360 / 256.0F;
                entity.setEntityId(i);
                entity.setUniqueId(packet.getUniqueId());
                this.world.addEntity(i, entity);
            }
        }
    }
}