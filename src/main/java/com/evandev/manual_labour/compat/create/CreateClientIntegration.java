package com.evandev.manual_labour.compat.create;

import net.minecraft.client.DeltaTracker;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public interface CreateClientIntegration {

    CreateClientIntegration ABSENT = new CreateClientIntegration() {
    };

    default void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    }

    default void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
    }

    default void onRenderFrame(DeltaTracker partialTick) {
    }
}
