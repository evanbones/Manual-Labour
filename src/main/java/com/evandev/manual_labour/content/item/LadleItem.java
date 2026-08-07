package com.evandev.manual_labour.content.item;

import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.compat.create.CreateIntegration.LadleResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.NotNull;

public class LadleItem extends Item {
    public LadleItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        LadleResult result = CreateCompat.get().tryStirBasin(level, context.getClickedPos(), player, context.getItemInHand());
        return switch (result) {
            case NOT_A_BASIN -> InteractionResult.PASS;
            case NOTHING_TO_MIX -> InteractionResult.CONSUME;
            case STIRRING -> InteractionResult.SUCCESS;
        };
    }
}
