package com.evandev.manual_labour.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record MortarGrindingRecipeInput(ItemStack item) implements RecipeInput {
    @Override
    public @NotNull ItemStack getItem(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Recipe does not contain slot " + index);
        }
        return this.item;
    }

    @Override
    public int size() {
        return 1;
    }
}
