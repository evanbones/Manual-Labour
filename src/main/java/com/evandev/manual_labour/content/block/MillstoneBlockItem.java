package com.evandev.manual_labour.content.block;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

public class MillstoneBlockItem extends BlockItem {
    private static final int OUTLINE_COLOR = 0xFFD82C;

    public MillstoneBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public @org.jetbrains.annotations.NotNull InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result != InteractionResult.FAIL) {
            return result;
        }

        BlockPlaceContext raised = BlockPlaceContext.at(context, context.getClickedPos().above(), Direction.UP);
        result = super.place(raised);
        if (result != InteractionResult.FAIL) {
            return result;
        }

        if (context.getLevel().isClientSide) {
            showBounds(context);
        }
        return result;
    }

    private static void showBounds(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        AABB bounds = new AABB(pos).inflate(1.0, 0.0, 1.0).expandTowards(0.0, 1.0, 0.0);
        Outliner.getInstance().showAABB(Pair.of("millstone", pos), bounds).colored(OUTLINE_COLOR);
        Player player = context.getPlayer();
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.manual_labour.millstone_space").withColor(0xFF5C6C), true);
        }
    }
}
