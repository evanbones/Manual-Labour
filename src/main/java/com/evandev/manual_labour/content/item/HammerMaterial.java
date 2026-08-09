package com.evandev.manual_labour.content.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.OrCondition;

import java.util.List;
import java.util.function.UnaryOperator;

public record HammerMaterial(
        String id,
        List<String> modIds,
        Tier tier,
        float attackDamageModifier,
        float attackSpeedModifier,
        TagKey<Item> craftingIngredient,
        UnaryOperator<Item.Properties> properties
) {
    public HammerMaterial(String id, List<String> modIds, Tier tier, float attackDamageModifier, float attackSpeedModifier, TagKey<Item> craftingIngredient) {
        this(id, modIds, tier, attackDamageModifier, attackSpeedModifier, craftingIngredient, UnaryOperator.identity());
    }

    public HammerMaterial(String id, String modId, Tier tier, float attackDamageModifier, float attackSpeedModifier, TagKey<Item> craftingIngredient) {
        this(id, List.of(modId), tier, attackDamageModifier, attackSpeedModifier, craftingIngredient);
    }

    public String itemId() {
        return id + "_hammer";
    }

    public boolean isLoaded() {
        ModList modList = ModList.get();
        return modIds.stream().anyMatch(modList::isLoaded);
    }

    public ICondition condition() {
        List<ICondition> conditions = modIds.stream().<ICondition>map(ModLoadedCondition::new).toList();
        return conditions.size() == 1 ? conditions.getFirst() : new OrCondition(conditions);
    }
}
