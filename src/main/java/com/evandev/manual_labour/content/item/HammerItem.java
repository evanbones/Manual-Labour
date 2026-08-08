package com.evandev.manual_labour.content.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class HammerItem extends DiggerItem {
    @Nullable
    private final Ingredient repairIngredient;

    public HammerItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        this(tier, attackDamageModifier, attackSpeedModifier, properties, null);
    }

    public HammerItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Item.Properties properties, @Nullable Ingredient repairIngredient) {
        super(tier, BlockTags.MINEABLE_WITH_PICKAXE, properties.attributes(
                DiggerItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)
        ));
        this.repairIngredient = repairIngredient;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        if (repairIngredient != null) {
            return repairIngredient.test(repair);
        }
        return super.isValidRepairItem(toRepair, repair);
    }
}
