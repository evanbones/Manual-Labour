package com.evandev.manual_labour.compat.caverns_and_chasms.impl;

import com.evandev.manual_labour.content.item.HammerItem;
import com.teamabnormals.caverns_and_chasms.common.item.copper.WeatheringCopperItem;
import com.teamabnormals.caverns_and_chasms.core.other.CCTiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

public class CopperHammerItem extends HammerItem implements WeatheringCopperItem {
    private final WeatherState weatherState;

    public CopperHammerItem(WeatherState weatherState, Item.Properties properties) {
        super(CCTiers.CCItemTiers.COPPER, 6.0F, -3.0F, properties);
        this.weatherState = weatherState;
    }

    @Override
    public WeatherState getAge() {
        return weatherState;
    }
}
