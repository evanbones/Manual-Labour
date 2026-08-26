package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.foundation.condition.LegacyMortarModelCondition;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Constants.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<LegacyMortarModelCondition>> LEGACY_MORTAR_MODEL =
            CONDITION_SERIALIZERS.register("legacy_mortar_model", () -> LegacyMortarModelCondition.CODEC);
}
