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
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WorkstoneRenderer implements BlockEntityRenderer<WorkstoneBlockEntity> {
    private final Random random = new Random();

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

        int modelCount = getModelCount(itemStack);
        int posLong = (int) workstone.getBlockPos().asLong();
        int seed = posLong + Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
        this.random.setSeed(seed);

        for (int i = 0; i < modelCount; i++) {
            poseStack.pushPose();

            float xOffset = modelCount == 1 ? 0 : (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            float zOffset = modelCount == 1 ? 0 : (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
            float yOffset = i * 0.03F;

            if (isBlockItem) {
                poseStack.translate(0.5D + xOffset, 0.96D + yOffset, 0.5D + zOffset);
                float f = -direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(f));
                poseStack.scale(0.65F, 0.65F, 0.65F);
            } else {
                poseStack.translate(0.5D + xOffset, 0.77D + yOffset, 0.5D + zOffset);
                float f = -direction.toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(f));
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.scale(0.5F, 0.5F, 0.5F);
            }

            itemRenderer.renderStatic(
                    itemStack, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffer, workstone.getLevel(), posLong + i
            );

            poseStack.popPose();
        }
    }

    protected int getModelCount(ItemStack stack) {
        int modelCount = 1;
        if (stack.getCount() > 1) {
            modelCount += Mth.ceil(((float) stack.getCount() / Math.min(64, stack.getMaxStackSize())) * 4);
        }
        return modelCount;
    }
}
