package com.evandev.manual_labour;

import com.evandev.manual_labour.client.ClientConfigSetup;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.evandev.manual_labour.compat.create.BasinStirPayload;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.content.block.MillstoneBlock;
import com.evandev.manual_labour.content.block.MillstoneItemHandler;
import com.evandev.manual_labour.content.block.MillstoneStructuralBlock;
import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.*;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class ManualLabour {
    public ManualLabour(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

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
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> resolveMillstoneItemHandler(level, pos, state),
                ModBlocks.MILLSTONE.get(),
                ModBlocks.MILLSTONE_STRUCTURAL.get()
        );
    }

    @Nullable
    private static MillstoneItemHandler resolveMillstoneItemHandler(Level level, BlockPos pos, BlockState state) {
        BlockPos master;
        if (state.getBlock() instanceof MillstoneBlock) {
            master = pos;
        } else if (state.getBlock() instanceof MillstoneStructuralBlock) {
            master = MillstoneStructuralBlock.getMaster(level, pos, state);
        } else {
            return null;
        }
        if (master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone) {
            return new MillstoneItemHandler(millstone);
        }
        return null;
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                BasinStirPayload.TYPE,
                BasinStirPayload.STREAM_CODEC,
                (payload, context) -> BasinStirClientState.set(payload.pos(), payload.tool())
        );
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.WORKSTONE_ITEM);
            event.accept(ModItems.MORTAR_ITEM);
            event.accept(ModItems.MILLSTONE_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.FLINT_HAMMER);
            event.accept(ModItems.IRON_HAMMER);
            event.accept(ModItems.GOLDEN_HAMMER);
            event.accept(ModItems.DIAMOND_HAMMER);
            event.accept(ModItems.NETHERITE_HAMMER);
            event.accept(ModItems.PESTLE);
            event.accept(ModItems.LADLE);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModConfig.load();
        event.enqueueWork(() -> {
            Block millstone = ModBlocks.MILLSTONE.get();
            BlockStressValues.IMPACTS.register(millstone, () -> (double) MillstoneBlockEntity.STRESS_IMPACT);
            TooltipModifier.REGISTRY.register(millstone.asItem(), new KineticStats(millstone));
        });
    }
}
