package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.MillstoneBlock;
import com.evandev.manual_labour.content.block.MillstoneStructuralBlock;
import com.evandev.manual_labour.content.block.MortarBlock;
import com.evandev.manual_labour.content.block.WorkstoneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final DeferredBlock<Block> WORKSTONE = BLOCKS.register("workstone",
            () -> new WorkstoneBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE)));

    public static final DeferredBlock<Block> MORTAR = BLOCKS.register("mortar",
            () -> new MortarBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE).noOcclusion()));

    public static final DeferredBlock<MillstoneBlock> MILLSTONE = BLOCKS.register("millstone",
            () -> new MillstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
                    .strength(3.0F).pushReaction(PushReaction.BLOCK).noOcclusion()));

    public static final DeferredBlock<MillstoneStructuralBlock> MILLSTONE_STRUCTURAL = BLOCKS.register("millstone_structural",
            () -> new MillstoneStructuralBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE)
                    .strength(3.0F).pushReaction(PushReaction.BLOCK).noOcclusion().noLootTable()));
}
