package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModRecipeSerializers;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class WorkstoneRecipeTypeInfo implements IRecipeTypeInfo {

    public static final WorkstoneRecipeTypeInfo INSTANCE = new WorkstoneRecipeTypeInfo();

    private WorkstoneRecipeTypeInfo() {
    }

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
        return (RecipeType<R>) ModRecipeTypes.WORKSTONE.get();
    }
}
