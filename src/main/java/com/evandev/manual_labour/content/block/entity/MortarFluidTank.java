package com.evandev.manual_labour.content.block.entity;

import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.fluids.FluidStack;

public class MortarFluidTank {
    private static final double CHASE_SPEED = 0.25D;

    private final MortarBlockEntity blockEntity;
    private final SmartFluidTank tank;
    private final LerpedFloat fluidLevel;

    private FluidStack renderedFluid = FluidStack.EMPTY;

    public MortarFluidTank(int capacity, MortarBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.tank = new SmartFluidTank(capacity, fluid -> onFluidStackChanged());
        this.fluidLevel = LerpedFloat.linear()
                .startWithValue(0)
                .chase(0, CHASE_SPEED, Chaser.EXP);
    }

    public void onFluidStackChanged() {
        if (!blockEntity.hasLevel()) return;

        syncLevelToContents();

        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
            blockEntity.notifyUpdate();
        }
    }

    public void syncLevelToContents() {
        fluidLevel.chase(tank.getFluidAmount() / (float) tank.getCapacity(), CHASE_SPEED, Chaser.EXP);

        if (!tank.getFluid().isEmpty()) {
            renderedFluid = tank.getFluid();
        }
    }

    public void tick() {
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

    public boolean isEmpty(float partialTicks) {
        return renderedFluid.isEmpty() || getTotalUnits(partialTicks) < 1;
    }

    public CompoundTag writeNBT(HolderLookup.Provider registries) {
        CompoundTag compound = new CompoundTag();
        compound.put("TankContent", tank.writeToNBT(registries, new CompoundTag()));
        compound.put("Level", fluidLevel.writeNBT());
        return compound;
    }

    public void readNBT(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        tank.readFromNBT(registries, compound.contains("TankContent") ? compound.getCompound("TankContent") : compound);
        fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
        if (!tank.getFluid().isEmpty()) {
            renderedFluid = tank.getFluid();
        }
    }
}
