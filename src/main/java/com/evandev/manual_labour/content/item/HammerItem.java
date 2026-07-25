package com.evandev.manual_labour.content.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class HammerItem extends DiggerItem {
    public HammerItem(Tier tier, float attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, BlockTags.MINEABLE_WITH_PICKAXE, properties.attributes(
                DiggerItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)
        ));
    }
}
