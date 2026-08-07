package com.evandev.manual_labour.compat.ponder;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.compat.create.CreateCompat;
import com.evandev.manual_labour.registry.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ManualLabourPonderPlugin implements PonderPlugin {

    @Override
    public @NotNull String getModId() {
        return Constants.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.addStoryBoard(ModBlocks.MORTAR.getId(), "mortar", ManualLabourPonderScenes::mortar);
        CreateCompat.get().registerPonderScenes(helper);
    }
}
