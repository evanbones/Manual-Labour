package com.evandev.manual_labour.recipe;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.IntFunction;

public interface WorkstoneProcess {

    List<ItemStack> rollResults(RandomSource random);

    default ToolUse toolUse() {
        return ToolUse.DAMAGE;
    }

    enum ToolUse implements StringRepresentable {
        DAMAGE("damage"),
        CONSUME("consume"),
        KEEP("keep");

        public static final Codec<ToolUse> CODEC = StringRepresentable.fromEnum(ToolUse::values);
        public static final IntFunction<ToolUse> BY_ID = ByIdMap.continuous(ToolUse::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, ToolUse> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ToolUse::ordinal);

        private final String name;

        ToolUse(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}

