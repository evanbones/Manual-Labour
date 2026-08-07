package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.WORKSTONE.get())
                .pattern("SS")
                .pattern("LL")
                .pattern("LL")
                .define('S', Blocks.SMOOTH_STONE_SLAB)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_smooth_stone", has(Blocks.SMOOTH_STONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FLINT_HAMMER.get())
                .pattern("XXX")
                .pattern("X|X")
                .pattern(" | ")
                .define('X', Items.FLINT)
                .define('|', Items.STICK)
                .unlockedBy("has_flint", has(Items.FLINT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_HAMMER.get())
                .pattern("XXX")
                .pattern("X|X")
                .pattern(" | ")
                .define('X', Tags.Items.INGOTS_IRON)
                .define('|', Items.STICK)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GOLDEN_HAMMER.get())
                .pattern("XXX")
                .pattern("X|X")
                .pattern(" | ")
                .define('X', Tags.Items.INGOTS_GOLD)
                .define('|', Items.STICK)
                .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.DIAMOND_HAMMER.get())
                .pattern("XXX")
                .pattern("X|X")
                .pattern(" | ")
                .define('X', Tags.Items.GEMS_DIAMOND)
                .define('|', Items.STICK)
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(output);

        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(ModItems.DIAMOND_HAMMER.get()),
                Ingredient.of(Items.NETHERITE_INGOT),
                RecipeCategory.TOOLS,
                ModItems.NETHERITE_HAMMER.get()
        ).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT)).save(output, "netherite_hammer_smithing");

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.MORTAR.get())
                .pattern("C C")
                .pattern("CCC")
                .pattern("L L")
                .define('C', Blocks.COBBLESTONE)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PESTLE.get())
                .pattern("L")
                .pattern("X")
                .pattern("X")
                .define('L', Items.LEATHER)
                .define('X', Blocks.COBBLESTONE)
                .unlockedBy("has_cobblestone", has(Blocks.COBBLESTONE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LADLE.get())
                .pattern("# ")
                .pattern("# ")
                .pattern("XH")
                .define('#', Items.STICK)
                .define('X', ItemTags.PLANKS)
                .define('H', Items.HONEYCOMB)
                .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
                .save(output);

        RecipeOutput createOnly = output.withConditions(new ModLoadedCondition(CreateCompat.CREATE));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CreateContent.MILLSTONE_ITEM.get())
                .pattern(" | ")
                .pattern("SSS")
                .pattern("BBB")
                .define('|', AllBlocks.SHAFT.get())
                .define('S', Tags.Items.STONES)
                .define('B', Blocks.STONE_BRICKS)
                .unlockedBy("has_shaft", has(AllBlocks.SHAFT.get()))
                .save(createOnly);

        WorkstoneRecipeProvider.buildRecipes(output);
        MortarRecipeProvider.buildRecipes(output);
    }
}
