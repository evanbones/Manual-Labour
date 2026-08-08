package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MortarFluidTank {
    public static final int OUTPUT_INDEX = 0;
    public static final int INPUT_INDEX = 1;

    private static final double CHASE_SPEED = 0.25D;

    private final MortarBlockEntity blockEntity;
    private final int capacity;
    private final Segment input;
    private final Segment output;
    private final List<Segment> segments;
    private final IFluidHandler capability;

    public MortarFluidTank(int capacity, MortarBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.capacity = capacity;
        this.input = new Segment(capacity, this);
        this.output = new Segment(capacity, this);
        this.segments = List.of(output, input);
        this.capability = new Combined();
    }

    public void onFluidStackChanged() {
        if (!blockEntity.hasLevel()) return;

        syncLevelToContents();

        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
            blockEntity.notifyUpdate();
        }
    }

    public void syncLevelToContents() {
        for (Segment segment : segments) {
            segment.syncLevelToContents();
        }
    }

    public void tick() {
        for (Segment segment : segments) {
            segment.tick();
        }
    }

    public void forceNextSync() {
        for (Segment segment : segments) {
            segment.getFluidLevel().forceNextSync();
        }
    }

    public SmartFluidTank getInputTank() {
        return input.getTank();
    }

    public SmartFluidTank getOutputTank() {
        return output.getTank();
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public IFluidHandler getCapability() {
        return capability;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isEmpty() {
        return input.getTank().isEmpty() && output.getTank().isEmpty();
    }

    public int getTotalAmount() {
        return input.getTank().getFluidAmount() + output.getTank().getFluidAmount();
    }

    public float getTotalUnits(float partialTicks) {
        float total = 0;
        for (Segment segment : segments) {
            total += segment.getTotalUnits(partialTicks);
        }
        return total;
    }

    public boolean isEmpty(float partialTicks) {
        return getTotalUnits(partialTicks) < 1;
    }

    public CompoundTag writeNBT(HolderLookup.Provider registries) {
        CompoundTag compound = new CompoundTag();
        compound.put("Input", input.writeNBT(registries));
        compound.put("Output", output.writeNBT(registries));
        return compound;
    }

    public void readNBT(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (compound.contains("Input") || compound.contains("Output")) {
            input.readNBT(compound.getCompound("Input"), registries, clientPacket);
            output.readNBT(compound.getCompound("Output"), registries, clientPacket);
            return;
        }

        input.readNBT(compound, registries, clientPacket);
        output.readNBT(new CompoundTag(), registries, clientPacket);
    }

    public static class Segment {
        private final SmartFluidTank tank;
        private final LerpedFloat fluidLevel;

        private FluidStack renderedFluid = FluidStack.EMPTY;

        private Segment(int capacity, MortarFluidTank parent) {
            this.tank = new SmartFluidTank(capacity, fluid -> parent.onFluidStackChanged());
            this.fluidLevel = LerpedFloat.linear()
                    .startWithValue(0)
                    .chase(0, CHASE_SPEED, Chaser.EXP);
        }

        private void syncLevelToContents() {
            float target = tank.getFluidAmount() / (float) tank.getCapacity();
            if (ModConfig.get().instantFluidFill) {
                fluidLevel.startWithValue(target);
            }
            fluidLevel.chase(target, CHASE_SPEED, Chaser.EXP);

            if (!tank.getFluid().isEmpty()) {
                renderedFluid = tank.getFluid();
            }
        }

        private void tick() {
            fluidLevel.tickChaser();
        }

        public SmartFluidTank getTank() {
            return tank;
        }

        public FluidStack getRenderedFluid() {
            return renderedFluid;
        }

        public LerpedFloat getFluidLevel() {
            return fluidLevel;
        }

        public float getTotalUnits(float partialTicks) {
            return fluidLevel.getValue(partialTicks) * tank.getCapacity();
        }

        private CompoundTag writeNBT(HolderLookup.Provider registries) {
            CompoundTag compound = new CompoundTag();
            compound.put("TankContent", tank.writeToNBT(registries, new CompoundTag()));
            compound.put("Level", fluidLevel.writeNBT());
            return compound;
        }

        private void readNBT(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
            tank.readFromNBT(registries, compound.contains("TankContent") ? compound.getCompound("TankContent") : compound);
            fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
            if (ModConfig.get().instantFluidFill) {
                fluidLevel.startWithValue(fluidLevel.getChaseTarget());
            }
            if (!tank.getFluid().isEmpty()) {
                renderedFluid = tank.getFluid();
            }
        }
    }

    private class Combined implements IFluidHandler {

        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return segmentFor(tank).getTank().getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacity;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == INPUT_INDEX && input.getTank().isFluidValid(stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            return input.getTank().fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;

            FluidStack remaining = resource.copy();
            FluidStack drained = output.getTank().drain(remaining, action);
            remaining.shrink(drained.getAmount());
            if (remaining.isEmpty()) return drained;

            return merge(drained, input.getTank().drain(remaining, action));
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;

            FluidStack drained = output.getTank().drain(maxDrain, action);
            if (drained.getAmount() >= maxDrain) return drained;

            return merge(drained, input.getTank().drain(maxDrain - drained.getAmount(), action));
        }

        private FluidStack merge(FluidStack first, FluidStack second) {
            if (first.isEmpty()) return second;
            if (second.isEmpty() || !FluidStack.isSameFluidSameComponents(first, second)) return first;
            return first.copyWithAmount(first.getAmount() + second.getAmount());
        }

        private Segment segmentFor(int tank) {
            return tank == OUTPUT_INDEX ? output : input;
        }
    }
}
