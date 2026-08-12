package com.evandev.manual_labour.compat.create.impl;

import com.evandev.manual_labour.recipe.WorkstoneProcess.ToolUse;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public class WorkstoneParams extends ProcessingRecipeParams {

    public static final MapCodec<WorkstoneParams> CODEC = workstoneCodec(WorkstoneParams::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstoneParams> STREAM_CODEC = workstoneStreamCodec(WorkstoneParams::new);

    protected ToolUse toolUse = ToolUse.DAMAGE;

    public WorkstoneParams() {
        super();
    }

    public ToolUse toolUse() {
        return toolUse;
    }

    protected static <P extends WorkstoneParams> MapCodec<P> workstoneCodec(Supplier<P> factory) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ProcessingRecipeParams.codec(factory).forGetter(p -> p),
                ToolUse.CODEC.optionalFieldOf("tool_use", ToolUse.DAMAGE).forGetter(WorkstoneParams::toolUse)
        ).apply(instance, (params, toolUse) -> {
            params.toolUse = toolUse;
            return params;
        }));
    }

    protected static <P extends WorkstoneParams> StreamCodec<RegistryFriendlyByteBuf, P> workstoneStreamCodec(Supplier<P> factory) {
        return StreamCodec.of(
                (buffer, params) -> params.encode(buffer),
                buffer -> {
                    P params = factory.get();
                    params.decode(buffer);
                    return params;
                }
        );
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        ToolUse.STREAM_CODEC.encode(buffer, toolUse);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        toolUse = ToolUse.STREAM_CODEC.decode(buffer);
    }
}
