package com.evandev.manual_labour.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Ported from Create's {@code EmptyBackground} (MIT, Copyright (c) simibubi).
 */
public record EmptyBackground(int width, int height) implements IDrawable {

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
    }
}
