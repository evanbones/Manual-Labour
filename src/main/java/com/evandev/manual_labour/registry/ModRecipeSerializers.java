package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WorkstoneRecipe>> WORKSTONE = RECIPE_SERIALIZERS.register("workstone", WorkstoneRecipe.Serializer::new);
}
