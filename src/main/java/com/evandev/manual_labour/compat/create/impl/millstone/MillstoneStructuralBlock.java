package com.evandev.manual_labour.compat.create.impl.millstone;

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
    private static final int MAX_CHAIN_HOPS = 4;

    public MillstoneStructuralBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(CORNER, false));
    }

    @Nullable
    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        int hopsRemaining = MAX_CHAIN_HOPS;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        BlockState here = state;

        while (true) {
            Block block = here.getBlock();
            if (block instanceof MillstoneBlock) {
                return cursor.immutable();
            }
            if (hopsRemaining-- <= 0 || !(block instanceof MillstoneStructuralBlock)) {
                return null;
            }
            cursor.move(here.getValue(FACING));
            here = level.getBlockState(cursor);
        }
    }

    @Nullable
    private static MillstoneBlockEntity masterEntity(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos master = getMaster(level, pos, state);
        if (master == null) {
            return null;
        }
        return level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone ? millstone : null;
    }

    private static void collapseFrom(LevelAccessor level, BlockPos pos, BlockState state, boolean dropResources) {
        BlockPos controller = getMaster(level, pos, state);
        if (controller != null) {
            level.destroyBlock(controller, dropResources);
        }
    }

    private void scheduleValidation(LevelAccessor level, BlockPos pos) {
        if (level.getBlockTicks().hasScheduledTick(pos, this)) {
            return;
        }
        level.scheduleTick(pos, this, 1);
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
        if (!level.isClientSide) {
            collapseFrom(level, pos, state, !player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean replacedByDifferentBlock = !state.is(newState.getBlock());
        if (replacedByDifferentBlock) {
            collapseFrom(level, pos, state, true);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (!stillValid(level, pos, state)) {
            scheduleValidation(level, pos);
        }
        return state;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!stillValid(level, pos, state)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter level, @NotNull Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (entity.level().isClientSide || !entity.isAlive() || !(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        BlockPos landedOn = entity.blockPosition();
        MillstoneBlockEntity millstone = masterEntity(level, landedOn, level.getBlockState(landedOn));
        if (millstone == null) {
            BlockPos below = landedOn.below();
            millstone = masterEntity(level, below, level.getBlockState(below));
        }
        if (millstone != null) {
            millstone.tryInsertItemEntity(itemEntity);
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        return MillstoneBlock.offerHeldItem(masterEntity(level, pos, state), player, hand, stack, level.isClientSide);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        MillstoneBlockEntity millstone = masterEntity(level, pos, state);
        return millstone == null ? InteractionResult.PASS : millstone.extractByHand(player);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(CreateContent.MILLSTONE.get());
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }
}
