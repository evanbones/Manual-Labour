package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

public final class MortarHeat {

    private MortarHeat() {
    }

    public static HeatCondition below(BlockGetter level, BlockPos mortarPos) {
        BlockState state = level.getBlockState(mortarPos.below());

        HeatCondition blazeBurner = CreateCompat.get().getBlazeBurnerHeat(state);
        if (blazeBurner != HeatCondition.NONE) return blazeBurner;

        return isLitHeatSource(state) ? HeatCondition.HEATED : HeatCondition.NONE;
    }

    public static List<ItemStack> sourceItems() {
        List<ItemStack> items = new ArrayList<>();

        BuiltInRegistries.BLOCK.getTag(ModTags.Blocks.HEAT_SOURCES).ifPresent(tag -> tag.forEach(holder -> {
            ItemStack stack = new ItemStack(holder.value());
            if (!stack.isEmpty()) items.add(stack);
        }));

        CreateCompat.get().addHeatSourceItems(items);
        return items;
    }

    private static boolean isLitHeatSource(BlockState state) {
        if (!state.is(ModTags.Blocks.HEAT_SOURCES)) return false;
        return !state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT);
    }
}
