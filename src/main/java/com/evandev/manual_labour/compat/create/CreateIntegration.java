package com.evandev.manual_labour.compat.create;

import com.evandev.manual_labour.foundation.recipe.HeatCondition;
import com.evandev.manual_labour.recipe.MortarProcess;
import com.evandev.manual_labour.recipe.WorkstoneProcess;
import com.evandev.manual_labour.recipe.WorkstoneRecipe;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface CreateIntegration {

    @Nullable
    private static SoundEvent fluidSound(FluidStack fluid, boolean emptying) {
        return fluid.getFluidType().getSound(fluid, emptying ? SoundActions.BUCKET_EMPTY : SoundActions.BUCKET_FILL);
    }

    static void playTransferSound(Level level, BlockPos pos, @Nullable SoundEvent sound, IFluidHandler tank) {
        if (sound == null) return;

        float fillRatio = tank.getTankCapacity(0) <= 0 ? 0F
                : Mth.clamp(tank.getFluidInTank(0).getAmount() / (float) tank.getTankCapacity(0), 0F, 1F);
        float pitch = (1F - fillRatio) / 1.5F + 0.5F + (level.random.nextFloat() - 0.5F) / 4F;

        level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.5F, pitch);
    }

    default void registerContent() {
    }

    default void onCommonSetup() {
    }

    default void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
    }

    default void registerCapabilities(RegisterCapabilitiesEvent event) {
    }

    default void registerPonderScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
    }

    default RecipeSerializer<?> workstoneRecipeSerializer() {
        return new WorkstoneRecipe.Serializer();
    }

    default Optional<WorkstoneProcess> findSequencedStep(Level level, ItemStack stored, ItemStack tool) {
        return Optional.empty();
    }

    default Optional<WorkstoneProcess> findPressing(Level level, ItemStack stored, boolean toolIsHammer) {
        return Optional.empty();
    }

    default boolean hasAnyWorkstoneProcessFor(Level level, ItemStack stored) {
        return false;
    }

    default boolean isSequencedAssemblyInProgress(ItemStack stack) {
        return false;
    }

    default Optional<MortarProcess> findMortarGrinding(Level level, ItemStack candidate) {
        return Optional.empty();
    }

    default Optional<MortarProcess> describeMortarRecipe(Recipe<?> recipe) {
        return Optional.empty();
    }

    default void addMortarGrindingRecipes(RecipeManager manager, List<RecipeHolder<Recipe<?>>> out) {
    }

    default void addMortarMixingRecipes(RecipeManager manager, List<RecipeHolder<Recipe<?>>> out) {
    }

    default Optional<MortarProcess> findMortarMixing(Level level, HeatCondition availableHeat, Predicate<MortarProcess> canRun) {
        return Optional.empty();
    }

    default HeatCondition getBlazeBurnerHeat(BlockState state) {
        return HeatCondition.NONE;
    }

    default boolean isBasin(BlockGetter level, BlockPos pos) {
        return false;
    }

    default LadleResult tryStirBasin(Level level, BlockPos pos, Player player, ItemStack ladle) {
        return LadleResult.NOT_A_BASIN;
    }

    default void playGrindSound(ServerLevel level, double x, double y, double z, float volume, float pitch) {
        SoundEvent sound = level.random.nextFloat() < 0.78F ? SoundEvents.GRAVEL_BREAK : SoundEvents.STONE_HIT;
        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    default void playMixSound(ServerLevel level, double x, double y, double z, float volume, float pitch) {
        level.playSound(null, x, y, z, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, volume, pitch);
    }

    default ParticleOptions fluidParticle(FluidStack fluid) {
        return new BlockParticleOption(ParticleTypes.BLOCK, fluid.getFluid().defaultFluidState().createLegacyBlock());
    }

    default boolean isFluidContainer(Level level, ItemStack stack) {
        return FluidUtil.getFluidHandler(stack.copyWithCount(1)).isPresent();
    }

    default boolean tryEmptyItemIntoTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                         ItemStack held, IFluidHandler tank) {
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(held.copyWithCount(1)).orElse(null);
        if (handler == null) return false;

        FluidStack drainable = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        if (drainable.isEmpty()) return false;
        if (tank.fill(drainable, FluidAction.SIMULATE) != drainable.getAmount()) return false;
        if (level.isClientSide) return true;

        FluidActionResult result = FluidUtil.tryEmptyContainerAndStow(held, tank,
                new PlayerMainInvWrapper(player.getInventory()), drainable.getAmount(), player, true);
        if (!result.isSuccess()) return false;

        if (!player.isCreative()) player.setItemInHand(hand, result.getResult());
        playTransferSound(level, pos, fluidSound(drainable, true), tank);
        return true;
    }

    default boolean tryFillItemFromTank(Level level, BlockPos pos, Player player, InteractionHand hand,
                                        ItemStack held, IFluidHandler tank) {
        FluidStack available = tank.getFluidInTank(0);
        if (available.isEmpty()) return false;

        IFluidHandlerItem handler = FluidUtil.getFluidHandler(held.copyWithCount(1)).orElse(null);
        if (handler == null) return false;
        if (handler.fill(available.copy(), FluidAction.SIMULATE) <= 0) return false;
        if (level.isClientSide) return true;

        FluidActionResult result = FluidUtil.tryFillContainerAndStow(held, tank,
                new PlayerMainInvWrapper(player.getInventory()), Integer.MAX_VALUE, player, true);
        if (!result.isSuccess()) return false;

        if (!player.isCreative()) player.setItemInHand(hand, result.getResult());
        playTransferSound(level, pos, fluidSound(available, false), tank);
        return true;
    }

    enum LadleResult {
        NOT_A_BASIN,
        NOTHING_TO_MIX,
        STIRRING
    }
}
