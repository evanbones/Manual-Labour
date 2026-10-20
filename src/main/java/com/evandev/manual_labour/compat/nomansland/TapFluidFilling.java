package com.evandev.manual_labour.compat.nomansland;

import com.evandev.manual_labour.registry.ModTags;
import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.block.tap.TapBlock;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.blockentity.TapBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public final class TapFluidFilling {
    private static final ResourceLocation HONEY_CAULDRON = ResourceLocation.fromNamespaceAndPath("nomansland", "honey_cauldron");
    private static final int MAX_DROP = 3;

    public static void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TapBlock.CLOSED)) return;
        IFluidHandler receiver = findReceiver(level, pos);
        if (receiver == null) return;

        BlockState behind = TapBlock.getBlockStateBehind(level, pos, state);
        for (Holder.Reference<TapInteraction> reference : level.registryAccess().registryOrThrow(NMLRegistries.TAP_INTERACTION_KEY).holders().toList()) {
            TapInteraction interaction = reference.value();
            if (interaction.particleType().isEmpty()) continue;
            if (matches(interaction, behind, level, pos, state) && random.nextFloat() < 0.05F * interaction.rate()) {
                CauldronFluidContent content = CauldronFluidContent.getForBlock(interaction.cauldron());
                if (content != null && canFill(receiver, stepOf(content))) {
                    receiver.fill(stepOf(content), IFluidHandler.FluidAction.EXECUTE);
                }
                return;
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TapBlockEntity tap) {
        if (level.isClientSide || state.getValue(TapBlock.CLOSED)) return;
        IFluidHandler receiver = findReceiver(level, pos);
        if (receiver == null) return;

        BlockPos behindPos = pos.relative(state.getValue(TapBlock.FACING).getOpposite());
        BlockState behind = level.getBlockState(behindPos);

        if (behind.hasProperty(BlockStateProperties.WATERLOGGED) && behind.getValue(BlockStateProperties.WATERLOGGED)) {
            FluidStack water = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME / 3);
            if (canFill(receiver, water) && advance(tap)) receiver.fill(water, IFluidHandler.FluidAction.EXECUTE);
            return;
        }

        if (behind.hasProperty(BeehiveBlock.HONEY_LEVEL) && behind.getValue(BeehiveBlock.HONEY_LEVEL) == BeehiveBlock.MAX_HONEY_LEVELS) {
            CauldronFluidContent honey = CauldronFluidContent.getForBlock(BuiltInRegistries.BLOCK.get(HONEY_CAULDRON));
            if (honey == null || !canFill(receiver, stepOf(honey))) return;
            if (advance(tap)) {
                receiver.fill(stepOf(honey), IFluidHandler.FluidAction.EXECUTE);
                level.setBlockAndUpdate(behindPos, behind.setValue(BeehiveBlock.HONEY_LEVEL, 0));
                level.gameEvent(GameEvent.BLOCK_CHANGE, behindPos, GameEvent.Context.of(behind));
            }
            return;
        }

        if (behind.getBlock() instanceof AbstractCauldronBlock) {
            CauldronFluidContent content = CauldronFluidContent.getForBlock(behind.getBlock());
            if (content == null || content.currentLevel(behind) <= 0 || !canFill(receiver, stepOf(content))) return;
            if (advance(tap)) {
                receiver.fill(stepOf(content), IFluidHandler.FluidAction.EXECUTE);
                lowerLevel(level, behindPos, behind, content);
            }
        }
    }

    @Nullable
    private static IFluidHandler findReceiver(Level level, BlockPos tapPos) {
        for (int i = 0; i <= MAX_DROP; i++) {
            BlockPos below = tapPos.below(i);
            BlockState state = level.getBlockState(below);
            if (state.getBlock() instanceof AbstractCauldronBlock) return null;
            if (state.is(ModTags.Blocks.TAP_RECEIVERS)) {
                return level.getCapability(Capabilities.FluidHandler.BLOCK, below, Direction.UP);
            }
            if (state.isCollisionShapeFullBlock(level, below)) return null;
        }
        return null;
    }

    private static boolean matches(TapInteraction interaction, BlockState behind, ServerLevel level, BlockPos pos, BlockState state) {
        BlockPos behindPos = pos.relative(state.getValue(TapBlock.FACING).getOpposite());
        for (BlockStateProvider source : interaction.sources()) {
            if (behind != source.getState(level.random, behindPos)) continue;
            if (!behind.is(BlockTags.LOGS)) return true;
            return behind == TapBlock.getBlockStateBehind(level, pos.above(), state)
                    && behind == TapBlock.getBlockStateBehind(level, pos.below(), state);
        }
        return false;
    }

    private static boolean advance(TapBlockEntity tap) {
        if (++tap.timeEmptying <= NMLConfig.TICKS_TO_FILL_CAULDRON.get()) return false;
        tap.timeEmptying = 0;
        return true;
    }

    private static FluidStack stepOf(CauldronFluidContent content) {
        return new FluidStack(content.fluid, content.totalAmount / content.maxLevel);
    }

    private static boolean canFill(IFluidHandler receiver, FluidStack stack) {
        return receiver.fill(stack, IFluidHandler.FluidAction.SIMULATE) == stack.getAmount();
    }

    private static void lowerLevel(Level level, BlockPos pos, BlockState state, CauldronFluidContent content) {
        int remaining = content.currentLevel(state) - 1;
        BlockState lowered = remaining <= 0 || content.levelProperty == null
                ? Blocks.CAULDRON.defaultBlockState()
                : state.setValue(content.levelProperty, remaining);
        level.setBlockAndUpdate(pos, lowered);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(lowered));
    }
}
