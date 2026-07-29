package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class ManualLabourJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "jei_plugin");

    private CreateRecipeCategory<WorkstoneRecipe> workstone;
    private CreateRecipeCategory<Recipe<?>> mortarGrinding;
    private CreateRecipeCategory<Recipe<?>> mortarMixing;
    private CreateRecipeCategory<SequencedAssemblyRecipe> manualAssembly;
    private CreateRecipeCategory<AbstractCrushingRecipe> millstone;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    private static RecipeManager recipeManager() {
        Level level = Minecraft.getInstance().level;
        return level == null ? null : level.getRecipeManager();
    }

    private static List<RecipeHolder<Recipe<?>>> gatherMortarGrindingRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<Recipe<?>>> recipes = new ArrayList<>();
        manager.getAllRecipesFor(ModRecipeTypes.MORTAR_GRINDING.get())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        manager.getAllRecipesFor(AllRecipeTypes.MILLING.<RecipeInput, MillingRecipe>getType())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        manager.getAllRecipesFor(AllRecipeTypes.CRUSHING.<RecipeInput, CrushingRecipe>getType())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        return recipes;
    }

    private static List<RecipeHolder<Recipe<?>>> gatherMortarMixingRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<Recipe<?>>> recipes = new ArrayList<>();
        manager.getAllRecipesFor(ModRecipeTypes.MORTAR_MIXING.get())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        manager.getAllRecipesFor(AllRecipeTypes.MIXING.<RecipeInput, MixingRecipe>getType())
                .forEach(r -> {
                    MixingRecipe recipe = r.value();
                    if (!recipe.getRequiredHeat().testBlazeBurner(HeatLevel.NONE)) return;
                    if (recipe.getFluidIngredients().size() > 1) return;
                    recipes.add(new RecipeHolder<>(r.id(), recipe));
                });
        return recipes;
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        workstone = new CreateRecipeCategory.Builder<>(WorkstoneRecipe.class)
                .addTypedRecipes(ModRecipeTypes.WORKSTONE_INFO)
                .catalyst(ModBlocks.WORKSTONE::get)
                .itemIcon(ModBlocks.WORKSTONE.get())
                .emptyBackground(177, 70)
                .build(id("workstone"), WorkstoneCategory::new);

        mortarGrinding = new MortarGrindingCategory(new CreateRecipeCategory.Info<>(
                RecipeType.createRecipeHolderType(id("mortar_grinding")),
                Component.translatable("manual_labour.recipe.mortar_grinding"),
                new EmptyBackground(177, 100),
                new DoubleItemIcon(() -> new ItemStack(ModItems.PESTLE.get()), () -> new ItemStack(ModItems.MORTAR_ITEM.get())),
                ManualLabourJeiPlugin::gatherMortarGrindingRecipes,
                List.of(() -> new ItemStack(ModItems.PESTLE.get()), () -> new ItemStack(ModBlocks.MORTAR.get()))
        ));

        mortarMixing = new MortarMixingCategory(new CreateRecipeCategory.Info<>(
                RecipeType.createRecipeHolderType(id("mortar_mixing")),
                Component.translatable("manual_labour.recipe.mortar_mixing"),
                new EmptyBackground(177, 100),
                new DoubleItemIcon(() -> new ItemStack(ModItems.LADLE.get()), () -> new ItemStack(ModItems.MORTAR_ITEM.get())),
                ManualLabourJeiPlugin::gatherMortarMixingRecipes,
                List.of(() -> new ItemStack(ModItems.LADLE.get()), () -> new ItemStack(ModBlocks.MORTAR.get()))
        ));

        manualAssembly = new CreateRecipeCategory.Builder<>(SequencedAssemblyRecipe.class)
                .addTypedRecipes(AllRecipeTypes.SEQUENCED_ASSEMBLY)
                .itemIcon(ModBlocks.WORKSTONE.get())
                .catalyst(ModBlocks.WORKSTONE::get)
                .emptyBackground(180, 115)
                .build(id("manual_assembly"), ManualAssemblyCategory::new);

        millstone = new CreateRecipeCategory.Builder<>(AbstractCrushingRecipe.class)
                .addTypedRecipes(AllRecipeTypes.MILLING)
                .itemIcon(ModBlocks.MILLSTONE.get())
                .catalyst(ModBlocks.MILLSTONE::get)
                .emptyBackground(177, 100)
                .build(id("millstone"), MillstoneCategory::new);

        registration.addRecipeCategories(workstone, mortarGrinding, mortarMixing, manualAssembly, millstone);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        workstone.registerRecipes(registration);
        mortarGrinding.registerRecipes(registration);
        mortarMixing.registerRecipes(registration);
        manualAssembly.registerRecipes(registration);
        millstone.registerRecipes(registration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        workstone.registerCatalysts(registration);
        mortarGrinding.registerCatalysts(registration);
        mortarMixing.registerCatalysts(registration);
        manualAssembly.registerCatalysts(registration);
        millstone.registerCatalysts(registration);
    }
}
