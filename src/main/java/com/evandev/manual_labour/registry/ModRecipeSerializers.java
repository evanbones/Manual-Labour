package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> WORKSTONE = RECIPE_SERIALIZERS.register("workstone", () -> CreateCompat.get().workstoneRecipeSerializer());

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MortarGrindingRecipe>> MORTAR_GRINDING = RECIPE_SERIALIZERS.register("mortar_grinding", MortarGrindingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MortarMixingRecipe>> MORTAR_MIXING = RECIPE_SERIALIZERS.register("mortar_mixing", MortarMixingRecipe.Serializer::new);
}
