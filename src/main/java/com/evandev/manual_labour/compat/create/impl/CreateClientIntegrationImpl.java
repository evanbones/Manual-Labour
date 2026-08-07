package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.compat.create.CreateClientIntegration;
import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.compat.create.impl.millstone.client.MillstonePlayerRotationHandler;
import com.evandev.manual_labour.compat.create.impl.millstone.client.MillstoneRenderer;
import net.minecraft.client.DeltaTracker;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class CreateClientIntegrationImpl implements CreateClientIntegration {

    @Override
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateContent.MILLSTONE_BE.get(), MillstoneRenderer::new);
    }

    @Override
    public void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(MillstoneRenderer.RUNNER_MODEL);
    }

    @Override
    public void onRenderFrame(DeltaTracker partialTick) {
        MillstonePlayerRotationHandler.gameRenderFrame(partialTick);
    }
}
