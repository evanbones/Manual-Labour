package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.BasinStirClientState;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.content.block.entity.MortarBlockEntity;
import com.evandev.manual_labour.registry.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ToolHandRenderHandler {
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ModTags.Items.PESTLES) && !stack.is(ModTags.Items.LADLES)) return;

        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.keyUse.isDown()) return;
        if (mc.player == null || mc.level == null) return;

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) return;

        Level level = mc.level;
        BlockPos pos = blockHit.getBlockPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MortarBlockEntity mortar) {
            if (mortar.isProcessing()) {
                event.setCanceled(true);
            }
        } else if (BasinStirClientState.isStirring(pos) && CreateCompat.get().isBasin(level, pos)) {
            event.setCanceled(true);
        }
    }
}
