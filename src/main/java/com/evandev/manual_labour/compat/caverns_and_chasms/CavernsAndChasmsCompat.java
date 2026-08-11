package com.evandev.manual_labour.compat.caverns_and_chasms;

import com.evandev.manual_labour.Constants;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

public final class CavernsAndChasmsCompat {

    public static final String MOD_ID = "caverns_and_chasms";

    public static final List<String> COPPER_HAMMER_IDS = List.of(
            "copper_hammer", "exposed_copper_hammer", "weathered_copper_hammer", "oxidized_copper_hammer"
    );

    private static final String CONTENT_IMPL = "com.evandev.manual_labour.compat.caverns_and_chasms.impl.CopperHammerContent";

    private static boolean loaded;
    private static List<DeferredItem<Item>> copperHammers = List.of();

    private CavernsAndChasmsCompat() {
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static List<DeferredItem<Item>> copperHammers() {
        return copperHammers;
    }

    @SuppressWarnings("unchecked")
    public static void init() {
        loaded = ModList.get().isLoaded(MOD_ID);
        if (!loaded) {
            Constants.LOG.info("Caverns and Chasms is not installed; copper hammer variants are disabled");
            return;
        }
        try {
            copperHammers = (List<DeferredItem<Item>>) Class.forName(CONTENT_IMPL).getMethod("register").invoke(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            Constants.LOG.error("Caverns and Chasms is installed but the copper hammer integration failed to load. "
                    + "Continuing without it.", e);
            loaded = false;
            copperHammers = List.of();
        }
    }
}
