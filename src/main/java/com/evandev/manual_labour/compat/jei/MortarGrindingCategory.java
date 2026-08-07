package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.registry.ModBlocks;
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
public class MortarGrindingCategory extends ManualRecipeCategory<Recipe<?>> {

    public MortarGrindingCategory(Info<Recipe<?>> info) {
        super(info);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe<?> recipe, IFocusGroup focuses) {
        MortarProcess process;
        if (recipe instanceof MortarGrindingRecipe mortarRecipe) {
            process = new MortarProcess.OwnGrindingProcess(mortarRecipe);
        } else {
            process = CreateCompat.get().describeMortarRecipe(recipe).orElse(null);
            if (process == null) return;
        }

        List<Ingredient> inputs = process.ingredients();
        if (inputs.isEmpty()) return;

        builder
                .addSlot(RecipeIngredientRole.INPUT, 51, 3)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredients(inputs.getFirst());

        List<ProcessingOutput> outputs = process.displayOutputs();
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
        CategorySkin.get().drawDownArrow(graphics, 72, 7);
        JeiBlockIcon.draw(graphics, ModBlocks.MORTAR.get().defaultBlockState(), getBackground().getWidth() / 2 - 13, 55, 20);
    }
}
