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

    public static final EmiStack MORTAR_STACK = EmiStack.of(ModItems.MORTAR_ITEM.get());

    public static final EmiRecipeCategory MORTAR_GRINDING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar_grinding"),
            EmiStack.of(ModItems.PESTLE.get()),
            MORTAR_STACK
    );

    public static final EmiRecipeCategory MORTAR_MIXING = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mortar_mixing"),
            EmiStack.of(ModItems.LADLE.get()),
            MORTAR_STACK
    );
}
