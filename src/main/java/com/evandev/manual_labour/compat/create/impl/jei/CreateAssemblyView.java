package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.compat.jei.assembly.AssemblyStep;
import com.evandev.manual_labour.compat.jei.assembly.AssemblyView;
import com.evandev.manual_labour.compat.jei.assembly.SimpleAssemblyView;
import com.evandev.manual_labour.compat.jei.assembly.WorkstoneAssemblyStep;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.*;

public final class CreateAssemblyView {

    private static final Map<ResourceLocation, SequencedAssemblySubCategory> SUB_CATEGORIES = new HashMap<>();

    private CreateAssemblyView() {
    }

    public static Optional<AssemblyView> describe(Recipe<?> recipe) {
        if (!(recipe instanceof SequencedAssemblyRecipe sequenced)) return Optional.empty();

        List<AssemblyStep> steps = new ArrayList<>();
        for (SequencedRecipe<?> step : sequenced.getSequence()) {
            steps.add(toStep(step));
        }

        return Optional.of(new SimpleAssemblyView(sequenced.getIngredient(), steps, sequenced.getLoops(),
                ManualRecipeCategory.getResultItem(sequenced), sequenced.getOutputChance()));
    }

    public static boolean isManual(SequencedAssemblyRecipe recipe) {
        for (SequencedRecipe<?> step : recipe.getSequence()) {
            if (isManualStep(step.getRecipe())) return true;
        }
        return false;
    }

    private static boolean isManualStep(Object stepRecipe) {
        if (stepRecipe instanceof WorkstoneRecipeLike) return true;
        if (ModConfig.get().useCreateDeployingRecipes && stepRecipe instanceof DeployerApplicationRecipe) return true;
        return ModConfig.get().useCreatePressingRecipes && stepRecipe instanceof PressingRecipe;
    }

    private static AssemblyStep toStep(SequencedRecipe<?> step) {
        ProcessingRecipe<?, ?> stepRecipe = step.getRecipe();

        if (stepRecipe instanceof WorkstoneRecipeLike workstone) {
            return WorkstoneAssemblyStep.of(workstone);
        }
        if (isManualStep(stepRecipe)) {
            NonNullList<Ingredient> ingredients = stepRecipe.getIngredients();
            Ingredient tool = ingredients.size() > 1 ? ingredients.get(1) : Ingredient.of(ModTags.Items.HAMMERS);
            return new WorkstoneAssemblyStep(tool, step.getAsAssemblyRecipe().getDescriptionForAssembly());
        }

        return new CreateStep(step, subCategory(step));
    }

    private static SequencedAssemblySubCategory subCategory(SequencedRecipe<?> step) {
        ResourceLocation key = RegisteredObjectsHelper.getKeyOrThrow(step.getRecipe().getSerializer());
        return SUB_CATEGORIES.computeIfAbsent(key, id -> step.getAsAssemblyRecipe().getJEISubCategory().get().get());
    }

    private record CreateStep(SequencedRecipe<?> recipe,
                              SequencedAssemblySubCategory subCategory) implements AssemblyStep {

        @Override
        public int width() {
            return subCategory.getWidth();
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, IFocusGroup focuses, int x) {
            subCategory.setRecipe(builder, recipe, focuses, x);
        }

        @Override
        public void draw(GuiGraphics graphics, double mouseX, double mouseY, int index) {
            subCategory.draw(recipe, graphics, mouseX, mouseY, index);
        }

        @Override
        public Component description() {
            return recipe.getAsAssemblyRecipe().getDescriptionForAssembly();
        }

        @Override
        public List<Ingredient> loopIngredients() {
            NonNullList<Ingredient> ingredients = recipe.getRecipe().getIngredients();
            return ingredients.size() < 2 ? List.of() : List.copyOf(ingredients.subList(1, ingredients.size()));
        }

        @Override
        public List<SizedFluidIngredient> loopFluids() {
            return recipe.getRecipe().getFluidIngredients();
        }
    }
}
