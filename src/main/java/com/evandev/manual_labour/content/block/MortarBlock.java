package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModTags;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MortarBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final MapCodec<MortarBlock> CODEC = simpleCodec(MortarBlock::new);

    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D),
            Block.box(13.0D, 0.0D, 0.0D, 16.0D, 3.0D, 3.0D),
            Block.box(0.0D, 0.0D, 13.0D, 3.0D, 3.0D, 16.0D),
            Block.box(13.0D, 0.0D, 13.0D, 16.0D, 3.0D, 16.0D),
            Block.box(0.0D, 3.0D, 0.0D, 16.0D, 5.0D, 16.0D),
            Block.box(0.0D, 5.0D, 0.0D, 2.0D, 16.0D, 16.0D),
            Block.box(14.0D, 5.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            Block.box(2.0D, 5.0D, 0.0D, 14.0D, 16.0D, 2.0D),
            Block.box(2.0D, 5.0D, 14.0D, 14.0D, 16.0D, 16.0D)
    );

    public MortarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MortarBlockEntity mortar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(ModTags.Items.PESTLES) || stack.is(ModTags.Items.LADLES)) {
            if (player.isShiftKeyDown()) {
                if (level.isClientSide) return ItemInteractionResult.CONSUME;
                return mortar.placeDecorativeTool(stack, player.getAbilities().instabuild) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
            }

            if (stack.is(ModTags.Items.PESTLES)) {
                boolean started = mortar.startOrContinueGrind(player, stack);
                return started ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.CONSUME;
            }

            boolean started = mortar.startOrContinueMix(player, stack);
            return started ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.CONSUME;
        }

        if (!stack.isEmpty()) {
            if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (level.isClientSide) return ItemInteractionResult.SUCCESS;

            ItemStack remainder = mortar.insertFromPlayer(stack.copy());
            if (remainder.getCount() == stack.getCount()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            player.setItemInHand(hand, remainder);

            Vec3 centerPos = pos.getCenter();
            level.playSound(null, centerPos.x(), centerPos.y(), centerPos.z(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            return ItemInteractionResult.SUCCESS;
        }

        ItemStack decorativeTool = mortar.getDecorativeTool();

        if (player.isShiftKeyDown() && !decorativeTool.isEmpty()) {
            if (level.isClientSide) return ItemInteractionResult.CONSUME;
            ItemStack removedTool = mortar.removeDecorativeTool();
            if (!player.isCreative()) player.getInventory().add(removedTool);
            return ItemInteractionResult.SUCCESS;
        }

        if (!decorativeTool.isEmpty() && (decorativeTool.is(ModTags.Items.PESTLES) || decorativeTool.is(ModTags.Items.LADLES))) {
            if (level.isClientSide) return ItemInteractionResult.CONSUME;
            boolean success = decorativeTool.is(ModTags.Items.PESTLES)
                    ? mortar.startOrContinueGrind(player, decorativeTool)
                    : mortar.startOrContinueMix(player, decorativeTool);
            return success ? ItemInteractionResult.SUCCESS : ItemInteractionResult.CONSUME;
        }

        if (!mortar.hasItem()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.CONSUME;

        mortar.removeAllItems(player);
        Vec3 centerPos = pos.getCenter();
        level.playSound(null, centerPos.x(), centerPos.y(), centerPos.z(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.25F, 0.5F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void updateEntityAfterFallOn(@NotNull BlockGetter level, @NotNull Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (!(entity instanceof ItemEntity itemEntity) || !entity.isAlive()) return;
        if (!level.getBlockState(entity.blockPosition()).is(this)) return;
        if (level.getBlockEntity(entity.blockPosition()) instanceof MortarBlockEntity mortar) {
            mortar.tryInsertItemEntity(itemEntity);
        }
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MortarBlockEntity mortar) {
                mortar.dropContents(level, pos);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlockEntities.MORTAR.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.MORTAR.get(), MortarBlockEntity::serverTick);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MortarBlockEntity mortar)) {
            return 0;
        }

        int itemSignal = ItemHelper.calcRedstoneFromInventory(mortar.getItemHandler());

        FluidStack fluid = mortar.getFluidHandler().getFluidInTank(0);
        int fluidSignal = fluid.isEmpty() ? 0
                : Math.max(1, Mth.floor((float) fluid.getAmount() / MortarBlockEntity.TANK_CAPACITY * 14.0F) + 1);

        return Math.max(itemSignal, fluidSignal);
    }
}
