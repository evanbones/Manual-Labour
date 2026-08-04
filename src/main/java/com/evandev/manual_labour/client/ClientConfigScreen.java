package com.evandev.manual_labour.client;

import com.evandev.manual_labour.config.ModConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientConfigScreen {
    public static Screen create(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.manual_labour.title"))
                .save(ModConfig::save);

        ConfigCategory.Builder mortar = ConfigCategory.createBuilder()
                .name(Component.translatable("config.manual_labour.category.mortar"))
                .option(createFloatOption("item_pile_y", 0.65F, 0.0F, 1.0F, 0.005F,
                        () -> ModConfig.get().itemPileY, val -> ModConfig.get().itemPileY = val))
                .option(createFloatOption("item_pile_radius", 0.3F, 0.0F, 0.5F, 0.005F,
                        () -> ModConfig.get().itemPileRadius, val -> ModConfig.get().itemPileRadius = val))
                .option(createFloatOption("pestle_tip_contact_offset", 0.3F, 0.0F, 1.5F, 0.005F,
                        () -> ModConfig.get().pestleTipContactOffset, val -> ModConfig.get().pestleTipContactOffset = val))
                .option(createFloatOption("decorative_tool_y", 0.7F, 0.0F, 1.0F, 0.005F,
                        () -> ModConfig.get().decorativeToolY, val -> ModConfig.get().decorativeToolY = val))
                .option(createFloatOption("decorative_tool_side_offset", 0.2F, 0.0F, 0.5F, 0.005F,
                        () -> ModConfig.get().decorativeToolSideOffset, val -> ModConfig.get().decorativeToolSideOffset = val))
                .option(createFloatOption("decorative_tool_tilt", 30.0F, 0.0F, 90.0F, 0.5F,
                        () -> ModConfig.get().decorativeToolTilt, val -> ModConfig.get().decorativeToolTilt = val));

        ConfigCategory.Builder create = ConfigCategory.createBuilder()
                .name(Component.translatable("config.manual_labour.category.create"))
                .option(createBoolOption("use_create_milling_recipes", true,
                        () -> ModConfig.get().useCreateMillingRecipes, val -> ModConfig.get().useCreateMillingRecipes = val))
                .option(createBoolOption("use_create_crushing_recipes", false,
                        () -> ModConfig.get().useCreateCrushingRecipes, val -> ModConfig.get().useCreateCrushingRecipes = val))
                .option(createBoolOption("use_create_mixing_recipes", true,
                        () -> ModConfig.get().useCreateMixingRecipes, val -> ModConfig.get().useCreateMixingRecipes = val))
                .option(createBoolOption("use_create_deploying_recipes", true,
                        () -> ModConfig.get().useCreateDeployingRecipes, val -> ModConfig.get().useCreateDeployingRecipes = val))
                .option(createBoolOption("use_create_pressing_recipes", true,
                        () -> ModConfig.get().useCreatePressingRecipes, val -> ModConfig.get().useCreatePressingRecipes = val));

        ConfigCategory.Builder jei = ConfigCategory.createBuilder()
                .name(Component.translatable("config.manual_labour.category.jei"))
                .option(createBoolOption("enable_workstone_jei", true,
                        () -> ModConfig.get().enableWorkstoneJei, val -> ModConfig.get().enableWorkstoneJei = val))
                .option(createBoolOption("enable_mortar_grinding_jei", true,
                        () -> ModConfig.get().enableMortarGrindingJei, val -> ModConfig.get().enableMortarGrindingJei = val))
                .option(createBoolOption("enable_mortar_mixing_jei", true,
                        () -> ModConfig.get().enableMortarMixingJei, val -> ModConfig.get().enableMortarMixingJei = val))
                .option(createBoolOption("enable_manual_assembly_jei", true,
                        () -> ModConfig.get().enableManualAssemblyJei, val -> ModConfig.get().enableManualAssemblyJei = val))
                .option(createBoolOption("enable_millstone_jei", true,
                        () -> ModConfig.get().enableMillstoneJei, val -> ModConfig.get().enableMillstoneJei = val))
                .option(createBoolOption("enable_manual_pressing_jei", true,
                        () -> ModConfig.get().enableManualPressingJei, val -> ModConfig.get().enableManualPressingJei = val));

        return builder.category(mortar.build()).category(create.build()).category(jei.build()).build().generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.manual_labour.option." + name))
                .description(OptionDescription.of(Component.translatable("config.manual_labour.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Float> createFloatOption(String name, float defaultValue, float min, float max, float step, Supplier<Float> getter, Consumer<Float> setter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.manual_labour.option." + name))
                .description(OptionDescription.of(Component.translatable("config.manual_labour.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }
}
