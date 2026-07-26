package com.evandev.manual_labour.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MortarGrindingEmiRecipe implements EmiRecipe {
    private static final int SLOT = 18;

    private final ResourceLocation id;
    private final EmiIngredient tool;
    private final EmiIngredient input;
    private final List<EmiStack> outputs;

    public MortarGrindingEmiRecipe(ResourceLocation id, EmiIngredient tool, EmiIngredient input, List<EmiStack> outputs) {
        this.id = id;
        this.tool = tool;
        this.input = input;
        this.outputs = outputs;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ManualLabourCategories.MORTAR_GRINDING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(tool);
    }

    @Override
    public int getDisplayWidth() {
        return SLOT * 4 + 12;
    }

    @Override
    public int getDisplayHeight() {
        return SLOT * 2 + 4;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(tool, 0, 0);
        widgets.addSlot(input, 0, SLOT + 2);

        int size = outputs.size();
        int gridX = SLOT * 2 + 8;
        for (int i = 0; i < size; i++) {
            int x = gridX + (i % 2) * SLOT;
            int y = (i / 2) * SLOT;
            widgets.addSlot(outputs.get(i), x, y).recipeContext(this);
        }
    }
}
