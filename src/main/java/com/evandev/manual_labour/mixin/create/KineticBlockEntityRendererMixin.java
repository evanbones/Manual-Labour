package com.evandev.manual_labour.mixin.create;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlock;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("create")
@Mixin(KineticBlockEntityRenderer.class)
public abstract class KineticBlockEntityRendererMixin {
    @ModifyExpressionValue(
            method = "getRotationOffsetForPosition",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/base/KineticBlockEntityVisual;rotationOffset(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction$Axis;Lnet/minecraft/core/Vec3i;)F"))
    private static float manual_labour$alignWithMillstoneSocket(float original, @Local(argsOnly = true) KineticBlockEntity kinetic, @Local(argsOnly = true) Direction.Axis axis) {
        return MillstoneBlock.seatsShaft(kinetic, axis) ? 0.0F : original;
    }
}
