package com.evandev.manual_labour.compat.create.impl.millstone;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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

    private static final VoxelShape BASE_SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    private static final int ASSEMBLY_DELAY = 1;

    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    private static BlockState ringStateFor(BlockPos offset) {
        BlockState blank = CreateContent.MILLSTONE_STRUCTURAL.get().defaultBlockState();
        Direction inward = MillstoneStructure.baseFacing(offset);
        boolean diagonal = MillstoneStructure.isCorner(offset);
        return blank.setValue(MillstoneStructuralBlock.FACING, inward)
                .setValue(MillstoneStructuralBlock.CORNER, diagonal);
    }

    private static boolean claimRingCell(ServerLevel level, BlockPos ringPos, BlockState desired) {
        BlockState occupant = level.getBlockState(ringPos);
        if (occupant == desired) {
            return true;
        }
        boolean cellIsAvailable = occupant.is(desired.getBlock()) || occupant.canBeReplaced();
        if (!cellIsAvailable) {
            return false;
        }
        level.setBlockAndUpdate(ringPos, desired);
        return true;
    }

    private static void disassembleRing(Level level, BlockPos pos) {
        MillstoneStructuralBlock structural = CreateContent.MILLSTONE_STRUCTURAL.get();
        for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
            BlockPos ringPos = pos.offset(offset);
            if (level.getBlockState(ringPos).is(structural)) {
                level.setBlockAndUpdate(ringPos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    static ItemInteractionResult offerHeldItem(@Nullable MillstoneBlockEntity millstone, Player player,
                                               InteractionHand hand, ItemStack stack, boolean clientSide) {
        if (millstone == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        ItemInteractionResult insertion = millstone.insertByHand(player, hand, stack);
        if (insertion.consumesAction()) {
            return insertion;
        }
        if (millstone.extractByHand(player).consumesAction()) {
            return ItemInteractionResult.sidedSuccess(clientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    private static MillstoneBlockEntity lookup(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone ? millstone : null;
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

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
        return CreateContent.MILLSTONE_BE.get();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos origin = context.getClickedPos();
        Level level = context.getLevel();
        boolean footprintIsClear = MillstoneStructure.ALL_OFFSETS.stream()
                .map(origin::offset)
                .allMatch(ringPos -> level.getBlockState(ringPos).canBeReplaced());
        return footprintIsClear ? defaultBlockState() : null;
    }

    private void queueAssembly(LevelAccessor level, BlockPos pos) {
        if (level.getBlockTicks().hasScheduledTick(pos, this)) {
            return;
        }
        level.scheduleTick(pos, this, ASSEMBLY_DELAY);
    }

    @Override
    public void onPlace(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        queueAssembly(level, pos);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            queueAssembly(realLevel, pos);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        for (BlockPos offset : MillstoneStructure.ALL_OFFSETS) {
            if (claimRingCell(level, pos.offset(offset), ringStateFor(offset))) {
                continue;
            }
            level.destroyBlock(pos, true);
            return;
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        boolean stillAMillstone = state.is(newState.getBlock());
        if (!stillAMillstone) {
            MillstoneBlockEntity millstone = lookup(level, pos);
            if (millstone != null) {
                millstone.dropBuffers();
            }
            disassembleRing(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MillstoneBlockEntity millstone = level.getBlockEntity(pos) instanceof MillstoneBlockEntity be ? be : null;
        return offerHeldItem(millstone, player, hand, stack, level.isClientSide);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
            return millstone.extractByHand(player);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter level, @NotNull Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (entity.level().isClientSide || !entity.isAlive() || !(entity instanceof ItemEntity itemEntity)) {
            return;
        }
        BlockPos landedOn = entity.blockPosition();
        MillstoneBlockEntity millstone = lookup(level, landedOn);
        if (millstone == null) {
            millstone = lookup(level, landedOn.below());
        }
        if (millstone != null) {
            millstone.tryInsertItemEntity(itemEntity);
        }
    }
}
