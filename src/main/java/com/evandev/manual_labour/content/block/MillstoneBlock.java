package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlock extends BaseEntityBlock {
    public static final MapCodec<MillstoneBlock> CODEC = simpleCodec(MillstoneBlock::new);

    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new MillstoneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MILLSTONE.get(),
                level.isClientSide ? MillstoneBlockEntity::clientTick : MillstoneBlockEntity::serverTick);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
            if (!level.getBlockState(pos.offset(offset)).canBeReplaced()) {
                return null;
            }
        }
        return defaultBlockState();
    }

    @Override
    protected void onPlace(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull net.minecraft.core.Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (level instanceof Level realLevel && !realLevel.isClientSide && !realLevel.getBlockTicks().hasScheduledTick(pos, this)) {
            realLevel.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        for (BlockPos offset : MillstoneStructure.BASE_OFFSETS) {
            BlockPos target = pos.offset(offset);
            BlockState expected = ModBlocks.MILLSTONE_STRUCTURAL.get().defaultBlockState()
                    .setValue(MillstoneStructuralBlock.FACING, MillstoneStructure.baseFacing(offset))
                    .setValue(MillstoneStructuralBlock.TOP, false)
                    .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(offset));
            if (!placePiece(level, pos, target, expected)) {
                return;
            }
        }

        BlockPos rotorPos = pos.offset(MillstoneStructure.ROTOR_OFFSET);
        if (!placePiece(level, pos, rotorPos, ModBlocks.MILLSTONE_ROTOR.get().defaultBlockState())) {
            return;
        }

        for (BlockPos offset : MillstoneStructure.TOP_OFFSETS) {
            BlockPos target = pos.offset(offset);
            BlockPos baseOffset = offset.below();
            BlockState expected = ModBlocks.MILLSTONE_STRUCTURAL.get().defaultBlockState()
                    .setValue(MillstoneStructuralBlock.FACING, MillstoneStructure.baseFacing(baseOffset))
                    .setValue(MillstoneStructuralBlock.TOP, true)
                    .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(baseOffset));
            if (!placePiece(level, pos, target, expected)) {
                return;
            }
        }
    }

    private boolean placePiece(ServerLevel level, BlockPos controllerPos, BlockPos target, BlockState expected) {
        BlockState current = level.getBlockState(target);
        if (current == expected) {
            return true;
        }
        if (current.is(expected.getBlock()) || current.canBeReplaced()) {
            level.setBlockAndUpdate(target, expected);
            return true;
        }
        level.destroyBlock(controllerPos, true);
        return false;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
            return millstone.insertByHand(player, hand, stack);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
            return millstone.extractByHand(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
                millstone.dropBuffers();
            }
            for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
                BlockPos target = pos.offset(offset);
                BlockState piece = level.getBlockState(target);
                if (piece.is(ModBlocks.MILLSTONE_STRUCTURAL.get()) || piece.is(ModBlocks.MILLSTONE_ROTOR.get())) {
                    level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
