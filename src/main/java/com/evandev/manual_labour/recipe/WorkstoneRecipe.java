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
import org.jetbrains.annotations.NotNull;

public class WorkstoneRecipe implements Recipe<WorkstoneRecipeInput> {
    private final String group;
    private final Ingredient input;
    private final Ingredient tool;
    private final NonNullList<ChanceResult> results;

    public WorkstoneRecipe(String group, Ingredient input, Ingredient tool, NonNullList<ChanceResult> results) {
        this.group = group;
        this.input = input;
        this.tool = tool;
        this.results = results;
    }

    public NonNullList<ChanceResult> getResults() {
        return results;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return results.isEmpty() ? ItemStack.EMPTY : results.getFirst().stack();
    }

    @Override
    public boolean matches(WorkstoneRecipeInput input, @NotNull Level level) {
        return this.input.test(input.item()) && this.tool.test(input.tool());
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull WorkstoneRecipeInput inv, HolderLookup.@NotNull Provider provider) {
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

    public Ingredient getTool() {
        return this.tool;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
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
        public static final MapCodec<WorkstoneRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(WorkstoneRecipe::getGroup),
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.input),
                Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(WorkstoneRecipe::getTool),
                ChanceResult.CODEC.listOf().fieldOf("results").forGetter(r -> r.results)
        ).apply(inst, (group, input, tool, resultsList) -> {
            NonNullList<ChanceResult> nonNullList = NonNullList.create();
            nonNullList.addAll(resultsList);
            return new WorkstoneRecipe(group, input, tool, nonNullList);
        }));

        public static final StreamCodec<RegistryFriendlyByteBuf, WorkstoneRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    buffer.writeUtf(recipe.group);
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.tool);
                    buffer.writeVarInt(recipe.results.size());
                    for (ChanceResult result : recipe.results) {
                        ItemStack.STREAM_CODEC.encode(buffer, result.stack());
                        buffer.writeFloat(result.chance());
                    }
                },
                (buffer) -> {
                    String group = buffer.readUtf();
                    Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                    Ingredient tool = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
                    int size = buffer.readVarInt();
                    NonNullList<ChanceResult> results = NonNullList.createWithCapacity(size);
                    for (int i = 0; i < size; i++) {
                        results.add(new ChanceResult(ItemStack.STREAM_CODEC.decode(buffer), buffer.readFloat()));
                    }
                    return new WorkstoneRecipe(group, input, tool, results);
                }
        );

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
