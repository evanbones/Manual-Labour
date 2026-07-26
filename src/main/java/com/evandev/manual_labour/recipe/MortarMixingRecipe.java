package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.registry.ModRecipeSerializers;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MortarMixingRecipe implements Recipe<MortarMixingRecipeInput> {
    public static final int DEFAULT_PROCESSING_TIME = 100;

    private final String group;
    private final NonNullList<Ingredient> inputs;
    private final Optional<SizedFluidIngredient> fluidInput;
    private final int processingTime;
    private final NonNullList<ChanceResult> results;

    public MortarMixingRecipe(String group, NonNullList<Ingredient> inputs, Optional<SizedFluidIngredient> fluidInput,
                              int processingTime, NonNullList<ChanceResult> results) {
        this.group = group;
        this.inputs = inputs;
        this.fluidInput = fluidInput;
        this.processingTime = processingTime;
        this.results = results;
    }

    public NonNullList<ChanceResult> getResults() {
        return results;
    }

    public Optional<SizedFluidIngredient> getFluidInput() {
        return fluidInput;
    }

    public int getProcessingTime() {
        return processingTime;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().stack();
    }

    @Override
    public boolean matches(MortarMixingRecipeInput input, @NotNull Level level) {
        return MortarMatching.matchIngredients(this.inputs, input.items())
                && MortarMatching.matchFluid(this.fluidInput, input.fluid());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MortarMixingRecipeInput input, HolderLookup.@NotNull Provider provider) {
        return this.results.isEmpty() ? ItemStack.EMPTY : this.results.getFirst().stack().copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull String getGroup() {
        return this.group;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.inputs;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MORTAR_MIXING.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.MORTAR_MIXING.get();
    }

    public static class Serializer implements RecipeSerializer<MortarMixingRecipe> {
        public static final MapCodec<MortarMixingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(MortarMixingRecipe::getGroup),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(r -> r.inputs),
                SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("fluid_ingredient").forGetter(MortarMixingRecipe::getFluidInput),
                Codec.INT.optionalFieldOf("processing_time", DEFAULT_PROCESSING_TIME).forGetter(MortarMixingRecipe::getProcessingTime),
                ChanceResult.CODEC.listOf().fieldOf("results").forGetter(r -> r.results)
        ).apply(inst, (group, inputsList, fluidInput, processingTime, resultsList) -> {
            NonNullList<Ingredient> inputs = NonNullList.create();
            inputs.addAll(inputsList);
            NonNullList<ChanceResult> results = NonNullList.create();
            results.addAll(resultsList);
            return new MortarMixingRecipe(group, inputs, fluidInput, processingTime, results);
        }));

        public static final StreamCodec<RegistryFriendlyByteBuf, MortarMixingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    buffer.writeUtf(recipe.group);
                    buffer.writeVarInt(recipe.inputs.size());
                    for (Ingredient ingredient : recipe.inputs) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                    }
                    buffer.writeBoolean(recipe.fluidInput.isPresent());
                    recipe.fluidInput.ifPresent(fluidIngredient -> SizedFluidIngredient.STREAM_CODEC.encode(buffer, fluidIngredient));
                    buffer.writeVarInt(recipe.processingTime);
                    buffer.writeVarInt(recipe.results.size());
                    for (ChanceResult result : recipe.results) {
                        ItemStack.STREAM_CODEC.encode(buffer, result.stack());
                        buffer.writeFloat(result.chance());
                    }
                },
                (buffer) -> {
                    String group = buffer.readUtf();
                    int inputCount = buffer.readVarInt();
                    NonNullList<Ingredient> inputs = NonNullList.createWithCapacity(inputCount);
                    for (int i = 0; i < inputCount; i++) {
                        inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                    }
                    Optional<SizedFluidIngredient> fluidInput = buffer.readBoolean()
                            ? Optional.of(SizedFluidIngredient.STREAM_CODEC.decode(buffer))
                            : Optional.empty();
                    int processingTime = buffer.readVarInt();
                    int resultCount = buffer.readVarInt();
                    NonNullList<ChanceResult> results = NonNullList.createWithCapacity(resultCount);
                    for (int i = 0; i < resultCount; i++) {
                        results.add(new ChanceResult(ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat()));
                    }
                    return new MortarMixingRecipe(group, inputs, fluidInput, processingTime, results);
                }
        );

        @Override
        public @NotNull MapCodec<MortarMixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MortarMixingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
