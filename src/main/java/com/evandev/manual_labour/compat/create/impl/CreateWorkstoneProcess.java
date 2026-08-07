package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.recipe.WorkstoneProcess;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record CreateWorkstoneProcess(ProcessingRecipe<?, ?> recipe, boolean pressing) implements WorkstoneProcess {

    public static CreateWorkstoneProcess of(ProcessingRecipe<?, ?> recipe) {
        return new CreateWorkstoneProcess(recipe, false);
    }

    public static CreateWorkstoneProcess pressing(ProcessingRecipe<?, ?> recipe) {
        return new CreateWorkstoneProcess(recipe, true);
    }

    @Override
    public List<ItemStack> rollResults(RandomSource random, float yieldMultiplier) {
        if (yieldMultiplier == 1.0F) {
            return recipe.rollResults(random);
        }

        List<ItemStack> rolled = new ArrayList<>();
        for (ProcessingOutput output : recipe.getRollableResults()) {
            float chance = output.getChance() * yieldMultiplier;
            if (chance >= 1.0F || random.nextFloat() < chance) {
                rolled.add(output.getStack().copy());
            }
        }
        return rolled;
    }

    @Override
    public ToolUse toolUse() {
        if (recipe instanceof DeployerApplicationRecipe deployerRecipe) {
            return deployerRecipe.shouldKeepHeldItem() ? ToolUse.KEEP : ToolUse.CONSUME;
        }
        return ToolUse.DAMAGE;
    }

    @Override
    public boolean usesPressingYield() {
        return pressing;
    }
}
