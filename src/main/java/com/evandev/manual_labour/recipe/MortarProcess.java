package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public interface MortarProcess {
    private static List<ProcessingOutput> toOutputs(List<ChanceResult> results) {
        return results.stream().map(r -> new ProcessingOutput(r.stack(), r.chance())).toList();
    }

    List<Ingredient> ingredients();

    List<SizedFluidIngredient> fluidIngredients();

    int processingTime();

    List<ItemStack> rollResults(RandomSource random);

    List<FluidStack> fluidResults();

    List<ProcessingOutput> displayOutputs();

    default HeatCondition heatRequirement() {
        return HeatCondition.NONE;
    }

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
            return recipe.getFluidResults();
        }

        @Override
        public List<ProcessingOutput> displayOutputs() {
            return toOutputs(recipe.getResults());
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
            return recipe.getFluidResults();
        }

        @Override
        public List<ProcessingOutput> displayOutputs() {
            return toOutputs(recipe.getResults());
        }

        @Override
        public HeatCondition heatRequirement() {
            return recipe.getHeatRequirement();
        }
    }
}
