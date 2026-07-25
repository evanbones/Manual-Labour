package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ModBlockLootProvider extends BlockLootSubProvider {

    protected ModBlockLootProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        this.dropSelf(ModBlocks.WORKSTONE.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return List.of(ModBlocks.WORKSTONE.get());
    }
}
