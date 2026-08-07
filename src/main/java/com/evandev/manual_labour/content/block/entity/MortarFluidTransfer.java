package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.compat.create.CreateCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class MortarFluidTransfer {

    private MortarFluidTransfer() {
    }

    public static boolean tryEmptyItemIntoTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                               ItemStack heldItem, IFluidHandler tank) {
        return CreateCompat.get().tryEmptyItemIntoTank(level, pos, player, hand, heldItem, tank);
    }

    public static boolean tryFillItemFromTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                              ItemStack heldItem, IFluidHandler tank) {
        return CreateCompat.get().tryFillItemFromTank(level, pos, player, hand, heldItem, tank);
    }
}
