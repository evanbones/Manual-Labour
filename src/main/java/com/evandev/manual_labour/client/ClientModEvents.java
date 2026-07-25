package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.client.renderer.WorkstoneRenderer;
import com.evandev.manual_labour.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORKSTONE.get(), WorkstoneRenderer::new);
    }
}
