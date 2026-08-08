package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.compat.create.CreateIntegration;
import com.evandev.manual_labour.compat.create.impl.millstone.*;
import com.evandev.manual_labour.compat.create.impl.ponder.MillstonePonderScene;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.recipe.ManualProcessingExclusions;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.recipe.WorkstoneProcess;
import com.evandev.manual_labour.registry.ModRecipeTypes;
import com.simibubi.create.*;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.data.Pair;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CreateIntegrationImpl implements CreateIntegration {

    public CreateIntegrationImpl() {
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, LadleBasinInteraction::onLevelTick);
    }

    @Nullable
    private static MillstoneItemHandler resolveMillstoneItemHandler(Level level, BlockPos pos, BlockState state) {
        BlockPos master;
        if (state.getBlock() instanceof MillstoneBlock) {
            master = pos;
        } else if (state.getBlock() instanceof MillstoneStructuralBlock) {
            master = MillstoneStructuralBlock.getMaster(level, pos, state);
        } else {
            return null;
        }
        if (master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone) {
            return new MillstoneItemHandler(millstone);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static RecipeType<CreateWorkstoneRecipe> workstoneRecipeType() {
        return (RecipeType<CreateWorkstoneRecipe>) (RecipeType<?>) ModRecipeTypes.WORKSTONE.get();
    }

    private static RecipeWrapper wrap(ItemStack stored, ItemStack tool) {
        ItemStackHandler handler = new ItemStackHandler(2);
        handler.setStackInSlot(0, stored);
        handler.setStackInSlot(1, tool);
        return new RecipeWrapper(handler);
    }

    private static HeatLevel asBlazeBurnerHeat(HeatCondition heat) {
        return switch (heat) {
            case NONE -> HeatLevel.NONE;
            case HEATED -> HeatLevel.KINDLED;
            case SUPERHEATED -> HeatLevel.SEETHING;
        };
    }

    @Override
    public void registerContent() {
        CreateContent.register();
    }

    @Override
    public void onCommonSetup() {
        Block millstone = CreateContent.MILLSTONE.get();
        BlockStressValues.IMPACTS.register(millstone, () -> (double) MillstoneBlockEntity.STRESS_IMPACT);
        TooltipModifier.REGISTRY.register(millstone.asItem(), new KineticStats(millstone));
    }

    @Override
    public void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) {
            event.accept(CreateContent.MILLSTONE_ITEM);
        }
    }

    @Override
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> resolveMillstoneItemHandler(level, pos, state),
                CreateContent.MILLSTONE.get(),
                CreateContent.MILLSTONE_STRUCTURAL.get()
        );
    }

    @Override
    public void registerPonderScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(CreateContent.MILLSTONE.getId(), "millstone", MillstonePonderScene::millstone);
    }

    @Override
    public RecipeSerializer<?> workstoneRecipeSerializer() {
        return new CreateWorkstoneRecipe.Serializer();
    }

    @Override
    public Optional<WorkstoneProcess> findSequencedStep(Level level, ItemStack stored, ItemStack tool) {
        RecipeWrapper wrapper = wrap(stored, tool);

        Optional<RecipeHolder<CreateWorkstoneRecipe>> workstoneStep = SequencedAssemblyRecipe.getRecipe(
                level, wrapper, workstoneRecipeType(), CreateWorkstoneRecipe.class);
        if (workstoneStep.isPresent()) {
            return Optional.of(workstoneStep.get().value());
        }

        if (ModConfig.get().useCreateDeployingRecipes) {
            Optional<RecipeHolder<DeployerApplicationRecipe>> deployingStep = SequencedAssemblyRecipe.getRecipe(
                    level, wrapper, AllRecipeTypes.DEPLOYING.getType(), DeployerApplicationRecipe.class);
            if (deployingStep.isPresent() && !ManualProcessingExclusions.isExcluded(deployingStep.get().id())) {
                return Optional.of(CreateWorkstoneProcess.of(deployingStep.get().value()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<WorkstoneProcess> findPressing(Level level, ItemStack stored, boolean toolIsHammer) {
        if (!ModConfig.get().useCreatePressingRecipes || !toolIsHammer) return Optional.empty();

        Optional<RecipeHolder<PressingRecipe>> pressingStep = SequencedAssemblyRecipe.getRecipe(
                level, stored, AllRecipeTypes.PRESSING.getType(), PressingRecipe.class);
        if (pressingStep.isPresent() && !ManualProcessingExclusions.isExcluded(pressingStep.get().id())) {
            return Optional.of(CreateWorkstoneProcess.of(pressingStep.get().value()));
        }

        Optional<RecipeHolder<PressingRecipe>> pressing =
                AllRecipeTypes.PRESSING.find(new SingleRecipeInput(stored), level);
        return pressing.filter(holder -> !ManualProcessingExclusions.isExcluded(holder.id()))
                .map(holder -> CreateWorkstoneProcess.of(holder.value()));
    }

    @Override
    public boolean hasAnyWorkstoneProcessFor(Level level, ItemStack stored) {
        if (ModConfig.get().useCreateDeployingRecipes) {
            for (RecipeHolder<?> holder : level.getRecipeManager().getAllRecipesFor(AllRecipeTypes.DEPLOYING.getType())) {
                if (holder.value() instanceof DeployerApplicationRecipe deployerRecipe
                        && !deployerRecipe.getIngredients().isEmpty()
                        && deployerRecipe.getIngredients().getFirst().test(stored)
                        && !ManualProcessingExclusions.isExcluded(holder.id())) {
                    return true;
                }
            }
        }

        if (ModConfig.get().useCreatePressingRecipes) {
            return AllRecipeTypes.PRESSING.find(new SingleRecipeInput(stored), level)
                    .filter(holder -> !ManualProcessingExclusions.isExcluded(holder.id()))
                    .isPresent();
        }

        return false;
    }

    @Override
    public boolean isSequencedAssemblyInProgress(ItemStack stack) {
        return stack.has(AllDataComponents.SEQUENCED_ASSEMBLY);
    }

    @Override
    public Optional<MortarProcess> findMortarGrinding(Level level, ItemStack candidate) {
        SingleRecipeInput input = new SingleRecipeInput(candidate);

        if (ModConfig.get().useCreateMillingRecipes) {
            Optional<RecipeHolder<MillingRecipe>> milling = AllRecipeTypes.MILLING.find(input, level);
            if (milling.isPresent() && !ManualProcessingExclusions.isExcluded(milling.get().id())) {
                return Optional.of(new CreateMortarProcess(milling.get().value()));
            }
        }

        if (ModConfig.get().useCreateCrushingRecipes) {
            Optional<RecipeHolder<CrushingRecipe>> crushing = AllRecipeTypes.CRUSHING.find(input, level);
            if (crushing.isPresent() && !ManualProcessingExclusions.isExcluded(crushing.get().id())) {
                return Optional.of(new CreateMortarProcess(crushing.get().value()));
            }
        }

        return Optional.empty();
    }

    @Override
    public void addMortarGrindingRecipes(RecipeManager manager, List<RecipeHolder<Recipe<?>>> out) {
        if (ModConfig.get().useCreateMillingRecipes) {
            manager.getAllRecipesFor(AllRecipeTypes.MILLING.<RecipeInput, MillingRecipe>getType())
                    .forEach(r -> {
                        if (!ManualProcessingExclusions.isExcluded(r.id()))
                            out.add(new RecipeHolder<>(r.id(), r.value()));
                    });
        }
        if (ModConfig.get().useCreateCrushingRecipes) {
            manager.getAllRecipesFor(AllRecipeTypes.CRUSHING.<RecipeInput, CrushingRecipe>getType())
                    .forEach(r -> {
                        if (!ManualProcessingExclusions.isExcluded(r.id()))
                            out.add(new RecipeHolder<>(r.id(), r.value()));
                    });
        }
    }

    @Override
    public void addMortarMixingRecipes(RecipeManager manager, List<RecipeHolder<Recipe<?>>> out) {
        if (!ModConfig.get().useCreateMixingRecipes) return;

        manager.getAllRecipesFor(AllRecipeTypes.MIXING.<RecipeInput, MixingRecipe>getType()).forEach(r -> {
            MixingRecipe recipe = r.value();
            if (!recipe.getRequiredHeat().testBlazeBurner(HeatLevel.KINDLED)) return;
            if (recipe.getFluidIngredients().size() > 1) return;
            if (ManualProcessingExclusions.isExcluded(r.id())) return;
            out.add(new RecipeHolder<>(r.id(), recipe));
        });
    }

    @Override
    public Optional<MortarProcess> describeMortarRecipe(Recipe<?> recipe) {
        if (recipe instanceof AbstractCrushingRecipe || recipe instanceof MixingRecipe) {
            return Optional.of(new CreateMortarProcess((ProcessingRecipe<?, ?>) recipe));
        }
        return Optional.empty();
    }

    @Override
    public Optional<MortarProcess> findMortarMixing(Level level, HeatCondition availableHeat, Predicate<MortarProcess> canRun) {
        if (!ModConfig.get().useCreateMixingRecipes) return Optional.empty();

        HeatLevel heat = asBlazeBurnerHeat(availableHeat);
        List<RecipeHolder<MixingRecipe>> mixingRecipes =
                level.getRecipeManager().getAllRecipesFor(AllRecipeTypes.MIXING.<RecipeInput, MixingRecipe>getType());
        for (RecipeHolder<MixingRecipe> holder : mixingRecipes) {
            MixingRecipe recipe = holder.value();
            if (!recipe.getRequiredHeat().testBlazeBurner(heat)) continue;
            if (ManualProcessingExclusions.isExcluded(holder.id())) continue;

            MortarProcess process = new CreateMortarProcess(recipe);
            if (canRun.test(process)) return Optional.of(process);
        }

        return Optional.empty();
    }

    @Override
    public HeatCondition getBlazeBurnerHeat(BlockState state) {
        if (!state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) return HeatCondition.NONE;

        return switch (state.getValue(BlazeBurnerBlock.HEAT_LEVEL)) {
            case NONE, SMOULDERING -> HeatCondition.NONE;
            case SEETHING -> HeatCondition.SUPERHEATED;
            default -> HeatCondition.HEATED;
        };
    }

    @Override
    public void addHeatSourceItems(List<ItemStack> out) {
        out.add(AllBlocks.BLAZE_BURNER.asStack());
    }

    @Override
    public boolean isBasin(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BasinBlockEntity;
    }

    @Override
    public LadleResult tryStirBasin(Level level, BlockPos pos, Player player, ItemStack ladle) {
        return LadleBasinInteraction.tryStir(level, pos, player, ladle);
    }

    @Override
    public void playGrindSound(ServerLevel level, double x, double y, double z, float volume, float pitch) {
        boolean primary = level.random.nextFloat() < 0.78F;
        (primary ? AllSoundEvents.CRUSHING_1 : AllSoundEvents.CRUSHING_2)
                .play(level, null, x, y, z, volume, pitch);
    }

    @Override
    public void playMixSound(ServerLevel level, double x, double y, double z, float volume, float pitch) {
        AllSoundEvents.MIXING.play(level, null, x, y, z, volume, pitch);
    }

    @Override
    public ParticleOptions fluidParticle(FluidStack fluid) {
        return FluidFX.getFluidParticle(fluid);
    }

    @Override
    public boolean isFluidContainer(Level level, ItemStack stack) {
        return GenericItemEmptying.canItemBeEmptied(level, stack) || GenericItemFilling.canItemBeFilled(level, stack);
    }

    @Override
    public boolean tryEmptyItemIntoTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                        ItemStack heldItem, IFluidHandler tank) {
        if (!GenericItemEmptying.canItemBeEmptied(level, heldItem)) return false;

        Pair<FluidStack, ItemStack> simulated = GenericItemEmptying.emptyItem(level, heldItem, true);
        FluidStack fluidStack = simulated.getFirst();

        if (fluidStack.isEmpty()) return false;
        if (fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE)) return false;
        if (level.isClientSide) return true;

        ItemStack copyOfHeld = heldItem.copy();
        Pair<FluidStack, ItemStack> result = GenericItemEmptying.emptyItem(level, copyOfHeld, false);
        tank.fill(fluidStack, FluidAction.EXECUTE);

        if (!player.isCreative()) {
            if (copyOfHeld.isEmpty()) {
                player.setItemInHand(hand, result.getSecond());
            } else {
                player.setItemInHand(hand, copyOfHeld);
                player.getInventory().placeItemBackInInventory(result.getSecond());
            }
        }

        CreateIntegration.playTransferSound(level, pos, FluidHelper.getEmptySound(fluidStack), tank);
        return true;
    }

    @Override
    public boolean tryFillItemFromTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                       ItemStack heldItem, IFluidHandler tank) {
        if (!GenericItemFilling.canItemBeFilled(level, heldItem)) return false;

        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluid = tank.getFluidInTank(i).copy();
            if (fluid.isEmpty()) continue;

            int requiredAmount = GenericItemFilling.getRequiredAmountForItem(level, heldItem, fluid.copy());
            if (requiredAmount == -1 || requiredAmount > fluid.getAmount()) continue;

            if (level.isClientSide) return true;

            ItemStack toFill = player.isCreative() ? heldItem.copy() : heldItem;
            ItemStack filled = GenericItemFilling.fillItem(level, requiredAmount, toFill, fluid.copy());

            tank.drain(FluidHelper.copyStackWithAmount(fluid, requiredAmount), FluidAction.EXECUTE);

            if (!player.isCreative()) {
                player.getInventory().placeItemBackInInventory(filled);
            }

            CreateIntegration.playTransferSound(level, pos, FluidHelper.getFillSound(fluid), tank);
            return true;
        }

        return false;
    }
}
