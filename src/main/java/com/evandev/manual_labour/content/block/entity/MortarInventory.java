package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.foundation.item.SmartInventory;
import net.minecraft.world.item.ItemStack;

public class MortarInventory extends SmartInventory {

    public MortarInventory(int slots, MortarBlockEntity be) {
        super(slots, be, 64, true);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        int firstFreeSlot = -1;

        for (int i = 0; i < getSlots(); i++) {
            if (i != slot && ItemStack.isSameItemSameComponents(stack, inv.getStackInSlot(i)))
                return stack;
            if (inv.getStackInSlot(i).isEmpty() && firstFreeSlot == -1)
                firstFreeSlot = i;
        }

        if (inv.getStackInSlot(slot).isEmpty() && firstFreeSlot != slot)
            return stack;

        return super.insertItem(slot, stack, simulate);
    }
}
