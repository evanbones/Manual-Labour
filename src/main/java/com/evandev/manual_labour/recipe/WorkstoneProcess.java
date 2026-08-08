package com.evandev.manual_labour.recipe;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface WorkstoneProcess {

    List<ItemStack> rollResults(RandomSource random);

    default ToolUse toolUse() {
        return ToolUse.DAMAGE;
    }

    enum ToolUse {
        DAMAGE,
        CONSUME,
        KEEP
    }
}
