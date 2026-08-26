package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.client.renderer.MortarRenderer;
import com.evandev.manual_labour.client.renderer.WorkstoneRenderer;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.compat.ponder.ManualLabourPonderPlugin;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModBlocks;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.WORKSTONE.get(), WorkstoneRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MORTAR.get(), MortarRenderer::new);
        CreateCompat.client().registerRenderers(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!ModConfig.get().enablePonders) return;
        PonderIndex.addPlugin(new ManualLabourPonderPlugin());
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModToolModels.LADLE);
        event.register(ModToolModels.PESTLE);
        event.register(MortarModelSwitcher.LEGACY);
        CreateCompat.client().registerAdditionalModels(event);
    }

    @SubscribeEvent
    public static void modifyMortarModel(ModelEvent.ModifyBakingResult event) {
        var mortarId = BuiltInRegistries.BLOCK.getKey(ModBlocks.MORTAR.get());
        event.getModels().replaceAll((location, model) ->
                location.id().equals(mortarId) ? new MortarModelSwitcher(model) : model);
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        CreateCompat.client().onRenderFrame(event.getPartialTick());
    }
}
