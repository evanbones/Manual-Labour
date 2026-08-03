package com.evandev.manual_labour.content.block.entity;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public final class MortarFluidTransfer {

    private MortarFluidTransfer() {
    }

    public static boolean tryEmptyItemIntoTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                               ItemStack heldItem, IFluidHandler tank) {
        if (!GenericItemEmptying.canItemBeEmptied(level, heldItem)) return false;

        Pair<FluidStack, ItemStack> simulated = GenericItemEmptying.emptyItem(level, heldItem, true);
        FluidStack fluidStack = simulated.getFirst();

        if (fluidStack.isEmpty()) return false;
        if (fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE)) return false;
        if (level.isClientSide) return true;

        ItemStack copyOfHeld = heldItem.copy();
        Pair<FluidStack, ItemStack> result = GenericItemEmptying.emptyItem(level, copyOfHeld, false);
        tank.fill(fluidStack, FluidAction.EXECUTE);

        if (!player.isCreative()) {
            if (copyOfHeld.isEmpty()) {
                player.setItemInHand(hand, result.getSecond());
            } else {
                player.setItemInHand(hand, copyOfHeld);
                player.getInventory().placeItemBackInInventory(result.getSecond());
            }
        }

        playTransferSound(level, pos, FluidHelper.getEmptySound(fluidStack), tank);
        return true;
    }

    public static boolean tryFillItemFromTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                              ItemStack heldItem, IFluidHandler tank) {
        if (!GenericItemFilling.canItemBeFilled(level, heldItem)) return false;

        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluid = tank.getFluidInTank(i).copy();
            if (fluid.isEmpty()) continue;

            int requiredAmount = GenericItemFilling.getRequiredAmountForItem(level, heldItem, fluid.copy());
            if (requiredAmount == -1 || requiredAmount > fluid.getAmount()) continue;

            if (level.isClientSide) return true;

            ItemStack toFill = player.isCreative() ? heldItem.copy() : heldItem;
            ItemStack filled = GenericItemFilling.fillItem(level, requiredAmount, toFill, fluid.copy());

            tank.drain(FluidHelper.copyStackWithAmount(fluid, requiredAmount), FluidAction.EXECUTE);

            if (!player.isCreative()) {
                player.getInventory().placeItemBackInInventory(filled);
            }

            playTransferSound(level, pos, FluidHelper.getFillSound(fluid), tank);
            return true;
        }

        return false;
    }

    private static void playTransferSound(Level level, BlockPos pos, SoundEvent sound, IFluidHandler tank) {
        if (sound == null) return;

        float fillRatio = tank.getTankCapacity(0) <= 0 ? 0F
                : Mth.clamp(tank.getFluidInTank(0).getAmount() / (float) tank.getTankCapacity(0), 0F, 1F);
        float pitch = (1F - fillRatio) / 1.5F + 0.5F + (level.random.nextFloat() - 0.5F) / 4F;

        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, pitch);
    }
}
