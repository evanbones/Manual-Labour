package com.evandev.manual_labour.compat.caverns_and_chasms.impl;

import com.evandev.manual_labour.content.item.HammerItem;
import com.evandev.manual_labour.registry.ModItems;
import com.teamabnormals.caverns_and_chasms.core.other.CCTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public final class CopperHammerContent {

    private CopperHammerContent() {
    }

    public static List<DeferredItem<Item>> register() {
        return List.of(
                ModItems.ITEMS.register("copper_hammer", () -> new CopperHammerItem(WeatherState.UNAFFECTED, new Item.Properties())),
                ModItems.ITEMS.register("exposed_copper_hammer", () -> new CopperHammerItem(WeatherState.EXPOSED, new Item.Properties())),
                ModItems.ITEMS.register("weathered_copper_hammer", () -> new CopperHammerItem(WeatherState.WEATHERED, new Item.Properties())),
                ModItems.ITEMS.register("oxidized_copper_hammer", () -> new CopperHammerItem(WeatherState.OXIDIZED, new Item.Properties())),
                ModItems.ITEMS.register("waxed_copper_hammer", () -> new HammerItem(CCTiers.CCItemTiers.COPPER, 6.0F, -3.0F, new Item.Properties())),
                ModItems.ITEMS.register("waxed_exposed_copper_hammer", () -> new HammerItem(CCTiers.CCItemTiers.COPPER, 6.0F, -3.0F, new Item.Properties())),
                ModItems.ITEMS.register("waxed_weathered_copper_hammer", () -> new HammerItem(CCTiers.CCItemTiers.COPPER, 6.0F, -3.0F, new Item.Properties())),
                ModItems.ITEMS.register("waxed_oxidized_copper_hammer", () -> new HammerItem(CCTiers.CCItemTiers.COPPER, 6.0F, -3.0F, new Item.Properties()))
        );
    }
}
