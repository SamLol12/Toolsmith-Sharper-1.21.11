package com.samlol12.toolsmithsharper;

import com.samlol12.toolsmithsharper.command.ModCommands;
import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.event.ModEvents;
import com.samlol12.toolsmithsharper.registry.ModComponents;
import com.samlol12.toolsmithsharper.registry.ModItems;
import net.fabricmc.api.ModInitializer;

public class ToolsmithSharper implements ModInitializer {

    public static final String MOD_ID = "toolsmithsharper";

    @Override
    public void onInitialize() {
        ModConfig.loadConfig();       // Config load
        ModComponents.register();     // Load NBT Keys
        ModItems.register();          // Load Items
        ModEvents.register();         // Load Events
        ModCommands.register();       // Load Commands
    }
}