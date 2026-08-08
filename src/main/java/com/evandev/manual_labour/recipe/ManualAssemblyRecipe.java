package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.registry.ModDataComponents;
import com.evandev.manual_labour.registry.ModRecipeSerializers;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This is a Create-independent counterpart to Create's {@code SequencedAssemblyRecipe} (MIT, Copyright
 * (c) simibubi).
 */
public class ManualAssemblyRecipe implements Recipe<RecipeWrapper> {

    private final Ingredient ingredient;
    private final ProcessingOutput transitionalItem;
    private final List<WorkstoneRecipeLike> sequence;
    private final List<ProcessingOutput> resultPool;
    private final int loops;

    public ManualAssemblyRecipe(Ingredient ingredient, ProcessingOutput transitionalItem,
                                List<WorkstoneRecipeLike> sequence, List<ProcessingOutput> resultPool, int loops) {
        this.ingredient = ingredient;
        this.transitionalItem = transitionalItem;
        this.sequence = sequence;
        this.resultPool = resultPool;
        this.loops = loops;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public ItemStack getTransitionalItem() {
        return transitionalItem.getStack();
    }

    public List<WorkstoneRecipeLike> getSequence() {
        return sequence;
    }

    public List<ProcessingOutput> getResultPool() {
        return resultPool;
    }

    public int getLoops() {
        return loops;
    }

    public boolean appliesTo(ResourceLocation id, ItemStack input) {
        ManualAssemblyState state = input.get(ModDataComponents.MANUAL_ASSEMBLY.get());
        if (state != null) {
            return state.id().equals(id) && getTransitionalItem().getItem() == input.getItem();
        }
        return ingredient.test(input);
    }

    public int getStep(ItemStack input) {
        ManualAssemblyState state = input.get(ModDataComponents.MANUAL_ASSEMBLY.get());
        return state == null ? 0 : state.step();
    }

    public WorkstoneRecipeLike getNextStep(ItemStack input) {
        return sequence.get(getStep(input) % sequence.size());
    }

    public ItemStack advance(ResourceLocation id, ItemStack input, RandomSource random) {
        int step = getStep(input) + 1;
        if (step / sequence.size() >= loops) {
            return rollResult(random);
        }

        ItemStack advanced = getTransitionalItem().copyWithCount(1);
        advanced.set(ModDataComponents.MANUAL_ASSEMBLY.get(), new ManualAssemblyState(id, step));
        return advanced;
    }

    private ItemStack rollResult(RandomSource random) {
        float totalWeight = 0;
        for (ProcessingOutput entry : resultPool) {
            totalWeight += entry.getChance();
        }

        float number = random.nextFloat() * totalWeight;
        for (ProcessingOutput entry : resultPool) {
            number -= entry.getChance();
            if (number < 0) return entry.getStack().copy();
        }
        return ItemStack.EMPTY;
    }

    public float getOutputChance() {
        float totalWeight = 0;
        for (ProcessingOutput entry : resultPool) {
            totalWeight += entry.getChance();
        }
        return totalWeight == 0 ? 1 : resultPool.getFirst().getChance() / totalWeight;
    }

    @Override
    public boolean matches(@NotNull RecipeWrapper inv, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeWrapper input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return resultPool.isEmpty() ? ItemStack.EMPTY : resultPool.getFirst().getStack();
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(ingredient);
        return ingredients;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MANUAL_ASSEMBLY.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipeTypes.MANUAL_ASSEMBLY.get();
    }

    public static class Serializer implements RecipeSerializer<ManualAssemblyRecipe> {

        private static final Codec<WorkstoneRecipeLike> STEP_CODEC = Recipe.CODEC.comapFlatMap(
                recipe -> recipe instanceof WorkstoneRecipeLike step
                        ? DataResult.success(step)
                        : DataResult.error(() -> recipe.getType() + " is not a Workstone recipe and cannot be an assembly step"),
                step -> step
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, WorkstoneRecipeLike> STEP_STREAM_CODEC =
                Recipe.STREAM_CODEC.map(
                        recipe -> {
                            if (recipe instanceof WorkstoneRecipeLike step) return step;
                            throw new DecoderException(recipe.getType() + " is not a Workstone recipe and cannot be an assembly step");
                        },
                        step -> step
                );

        private static final MapCodec<ManualAssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ManualAssemblyRecipe::getIngredient),
                ProcessingOutput.CODEC.fieldOf("transitional_item").forGetter(r -> r.transitionalItem),
                ExtraCodecs.nonEmptyList(STEP_CODEC.listOf()).fieldOf("sequence").forGetter(ManualAssemblyRecipe::getSequence),
                ExtraCodecs.nonEmptyList(ProcessingOutput.CODEC.listOf()).fieldOf("results").forGetter(ManualAssemblyRecipe::getResultPool),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("loops", 1).forGetter(ManualAssemblyRecipe::getLoops)
        ).apply(instance, ManualAssemblyRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ManualAssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ManualAssemblyRecipe::getIngredient,
                ProcessingOutput.STREAM_CODEC, r -> r.transitionalItem,
                STEP_STREAM_CODEC.apply(ByteBufCodecs.list()), ManualAssemblyRecipe::getSequence,
                ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), ManualAssemblyRecipe::getResultPool,
                ByteBufCodecs.VAR_INT, ManualAssemblyRecipe::getLoops,
                ManualAssemblyRecipe::new
        );

        @Override
        public @NotNull MapCodec<ManualAssemblyRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, ManualAssemblyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
