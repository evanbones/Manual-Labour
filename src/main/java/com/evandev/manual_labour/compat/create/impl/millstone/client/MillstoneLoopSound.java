package com.evandev.manual_labour.compat.create.impl.millstone.client;

import com.evandev.manual_labour.compat.create.impl.millstone.MillstoneBlockEntity;
import com.evandev.manual_labour.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MillstoneLoopSound extends AbstractTickableSoundInstance {

    private static final float VOLUME_FLOOR = 0.55F;
    private static final float VOLUME_CEILING = 0.95F;
    private static final float VOLUME_PER_RPM = 0.0065F;

    private final BlockPos controllerPos;

    public MillstoneLoopSound(BlockPos controllerPos) {
        super(ModSounds.MILLSTONE_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.controllerPos = controllerPos.immutable();

        Vec3 anchor = this.controllerPos.getCenter();
        this.x = anchor.x;
        this.y = anchor.y;
        this.z = anchor.z;

        this.pitch = 1.0F;
        this.delay = 0;
        this.looping = true;
        this.volume = loudnessAt(currentSpeed());
    }

    private static float loudnessAt(float rpm) {
        return Mth.clamp(VOLUME_FLOOR + rpm * VOLUME_PER_RPM, VOLUME_FLOOR, VOLUME_CEILING);
    }

    private float currentSpeed() {
        Level level = Minecraft.getInstance().level;
        if (level != null
                && level.getBlockEntity(controllerPos) instanceof MillstoneBlockEntity millstone
                && !millstone.isOverspeed()) {
            return Math.abs(millstone.getSpeed());
        }
        return 0.0F;
    }

    @Override
    public void tick() {
        float rpm = currentSpeed();
        if (rpm <= 0.0F) {
            stop();
            return;
        }
        this.volume = loudnessAt(rpm);
    }
}
