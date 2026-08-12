package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.registry.ModTags;
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

        Component toolName;
        if (tools.length > 1 && isHammerIngredient(tools)) {
            toolName = Component.translatable("manual_labour.ingredient.hammer");
        } else {
            toolName = tools[0].getHoverName();
        }

        String key = switch (step.toolUse()) {
            case CONSUME -> "recipe.assembly.manual_labour.workstone.consume";
            case KEEP -> "recipe.assembly.manual_labour.workstone.keep";
            case DAMAGE -> "recipe.assembly.manual_labour.workstone";
        };
        return Component.translatable(key, toolName);
    }

    private static boolean isHammerIngredient(ItemStack[] tools) {
        for (ItemStack stack : tools) {
            if (stack.is(ModTags.Items.HAMMERS) || stack.is(ModTags.Items.TOOLS_HAMMER)) {
                return true;
            }
        }
        return false;
    }
}
