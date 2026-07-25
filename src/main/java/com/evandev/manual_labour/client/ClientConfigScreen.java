package com.evandev.manual_labour.client;

import com.evandev.manual_labour.config.ModConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
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

        ConfigCategory.Builder general = ConfigCategory.createBuilder()
                .name(Component.translatable("config.manual_labour.category.general"))
                .option(createBoolOption("enabled", true, () -> ModConfig.get().enabled, val -> ModConfig.get().enabled = val));

        return builder.category(general.build()).build().generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.manual_labour.option." + name))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }
}
