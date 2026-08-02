package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.content.block.MortarBlock;
import com.evandev.manual_labour.recipe.MortarGrindingRecipe;
import com.evandev.manual_labour.recipe.MortarGrindingRecipeInput;
import com.evandev.manual_labour.recipe.MortarMixingRecipe;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MortarBlockEntity extends BlockEntity {
    public static final int SLOT_COUNT = 1;
    public static final int TANK_CAPACITY = 1000;
    private static final int HOLD_GRACE_TICKS = 10;
    private static final int PARTICLE_INTERVAL = 5;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClients();
        }
    };

    private final FluidTank fluidTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            syncToClients();
        }
    };

    private final RecipeManager.CachedCheck<MortarGrindingRecipeInput, MortarGrindingRecipe> grindingCheck;

    private int holdGraceTicks = 0;
    private int heldDurationTicks = 0;
    @Nullable
    private MortarProcess activeProcess;
    private boolean processingIsGrinding;
    @Nullable
    private Player activePlayer;

    private ItemStack activeTool = ItemStack.EMPTY;
    private boolean activeToolIsDecorative = false;
    private long processingStartGameTime = -1L;
    private int processingDuration = 0;

    private ItemStack decorativeTool = ItemStack.EMPTY;

    public MortarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MORTAR.get(), pos, state);
        grindingCheck = RecipeManager.createCheck(ModRecipeTypes.MORTAR_GRINDING.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MortarBlockEntity be) {
        if (be.activeProcess == null) return;

        if (be.holdGraceTicks <= 0) {
            be.cancelHold();
            return;
        }

        be.holdGraceTicks--;
        be.heldDurationTicks++;

        if (be.heldDurationTicks % PARTICLE_INTERVAL == 0) {
            be.spawnProcessingEffects();
        }

        if (be.heldDurationTicks >= be.processingDuration) {
            be.completeProcess();
        }
    }

    public boolean startOrContinueGrind(Player player, ItemStack toolStack) {
        return attemptProcess(player, toolStack, true);
    }

    public boolean startOrContinueMix(Player player, ItemStack toolStack) {
        return attemptProcess(player, toolStack, false);
    }

    private boolean attemptProcess(Player player, ItemStack toolStack, boolean grinding) {
        if (level == null) return false;

        if (activeProcess != null && processingIsGrinding == grinding) {
            if (level.isClientSide) return true;
            holdGraceTicks = HOLD_GRACE_TICKS;
            activePlayer = player;
            activeTool = toolStack;
            activeToolIsDecorative = !decorativeTool.isEmpty() && toolStack == decorativeTool;
            return true;
        }

        Optional<MortarProcess> found = grinding ? findGrindingProcess() : findMixingProcess();
        if (found.isEmpty()) return false;
        if (level.isClientSide) return true;

        activeProcess = found.get();
        processingIsGrinding = grinding;
        activeTool = toolStack;
        activeToolIsDecorative = !decorativeTool.isEmpty() && toolStack == decorativeTool;
        activePlayer = player;
        holdGraceTicks = HOLD_GRACE_TICKS;
        heldDurationTicks = 0;
        processingStartGameTime = level.getGameTime();
        processingDuration = Math.max(1, activeProcess.processingTime());
        setChanged();
        syncToClients();
        return true;
    }

    private void cancelHold() {
        activeProcess = null;
        activePlayer = null;
        heldDurationTicks = 0;
        holdGraceTicks = 0;
        activeTool = ItemStack.EMPTY;
        activeToolIsDecorative = false;
        processingStartGameTime = -1L;
        processingDuration = 0;
        setChanged();
        syncToClients();
    }

    private void completeProcess() {
        if (level == null || activeProcess == null) {
            cancelHold();
            return;
        }

        MortarProcess process = activeProcess;
        ItemStack tool = activeTool;
        Player player = activePlayer;
        boolean grinding = processingIsGrinding;

        if (!consumeIngredients(process, true)) {
            cancelHold();
            return;
        }
        consumeIngredients(process, false);

        ejectOutputs(process.rollResults(level.random));

        for (FluidStack fluidResult : process.fluidResults()) {
            if (!fluidResult.isEmpty()) {
                fluidTank.fill(fluidResult.copy(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        if (!level.isClientSide && player != null && !tool.isEmpty() && level instanceof ServerLevel serverLevel) {
            tool.hurtAndBreak(1, serverLevel, player, item -> {
            });
        }

        Optional<MortarProcess> next = tool.isEmpty() ? Optional.empty()
                : (grinding ? findGrindingProcess() : findMixingProcess());

        if (next.isPresent()) {
            activeProcess = next.get();
            heldDurationTicks = 0;
            processingStartGameTime = level.getGameTime();
            processingDuration = Math.max(1, activeProcess.processingTime());
            holdGraceTicks = HOLD_GRACE_TICKS;
            setChanged();
            syncToClients();
            return;
        }

        cancelHold();
    }

    private Optional<MortarProcess> findGrindingProcess() {
        if (level == null) return Optional.empty();
        ItemStack primary = getPrimaryItem();
        if (primary.isEmpty()) return Optional.empty();

        Optional<RecipeHolder<MortarGrindingRecipe>> own = grindingCheck.getRecipeFor(new MortarGrindingRecipeInput(primary), level);
        if (own.isPresent()) {
            return Optional.of(new MortarProcess.OwnGrindingProcess(own.get().value()));
        }

        SingleRecipeInput createInput = new SingleRecipeInput(primary);

        Optional<RecipeHolder<MillingRecipe>> milling = AllRecipeTypes.MILLING.find(createInput, level);
        if (milling.isPresent()) {
            return Optional.of(new MortarProcess.CreateProcess(milling.get().value()));
        }

        Optional<RecipeHolder<CrushingRecipe>> crushing = AllRecipeTypes.CRUSHING.find(createInput, level);
        return crushing.map(holder -> new MortarProcess.CreateProcess(holder.value()));
    }

    private Optional<MortarProcess> findMixingProcess() {
        if (level == null) return Optional.empty();

        for (RecipeHolder<MortarMixingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.MORTAR_MIXING.get())) {
            MortarProcess process = new MortarProcess.OwnMixingProcess(holder.value());
            if (consumeIngredients(process, true)) return Optional.of(process);
        }

        List<RecipeHolder<MixingRecipe>> mixingRecipes =
                level.getRecipeManager().getAllRecipesFor(AllRecipeTypes.MIXING.getType());
        for (RecipeHolder<MixingRecipe> holder : mixingRecipes) {
            MixingRecipe recipe = holder.value();
            if (!recipe.getRequiredHeat().testBlazeBurner(HeatLevel.NONE)) continue;
            MortarProcess process = new MortarProcess.CreateProcess(recipe);
            if (consumeIngredients(process, true)) return Optional.of(process);
        }

        return Optional.empty();
    }

    private ItemStack getPrimaryItem() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    private boolean consumeIngredients(MortarProcess process, boolean simulate) {
        int slotCount = inventory.getSlots();
        int[] remaining = new int[slotCount];
        int[] toExtract = new int[slotCount];
        for (int i = 0; i < slotCount; i++) {
            remaining[i] = inventory.getStackInSlot(i).getCount();
        }

        for (Ingredient ingredient : process.ingredients()) {
            int slot = findMatchingSlotWithRemaining(ingredient, remaining);
            if (slot < 0) return false;
            remaining[slot]--;
            toExtract[slot]++;
        }

        for (SizedFluidIngredient fluidIngredient : process.fluidIngredients()) {
            if (!fluidIngredient.test(fluidTank.getFluid())) return false;
        }

        if (!simulate) {
            for (int i = 0; i < slotCount; i++) {
                if (toExtract[i] > 0) inventory.extractItem(i, toExtract[i], false);
            }
            for (SizedFluidIngredient fluidIngredient : process.fluidIngredients()) {
                fluidTank.drain(fluidIngredient.amount(), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return true;
    }

    private int findMatchingSlotWithRemaining(Ingredient ingredient, int[] remaining) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (remaining[i] <= 0) continue;
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && ingredient.test(stack)) return i;
        }
        return -1;
    }

    private void ejectOutputs(List<ItemStack> outputs) {
        if (level == null) return;
        Direction direction = getBlockState().getValue(MortarBlock.FACING).getCounterClockWise();
        for (ItemStack output : outputs) {
            if (output.isEmpty()) continue;
            ItemEntity entity = new ItemEntity(
                    level, worldPosition.getX() + 0.5 + (direction.getStepX() * 0.2),
                    worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5 + (direction.getStepZ() * 0.2),
                    output
            );
            entity.setDeltaMovement(direction.getStepX() * 0.2F, 0.0F, direction.getStepZ() * 0.2F);
            level.addFreshEntity(entity);
        }
    }

    private void spawnProcessingEffects() {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack particleItem = getPrimaryItem();
        if (particleItem.isEmpty()) particleItem = activeTool;
        if (!particleItem.isEmpty()) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleItem),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5,
                    3, 0.15, 0.1, 0.15, 0.02);
        }

        if (!processingIsGrinding) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.85, worldPosition.getZ() + 0.5,
                    2, 0.15, 0.05, 0.15, 0.0);
        }

        playProcessingSound(serverLevel);
    }

    private void playProcessingSound(ServerLevel serverLevel) {
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.5;
        double z = worldPosition.getZ() + 0.5;
        float pitch = 0.85F + serverLevel.random.nextFloat() * 0.3F;

        if (processingIsGrinding) {
            boolean primary = serverLevel.random.nextFloat() < 0.78F;
            (primary ? AllSoundEvents.CRUSHING_1 : AllSoundEvents.CRUSHING_2)
                    .play(serverLevel, null, x, y, z, 0.5F, pitch);
        } else {
            AllSoundEvents.MIXING.play(serverLevel, null, x, y, z, 0.6F, pitch);
        }
    }

    public ItemStack setPrimaryItem(ItemStack stack) {
        ItemStack previous = inventory.getStackInSlot(0);
        inventory.setStackInSlot(0, stack);
        return previous;
    }

    public ItemStack removeItem() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                return inventory.extractItem(i, stack.getCount(), false);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasItem() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) return false;
        }
        return fluidTank.isEmpty();
    }

    public IItemHandler getItemHandler() {
        return inventory;
    }

    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(i));
        }
        if (!decorativeTool.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), decorativeTool);
        }
    }

    public boolean isProcessing() {
        return processingStartGameTime >= 0L && processingDuration > 0;
    }

    public ItemStack getActiveTool() {
        return activeTool;
    }

    public boolean isActiveToolDecorative() {
        return activeToolIsDecorative;
    }

    public ItemStack getDecorativeTool() {
        return decorativeTool;
    }

    public boolean placeDecorativeTool(ItemStack stack, boolean creative) {
        if (level == null || level.isClientSide || !decorativeTool.isEmpty() || isProcessing()) return false;

        decorativeTool = stack.copyWithCount(1);
        if (!creative) stack.shrink(1);
        setChanged();
        syncToClients();

        level.playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5F, 1.2F);
        return true;
    }

    public ItemStack removeDecorativeTool() {
        ItemStack result = decorativeTool;
        decorativeTool = ItemStack.EMPTY;
        setChanged();
        syncToClients();

        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.5F, 0.8F);
        }
        return result;
    }

    public float getProcessingProgress(float partialTick) {
        if (!isProcessing() || level == null) return 0F;
        float elapsed = (level.getGameTime() - processingStartGameTime) + partialTick;
        return Mth.clamp(elapsed / processingDuration, 0F, 1F);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && activeProcess == null && processingStartGameTime >= 0L) {
            processingStartGameTime = -1L;
            processingDuration = 0;
            activeTool = ItemStack.EMPTY;
            activeToolIsDecorative = false;
            setChanged();
        }
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
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

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        activeTool = tag.contains("ActiveTool") ? ItemStack.parseOptional(registries, tag.getCompound("ActiveTool")) : ItemStack.EMPTY;
        activeToolIsDecorative = tag.getBoolean("ActiveToolIsDecorative");
        processingStartGameTime = tag.getLong("ProcessingStart");
        processingDuration = tag.getInt("ProcessingDuration");
        decorativeTool = tag.contains("DecorativeTool") ? ItemStack.parseOptional(registries, tag.getCompound("DecorativeTool")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        if (!activeTool.isEmpty()) {
            tag.put("ActiveTool", activeTool.save(registries));
        }
        tag.putBoolean("ActiveToolIsDecorative", activeToolIsDecorative);
        tag.putLong("ProcessingStart", processingStartGameTime);
        tag.putInt("ProcessingDuration", processingDuration);
        if (!decorativeTool.isEmpty()) {
            tag.put("DecorativeTool", decorativeTool.save(registries));
        }
    }
}
