package com.evandev.manual_labour.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientConfigSetup {
    public static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> ClientConfigScreen.create(parent));
    }
}
