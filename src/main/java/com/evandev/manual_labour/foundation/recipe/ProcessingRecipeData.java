package com.evandev.manual_labour.foundation.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * The payload of a processing-style recipe: ingredients, chance-weighted results, a duration and a
 * heat requirement.
 * <p>
 * Ported from Create's {@code ProcessingRecipeParams} (MIT, Copyright (c) simibubi).
 */
public class ProcessingRecipeData {

    public static final MapCodec<ProcessingRecipeData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(d -> d.ingredients),
            ProcessingOutput.CODEC.listOf().fieldOf("results").forGetter(d -> d.results),
            Codec.INT.optionalFieldOf("processing_time", 0).forGetter(d -> d.processingDuration),
            HeatCondition.CODEC.optionalFieldOf("heat_requirement", HeatCondition.NONE).forGetter(d -> d.requiredHeat)
    ).apply(instance, ProcessingRecipeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipeData> STREAM_CODEC = StreamCodec.of(
            (buffer, data) -> {
                CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, data.ingredients);
                CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).encode(buffer, data.fluidIngredients);
                CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buffer, data.results);
                CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).encode(buffer, data.fluidResults);
                ByteBufCodecs.VAR_INT.encode(buffer, data.processingDuration);
                CatnipStreamCodecBuilders.ofEnum(HeatCondition.class).encode(buffer, data.requiredHeat);
            },
            buffer -> {
                NonNullList<Ingredient> ingredients =
                        CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
                NonNullList<SizedFluidIngredient> fluidIngredients =
                        CatnipStreamCodecBuilders.nonNullList(SizedFluidIngredient.STREAM_CODEC).decode(buffer);
                NonNullList<ProcessingOutput> results =
                        CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buffer);
                NonNullList<FluidStack> fluidResults =
                        CatnipStreamCodecBuilders.nonNullList(FluidStack.STREAM_CODEC).decode(buffer);
                int duration = ByteBufCodecs.VAR_INT.decode(buffer);
                HeatCondition heat = CatnipStreamCodecBuilders.ofEnum(HeatCondition.class).decode(buffer);
                return new ProcessingRecipeData(ingredients, fluidIngredients, results, fluidResults, duration, heat);
            }
    );

    private final NonNullList<Ingredient> ingredients;
    private final NonNullList<SizedFluidIngredient> fluidIngredients;
    private final NonNullList<ProcessingOutput> results;
    private final NonNullList<FluidStack> fluidResults;
    private final int processingDuration;
    private final HeatCondition requiredHeat;

    public ProcessingRecipeData(List<Ingredient> ingredients, List<ProcessingOutput> results,
                                int processingDuration, HeatCondition requiredHeat) {
        this(copyOf(ingredients), NonNullList.create(), copyOf(results), NonNullList.create(),
                processingDuration, requiredHeat);
    }

    private ProcessingRecipeData(NonNullList<Ingredient> ingredients, NonNullList<SizedFluidIngredient> fluidIngredients,
                                 NonNullList<ProcessingOutput> results, NonNullList<FluidStack> fluidResults,
                                 int processingDuration, HeatCondition requiredHeat) {
        this.ingredients = ingredients;
        this.fluidIngredients = fluidIngredients;
        this.results = results;
        this.fluidResults = fluidResults;
        this.processingDuration = processingDuration;
        this.requiredHeat = requiredHeat;
    }

    private static <T> NonNullList<T> copyOf(List<T> source) {
        NonNullList<T> list = NonNullList.create();
        list.addAll(source);
        return list;
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public NonNullList<ProcessingOutput> getResults() {
        return results;
    }

    public NonNullList<SizedFluidIngredient> getFluidIngredients() {
        return fluidIngredients;
    }

    public NonNullList<FluidStack> getFluidResults() {
        return fluidResults;
    }

    public int getProcessingDuration() {
        return processingDuration;
    }

    public HeatCondition getRequiredHeat() {
        return requiredHeat;
    }

    public List<ItemStack> rollResults(RandomSource random) {
        List<ItemStack> rolled = new ArrayList<>(results.size());
        for (ProcessingOutput output : results) {
            ItemStack stack = output.rollOutput(random);
            if (!stack.isEmpty()) rolled.add(stack);
        }
        return rolled;
    }
}
