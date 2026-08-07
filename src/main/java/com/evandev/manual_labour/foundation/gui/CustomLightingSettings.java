package com.evandev.manual_labour.foundation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.createmod.catnip.gui.ILightingSettings;
import org.joml.Vector3f;

/**
 * Two directional lights aimed at a GUI-rendered block.
 * <p>
 * Ported from Create's {@code com.simibubi.create.foundation.gui.CustomLightingSettings} (MIT,
 * Copyright (c) simibubi).
 */
public class CustomLightingSettings implements ILightingSettings {

    private final Vector3f light1;
    private final Vector3f light2;

    protected CustomLightingSettings(float yRot1, float xRot1, float yRot2, float xRot2, boolean doubleLight) {
        light1 = new Vector3f(0, 0, 1);
        light1.rotate(Axis.YP.rotationDegrees(yRot1));
        light1.rotate(Axis.XN.rotationDegrees(xRot1));

        if (doubleLight) {
            light2 = new Vector3f(0, 0, 1);
            light2.rotate(Axis.YP.rotationDegrees(yRot2));
            light2.rotate(Axis.XN.rotationDegrees(xRot2));
        } else {
            light2 = new Vector3f();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void applyLighting() {
        RenderSystem.setShaderLights(light1, light2);
    }

    public static class Builder {

        private float yRot1, xRot1;
        private float yRot2, xRot2;
        private boolean doubleLight;

        public Builder firstLightRotation(float yRot, float xRot) {
            yRot1 = yRot;
            xRot1 = xRot;
            return this;
        }

        public Builder secondLightRotation(float yRot, float xRot) {
            yRot2 = yRot;
            xRot2 = xRot;
            doubleLight = true;
            return this;
        }

        public Builder doubleLight() {
            doubleLight = true;
            return this;
        }

        public CustomLightingSettings build() {
            return new CustomLightingSettings(yRot1, xRot1, yRot2, xRot2, doubleLight);
        }
    }
}
