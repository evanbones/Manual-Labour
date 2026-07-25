package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.content.block.WorkstoneBlock;
import com.evandev.manual_labour.content.block.entity.WorkstoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WorkstoneRenderer implements BlockEntityRenderer<WorkstoneBlockEntity> {

    public WorkstoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WorkstoneBlockEntity workstone, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack itemStack = workstone.getStoredItem();
        if (itemStack.isEmpty()) {
            return;
        }

        Direction direction = workstone.getBlockState().getValue(WorkstoneBlock.FACING).getOpposite();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.pushPose();
        boolean isBlockItem = itemRenderer.getModel(itemStack, workstone.getLevel(), null, 0)
                .applyTransform(ItemDisplayContext.FIXED, poseStack, false).isGui3d();
        poseStack.popPose();

        poseStack.pushPose();

        if (isBlockItem) {
            poseStack.translate(0.5D, 0.96D, 0.5D);
            float f = -direction.toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(f));
            poseStack.scale(0.65F, 0.65F, 0.65F);
        } else {
            poseStack.translate(0.5D, 0.77D, 0.5D);
            float f = -direction.toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(f));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }

        int posLong = (int) workstone.getBlockPos().asLong();
        itemRenderer.renderStatic(
                itemStack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                poseStack, buffer, workstone.getLevel(), posLong
        );

        poseStack.popPose();
    }
}
