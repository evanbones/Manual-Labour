package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.ManualAssemblyRecipe;
import com.evandev.manual_labour.recipe.ManualAssemblyState;
import com.evandev.manual_labour.recipe.WorkstoneStepDescription;
import com.evandev.manual_labour.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Optional;

/**
 * Mirrors Create's {@code SequencedAssemblyRecipe#addToTooltip} (MIT, Copyright (c) simibubi) for
 * assembly recipes.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ManualAssemblyTooltip {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ManualAssemblyState state = event.getItemStack().get(ModDataComponents.MANUAL_ASSEMBLY.get());
        if (state == null) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Optional<RecipeHolder<?>> holder = level.getRecipeManager().byKey(state.id());
        if (holder.isEmpty() || !(holder.get().value() instanceof ManualAssemblyRecipe recipe)) return;

        List<Component> tooltip = event.getToolTip();
        int length = recipe.getSequence().size();
        int total = length * recipe.getLoops();
        int step = Math.min(state.step(), total);

        tooltip.add(CommonComponents.EMPTY);
        tooltip.add(Component.translatable("manual_labour.recipe.assembly").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("manual_labour.recipe.assembly.progress", step, total)
                .withStyle(ChatFormatting.DARK_GRAY));

        int remaining = total - step;
        for (int i = 0; i < length && i < remaining; i++) {
            Component description = WorkstoneStepDescription.of(recipe.getSequence().get((i + step) % length));
            if (i == 0) {
                tooltip.add(Component.translatable("manual_labour.recipe.assembly.next", description)
                        .withStyle(ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.literal("-> ").append(description).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }
}
