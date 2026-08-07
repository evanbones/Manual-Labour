package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public record CreateMortarProcess(ProcessingRecipe<?, ?> recipe) implements MortarProcess {

    @Override
    public List<Ingredient> ingredients() {
        return recipe.getIngredients();
    }

    @Override
    public List<SizedFluidIngredient> fluidIngredients() {
        return recipe.getFluidIngredients();
    }

    @Override
    public int processingTime() {
        int duration = recipe.getProcessingDuration();
        return duration > 0 ? duration : MortarMixingRecipe.DEFAULT_PROCESSING_TIME;
    }

    @Override
    public List<ItemStack> rollResults(RandomSource random) {
        return recipe.rollResults(random);
    }

    @Override
    public List<FluidStack> fluidResults() {
        return recipe.getFluidResults();
    }

    @Override
    public List<ProcessingOutput> displayOutputs() {
        return recipe.getRollableResults().stream()
                .map(result -> new ProcessingOutput(result.getStack(), result.getChance()))
                .toList();
    }
}
