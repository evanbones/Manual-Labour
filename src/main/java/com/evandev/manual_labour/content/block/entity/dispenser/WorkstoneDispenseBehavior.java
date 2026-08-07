package com.evandev.manual_labour.content.block.entity.dispenser;

import com.evandev.manual_labour.content.block.WorkstoneBlock;
import com.evandev.manual_labour.content.block.entity.WorkstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkstoneDispenseBehavior extends OptionalDispenseItemBehavior {
    public static final WorkstoneDispenseBehavior INSTANCE = new WorkstoneDispenseBehavior();

    public boolean tryDispenseStackOnWorkstone(BlockSource source, ItemStack stack, int slot, DispenserBlockEntity dispenser) {
        setSuccess(false);
        Level level = source.level();
        BlockPos pos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof WorkstoneBlock && level.getBlockEntity(pos) instanceof WorkstoneBlockEntity workstone) {
            if (!workstone.isEmpty()) {
                if (workstone.processStoredItemUsingTool(stack, null)) {
                    setSuccess(true);
                    this.playSound(source);
                    this.playAnimation(source, source.state().getValue(DispenserBlock.FACING));
                    dispenser.setItem(slot, stack);
                    return true;
                }
            } else {
                if (workstone.canAddItem(stack)) {
                    ItemStack singleItem = stack.split(1);
                    ItemStack remainder = workstone.addItem(singleItem);
                    if (remainder.isEmpty()) {
                        setSuccess(true);
                        this.playSound(source);
                        this.playAnimation(source, source.state().getValue(DispenserBlock.FACING));
                        dispenser.setItem(slot, stack);
                        return true;
                    } else {
                        stack.grow(1);
                    }
                }
            }
        }
        return false;
    }
}
