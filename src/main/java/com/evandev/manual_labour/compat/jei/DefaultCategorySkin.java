package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.Constants;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DefaultCategorySkin implements CategorySkin {

    private static final ResourceLocation CHANCE_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/jei/chance_slot.png");

    private final IDrawable slot;
    private final IDrawable chanceSlot;

    public DefaultCategorySkin(IGuiHelper guiHelper) {
        this.slot = guiHelper.getSlotDrawable();
        this.chanceSlot = guiHelper.drawableBuilder(CHANCE_SLOT_TEXTURE, 0, 0, 18, 18)
                .setTextureSize(18, 18)
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
    }

    @Override
    public void drawShadow(GuiGraphics graphics, int x, int y) {
    }
}
