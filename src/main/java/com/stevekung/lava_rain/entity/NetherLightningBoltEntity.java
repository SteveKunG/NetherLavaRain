package com.stevekung.lava_rain.entity;

import java.util.List;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class NetherLightningBoltEntity extends LightningBoltEntity
{
    private int lightningState;
    public long boltVertex;
    private int boltLivingTime;
    private boolean effectOnly;

    public NetherLightningBoltEntity(EntityType<? extends NetherLightningBoltEntity> p_i231491_1_, World world)
    {
        super(p_i231491_1_, world);
        this.ignoreFrustumCheck = true;
        this.lightningState = 2;
        this.boltVertex = this.rand.nextLong();
        this.boltLivingTime = this.rand.nextInt(3) + 1;
    }

    @Override
    public void setEffectOnly(boolean effectOnly)
    {
        this.effectOnly = effectOnly;
    }

    @Override
    public void tick()
    {
        this.baseTick();

        if (this.lightningState == 2)
        {
            Difficulty difficulty = this.world.getDifficulty();

            if (difficulty == LavaRainMod.NETHER_RAIN)
            {
                this.igniteBlocks(12);
            }

            this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0F, 0.8F + this.rand.nextFloat() * 0.2F);
            this.world.playSound(null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
        }

        --this.lightningState;

        if (this.lightningState < 0)
        {
            if (this.boltLivingTime == 0)
            {
                this.remove();
            }
            else if (this.lightningState < -this.rand.nextInt(10))
            {
                --this.boltLivingTime;
                this.lightningState = 1;
                this.boltVertex = this.rand.nextLong();
                this.igniteBlocks(0);
            }
        }

        if (this.lightningState >= 0)
        {
            if (!(this.world instanceof ServerWorld))
            {
                this.world.setTimeLightningFlash(2);
            }
            else if (!this.effectOnly)
            {
                List<Entity> list = this.world.getEntitiesInAABBexcluding(this, new AxisAlignedBB(this.getPosX() - 3.0D, this.getPosY() - 3.0D, this.getPosZ() - 3.0D, this.getPosX() + 3.0D, this.getPosY() + 6.0D + 3.0D, this.getPosZ() + 3.0D), Entity::isAlive);

                for (Entity entity : list)
                {
                    if (!net.minecraftforge.event.ForgeEventFactory.onEntityStruckByLightning(entity, this))
                    {
                        entity.func_241841_a((ServerWorld)this.world, this);
                        this.attackEntityFrom(DamageSource.LIGHTNING_BOLT, 8.0F);
                    }
                }

                if (this.world.rand.nextInt(50) == 0)
                {
                    WitherSkeletonEntity wither = EntityType.WITHER_SKELETON.create(this.world);
                    wither.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), 0, 0);
                    wither.onInitialSpawn((IServerWorld)this.world, this.world.getDifficultyForLocation(this.getPosition()), SpawnReason.COMMAND, null, null);
                    this.world.addEntity(wither);
                }
            }
        }
    }

    private void igniteBlocks(int extraIgnitions)
    {
        if (!this.effectOnly && !this.world.isRemote && this.world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK))
        {
            BlockPos blockpos = this.getPosition();
            BlockState blockstate = AbstractFireBlock.getFireForPlacement(this.world, blockpos);

            if (this.world.getBlockState(blockpos).getBlock().isAir(blockstate, this.world, blockpos) && blockstate.isValidPosition(this.world, blockpos))
            {
                this.world.setBlockState(blockpos, blockstate);
            }

            for (int i = 0; i < extraIgnitions; ++i)
            {
                BlockPos blockpos1 = blockpos.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);
                blockstate = AbstractFireBlock.getFireForPlacement(this.world, blockpos1);

                if (this.world.getBlockState(blockpos1).getBlock().isAir(blockstate, this.world, blockpos) && blockstate.isValidPosition(this.world, blockpos1))
                {
                    this.world.setBlockState(blockpos1, blockstate);
                }
            }
        }
    }
}