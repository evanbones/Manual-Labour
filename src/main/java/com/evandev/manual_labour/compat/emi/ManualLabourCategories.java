package com.evandev.manual_labour.compat.emi;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModItems;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

public class ManualLabourCategories {
    public static final EmiStack WORKSTONE_STACK = EmiStack.of(ModItems.WORKSTONE_ITEM.get());

    public static final EmiRecipeCategory WORKSTONE = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "workstone"),
            WORKSTONE_STACK,
            WORKSTONE_STACK
    );
}
