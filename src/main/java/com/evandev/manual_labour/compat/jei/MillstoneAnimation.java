package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import mezz.jei.api.gui.drawable.IDrawable;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class MillstoneAnimation implements IDrawable {
    private static final ILightingSettings LIGHTING = CustomLightingSettings.builder()
            .firstLightRotation(12.5f, -45.0f)
            .secondLightRotation(-20.0f, -50.0f)
            .build();
    private static final float SPIN_SPEED = 4.0F;
    private static final int SCALE = 22;

    private final MillstoneBlockEntity millstone =
            new MillstoneBlockEntity(BlockPos.ZERO, ModBlocks.MILLSTONE.get().defaultBlockState());

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        float angle = (AnimationTickHolder.getRenderTime() * SPIN_SPEED) % 360.0F;
        millstone.angle = angle;
        millstone.prevAngle = angle;

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, 100);

        GuiGameElement.of(ModBlocks.MILLSTONE.get().defaultBlockState(), millstone)
                .lighting(LIGHTING)
                .rotateBlock(22.5, 22.5, 0)
                .scale(SCALE)
                .render(graphics);

        poseStack.popPose();
    }
}
