package com.evandev.manual_labour.recipe;

import com.evandev.manual_labour.compat.jei.WorkstoneAssemblySubCategory;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class WorkstoneRecipe extends StandardProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

    public WorkstoneRecipe(ProcessingRecipeParams params) {
        super(ModRecipeTypes.WORKSTONE_INFO, params);
    }

    @Override
    public boolean matches(@NotNull RecipeWrapper inv, @NotNull Level level) {
        if (ingredients.size() < 2) return false;
        return getInputIngredient().test(inv.getItem(0)) && getToolIngredient().test(inv.getItem(1));
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    public Ingredient getInputIngredient() {
        return ingredients.getFirst();
    }

    public Ingredient getToolIngredient() {
        return ingredients.get(1);
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(getToolIngredient());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = getToolIngredient().getItems();
        if (matchingStacks.length == 0) {
            return Component.literal("Invalid");
        }
        return Component.translatable("recipe.assembly.manual_labour.workstone",
                Component.translatable(matchingStacks[0].getDescriptionId()).getString());
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(ModBlocks.WORKSTONE.get());
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> WorkstoneAssemblySubCategory::new;
    }

    public static class Serializer extends StandardProcessingRecipe.Serializer<WorkstoneRecipe> {
        public Serializer() {
            super(WorkstoneRecipe::new);
        }
    }
}
