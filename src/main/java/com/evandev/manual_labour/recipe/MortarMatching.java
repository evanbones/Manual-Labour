package com.evandev.manual_labour.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public final class MortarMatching {
    private MortarMatching() {
    }

    public static boolean matchIngredients(List<Ingredient> ingredients, List<ItemStack> availableStacks) {
        if (ingredients.isEmpty()) return true;
        boolean[] claimed = new boolean[availableStacks.size()];
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < availableStacks.size(); i++) {
                if (claimed[i]) continue;
                if (ingredient.test(availableStacks.get(i))) {
                    claimed[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    public static boolean matchFluid(Optional<SizedFluidIngredient> fluidInput, FluidStack fluid) {
        return fluidInput.isEmpty() || fluidInput.get().test(fluid);
    }

    public static boolean matchFluidIngredients(List<SizedFluidIngredient> fluidIngredients, FluidStack fluid) {
        for (SizedFluidIngredient fluidIngredient : fluidIngredients) {
            if (!fluidIngredient.test(fluid)) return false;
        }
        return true;
    }
}
