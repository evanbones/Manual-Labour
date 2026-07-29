package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class MillstoneItemHandler implements IItemHandler {
    private final MillstoneBlockEntity millstone;

    public MillstoneItemHandler(MillstoneBlockEntity millstone) {
        this.millstone = millstone;
    }

    @Override
    public int getSlots() {
        return 18;
    }

    private boolean isOutputSlot(int slot) {
        return slot < 9;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isOutputSlot(slot) ? millstone.getBufferStack(false, slot) : millstone.getBufferStack(true, slot - 9);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (isOutputSlot(slot)) {
            return stack;
        }
        return millstone.insertInput(stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isOutputSlot(slot)) {
            return ItemStack.EMPTY;
        }
        return millstone.extractOutput(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !isOutputSlot(slot) && millstone.acceptsItem(stack);
    }
}
