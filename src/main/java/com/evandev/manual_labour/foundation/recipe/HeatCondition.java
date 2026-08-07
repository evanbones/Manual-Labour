package com.evandev.manual_labour.foundation.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The heat requirement of a processing recipe.
 * <p>
 * Mirrors Create's {@code com.simibubi.create.content.processing.recipe.HeatCondition} (MIT,
 * Copyright (c) simibubi).
 */
public enum HeatCondition implements StringRepresentable {
    NONE,
    HEATED,
    SUPERHEATED;

    public static final Codec<HeatCondition> CODEC = StringRepresentable.fromEnum(HeatCondition::values);

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getTranslationKey() {
        return "recipe.heat_requirement." + getSerializedName();
    }
}
