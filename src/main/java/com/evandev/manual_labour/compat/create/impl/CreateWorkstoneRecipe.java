package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.compat.create.impl.jei.WorkstoneAssemblySubCategory;
import com.evandev.manual_labour.foundation.recipe.ProcessingOutput;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.recipe.WorkstoneStepDescription;
import com.evandev.manual_labour.registry.ModBlocks;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class CreateWorkstoneRecipe extends StandardProcessingRecipe<RecipeWrapper>
        implements IAssemblyRecipe, WorkstoneRecipeLike {

    public CreateWorkstoneRecipe(ProcessingRecipeParams params) {
        super(WorkstoneRecipeTypeInfo.INSTANCE, params);
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

    @Override
    public Ingredient getInputIngredient() {
        return ingredients.getFirst();
    }

    @Override
    public Ingredient getToolIngredient() {
        return ingredients.get(1);
    }

    @Override
    public List<ProcessingOutput> getOutputs() {
        List<ProcessingOutput> outputs = new ArrayList<>(results.size());
        for (com.simibubi.create.content.processing.recipe.ProcessingOutput result : results) {
            outputs.add(new ProcessingOutput(result.getStack(), result.getChance()));
        }
        return outputs;
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(getToolIngredient());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Component getDescriptionForAssembly() {
        return WorkstoneStepDescription.of(this);
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(ModBlocks.WORKSTONE.get());
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> WorkstoneAssemblySubCategory::new;
    }

    public static class Serializer extends StandardProcessingRecipe.Serializer<CreateWorkstoneRecipe> {
        public Serializer() {
            super(CreateWorkstoneRecipe::new);
        }
    }
}
