package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.client.ModToolModels;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.content.block.MortarBlock;
import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.content.block.entity.MortarFluidTank;
import com.evandev.manual_labour.registry.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MortarRenderer implements BlockEntityRenderer<MortarBlockEntity> {
    private static final float PESTLE_THUMP_SPEED = 18.0F;
    private static final float PESTLE_GRIND_SPEED = 10.0F;

    private static final float FLUID_MIN_X = 2.0F / 16.0F;
    private static final float FLUID_MAX_X = 14.0F / 16.0F;
    private static final float FLUID_MIN_Y = 5.0F / 16.0F;
    private static final float FLUID_MAX_Y = 14.0F / 16.0F;

    private static final int CROWDED_PILE_COUNT = 4;
    private static final float CROWDED_PILE_SCALE = 0.6F;
    private static final float CROWDED_RING_SPREAD = 1.25F;

    private static final float TOOL_PIVOT_X = 8.0F / 16.0F;
    private static final float TOOL_PIVOT_Y = 5.5F / 16.0F;
    private static final float TOOL_PIVOT_Z = 10.5F / 16.0F;

    public MortarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MortarBlockEntity mortar, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float fluidSurfaceY = renderFluid(mortar, partialTicks, poseStack, buffer, packedLight);

        List<ItemStack> stacks = new ArrayList<>();
        IItemHandler inventory = mortar.getItemHandler();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) stacks.add(stack);
        }

        ModConfig config = ModConfig.get();
        int count = stacks.size();
        boolean crowded = count > CROWDED_PILE_COUNT;
        float ringRadius = config.itemPileRadius * (crowded ? CROWDED_RING_SPREAD : 1.0F);

        float basePileY = fluidSurfaceY > 0.0F
                ? Math.max(config.itemPileY, fluidSurfaceY - config.itemFloatSinkDepth)
                : config.itemPileY;
        float renderTime = AnimationTickHolder.getRenderTime(mortar.getLevel());
        float anglePartition = count > 0 ? 360.0F / count : 0.0F;

        float firstPileY = basePileY;

        for (int i = 0; i < count; i++) {
            float pileY = basePileY;
            if (fluidSurfaceY > 0.0F) {
                pileY += (Mth.sin(renderTime / 12.0F + anglePartition * i) + 1.5F) / 32.0F;
            }
            if (i == 0) firstPileY = pileY;

            poseStack.pushPose();
            poseStack.translate(0.5D, pileY, 0.5D);

            if (count > 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(anglePartition * i));
                poseStack.translate(ringRadius, 0.0D, 0.0D);
            }

            if (crowded) {
                poseStack.scale(CROWDED_PILE_SCALE, CROWDED_PILE_SCALE, CROWDED_PILE_SCALE);
            }

            renderItemPile(poseStack, buffer, packedLight, packedOverlay, stacks.get(i), i);

            poseStack.popPose();
        }

        ItemStack decorativeTool = mortar.getDecorativeTool();
        ItemStack activeTool = mortar.getActiveTool();
        boolean processing = mortar.isProcessing();
        BlockPos pos = mortar.getBlockPos();

        if (processing && !activeTool.isEmpty()) {
            float time = mortar.getLevel() != null ? mortar.getLevel().getGameTime() + partialTicks : partialTicks;

            if (activeTool.is(ModTags.Items.LADLES)) {
                BasinStirClientState.set(pos, activeTool);
                BasinStirClientState.StirState state = BasinStirClientState.getOrCreate(pos, activeTool);
                float angle = state.getInterpolatedAngle(partialTicks);
                ToolAnimations.renderLadleStirring(activeTool, angle, poseStack, buffer, packedLight, packedOverlay);
            } else if (activeTool.is(ModTags.Items.PESTLES)) {
                ItemStack primary = stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst();
                renderPestleGrinding(activeTool, primary, firstPileY, time, poseStack, buffer, packedLight, packedOverlay);
            }
        } else if (!decorativeTool.isEmpty()) {
            if (decorativeTool.is(ModTags.Items.LADLES)) {
                BasinStirClientState.StirState state = BasinStirClientState.getOrCreate(pos, decorativeTool);
                state.active = false;
                state.targetAngularVelocity = 0.0F;
                float angle = state.getInterpolatedAngle(partialTicks);
                ToolAnimations.renderLadleStirring(decorativeTool, angle, poseStack, buffer, packedLight, packedOverlay);
            } else {
                renderDecorativeTool(mortar, decorativeTool, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(Sheets.translucentCullBlockSheet());
            bufferSource.endBatch(Sheets.cutoutBlockSheet());
        }
    }

    private void renderDecorativeTool(MortarBlockEntity mortar, ItemStack tool, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = mortar.getBlockState().getValue(MortarBlock.FACING);

        ModConfig config = ModConfig.get();

        poseStack.pushPose();
        poseStack.translate(0.5D + facing.getStepX() * config.decorativeToolSideOffset, config.decorativeToolY, 0.5D + facing.getStepZ() * config.decorativeToolSideOffset);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.scale(1.1F, 1.1F, 1.1F);
        poseStack.translate(TOOL_PIVOT_X, TOOL_PIVOT_Y, TOOL_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(config.decorativeToolTilt));
        poseStack.translate(-TOOL_PIVOT_X, -TOOL_PIVOT_Y, -TOOL_PIVOT_Z);

        BakedModel model = tool.is(ModTags.Items.LADLES) ? ModToolModels.ladle() : ModToolModels.pestle();
        ToolAnimations.renderToolModel(tool, model, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderPestleGrinding(ItemStack tool, ItemStack primary, float pileY, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int maxStackSize = primary.isEmpty() ? 64 : primary.getMaxStackSize();
        boolean thumping = primary.getCount() > maxStackSize / 2;
        float contactY = getPileTopY(primary, pileY) + ModConfig.get().pestleTipContactOffset;

        poseStack.pushPose();

        if (thumping) {
            float bounce = (float) Math.abs(Math.sin(Math.toRadians(time * PESTLE_THUMP_SPEED)));
            poseStack.translate(0.5D, contactY + bounce * 0.35D, 0.5D);
        } else {
            float angle = time * PESTLE_GRIND_SPEED;
            Vec3 offset = new Vec3(0.08D, 0.0D, 0.0D).yRot((float) Math.toRadians(angle));
            poseStack.translate(0.5D + offset.x(), contactY, 0.5D + offset.z());
            poseStack.mulPose(Axis.XP.rotationDegrees(12.0F));
        }

        poseStack.scale(1.1F, 1.1F, 1.1F);
        ToolAnimations.renderToolModel(tool, ModToolModels.pestle(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private float getPileTopY(ItemStack stack, float pileY) {
        if (stack.isEmpty()) return pileY;

        BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        boolean blockItem = bakedModel.isGui3d();
        int layers = Mth.log2(stack.getCount()) / 2;
        float layerHeight = blockItem ? 1.0F / 64.0F : 1.0F / 16.0F;
        return pileY + (layers + 1) * layerHeight;
    }

    private void renderItemPile(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, ItemStack stack, int seed) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel bakedModel = itemRenderer.getModel(stack, null, null, 0);
        boolean blockItem = bakedModel.isGui3d();

        Random random = new Random(seed);
        int layers = Mth.log2(stack.getCount()) / 2;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextInt(360)));

        for (int i = 0; i <= layers; i++) {
            poseStack.pushPose();

            if (blockItem) {
                poseStack.translate(random.nextFloat() * 0.0625F * i, 0.0D, random.nextFloat() * 0.0625F * i);
            }

            poseStack.scale(0.5F, 0.5F, 0.5F);

            if (!blockItem) {
                poseStack.translate(0.0D, -3.0F / 16.0F, 0.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }

            itemRenderer.render(stack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight, packedOverlay, bakedModel);
            poseStack.popPose();

            if (!blockItem) {
                poseStack.mulPose(Axis.YP.rotationDegrees(10.0F));
            }
            poseStack.translate(0.0D, blockItem ? 1.0F / 64.0F : 1.0F / 16.0F, 0.0D);
        }

        poseStack.popPose();
    }

    private float renderFluid(MortarBlockEntity mortar, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        MortarFluidTank tank = mortar.getFluidTank();
        float totalUnits = tank.getTotalUnits(partialTicks);
        if (totalUnits < 1.0F) return 0.0F;

        float fill = Mth.clamp(totalUnits / (tank.getCapacity() * tank.getSegments().size()), 0.0F, 1.0F);
        fill = 1.0F - ((1.0F - fill) * (1.0F - fill));

        float fluidY = Mth.lerp(fill, FLUID_MIN_Y, FLUID_MAX_Y);

        float xMin = FLUID_MIN_X;
        float xMax = FLUID_MIN_X;

        for (MortarFluidTank.Segment segment : tank.getSegments()) {
            if (segment.getRenderedFluid().isEmpty()) continue;

            float units = segment.getTotalUnits(partialTicks);
            if (units < 1.0F) continue;

            xMax += Mth.clamp(units / totalUnits, 0.0F, 1.0F) * (FLUID_MAX_X - FLUID_MIN_X);
            NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(segment.getRenderedFluid(),
                    xMin, FLUID_MIN_Y, FLUID_MIN_X, xMax, fluidY, FLUID_MAX_X,
                    buffer, poseStack, packedLight, false, false);

            xMin = xMax;
        }

        return fluidY;
    }
}
