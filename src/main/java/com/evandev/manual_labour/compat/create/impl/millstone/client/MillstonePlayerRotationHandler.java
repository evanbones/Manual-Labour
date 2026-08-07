package com.evandev.manual_labour.compat.create.impl.millstone.client;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlock;
import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneStructuralBlock;
import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MillstonePlayerRotationHandler {

    public static void gameRenderFrame(DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.isPaused()) {
            return;
        }
        if (!mc.player.onGround()) {
            return;
        }

        BlockPos pos = mc.player.getOnPos();
        BlockState state = mc.level.getBlockState(pos);
        BlockPos masterPos = null;

        if (state.getBlock() instanceof MillstoneBlock) {
            masterPos = pos;
        } else if (state.getBlock() instanceof MillstoneStructuralBlock) {
            masterPos = MillstoneStructuralBlock.getMaster(mc.level, pos, state);
        }

        if (masterPos == null) {
            return;
        }

        BlockEntity be = mc.level.getBlockEntity(masterPos);
        if (!(be instanceof MillstoneBlockEntity millstone)) {
            return;
        }

        float tickSpeed = mc.level.tickRateManager().tickrate() / 20.0F;
        float speedPerTick = millstone.getSpeed() * (3.0F / 10.0F) * tickSpeed;

        if (speedPerTick == 0.0F || millstone.isOverspeed()) {
            return;
        }

        float yRotOffset = speedPerTick * deltaTracker.getRealtimeDeltaTicks();
        mc.player.setYRot(mc.player.getYRot() - yRotOffset);
        mc.player.yBodyRot -= yRotOffset;
    }
}
