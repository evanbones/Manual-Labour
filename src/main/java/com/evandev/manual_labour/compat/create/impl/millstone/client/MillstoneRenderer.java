package com.evandev.manual_labour.compat.create.impl.millstone.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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

public class MillstoneRenderer implements BlockEntityRenderer<MillstoneBlockEntity> {
    public static final ModelResourceLocation RUNNER_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/millstone/runner"));

    public MillstoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static BakedModel runnerModel() {
        return Minecraft.getInstance().getModelManager().getModel(RUNNER_MODEL);
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(MillstoneBlockEntity millstone) {
        return new AABB(millstone.getBlockPos()).inflate(1.0, 0.0, 1.0);
    }

    @Override
    public void render(MillstoneBlockEntity millstone, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = Mth.lerp(partialTicks, millstone.prevAngle, millstone.angle);
        int light = millstone.hasLevel()
                ? LevelRenderer.getLightColor(millstone.getLevel(), millstone.getBlockPos().above())
                : packedLight;

        poseStack.pushPose();
        poseStack.translate(0.5, -0.005, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, 0.0, -0.5);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(), buffer.getBuffer(RenderType.cutoutMipped()), null, runnerModel(),
                1.0F, 1.0F, 1.0F, light, packedOverlay
        );

        poseStack.popPose();
    }
}
