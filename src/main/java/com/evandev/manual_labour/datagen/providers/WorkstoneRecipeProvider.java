package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class WorkstoneRecipeProvider {

    public static void buildRecipes(RecipeOutput output) {
        hammer(output, "cracked_stone_bricks", Items.STONE_BRICKS, Items.CRACKED_STONE_BRICKS, 1);
        hammer(output, "cracked_deepslate_bricks", Items.DEEPSLATE_BRICKS, Items.CRACKED_DEEPSLATE_BRICKS, 1);
        hammer(output, "cracked_deepslate_tiles", Items.DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES, 1);
        hammer(output, "cracked_nether_bricks", Items.NETHER_BRICKS, Items.CRACKED_NETHER_BRICKS, 1);
        hammer(output, "cracked_polished_blackstone_bricks", Items.POLISHED_BLACKSTONE_BRICKS, Items.CRACKED_POLISHED_BLACKSTONE_BRICKS, 1);

        hammer(output, "cobblestone_to_gravel", Items.COBBLESTONE, Items.GRAVEL, 1);
        hammer(output, "gravel_to_sand", Items.GRAVEL, List.of(
                new ProcessingOutput(new ItemStack(Items.SAND), 1.0F),
                new ProcessingOutput(new ItemStack(Items.FLINT), 0.25F)
        ));
        hammer(output, "cobbled_deepslate_to_gravel", Items.COBBLED_DEEPSLATE, List.of(
                new ProcessingOutput(new ItemStack(Items.GRAVEL), 1.0F),
                new ProcessingOutput(new ItemStack(Items.FLINT), 0.30F)
        ));
        hammer(output, "tuff_to_gravel", Items.TUFF, List.of(
                new ProcessingOutput(new ItemStack(Items.GRAVEL), 1.0F),
                new ProcessingOutput(new ItemStack(Items.FLINT), 0.20F)
        ));
        hammer(output, "sandstone_to_sand", Ingredient.of(Items.SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE), Items.SAND, 4);
        hammer(output, "red_sandstone_to_red_sand", Ingredient.of(Items.RED_SANDSTONE, Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE), Items.RED_SAND, 4);
        hammer(output, "granite_to_cobblestone", Items.GRANITE, List.of(
                new ProcessingOutput(new ItemStack(Items.COBBLESTONE), 1.0F),
                new ProcessingOutput(new ItemStack(Items.QUARTZ), 0.15F)
        ));
        hammer(output, "diorite_to_cobblestone", Items.DIORITE, List.of(
                new ProcessingOutput(new ItemStack(Items.COBBLESTONE), 1.0F),
                new ProcessingOutput(new ItemStack(Items.QUARTZ), 0.25F)
        ));
        hammer(output, "andesite_to_cobblestone", Items.ANDESITE, List.of(
                new ProcessingOutput(new ItemStack(Items.COBBLESTONE), 1.0F),
                new ProcessingOutput(new ItemStack(Items.IRON_NUGGET), 0.15F)
        ));
        hammer(output, "clay_to_clay_balls", Items.CLAY, Items.CLAY_BALL, 4);
        hammer(output, "packed_mud_to_mud", Items.PACKED_MUD, List.of(
                new ProcessingOutput(new ItemStack(Items.MUD), 1.0F),
                new ProcessingOutput(new ItemStack(Items.WHEAT), 0.25F)
        ));
        hammer(output, "mud_bricks_to_mud", Items.MUD_BRICKS, Items.MUD, 1);
        hammer(output, "dripstone_block_to_pointed_dripstone", Items.DRIPSTONE_BLOCK, Items.POINTED_DRIPSTONE, 4);
        hammer(output, "calcite_to_bone_meal", Items.CALCITE, List.of(
                new ProcessingOutput(new ItemStack(Items.BONE_MEAL, 2), 1.0F),
                new ProcessingOutput(new ItemStack(Items.BONE_MEAL, 1), 0.50F)
        ));

        hammer(output, "coal_ore_to_coal", Ingredient.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.COAL, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.COAL, 1), 0.50F)
        ));
        hammer(output, "iron_ore_to_raw_iron", Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.RAW_IRON, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.RAW_IRON, 1), 0.35F)
        ));
        hammer(output, "gold_ore_to_raw_gold", Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.RAW_GOLD, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.RAW_GOLD, 1), 0.35F)
        ));
        hammer(output, "copper_ore_to_raw_copper", Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.RAW_COPPER, 3), 1.0F),
                new ProcessingOutput(new ItemStack(Items.RAW_COPPER, 2), 0.50F)
        ));
        hammer(output, "redstone_ore_to_redstone", Ingredient.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.REDSTONE, 4), 1.0F),
                new ProcessingOutput(new ItemStack(Items.REDSTONE, 2), 0.50F)
        ));
        hammer(output, "lapis_ore_to_lapis", Ingredient.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.LAPIS_LAZULI, 4), 1.0F),
                new ProcessingOutput(new ItemStack(Items.LAPIS_LAZULI, 3), 0.50F)
        ));
        hammer(output, "diamond_ore_to_diamond", Ingredient.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.DIAMOND, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.DIAMOND, 1), 0.25F)
        ));
        hammer(output, "emerald_ore_to_emerald", Ingredient.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE), List.of(
                new ProcessingOutput(new ItemStack(Items.EMERALD, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.EMERALD, 1), 0.25F)
        ));
        hammer(output, "nether_quartz_ore_to_quartz", Items.NETHER_QUARTZ_ORE, List.of(
                new ProcessingOutput(new ItemStack(Items.QUARTZ, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.QUARTZ, 1), 0.75F)
        ));
        hammer(output, "nether_gold_ore_to_gold_nuggets", Items.NETHER_GOLD_ORE, List.of(
                new ProcessingOutput(new ItemStack(Items.GOLD_NUGGET, 4), 1.0F),
                new ProcessingOutput(new ItemStack(Items.GOLD_NUGGET, 2), 0.50F)
        ));
        hammer(output, "amethyst_block_to_shards", Ingredient.of(Items.AMETHYST_BLOCK, Items.AMETHYST_CLUSTER), Items.AMETHYST_SHARD, 4);
        hammer(output, "gilded_blackstone_shatter", Items.GILDED_BLACKSTONE, List.of(
                new ProcessingOutput(new ItemStack(Items.BLACKSTONE, 1), 1.0F),
                new ProcessingOutput(new ItemStack(Items.GOLD_NUGGET, 2), 1.0F),
                new ProcessingOutput(new ItemStack(Items.GOLD_NUGGET, 2), 0.50F)
        ));

        hammer(output, "breeze_rod_to_wind_charges", Items.BREEZE_ROD, List.of(
                new ProcessingOutput(new ItemStack(Items.WIND_CHARGE, 4), 1.0F),
                new ProcessingOutput(new ItemStack(Items.WIND_CHARGE, 1), 0.50F)
        ));
        hammer(output, "blaze_rod_to_blaze_powder", Items.BLAZE_ROD, List.of(
                new ProcessingOutput(new ItemStack(Items.BLAZE_POWDER, 3), 1.0F),
                new ProcessingOutput(new ItemStack(Items.BLAZE_POWDER, 1), 0.50F)
        ));
        hammer(output, "bone_block_to_bone_meal", Items.BONE_BLOCK, Items.BONE_MEAL, 9);
        hammer(output, "bone_to_bone_meal", Items.BONE, List.of(
                new ProcessingOutput(new ItemStack(Items.BONE_MEAL, 3), 1.0F),
                new ProcessingOutput(new ItemStack(Items.BONE_MEAL, 1), 0.50F)
        ));
        hammer(output, "sugar_cane_to_sugar", Items.SUGAR_CANE, List.of(
                new ProcessingOutput(new ItemStack(Items.SUGAR, 2), 1.0F),
                new ProcessingOutput(new ItemStack(Items.SUGAR, 1), 0.50F)
        ));
        hammer(output, "honeycomb_block_to_honeycomb", Items.HONEYCOMB_BLOCK, Items.HONEYCOMB, 4);
        hammer(output, "melon_to_melon_slices", Items.MELON, Items.MELON_SLICE, 9);
        hammer(output, "magma_block_to_magma_cream", Items.MAGMA_BLOCK, List.of(
                new ProcessingOutput(new ItemStack(Items.MAGMA_CREAM, 2), 1.0F),
                new ProcessingOutput(new ItemStack(Items.MAGMA_CREAM, 1), 0.50F)
        ));
        hammer(output, "packed_ice_to_ice", Items.PACKED_ICE, Items.ICE, 9);
        hammer(output, "blue_ice_to_packed_ice", Items.BLUE_ICE, Items.PACKED_ICE, 9);
        hammer(output, "glowstone_to_glowstone_dust", Items.GLOWSTONE, Items.GLOWSTONE_DUST, 4);
        hammer(output, "sea_lantern_to_prismarine", Items.SEA_LANTERN, List.of(
                new ProcessingOutput(new ItemStack(Items.PRISMARINE_CRYSTALS, 4), 1.0F),
                new ProcessingOutput(new ItemStack(Items.PRISMARINE_SHARD, 5), 0.50F)
        ));
    }

    private static void hammer(RecipeOutput output, String recipeName, Ingredient input, ItemLike result, int count) {
        createHammerRecipe(output, recipeName, input, List.of(new ProcessingOutput(new ItemStack(result, count), 1.0F)));
    }

    private static void hammer(RecipeOutput output, String recipeName, ItemLike input, ItemLike result, int count) {
        hammer(output, recipeName, Ingredient.of(input), result, count);
    }

    private static void hammer(RecipeOutput output, String recipeName, ItemLike input, List<ProcessingOutput> resultsList) {
        createHammerRecipe(output, recipeName, Ingredient.of(input), resultsList);
    }

    private static void hammer(RecipeOutput output, String recipeName, Ingredient input, List<ProcessingOutput> resultsList) {
        createHammerRecipe(output, recipeName, input, resultsList);
    }

    private static void createHammerRecipe(RecipeOutput output, String recipeName, Ingredient input, List<ProcessingOutput> resultsList) {
        Ingredient tool = Ingredient.of(ModTags.Items.HAMMERS);

        StandardProcessingRecipe.Builder<WorkstoneRecipe> builder = new StandardProcessingRecipe.Builder<>(
                WorkstoneRecipe::new, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, recipeName))
                .require(input)
                .require(tool);

        for (ProcessingOutput result : resultsList) {
            builder.output(result);
        }

        builder.build(output);
    }
}
