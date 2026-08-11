package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.caverns_and_chasms.CavernsAndChasmsCompat;
import com.evandev.manual_labour.content.item.HammerMaterial;
import com.evandev.manual_labour.registry.HammerMaterials;
import com.evandev.manual_labour.registry.ModItems;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModItemTagsProvider extends ItemTagsProvider {

    private static final List<ResourceLocation> COMPAT_HAMMER_IDS = Stream.concat(
                    HammerMaterials.COMPAT.stream().map(HammerMaterial::itemId),
                    CavernsAndChasmsCompat.COPPER_HAMMER_IDS.stream())
            .map(id -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id))
            .toList();

    private static final ResourceLocation SILVER_HAMMER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "silver_hammer");
    private static final ResourceLocation NECROMIUM_HAMMER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "necromium_hammer");
    private static final ResourceLocation ELECTRUM_HAMMER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "electrum_hammer");

    public ModItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, Constants.MOD_ID, existingFileHelper);
    }

    private static TagKey<Item> externalTag(String namespace, String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        hammerTag(this.tag(ItemTags.CLUSTER_MAX_HARVESTABLES))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        hammerTag(this.tag(ModTags.Items.HAMMERS))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        this.tag(ModTags.Items.PESTLES)
                .add(ModItems.PESTLE.get());

        this.tag(ModTags.Items.LADLES)
                .add(ModItems.LADLE.get());

        hammerTag(this.tag(Tags.Items.TOOLS))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get())
                .add(ModItems.PESTLE.get())
                .add(ModItems.LADLE.get());

        hammerTag(this.tag(ModTags.Items.TOOLS_HAMMER))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        hammerTag(this.tag(ItemTags.MINING_ENCHANTABLE))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        hammerTag(this.tag(ItemTags.MINING_LOOT_ENCHANTABLE))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get());

        hammerTag(this.tag(ItemTags.DURABILITY_ENCHANTABLE))
                .add(ModItems.FLINT_HAMMER.get())
                .add(ModItems.IRON_HAMMER.get())
                .add(ModItems.GOLDEN_HAMMER.get())
                .add(ModItems.DIAMOND_HAMMER.get())
                .add(ModItems.NETHERITE_HAMMER.get())
                .add(ModItems.PESTLE.get())
                .add(ModItems.LADLE.get());

        this.tag(externalTag("caverns_and_chasms", "magic_damage_items"))
                .addOptional(SILVER_HAMMER);

        this.tag(externalTag("caverns_and_chasms", "slowness_inflicting_items"))
                .addOptional(NECROMIUM_HAMMER);

        this.tag(externalTag("oreganized", "has_kinetic_damage"))
                .addOptional(ELECTRUM_HAMMER);
    }

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> hammerTag(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> appender) {
        COMPAT_HAMMER_IDS.forEach(appender::addOptional);
        return appender;
    }
}
