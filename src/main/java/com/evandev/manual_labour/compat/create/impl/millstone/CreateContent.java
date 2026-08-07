package com.evandev.manual_labour.compat.create.impl.millstone;

import com.evandev.manual_labour.registry.ModBlockEntities;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CreateContent {

    public static final DeferredBlock<MillstoneBlock> MILLSTONE = ModBlocks.BLOCKS.register("millstone",
            () -> new MillstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
                    .strength(3.0F).pushReaction(PushReaction.BLOCK).noOcclusion()));

    public static final DeferredBlock<MillstoneStructuralBlock> MILLSTONE_STRUCTURAL = ModBlocks.BLOCKS.register("millstone_structural",
            () -> new MillstoneStructuralBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
                    .strength(3.0F).pushReaction(PushReaction.BLOCK).noOcclusion().noLootTable()));

    public static final DeferredItem<Item> MILLSTONE_ITEM = ModItems.ITEMS.register("millstone",
            () -> new MillstoneBlockItem(MILLSTONE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneBlockEntity>> MILLSTONE_BE =
            ModBlockEntities.BLOCK_ENTITIES.register("millstone",
                    () -> BlockEntityType.Builder.of(MillstoneBlockEntity::new, MILLSTONE.get()).build(null));

    private CreateContent() {
    }

    public static void register() {
    }
}
