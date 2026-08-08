package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> HEAT_SOURCES = tag("heat_sources");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> HAMMERS = tag("hammers");
        public static final TagKey<Item> PESTLES = tag("pestles");
        public static final TagKey<Item> LADLES = tag("ladles");

        public static final TagKey<Item> TOOLS_HAMMER = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/hammer"));

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
        }
    }
}
