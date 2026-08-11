package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.content.item.HammerMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.List;

public final class HammerMaterials {

    public static final List<HammerMaterial> COMPAT = List.of(
            new HammerMaterial("silver", List.of("caverns_and_chasms", "oreganized"),
                    new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 157, 9.0F, 1.0F, 18, () -> Ingredient.of(ingotTag("silver"))),
                    6.0F, -3.0F, ingotTag("silver")),

            new HammerMaterial("necromium", "caverns_and_chasms",
                    new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 2031, 9.0F, 3.0F, 15, () -> Ingredient.of(ingotTag("necromium"))),
                    6.0F, -3.0F, ingotTag("necromium")),

            new HammerMaterial("electrum", "oreganized",
                    new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL, 1561, 8.0F, 3.0F, 14, () -> Ingredient.of(ingotTag("electrum"))),
                    5.0F, -3.0F, ingotTag("electrum"))
    );

    public static List<HammerMaterial> loaded() {
        return COMPAT.stream().filter(HammerMaterial::isLoaded).toList();
    }

    public static TagKey<Item> ingotTag(String material) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/" + material));
    }

    private HammerMaterials() {
    }
}
