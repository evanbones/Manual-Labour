package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MortarRenderer implements BlockEntityRenderer<MortarBlockEntity> {

    public MortarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MortarBlockEntity mortar, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        int posLong = (int) mortar.getBlockPos().asLong();

        List<ItemStack> stacks = new ArrayList<>();
        IItemHandler inventory = mortar.getItemHandler();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) stacks.add(stack);
        }

        int count = stacks.size();
        for (int i = 0; i < count; i++) {
            poseStack.pushPose();

            Vec3 offset = new Vec3(0.18D, 0.0D, 0.0D).yRot((float) (2 * Math.PI * i / count));
            poseStack.translate(0.5D + offset.x(), 0.6D, 0.5D + offset.z());
            poseStack.scale(0.4F, 0.4F, 0.4F);

            itemRenderer.renderStatic(stacks.get(i), ItemDisplayContext.GROUND, packedLight, packedOverlay,
                    poseStack, buffer, mortar.getLevel(), posLong);

            poseStack.popPose();
        }

        ItemStack activeTool = mortar.getActiveTool();
        if (mortar.isProcessing() && !activeTool.isEmpty()) {
            poseStack.pushPose();

            poseStack.translate(0.5D, 0.78D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(100.0F));
            poseStack.scale(0.6F, 0.6F, 0.6F);

            itemRenderer.renderStatic(activeTool, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                    poseStack, buffer, mortar.getLevel(), posLong);

            poseStack.popPose();
        }
    }
}
