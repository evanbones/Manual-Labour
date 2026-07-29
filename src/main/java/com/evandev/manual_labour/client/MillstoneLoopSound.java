package com.evandev.manual_labour.client;

import com.evandev.manual_labour.content.block.MillstoneStructure;
import com.evandev.manual_labour.content.block.entity.MillstoneBlockEntity;
import com.evandev.manual_labour.content.block.entity.MillstoneRotorBlockEntity;
import com.evandev.manual_labour.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class MillstoneLoopSound extends AbstractTickableSoundInstance {
    private final BlockPos controllerPos;

    public MillstoneLoopSound(BlockPos controllerPos) {
        super(ModSounds.MILLSTONE_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.controllerPos = controllerPos;
        this.looping = true;
        this.delay = 0;
        this.x = controllerPos.getX() + 0.5;
        this.y = controllerPos.getY() + 1.0;
        this.z = controllerPos.getZ() + 0.5;
        this.pitch = 1.0F;
        this.volume = volumeFor(currentSpeed());
    }

    private static float volumeFor(float speed) {
        return Mth.clamp(0.6F + speed / 128.0F, 0.6F, 1.0F);
    }

    private float currentSpeed() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0F;
        }
        if (!(minecraft.level.getBlockEntity(controllerPos) instanceof MillstoneBlockEntity)) {
            return 0.0F;
        }
        BlockPos rotorPos = controllerPos.offset((Vec3i) MillstoneStructure.ROTOR_OFFSET);
        if (!(minecraft.level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor)) {
            return 0.0F;
        }
        if (rotor.isOverspeed()) {
            return 0.0F;
        }
        return Math.abs(rotor.getSpeed());
    }

    @Override
    public void tick() {
        float speed = currentSpeed();
        if (speed == 0.0F) {
            stop();
            return;
        }
        this.volume = volumeFor(speed);
    }
}
