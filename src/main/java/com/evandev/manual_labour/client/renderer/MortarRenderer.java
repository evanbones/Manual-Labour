package com.evandev.manual_labour.client.renderer;

import com.evandev.manual_labour.client.ModToolModels;
import com.evandev.manual_labour.config.ModConfig;
import com.evandev.manual_labour.content.block.MortarBlock;
import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.registry.ModTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.createmod.catnip.render.FluidRenderHelper;
import net.createmod.catnip.render.PonderRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MortarRenderer implements BlockEntityRenderer<MortarBlockEntity> {
    private static final float PESTLE_THUMP_SPEED = 18.0F;
    private static final float PESTLE_GRIND_SPEED = 10.0F;
    private static final float LADLE_STIR_SPEED = 6.0F;

    private static final float FLUID_MIN_X = 2.0F / 16.0F;
    private static final float FLUID_MAX_X = 14.0F / 16.0F;
    private static final float FLUID_MIN_Y = 5.0F / 16.0F;
    private static final float FLUID_MAX_Y = 14.0F / 16.0F;
    private static final int FLUID_ALPHA = 204;

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
        List<ItemStack> stacks = new ArrayList<>();
        IItemHandler inventory = mortar.getItemHandler();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) stacks.add(stack);
        }

        int count = stacks.size();
        boolean crowded = count > CROWDED_PILE_COUNT;
        float ringRadius = ModConfig.get().itemPileRadius * (crowded ? CROWDED_RING_SPREAD : 1.0F);

        for (int i = 0; i < count; i++) {
            poseStack.pushPose();
            poseStack.translate(0.5D, ModConfig.get().itemPileY, 0.5D);

            if (count > 1) {
                poseStack.mulPose(Axis.YP.rotationDegrees(360.0F / count * i));
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

        if (!decorativeTool.isEmpty() && !processing) {
            renderDecorativeTool(mortar, decorativeTool, poseStack, buffer, packedLight, packedOverlay);
        }

        if (processing && !activeTool.isEmpty()) {
            float time = mortar.getLevel() != null ? mortar.getLevel().getGameTime() + partialTicks : partialTicks;

            if (activeTool.is(ModTags.Items.LADLES)) {
                renderLadleStirring(activeTool, time, poseStack, buffer, packedLight, packedOverlay);
            } else if (activeTool.is(ModTags.Items.PESTLES)) {
                ItemStack primary = stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst();
                renderPestleGrinding(activeTool, primary, time, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(Sheets.translucentCullBlockSheet());
            bufferSource.endBatch(Sheets.cutoutBlockSheet());
        }

        renderFluid(mortar, poseStack, buffer, packedLight);
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
        renderToolModel(tool, model, poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderLadleStirring(ItemStack tool, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = time * LADLE_STIR_SPEED;
        Vec3 offset = new Vec3(0.12D, 0.0D, 0.0D).yRot((float) Math.toRadians(angle));

        poseStack.pushPose();
        poseStack.translate(0.5D + offset.x(), 0.95D, 0.5D + offset.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        poseStack.scale(1.1F, 1.1F, 1.1F);
        poseStack.translate(TOOL_PIVOT_X, TOOL_PIVOT_Y, TOOL_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
        poseStack.translate(-TOOL_PIVOT_X, -TOOL_PIVOT_Y, -TOOL_PIVOT_Z);

        renderToolModel(tool, ModToolModels.ladle(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderPestleGrinding(ItemStack tool, ItemStack primary, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int maxStackSize = primary.isEmpty() ? 64 : primary.getMaxStackSize();
        boolean thumping = primary.getCount() > maxStackSize / 2;
        float contactY = getPileTopY(primary) + ModConfig.get().pestleTipContactOffset;

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
        renderToolModel(tool, ModToolModels.pestle(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private float getPileTopY(ItemStack stack) {
        float itemPileY = ModConfig.get().itemPileY;
        if (stack.isEmpty()) return itemPileY;

        BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
        boolean blockItem = bakedModel.isGui3d();
        int layers = Mth.log2(stack.getCount()) / 2;
        float layerHeight = blockItem ? 1.0F / 64.0F : 1.0F / 16.0F;
        return itemPileY + (layers + 1) * layerHeight;
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

    private void renderToolModel(ItemStack tool, BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft.getInstance().getItemRenderer().render(
                tool, ItemDisplayContext.NONE, false,
                poseStack, buffer, packedLight, packedOverlay, model
        );
    }

    private void renderFluid(MortarBlockEntity mortar, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        FluidStack fluidStack = mortar.getFluidHandler().getFluidInTank(0);
        if (fluidStack.isEmpty()) return;

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture(fluidStack));
        int color = (FLUID_ALPHA << 24) | (extensions.getTintColor(fluidStack) & 0x00FFFFFF);

        float fillRatio = Mth.clamp((float) fluidStack.getAmount() / MortarBlockEntity.TANK_CAPACITY, 0.0F, 1.0F);
        float fluidY = Mth.lerp(fillRatio, FLUID_MIN_Y, FLUID_MAX_Y);

        VertexConsumer consumer = buffer.getBuffer(PonderRenderTypes.fluid());

        FluidRenderHelper.renderStillTiledFace(Direction.UP, FLUID_MIN_X, FLUID_MIN_X, FLUID_MAX_X, FLUID_MAX_X,
                fluidY, consumer, poseStack, packedLight, color, sprite);
        FluidRenderHelper.renderStillTiledFace(Direction.DOWN, FLUID_MIN_X, FLUID_MIN_X, FLUID_MAX_X, FLUID_MAX_X,
                fluidY, consumer, poseStack, packedLight, color, sprite);
    }
}
