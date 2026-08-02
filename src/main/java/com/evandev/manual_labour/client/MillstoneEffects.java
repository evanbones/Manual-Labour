package com.evandev.manual_labour.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MillstoneEffects {
    private static final Map<Item, Vector3f> AVERAGE_COLORS = new HashMap<>();
    private static final Map<BlockPos, MillstoneLoopSound> LOOPS = new HashMap<>();

    private static final double SEAM_Y_OFFSET = 0.53;
    private static final double RIM_MIN = 1.30;

    private MillstoneEffects() {
    }

    public static void tick(Level level, BlockPos controllerPos, ItemStack grinding, float speed) {
        MillstoneLoopSound loop = LOOPS.get(controllerPos);
        if (loop == null || loop.isStopped()) {
            loop = new MillstoneLoopSound(controllerPos.immutable());
            LOOPS.put(controllerPos.immutable(), loop);
            Minecraft.getInstance().getSoundManager().play((SoundInstance) loop);
        }

        RandomSource random = level.getRandom();
        float absSpeed = Math.abs(speed);

        double cx = controllerPos.getX() + 0.5;
        double cz = controllerPos.getZ() + 0.5;
        double seamY = controllerPos.getY() + SEAM_Y_OFFSET;

        if (grinding.isEmpty()) {
            spawnIdleSparks(level, random, cx, cz, seamY, absSpeed, speed);
            return;
        }

        if (random.nextFloat() < Math.min(absSpeed / 300.0F, 0.24F)) {
            SoundEvent sound = random.nextFloat() < 0.78F
                    ? AllSoundEvents.CRUSHING_1.getMainEvent()
                    : AllSoundEvents.CRUSHING_2.getMainEvent();
            level.playLocalSound(controllerPos.getX() + 0.5, controllerPos.getY() + 0.5, controllerPos.getZ() + 0.5,
                    sound, SoundSource.BLOCKS,
                    Mth.clamp(0.5F + absSpeed / 256.0F, 0.5F, 1.0F),
                    0.9F + random.nextFloat() * 0.2F + Math.min(absSpeed / 1024.0F, 0.2F), false);
        }

        float dustPerTick = Mth.clamp(absSpeed / 8.0F, 0.5F, 16.0F);
        int guaranteed = (int) dustPerTick;
        float fractional = dustPerTick - guaranteed;
        int count = guaranteed + (random.nextFloat() < fractional ? 1 : 0);

        Vector3f color = averageColor(grinding);
        DustParticleOptions dust = new DustParticleOptions(color, 0.7F);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = RIM_MIN + 0.05 + random.nextDouble() * 0.25;
            double y = seamY - random.nextDouble() * random.nextDouble() * 0.3;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;
            double outward = 0.12 + random.nextDouble() * 0.08;
            double vx = Math.cos(angle) * outward;
            double vz = Math.sin(angle) * outward;
            level.addParticle(dust, x, y, z, vx, -0.01, vz);
        }

        if (random.nextInt(2) == 0) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = RIM_MIN + 0.05 + random.nextDouble() * 0.15;
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, grinding),
                    cx + Math.cos(angle) * radius, seamY - random.nextDouble() * 0.15, cz + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.16, 0.06, Math.sin(angle) * 0.16);
        }
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
        return AVERAGE_COLORS.computeIfAbsent(stack.getItem(), item -> {
            TextureAtlasSprite sprite = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0).getParticleIcon();
            NativeImage image = sprite.contents().getOriginalImage();
            long r = 0;
            long g = 0;
            long b = 0;
            long n = 0;
            int width = Math.min(image.getWidth(), sprite.contents().width());
            int height = Math.min(image.getHeight(), sprite.contents().height());
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = image.getPixelRGBA(x, y);
                    int alpha = pixel >> 24 & 0xFF;
                    if (alpha >= 128) {
                        r += pixel & 0xFF;
                        g += pixel >> 8 & 0xFF;
                        b += pixel >> 16 & 0xFF;
                        n++;
                    }
                }
            }
            return n == 0L ? new Vector3f(0.7F, 0.7F, 0.7F) : new Vector3f((float) r / n / 255.0F, (float) g / n / 255.0F, (float) b / n / 255.0F);
        });
    }
}
