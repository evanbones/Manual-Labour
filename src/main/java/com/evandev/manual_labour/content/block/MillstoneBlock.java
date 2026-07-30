package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlock extends KineticBlock implements IBE<MillstoneBlockEntity> {
    public static final MapCodec<MillstoneBlock> CODEC = simpleCodec(MillstoneBlock::new);

    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 8, 16);

    @Override
    protected @NotNull VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return BASE_SHAPE;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasShaftTowards(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Direction face) {
        return face == Direction.UP;
    }

    @Override
    public Direction.Axis getRotationAxis(@NotNull BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public Class<MillstoneBlockEntity> getBlockEntityClass() {
        return MillstoneBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MillstoneBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MILLSTONE.get();
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
    public void onPlace(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (level instanceof Level realLevel && !realLevel.isClientSide && !realLevel.getBlockTicks().hasScheduledTick(pos, this)) {
            realLevel.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        for (BlockPos offset : MillstoneStructure.BASE_OFFSETS) {
            BlockPos target = pos.offset(offset);
            BlockState expected = ModBlocks.MILLSTONE_STRUCTURAL.get().defaultBlockState()
                    .setValue(MillstoneStructuralBlock.FACING, MillstoneStructure.baseFacing(offset))
                    .setValue(MillstoneStructuralBlock.CORNER, MillstoneStructure.isCorner(offset));
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
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
                millstone.dropBuffers();
            }
            for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
                BlockPos target = pos.offset(offset);
                BlockState piece = level.getBlockState(target);
                if (piece.is(ModBlocks.MILLSTONE_STRUCTURAL.get())) {
                    level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
