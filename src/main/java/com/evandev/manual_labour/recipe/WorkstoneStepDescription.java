package com.evandev.manual_labour.recipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class WorkstoneStepDescription {

    private WorkstoneStepDescription() {
    }

    public static Component of(WorkstoneRecipeLike step) {
        if (step.getIngredients().size() < 2) {
            return Component.translatable("recipe.assembly.manual_labour.workstone.invalid");
        }

        ItemStack[] tools = step.getToolIngredient().getItems();
        if (tools.length == 0) {
            return Component.translatable("recipe.assembly.manual_labour.workstone.invalid");
        }

        return Component.translatable("recipe.assembly.manual_labour.workstone", tools[0].getHoverName());
    }
}
