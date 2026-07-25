package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> HAMMERS = tag("hammers");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
        }
    }
}
