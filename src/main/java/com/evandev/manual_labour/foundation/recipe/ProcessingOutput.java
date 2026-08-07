package com.evandev.manual_labour.foundation.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * A recipe output with an optional roll chance.
 * <p>
 * Ported from Create's {@code com.simibubi.create.content.processing.recipe.ProcessingOutput}
 * (MIT, Copyright (c) simibubi).
 */
public class ProcessingOutput {

    public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

    public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), o -> o.item,
            ByteBufCodecs.INT, o -> o.count,
            DataComponentPatch.STREAM_CODEC, o -> o.patch,
            ByteBufCodecs.FLOAT, o -> o.chance,
            ProcessingOutput::new
    );

    private static final Codec<Either<Item, ResourceLocation>> ITEM_CODEC = Codec.either(
            BuiltInRegistries.ITEM.byNameCodec(),
            ResourceLocation.CODEC
    );

    private static final Codec<ProcessingOutput> CODEC_CURRENT = RecordCodecBuilder.create(i -> i.group(
            ITEM_CODEC.fieldOf("id").forGetter(o -> o.unresolved != null ? Either.right(o.unresolved) : Either.left(o.item)),
            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(o -> o.count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(o -> o.patch),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 1F).forGetter(o -> o.chance)
    ).apply(i, (item, count, components, chance) -> item.map(
            resolved -> new ProcessingOutput(resolved, count, components, chance),
            unresolved -> new ProcessingOutput(unresolved, count, chance)
    )));

    private static final Codec<ProcessingOutput> CODEC_LEGACY = RecordCodecBuilder.create(i -> i.group(
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").forGetter(ProcessingOutput::getStack),
            ExtraCodecs.intRange(1, 99).optionalFieldOf("count", 1).forGetter(o -> o.count),
            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("chance", 1F).forGetter(o -> o.chance)
    ).apply(i, (stack, count, chance) -> new ProcessingOutput(stack.getItem(), count, stack.getComponentsPatch(), chance)));

    public static final Codec<ProcessingOutput> CODEC = Codec.withAlternative(CODEC_CURRENT, CODEC_LEGACY);

    private final Item item;
    private final int count;
    private final DataComponentPatch patch;
    private final float chance;

    @Nullable
    private final ResourceLocation unresolved;

    public ProcessingOutput(ItemStack stack, float chance) {
        this(stack.getItem(), stack.getCount(), stack.getComponentsPatch(), chance);
    }

    public ProcessingOutput(Item item, int count, float chance) {
        this(item, count, DataComponentPatch.EMPTY, chance);
    }

    public ProcessingOutput(Item item, int count, DataComponentPatch patch, float chance) {
        this.item = item;
        this.count = count;
        this.patch = patch;
        this.chance = chance;
        this.unresolved = null;
    }

    public ProcessingOutput(ResourceLocation unresolved, int count, float chance) {
        this.item = Items.AIR;
        this.count = count;
        this.patch = DataComponentPatch.EMPTY;
        this.chance = chance;
        this.unresolved = unresolved;
    }

    private ItemStack getStack(int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!patch.isEmpty()) stack.applyComponents(patch);
        return stack;
    }

    public ItemStack getStack() {
        return getStack(count);
    }

    public float getChance() {
        return chance;
    }

    public ItemStack rollOutput(RandomSource random) {
        if (chance >= 1F) return getStack();

        int rolled = count;
        for (int roll = 0; roll < count; roll++) {
            if (random.nextFloat() > chance) rolled--;
        }
        return rolled == 0 ? ItemStack.EMPTY : getStack(rolled);
    }
}
