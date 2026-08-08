package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModItems;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(ItemTags.CLUSTER_MAX_HARVESTABLES)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ModTags.Items.HAMMERS)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ModTags.Items.PESTLES)
                .add(ModItems.PESTLE.get());

        this.tag(ModTags.Items.LADLES)
                .add(ModItems.LADLE.get());

        this.tag(Tags.Items.TOOLS)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get())
                .add(ModItems.PESTLE.get())
                .add(ModItems.LADLE.get());

        this.tag(ModTags.Items.TOOLS_HAMMER)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ItemTags.MINING_ENCHANTABLE)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get())
                .add(ModItems.PESTLE.get())
                .add(ModItems.LADLE.get());
    }
}
