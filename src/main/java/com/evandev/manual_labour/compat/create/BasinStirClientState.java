package com.evandev.manual_labour.compat.create;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class BasinStirClientState {
    private static final Map<BlockPos, StirState> STIR_STATES = new ConcurrentHashMap<>();

    private BasinStirClientState() {
    }

    public static float getStirSpeed() {
        return ModConfig.get().ladleStirSpeed;
    }

    public static StirState getOrCreate(BlockPos pos, ItemStack tool) {
        StirState state = STIR_STATES.get(pos);
        if (state == null) {
            state = new StirState(tool);
            STIR_STATES.put(pos, state);
        } else if (!tool.isEmpty()) {
            state.tool = tool;
        }
        return state;
    }

    public static void set(BlockPos pos, ItemStack tool) {
        if (tool.isEmpty()) {
            StirState state = STIR_STATES.get(pos);
            if (state != null) {
                state.active = false;
                state.targetAngularVelocity = 0.0F;
            }
        } else {
            StirState state = getOrCreate(pos, tool);
            state.tool = tool;
            state.active = true;
            state.targetAngularVelocity = getStirSpeed();
        }
    }

    public static Map<BlockPos, StirState> stirStates() {
        return STIR_STATES;
    }

    public static boolean isStirring(BlockPos pos) {
        StirState state = STIR_STATES.get(pos);
        return state != null && state.active;
    }

    public static boolean isEmpty() {
        return STIR_STATES.isEmpty();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (STIR_STATES.isEmpty()) return;

        for (StirState state : STIR_STATES.values()) {
            state.tick();
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        STIR_STATES.clear();
    }

    public static class StirState {
        public ItemStack tool;
        public boolean active;
        public float independentAngle;
        public float chasingAngularVelocity;
        public float targetAngularVelocity;

        public StirState(ItemStack tool) {
            this.tool = tool;
            this.active = true;
            this.independentAngle = 0.0F;
            this.chasingAngularVelocity = 0.0F;
            this.targetAngularVelocity = getStirSpeed();
        }

        public void tick() {
            if (active) {
                targetAngularVelocity = getStirSpeed();
                chasingAngularVelocity += (targetAngularVelocity - chasingAngularVelocity) * 0.25F;
                independentAngle += chasingAngularVelocity;
            } else {
                targetAngularVelocity = 0.0F;
                if (Math.abs(chasingAngularVelocity) > 0.2F) {
                    chasingAngularVelocity += (targetAngularVelocity - chasingAngularVelocity) * 0.15F;
                    independentAngle += chasingAngularVelocity;
                } else {
                    chasingAngularVelocity = 0.0F;
                    float targetSnapAngle = Math.round(independentAngle / 45.0F) * 45.0F;
                    independentAngle += (targetSnapAngle - independentAngle) * 0.25F;
                }
            }
        }

        public float getInterpolatedAngle(float partialTicks) {
            return independentAngle + partialTicks * chasingAngularVelocity;
        }
    }
}

