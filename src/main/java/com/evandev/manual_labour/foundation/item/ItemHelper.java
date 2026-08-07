package com.evandev.manual_labour.foundation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers from Create's {@code com.simibubi.create.foundation.item.ItemHelper}
 * (MIT, Copyright (c) simibubi)
 */
public final class ItemHelper {

    private ItemHelper() {
    }

    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
        if (inv == null) return 0;

        int filledSlots = 0;
        float fillRatio = 0.0F;
        int totalSlots = inv.getSlots();

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            int slotLimit = inv.getSlotLimit(slot);
            if (slotLimit == 0) {
                totalSlots--;
                continue;
            }
            ItemStack stack = inv.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                fillRatio += (float) stack.getCount() / (float) Math.min(slotLimit, stack.getMaxStackSize());
                filledSlots++;
            }
        }

        if (totalSlots == 0) return 0;

        fillRatio = fillRatio / totalSlots;
        return Mth.floor(fillRatio * 14.0F) + (filledSlots > 0 ? 1 : 0);
    }

    public static void dropContents(Level level, BlockPos pos, IItemHandler inv) {
        for (int slot = 0; slot < inv.getSlots(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inv.getStackInSlot(slot));
        }
    }
}
