package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class MortarHeat {

    private MortarHeat() {
    }

    public static HeatCondition below(BlockGetter level, BlockPos mortarPos) {
        BlockState state = level.getBlockState(mortarPos.below());

        HeatCondition blazeBurner = CreateCompat.get().getBlazeBurnerHeat(state);
        if (blazeBurner != HeatCondition.NONE) return blazeBurner;

        return isLitHeatSource(state) ? HeatCondition.HEATED : HeatCondition.NONE;
    }

    private static boolean isLitHeatSource(BlockState state) {
        if (!state.is(ModTags.Blocks.HEAT_SOURCES)) return false;
        return !state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT);
    }
}
