package com.evandev.manual_labour.compat.jei;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.CreateCompat;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public interface CategorySkin {

    String CREATE_SKIN = "com.evandev.manual_labour.compat.create.impl.jei.CreateCategorySkin";

    static void init(IGuiHelper guiHelper) {
        Holder.init(guiHelper);
    }

    static CategorySkin get() {
        return Holder.current;
    }

    IDrawable slot();

    IDrawable chanceSlot();

    void drawDownArrow(GuiGraphics graphics, int x, int y);

    void drawShadow(GuiGraphics graphics, int x, int y);

    void drawLongArrow(GuiGraphics graphics, int x, int y);

    void drawRepeatIcon(GuiGraphics graphics, int x, int y);

    void drawHeatBar(GuiGraphics graphics, int x, int y, boolean heated);

    final class Holder {
        private static CategorySkin current;

        private Holder() {
        }

        static void init(IGuiHelper guiHelper) {
            if (CreateCompat.isLoaded()) {
                try {
                    current = (CategorySkin) Class.forName(CREATE_SKIN).getDeclaredConstructor().newInstance();
                    return;
                } catch (ReflectiveOperationException | LinkageError e) {
                    Constants.LOG.error("Failed to load Create's JEI styling; falling back to the default look", e);
                }
            }
            current = new DefaultCategorySkin(guiHelper);
        }
    }
}
