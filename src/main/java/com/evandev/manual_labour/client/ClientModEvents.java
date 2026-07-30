package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.client.renderer.MillstoneRenderer;
import com.evandev.manual_labour.client.renderer.MortarRenderer;
import com.evandev.manual_labour.client.renderer.WorkstoneRenderer;
import com.evandev.manual_labour.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORKSTONE.get(), WorkstoneRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MORTAR.get(), MortarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MILLSTONE.get(), MillstoneRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModToolModels.LADLE);
        event.register(ModToolModels.PESTLE);
        event.register(MillstoneRenderer.RUNNER_MODEL);
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        MillstonePlayerRotationHandler.gameRenderFrame(event.getPartialTick());
    }
}
