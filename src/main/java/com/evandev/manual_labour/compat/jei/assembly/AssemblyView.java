package com.evandev.manual_labour.compat.jei.assembly;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface AssemblyView {

    Ingredient input();

    List<AssemblyStep> steps();

    int loops();

    ItemStack result();

    float outputChance();
}
