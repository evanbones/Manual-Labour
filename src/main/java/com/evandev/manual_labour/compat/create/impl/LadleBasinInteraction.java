package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.compat.create.BasinStirPayload;
import com.evandev.manual_labour.compat.create.CreateIntegration.LadleResult;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class LadleBasinInteraction {
    private static final int HOLD_GRACE_TICKS = 10;

    private static final Map<BasinKey, HoldState> ACTIVE_HOLDS = new HashMap<>();

    private LadleBasinInteraction() {
    }

    public static LadleResult tryStir(Level level, BlockPos pos, Player player, ItemStack ladleStack) {
        if (!(level.getBlockEntity(pos) instanceof BasinBlockEntity basin)) return LadleResult.NOT_A_BASIN;
        if (level.isClientSide) return LadleResult.NOTHING_TO_MIX;
        return tryMix(basin, player, ladleStack) ? LadleResult.STIRRING : LadleResult.NOTHING_TO_MIX;
    }

    public static boolean tryMix(BasinBlockEntity basin, Player player, ItemStack ladleStack) {
        Level level = basin.getLevel();
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return false;
        BlockPos basinPos = basin.getBlockPos();
        BasinKey key = new BasinKey(serverLevel, basinPos);

        HeatLevel heat = BasinBlockEntity.getHeatLevelOf(level.getBlockState(basinPos.below()));
        if (heat == HeatLevel.SEETHING) {
            cancelHold(key);
            return false;
        }

        HoldState state = ACTIVE_HOLDS.get(key);
        if (state == null) {
            MortarProcess process = findMatchingProcess(serverLevel, basin, heat);
            if (process == null) return false;

            ACTIVE_HOLDS.put(key, new HoldState(process, ladleStack, serverPlayer));
            broadcastStir(serverLevel, basinPos, ladleStack);
            return true;
        }

        state.graceTicks = HOLD_GRACE_TICKS;
        state.tool = ladleStack;
        state.player = serverPlayer;
        return true;
    }

    @Nullable
    private static MortarProcess findMatchingProcess(ServerLevel level, BasinBlockEntity basin, HeatLevel heat) {
        for (RecipeHolder<MortarMixingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MORTAR_MIXING.get())) {
            MortarProcess process = new MortarProcess.OwnMixingProcess(holder.value());
            if (canProcess(level, basin, process)) return process;
        }

        if (ModConfig.get().useCreateMixingRecipes) {
            for (RecipeHolder<MixingRecipe> holder : level.getRecipeManager().getAllRecipesFor(AllRecipeTypes.MIXING.<RecipeInput, MixingRecipe>getType())) {
                MixingRecipe recipe = holder.value();
                if (!recipe.getRequiredHeat().testBlazeBurner(heat)) continue;

                MortarProcess process = new CreateMortarProcess(recipe);
                if (canProcess(level, basin, process)) return process;
            }
        }

        return null;
    }

    private static boolean canProcess(ServerLevel level, BasinBlockEntity basin, MortarProcess process) {
        return applyProcess(level, basin, process, true);
    }

    private static boolean applyProcess(ServerLevel level, BasinBlockEntity basin, MortarProcess process, boolean simulate) {
        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);
        IFluidHandler fluids = level.getCapability(Capabilities.FluidHandler.BLOCK, basin.getBlockPos(), null);
        if (items == null || fluids == null) return false;

        if (!consumeIngredients(items, fluids, process, true)) return false;

        List<ItemStack> results = process.rollResults(level.random);
        List<FluidStack> fluidResults = process.fluidResults();
        if (!basin.acceptOutputs(results, fluidResults, true)) return false;

        if (simulate) return true;

        consumeIngredients(items, fluids, process, false);
        return basin.acceptOutputs(results, fluidResults, false);
    }

    private static boolean consumeIngredients(IItemHandler items, IFluidHandler fluids, MortarProcess process, boolean simulate) {
        int slotCount = items.getSlots();
        int[] remaining = new int[slotCount];
        int[] toExtract = new int[slotCount];
        for (int i = 0; i < slotCount; i++) {
            remaining[i] = items.getStackInSlot(i).getCount();
        }

        for (Ingredient ingredient : process.ingredients()) {
            int slot = findMatchingSlotWithRemaining(items, ingredient, remaining);
            if (slot < 0) return false;
            remaining[slot]--;
            toExtract[slot]++;
        }

        for (SizedFluidIngredient fluidIngredient : process.fluidIngredients()) {
            if (findMatchingFluidTank(fluids, fluidIngredient) < 0) return false;
        }

        if (!simulate) {
            for (int i = 0; i < slotCount; i++) {
                if (toExtract[i] > 0) items.extractItem(i, toExtract[i], false);
            }
            for (SizedFluidIngredient fluidIngredient : process.fluidIngredients()) {
                int tank = findMatchingFluidTank(fluids, fluidIngredient);
                if (tank < 0) continue;
                FluidStack toDrain = fluids.getFluidInTank(tank).copyWithAmount(fluidIngredient.amount());
                fluids.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return true;
    }

    private static int findMatchingSlotWithRemaining(IItemHandler items, Ingredient ingredient, int[] remaining) {
        for (int i = 0; i < items.getSlots(); i++) {
            if (remaining[i] <= 0) continue;
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && ingredient.test(stack)) return i;
        }
        return -1;
    }

    private static int findMatchingFluidTank(IFluidHandler fluids, SizedFluidIngredient ingredient) {
        for (int t = 0; t < fluids.getTanks(); t++) {
            FluidStack stack = fluids.getFluidInTank(t);
            if (!stack.isEmpty() && ingredient.test(stack)) return t;
        }
        return -1;
    }

    private static void cancelHold(BasinKey key) {
        if (ACTIVE_HOLDS.remove(key) != null) {
            broadcastStir(key.level(), key.pos(), ItemStack.EMPTY);
        }
    }

    private static void broadcastStir(ServerLevel level, BlockPos pos, ItemStack tool) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), new BasinStirPayload(pos, tool));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (ACTIVE_HOLDS.isEmpty() || !(event.getLevel() instanceof ServerLevel serverLevel)) return;

        Iterator<Map.Entry<BasinKey, HoldState>> iterator = ACTIVE_HOLDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BasinKey, HoldState> entry = iterator.next();
            BasinKey key = entry.getKey();
            if (key.level() != serverLevel) continue;

            HoldState state = entry.getValue();
            if (--state.graceTicks <= 0) {
                iterator.remove();
                broadcastStir(serverLevel, key.pos(), ItemStack.EMPTY);
                continue;
            }

            if (++state.heldTicks < state.process.processingTime()) continue;

            HeatLevel heat = BasinBlockEntity.getHeatLevelOf(serverLevel.getBlockState(key.pos().below()));
            if (heat == HeatLevel.SEETHING
                    || !(serverLevel.getBlockEntity(key.pos()) instanceof BasinBlockEntity basin)
                    || !completeProcess(serverLevel, basin, state)) {
                iterator.remove();
                broadcastStir(serverLevel, key.pos(), ItemStack.EMPTY);
                continue;
            }

            MortarProcess next = state.tool.isEmpty() ? null : findMatchingProcess(serverLevel, basin, heat);
            if (next == null) {
                iterator.remove();
                broadcastStir(serverLevel, key.pos(), ItemStack.EMPTY);
                continue;
            }

            state.process = next;
            state.heldTicks = 0;
        }
    }

    private static boolean completeProcess(ServerLevel level, BasinBlockEntity basin, HoldState state) {
        if (!applyProcess(level, basin, state.process, false)) return false;

        state.tool.hurtAndBreak(1, level, state.player, item -> {
        });
        BlockPos basinPos = basin.getBlockPos();
        level.playSound(null, basinPos, SoundEvents.GILDED_BLACKSTONE_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
        level.playSound(null, basinPos, SoundEvents.NETHERRACK_BREAK, SoundSource.BLOCKS, 0.6F, 0.8F);
        return true;
    }

    private record BasinKey(ServerLevel level, BlockPos pos) {
    }

    private static final class HoldState {
        MortarProcess process;
        ItemStack tool;
        ServerPlayer player;
        int heldTicks = 0;
        int graceTicks = HOLD_GRACE_TICKS;

        HoldState(MortarProcess process, ItemStack tool, ServerPlayer player) {
            this.process = process;
            this.tool = tool;
            this.player = player;
        }
    }
}
