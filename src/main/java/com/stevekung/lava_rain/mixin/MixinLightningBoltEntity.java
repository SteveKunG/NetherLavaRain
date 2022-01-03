package com.stevekung.lava_rain.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;

@Mixin(LightningBoltEntity.class)
public abstract class MixinLightningBoltEntity extends Entity
{
    private final LightningBoltEntity that = (LightningBoltEntity) (Object) this;

    @Shadow
    private int lightningState;

    @Shadow
    private int boltLivingTime;

    @Shadow
    private boolean effectOnly;

    @Shadow
    private ServerPlayerEntity caster;

    @Shadow
    private void igniteBlocks(int extraIgnitions) {}

    private MixinLightningBoltEntity()
    {
        super(null, null);
    }

    @Override
    @Overwrite
    public void tick() {
        super.tick();
        if (this.lightningState == 2) {
            Difficulty difficulty = this.world.getDifficulty();
            if (difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD) {
                this.igniteBlocks(4);
            }
            else if (difficulty == LavaRainMod.NETHER_RAIN) {
                this.igniteBlocks(8);
            }

            this.world.playSound((PlayerEntity)null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
            this.world.playSound((PlayerEntity)null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
        }

        --this.lightningState;
        if (this.lightningState < 0) {
            if (this.boltLivingTime == 0) {
                this.remove();
            } else if (this.lightningState < -this.rand.nextInt(10)) {
                --this.boltLivingTime;
                this.lightningState = 1;
                this.that.boltVertex = this.rand.nextLong();
                this.igniteBlocks(0);
            }
        }

        if (this.lightningState >= 0) {
            if (!(this.world instanceof ServerWorld)) {
                this.world.setTimeLightningFlash(2);
            } else if (!this.effectOnly) {
                List<Entity> list = this.world.getEntitiesInAABBexcluding(this, new AxisAlignedBB(this.getPosX() - 3.0D, this.getPosY() - 3.0D, this.getPosZ() - 3.0D, this.getPosX() + 3.0D, this.getPosY() + 6.0D + 3.0D, this.getPosZ() + 3.0D), Entity::isAlive);

                for(Entity entity : list) {
                    if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this.that))
                    {
                        entity.func_241841_a((ServerWorld)this.world, this.that);
                    }
                }

                if (this.caster != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.caster, list);
                }
            }
        }

    }
}