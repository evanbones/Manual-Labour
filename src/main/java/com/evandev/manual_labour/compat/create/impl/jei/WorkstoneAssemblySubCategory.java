package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.JeiBlockIcon;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
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
        super(25);
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
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(-7, 50, 0);
        ms.scale(.75f, .75f, .75f);

        JeiBlockIcon.draw(graphics, ModBlocks.WORKSTONE.get().defaultBlockState(), getWidth() / 2, 0, 20);

        ms.popPose();
    }
}
