package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.client.MillstoneEffects;
import com.evandev.manual_labour.content.block.MillstoneStructure;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MillstoneBlockEntity extends BlockEntity {
    public static final int SLOTS_PER_BUFFER = 9;
    public static final int BUFFER_CAPACITY = 64;
    public static final float BASE_SPEED = 256.0F;

    private NonNullList<ItemStack> input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private NonNullList<ItemStack> output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private float progress;
    private ItemStack grindingStack = ItemStack.EMPTY;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    public ItemStack getGrindingStack() {
        return grindingStack;
    }

    private void setGrindingStack(ItemStack stack) {
        if (ItemStack.isSameItemSameComponents(grindingStack, stack)) {
            return;
        }
        grindingStack = stack;
        setChanged();
        syncToClients();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        float speed = millstone.rotorSpeed();
        if (speed <= 0.0F || speed > MillstoneRotorBlockEntity.SPEED_LIMIT) {
            millstone.setGrindingStack(ItemStack.EMPTY);
            if (millstone.progress != 0.0F) {
                millstone.progress = 0.0F;
                millstone.setChanged();
            }
            return;
        }

        int workSlot = -1;
        RecipeHolder<MillingRecipe> recipe = null;
        for (int slot = 0; slot < SLOTS_PER_BUFFER; slot++) {
            ItemStack stack = millstone.input.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<RecipeHolder<MillingRecipe>> match = AllRecipeTypes.MILLING.find(new SingleRecipeInput(stack), level);
            if (match.isPresent() && millstone.canFitResult(match.get().value())) {
                workSlot = slot;
                recipe = match.get();
                break;
            }
        }

        if (recipe == null) {
            millstone.setGrindingStack(ItemStack.EMPTY);
            if (millstone.progress != 0.0F) {
                millstone.progress = 0.0F;
                millstone.setChanged();
            }
            return;
        }

        millstone.setGrindingStack(millstone.input.get(workSlot).copyWithCount(1));
        millstone.progress += speed / BASE_SPEED;

        if (millstone.progress >= recipe.value().getProcessingDuration()) {
            millstone.complete(level, workSlot, recipe.value());
            millstone.progress = 0.0F;
        }
        millstone.setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        BlockPos rotorPos = pos.offset((Vec3i) MillstoneStructure.ROTOR_OFFSET);
        if (!(level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor)) {
            return;
        }
        float speed = Math.abs(rotor.getSpeed());
        if (speed == 0.0F || rotor.isOverspeed()) {
            return;
        }
        MillstoneEffects.tick(level, pos, millstone.grindingStack, speed);
    }

    private void complete(Level level, int slot, MillingRecipe recipe) {
        ItemStack stackInSlot = input.get(slot);
        ItemStack craftingRemainder = stackInSlot.getCraftingRemainingItem();
        stackInSlot.shrink(1);

        for (ItemStack result : recipe.rollResults(level.random)) {
            insertResult(result);
        }
        if (!craftingRemainder.isEmpty()) {
            insertResult(craftingRemainder);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private float rotorSpeed() {
        if (level == null) {
            return 0.0F;
        }
        BlockPos rotorPos = worldPosition.offset((Vec3i) MillstoneStructure.ROTOR_OFFSET);
        if (level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor) {
            return Math.abs(rotor.getSpeed());
        }
        return 0.0F;
    }

    public boolean acceptsItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        return AllRecipeTypes.MILLING.find(new SingleRecipeInput(stack), level).isPresent();
    }

    public int totalCount(boolean isInput) {
        NonNullList<ItemStack> buffer = isInput ? input : output;
        int total = 0;
        for (ItemStack stack : buffer) {
            total += stack.getCount();
        }
        return total;
    }

    public ItemStack getBufferStack(boolean isInput, int slot) {
        return (isInput ? input : output).get(slot);
    }

    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        if (!acceptsItem(stack)) {
            return stack;
        }
        int room = BUFFER_CAPACITY - totalCount(true);
        if (room <= 0) {
            return stack;
        }
        int toInsert = Math.min(stack.getCount(), room);
        int remaining = toInsert;

        for (int slot = 0; slot < SLOTS_PER_BUFFER && remaining > 0; slot++) {
            ItemStack existing = input.get(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) {
                continue;
            }
            int space = Math.min(existing.getMaxStackSize(), BUFFER_CAPACITY) - existing.getCount();
            int moved = Math.min(space, remaining);
            if (moved > 0) {
                if (!simulate) {
                    existing.grow(moved);
                }
                remaining -= moved;
            }
        }

        for (int slot = 0; slot < SLOTS_PER_BUFFER && remaining > 0; slot++) {
            if (!input.get(slot).isEmpty()) {
                continue;
            }
            int moved = Math.min(stack.getMaxStackSize(), remaining);
            if (!simulate) {
                input.set(slot, stack.copyWithCount(moved));
            }
            remaining -= moved;
        }

        int inserted = toInsert - remaining;
        if (inserted <= 0) {
            return stack;
        }
        if (!simulate) {
            setChanged();
            syncToClients();
        }
        return stack.copyWithCount(stack.getCount() - inserted);
    }

    public ItemStack extractOutput(int slot, int amount, boolean simulate) {
        ItemStack existing = output.get(slot);
        if (existing.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        int taken = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(taken);
        if (!simulate) {
            existing.shrink(taken);
            setChanged();
            syncToClients();
        }
        return result;
    }

    private boolean canFitResult(MillingRecipe recipe) {
        int amount = 0;
        for (ItemStack possible : recipe.getRollableResultsAsItemStacks()) {
            amount += possible.getCount();
        }
        return totalCount(false) + amount <= BUFFER_CAPACITY;
    }

    private void insertResult(ItemStack result) {
        for (int slot = 0; slot < SLOTS_PER_BUFFER && !result.isEmpty(); slot++) {
            ItemStack existing = output.get(slot);
            if (existing.isEmpty()) {
                output.set(slot, result.copy());
                result.setCount(0);
            } else if (ItemStack.isSameItemSameComponents(existing, result)) {
                int moved = Math.min(existing.getMaxStackSize() - existing.getCount(), result.getCount());
                existing.grow(moved);
                result.shrink(moved);
            }
        }
        setChanged();
        syncToClients();
    }

    public ItemInteractionResult insertByHand(Player player, InteractionHand hand, ItemStack stack) {
        if (level == null || !acceptsItem(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }
        ItemStack leftover = insertInput(stack, false);
        if (leftover.getCount() == stack.getCount()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        player.setItemInHand(hand, leftover);
        return ItemInteractionResult.sidedSuccess(false);
    }

    public InteractionResult extractByHand(Player player) {
        if (level == null) {
            return InteractionResult.PASS;
        }
        boolean hasOutput = totalCount(false) > 0;
        if (level.isClientSide) {
            return hasOutput ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        for (int slot = SLOTS_PER_BUFFER - 1; slot >= 0; slot--) {
            ItemStack existing = output.get(slot);
            if (existing.isEmpty()) {
                continue;
            }
            ItemStack taken = existing.copy();
            output.set(slot, ItemStack.EMPTY);
            setChanged();
            syncToClients();
            player.getInventory().placeItemBackInInventory(taken);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public void dropBuffers() {
        if (level == null) {
            return;
        }
        for (ItemStack stack : input) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, stack);
            }
        }
        for (ItemStack stack : output) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, stack);
            }
        }
        input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
        output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Progress", progress);
        CompoundTag inputTag = new CompoundTag();
        net.minecraft.world.ContainerHelper.saveAllItems(inputTag, input, registries);
        tag.put("Input", inputTag);
        CompoundTag outputTag = new CompoundTag();
        net.minecraft.world.ContainerHelper.saveAllItems(outputTag, output, registries);
        tag.put("Output", outputTag);
        if (!grindingStack.isEmpty()) {
            tag.put("Grinding", grindingStack.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getFloat("Progress");
        input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
        output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(tag.getCompound("Input"), input, registries);
        net.minecraft.world.ContainerHelper.loadAllItems(tag.getCompound("Output"), output, registries);
        grindingStack = tag.contains("Grinding") ? ItemStack.parseOptional(registries, tag.getCompound("Grinding")) : ItemStack.EMPTY;
    }
}
