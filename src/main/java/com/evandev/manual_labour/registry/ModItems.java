package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.item.HammerItem;
import com.evandev.manual_labour.content.item.LadleItem;
import com.evandev.manual_labour.content.item.PestleItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    public static final DeferredItem<Item> WORKSTONE_ITEM = ITEMS.register("workstone",
            () -> new BlockItem(ModBlocks.WORKSTONE.get(), new Item.Properties()));

    public static final DeferredItem<Item> MORTAR_ITEM = ITEMS.register("mortar",
            () -> new BlockItem(ModBlocks.MORTAR.get(), new Item.Properties()));

    public static final DeferredItem<Item> FLINT_HAMMER = ITEMS.register("flint_hammer", () -> new HammerItem(Tiers.STONE, 7.0f, -3.2f, new Item.Properties()));
    public static final DeferredItem<Item> IRON_HAMMER = ITEMS.register("iron_hammer", () -> new HammerItem(Tiers.IRON, 6.0f, -3.1f, new Item.Properties()));
    public static final DeferredItem<Item> GOLDEN_HAMMER = ITEMS.register("golden_hammer", () -> new HammerItem(Tiers.GOLD, 6.0f, -3.0f, new Item.Properties()));
    public static final DeferredItem<Item> DIAMOND_HAMMER = ITEMS.register("diamond_hammer", () -> new HammerItem(Tiers.DIAMOND, 5.0f, -3.0f, new Item.Properties()));
    public static final DeferredItem<Item> NETHERITE_HAMMER = ITEMS.register("netherite_hammer", () -> new HammerItem(Tiers.NETHERITE, 6.0f, -3.0f, new Item.Properties().fireResistant()));

    public static final DeferredItem<Item> PESTLE = ITEMS.register("pestle", () -> new PestleItem(new Item.Properties().durability(64)));
    public static final DeferredItem<Item> LADLE = ITEMS.register("ladle", () -> new LadleItem(new Item.Properties().durability(48)));
}
