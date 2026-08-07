package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.CategorySkin;
import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.layout.LayoutHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MillstoneCategory extends ManualRecipeCategory<AbstractCrushingRecipe> {
    private final MillstoneAnimation millstone = new MillstoneAnimation();

    public MillstoneCategory(Info<AbstractCrushingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AbstractCrushingRecipe recipe, IFocusGroup focuses) {
        Ingredient input = recipe.getIngredients().getFirst();
        List<ProcessingOutput> outputs = recipe.getRollableResults().stream()
                .map(result -> new ProcessingOutput(result.getStack(), result.getChance()))
                .toList();

        builder.addSlot(RecipeIngredientRole.INPUT, 51, 3)
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
    public void draw(AbstractCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        CategorySkin.get().drawDownArrow(graphics, 72, 7);
        millstone.draw(graphics, getBackground().getWidth() / 2, 60);
    }
}
