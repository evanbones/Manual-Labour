package com.evandev.manual_labour.recipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface WorkstoneProcess {

    List<ItemStack> rollResults(RandomSource random, float yieldMultiplier);

    default ToolUse toolUse() {
        return ToolUse.DAMAGE;
    }

    default boolean usesPressingYield() {
        return false;
    }

    enum ToolUse {
        DAMAGE,
        CONSUME,
        KEEP
    }
}
