package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.compat.create.impl.jei.CreateJeiCategories;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.WorkstoneRecipeLike;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
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
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@JeiPlugin
public class ManualLabourJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "jei_plugin");

    private final List<ManualRecipeCategory<?>> categories = new ArrayList<>();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    public static <T extends Recipe<?>> ManualRecipeCategory.Info<T> info(
            ResourceLocation id, IDrawable background, IDrawable icon,
            Supplier<List<RecipeHolder<T>>> recipes, List<Supplier<? extends ItemStack>> catalysts) {
        return new ManualRecipeCategory.Info<>(
                RecipeType.createRecipeHolderType(id),
                Component.translatable(id.getNamespace() + ".recipe." + id.getPath()),
                background,
                icon,
                recipes,
                catalysts
        );
    }

    @Nullable
    public static RecipeManager recipeManager() {
        Level level = Minecraft.getInstance().level;
        return level == null ? null : level.getRecipeManager();
    }

    private static List<RecipeHolder<WorkstoneRecipeLike>> gatherWorkstoneRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) return List.of();
        return new ArrayList<>(manager.getAllRecipesFor(ModRecipeTypes.WORKSTONE.get()));
    }

    private static List<RecipeHolder<Recipe<?>>> gatherMortarGrindingRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<Recipe<?>>> recipes = new ArrayList<>();
        manager.getAllRecipesFor(ModRecipeTypes.MORTAR_GRINDING.get())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        CreateCompat.get().addMortarGrindingRecipes(manager, recipes);
        return recipes;
    }

    private static List<RecipeHolder<Recipe<?>>> gatherMortarMixingRecipes() {
        RecipeManager manager = recipeManager();
        if (manager == null) return List.of();

        List<RecipeHolder<Recipe<?>>> recipes = new ArrayList<>();
        manager.getAllRecipesFor(ModRecipeTypes.MORTAR_MIXING.get())
                .forEach(r -> recipes.add(new RecipeHolder<>(r.id(), r.value())));
        CreateCompat.get().addMortarMixingRecipes(manager, recipes);
        return recipes;
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        CategorySkin.init(guiHelper);

        if (ModConfig.get().enableWorkstoneJei) {
            ResourceLocation workstoneTexture = id("textures/gui/jei/workstone.png");
            IDrawable workstoneBackground = guiHelper.createDrawable(workstoneTexture, 0, 0, 117, 57);
            IDrawable workstoneSlot = guiHelper.createDrawable(workstoneTexture, 0, 58, 18, 18);
            IDrawable workstoneChanceSlot = guiHelper.createDrawable(workstoneTexture, 18, 58, 18, 18);

            categories.add(new WorkstoneCategory(info(
                    id("workstone"),
                    workstoneBackground,
                    new ItemIcon(() -> new ItemStack(ModBlocks.WORKSTONE.get())),
                    ManualLabourJeiPlugin::gatherWorkstoneRecipes,
                    List.of(() -> new ItemStack(ModBlocks.WORKSTONE.get()))
            ), workstoneSlot, workstoneChanceSlot));
        }

        if (ModConfig.get().enableMortarGrindingJei) {
            categories.add(new MortarGrindingCategory(info(
                    id("mortar_grinding"),
                    new EmptyBackground(177, 100),
                    new DoubleItemIcon(() -> new ItemStack(ModItems.PESTLE.get()), () -> new ItemStack(ModItems.MORTAR_ITEM.get())),
                    ManualLabourJeiPlugin::gatherMortarGrindingRecipes,
                    List.of(() -> new ItemStack(ModItems.PESTLE.get()), () -> new ItemStack(ModBlocks.MORTAR.get()))
            )));
        }

        if (ModConfig.get().enableMortarMixingJei) {
            categories.add(new MortarMixingCategory(info(
                    id("mortar_mixing"),
                    new EmptyBackground(177, 100),
                    new DoubleItemIcon(() -> new ItemStack(ModItems.LADLE.get()), () -> new ItemStack(ModItems.MORTAR_ITEM.get())),
                    ManualLabourJeiPlugin::gatherMortarMixingRecipes,
                    List.of(() -> new ItemStack(ModItems.LADLE.get()), () -> new ItemStack(ModBlocks.MORTAR.get()))
            )));
        }

        if (CreateCompat.isLoaded()) {
            CreateJeiCategories.addCategories(categories::add);
        }

        categories.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        categories.forEach(category -> category.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        categories.forEach(category -> category.registerCatalysts(registration));
    }
}
