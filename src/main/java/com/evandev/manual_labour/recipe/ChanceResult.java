package com.evandev.manual_labour.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public record ChanceResult(ItemStack stack, float chance) {
    public static final Codec<ChanceResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.CODEC.fieldOf("result").forGetter(ChanceResult::stack),
            Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(ChanceResult::chance)
    ).apply(inst, ChanceResult::new));

    public ItemStack rollOutput(RandomSource random) {
        return chance >= 1.0F || random.nextFloat() < chance ? stack.copy() : ItemStack.EMPTY;
    }
}
