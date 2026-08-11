package com.evandev.manual_labour;

import com.evandev.manual_labour.client.ClientConfigSetup;
import com.evandev.manual_labour.compat.caverns_and_chasms.CavernsAndChasmsCompat;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.evandev.manual_labour.compat.create.BasinStirPayload;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.recipe.ManualProcessingExclusions;
import com.evandev.manual_labour.registry.*;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class ManualLabour {
    public ManualLabour(IEventBus modEventBus, ModContainer modContainer) {
        CreateCompat.init();
        CavernsAndChasmsCompat.init();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        CreateCompat.get().registerContent();

        if (FMLEnvironment.dist.isClient()) {
            ClientConfigSetup.register(modContainer);
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.WORKSTONE.get(),
                (be, context) -> be.getInventory()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MORTAR.get(),
                (be, context) -> be.getItemHandler()
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MORTAR.get(),
                (be, context) -> be.getFluidHandler()
        );

        CreateCompat.get().registerCapabilities(event);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ManualProcessingExclusions());
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.WORKSTONE_ITEM);
            event.accept(ModItems.MORTAR_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FLINT_HAMMER);
            event.accept(ModItems.IRON_HAMMER);
            event.accept(ModItems.GOLDEN_HAMMER);
            event.accept(ModItems.DIAMOND_HAMMER);
            event.accept(ModItems.NETHERITE_HAMMER);
            event.accept(ModItems.PESTLE);
            event.accept(ModItems.LADLE);
            ModItems.COMPAT_HAMMERS.values().forEach(event::accept);
            CavernsAndChasmsCompat.copperHammers().forEach(event::accept);
        }

        CreateCompat.get().addCreativeTabItems(event);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                BasinStirPayload.TYPE,
                BasinStirPayload.STREAM_CODEC,
                (payload, context) -> BasinStirClientState.set(payload.pos(), payload.tool())
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModConfig.load();
        event.enqueueWork(() -> CreateCompat.get().onCommonSetup());
    }
}
