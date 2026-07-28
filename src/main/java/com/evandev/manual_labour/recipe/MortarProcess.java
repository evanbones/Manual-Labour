package com.evandev.manual_labour.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public interface MortarProcess {
    List<Ingredient> ingredients();

    List<SizedFluidIngredient> fluidIngredients();

    int processingTime();

    List<ItemStack> rollResults(RandomSource random);

    List<FluidStack> fluidResults();

    record OwnGrindingProcess(MortarGrindingRecipe recipe) implements MortarProcess {
        @Override
        public List<Ingredient> ingredients() {
            return List.of(recipe.getIngredients().getFirst());
        }

        @Override
        public List<SizedFluidIngredient> fluidIngredients() {
            return List.of();
        }

        @Override
        public int processingTime() {
            return recipe.getProcessingTime();
        }

        @Override
        public List<ItemStack> rollResults(RandomSource random) {
            return recipe.getResults().stream()
                    .map(chanceResult -> chanceResult.rollOutput(random))
                    .filter(stack -> !stack.isEmpty())
                    .toList();
        }

        @Override
        public List<FluidStack> fluidResults() {
            return List.of();
        }
    }

    record OwnMixingProcess(MortarMixingRecipe recipe) implements MortarProcess {
        @Override
        public List<Ingredient> ingredients() {
            return recipe.getIngredients();
        }

        @Override
        public List<SizedFluidIngredient> fluidIngredients() {
            Optional<SizedFluidIngredient> fluidInput = recipe.getFluidInput();
            return fluidInput.map(List::of).orElseGet(List::of);
        }

        @Override
        public int processingTime() {
            return recipe.getProcessingTime();
        }

        @Override
        public List<ItemStack> rollResults(RandomSource random) {
            return recipe.getResults().stream()
                    .map(chanceResult -> chanceResult.rollOutput(random))
                    .filter(stack -> !stack.isEmpty())
                    .toList();
        }

        @Override
        public List<FluidStack> fluidResults() {
            return List.of();
        }
    }

    record CreateProcess(ProcessingRecipe<?, ?> recipe) implements MortarProcess {
        @Override
        public List<Ingredient> ingredients() {
            return recipe.getIngredients();
        }

        @Override
        public List<SizedFluidIngredient> fluidIngredients() {
            return recipe.getFluidIngredients();
        }

        @Override
        public int processingTime() {
            int duration = recipe.getProcessingDuration();
            return duration > 0 ? duration : MortarMixingRecipe.DEFAULT_PROCESSING_TIME;
        }

        @Override
        public List<ItemStack> rollResults(RandomSource random) {
            return recipe.rollResults(random);
        }

        @Override
        public List<FluidStack> fluidResults() {
            return recipe.getFluidResults();
        }
    }
}
