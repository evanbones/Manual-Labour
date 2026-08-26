package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MortarModelSwitcher implements IDynamicBakedModel {
    public static final ModelResourceLocation LEGACY = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "block/mortar_legacy"));

    private final BakedModel current;

    public MortarModelSwitcher(BakedModel current) {
        this.current = current;
    }

    private BakedModel active() {
        if (!ModConfig.get().legacyMortarModel) return current;
        return Minecraft.getInstance().getModelManager().getModel(LEGACY);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return active().getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return active().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return active().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return active().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return active().isCustomRenderer();
    }

    @Override
    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleIcon() {
        return active().getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides() {
        return active().getOverrides();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemTransforms getTransforms() {
        return active().getTransforms();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return active().applyTransform(transformType, poseStack, applyLeftHandTransform);
    }
}
