package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<WorkstoneRecipe>> WORKSTONE = RECIPE_TYPES.register("workstone", () -> new RecipeType<WorkstoneRecipe>() {
        @Override
        public String toString() {
            return "workstone";
        }
    });
}
