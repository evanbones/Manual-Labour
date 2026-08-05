package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.registry.ModBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.layout.LayoutHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class MortarMixingCategory extends CreateRecipeCategory<Recipe<?>> {

    public MortarMixingCategory(Info<Recipe<?>> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe<?> recipe, IFocusGroup focuses) {
        List<Ingredient> inputs;
        Optional<SizedFluidIngredient> fluidInput;
        List<ProcessingOutput> outputs;
        List<FluidStack> fluidOutputs = List.of();

        if (recipe instanceof MortarMixingRecipe mortarRecipe) {
            inputs = mortarRecipe.getIngredients();
            fluidInput = mortarRecipe.getFluidInput();
            outputs = mortarRecipe.getResults().stream()
                    .map(cr -> new ProcessingOutput(cr.stack(), cr.chance()))
                    .toList();
        } else if (recipe instanceof MixingRecipe mixingRecipe) {
            inputs = mixingRecipe.getIngredients();
            fluidInput = mixingRecipe.getFluidIngredients().stream().findFirst();
            outputs = mixingRecipe.getRollableResults();
            fluidOutputs = mixingRecipe.getFluidResults();
        } else {
            return;
        }

        int xOffset = getBackground().getWidth() / 2;
        int inputCount = inputs.size() + (fluidInput.isPresent() ? 1 : 0);
        LayoutHelper inputLayout = LayoutHelper.centeredHorizontal(inputCount, 1, 18, 18, 1);

        for (Ingredient ingredient : inputs) {
            builder.addSlot(RecipeIngredientRole.INPUT, xOffset + inputLayout.getX() + 1, 10 + inputLayout.getY() + 1)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addIngredients(ingredient);
            inputLayout.next();
        }
        fluidInput.ifPresent(sizedFluidIngredient -> addFluidSlot(builder, xOffset + inputLayout.getX() + 1, 10 + inputLayout.getY() + 1, sizedFluidIngredient));

        int yOffset = 86;
        int totalOutputs = outputs.size() + fluidOutputs.size();
        LayoutHelper outputLayout = LayoutHelper.centeredHorizontal(totalOutputs, 1, 18, 18, 1);
        for (ProcessingOutput output : outputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, xOffset + outputLayout.getX() + 1, yOffset + outputLayout.getY() + 1)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            outputLayout.next();
        }
        for (FluidStack fluidOutput : fluidOutputs) {
            addFluidSlot(builder, xOffset + outputLayout.getX() + 1, yOffset + outputLayout.getY() + 1, fluidOutput);
            outputLayout.next();
        }
    }

    @Override
    public void draw(Recipe<?> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 72, 30);
        JeiBlockIcon.draw(graphics, ModBlocks.MORTAR.get().defaultBlockState(), getBackground().getWidth() / 2 - 13, 66, 20);
    }
}
