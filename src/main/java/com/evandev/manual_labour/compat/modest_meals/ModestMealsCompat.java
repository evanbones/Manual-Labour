package com.evandev.manual_labour.compat.modest_meals;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public final class ModestMealsCompat {

    public static final String MOD_ID = "modest_meals";

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }

    public static boolean canExert(@Nullable Player player, float cost) {
        return !applies(player, cost) || ModestMealsStamina.canExert(player);
    }

    public static void exert(@Nullable Player player, float cost) {
        if (applies(player, cost) && !player.level().isClientSide()) {
            ModestMealsStamina.exert(player, cost);
        }
    }

    private static boolean applies(@Nullable Player player, float cost) {
        return cost > 0.0F && player != null && !(player instanceof FakePlayer) && isLoaded()
                && !player.isCreative() && !player.isSpectator();
    }
}
