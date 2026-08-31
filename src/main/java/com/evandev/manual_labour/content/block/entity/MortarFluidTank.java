package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MortarFluidTank {
    public static final int SEGMENTS_PER_SIDE = 2;

    private static final double CHASE_SPEED = 0.25D;

    private final MortarBlockEntity blockEntity;
    private final int capacity;
    private final List<Segment> outputSegments;
    private final List<Segment> inputSegments;
    private final List<Segment> segments;
    private final IFluidHandler capability;

    public MortarFluidTank(int capacity, MortarBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.capacity = capacity;

        this.outputSegments = new ArrayList<>(SEGMENTS_PER_SIDE);
        this.inputSegments = new ArrayList<>(SEGMENTS_PER_SIDE);
        for (int i = 0; i < SEGMENTS_PER_SIDE; i++) {
            outputSegments.add(new Segment(capacity, this));
        }
        for (int i = 0; i < SEGMENTS_PER_SIDE; i++) {
            inputSegments.add(new Segment(capacity, this));
        }

        List<Segment> combined = new ArrayList<>(outputSegments.size() + inputSegments.size());
        combined.addAll(outputSegments);
        combined.addAll(inputSegments);
        this.segments = List.copyOf(combined);

        this.capability = new Combined();
    }

    private static FluidStack getAnyFluid(List<Segment> segments) {
        for (Segment segment : segments) {
            FluidStack fluid = segment.getTank().getFluid();
            if (!fluid.isEmpty()) return fluid;
        }
        return FluidStack.EMPTY;
    }

    private static int fillInto(List<Segment> segments, FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) return 0;

        for (Segment segment : segments) {
            if (FluidStack.isSameFluidSameComponents(segment.getTank().getFluid(), resource)) {
                return segment.getTank().fill(resource, action);
            }
        }

        for (Segment segment : segments) {
            if (segment.getTank().isEmpty()) {
                return segment.getTank().fill(resource, action);
            }
        }

        return 0;
    }

    private static ListTag writeSegments(List<Segment> segments, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Segment segment : segments) {
            list.add(segment.writeNBT(registries));
        }
        return list;
    }

    private static void readSegments(List<Segment> segments, Tag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (tag instanceof ListTag list) {
            for (int i = 0; i < segments.size(); i++) {
                CompoundTag segmentTag = i < list.size() ? list.getCompound(i) : new CompoundTag();
                segments.get(i).readNBT(segmentTag, registries, clientPacket);
            }
            return;
        }

        // Legacy single-segment save format for this side.
        CompoundTag legacy = tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
        if (!segments.isEmpty()) {
            segments.get(0).readNBT(legacy, registries, clientPacket);
        }
        for (int i = 1; i < segments.size(); i++) {
            segments.get(i).readNBT(new CompoundTag(), registries, clientPacket);
        }
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

    public List<Segment> getInputSegments() {
        return inputSegments;
    }

    public List<Segment> getOutputSegments() {
        return outputSegments;
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
        for (Segment segment : segments) {
            if (!segment.getTank().isEmpty()) return false;
        }
        return true;
    }

    public int getTotalAmount() {
        int total = 0;
        for (Segment segment : segments) {
            total += segment.getTank().getFluidAmount();
        }
        return total;
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

    public FluidStack getAnyInputFluid() {
        return getAnyFluid(inputSegments);
    }

    public FluidStack getAnyOutputFluid() {
        return getAnyFluid(outputSegments);
    }

    public boolean hasInputFluid(SizedFluidIngredient ingredient) {
        for (Segment segment : inputSegments) {
            FluidStack fluid = segment.getTank().getFluid();
            if (!fluid.isEmpty() && ingredient.test(fluid) && fluid.getAmount() >= ingredient.amount()) return true;
        }
        return false;
    }

    public void drainInput(SizedFluidIngredient ingredient, IFluidHandler.FluidAction action) {
        for (Segment segment : inputSegments) {
            FluidStack fluid = segment.getTank().getFluid();
            if (!fluid.isEmpty() && ingredient.test(fluid) && fluid.getAmount() >= ingredient.amount()) {
                segment.getTank().drain(ingredient.amount(), action);
                return;
            }
        }
    }

    public int fillOutput(FluidStack resource, IFluidHandler.FluidAction action) {
        return fillInto(outputSegments, resource, action);
    }

    public CompoundTag writeNBT(HolderLookup.Provider registries) {
        CompoundTag compound = new CompoundTag();
        compound.put("Output", writeSegments(outputSegments, registries));
        compound.put("Input", writeSegments(inputSegments, registries));
        return compound;
    }

    public void readNBT(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (compound.contains("Input") || compound.contains("Output")) {
            readSegments(inputSegments, compound.get("Input"), registries, clientPacket);
            readSegments(outputSegments, compound.get("Output"), registries, clientPacket);
            return;
        }

        // Legacy single-segment save format.
        if (!inputSegments.isEmpty()) {
            inputSegments.get(0).readNBT(compound, registries, clientPacket);
        }
        for (int i = 1; i < inputSegments.size(); i++) {
            inputSegments.get(i).readNBT(new CompoundTag(), registries, clientPacket);
        }
        for (Segment segment : outputSegments) {
            segment.readNBT(new CompoundTag(), registries, clientPacket);
        }
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
            return segments.size();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return segments.get(tank).getTank().getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return capacity;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return inputSegments.contains(segments.get(tank)) && segments.get(tank).getTank().isFluidValid(stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            return fillInto(inputSegments, resource, action);
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty()) return FluidStack.EMPTY;

            FluidStack remaining = resource.copy();
            FluidStack drained = drainMatching(outputSegments, remaining, action);
            remaining.shrink(drained.getAmount());
            if (remaining.isEmpty()) return drained;

            return merge(drained, drainMatching(inputSegments, remaining, action));
        }

        private FluidStack drainMatching(List<Segment> segments, FluidStack resource, FluidAction action) {
            FluidStack drained = FluidStack.EMPTY;
            FluidStack remaining = resource.copy();

            for (Segment segment : segments) {
                FluidStack fromSegment = segment.getTank().drain(remaining, action);
                if (fromSegment.isEmpty()) continue;

                remaining.shrink(fromSegment.getAmount());
                drained = merge(drained, fromSegment);
                if (remaining.isEmpty()) break;
            }

            return drained;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (maxDrain <= 0) return FluidStack.EMPTY;

            FluidStack drained = drainAny(outputSegments, maxDrain, action);
            if (drained.getAmount() >= maxDrain) return drained;

            return merge(drained, drainAny(inputSegments, maxDrain - drained.getAmount(), action));
        }

        private FluidStack drainAny(List<Segment> segments, int maxDrain, FluidAction action) {
            for (Segment segment : segments) {
                FluidStack drained = segment.getTank().drain(maxDrain, action);
                if (!drained.isEmpty()) return drained;
            }
            return FluidStack.EMPTY;
        }

        private FluidStack merge(FluidStack first, FluidStack second) {
            if (first.isEmpty()) return second;
            if (second.isEmpty() || !FluidStack.isSameFluidSameComponents(first, second)) return first;
            return first.copyWithAmount(first.getAmount() + second.getAmount());
        }
    }
}
