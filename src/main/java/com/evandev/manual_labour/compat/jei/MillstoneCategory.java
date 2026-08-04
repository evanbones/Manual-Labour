package com.evandev.manual_labour.compat.jei;

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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MillstoneCategory extends CreateRecipeCategory<AbstractCrushingRecipe> {
    private final MillstoneAnimation millstone = new MillstoneAnimation();

    public MillstoneCategory(Info<AbstractCrushingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AbstractCrushingRecipe recipe, IFocusGroup focuses) {
        Ingredient input = recipe.getIngredients().getFirst();
        List<ProcessingOutput> outputs = recipe.getRollableResults();

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
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 72, 7);
        millstone.draw(graphics, getBackground().getWidth() / 2, 60);
    }
}
