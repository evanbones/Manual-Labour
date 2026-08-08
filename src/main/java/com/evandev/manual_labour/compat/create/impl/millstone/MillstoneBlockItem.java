package com.evandev.manual_labour.compat.create.impl.millstone;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class MillstoneBlockItem extends BlockItem {
    private static final int OUTLINE_COLOR = 0xFFD82C;
    private static final int MESSAGE_COLOR = 0xFF5C6C;
    private static final String OUTLINE_KEY = "millstone";

    public MillstoneBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    private static void explainFailure(@Nullable Player player, BlockPos origin) {
        AABB footprint = new AABB(origin).inflate(1.0, 0.0, 1.0);
        Outliner outliner = Outliner.getInstance();
        outliner.showAABB(Pair.of(OUTLINE_KEY, origin), footprint).colored(OUTLINE_COLOR);

        if (player == null) {
            return;
        }
        Component reason = Component.translatable("message.manual_labour.millstone_space").withColor(MESSAGE_COLOR);
        player.displayClientMessage(reason, true);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();

        List<Supplier<BlockPlaceContext>> attempts = List.of(
                () -> context,
                () -> BlockPlaceContext.at(context, clicked.above(), Direction.UP));

        InteractionResult lastResult = InteractionResult.FAIL;
        for (Supplier<BlockPlaceContext> attempt : attempts) {
            lastResult = super.place(attempt.get());
            if (lastResult != InteractionResult.FAIL) {
                return lastResult;
            }
        }

        if (level.isClientSide) {
            explainFailure(context.getPlayer(), clicked);
        }
        return lastResult;
    }
}
