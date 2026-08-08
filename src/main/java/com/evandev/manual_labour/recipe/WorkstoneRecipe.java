package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.foundation.recipe.ProcessingRecipeData;
import com.evandev.manual_labour.registry.ModRecipeSerializers;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WorkstoneRecipe(ProcessingRecipeData data) implements WorkstoneRecipeLike {

    @Override
    public boolean matches(@NotNull RecipeWrapper inv, @NotNull Level level) {
        if (data.getIngredients().size() < 2) return false;
        return getInputIngredient().test(inv.getItem(0)) && getToolIngredient().test(inv.getItem(1));
    }

    @Override
    public Ingredient getInputIngredient() {
        return data.getIngredients().getFirst();
    }

    @Override
    public Ingredient getToolIngredient() {
        return data.getIngredients().get(1);
    }

    @Override
    public List<ProcessingOutput> getOutputs() {
        return data.getResults();
    }

    @Override
    public List<ItemStack> rollResults(RandomSource random) {
        return data.rollResults(random);
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return data.getIngredients();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeWrapper input, HolderLookup.@NotNull Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return data.getResults().isEmpty() ? ItemStack.EMPTY : data.getResults().getFirst().getStack();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull String getGroup() {
        return "processing";
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.WORKSTONE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.WORKSTONE.get();
    }

    public static class Serializer implements RecipeSerializer<WorkstoneRecipe> {

        private static final MapCodec<WorkstoneRecipe> CODEC =
                ProcessingRecipeData.CODEC.xmap(WorkstoneRecipe::new, WorkstoneRecipe::data);

        private static final StreamCodec<RegistryFriendlyByteBuf, WorkstoneRecipe> STREAM_CODEC =
                ProcessingRecipeData.STREAM_CODEC.map(WorkstoneRecipe::new, WorkstoneRecipe::data);

        @Override
        public @NotNull MapCodec<WorkstoneRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, WorkstoneRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
