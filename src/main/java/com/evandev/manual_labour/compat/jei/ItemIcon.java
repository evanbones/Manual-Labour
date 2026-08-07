package com.evandev.manual_labour.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * A category icon showing one item.
 * <p>
 * Ported from Create's {@code ItemIcon} (MIT, Copyright (c) simibubi).
 */
public class ItemIcon implements IDrawable {

    private final Supplier<ItemStack> stack;

    public ItemIcon(Supplier<ItemStack> stack) {
        this.stack = stack;
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
        graphics.renderItem(stack.get(), xOffset, yOffset);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack.get(), xOffset, yOffset);
    }
}
