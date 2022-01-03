package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stevekung.lava_rain.LavaRainMod;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(Item.class)
public abstract class MixinItem
{
    private final Item that = (Item) (Object) this;

    @Shadow
    private Food food;

    @Overwrite
    public boolean isFood()
    {
        return this.isWart() ? true : this.food != null;
    }

    @Overwrite
    public Food getFood()
    {
        if (this.isWart())
        {
            return LavaRainMod.WART_BLOCK;
        }
        return this.food;
    }

    @Inject(method = "onItemUseFinish(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving, CallbackInfoReturnable<ItemStack> info) {
        if (!worldIn.isRemote && this.that == Blocks.WARPED_WART_BLOCK.asItem()) {
            double d0 = entityLiving.getPosX();
            double d1 = entityLiving.getPosY();
            double d2 = entityLiving.getPosZ();

            for(int i = 0; i < 16; ++i) {
                double d3 = entityLiving.getPosX() + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                double d4 = MathHelper.clamp(entityLiving.getPosY() + (entityLiving.getRNG().nextInt(16) - 8), 0.0D, worldIn.func_234938_ad_() - 1);
                double d5 = entityLiving.getPosZ() + (entityLiving.getRNG().nextDouble() - 0.5D) * 16.0D;
                if (entityLiving.isPassenger()) {
                    entityLiving.stopRiding();
                }

                if (entityLiving.attemptTeleport(d3, d4, d5, true)) {
                    SoundEvent soundevent = entityLiving instanceof FoxEntity ? SoundEvents.ENTITY_FOX_TELEPORT : SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT;
                    worldIn.playSound((PlayerEntity)null, d0, d1, d2, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    entityLiving.playSound(soundevent, 1.0F, 1.0F);
                    break;
                }
            }
        }
        info.setReturnValue(entityLiving.onFoodEaten(worldIn, stack));
    }

    private boolean isWart()
    {
        return this.that == Blocks.NETHER_WART_BLOCK.asItem() || this.that == Blocks.WARPED_WART_BLOCK.asItem();
    }
}