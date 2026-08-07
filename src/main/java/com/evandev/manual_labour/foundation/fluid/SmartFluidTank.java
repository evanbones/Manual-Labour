package com.evandev.manual_labour.foundation.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.function.Consumer;

/**
 * A fluid tank that reports every content change to a callback.
 * <p>
 * Ported from Create's {@code com.simibubi.create.foundation.fluid.SmartFluidTank} (MIT,
 * Copyright (c) simibubi).
 */
public class SmartFluidTank extends FluidTank {

    private final Consumer<FluidStack> updateCallback;

    public SmartFluidTank(int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity);
        this.updateCallback = updateCallback;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        updateCallback.accept(getFluid());
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        updateCallback.accept(stack);
    }
}
