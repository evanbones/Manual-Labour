package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.content.block.WorkstoneBlock;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.evandev.manual_labour.registry.ModSounds;
import com.evandev.manual_labour.registry.ModTags;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorkstoneBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    };

    private final ItemStackHandler recipeInv = new ItemStackHandler(2);
    private final RecipeManager.CachedCheck<RecipeWrapper, WorkstoneRecipe> quickCheck;
    private boolean isItemCarvingWorkstone;

    public WorkstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORKSTONE.get(), pos, state);
        quickCheck = RecipeManager.createCheck(ModRecipeTypes.WORKSTONE.get());
    }

    public boolean processStoredItemUsingTool(ItemStack toolStack, Player player) {
        if (level == null || isItemCarvingWorkstone) return false;

        Optional<RecipeHolder<? extends ProcessingRecipe<?, ?>>> matchingRecipe = getMatchingRecipe(toolStack);

        if (matchingRecipe.isPresent()) {
            RecipeHolder<? extends ProcessingRecipe<?, ?>> recipeHolder = matchingRecipe.get();
            ProcessingRecipe<?, ?> recipe = recipeHolder.value();
            Direction direction = getBlockState().getValue(WorkstoneBlock.FACING).getCounterClockWise();
            ItemStack hitItem = getStoredItem();

            List<ItemStack> rolledResults;
            boolean isStandalonePressing = recipe instanceof PressingRecipe
                    && !hitItem.has(AllDataComponents.SEQUENCED_ASSEMBLY);

            if (isStandalonePressing) {
                float yieldMultiplier = ModConfig.get().workstonePressingYield;
                rolledResults = new ArrayList<>();
                for (ProcessingOutput output : recipe.getRollableResults()) {
                    float chance = output.getChance() * yieldMultiplier;
                    if (chance >= 1.0F || level.random.nextFloat() < chance) {
                        rolledResults.add(output.getStack().copy());
                    }
                }
            } else {
                rolledResults = recipe.rollResults(level.random);
            }

            boolean stillInProgress = !rolledResults.isEmpty()
                    && rolledResults.getFirst().has(AllDataComponents.SEQUENCED_ASSEMBLY);

            int ejectFrom = stillInProgress ? 1 : 0;
            for (int i = ejectFrom; i < rolledResults.size(); i++) {
                ItemStack resultStack = rolledResults.get(i);
                if (resultStack.isEmpty()) continue;

                ItemEntity entity = new ItemEntity(
                        level, worldPosition.getX() + 0.5 + (direction.getStepX() * 0.2),
                        worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5 + (direction.getStepZ() * 0.2),
                        resultStack
                );
                entity.setDeltaMovement(direction.getStepX() * 0.2F, 0.0F, direction.getStepZ() * 0.2F);
                level.addFreshEntity(entity);
            }

            if (!level.isClientSide) {
                if (recipe instanceof DeployerApplicationRecipe deployerRecipe) {
                    if (!deployerRecipe.shouldKeepHeldItem()) {
                        toolStack.shrink(1);
                    }
                } else {
                    toolStack.hurtAndBreak(1, (ServerLevel) level, player, (item) -> {
                    });
                }

                if (player != null) {
                    player.awardStat(Stats.ITEM_USED.get(toolStack.getItem()));
                }

                SoundEvent hitSound;
                if (hitItem.getItem() instanceof BlockItem blockItem) {
                    hitSound = blockItem.getBlock().defaultBlockState().getSoundType().getBreakSound();
                } else {
                    hitSound = level.random.nextBoolean() ? ModSounds.HAMMER1.get() : ModSounds.HAMMER2.get();
                }

                level.playSound(null, worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F,
                        hitSound, SoundSource.BLOCKS, 0.8F, 1.0F);
            }

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, hitItem.copy()), worldPosition.getX() + 0.5, worldPosition.getY() + 0.8, worldPosition.getZ() + 0.5, 5, 0.1, 0.1, 0.1, 0.05D);
            }

            if (stillInProgress) {
                inventory.setStackInSlot(0, rolledResults.getFirst());
            } else {
                inventory.extractItem(0, 1, false);
            }

            if (player != null && ModConfig.get().workstoneHammerCooldownTicks > 0) {
                player.getCooldowns().addCooldown(toolStack.getItem(), ModConfig.get().workstoneHammerCooldownTicks);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack remaining = getStoredItem();
                if (!remaining.isEmpty()) {
                    serverPlayer.displayClientMessage(Component.translatable("manual_labour.workstone.remaining_items", remaining.getCount()), true);
                } else {
                    serverPlayer.displayClientMessage(Component.empty(), true);
                }
            }
        } else if (player instanceof ServerPlayer serverPlayer) {
            ItemStack storedItem = getStoredItem();
            if (!storedItem.isEmpty()) {
                if (hasAnyRecipeFor(storedItem)) {
                    serverPlayer.displayClientMessage(Component.translatable("block.manual_labour.workstone.invalid_tool"), true);
                } else {
                    serverPlayer.displayClientMessage(Component.translatable("block.manual_labour.workstone.invalid_item"), true);
                }
            }
        }

        return matchingRecipe.isPresent();
    }

    public boolean hasAnyRecipeFor(ItemStack storedItem) {
        if (level == null || storedItem.isEmpty()) return false;

        RecipeManager manager = level.getRecipeManager();

        for (RecipeHolder<WorkstoneRecipe> recipe : manager.getAllRecipesFor(ModRecipeTypes.WORKSTONE.get())) {
            if (!recipe.value().getIngredients().isEmpty() && recipe.value().getIngredients().getFirst().test(storedItem)) {
                return true;
            }
        }

        if (ModConfig.get().useCreateDeployingRecipes) {
            for (RecipeHolder<?> holder : manager.getAllRecipesFor(AllRecipeTypes.DEPLOYING.getType())) {
                if (holder.value() instanceof DeployerApplicationRecipe deployerRecipe) {
                    if (!deployerRecipe.getIngredients().isEmpty() && deployerRecipe.getIngredients().getFirst().test(storedItem)) {
                        return true;
                    }
                }
            }
        }

        if (ModConfig.get().useCreatePressingRecipes) {
            SingleRecipeInput pressingInput = new SingleRecipeInput(storedItem);
            return AllRecipeTypes.PRESSING.find(pressingInput, level).isPresent();
        }

        return false;
    }

    private Optional<RecipeHolder<? extends ProcessingRecipe<?, ?>>> getMatchingRecipe(ItemStack toolStack) {
        if (level == null) return Optional.empty();

        recipeInv.setStackInSlot(0, getStoredItem());
        recipeInv.setStackInSlot(1, toolStack);
        RecipeWrapper wrapper = new RecipeWrapper(recipeInv);

        Optional<RecipeHolder<WorkstoneRecipe>> workstoneStep = SequencedAssemblyRecipe.getRecipe(
                level, wrapper, ModRecipeTypes.WORKSTONE.get(), WorkstoneRecipe.class);
        if (workstoneStep.isPresent()) {
            return Optional.of(workstoneStep.get());
        }

        if (ModConfig.get().useCreateDeployingRecipes) {
            Optional<RecipeHolder<DeployerApplicationRecipe>> deployingStep = SequencedAssemblyRecipe.getRecipe(
                    level, wrapper, AllRecipeTypes.DEPLOYING.getType(), DeployerApplicationRecipe.class);
            if (deployingStep.isPresent()) {
                return Optional.of(deployingStep.get());
            }
        }

        Optional<RecipeHolder<WorkstoneRecipe>> standalone = quickCheck.getRecipeFor(wrapper, level);
        if (standalone.isPresent()) {
            return Optional.of(standalone.get());
        }

        if (ModConfig.get().useCreatePressingRecipes && toolStack.is(ModTags.Items.HAMMERS)) {
            Optional<RecipeHolder<PressingRecipe>> pressingStep = SequencedAssemblyRecipe.getRecipe(
                    level, getStoredItem(), AllRecipeTypes.PRESSING.getType(), PressingRecipe.class);
            if (pressingStep.isPresent()) {
                return Optional.of(pressingStep.get());
            }

            SingleRecipeInput pressingInput = new SingleRecipeInput(getStoredItem());
            Optional<RecipeHolder<PressingRecipe>> pressing = AllRecipeTypes.PRESSING.find(pressingInput, level);
            if (pressing.isPresent()) {
                return Optional.of(pressing.get());
            }
        }

        return Optional.empty();
    }

    public boolean canAddItem(ItemStack addedStack) {
        if (isItemCarvingWorkstone || addedStack.isEmpty()) return false;
        return inventory.insertItem(0, addedStack.copy(), true).getCount() != addedStack.getCount();
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

    public IItemHandler getInventory() {
        return inventory;
    }

    public ItemStack addItem(ItemStack addedStack) {
        if (!isItemCarvingWorkstone) {
            return inventory.insertItem(0, addedStack.copy(), false);
        }
        return addedStack;
    }

    public ItemStack removeItem() {
        isItemCarvingWorkstone = false;
        return inventory.extractItem(0, inventory.getSlotLimit(0), false);
    }

    public boolean carveToolOnWorkstone(ItemStack toolStack) {
        if (toolStack.getItem() instanceof TieredItem || toolStack.getItem() instanceof TridentItem || toolStack.getItem() instanceof ShearsItem) {
            if (addItem(toolStack).isEmpty()) {
                isItemCarvingWorkstone = true;
                return true;
            }
        }
        return false;
    }

    public boolean isItemCarvingWorkstone() {
        return isItemCarvingWorkstone;
    }

    public ItemStack getStoredItem() {
        return inventory.getStackInSlot(0);
    }

    public int getMaxStackSize() {
        return inventory.getSlotLimit(0);
    }

    public boolean isEmpty() {
        return getStoredItem().isEmpty();
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        isItemCarvingWorkstone = tag.getBoolean("IsItemCarved");
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putBoolean("IsItemCarved", isItemCarvingWorkstone);
    }
}
