package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class WorkstoneCategory extends ManualRecipeCategory<WorkstoneRecipeLike> {

    private static final int OUTPUT_GRID_X = 76;
    private static final int OUTPUT_GRID_Y = 10;

    private final IDrawable slot;
    private final IDrawable chanceSlot;

    public WorkstoneCategory(Info<WorkstoneRecipeLike> info, IDrawable slot, IDrawable chanceSlot) {
        super(info);
        this.slot = slot;
        this.chanceSlot = chanceSlot;
    }

    private IDrawable getSlot(ProcessingOutput output) {
        return output.getChance() == 1 ? slot : chanceSlot;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkstoneRecipeLike recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 8)
                .addIngredients(recipe.getToolIngredient());
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 27)
                .addIngredients(recipe.getInputIngredient());

        List<ProcessingOutput> outputs = recipe.getOutputs();
        int size = outputs.size();
        int centerX = size > 1 ? 1 : 10;
        int centerY = size > 2 ? 1 : 10;

        for (int i = 0; i < size; i++) {
            ProcessingOutput output = outputs.get(i);
            int xOffset = centerX + (i % 2 == 0 ? 0 : 19);
            int yOffset = centerY + ((i / 2) * 19);

            builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_GRID_X + xOffset, OUTPUT_GRID_Y + yOffset)
                    .setBackground(getSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(WorkstoneRecipeLike recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
    }
}
