package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.WorkstoneBlockEntity;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.evandev.manual_labour.Constants;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class WorkstoneBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final MapCodec<WorkstoneBlock> CODEC = simpleCodec(WorkstoneBlock::new);
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    public WorkstoneBlock(Properties properties) {
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
        if (!(level.getBlockEntity(pos) instanceof WorkstoneBlockEntity workstone)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack mainHandStack = player.getMainHandItem();

        if (!mainHandStack.isEmpty() && player.getCooldowns().isOnCooldown(mainHandStack.getItem())) {
            return ItemInteractionResult.CONSUME;
        }

        if (mainHandStack.isEmpty()) {
            if (workstone.isEmpty() || level.isClientSide) return ItemInteractionResult.CONSUME;
            ItemStack removedStack = workstone.removeItem();
            if (!player.isCreative()) player.getInventory().add(removedStack);
            Vec3 centerPos = pos.getCenter();
            level.playSound(null, centerPos.x(), centerPos.y(), centerPos.z(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.25F, 0.5F);
            return ItemInteractionResult.SUCCESS;
        }

        if (workstone.canAddItem(mainHandStack)) {
            if (level.isClientSide) return ItemInteractionResult.CONSUME;
            ItemStack remainderStack = workstone.addItem(player.getAbilities().instabuild ? mainHandStack.copy() : mainHandStack);
            if (!player.isCreative()) player.setItemSlot(EquipmentSlot.MAINHAND, remainderStack);
            Vec3 centerPos = pos.getCenter();
            level.playSound(null, centerPos.x(), centerPos.y(), centerPos.z(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            return ItemInteractionResult.SUCCESS;
        } else {
            if (workstone.processStoredItemUsingTool(mainHandStack, player)) {
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof WorkstoneBlockEntity workstone) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), workstone.getStoredItem());
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
        return ModBlockEntities.WORKSTONE.get().create(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof WorkstoneBlockEntity workstone)) {
            return 0;
        }
        ItemStack storedStack = workstone.getStoredItem();
        if (!storedStack.isEmpty()) {
            float proportion = (float) storedStack.getCount() / Math.min(workstone.getMaxStackSize(), storedStack.getMaxStackSize());
            return Mth.floor(proportion * 14.0F) + 1;
        }
        return 0;
    }

    @EventBusSubscriber(modid = Constants.MOD_ID)
    public static class ToolCarvingEvent {
        @SubscribeEvent
        public static void onSneakPlaceTool(PlayerInteractEvent.RightClickBlock event) {
            Level level = event.getLevel();
            BlockPos pos = event.getPos();

            if (!(level.getBlockEntity(pos) instanceof WorkstoneBlockEntity workstone)) {
                return;
            }

            Player player = event.getEntity();
            ItemStack heldStack = player.getMainHandItem();

            if (!player.isSecondaryUseActive() || heldStack.isEmpty()) {
                return;
            }

            if (workstone.carveToolOnWorkstone(player.getAbilities().instabuild ? heldStack.copy() : heldStack)) {
                if (!player.isCreative()) {
                    player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
                Vec3 centerPos = pos.getCenter();
                level.playSound(null, centerPos.x(), centerPos.y(), centerPos.z(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
