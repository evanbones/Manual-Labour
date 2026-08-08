package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.compat.jei.assembly.AssemblyStep;
import com.evandev.manual_labour.compat.jei.assembly.AssemblyView;
import com.evandev.manual_labour.compat.jei.assembly.AssemblyViews;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Layout ported from Create's {@code SequencedAssemblyCategory} (MIT, Copyright (c) simibubi).
 */
@ParametersAreNonnullByDefault
public class ManualAssemblyCategory extends ManualRecipeCategory<Recipe<?>> {

    private static final int STEP_MARGIN = 3;

    private final Map<Recipe<?>, AssemblyView> views = new IdentityHashMap<>();

    public ManualAssemblyCategory(Info<Recipe<?>> info) {
        super(info);
    }

    @Nullable
    private AssemblyView view(Recipe<?> recipe) {
        return views.computeIfAbsent(recipe, key -> AssemblyViews.describe(key).orElse(null));
    }

    private int rowWidth(AssemblyView view) {
        int width = 0;
        for (AssemblyStep step : view.steps()) {
            width += step.width() + STEP_MARGIN;
        }
        return width - STEP_MARGIN;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe<?> recipe, IFocusGroup focuses) {
        AssemblyView view = view(recipe);
        if (view == null) return;

        boolean noRandomOutput = view.outputChance() == 1;
        int xOffset = noRandomOutput ? 0 : -7;

        builder.addSlot(RecipeIngredientRole.INPUT, 27 + xOffset, 91)
                .setBackground(getRenderedSlot(), -1, -1)
                .addItemStacks(List.of(view.input().getItems()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 132 + xOffset, 91)
                .setBackground(getRenderedSlot(view.outputChance()), -1, -1)
                .addItemStack(view.result())
                .addRichTooltipCallback((slotView, tooltip) -> {
                    if (!noRandomOutput) tooltip.add(chanceComponent(view.outputChance()));
                });

        int x = rowWidth(view) / -2 + getBackground().getWidth() / 2;
        for (AssemblyStep step : view.steps()) {
            step.setRecipe(builder, focuses, x);
            x += step.width() + STEP_MARGIN;
        }

        for (int loop = 1; loop < view.loops(); loop++) {
            for (AssemblyStep step : view.steps()) {
                for (Ingredient ingredient : step.loopIngredients()) {
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addIngredients(ingredient);
                }
                for (SizedFluidIngredient fluid : step.loopFluids()) {
                    builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
                            .addIngredients(NeoForgeTypes.FLUID_STACK, Arrays.asList(fluid.getFluids()));
                }
            }
        }
    }

    @Override
    public void draw(Recipe<?> recipe, IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AssemblyView view = view(recipe);
        if (view == null) return;

        Font font = Minecraft.getInstance().font;
        PoseStack pose = graphics.pose();
        pose.pushPose();

        pose.pushPose();
        pose.translate(0, 15, 0);

        boolean singleOutput = view.outputChance() == 1;
        int xOffset = singleOutput ? 0 : -7;
        CategorySkin.get().drawLongArrow(graphics, 52 + xOffset, 79);

        if (!singleOutput) {
            CategorySkin.get().chanceSlot().draw(graphics, 150 + xOffset, 75);
            Component questionMark = Component.literal("?").withStyle(ChatFormatting.BOLD);
            graphics.drawString(font, questionMark, font.width(questionMark) / -2 + 8 + 150 + xOffset, 2 + 78, 0xEFEFEF);
        }

        if (view.loops() > 1) {
            pose.pushPose();
            pose.translate(15, 9, 0);
            CategorySkin.get().drawRepeatIcon(graphics, 50 + xOffset, 75);
            graphics.drawString(font, Component.literal("x" + view.loops()), 66 + xOffset, 80, 0x888888, false);
            pose.popPose();
        }

        pose.popPose();

        pose.translate((float) rowWidth(view) / -2 + (float) getBackground().getWidth() / 2, 0, 0);

        pose.pushPose();
        List<AssemblyStep> steps = view.steps();
        for (int i = 0; i < steps.size(); i++) {
            AssemblyStep step = steps.get(i);
            step.draw(graphics, mouseX, mouseY, i);
            pose.translate(step.width() + STEP_MARGIN, 0, 0);
        }
        pose.popPose();

        pose.popPose();
    }

    @Override
    public @NotNull List<Component> getTooltipStrings(Recipe<?> recipe, IRecipeSlotsView slotsView, double mouseX, double mouseY) {
        AssemblyView view = view(recipe);
        if (view == null) return List.of();

        List<Component> tooltip = new ArrayList<>();
        boolean singleOutput = view.outputChance() == 1;

        int xOffset = -7;
        int minX = 150 + xOffset;
        int minY = 90;
        if (!singleOutput && mouseX >= minX && mouseX < minX + 18 && mouseY >= minY && mouseY < minY + 18) {
            tooltip.add(Component.translatable("manual_labour.recipe.assembly.junk"));
            tooltip.add(chanceComponent(1 - view.outputChance()));
            return tooltip;
        }

        minX = 55 + xOffset;
        minY = 92;
        if (view.loops() > 1 && mouseX >= minX && mouseX < minX + 65 && mouseY >= minY && mouseY < minY + 24) {
            tooltip.add(Component.translatable("manual_labour.recipe.assembly.repeat", view.loops()));
            return tooltip;
        }

        if (mouseY > 5 && mouseY < 84) {
            double relativeX = mouseX + (double) rowWidth(view) / 2 + (double) getBackground().getWidth() / -2;
            List<AssemblyStep> steps = view.steps();
            for (int i = 0; i < steps.size(); i++) {
                AssemblyStep step = steps.get(i);
                if (relativeX >= 0 && relativeX < step.width()) {
                    tooltip.add(Component.translatable("manual_labour.recipe.assembly.step", i + 1));
                    tooltip.add(step.description().copy().withStyle(ChatFormatting.DARK_GREEN));
                    return tooltip;
                }
                relativeX -= step.width() + STEP_MARGIN;
            }
        }

        return tooltip;
    }

    private MutableComponent chanceComponent(float chance) {
        String number = chance < 0.01 ? "<1" : chance > 0.99 ? ">99" : String.valueOf(Math.round(chance * 100));
        return Component.translatable("manual_labour.recipe.processing.chance", number).withStyle(ChatFormatting.GOLD);
    }
}
