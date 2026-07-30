package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MillstoneStructuralBlock extends DirectionalBlock {
    public static final BooleanProperty CORNER = BooleanProperty.create("corner");
    public static final MapCodec<MillstoneStructuralBlock> CODEC = simpleCodec(MillstoneStructuralBlock::new);
    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    public MillstoneStructuralBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CORNER, false));
    }

    @Nullable
    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos cursor = pos;
        BlockState cursorState = state;
        for (int i = 0; i < 4; i++) {
            if (cursorState.getBlock() instanceof MillstoneBlock) {
                return cursor;
            }
            if (!(cursorState.getBlock() instanceof MillstoneStructuralBlock)) {
                return null;
            }
            cursor = cursor.relative(cursorState.getValue(FACING));
            cursorState = level.getBlockState(cursor);
        }
        return cursorState.getBlock() instanceof MillstoneBlock ? cursor : null;
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CORNER);
    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return BASE_SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.block();
    }

    public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
        return state.is(this) && getMaster(level, pos, state) != null;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        BlockPos master = getMaster(level, pos, state);
        if (master != null && !level.isClientSide) {
            level.destroyBlock(master, !player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos master = getMaster(level, pos, state);
            if (master != null) {
                level.destroyBlock(master, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (!stillValid(level, pos, state) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!stillValid(level, pos, state)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter level, @NotNull Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (entity.level().isClientSide || !(entity instanceof ItemEntity itemEntity) || !entity.isAlive()) {
            return;
        }
        BlockPos masterPos = getMaster(level, entity.blockPosition(), level.getBlockState(entity.blockPosition()));
        if (masterPos == null) {
            masterPos = getMaster(level, entity.blockPosition().below(), level.getBlockState(entity.blockPosition().below()));
        }
        if (masterPos != null && level.getBlockEntity(masterPos) instanceof MillstoneBlockEntity millstone) {
            millstone.tryInsertItemEntity(itemEntity);
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        BlockPos master = getMaster(level, pos, state);
        if (master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone) {
            ItemInteractionResult res = millstone.insertByHand(player, hand, stack);
            if (res.consumesAction()) {
                return res;
            }
            InteractionResult extractRes = millstone.extractByHand(player);
            if (extractRes.consumesAction()) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        BlockPos master = getMaster(level, pos, state);
        if (master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone) {
            return millstone.extractByHand(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(ModBlocks.MILLSTONE.get());
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }
}
