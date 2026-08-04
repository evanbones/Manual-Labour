package com.evandev.manual_labour.compat.create;

import com.evandev.manual_labour.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class BasinStirClientState {
    private static final Map<BlockPos, ItemStack> ACTIVE_STIRS = new ConcurrentHashMap<>();

    private BasinStirClientState() {
    }

    public static void set(BlockPos pos, ItemStack tool) {
        if (tool.isEmpty()) {
            ACTIVE_STIRS.remove(pos);
        } else {
            ACTIVE_STIRS.put(pos, tool);
        }
    }

    public static Map<BlockPos, ItemStack> activeStirs() {
        return ACTIVE_STIRS;
    }

    public static boolean isEmpty() {
        return ACTIVE_STIRS.isEmpty();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        ACTIVE_STIRS.clear();
    }
}
