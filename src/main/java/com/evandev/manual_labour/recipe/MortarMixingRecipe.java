package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.foundation.recipe.HeatCondition;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MortarMixingRecipe implements Recipe<MortarMixingRecipeInput> {
    private final String group;
    private final NonNullList<Ingredient> inputs;
    private final Optional<SizedFluidIngredient> fluidInput;
    private final int processingTime;
    private final HeatCondition heatRequirement;
    private final NonNullList<ChanceResult> results;
    private final NonNullList<FluidStack> fluidResults;

    public MortarMixingRecipe(String group, NonNullList<Ingredient> inputs, Optional<SizedFluidIngredient> fluidInput,
                              int processingTime, HeatCondition heatRequirement, NonNullList<ChanceResult> results,
                              NonNullList<FluidStack> fluidResults) {
        this.group = group;
        this.inputs = inputs;
        this.fluidInput = fluidInput;
        this.processingTime = processingTime;
        this.heatRequirement = heatRequirement;
        this.results = results;
        this.fluidResults = fluidResults;
    }

    public HeatCondition getHeatRequirement() {
        return heatRequirement;
    }

    public NonNullList<ChanceResult> getResults() {
        return results;
    }

    public NonNullList<FluidStack> getFluidResults() {
        return fluidResults;
    }

    public Optional<SizedFluidIngredient> getFluidInput() {
        return fluidInput;
    }

    public int getProcessingTime() {
        return processingTime > 0 ? processingTime : Math.max(1, ModConfig.get().defaultRecipeProcessingTicks);
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
                Codec.INT.optionalFieldOf("processing_time", 0).forGetter(r -> r.processingTime),
                HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(MortarMixingRecipe::getHeatRequirement),
                ChanceResult.CODEC.listOf().optionalFieldOf("results", List.of()).forGetter(r -> r.results),
                FluidStack.CODEC.listOf().optionalFieldOf("fluid_results", List.of()).forGetter(r -> r.fluidResults)
        ).apply(inst, (group, inputsList, fluidInput, processingTime, heatRequirement, resultsList, fluidResultsList) -> {
            NonNullList<Ingredient> inputs = NonNullList.create();
            inputs.addAll(inputsList);
            NonNullList<ChanceResult> results = NonNullList.create();
            results.addAll(resultsList);
            NonNullList<FluidStack> fluidResults = NonNullList.create();
            fluidResults.addAll(fluidResultsList);
            return new MortarMixingRecipe(group, inputs, fluidInput, processingTime, heatRequirement, results, fluidResults);
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
                    buffer.writeEnum(recipe.heatRequirement);
                    buffer.writeVarInt(recipe.results.size());
                    for (ChanceResult result : recipe.results) {
                        ItemStack.STREAM_CODEC.encode(buffer, result.stack());
                        buffer.writeFloat(result.chance());
                    }
                    buffer.writeVarInt(recipe.fluidResults.size());
                    for (FluidStack fluidResult : recipe.fluidResults) {
                        FluidStack.STREAM_CODEC.encode(buffer, fluidResult);
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
                    HeatCondition heatRequirement = buffer.readEnum(HeatCondition.class);
                    int resultCount = buffer.readVarInt();
                    NonNullList<ChanceResult> results = NonNullList.createWithCapacity(resultCount);
                    for (int i = 0; i < resultCount; i++) {
                        results.add(new ChanceResult(ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat()));
                    }
                    int fluidResultCount = buffer.readVarInt();
                    NonNullList<FluidStack> fluidResults = NonNullList.createWithCapacity(fluidResultCount);
                    for (int i = 0; i < fluidResultCount; i++) {
                        fluidResults.add(FluidStack.STREAM_CODEC.decode(buffer));
                    }
                    return new MortarMixingRecipe(group, inputs, fluidInput, processingTime, heatRequirement, results, fluidResults);
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
