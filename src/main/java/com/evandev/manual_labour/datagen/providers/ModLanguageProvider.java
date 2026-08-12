package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.caverns_and_chasms.CavernsAndChasmsCompat;
import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.compat.ponder.ManualLabourPonderPlugin;
import com.evandev.manual_labour.content.item.HammerMaterial;
import com.evandev.manual_labour.registry.HammerMaterials;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, Constants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("config.manual_labour.title", "Manual Labour Config");
        add("config.manual_labour.category.workstone", "Workstone");
        add("config.manual_labour.option.workstone_hammer_cooldown_ticks", "Workstone Hammer Cooldown (Ticks)");
        add("config.manual_labour.option.workstone_hammer_cooldown_ticks.tooltip", "Cooldown in ticks applied to tools when processing items on the Workstone. Set to 0 to disable.");
        add("manual_labour.workstone.remaining_items", "%s left...");
        add("config.manual_labour.category.mortar", "Mortar Tools");
        add("config.manual_labour.option.item_pile_y", "Item Pile Height");
        add("config.manual_labour.option.item_pile_y.tooltip", "Vertical position of items piled inside the Mortar, relative to the block's base.");
        add("config.manual_labour.option.item_pile_radius", "Item Pile Spread Radius");
        add("config.manual_labour.option.item_pile_radius.tooltip", "How far from the center items scatter when piled inside the Mortar.");
        add("config.manual_labour.option.item_float_sink_depth", "Item Float Sink Depth");
        add("config.manual_labour.option.item_float_sink_depth.tooltip", "How deep floating items sink into fluids inside the Mortar.");
        add("config.manual_labour.option.pestle_tip_contact_offset", "Pestle Contact Offset");
        add("config.manual_labour.option.pestle_tip_contact_offset.tooltip", "Vertical offset for where the Pestle's tip appears to touch the contents of the Mortar while grinding.");
        add("config.manual_labour.option.decorative_tool_y", "Placed Tool Height");
        add("config.manual_labour.option.decorative_tool_y.tooltip", "Vertical position of a Pestle or Ladle rested decoratively on the Mortar.");
        add("config.manual_labour.option.decorative_tool_side_offset", "Placed Tool Side Offset");
        add("config.manual_labour.option.decorative_tool_side_offset.tooltip", "How far to the side a rested Pestle or Ladle is offset from the Mortar's center.");
        add("config.manual_labour.option.decorative_tool_tilt", "Placed Tool Tilt");
        add("config.manual_labour.option.decorative_tool_tilt.tooltip", "Tilt angle, in degrees, of a Pestle or Ladle rested decoratively on the Mortar.");
        add("config.manual_labour.option.ladle_stir_speed", "Ladle Stirring Speed");
        add("config.manual_labour.option.ladle_stir_speed.tooltip", "Speed in degrees per tick at which the Ladle rotates while stirring in a Basin or Mortar.");
        add("config.manual_labour.option.instant_fluid_fill", "Instant Fluid Fill");
        add("config.manual_labour.option.instant_fluid_fill.tooltip", "Snap the Mortar's fluid level straight to its new height instead of animating it, matching how a vanilla Cauldron fills and drains.");

        add("config.manual_labour.category.create", "Create Compatibility");
        add("config.manual_labour.requires_create", "Requires the Create mod, which is not installed.");
        add("config.manual_labour.option.use_create_milling_recipes", "Use Create Milling Recipes");
        add("config.manual_labour.option.use_create_milling_recipes.tooltip", "Let the Mortar grind items using Create's Millstone recipes as a fallback when no Manual Labour grinding recipe matches. Disable to rely only on hand-authored mortar_grinding recipes.");
        add("config.manual_labour.option.use_create_crushing_recipes", "Use Create Crushing Recipes");
        add("config.manual_labour.option.use_create_crushing_recipes.tooltip", "Let the Mortar grind items using Create's Crushing Wheel recipes as a fallback when no Manual Labour grinding recipe matches. Disable to rely only on hand-authored mortar_grinding recipes.");
        add("config.manual_labour.option.use_create_mixing_recipes", "Use Create Mixing Recipes");
        add("config.manual_labour.option.use_create_mixing_recipes.tooltip", "Let the Mortar mix ingredients using Create's Mechanical Mixer recipes as a fallback when no Manual Labour mixing recipe matches. Disable to rely only on hand-authored mortar_mixing recipes.");
        add("config.manual_labour.option.use_create_deploying_recipes", "Use Create Deploying Recipes");
        add("config.manual_labour.option.use_create_deploying_recipes.tooltip", "Let the Workstone complete Create's Deployer steps within Sequenced Assembly recipes as a fallback when no Manual Labour workstone recipe matches.");
        add("config.manual_labour.option.use_create_pressing_recipes", "Use Create Pressing Recipes");
        add("config.manual_labour.option.use_create_pressing_recipes.tooltip", "Let the Workstone press items with a Hammer using Create's Mechanical Press recipes, including steps in Sequenced Assembly recipes.");

        add("block.manual_labour.workstone.invalid_item", "This doesn't seem hammerable...");
        add("block.manual_labour.workstone.invalid_tool", "Maybe with a different tool...");

        add("config.manual_labour.category.jei", "JEI Integration");
        add("config.manual_labour.option.enable_workstone_jei", "Enable Workstone JEI Category");
        add("config.manual_labour.option.enable_workstone_jei.tooltip", "Show the Workstone category in JEI.");
        add("config.manual_labour.option.enable_mortar_grinding_jei", "Enable Mortar Grinding JEI Category");
        add("config.manual_labour.option.enable_mortar_grinding_jei.tooltip", "Show the Mortar Grinding category in JEI.");
        add("config.manual_labour.option.enable_mortar_mixing_jei", "Enable Mortar Mixing JEI Category");
        add("config.manual_labour.option.enable_mortar_mixing_jei.tooltip", "Show the Mortar Mixing category in JEI.");
        add("config.manual_labour.option.enable_manual_assembly_jei", "Enable Manual Assembly JEI Category");
        add("config.manual_labour.option.enable_manual_assembly_jei.tooltip", "Show the Manual Assembly category in JEI.");
        add("config.manual_labour.option.enable_millstone_jei", "Enable Millstone JEI Category");
        add("config.manual_labour.option.enable_millstone_jei.tooltip", "Show the Millstone category in JEI.");
        add("config.manual_labour.option.enable_manual_pressing_jei", "Enable Manual Pressing JEI Category");
        add("config.manual_labour.option.enable_manual_pressing_jei.tooltip", "Show the Manual Pressing category in JEI.");

        add("config.manual_labour.category.ponder", "Ponder");
        add("config.manual_labour.option.enable_ponders", "Enable Ponder Scenes");
        add("config.manual_labour.option.enable_ponders.tooltip", "Disable to hide them from the Ponder menu and item tooltips. Requires a game restart to take effect.");

        addBlock(ModBlocks.WORKSTONE, "Workstone");
        addBlock(ModBlocks.MORTAR, "Mortar");
        addBlock(CreateContent.MILLSTONE, "Millstone");

        addItem(ModItems.FLINT_HAMMER, "Flint Hammer");
        addItem(ModItems.IRON_HAMMER, "Iron Hammer");
        addItem(ModItems.GOLDEN_HAMMER, "Golden Hammer");
        addItem(ModItems.DIAMOND_HAMMER, "Diamond Hammer");
        addItem(ModItems.NETHERITE_HAMMER, "Netherite Hammer");
        addItem(ModItems.PESTLE, "Pestle");
        addItem(ModItems.LADLE, "Ladle");

        for (HammerMaterial material : HammerMaterials.COMPAT) {
            add("item.manual_labour." + material.itemId(), capitalize(material.id()) + " Hammer");
        }
        for (String copperHammerId : CavernsAndChasmsCompat.COPPER_HAMMER_IDS) {
            add("item.manual_labour." + copperHammerId, capitalize(copperHammerId.replace("_hammer", "")) + " Hammer");
        }

        add("subtitles.manual_labour.block.workstone.hammer", "Workstone hammered");
        add("recipe.assembly.manual_labour.workstone", "Hit with %s");
        add("recipe.assembly.manual_labour.workstone.consume", "Apply %s");
        add("recipe.assembly.manual_labour.workstone.keep", "Use %s");
        add("recipe.assembly.manual_labour.workstone.invalid", "Invalid Step");
        add("manual_labour.ingredient.hammer", "a Hammer");

        add("manual_labour.recipe.assembly", "Recipe Sequence");
        add("manual_labour.recipe.assembly.progress", "Progress: %1$s/%2$s");
        add("manual_labour.recipe.assembly.next", "Next: %1$s");
        add("manual_labour.recipe.assembly.step", "Step %1$s:");
        add("manual_labour.recipe.assembly.junk", "Random salvage");
        add("manual_labour.recipe.assembly.repeat", "Repeat Sequence %1$s Times");

        add("manual_labour.recipe.heat_requirement.none", "No Heating Required");
        add("manual_labour.recipe.heat_requirement.heated", "Heated");
        add("manual_labour.recipe.heat_requirement.superheated", "Super-Heated");

        add("manual_labour.recipe.workstone", "Workstone");
        add("manual_labour.recipe.mortar_grinding", "Mortar Grinding");
        add("manual_labour.recipe.mortar_mixing", "Mortar Mixing");
        add("manual_labour.recipe.manual_assembly", "Manual Assembly");
        add("manual_labour.recipe.millstone", "Milling");
        add("manual_labour.recipe.manual_pressing", "Manual Pressing");
        add("manual_labour.recipe.processing.chance", "%1$s%% Chance");

        add("message.manual_labour.millstone_space", "Clear Blocks for Placement");
        add("subtitles.manual_labour.block.millstone.loop", "Millstone turns");
        add("subtitles.manual_labour.block.millstone.use", "Millstone grinds");
        add("manual_labour.millstone.too_fast", "Too Fast");

        providePonderLang();
    }

    private void providePonderLang() {
        PonderIndex.addPlugin(new ManualLabourPonderPlugin());
        PonderIndex.getLangAccess().provideLang(Constants.MOD_ID, this::add);
    }

    private static String capitalize(String id) {
        return Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
