package com.evandev.manual_labour.mixin.nomansland;

import com.evandev.manual_labour.compat.nomansland.TapFluidFilling;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.farcr.nomansland.common.blockentity.TapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("nomansland")
@Mixin(TapBlockEntity.class)
public abstract class TapBlockEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private static void manual_labour$drainIntoReceiver(Level level, BlockPos pos, BlockState state, TapBlockEntity tap, CallbackInfo ci) {
        TapFluidFilling.tick(level, pos, state, tap);
    }
}
