package com.evandev.manual_labour.compat.caverns_and_chasms.impl;

import com.evandev.manual_labour.registry.ModItems;
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
                ModItems.ITEMS.register("oxidized_copper_hammer", () -> new CopperHammerItem(WeatherState.OXIDIZED, new Item.Properties()))
        );
    }
}
