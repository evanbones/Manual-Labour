package com.evandev.manual_labour.foundation.item;

import com.evandev.manual_labour.foundation.blockentity.SyncedBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An item handler that notifies its owning block entity whenever its contents change, and can
 * optionally stack normally-unstackable items up to a shared limit.
 * <p>
 * Adapted from Create's {@code com.simibubi.create.foundation.item.SmartInventory} (MIT, Copyright
 * (c) simibubi).
 */
public class SmartInventory implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    protected final SyncedStackHandler inv;
    protected final boolean stackNonStackables;
    protected boolean insertionAllowed = true;
    protected boolean extractionAllowed = true;
    protected int stackSize;

    public SmartInventory(int slots, SyncedBlockEntity be) {
        this(slots, be, 64, false);
    }

    public SmartInventory(int slots, SyncedBlockEntity be, int stackSize, boolean stackNonStackables) {
        this.inv = new SyncedStackHandler(slots, be, stackNonStackables, stackSize);
        this.stackNonStackables = stackNonStackables;
        this.stackSize = stackSize;
    }

    public SmartInventory withMaxStackSize(int maxStackSize) {
        stackSize = maxStackSize;
        inv.stackSize = maxStackSize;
        return this;
    }

    public SmartInventory whenContentsChanged(Consumer<Integer> updateCallback) {
        inv.whenContentsChange(updateCallback);
        return this;
    }

    public SmartInventory allowInsertion() {
        insertionAllowed = true;
        return this;
    }

    public SmartInventory allowExtraction() {
        extractionAllowed = true;
        return this;
    }

    public SmartInventory forbidInsertion() {
        insertionAllowed = false;
        return this;
    }

    public SmartInventory forbidExtraction() {
        extractionAllowed = false;
        return this;
    }

    @Override
    public int getSlots() {
        return inv.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inv.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        inv.setStackInSlot(slot, stack);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!insertionAllowed) return stack;
        return inv.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!extractionAllowed) return ItemStack.EMPTY;
        if (stackNonStackables) {
            ItemStack extracted = inv.extractItem(slot, amount, true);
            if (!extracted.isEmpty() && extracted.getMaxStackSize() < extracted.getCount()) {
                amount = extracted.getMaxStackSize();
            }
        }
        return inv.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Math.min(inv.getSlotLimit(slot), stackSize);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return inv.isItemValid(slot, stack);
    }

    public int getStackLimit(int slot, @NotNull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider registries) {
        return inv.serializeNBT(registries);
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider registries, @NotNull CompoundTag nbt) {
        inv.deserializeNBT(registries, nbt);
    }

    protected static class SyncedStackHandler extends ItemStackHandler {

        private final SyncedBlockEntity blockEntity;
        private final boolean stackNonStackables;
        private int stackSize;
        private Consumer<Integer> updateCallback;

        public SyncedStackHandler(int slots, SyncedBlockEntity be, boolean stackNonStackables, int stackSize) {
            super(slots);
            this.blockEntity = be;
            this.stackNonStackables = stackNonStackables;
            this.stackSize = stackSize;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (updateCallback != null) updateCallback.accept(slot);
            blockEntity.notifyUpdate();
        }

        @Override
        public int getSlotLimit(int slot) {
            return Math.min(stackNonStackables ? 64 : super.getSlotLimit(slot), stackSize);
        }

        public void whenContentsChange(Consumer<Integer> updateCallback) {
            this.updateCallback = updateCallback;
        }
    }
}
