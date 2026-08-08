package com.evandev.manual_labour.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Mirrors Create's {@code SequencedAssemblyRecipe.SequencedAssembly} component (MIT, Copyright (c)
 * simibubi).
 */
public record ManualAssemblyState(ResourceLocation id, int step) {

    public static final Codec<ManualAssemblyState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ManualAssemblyState::id),
            Codec.INT.fieldOf("step").forGetter(ManualAssemblyState::step)
    ).apply(instance, ManualAssemblyState::new));

    public static final StreamCodec<ByteBuf, ManualAssemblyState> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ManualAssemblyState::id,
            ByteBufCodecs.VAR_INT, ManualAssemblyState::step,
            ManualAssemblyState::new
    );
}
