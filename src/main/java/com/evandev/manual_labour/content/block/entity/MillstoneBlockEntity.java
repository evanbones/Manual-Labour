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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class MillstoneBlockEntity extends KineticBlockEntity {
    public static final float BASE_SPEED = 256.0F;
    public static final float SPEED_LIMIT = 64.0F;
    public static final float STRESS_IMPACT = 16.0F;

    public ItemStackHandler inputInv;
    public ItemStackHandler outputInv;
    public IItemHandler capability;
    public int timer;
    private MillingRecipe lastRecipe;

    public float angle;
    public float prevAngle;
    private ItemStack grindingStack = ItemStack.EMPTY;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
        inputInv = new ItemStackHandler(1);
        outputInv = new ItemStackHandler(9);
        capability = new MillstoneInventoryHandler();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        float speed = millstone.rotorSpeed();
        AABB topVolume = new AABB(pos).inflate(1.5, 0.5, 1.5).move(0.0, 0.5, 0.0);
        for (Entity entity : level.getEntities((Entity) null, topVolume, Entity::isAlive)) {
            if (entity instanceof ItemEntity itemEntity) {
                millstone.tryInsertItemEntity(itemEntity);
            } else if (!(entity instanceof Player) && speed > 0.0F && !millstone.isOverspeed()) {
                turnEntity(level, pos, pos, entity);
            }
        }

        if (speed <= 0.0F || millstone.isOverspeed()) {
            millstone.setGrindingStack(ItemStack.EMPTY);
            if (millstone.timer != 0) {
                millstone.timer = 0;
                millstone.setChanged();
            }
            return;
        }

        boolean outputFull = true;
        for (int slot = 0; slot < millstone.outputInv.getSlots(); slot++) {
            if (millstone.outputInv.getStackInSlot(slot).getCount() < millstone.outputInv.getSlotLimit(slot)) {
                outputFull = false;
                break;
            }
        }
        if (outputFull) {
            return;
        }

        if (millstone.timer > 0) {
            millstone.timer -= millstone.getProcessingSpeed();
            millstone.setGrindingStack(millstone.inputInv.getStackInSlot(0).copyWithCount(1));
            if (millstone.timer <= 0) {
                millstone.process(level);
            }
            millstone.setChanged();
            return;
        }

        if (millstone.inputInv.getStackInSlot(0).isEmpty()) {
            millstone.setGrindingStack(ItemStack.EMPTY);
            return;
        }

        RecipeWrapper inventoryIn = new RecipeWrapper(millstone.inputInv);
        if (millstone.lastRecipe == null || !millstone.lastRecipe.matches(inventoryIn, level)) {
            Optional<RecipeHolder<MillingRecipe>> recipe = AllRecipeTypes.MILLING.find(inventoryIn, level);
            if (recipe.isEmpty()) {
                millstone.timer = 100;
                millstone.sendData();
            } else {
                millstone.lastRecipe = recipe.get().value();
                millstone.timer = millstone.lastRecipe.getProcessingDuration();
                millstone.sendData();
            }
            return;
        }

        millstone.timer = millstone.lastRecipe.getProcessingDuration();
        millstone.sendData();
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
            entity.move(MoverType.SHULKER_BOX, movement);
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

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16.0F), 1, 512);
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
                MillstoneEffects.tick(level, worldPosition, grindingStack, getSpeed());
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
        return inputInv.getStackInSlot(0);
    }

    private void setGrindingStack(ItemStack stack) {
        if (ItemStack.isSameItemSameComponents(grindingStack, stack)) {
            return;
        }
        grindingStack = stack;
        setChanged();
        sendData();
    }

    private void process(Level level) {
        RecipeWrapper inventoryIn = new RecipeWrapper(inputInv);

        if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level)) {
            Optional<RecipeHolder<MillingRecipe>> recipe = AllRecipeTypes.MILLING.find(inventoryIn, level);
            if (recipe.isEmpty()) {
                return;
            }
            lastRecipe = recipe.get().value();
        }

        ItemStack stackInSlot = inputInv.getStackInSlot(0);
        ItemStack craftingRemainingItem = stackInSlot.getCraftingRemainingItem();
        stackInSlot.shrink(1);
        inputInv.setStackInSlot(0, stackInSlot);

        for (ItemStack result : lastRecipe.rollResults(level.random)) {
            ItemHandlerHelper.insertItemStacked(outputInv, result, false);
        }
        if (!craftingRemainingItem.isEmpty()) {
            ItemHandlerHelper.insertItemStacked(outputInv, craftingRemainingItem, false);
        }

        sendData();
        setChanged();
    }

    private float rotorSpeed() {
        return Math.abs(getSpeed());
    }

    public boolean acceptsItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        ItemStackHandler tester = new ItemStackHandler(1);
        tester.setStackInSlot(0, stack);
        RecipeWrapper inventoryIn = new RecipeWrapper(tester);
        if (lastRecipe != null && lastRecipe.matches(inventoryIn, level)) {
            return true;
        }
        return AllRecipeTypes.MILLING.find(new SingleRecipeInput(stack), level).isPresent();
    }

    public void tryInsertItemEntity(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || !itemEntity.isAlive()) {
            return;
        }
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || !acceptsItem(stack)) {
            return;
        }
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inputInv, stack, false);
        if (remainder.isEmpty()) {
            itemEntity.discard();
            setChanged();
            sendData();
        } else if (remainder.getCount() < stack.getCount()) {
            itemEntity.setItem(remainder);
            setChanged();
            sendData();
        }
    }

    public ItemInteractionResult insertByHand(Player player, InteractionHand hand, ItemStack stack) {
        if (level == null || stack.isEmpty() || !acceptsItem(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inputInv, stack, false);
        if (remainder.getCount() < stack.getCount()) {
            player.setItemInHand(hand, remainder);
            setChanged();
            sendData();
            return ItemInteractionResult.sidedSuccess(false);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public InteractionResult extractByHand(Player player) {
        if (level == null) {
            return InteractionResult.PASS;
        }

        boolean emptiedAnything = false;
        if (level.isClientSide) {
            boolean hasItems = false;
            for (int slot = 0; slot < outputInv.getSlots(); slot++) {
                if (!outputInv.getStackInSlot(slot).isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
            if (!hasItems) {
                for (int slot = 0; slot < inputInv.getSlots(); slot++) {
                    if (!inputInv.getStackInSlot(slot).isEmpty()) {
                        hasItems = true;
                        break;
                    }
                }
            }
            return hasItems ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        for (int slot = 0; slot < outputInv.getSlots(); slot++) {
            ItemStack stackInSlot = outputInv.getStackInSlot(slot);
            if (!stackInSlot.isEmpty()) {
                emptiedAnything = true;
                player.getInventory().placeItemBackInInventory(stackInSlot);
                outputInv.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        if (!emptiedAnything) {
            for (int slot = 0; slot < inputInv.getSlots(); slot++) {
                ItemStack stackInSlot = inputInv.getStackInSlot(slot);
                if (!stackInSlot.isEmpty()) {
                    emptiedAnything = true;
                    player.getInventory().placeItemBackInInventory(stackInSlot);
                    inputInv.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        if (emptiedAnything) {
            setChanged();
            sendData();
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    public void dropBuffers() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < inputInv.getSlots(); i++) {
            ItemStack stack = inputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, stack);
            }
        }
        for (int i = 0; i < outputInv.getSlots(); i++) {
            ItemStack stack = outputInv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, stack);
            }
        }
        inputInv = new ItemStackHandler(1);
        outputInv = new ItemStackHandler(9);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Timer", timer);
        tag.put("InputInventory", inputInv.serializeNBT(registries));
        tag.put("OutputInventory", outputInv.serializeNBT(registries));
        if (!grindingStack.isEmpty()) {
            tag.put("Grinding", grindingStack.save(registries));
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        timer = tag.getInt("Timer");
        if (tag.contains("InputInventory")) {
            inputInv.deserializeNBT(registries, tag.getCompound("InputInventory"));
        }
        if (tag.contains("OutputInventory")) {
            outputInv.deserializeNBT(registries, tag.getCompound("OutputInventory"));
        }
        grindingStack = tag.contains("Grinding") ? ItemStack.parseOptional(registries, tag.getCompound("Grinding")) : ItemStack.EMPTY;
    }

    private class MillstoneInventoryHandler extends CombinedInvWrapper {
        public MillstoneInventoryHandler() {
            super(inputInv, outputInv);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (getHandlerFromIndex(getIndexForSlot(slot)) == outputInv) {
                return false;
            }
            return acceptsItem(stack) && super.isItemValid(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (getHandlerFromIndex(getIndexForSlot(slot)) == outputInv) {
                return stack;
            }
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (getHandlerFromIndex(getIndexForSlot(slot)) == inputInv) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }
    }
}
