package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.registry.ModDataComponents;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public final class ManualAssembly {

    private ManualAssembly() {
    }

    public static boolean isInProgress(ItemStack stack) {
        return stack.has(ModDataComponents.MANUAL_ASSEMBLY.get())
                || CreateCompat.get().isSequencedAssemblyInProgress(stack);
    }

    public static Optional<WorkstoneProcess> findStep(Level level, ItemStack stored, ItemStack tool) {
        if (stored.isEmpty() || tool.isEmpty()) return Optional.empty();

        for (RecipeHolder<ManualAssemblyRecipe> holder : recipes(level)) {
            ManualAssemblyRecipe recipe = holder.value();
            if (!recipe.appliesTo(holder.id(), stored)) continue;

            WorkstoneRecipeLike step = recipe.getNextStep(stored);
            if (!toolIngredient(step).test(tool)) continue;

            return Optional.of(new ManualAssemblyProcess(recipe, holder.id(), stored, step));
        }

        return Optional.empty();
    }

    public static boolean hasAnyFor(Level level, ItemStack stored) {
        if (stored.isEmpty()) return false;

        for (RecipeHolder<ManualAssemblyRecipe> holder : recipes(level)) {
            if (holder.value().appliesTo(holder.id(), stored)) return true;
        }
        return false;
    }

    private static List<RecipeHolder<ManualAssemblyRecipe>> recipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MANUAL_ASSEMBLY.get());
    }

    private static Ingredient toolIngredient(WorkstoneRecipeLike step) {
        return step.getIngredients().size() < 2 ? Ingredient.EMPTY : step.getToolIngredient();
    }
}
