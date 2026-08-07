package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import com.evandev.manual_labour.config.ModConfig;

@ParametersAreNonnullByDefault
public class ManualPressingCategory extends CreateRecipeCategory<PressingRecipe> {

    public ManualPressingCategory(Info<PressingRecipe> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PressingRecipe recipe, IFocusGroup focuses) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 27, 51)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(recipe.getIngredients().getFirst());
        builder
                .addSlot(RecipeIngredientRole.INPUT, 51, 5)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(Ingredient.of(ModTags.Items.HAMMERS));

        List<ProcessingOutput> results = recipe.getRollableResults();
        boolean single = results.size() == 1;
        float yieldMultiplier = ModConfig.get().workstonePressingYield;
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            ProcessingOutput adjustedOutput = new ProcessingOutput(output.getStack(), output.getChance() * yieldMultiplier);
            int xOffset = i % 2 == 0 ? 0 : 19;
            int yOffset = (i / 2) * -19;
            builder.addSlot(RecipeIngredientRole.OUTPUT, single ? 132 : 132 + xOffset, 51 + yOffset)
                    .setBackground(getRenderedSlot(adjustedOutput), -1, -1)
                    .addItemStack(adjustedOutput.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(adjustedOutput));
        }
    }

    @Override
    public void draw(PressingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29 + (recipe.getRollableResults().size() > 2 ? -19 : 0));
        JeiBlockIcon.draw(graphics, ModBlocks.WORKSTONE.get().defaultBlockState(), getBackground().getWidth() / 2 - 13, 56, 20);
    }
}
