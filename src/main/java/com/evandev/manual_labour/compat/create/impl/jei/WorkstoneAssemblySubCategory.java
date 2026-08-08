package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.assembly.WorkstoneAssemblyStep;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.Ingredient;

public class WorkstoneAssemblySubCategory extends SequencedAssemblySubCategory {

    public WorkstoneAssemblySubCategory() {
        super(WorkstoneAssemblyStep.WIDTH);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SequencedRecipe<?> recipe, IFocusGroup focuses, int x) {
        var ingredients = recipe.getRecipe().getIngredients();
        Ingredient ingredient = ingredients.size() > 1
                ? ingredients.get(1)
                : Ingredient.of(ModTags.Items.HAMMERS);
        builder.addSlot(RecipeIngredientRole.INPUT, x + 4, 15)
                .setBackground(CreateRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(ingredient);
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        WorkstoneAssemblyStep.drawWorkstone(graphics);
    }
}
