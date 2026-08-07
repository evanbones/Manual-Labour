package com.evandev.manual_labour.datagen.providers;

import com.evandev.manual_labour.Constants;
import com.evandev.manual_labour.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        handheldItem(ModItems.FLINT_HAMMER.get());
        handheldItem(ModItems.IRON_HAMMER.get());
        handheldItem(ModItems.GOLDEN_HAMMER.get());
        handheldItem(ModItems.DIAMOND_HAMMER.get());
        handheldItem(ModItems.NETHERITE_HAMMER.get());

        handheldItem(ModItems.PESTLE.get())
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                    .rotation(0, -90, -125)
                    .translation(0, 4.0f, 0.5f)
                    .scale(0.85f)
                    .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                    .rotation(0, 90, 125)
                    .translation(0, 4.0f, 0.5f)
                    .scale(0.85f)
                    .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(0, -90, -155)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f)
                    .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                    .rotation(0, 90, 155)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f)
                    .end()
                .end();

        handheldItem(ModItems.LADLE.get())
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                    .rotation(0, -90, -125)
                    .translation(0, 4.0f, 0.5f)
                    .scale(0.85f)
                    .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                    .rotation(0, 90, 125)
                    .translation(0, 4.0f, 0.5f)
                    .scale(0.85f)
                    .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(0, -90, -155)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f)
                    .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                    .rotation(0, 90, 155)
                    .translation(1.13f, 3.2f, 1.13f)
                    .scale(0.68f)
                    .end()
                .end();
    }

    public @NotNull ItemModelBuilder handheldItem(@NotNull Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        return withExistingParent(name, "item/handheld")
                .texture("layer0", modLoc("item/" + name));
    }
}
