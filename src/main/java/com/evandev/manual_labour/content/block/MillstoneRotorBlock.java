package com.evandev.manual_labour.content.block;

import com.evandev.manual_labour.content.block.entity.MillstoneRotorBlockEntity;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MillstoneRotorBlock extends KineticBlock implements IBE<MillstoneRotorBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(3.0, 8.0, 3.0, 13.0, 10.0, 13.0),
            Block.box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0)
    );

    public static final MapCodec<MillstoneRotorBlock> CODEC = simpleCodec(MillstoneRotorBlock::new);

    public MillstoneRotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
    public Class<MillstoneRotorBlockEntity> getBlockEntityClass() {
        return MillstoneRotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MillstoneRotorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.MILLSTONE_ROTOR.get();
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        BlockPos master = pos.below();
        if (!level.isClientSide && level.getBlockState(master).getBlock() instanceof MillstoneBlock) {
            level.destroyBlock(master, !player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockPos master = pos.below();
            if (level.getBlockState(master).getBlock() instanceof MillstoneBlock) {
                level.destroyBlock(master, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.DOWN && !(neighborState.getBlock() instanceof MillstoneBlock)
                && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!(level.getBlockState(pos.below()).getBlock() instanceof MillstoneBlock)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, HitResult target, @NotNull LevelReader level, @NotNull BlockPos pos, Player player) {
        return new ItemStack(ModBlocks.MILLSTONE.get());
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        turnEntity(level, pos, pos, entity);
    }

    public static void turnEntity(Level level, BlockPos rotorPos, BlockPos standingPos, Entity entity) {
        if (!entity.onGround() || entity.getDeltaMovement().y > 0.0) {
            return;
        }
        if (entity.getY() < standingPos.getY() + 0.45) {
            return;
        }
        if (!(level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor)) {
            return;
        }
        if (rotor.isOverspeed()) {
            return;
        }
        float speed = rotor.getSpeed() * 3.0F / 10.0F;
        if (speed == 0.0F) {
            return;
        }
        if (level.isClientSide && entity instanceof Player) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            float diff = entity.getYHeadRot() - speed;
            living.setNoActionTime(20);
            living.setYBodyRot(diff);
            living.setYHeadRot(diff);
            entity.setOnGround(false);
            entity.hurtMarked = true;
        }

        entity.setYRot(entity.getYRot() - speed);

        Vec3 origin = new Vec3(rotorPos.getX() + 0.5, entity.getY(), rotorPos.getZ() + 0.5);
        Vec3 offset = entity.position().subtract(origin);
        offset = VecHelper.rotate(offset, Mth.clamp(speed, -16.0F, 16.0F), Direction.Axis.Y);
        Vec3 movement = origin.add(offset).subtract(entity.position());
        entity.setDeltaMovement(entity.getDeltaMovement().add(movement));
        entity.hurtMarked = true;
    }
}
