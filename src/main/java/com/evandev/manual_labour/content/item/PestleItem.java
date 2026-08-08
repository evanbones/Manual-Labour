package com.evandev.manual_labour.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

public class PestleItem extends Item {
    public PestleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return repair.is(ItemTags.STONE_TOOL_MATERIALS) || super.isValidRepairItem(toRepair, repair);
    }
}
