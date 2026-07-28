package com.evandev.manual_labour.client;

import com.evandev.manual_labour.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class ModToolModels {
    public static final ModelResourceLocation LADLE = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "misc/ladle"));
    public static final ModelResourceLocation PESTLE = ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "misc/pestle"));

    public static BakedModel ladle() {
        return Minecraft.getInstance().getModelManager().getModel(LADLE);
    }

    public static BakedModel pestle() {
        return Minecraft.getInstance().getModelManager().getModel(PESTLE);
    }
}
