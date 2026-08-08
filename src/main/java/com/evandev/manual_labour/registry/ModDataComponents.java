package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.recipe.ManualAssemblyState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ManualAssemblyState>> MANUAL_ASSEMBLY =
            DATA_COMPONENTS.registerComponentType("manual_assembly", builder -> builder
                    .persistent(ManualAssemblyState.CODEC)
                    .networkSynchronized(ManualAssemblyState.STREAM_CODEC));
}
