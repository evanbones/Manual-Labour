package com.evandev.manual_labour.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ManualAssemblyProcess(ManualAssemblyRecipe recipe, ResourceLocation id, ItemStack input,
                                    WorkstoneRecipeLike step) implements WorkstoneProcess {

    @Override
    public List<ItemStack> rollResults(RandomSource random) {
        ItemStack advanced = recipe.advance(id, input, random);
        return advanced.isEmpty() ? List.of() : List.of(advanced);
    }

    @Override
    public ToolUse toolUse() {
        return step.toolUse();
    }
}
