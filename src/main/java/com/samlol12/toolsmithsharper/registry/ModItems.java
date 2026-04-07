package com.samlol12.toolsmithsharper.registry;

import com.samlol12.toolsmithsharper.ToolsmithSharper;
import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.item.ToolsmithItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item WHETSTONE = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "whetstone"),
            new ToolsmithItem(new FabricItemSettings().maxDamage(ModConfig.MAX_WHETSTONE_USES), ModConfig.WHETSTONE_USE_TIME, "none", false));

    public static final Item FIRE_OIL = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "fire_oil"),
            new ToolsmithItem(new FabricItemSettings(), 10, "fire", true));

    public static final Item POISON_OIL = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "poison_oil"),
            new ToolsmithItem(new FabricItemSettings(), 10, "poison", true));

    public static final Item VAMPIRE_OIL = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "vampire_oil"),
            new ToolsmithItem(new FabricItemSettings(), 10, "vampire", true));

    public static final Item FROST_OIL = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "frost_oil"),
            new ToolsmithItem(new FabricItemSettings(), 10, "frost", true));

    public static final Item LUCK_OIL = Registry.register(Registries.ITEM, new Identifier(ToolsmithSharper.MOD_ID, "luck_oil"),
            new ToolsmithItem(new FabricItemSettings(), 10, "luck", true));

    public static void register() {
        // Appelé dans onInitialize pour charger la classe
    }
}