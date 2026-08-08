package com.evandev.manual_labour.compat.create.impl.millstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;

public final class MillstoneStructure {

    public static final List<BlockPos> BASE_OFFSETS = List.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 0),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(1, 0, 1));

    public static final List<BlockPos> ALL_OFFSETS = BASE_OFFSETS;

    private MillstoneStructure() {
    }

    public static boolean isCorner(BlockPos offset) {
        boolean offCentreOnX = offset.getX() != 0;
        boolean offCentreOnZ = offset.getZ() != 0;
        return offCentreOnX && offCentreOnZ;
    }

    public static Direction baseFacing(BlockPos offset) {
        Direction.Axis axis = inwardAxis(offset);
        int stepAlongAxis = offset.get(axis);
        Direction.AxisDirection towardsCentre = stepAlongAxis > 0
                ? Direction.AxisDirection.NEGATIVE
                : Direction.AxisDirection.POSITIVE;
        return Direction.fromAxisAndDirection(axis, towardsCentre);
    }

    private static Direction.Axis inwardAxis(BlockPos offset) {
        if (!isCorner(offset)) {
            return offset.getX() != 0 ? Direction.Axis.X : Direction.Axis.Z;
        }
        return offset.getX() == offset.getZ() ? Direction.Axis.X : Direction.Axis.Z;
    }
}
