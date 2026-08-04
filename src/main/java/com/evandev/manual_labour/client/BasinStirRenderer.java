package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.client.renderer.ToolAnimations;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
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
        float time = level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (Map.Entry<BlockPos, ItemStack> entry : BasinStirClientState.activeStirs().entrySet()) {
            BlockPos pos = entry.getKey();
            if (!(level.getBlockEntity(pos) instanceof BasinBlockEntity)) continue;

            int packedLight = LevelRenderer.getLightColor(level, pos);

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            ToolAnimations.renderLadleStirring(entry.getValue(), time, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }

        buffer.endBatch();
    }
}
