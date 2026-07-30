package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.client.MillstoneEffects;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class MillstoneBlockEntity extends KineticBlockEntity {
    public static final int SLOTS_PER_BUFFER = 9;
    public static final int BUFFER_CAPACITY = 64;
    public static final float BASE_SPEED = 256.0F;
    public static final float SPEED_LIMIT = 64.0F;
    public static final float STRESS_IMPACT = 16.0F;

    public float angle;
    public float prevAngle;

    private NonNullList<ItemStack> input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private NonNullList<ItemStack> output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private float progress;
    private ItemStack grindingStack = ItemStack.EMPTY;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        float speed = millstone.rotorSpeed();
        if (speed > 0.0F && !millstone.isOverspeed()) {
            AABB topVolume = new AABB(pos).inflate(1.5, 0.5, 1.5).move(0.0, 0.5, 0.0);
            for (Entity entity : level.getEntities((Entity) null, topVolume, e -> !(e instanceof Player))) {
                turnEntity(level, pos, pos, entity);
            }
        }

        if (speed <= 0.0F || millstone.isOverspeed()) {
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

    public static void turnEntity(Level level, BlockPos masterPos, BlockPos standingPos, Entity entity) {
        if (!entity.onGround() || entity.getDeltaMovement().y > 0.0) {
            return;
        }
        if (entity.getY() < standingPos.getY() + 0.95) {
            return;
        }
        if (!(level.getBlockEntity(masterPos) instanceof MillstoneBlockEntity millstone)) {
            return;
        }
        if (millstone.isOverspeed()) {
            return;
        }
        float speed = millstone.getSpeed() * 3.0F / 10.0F;
        if (speed == 0.0F) {
            return;
        }
        if (level.isClientSide && entity instanceof Player) {
            Vec3 origin = new Vec3(masterPos.getX() + 0.5, entity.getY(), masterPos.getZ() + 0.5);
            Vec3 offset = entity.position().subtract(origin);
            offset = VecHelper.rotate(offset, Mth.clamp(speed, -16.0F, 16.0F), Direction.Axis.Y);
            Vec3 movement = origin.add(offset).subtract(entity.position());
            entity.move(net.minecraft.world.entity.MoverType.SHULKER_BOX, movement);
            return;
        }

        if (entity instanceof Player) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            float diff = entity.getYHeadRot() - speed;
            living.setNoActionTime(20);
            living.setYBodyRot(diff);
            living.setYHeadRot(diff);
        }

        entity.setYRot(entity.getYRot() - speed);

        Vec3 origin = new Vec3(masterPos.getX() + 0.5, entity.getY(), masterPos.getZ() + 0.5);
        Vec3 offset = entity.position().subtract(origin);
        offset = VecHelper.rotate(offset, Mth.clamp(speed, -16.0F, 16.0F), Direction.Axis.Y);
        Vec3 movement = origin.add(offset).subtract(entity.position());
        entity.move(MoverType.SHULKER_BOX, movement);
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = STRESS_IMPACT;
        return STRESS_IMPACT;
    }

    public boolean isOverspeed() {
        return Math.abs(getSpeed()) > SPEED_LIMIT;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (isOverspeed()) {
            tooltip.add(Component.literal("    ")
                    .append(Component.translatable("manual_labour.millstone.too_fast").withStyle(ChatFormatting.RED)));
            added = true;
        }
        return added;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }

        if (level.isClientSide) {
            prevAngle = angle;
            if (isOverspeed()) {
                spawnOverspeedParticles();
                return;
            }
            float speed = Math.abs(getSpeed());
            angle += getSpeed() * 3.0F / 10.0F;
            if (angle >= 360.0F) {
                angle -= 360.0F;
                prevAngle -= 360.0F;
            }
            if (angle <= -360.0F) {
                angle += 360.0F;
                prevAngle += 360.0F;
            }

            if (speed > 0.0F) {
                MillstoneEffects.tick(level, worldPosition, grindingStack, speed);
                AABB topVolume = new AABB(worldPosition).inflate(1.5, 0.5, 1.5).move(0.0, 0.5, 0.0);
                for (Entity entity : level.getEntities((Entity) null, topVolume, e -> e instanceof Player)) {
                    turnEntity(level, worldPosition, worldPosition, entity);
                }
            }
        } else {
            serverTick(level, worldPosition, getBlockState(), this);
        }
    }

    private void spawnOverspeedParticles() {
        Vec3 center = getBlockPos().getCenter();

        if (level.random.nextFloat() < 0.6F) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + level.random.nextDouble() * 0.25;
            level.addParticle(ParticleTypes.CRIT,
                    center.x + Math.cos(angle) * radius, center.y + 0.08 + level.random.nextDouble() * 0.5, center.z + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.05, 0.04, Math.sin(angle) * 0.05);
        }

        for (int i = 0; i < 2; i++) {
            if (level.random.nextFloat() <= 0.7F) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double radius = 1.52 + level.random.nextDouble() * 0.18;
                level.addParticle(ParticleTypes.CRIT,
                        center.x + Math.cos(angle) * radius, center.y - 0.45 + level.random.nextDouble() * 0.4, center.z + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.12, 0.03, Math.sin(angle) * 0.12);
            }
        }
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
        sendData();
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

    private float rotorSpeed() {
        return Math.abs(getSpeed());
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
            sendData();
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
            sendData();
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
        sendData();
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
            sendData();
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

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putFloat("Progress", progress);
        CompoundTag inputTag = new CompoundTag();
        ContainerHelper.saveAllItems(inputTag, input, registries);
        tag.put("Input", inputTag);
        CompoundTag outputTag = new CompoundTag();
        ContainerHelper.saveAllItems(outputTag, output, registries);
        tag.put("Output", outputTag);
        if (!grindingStack.isEmpty()) {
            tag.put("Grinding", grindingStack.save(registries));
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        progress = tag.getFloat("Progress");
        input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
        output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Input"), input, registries);
        ContainerHelper.loadAllItems(tag.getCompound("Output"), output, registries);
        grindingStack = tag.contains("Grinding") ? ItemStack.parseOptional(registries, tag.getCompound("Grinding")) : ItemStack.EMPTY;
    }
}
