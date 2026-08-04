package com.evandev.manual_labour.compat.create;

import com.evandev.manual_labour.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record BasinStirPayload(BlockPos pos, ItemStack tool) implements CustomPacketPayload {
    public static final Type<BasinStirPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "basin_stir"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BasinStirPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.cast(), BasinStirPayload::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, BasinStirPayload::tool,
            BasinStirPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
