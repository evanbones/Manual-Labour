package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
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

    public static final IRecipeTypeInfo WORKSTONE_INFO = new IRecipeTypeInfo() {
        @Override
        public ResourceLocation getId() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "workstone");
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends RecipeSerializer<?>> T getSerializer() {
            return (T) ModRecipeSerializers.WORKSTONE.get();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
            return (RecipeType<R>) WORKSTONE.get();
        }
    };

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
