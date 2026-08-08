package com.evandev.manual_labour.compat.create.impl.jei;

import com.evandev.manual_labour.compat.jei.CategorySkin;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public class CreateCategorySkin implements CategorySkin {

    private static final IDrawable SLOT = asDrawable(AllGuiTextures.JEI_SLOT);
    private static final IDrawable CHANCE_SLOT = asDrawable(AllGuiTextures.JEI_CHANCE_SLOT);

    private static IDrawable asDrawable(AllGuiTextures texture) {
        return new IDrawable() {
            @Override
            public int getWidth() {
                return texture.getWidth();
            }

            @Override
            public int getHeight() {
                return texture.getHeight();
            }

            @Override
            public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
                texture.render(graphics, xOffset, yOffset);
            }
        };
    }

    @Override
    public IDrawable slot() {
        return SLOT;
    }

    @Override
    public IDrawable chanceSlot() {
        return CHANCE_SLOT;
    }

    @Override
    public void drawDownArrow(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, x, y);
    }

    @Override
    public void drawShadow(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.JEI_SHADOW.render(graphics, x, y);
    }

    @Override
    public void drawLongArrow(GuiGraphics graphics, int x, int y) {
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, x, y);
    }

    @Override
    public void drawRepeatIcon(GuiGraphics graphics, int x, int y) {
        AllIcons.I_SEQ_REPEAT.render(graphics, x, y);
    }

    @Override
    public void drawHeatBar(GuiGraphics graphics, int x, int y, boolean heated) {
        (heated ? AllGuiTextures.JEI_HEAT_BAR : AllGuiTextures.JEI_NO_HEAT_BAR).render(graphics, x, y);
    }
}
