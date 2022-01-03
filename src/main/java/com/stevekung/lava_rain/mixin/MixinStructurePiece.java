package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.world.gen.feature.structure.StructurePiece;

@Mixin(StructurePiece.class)
public abstract class MixinStructurePiece
{
    @ModifyVariable(method = "setBlockState(Lnet/minecraft/world/ISeedReader;Lnet/minecraft/block/BlockState;IIILnet/minecraft/util/math/MutableBoundingBox;)V", at = @At("HEAD"), argsOnly = true)
    private BlockState setBlockState(BlockState defBlockstateIn)
    {
        if (defBlockstateIn.getBlock() == Blocks.COBBLESTONE || defBlockstateIn.getBlock() == Blocks.STONE_BRICKS)
        {
            return Blocks.NETHER_BRICKS.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.SMOOTH_STONE_SLAB)
        {
            return Blocks.NETHER_BRICK_SLAB.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.STONE_BRICK_SLAB)
        {
            return Blocks.RED_NETHER_BRICK_SLAB.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.IRON_BARS || defBlockstateIn.getBlock() == Blocks.OAK_FENCE)
        {
            return Blocks.NETHER_BRICK_FENCE.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.OAK_PLANKS || defBlockstateIn.getBlock() == Blocks.CRACKED_STONE_BRICKS || defBlockstateIn.getBlock() == Blocks.MOSSY_STONE_BRICKS || defBlockstateIn.getBlock() == Blocks.INFESTED_STONE_BRICKS)
        {
            return Blocks.RED_NETHER_BRICKS.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.COBBLESTONE_STAIRS)
        {
            return Blocks.NETHER_BRICK_STAIRS.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.STONE_BRICK_STAIRS)
        {
            return Blocks.RED_NETHER_BRICK_STAIRS.getDefaultState();
        }
        else if (defBlockstateIn.getBlock() == Blocks.WATER)
        {
            return Blocks.LAVA.getDefaultState();
        }
        else if (defBlockstateIn == Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER))
        {
            return Blocks.CRIMSON_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        }
        else if (defBlockstateIn == Blocks.OAK_DOOR.getDefaultState())
        {
            return Blocks.CRIMSON_DOOR.getDefaultState();
        }
        return defBlockstateIn;
    }
}