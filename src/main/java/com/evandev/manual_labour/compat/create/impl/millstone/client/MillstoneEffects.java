package com.evandev.manual_labour.compat.create.impl.millstone.client;

import com.simibubi.create.AllSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MillstoneEffects {
    private static final Map<Item, Vector3f> AVERAGE_COLORS = new HashMap<>();
    private static final Map<BlockPos, MillstoneLoopSound> LOOPS = new HashMap<>();

    private static final double SEAM_Y_OFFSET = 0.53;
    private static final double RIM_MIN = 1.30;

    private static final float IMPACT_CHANCE_PER_RPM = 0.0032F;
    private static final float IMPACT_CHANCE_MAX = 0.22F;
    private static final float IMPACT_VOLUME_BASE = 0.45F;
    private static final float IMPACT_VOLUME_PER_RPM = 0.0045F;
    private static final float IMPACT_VOLUME_MAX = 0.98F;
    private static final float IMPACT_PITCH_BASE = 0.86F;
    private static final float IMPACT_PITCH_JITTER = 0.26F;
    private static final float IMPACT_PITCH_PER_RPM = 0.0009F;
    private static final float IMPACT_PITCH_RISE_MAX = 0.18F;

    private static final float DUST_PER_RPM = 0.14F;
    private static final float DUST_MIN = 0.4F;
    private static final float DUST_MAX = 14.0F;
    private static final float DUST_SCALE = 0.85F;
    private static final double DUST_RING_INSET = 0.02;
    private static final double DUST_RING_SPREAD = 0.30;
    private static final double DUST_DROP = 0.28;
    private static final double DUST_DROP_BIAS = 2.2;
    private static final double DUST_PUSH_MIN = 0.10;
    private static final double DUST_PUSH_SPREAD = 0.11;
    private static final double DUST_SINK = -0.015;

    private static final float CHUNK_CHANCE = 0.4F;
    private static final double CHUNK_RING_INSET = 0.03;
    private static final double CHUNK_RING_SPREAD = 0.2;
    private static final double CHUNK_DROP = 0.12;
    private static final double CHUNK_PUSH = 0.19;
    private static final double CHUNK_LIFT = 0.075;

    private static final int SPRITE_ALPHA_CUTOFF = 90;
    private static final int SPRITE_SAMPLES_PER_AXIS = 16;
    private static final Vector3f FALLBACK_TINT = new Vector3f(0.66F, 0.63F, 0.58F);

    private static final double TAU = Math.PI * 2.0;

    private MillstoneEffects() {
    }

    public static void tick(Level level, BlockPos controllerPos, ItemStack grinding, float speed) {
        BlockPos key = controllerPos.immutable();
        MillstoneLoopSound loop = LOOPS.get(key);
        if (loop == null || loop.isStopped()) {
            MillstoneLoopSound replacement = new MillstoneLoopSound(key);
            LOOPS.put(key, replacement);
            Minecraft.getInstance().getSoundManager().play(replacement);
        }

        RandomSource random = level.getRandom();
        float rpm = Math.abs(speed);

        Vec3 axle = controllerPos.getCenter();
        double seamY = controllerPos.getY() + SEAM_Y_OFFSET;

        if (grinding.isEmpty()) {
            spawnIdleSparks(level, random, axle.x, axle.z, seamY, rpm, speed);
            return;
        }

        playGrindingImpact(level, axle, random, rpm);
        spawnDust(level, random, grinding, axle.x, axle.z, seamY, rpm);
        spawnChunk(level, random, grinding, axle.x, axle.z, seamY);
    }

    private static void playGrindingImpact(Level level, Vec3 axle, RandomSource random, float rpm) {
        float chance = Mth.clamp(rpm * IMPACT_CHANCE_PER_RPM, 0.0F, IMPACT_CHANCE_MAX);
        if (random.nextFloat() >= chance) {
            return;
        }

        SoundEvent sound = random.nextInt(4) == 0
                ? AllSoundEvents.CRUSHING_2.getMainEvent()
                : AllSoundEvents.CRUSHING_1.getMainEvent();
        float volume = Mth.clamp(IMPACT_VOLUME_BASE + rpm * IMPACT_VOLUME_PER_RPM, IMPACT_VOLUME_BASE, IMPACT_VOLUME_MAX);
        float pitch = IMPACT_PITCH_BASE
                + random.nextFloat() * IMPACT_PITCH_JITTER
                + Mth.clamp(rpm * IMPACT_PITCH_PER_RPM, 0.0F, IMPACT_PITCH_RISE_MAX);

        level.playLocalSound(axle.x, axle.y, axle.z, sound, SoundSource.BLOCKS, volume, pitch, false);
    }

    private static void spawnDust(Level level, RandomSource random, ItemStack grinding,
                                  double cx, double cz, double seamY, float rpm) {
        int count = randomRound(random, Mth.clamp(rpm * DUST_PER_RPM, DUST_MIN, DUST_MAX));
        if (count <= 0) {
            return;
        }

        DustParticleOptions dust = new DustParticleOptions(averageColor(grinding), DUST_SCALE);
        for (int i = 0; i < count; i++) {
            double bearing = random.nextDouble() * TAU;
            double sin = Math.sin(bearing);
            double cos = Math.cos(bearing);

            double radius = RIM_MIN + DUST_RING_INSET + random.nextDouble() * DUST_RING_SPREAD;
            double y = seamY - Math.pow(random.nextDouble(), DUST_DROP_BIAS) * DUST_DROP;
            double push = DUST_PUSH_MIN + random.nextDouble() * DUST_PUSH_SPREAD;

            level.addParticle(dust,
                    cx + cos * radius, y, cz + sin * radius,
                    cos * push, DUST_SINK, sin * push);
        }
    }

    private static void spawnChunk(Level level, RandomSource random, ItemStack grinding,
                                   double cx, double cz, double seamY) {
        if (random.nextFloat() >= CHUNK_CHANCE) {
            return;
        }

        double bearing = random.nextDouble() * TAU;
        double sin = Math.sin(bearing);
        double cos = Math.cos(bearing);
        double radius = RIM_MIN + CHUNK_RING_INSET + random.nextDouble() * CHUNK_RING_SPREAD;

        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, grinding),
                cx + cos * radius, seamY - random.nextDouble() * CHUNK_DROP, cz + sin * radius,
                cos * CHUNK_PUSH, CHUNK_LIFT, sin * CHUNK_PUSH);
    }

    private static int randomRound(RandomSource random, float rate) {
        int whole = Mth.floor(rate);
        return random.nextFloat() < rate - whole ? whole + 1 : whole;
    }

    private static void spawnIdleSparks(Level level, RandomSource random, double cx, double cz, double seamY,
                                        float absSpeed, float signedSpeed) {
        float sparksPerTick = Mth.clamp(absSpeed / 48.0F, 0.15F, 1.5F);
        int guaranteed = (int) sparksPerTick;
        float fractional = sparksPerTick - guaranteed;
        int count = guaranteed + (random.nextFloat() < fractional ? 1 : 0);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = RIM_MIN + random.nextDouble() * 0.30;
            double y = seamY - 0.02 + random.nextDouble() * 0.06;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;

            double outward = 0.02 + random.nextDouble() * 0.05;
            double tangential = Math.copySign(0.05 + random.nextDouble() * 0.07, signedSpeed);
            double vx = Math.cos(angle) * outward - Math.sin(angle) * tangential;
            double vz = Math.sin(angle) * outward + Math.cos(angle) * tangential;

            level.addParticle(ParticleTypes.CRIT, x, y, z, vx, 0.02 + random.nextDouble() * 0.03, vz);
        }
    }

    private static Vector3f averageColor(ItemStack stack) {
        Item key = stack.getItem();
        Vector3f known = AVERAGE_COLORS.get(key);
        if (known == null) {
            known = sampleParticleSprite(stack);
            AVERAGE_COLORS.put(key, known);
        }
        return known;
    }

    private static Vector3f sampleParticleSprite(ItemStack stack) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();

        int spriteWidth = sprite.contents().width();
        int spriteHeight = sprite.contents().height();
        if (spriteWidth <= 0 || spriteHeight <= 0) {
            return new Vector3f(FALLBACK_TINT);
        }

        int strideX = Math.max(1, spriteWidth / SPRITE_SAMPLES_PER_AXIS);
        int strideY = Math.max(1, spriteHeight / SPRITE_SAMPLES_PER_AXIS);

        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;
        int taken = 0;

        for (int y = 0; y < spriteHeight; y += strideY) {
            for (int x = 0; x < spriteWidth; x += strideX) {
                int abgr = sprite.getPixelRGBA(0, x, y);
                if (FastColor.ABGR32.alpha(abgr) < SPRITE_ALPHA_CUTOFF) {
                    continue;
                }
                redSum += FastColor.ABGR32.red(abgr);
                greenSum += FastColor.ABGR32.green(abgr);
                blueSum += FastColor.ABGR32.blue(abgr);
                taken++;
            }
        }

        if (taken == 0) {
            return new Vector3f(FALLBACK_TINT);
        }

        float scale = taken * 255.0F;
        return new Vector3f(redSum / scale, greenSum / scale, blueSum / scale);
    }
}
