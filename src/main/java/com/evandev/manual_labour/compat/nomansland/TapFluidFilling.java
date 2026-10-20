package com.evandev.manual_labour.compat.nomansland;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class TapFluidFilling {
    public static boolean fillReceiver(Level level, BlockPos pos, Block cauldron) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(cauldron);
        if (content == null) return false;
        IFluidHandler receiver = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP);
        if (receiver == null) return false;
        FluidStack step = new FluidStack(content.fluid, content.totalAmount / content.maxLevel);
        if (receiver.fill(step, IFluidHandler.FluidAction.SIMULATE) != step.getAmount()) return false;
        receiver.fill(step, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }
}
