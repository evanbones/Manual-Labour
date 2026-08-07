package com.evandev.manual_labour.compat.create.impl.millstone;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class MillstoneItemHandler implements IItemHandler {
    private final MillstoneBlockEntity millstone;

    public MillstoneItemHandler(MillstoneBlockEntity millstone) {
        this.millstone = millstone;
    }

    @Override
    public int getSlots() {
        return millstone.capability != null ? millstone.capability.getSlots() : 10;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return millstone.capability != null ? millstone.capability.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return millstone.capability != null ? millstone.capability.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return millstone.capability != null ? millstone.capability.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return millstone.capability != null ? millstone.capability.getSlotLimit(slot) : 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return millstone.capability != null && millstone.capability.isItemValid(slot, stack);
    }
}
