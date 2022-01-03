package com.stevekung.lava_rain.mixin.client;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.util.math.BlockPos;

@Mixin(DiggingParticle.class)
public abstract class MixinDiggingParticle extends SpriteTexturedParticle
{
    private final DiggingParticle that = (DiggingParticle) (Object) this;

    @Shadow
    private BlockState sourceState;

    @Shadow
    private BlockPos sourcePos;

    @Shadow
    private Particle updateSprite(BlockPos pos)
    {
        return null;
    }

    @Shadow
    protected abstract void multiplyColor(@Nullable BlockPos p_187154_1_);

    private MixinDiggingParticle()
    {
        super(null, 0, 0, 0, 0, 0, 0);
    }

    @Overwrite
    public DiggingParticle setBlockPos(BlockPos pos) {
        this.updateSprite(pos);
        this.sourcePos = pos;
        if (this.sourceState.isIn(Blocks.GRASS_BLOCK) || this.sourceState.isIn(Blocks.CAULDRON)) {
            return this.that;
        } else {
            this.multiplyColor(pos);
            return this.that;
        }
    }

    @Overwrite
    public DiggingParticle init() {
        this.sourcePos = new BlockPos(this.posX, this.posY, this.posZ);
        if (this.sourceState.isIn(Blocks.GRASS_BLOCK) || this.sourceState.isIn(Blocks.CAULDRON)) {
            return this.that;
        } else {
            this.multiplyColor(this.sourcePos);
            return this.that;
        }
    }
}