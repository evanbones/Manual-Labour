package com.evandev.manual_labour.compat.jei.assembly;

import com.evandev.manual_labour.recipe.ManualAssemblyRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record SimpleAssemblyView(Ingredient input, List<AssemblyStep> steps, int loops, ItemStack result,
                                 float outputChance) implements AssemblyView {

    public static SimpleAssemblyView of(ManualAssemblyRecipe recipe) {
        List<AssemblyStep> steps = recipe.getSequence().stream()
                .map(step -> (AssemblyStep) WorkstoneAssemblyStep.of(step))
                .toList();
        ItemStack result = recipe.getResultPool().isEmpty()
                ? ItemStack.EMPTY
                : recipe.getResultPool().getFirst().getStack();
        return new SimpleAssemblyView(recipe.getIngredient(), steps, recipe.getLoops(), result, recipe.getOutputChance());
    }
}
