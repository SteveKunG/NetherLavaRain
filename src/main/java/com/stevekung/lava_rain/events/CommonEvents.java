package com.stevekung.lava_rain.events;

import java.util.Optional;
import java.util.stream.Collectors;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.lava_rain.config.LavaRainServerConfig;
import com.stevekung.lava_rain.effects.LREffects;
import com.stevekung.lava_rain.entity.LREntities;
import com.stevekung.lava_rain.entity.NetherLightningBoltEntity;
import com.stevekung.lava_rain.utils.NetherUtils;
import com.stevekung.stevekungslib.utils.event.WeatherTickEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.HoglinEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEvents
{
    private static final DamageSource LAVA_RAIN = new DamageSource("lava_rain").setFireDamage();

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        PlayerEntity player = event.getPlayer();
        Hand hand = event.getHand();
        BlockState state = event.getWorld().getBlockState(event.getPos());
        Item item = event.getItemStack().getItem();

        if (state.getBlock() == Blocks.CAULDRON)
        {
            boolean lava = state.get(LavaRainMod.LAVA);
            int i = state.get(CauldronBlock.LEVEL);

            if (item == Items.WATER_BUCKET && lava)
            {
                event.setCanceled(true);
            }
            else if (item == Items.LAVA_BUCKET && lava)
            {
                if (i < 3 && !world.isRemote)
                {
                    if (!player.abilities.isCreativeMode)
                    {
                        player.setHeldItem(hand, new ItemStack(Items.BUCKET));
                    }
                    player.addStat(Stats.FILL_CAULDRON);
                    ((CauldronBlock)state.getBlock()).setWaterLevel(world, pos, state.with(LavaRainMod.LAVA, true), 3);
                    world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                event.setCancellationResult(ActionResultType.func_233537_a_(world.isRemote));
                event.setCanceled(true);
            }
            else if (item == Items.BUCKET)
            {
                if (i == 3 && !world.isRemote)
                {
                    if (!player.abilities.isCreativeMode)
                    {
                        event.getItemStack().shrink(1);

                        if (event.getItemStack().isEmpty())
                        {
                            player.setHeldItem(hand, new ItemStack(lava ? Items.LAVA_BUCKET : Items.WATER_BUCKET));
                        }
                        else if (!player.inventory.addItemStackToInventory(new ItemStack(lava ? Items.LAVA_BUCKET : Items.WATER_BUCKET)))
                        {
                            player.dropItem(new ItemStack(lava ? Items.LAVA_BUCKET : Items.WATER_BUCKET), false);
                        }
                    }
                    player.addStat(Stats.USE_CAULDRON);
                    ((CauldronBlock)state.getBlock()).setWaterLevel(world, pos, state, 0);
                    world.playSound(null, pos, lava ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                event.setCancellationResult(ActionResultType.func_233537_a_(world.isRemote));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onWeatherTick(WeatherTickEvent event)
    {
        if (!LavaRainServerConfig.GENERAL.netherThunder.get())
        {
            return;
        }

        World world = event.getWorld();
        BlockPos blockpos = event.getStrikePos();

        if (NetherUtils.isNetherBiomes(world.func_241828_r(), world.getBiome(blockpos)) && world.isRaining() && world.rand.nextInt(1000) == 0)
        {
            if (world.isRainingAt(blockpos))
            {
                NetherLightningBoltEntity lightningboltentity = LREntities.NETHER_LIGHTNING_BOLT.create(world);
                lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
                lightningboltentity.setEffectOnly(false);
                world.addEntity(lightningboltentity);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUseItemFinish(LivingEntityUseItemEvent.Finish event)
    {
        if (event.getItem().isItemEqual(Items.POTION.getDefaultInstance()))
        {
            event.getEntityLiving().extinguish();

            if (event.getEntityLiving().isBurning())
            {
                event.getEntityLiving().playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (event.getEntityLiving().getRNG().nextFloat() - event.getEntityLiving().getRNG().nextFloat()) * 0.4F);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event)
    {
        LivingEntity living = event.getEntityLiving();

        if (living.world.getDifficulty() != LavaRainMod.NETHER_RAIN)
        {
            return;
        }

        if (!living.world.isRemote())
        {
            boolean disable = false;
            Optional<RegistryKey<Biome>> optional = living.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(living.world.getBiome(living.getPosition()));
            ItemStack head = living.getItemStackFromSlot(EquipmentSlotType.HEAD);

            if (!disable && (living instanceof PlayerEntity && ((PlayerEntity)living).isCreative() || living.isSpectator()))
            {
                return;
            }

            if (living.world.isRaining() && !(living instanceof EndermanEntity) || optional.get() == Biomes.THE_END)
            {
                if (living.world.isRainingAt(living.getPosition().up()))
                {
                    if (!disable && NetherUtils.isNetherBiomes(living.world.func_241828_r(), living.world.getBiome(living.getPosition())) && !(living.isImmuneToFire() || living instanceof PiglinEntity || living instanceof HoglinEntity || living instanceof SkeletonEntity))
                    {
                        if (head.isEmpty())
                        {
                            if (living.getRNG().nextInt(75) == 0 && optional.get() != Biomes.THE_END)
                            {
                                living.addPotionEffect(new EffectInstance(LREffects.LAVA, 20));
                            }
                            else if (living.getRNG().nextInt(5) == 0)
                            {
                                living.attackEntityFrom(LAVA_RAIN, 0.25F);
                            }
                        }
                        else
                        {
                            if (living.getRNG().nextInt(250) == 0)
                            {
                                head.damageItem(1, living, living1 -> living1.sendBreakAnimation(EquipmentSlotType.HEAD));
                            }
                        }
                    }

                    if (optional.get() == Biomes.WARPED_FOREST)
                    {
                        if (living.getRNG().nextInt(100) == 0)
                        {
                            for (int i = 0; i < 16; ++i)
                            {
                                double d3 = living.getPosX() + (living.getRNG().nextDouble() - 0.5D) * 16.0D;
                                double d4 = MathHelper.clamp(living.getPosY() + (living.getRNG().nextInt(16) - 8), 0.0D, living.world.func_234938_ad_() - 1);
                                double d5 = living.getPosZ() + (living.getRNG().nextDouble() - 0.5D) * 16.0D;

                                if (living.isPassenger())
                                {
                                    living.stopRiding();
                                }

                                if (living.attemptTeleport(d3, d4, d5, true))
                                {
                                    living.world.playSound(null, living.prevPosX, living.prevPosY, living.prevPosZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                                    living.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                                    break;
                                }
                            }
                        }
                    }
                    else if (optional.get() == Biomes.SOUL_SAND_VALLEY)
                    {
                        if (living.getRNG().nextInt(100) == 0)
                        {
                            living.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 100));
                        }
                    }
                }
                if (optional.get() == Biomes.SOUL_SAND_VALLEY)
                {
                    if (living.getRNG().nextInt(50) == 0)
                    {
                        living.world.playSound(null, living.getPosition(), living.getRNG().nextBoolean() ? SoundEvents.ENTITY_GHAST_AMBIENT : SoundEvents.ENTITY_WITCH_AMBIENT, SoundCategory.PLAYERS, 0.2F, 0.2F + (living.getRNG().nextFloat() - living.getRNG().nextFloat()) * 0.8F);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemEntityUpdate(WorldTickEvent event)
    {
        if (event.side.isServer())
        {
            ServerWorld world = (ServerWorld)event.world;

            if (world.getDifficulty() == LavaRainMod.NETHER_RAIN)
            {
                world.func_241113_a_(0, 50, true, false);
            }
            else
            {
                return;
            }

            for (ItemEntity itemEntity : world.getEntities().filter(entity -> entity instanceof ItemEntity && entity.isAlive()).map(entity -> (ItemEntity)entity).collect(Collectors.toList()))
            {
                IRecipe<?> irecipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(itemEntity.getItem()), world).orElse(null);

                if (NetherUtils.isNetherBiomes(world.func_241828_r(), world.getBiome(itemEntity.getPosition())) && world.isRaining() && world.isRainingAt(itemEntity.getPosition().up()) && irecipe != null)
                {
                    ItemStack output = irecipe.getRecipeOutput().copy();

                    if (itemEntity.ticksExisted >= 20 && world.rand.nextInt(50) == 0 && !output.isEmpty())
                    {
                        for (int i = 0; i < 20; ++i)
                        {
                            world.spawnParticle(ParticleTypes.SMOKE, itemEntity.getPosX(), itemEntity.getPosYHeight(0.6666666666666666D), itemEntity.getPosZ(), 50, (double)(itemEntity.getWidth() / 4.0F), (double)(itemEntity.getHeight() / 4.0F), (double)(itemEntity.getWidth() / 4.0F), 0.01D);
                        }
                        ItemStack itemStackE = itemEntity.getItem();
                        itemStackE.shrink(1);
                        itemEntity.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.4F);
                        Block.spawnAsEntity(world, itemEntity.getPosition(), output);
                        itemEntity.setItem(new ItemStack(itemStackE.getItem(), itemStackE.getCount()));
                    }
                }
            }
        }
    }
}