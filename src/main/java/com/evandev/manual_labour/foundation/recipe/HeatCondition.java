package com.evandev.manual_labour.foundation.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Mirrors Create's {@code com.simibubi.create.content.processing.recipe.HeatCondition} (MIT,
 * Copyright (c) simibubi).
 */
public enum HeatCondition implements StringRepresentable {
    NONE(0xFFFFFF),
    HEATED(0xE88300),
    SUPERHEATED(0x5C93E8);

    public static final Codec<HeatCondition> CODEC = StringRepresentable.fromEnum(HeatCondition::values);

    private final int color;

    HeatCondition(int color) {
        this.color = color;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "manual_labour.recipe.heat_requirement." + getSerializedName();
    }

    public int getColor() {
        return color;
    }

    public boolean test(HeatCondition available) {
        return available.ordinal() >= ordinal();
    }
}
