package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MortarBlockEntity extends AbstractMortarBlockEntity {
    public MortarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MORTAR.get(), pos, state);
    }

    @Override
    public float getFluidMinX() {
        return 2.0F / 16.0F;
    }
    @Override
    public float getFluidMaxX() {
        return 14.0F / 16.0F;
    }
    @Override
    public float getFluidMinY() {
        return 5.0F / 16.0F;
    }
    @Override
    public float getFluidMaxY() {
        return 14.0F / 16.0F;
    }
}
