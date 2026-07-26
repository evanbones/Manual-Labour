package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<WorkstoneRecipe>> WORKSTONE = RECIPE_TYPES.register("workstone", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return "workstone";
        }
    });

    public static final DeferredHolder<RecipeType<?>, RecipeType<MortarGrindingRecipe>> MORTAR_GRINDING = RECIPE_TYPES.register("mortar_grinding", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return "mortar_grinding";
        }
    });

    public static final DeferredHolder<RecipeType<?>, RecipeType<MortarMixingRecipe>> MORTAR_MIXING = RECIPE_TYPES.register("mortar_mixing", () -> new RecipeType<>() {
        @Override
        public String toString() {
            return "mortar_mixing";
        }
    });
}
