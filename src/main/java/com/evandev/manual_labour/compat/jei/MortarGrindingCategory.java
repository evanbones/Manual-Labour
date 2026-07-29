package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.registry.ModBlocks;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MortarGrindingCategory extends CreateRecipeCategory<Recipe<?>> {

    public MortarGrindingCategory(Info<Recipe<?>> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe<?> recipe, IFocusGroup focuses) {
        Ingredient input;
        List<ProcessingOutput> outputs;

        if (recipe instanceof MortarGrindingRecipe mortarRecipe) {
            input = mortarRecipe.getIngredients().getFirst();
            outputs = mortarRecipe.getResults().stream()
                    .map(cr -> new ProcessingOutput(cr.stack(), cr.chance()))
                    .toList();
        } else if (recipe instanceof AbstractCrushingRecipe crushingRecipe) {
            input = crushingRecipe.getIngredients().getFirst();
            outputs = crushingRecipe.getRollableResults();
        } else {
            return;
        }

        builder
                .addSlot(RecipeIngredientRole.INPUT, 51, 3)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(input);

        int xOffset = getBackground().getWidth() / 2;
        int yOffset = 86;
        LayoutHelper layout = LayoutHelper.centeredHorizontal(outputs.size(), 1, 18, 18, 1);
        for (ProcessingOutput output : outputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, xOffset + layout.getX() + 1, yOffset + layout.getY() + 1)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            layout.next();
        }
    }

    @Override
    public void draw(Recipe<?> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 72, 7);
        JeiBlockIcon.draw(graphics, ModBlocks.MORTAR.get().defaultBlockState(), 62, 45, 20);
    }
}
