package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.CategorySkin;
import com.evandev.manual_labour.compat.jei.JeiBlockIcon;
import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ManualPressingCategory extends ManualRecipeCategory<PressingRecipe> {

    public ManualPressingCategory(Info<PressingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressingRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 43, 8)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(Ingredient.of(ModTags.Items.HAMMERS));
        builder
                .addSlot(RecipeIngredientRole.INPUT, 43, 31)
                .addIngredients(recipe.getIngredients().getFirst());

        List<com.simibubi.create.content.processing.recipe.ProcessingOutput> results = recipe.getRollableResults();
        boolean single = results.size() == 1;
        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            ProcessingOutput output = new ProcessingOutput(result.getStack(), result.getChance());
            int xOffset = (single ? 9 : 0) + (i % 2 == 0 ? 0 : 19);
            int yOffset = (i / 2) * -19;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 77 + xOffset, 47 + yOffset)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
        }
    }

    @Override
    public void draw(PressingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        CategorySkin skin = CategorySkin.get();
        skin.drawShadow(graphics, 25, 54);
        skin.drawDownArrow(graphics, recipe.getRollableResults().size() > 1 ? 73 : 80, 22 + (recipe.getRollableResults().size() > 2 ? -19 : 0));
        JeiBlockIcon.draw(graphics, ModBlocks.WORKSTONE.get().defaultBlockState(), 38, 58, 20);
    }
}
