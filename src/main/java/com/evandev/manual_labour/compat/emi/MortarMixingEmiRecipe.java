package com.evandev.manual_labour.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MortarMixingEmiRecipe implements EmiRecipe {
    private static final int SLOT = 18;

    private final ResourceLocation id;
    private final EmiIngredient tool;
    private final List<EmiIngredient> inputs;
    private final Optional<EmiIngredient> fluidInput;
    private final List<EmiStack> outputs;

    public MortarMixingEmiRecipe(ResourceLocation id, EmiIngredient tool, List<EmiIngredient> inputs,
                                  Optional<EmiIngredient> fluidInput, List<EmiStack> outputs) {
        this.id = id;
        this.tool = tool;
        this.inputs = inputs;
        this.fluidInput = fluidInput;
        this.outputs = outputs;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return ManualLabourCategories.MORTAR_MIXING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> all = new ArrayList<>(inputs);
        fluidInput.ifPresent(all::add);
        return all;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(tool);
    }

    private int inputRows() {
        return inputs.size() + (fluidInput.isPresent() ? 1 : 0);
    }

    @Override
    public int getDisplayWidth() {
        return SLOT * 4 + 12;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(SLOT * 2, inputRows() * SLOT) + 4;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(tool, 0, 0);

        int row = 1;
        for (EmiIngredient input : inputs) {
            widgets.addSlot(input, 0, row * SLOT);
            row++;
        }
        int fluidRow = row;
        fluidInput.ifPresent(fluid -> widgets.addSlot(fluid, 0, fluidRow * SLOT));

        int size = outputs.size();
        int gridX = SLOT * 2 + 8;
        for (int i = 0; i < size; i++) {
            int x = gridX + (i % 2) * SLOT;
            int y = (i / 2) * SLOT;
            widgets.addSlot(outputs.get(i), x, y).recipeContext(this);
        }
    }
}
