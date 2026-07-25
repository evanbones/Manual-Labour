package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Constants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("config.manual_labour.title", "Manual Labour Config");
        add("config.manual_labour.category.general", "General");
        add("config.manual_labour.option.enabled", "Enabled");

        addBlock(ModBlocks.WORKSTONE, "Workstone");

        addItem(ModItems.FLINT_HAMMER, "Flint Hammer");
        addItem(ModItems.IRON_HAMMER, "Iron Hammer");
        addItem(ModItems.GOLDEN_HAMMER, "Golden Hammer");
        addItem(ModItems.DIAMOND_HAMMER, "Diamond Hammer");
        addItem(ModItems.NETHERITE_HAMMER, "Netherite Hammer");

        add("subtitles.manual_labour.block.workstone.hammer", "Workstone hammered");
        add("emi.category.manual_labour.workstone", "Workstone");
    }
}
