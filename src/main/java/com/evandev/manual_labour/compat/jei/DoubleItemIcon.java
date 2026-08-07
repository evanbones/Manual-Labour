package com.evandev.manual_labour.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class DoubleItemIcon implements IDrawable {

    private final Supplier<ItemStack> primary;
    private final Supplier<ItemStack> secondary;

    public DoubleItemIcon(Supplier<ItemStack> primary, Supplier<ItemStack> secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = graphics.pose();

        pose.pushPose();
        pose.translate(xOffset + 3, yOffset + 3, 0);
        pose.scale(0.75F, 0.75F, 0.75F);
        graphics.renderItem(secondary.get(), 0, 0);
        graphics.renderItemDecorations(mc.font, secondary.get(), 0, 0);
        pose.popPose();

        pose.pushPose();
        pose.translate(xOffset - 2, yOffset - 2, 100);
        pose.scale(0.75F, 0.75F, 0.75F);
        graphics.renderItem(primary.get(), 0, 0);
        graphics.renderItemDecorations(mc.font, primary.get(), 0, 0);
        pose.popPose();
    }
}
