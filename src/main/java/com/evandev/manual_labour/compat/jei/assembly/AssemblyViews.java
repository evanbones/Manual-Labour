package com.evandev.manual_labour.compat.jei.assembly;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.compat.create.impl.jei.CreateAssemblyView;
import com.evandev.manual_labour.recipe.ManualAssemblyRecipe;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

public final class AssemblyViews {

    private AssemblyViews() {
    }

    public static Optional<AssemblyView> describe(Recipe<?> recipe) {
        if (recipe instanceof ManualAssemblyRecipe own) {
            return Optional.of(SimpleAssemblyView.of(own));
        }
        if (CreateCompat.isLoaded()) {
            return CreateAssemblyView.describe(recipe);
        }
        return Optional.empty();
    }
}
