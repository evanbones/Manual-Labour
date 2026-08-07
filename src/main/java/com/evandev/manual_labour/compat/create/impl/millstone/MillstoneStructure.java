package com.evandev.manual_labour.compat.create.impl.millstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public final class MillstoneStructure {
    public static final List<BlockPos> BASE_OFFSETS = baseOffsets();
    public static final List<BlockPos> ALL_OFFSETS = BASE_OFFSETS;

    private static List<BlockPos> baseOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 || dz != 0) {
                    offsets.add(new BlockPos(dx, 0, dz));
                }
            }
        }
        return List.copyOf(offsets);
    }

    public static boolean isCorner(BlockPos offset) {
        return offset.getX() != 0 && offset.getZ() != 0;
    }

    public static Direction baseFacing(BlockPos offset) {
        int dx = offset.getX();
        int dz = offset.getZ();
        if (dx == 0) {
            return dz > 0 ? Direction.NORTH : Direction.SOUTH;
        }
        if (dz == 0) {
            return dx > 0 ? Direction.WEST : Direction.EAST;
        }
        if (dx > 0 && dz > 0) {
            return Direction.WEST;
        }
        if (dx < 0 && dz > 0) {
            return Direction.NORTH;
        }
        if (dx < 0) {
            return Direction.EAST;
        }
        return Direction.SOUTH;
    }
}
