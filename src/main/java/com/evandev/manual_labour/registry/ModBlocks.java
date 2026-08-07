package com.evandev.manual_labour.registry;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.content.block.MortarBlock;
import com.evandev.manual_labour.content.block.WorkstoneBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final DeferredBlock<Block> WORKSTONE = BLOCKS.register("workstone",
            () -> new WorkstoneBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE)));

    public static final DeferredBlock<Block> MORTAR = BLOCKS.register("mortar",
            () -> new MortarBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE).noOcclusion()));
}
