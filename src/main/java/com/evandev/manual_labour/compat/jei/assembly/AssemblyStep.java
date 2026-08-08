package com.evandev.manual_labour.compat.jei.assembly;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public interface AssemblyStep {

    int width();

    default void setRecipe(IRecipeLayoutBuilder builder, IFocusGroup focuses, int x) {
    }

    void draw(GuiGraphics graphics, double mouseX, double mouseY, int index);

    Component description();

    default List<Ingredient> loopIngredients() {
        return List.of();
    }

    default List<SizedFluidIngredient> loopFluids() {
        return List.of();
    }
}
