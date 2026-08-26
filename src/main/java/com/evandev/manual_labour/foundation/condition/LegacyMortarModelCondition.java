package com.evandev.manual_labour.foundation.condition;

import com.evandev.manual_labour.config.ModConfig;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

public record LegacyMortarModelCondition() implements ICondition {
    public static final MapCodec<LegacyMortarModelCondition> CODEC = MapCodec.unit(LegacyMortarModelCondition::new);

    @Override
    public boolean test(IContext context) {
        return ModConfig.get().legacyMortarModel;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "legacy_mortar_model()";
    }
}
