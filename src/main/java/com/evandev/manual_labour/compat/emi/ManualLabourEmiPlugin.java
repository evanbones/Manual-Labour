package com.evandev.manual_labour.compat.emi;

import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.evandev.manual_labour.registry.ModTags;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EmiEntrypoint
public class ManualLabourEmiPlugin implements EmiPlugin {

    private static EmiIngredient toEmiIngredient(SizedFluidIngredient fluidIngredient) {
        List<EmiStack> options = Arrays.stream(fluidIngredient.getFluids())
                .map(fluidStack -> EmiStack.of(fluidStack.getFluid(), fluidStack.getAmount()))
                .toList();
        return EmiIngredient.of(options);
    }

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ManualLabourCategories.WORKSTONE);
        registry.addWorkstation(ManualLabourCategories.WORKSTONE, ManualLabourCategories.WORKSTONE_STACK);

        for (RecipeHolder<WorkstoneRecipe> recipeHolder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.WORKSTONE.get())) {
            WorkstoneRecipe recipe = recipeHolder.value();
            var emiOutputs = recipe.getResults().stream()
                    .map(chanceResult -> EmiStack.of(chanceResult.stack()).setChance(chanceResult.chance()))
                    .toList();

            registry.addRecipe(new WorkstoneEmiRecipe(
                    recipeHolder.id(),
                    EmiIngredient.of(recipe.getTool()),
                    EmiIngredient.of(recipe.getIngredients().getFirst()),
                    emiOutputs
            ));
        }

        registry.addCategory(ManualLabourCategories.MORTAR_GRINDING);
        registry.addWorkstation(ManualLabourCategories.MORTAR_GRINDING, ManualLabourCategories.MORTAR_STACK);
        EmiIngredient pestleCatalyst = EmiIngredient.of(Ingredient.of(ModTags.Items.PESTLES));

        for (RecipeHolder<MortarGrindingRecipe> recipeHolder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MORTAR_GRINDING.get())) {
            MortarGrindingRecipe recipe = recipeHolder.value();
            var emiOutputs = recipe.getResults().stream()
                    .map(chanceResult -> EmiStack.of(chanceResult.stack()).setChance(chanceResult.chance()))
                    .toList();

            registry.addRecipe(new MortarGrindingEmiRecipe(
                    recipeHolder.id(),
                    pestleCatalyst,
                    EmiIngredient.of(recipe.getIngredients().getFirst()),
                    emiOutputs
            ));
        }

        registry.addCategory(ManualLabourCategories.MORTAR_MIXING);
        registry.addWorkstation(ManualLabourCategories.MORTAR_MIXING, ManualLabourCategories.MORTAR_STACK);
        EmiIngredient ladleCatalyst = EmiIngredient.of(Ingredient.of(ModTags.Items.LADLES));

        for (RecipeHolder<MortarMixingRecipe> recipeHolder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MORTAR_MIXING.get())) {
            MortarMixingRecipe recipe = recipeHolder.value();
            var emiOutputs = recipe.getResults().stream()
                    .map(chanceResult -> EmiStack.of(chanceResult.stack()).setChance(chanceResult.chance()))
                    .toList();
            List<EmiIngredient> emiInputs = recipe.getIngredients().stream()
                    .<EmiIngredient>map(EmiIngredient::of)
                    .toList();
            Optional<EmiIngredient> emiFluidInput = recipe.getFluidInput().map(ManualLabourEmiPlugin::toEmiIngredient);

            registry.addRecipe(new MortarMixingEmiRecipe(
                    recipeHolder.id(),
                    ladleCatalyst,
                    emiInputs,
                    emiFluidInput,
                    emiOutputs
            ));
        }
    }
}
