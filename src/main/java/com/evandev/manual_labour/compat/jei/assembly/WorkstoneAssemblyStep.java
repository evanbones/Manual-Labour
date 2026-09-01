package com.evandev.manual_labour.compat.jei.assembly;

import com.evandev.manual_labour.compat.jei.JeiBlockIcon;
import com.evandev.manual_labour.compat.jei.ManualRecipeCategory;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.recipe.WorkstoneStepDescription;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record WorkstoneAssemblyStep(Ingredient tool, Component description) implements AssemblyStep {

    public static final int WIDTH = 25;

    public static WorkstoneAssemblyStep of(WorkstoneRecipeLike step) {
        Ingredient tool = step.getIngredients().size() > 1 ? step.getToolIngredient() : Ingredient.of(ModTags.Items.HAMMERS);
        return new WorkstoneAssemblyStep(tool, WorkstoneStepDescription.of(step));
    }

    public static void drawWorkstone(GuiGraphics graphics) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(-7, 50, 0);
        pose.scale(.75f, .75f, .75f);
        JeiBlockIcon.draw(graphics, ModBlocks.WORKSTONE.get().defaultBlockState(), WIDTH / 2, 0, 20);
        pose.popPose();
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IFocusGroup focuses, int x) {
        builder.addSlot(RecipeIngredientRole.INPUT, x + 4, 15)
                .setBackground(ManualRecipeCategory.getRenderedSlot(), -1, -1)
                .addIngredients(tool);
    }

    @Override
    public void draw(GuiGraphics graphics, double mouseX, double mouseY, int index) {
        drawWorkstone(graphics);
    }

    @Override
    public List<Ingredient> loopIngredients() {
        return tool.isEmpty() ? List.of() : List.of(tool);
    }
}
