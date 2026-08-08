package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.recipe.WorkstoneProcess;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CreateWorkstoneProcess(ProcessingRecipe<?, ?> recipe) implements WorkstoneProcess {

    public static CreateWorkstoneProcess of(ProcessingRecipe<?, ?> recipe) {
        return new CreateWorkstoneProcess(recipe);
    }

    @Override
    public List<ItemStack> rollResults(RandomSource random) {
        return recipe.rollResults(random);
    }

    @Override
    public ToolUse toolUse() {
        if (recipe instanceof DeployerApplicationRecipe deployerRecipe) {
            return deployerRecipe.shouldKeepHeldItem() ? ToolUse.KEEP : ToolUse.CONSUME;
        }
        return ToolUse.DAMAGE;
    }
}
