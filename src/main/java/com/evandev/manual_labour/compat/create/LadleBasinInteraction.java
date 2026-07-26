package com.evandev.manual_labour.compat.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets the Ladle perform Mixing recipes directly on a Create Basin, as long as it's Heated
 * but not Super-Heated
 */
public final class LadleBasinInteraction {
    private static final int HOLD_CLICKS_REQUIRED = 5;
    private static final int STALE_AFTER_TICKS = 10;

    private static final Map<BlockPos, HoldState> ACTIVE_HOLDS = new HashMap<>();

    private LadleBasinInteraction() {
    }

    public static boolean tryMix(BasinBlockEntity basin, Player player, ItemStack ladleStack) {
        Level level = basin.getLevel();
        if (level == null) return false;
        BlockPos basinPos = basin.getBlockPos();

        HeatLevel heat = BasinBlockEntity.getHeatLevelOf(level.getBlockState(basinPos.below()));
        if (heat != HeatLevel.FADING && heat != HeatLevel.KINDLED) {
            ACTIVE_HOLDS.remove(basinPos);
            return false;
        }

        long now = level.getGameTime();
        HoldState previous = ACTIVE_HOLDS.get(basinPos);
        int count = (previous != null && now - previous.lastGameTime() <= STALE_AFTER_TICKS) ? previous.count() + 1 : 1;

        if (count < HOLD_CLICKS_REQUIRED) {
            ACTIVE_HOLDS.put(basinPos, new HoldState(count, now));
            return true;
        }
        ACTIVE_HOLDS.remove(basinPos);

        for (RecipeHolder<MixingRecipe> holder : level.getRecipeManager().getAllRecipesFor(AllRecipeTypes.MIXING.<RecipeInput, MixingRecipe>getType())) {
            if (BasinRecipe.apply(basin, holder.value())) {
                if (level instanceof ServerLevel serverLevel) {
                    ladleStack.hurtAndBreak(1, serverLevel, player, item -> {
                    });
                }
                level.playSound(null, basinPos, SoundEvents.GILDED_BLACKSTONE_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                level.playSound(null, basinPos, SoundEvents.NETHERRACK_BREAK, SoundSource.BLOCKS, 0.6F, 0.8F);
                return true;
            }
        }
        return false;
    }

    private record HoldState(int count, long lastGameTime) {
    }
}
