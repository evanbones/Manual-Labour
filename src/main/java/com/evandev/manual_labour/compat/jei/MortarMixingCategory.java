package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.content.block.MortarHeat;
import com.evandev.manual_labour.foundation.item.ItemHelper;
import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.layout.LayoutHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MortarMixingCategory extends ManualRecipeCategory<Recipe<?>> {

    private static final int HEAT_BAR_X = 4;
    private static final int HEAT_BAR_Y = 106;

    public MortarMixingCategory(Info<Recipe<?>> info) {
        super(info);
    }

    @Nullable
    private static MortarProcess process(Recipe<?> recipe) {
        if (recipe instanceof MortarMixingRecipe mortarRecipe) {
            return new MortarProcess.OwnMixingProcess(mortarRecipe);
        }
        return CreateCompat.get().describeMortarRecipe(recipe).orElse(null);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe<?> recipe, IFocusGroup focuses) {
        MortarProcess process = process(recipe);
        if (process == null) return;

        List<Pair<Ingredient, MutableInt>> inputs = ItemHelper.condenseIngredients(process.ingredients());
        List<SizedFluidIngredient> fluidInputs = process.fluidIngredients();
        List<ProcessingOutput> outputs = process.displayOutputs();
        List<FluidStack> fluidOutputs = process.fluidResults();

        int xOffset = getBackground().getWidth() / 2;
        LayoutHelper inputLayout = LayoutHelper.centeredHorizontal(inputs.size() + fluidInputs.size(), 1, 18, 18, 1);

        for (Pair<Ingredient, MutableInt> ingredient : inputs) {
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack itemStack : ingredient.getFirst().getItems()) {
                ItemStack copy = itemStack.copy();
                copy.setCount(ingredient.getSecond().getValue());
                stacks.add(copy);
            }

            builder.addSlot(RecipeIngredientRole.INPUT, xOffset + inputLayout.getX() + 1, 10 + inputLayout.getY() + 1)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStacks(stacks);
            inputLayout.next();
        }
        for (SizedFluidIngredient fluidInput : fluidInputs) {
            addFluidSlot(builder, xOffset + inputLayout.getX() + 1, 10 + inputLayout.getY() + 1, fluidInput);
            inputLayout.next();
        }

        int yOffset = 86;
        LayoutHelper outputLayout = LayoutHelper.centeredHorizontal(outputs.size() + fluidOutputs.size(), 1, 18, 18, 1);
        for (ProcessingOutput output : outputs) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, xOffset + outputLayout.getX() + 1, yOffset + outputLayout.getY() + 1)
                    .setBackground(getRenderedSlot(output), -1, -1)
                    .addItemStack(output.getStack())
                    .addRichTooltipCallback(addStochasticTooltip(output));
            outputLayout.next();
        }
        for (FluidStack fluidOutput : fluidOutputs) {
            addFluidSlot(builder, xOffset + outputLayout.getX() + 1, yOffset + outputLayout.getY() + 1, fluidOutput);
            outputLayout.next();
        }

        if (process.heatRequirement() != HeatCondition.NONE) {
            builder.addSlot(RecipeIngredientRole.CATALYST, HEAT_BAR_X + 148, HEAT_BAR_Y + 1)
                    .setBackground(getRenderedSlot(), -1, -1)
                    .addItemStacks(MortarHeat.sourceItems());
        }
    }

    @Override
    public void draw(Recipe<?> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        CategorySkin.get().drawDownArrow(graphics, 72, 30);
        JeiBlockIcon.draw(graphics, ModBlocks.MORTAR.get().defaultBlockState(), getBackground().getWidth() / 2 - 13, 66, 20);

        MortarProcess process = process(recipe);
        HeatCondition heat = process == null ? HeatCondition.NONE : process.heatRequirement();

        CategorySkin.get().drawHeatBar(graphics, HEAT_BAR_X, HEAT_BAR_Y, heat != HeatCondition.NONE);
        graphics.drawString(Minecraft.getInstance().font, Component.translatable(heat.getTranslationKey()),
                HEAT_BAR_X + 5, HEAT_BAR_Y + 6, heat.getColor(), false);
    }
}
