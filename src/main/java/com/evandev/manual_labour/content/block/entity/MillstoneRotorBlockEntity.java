package com.evandev.manual_labour.content.block.entity;

import com.evandev.manual_labour.registry.ModBlockEntities;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MillstoneRotorBlockEntity extends KineticBlockEntity {
    public static final float SPEED_LIMIT = 64.0F;
    public static final float STRESS_IMPACT = 16.0F;

    public float angle;
    public float prevAngle;

    public MillstoneRotorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE_ROTOR.get(), pos, state);
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = STRESS_IMPACT;
        return STRESS_IMPACT;
    }

    public boolean isOverspeed() {
        return Math.abs(getSpeed()) > SPEED_LIMIT;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (isOverspeed()) {
            tooltip.add(Component.literal("    ")
                    .append(Component.translatable("manual_labour.millstone.too_fast").withStyle(ChatFormatting.RED)));
            added = true;
        }
        return added;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || !level.isClientSide) {
            return;
        }
        prevAngle = angle;
        if (isOverspeed()) {
            spawnOverspeedParticles();
            return;
        }
        angle += getSpeed() * 3.0F / 10.0F;
        if (angle >= 360.0F) {
            angle -= 360.0F;
            prevAngle -= 360.0F;
        }
        if (angle <= -360.0F) {
            angle += 360.0F;
            prevAngle += 360.0F;
        }
    }

    private void spawnOverspeedParticles() {
        Vec3 center = getBlockPos().getCenter();

        if (level.random.nextFloat() < 0.6F) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + level.random.nextDouble() * 0.25;
            level.addParticle(ParticleTypes.CRIT,
                    center.x + Math.cos(angle) * radius, center.y + 0.08 + level.random.nextDouble() * 0.5, center.z + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.05, 0.04, Math.sin(angle) * 0.05);
        }

        for (int i = 0; i < 2; i++) {
            if (level.random.nextFloat() <= 0.7F) {
                double angle = level.random.nextDouble() * Math.PI * 2.0;
                double radius = 1.52 + level.random.nextDouble() * 0.18;
                level.addParticle(ParticleTypes.CRIT,
                        center.x + Math.cos(angle) * radius, center.y - 0.45 + level.random.nextDouble() * 0.4, center.z + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.12, 0.03, Math.sin(angle) * 0.12);
            }
        }
    }
}
