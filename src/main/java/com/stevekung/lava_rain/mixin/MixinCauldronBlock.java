package com.stevekung.lava_rain.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.stevekung.lava_rain.LavaRainMod;
import com.stevekung.lava_rain.utils.NetherUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CauldronBlock.class)
public abstract class MixinCauldronBlock extends Block
{
    private final CauldronBlock that = (CauldronBlock) (Object) this;

    private MixinCauldronBlock()
    {
        super(null);
    }

    @Redirect(method = "<init>(Lnet/minecraft/block/AbstractBlock$Properties;)V", at = @At(value = "INVOKE", target = "net/minecraft/block/CauldronBlock.setDefaultState(Lnet/minecraft/block/BlockState;)V"))
    private void addLavaState(CauldronBlock block, BlockState state)
    {
        this.setDefaultState(this.stateContainer.getBaseState().with(CauldronBlock.LEVEL, 0).with(LavaRainMod.LAVA, false));
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        int i = state.get(CauldronBlock.LEVEL);
        float f = pos.getY() + (6.0F + 3 * i) / 16.0F;

        if (!worldIn.isRemote && i > 0 && entityIn.getPosY() <= f)
        {
            if (!state.get(LavaRainMod.LAVA))
            {
                if (entityIn.isBurning())
                {
                    entityIn.extinguish();
                    this.that.setWaterLevel(worldIn, pos, state, i - 1);
                }
            }
            else
            {
                if (!entityIn.isImmuneToFire() && entityIn instanceof PlayerEntity && !((PlayerEntity)entityIn).isCreative())
                {
                    entityIn.setFire(15);
                    entityIn.attackEntityFrom(DamageSource.LAVA, 4.0F);

                    if (worldIn.rand.nextInt(50) == 0)
                    {
                        this.that.setWaterLevel(worldIn, pos, state, i - 1);
                    }
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        BlockState blockstate = this.getDefaultState();
        return blockstate.with(LavaRainMod.LAVA, NetherUtils.isNetherBiomes(context.getWorld().func_241828_r(), context.getWorld().getBiome(context.getPos())));
    }

    @Override
    public void fillWithRain(World worldIn, BlockPos pos)
    {
        BlockState blockstate = worldIn.getBlockState(pos);

        if (worldIn.rand.nextInt(NetherUtils.isNetherBiomes(worldIn.func_241828_r(), worldIn.getBiome(pos)) ? 3 : 20) == 1)
        {
            float f = worldIn.getBiome(pos).getTemperature(pos);

            if (!(f < 0.15F))
            {
                if (blockstate.get(CauldronBlock.LEVEL) < 3)
                {
                    worldIn.setBlockState(pos, blockstate.func_235896_a_(CauldronBlock.LEVEL), 2);
                }
            }
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(CauldronBlock.LEVEL, LavaRainMod.LAVA);
    }
}