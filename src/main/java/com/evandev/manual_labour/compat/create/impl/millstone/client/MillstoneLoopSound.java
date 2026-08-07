package com.evandev.manual_labour.compat.create.impl.millstone.client;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
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
        this.y = controllerPos.getY() + 0.5;
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
        if (!(minecraft.level.getBlockEntity(controllerPos) instanceof MillstoneBlockEntity millstone)) {
            return 0.0F;
        }
        if (millstone.isOverspeed()) {
            return 0.0F;
        }
        return Math.abs(millstone.getSpeed());
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
