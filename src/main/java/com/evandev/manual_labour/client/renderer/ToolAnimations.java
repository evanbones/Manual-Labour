package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.client.ModToolModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class ToolAnimations {
    private static final float LADLE_STIR_SPEED = 6.0F;
    private static final float TOOL_PIVOT_X = 8.0F / 16.0F;
    private static final float TOOL_PIVOT_Y = 5.5F / 16.0F;
    private static final float TOOL_PIVOT_Z = 10.5F / 16.0F;

    private ToolAnimations() {
    }

    public static void renderLadleStirring(ItemStack tool, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = time * LADLE_STIR_SPEED;
        float rad = (float) Math.toRadians(angle);

        float dipY = Mth.sin(rad) * 0.025F;
        float pitch = 20.0F + Mth.sin(rad) * 6.0F;
        float roll = Mth.cos(rad) * 4.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.92D - dipY, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.translate(0.0D, 0.0D, 0.10D);
        poseStack.scale(1.1F, 1.1F, 1.1F);
        poseStack.translate(TOOL_PIVOT_X, TOOL_PIVOT_Y, TOOL_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
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
