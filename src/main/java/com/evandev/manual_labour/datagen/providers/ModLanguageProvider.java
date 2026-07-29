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
        add("config.manual_labour.category.mortar", "Mortar Tools");
        add("config.manual_labour.option.item_pile_y", "Item Pile Height");
        add("config.manual_labour.option.item_pile_radius", "Item Pile Spread Radius");
        add("config.manual_labour.option.pestle_tip_contact_offset", "Pestle Contact Offset");
        add("config.manual_labour.option.decorative_tool_y", "Placed Tool Height");
        add("config.manual_labour.option.decorative_tool_side_offset", "Placed Tool Side Offset");
        add("config.manual_labour.option.decorative_tool_tilt", "Placed Tool Tilt");

        addBlock(ModBlocks.WORKSTONE, "Workstone");
        addBlock(ModBlocks.MORTAR, "Mortar");

        addItem(ModItems.FLINT_HAMMER, "Flint Hammer");
        addItem(ModItems.IRON_HAMMER, "Iron Hammer");
        addItem(ModItems.GOLDEN_HAMMER, "Golden Hammer");
        addItem(ModItems.DIAMOND_HAMMER, "Diamond Hammer");
        addItem(ModItems.NETHERITE_HAMMER, "Netherite Hammer");
        addItem(ModItems.PESTLE, "Pestle");
        addItem(ModItems.LADLE, "Ladle");

        add("subtitles.manual_labour.block.workstone.hammer", "Workstone hammered");
        add("recipe.assembly.manual_labour.workstone", "Hit with %s");

        add("manual_labour.recipe.workstone", "Workstone");
        add("manual_labour.recipe.mortar_grinding", "Mortar Grinding");
        add("manual_labour.recipe.mortar_mixing", "Mortar Mixing");
        add("manual_labour.recipe.manual_assembly", "Manual Assembly");
    }
}
