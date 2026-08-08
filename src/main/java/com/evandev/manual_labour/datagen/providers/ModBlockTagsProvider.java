package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.compat.create.impl.millstone.CreateContent;
import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModBlocks;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.WORKSTONE.get())
                .add(ModBlocks.MORTAR.get())
                .addOptional(CreateContent.MILLSTONE.getId())
                .addOptional(CreateContent.MILLSTONE_STRUCTURAL.getId());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.WORKSTONE.get())
                .add(ModBlocks.MORTAR.get())
                .addOptional(CreateContent.MILLSTONE.getId())
                .addOptional(CreateContent.MILLSTONE_STRUCTURAL.getId());

        this.tag(ModTags.Blocks.HEAT_SOURCES)
                .addTag(BlockTags.CAMPFIRES)
                .addTag(BlockTags.FIRE)
                .add(Blocks.LAVA)
                .add(Blocks.MAGMA_BLOCK)
                .add(Blocks.FURNACE)
                .add(Blocks.BLAST_FURNACE)
                .add(Blocks.SMOKER)
                .addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "heat_sources"));
    }
}
