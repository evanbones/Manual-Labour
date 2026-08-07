package com.evandev.manual_labour.mixin;

import com.evandev.manual_labour.content.block.WorkstoneBlock;
import com.evandev.manual_labour.content.block.entity.dispenser.WorkstoneDispenseBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public abstract class WorkstoneDispenserMixin {
    @Inject(
            method = "dispenseFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/DispenserBlock;getDispenseMethod(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/core/dispenser/DispenseItemBehavior;"
            ),
            cancellable = true
    )
    public void onWorkstoneDispenseFromInject(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo ci) {
        BlockPos facingPos = pos.relative(state.getValue(DispenserBlock.FACING));
        BlockState facingState = level.getBlockState(facingPos);
        if (facingState.getBlock() instanceof WorkstoneBlock) {
            DispenserBlockEntity dispenser = (DispenserBlockEntity) level.getBlockEntity(pos);
            if (dispenser != null) {
                int slot = dispenser.getRandomSlot(level.random);
                if (slot >= 0) {
                    ItemStack stack = dispenser.getItem(slot);
                    if (!stack.isEmpty()) {
                        BlockSource source = new BlockSource(level, pos, state, dispenser);
                        if (WorkstoneDispenseBehavior.INSTANCE.tryDispenseStackOnWorkstone(source, stack, slot, dispenser)) {
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}
