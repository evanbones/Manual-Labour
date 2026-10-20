package com.evandev.manual_labour.mixin.nomansland;

import com.evandev.manual_labour.compat.nomansland.TapFluidFilling;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.farcr.nomansland.common.block.tap.TapBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("nomansland")
@Mixin(TapBlock.class)
public abstract class TapBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void manual_labour$fillReceiver(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        TapFluidFilling.randomTick(state, level, pos, random);
    }
}
