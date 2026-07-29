package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.entity.MillstoneRotorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class MillstoneRotorRenderer implements BlockEntityRenderer<MillstoneRotorBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/millstone/rotor"));

    public MillstoneRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static BakedModel model() {
        return Minecraft.getInstance().getModelManager().getModel(MODEL);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(MillstoneRotorBlockEntity rotor) {
        return new AABB(rotor.getBlockPos()).inflate(1);
    }

    @Override
    public void render(MillstoneRotorBlockEntity rotor, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = Mth.lerp(partialTicks, rotor.prevAngle, rotor.angle);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, 0.0, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), buffer.getBuffer(RenderType.solid()), null, model(),
                1.0F, 1.0F, 1.0F, packedLight, packedOverlay
        );

        poseStack.popPose();
    }
}
