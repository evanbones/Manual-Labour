package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.client.renderer.ToolAnimations;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class BasinStirRenderer {
    private BasinStirRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (BasinStirClientState.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (Map.Entry<BlockPos, BasinStirClientState.StirState> entry : BasinStirClientState.stirStates().entrySet()) {
            BlockPos pos = entry.getKey();
            if (!CreateCompat.get().isBasin(level, pos)) continue;

            BasinStirClientState.StirState state = entry.getValue();
            if (state.tool.isEmpty()) continue;

            int packedLight = LevelRenderer.getLightColor(level, pos);
            float interpolatedAngle = state.getInterpolatedAngle(partialTick);

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            ToolAnimations.renderLadleStirring(state.tool, interpolatedAngle, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        buffer.endBatch();
    }
}
