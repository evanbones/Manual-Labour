package com.evandev.manual_labour.compat.modest_meals;

import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.world.entity.player.Player;

final class ModestMealsStamina {

    static boolean canExert(Player player) {
        return !StaminaApi.isEnabled() || (!StaminaApi.isExhausted(player) && StaminaApi.getStamina(player) > 0);
    }

    static void exert(Player player, float cost) {
        if (!StaminaApi.isEnabled()) {
            return;
        }
        PlayerStamina stamina = StaminaHelper.get(player);
        stamina.spend(cost);
        stamina.getData().setCooldown(Math.max(stamina.getData().getCooldown(), stamina.getCooldownInTicks()));
        stamina.syncNow();
    }
}
