package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.Constants;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DefaultCategorySkin implements CategorySkin {

    private static final ResourceLocation CHANCE_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/jei/chance_slot.png");
    private static final ResourceLocation WIDGETS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/jei/widgets.png");

    private static final int SHEET_WIDTH = 176;
    private static final int SHEET_HEIGHT = 64;

    private final IDrawable slot;
    private final IDrawable chanceSlot;
    private final IDrawable longArrow;
    private final IDrawable downArrow;
    private final IDrawable repeatIcon;
    private final IDrawable heatBar;
    private final IDrawable noHeatBar;

    public DefaultCategorySkin(IGuiHelper guiHelper) {
        this.slot = guiHelper.getSlotDrawable();
        this.chanceSlot = guiHelper.drawableBuilder(CHANCE_SLOT_TEXTURE, 0, 0, 18, 18)
                .setTextureSize(18, 18)
                .build();

        this.longArrow = sprite(guiHelper, 0, 0, 71, 10);
        this.downArrow = sprite(guiHelper, 72, 0, 18, 14);
        this.repeatIcon = sprite(guiHelper, 92, 0, 16, 16);
        this.heatBar = sprite(guiHelper, 0, 20, 169, 19);
        this.noHeatBar = sprite(guiHelper, 0, 40, 169, 19);
    }

    private static IDrawable sprite(IGuiHelper guiHelper, int u, int v, int width, int height) {
        return guiHelper.drawableBuilder(WIDGETS_TEXTURE, u, v, width, height)
                .setTextureSize(SHEET_WIDTH, SHEET_HEIGHT)
                .build();
    }

    @Override
    public IDrawable slot() {
        return slot;
    }

    @Override
    public IDrawable chanceSlot() {
        return chanceSlot;
    }

    @Override
    public void drawDownArrow(GuiGraphics graphics, int x, int y) {
        downArrow.draw(graphics, x, y);
    }

    @Override
    public void drawShadow(GuiGraphics graphics, int x, int y) {
    }

    @Override
    public void drawLongArrow(GuiGraphics graphics, int x, int y) {
        longArrow.draw(graphics, x, y);
    }

    @Override
    public void drawRepeatIcon(GuiGraphics graphics, int x, int y) {
        repeatIcon.draw(graphics, x, y);
    }

    @Override
    public void drawHeatBar(GuiGraphics graphics, int x, int y, boolean heated) {
        (heated ? heatBar : noHeatBar).draw(graphics, x, y);
    }
}
