package com.evandev.manual_labour.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import net.createmod.catnip.gui.ILightingSettings;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.block.state.BlockState;

public class JeiBlockIcon {
    private static final ILightingSettings LIGHTING = CustomLightingSettings.builder()
            .firstLightRotation(12.5f, -45.0f)
            .secondLightRotation(-20.0f, -50.0f)
            .build();

    public static void draw(GuiGraphics graphics, BlockState state, int x, int y, float scale) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(x, y, 100);
        ms.mulPose(Axis.XP.rotationDegrees(-15.5f));
        ms.mulPose(Axis.YP.rotationDegrees(22.5f));

        GuiGameElement.of(state)
                .lighting(LIGHTING)
                .scale(scale)
                .render(graphics);

        ms.popPose();
    }
}
