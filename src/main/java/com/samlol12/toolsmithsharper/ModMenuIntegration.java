package com.samlol12.toolsmithsharper;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.translatable("config.toolsmithsharper.title"))
                    .setSavingRunnable(ToolsmithSharper::saveConfig);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            // ==========================================
            // CATEGORY 1 : BASE MECANIC
            // ==========================================
            ConfigCategory mechanics = builder.getOrCreateCategory(Text.translatable("config.toolsmithsharper.category.mechanics"));

            // Champ INT : Max Honed Uses
            mechanics.addEntry(entryBuilder.startIntField(Text.translatable("config.toolsmithsharper.max_uses"), ToolsmithSharper.MAX_SHARPER_USES)
                    .setDefaultValue(32)
                    .setMin(1)
                    .setTooltip(Text.translatable("config.toolsmithsharper.max_uses.tooltip"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.MAX_SHARPER_USES = newValue)
                    .build());

            // Champ INT : Max Coating Use
            mechanics.addEntry(entryBuilder.startIntField(Text.translatable("config.toolsmithsharper.max_coating_uses"), ToolsmithSharper.MAX_COATING_USES)
                    .setDefaultValue(10)
                    .setMin(1)
                    .setTooltip(Text.translatable("config.toolsmithsharper.max_coating_uses.tooltip"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.MAX_COATING_USES = newValue)
                    .build());

            // Champ INT : XP Cost
            mechanics.addEntry(entryBuilder.startIntField(Text.translatable("config.toolsmithsharper.xp_cost"), ToolsmithSharper.XP_COST)
                    .setDefaultValue(1)
                    .setMin(0)
                    .setTooltip(Text.translatable("config.toolsmithsharper.xp_cost.tooltip"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.XP_COST = newValue)
                    .build());

            // SLIDER DOUBLE : Repair Percentage
            mechanics.addEntry(entryBuilder.startIntSlider(Text.translatable("config.toolsmithsharper.repair_percentage"), (int)(ToolsmithSharper.REPAIR_PERCENTAGE * 100), 0, 100)
                    .setDefaultValue(10)
                    .setTooltip(Text.translatable("config.toolsmithsharper.repair_percentage.tooltip"))
                    .setTextGetter(value -> Text.literal(value + "%"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.REPAIR_PERCENTAGE = newValue / 100.0)
                    .build());


            // ==========================================
            // CATEGORY 2 : COMBAT BALANCE & MINING
            // ==========================================
            ConfigCategory balancing = builder.getOrCreateCategory(Text.translatable("config.toolsmithsharper.category.balancing"));

            // Champ DOUBLE : Damage Multiplier
            balancing.addEntry(entryBuilder.startDoubleField(Text.translatable("config.toolsmithsharper.damage_multiplier"), ToolsmithSharper.DAMAGE_MULTIPLIER)
                    .setDefaultValue(0.25)
                    .setMin(0.0)
                    .setTooltip(Text.translatable("config.toolsmithsharper.damage_multiplier.tooltip"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.DAMAGE_MULTIPLIER = newValue)
                    .build());

            // Champ DOUBLE : Speed Boost
            balancing.addEntry(entryBuilder.startDoubleField(Text.translatable("config.toolsmithsharper.speed_boost"), ToolsmithSharper.SPEED_BOOST)
                    .setDefaultValue(2.0)
                    .setMin(0.0)
                    .setTooltip(Text.translatable("config.toolsmithsharper.speed_boost.tooltip"))
                    .setSaveConsumer(newValue -> ToolsmithSharper.SPEED_BOOST = newValue)
                    .build());

            return builder.build();
        };
    }
}