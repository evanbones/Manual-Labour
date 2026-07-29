package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER1 = SOUNDS.register("block.workstone.hammer1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block.workstone.hammer1")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER2 = SOUNDS.register("block.workstone.hammer2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block.workstone.hammer2")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_LOOP = SOUNDS.register("block.millstone.loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block.millstone.loop")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_USE = SOUNDS.register("block.millstone.use",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block.millstone.use")));
}
