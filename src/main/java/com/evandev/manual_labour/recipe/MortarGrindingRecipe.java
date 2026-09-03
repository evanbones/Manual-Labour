package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.config.ModConfig;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MortarGrindingRecipe implements Recipe<MortarGrindingRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final int processingTime;
    private final NonNullList<ChanceResult> results;
    private final NonNullList<FluidStack> fluidResults;

    public MortarGrindingRecipe(String group, Ingredient input, int processingTime,
                                NonNullList<ChanceResult> results, NonNullList<FluidStack> fluidResults) {
        this.group = group;
        this.input = input;
        this.processingTime = processingTime;
        this.results = results;
        this.fluidResults = fluidResults;
    }

    public NonNullList<ChanceResult> getResults() {
        return results;
    }

    public NonNullList<FluidStack> getFluidResults() {
        return fluidResults;
    }

    public int getProcessingTime() {
        return processingTime > 0 ? processingTime : Math.max(1, ModConfig.get().defaultRecipeProcessingTicks);
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().stack();
    }

    @Override
    public boolean matches(MortarGrindingRecipeInput input, @NotNull Level level) {
        return this.input.test(input.item());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MortarGrindingRecipeInput input, HolderLookup.@NotNull Provider provider) {
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
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MORTAR_GRINDING.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.MORTAR_GRINDING.get();
    }

    public static class Serializer implements RecipeSerializer<MortarGrindingRecipe> {
        public static final MapCodec<MortarGrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(MortarGrindingRecipe::getGroup),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.input),
                Codec.INT.optionalFieldOf("processing_time", 0).forGetter(r -> r.processingTime),
                ChanceResult.CODEC.listOf().optionalFieldOf("results", List.of()).forGetter(r -> r.results),
                FluidStack.CODEC.listOf().optionalFieldOf("fluid_results", List.of()).forGetter(r -> r.fluidResults)
        ).apply(inst, (group, input, processingTime, resultsList, fluidResultsList) -> {
            NonNullList<ChanceResult> results = NonNullList.create();
            results.addAll(resultsList);
            NonNullList<FluidStack> fluidResults = NonNullList.create();
            fluidResults.addAll(fluidResultsList);
            return new MortarGrindingRecipe(group, input, processingTime, results, fluidResults);
        }));

        public static final StreamCodec<RegistryFriendlyByteBuf, MortarGrindingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    buffer.writeUtf(recipe.group);
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
                    buffer.writeVarInt(recipe.processingTime);
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
                    Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                    int processingTime = buffer.readVarInt();
                    int size = buffer.readVarInt();
                    NonNullList<ChanceResult> results = NonNullList.createWithCapacity(size);
                    for (int i = 0; i < size; i++) {
                        results.add(new ChanceResult(ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat()));
                    }
                    int fluidResultCount = buffer.readVarInt();
                    NonNullList<FluidStack> fluidResults = NonNullList.createWithCapacity(fluidResultCount);
                    for (int i = 0; i < fluidResultCount; i++) {
                        fluidResults.add(FluidStack.STREAM_CODEC.decode(buffer));
                    }
                    return new MortarGrindingRecipe(group, input, processingTime, results, fluidResults);
                }
        );

        @Override
        public @NotNull MapCodec<MortarGrindingRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, MortarGrindingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
