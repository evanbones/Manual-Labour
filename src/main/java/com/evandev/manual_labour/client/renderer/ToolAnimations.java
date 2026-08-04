package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.client.ModToolModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ToolAnimations {
    private static final float LADLE_STIR_SPEED = 6.0F;

    private static final float TOOL_PIVOT_X = 8.0F / 16.0F;
    private static final float TOOL_PIVOT_Y = 5.5F / 16.0F;
    private static final float TOOL_PIVOT_Z = 10.5F / 16.0F;

    private ToolAnimations() {
    }

    public static void renderLadleStirring(ItemStack tool, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = time * LADLE_STIR_SPEED;
        Vec3 offset = new Vec3(0.12D, 0.0D, 0.0D).yRot((float) Math.toRadians(angle));

        poseStack.pushPose();
        poseStack.translate(0.5D + offset.x(), 0.95D, 0.5D + offset.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.scale(1.1F, 1.1F, 1.1F);
        poseStack.translate(TOOL_PIVOT_X, TOOL_PIVOT_Y, TOOL_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
        poseStack.translate(-TOOL_PIVOT_X, -TOOL_PIVOT_Y, -TOOL_PIVOT_Z);

        renderToolModel(tool, ModToolModels.ladle(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    public static void renderToolModel(ItemStack tool, BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getItemRenderer().render(
                tool, ItemDisplayContext.NONE, false,
                poseStack, buffer, packedLight, packedOverlay, model
        );
    }
}
