package com.evandev.manual_labour.mixin.nomansland;

import com.evandev.manual_labour.compat.nomansland.TapFluidFilling;
import com.farcr.nomansland.common.blockentity.TapBlockEntity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("nomansland")
@Mixin(TapBlockEntity.class)
public abstract class TapBlockEntityMixin {
    @WrapMethod(method = "raiseCauldron")
    private static boolean manual_labour$fillReceiver(Level level, BlockPos pos, BlockState state, Block cauldron, Operation<Boolean> original) {
        if (state.getBlock() instanceof AbstractCauldronBlock) return original.call(level, pos, state, cauldron);
        return TapFluidFilling.fillReceiver(level, pos, cauldron);
    }
}
