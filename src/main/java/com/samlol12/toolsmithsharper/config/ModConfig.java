package com.samlol12.toolsmithsharper.config;

import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Toolsmith Sharper Config");
    
    public static int MAX_SHARPER_BASE_USES = 32;
    public static int MAX_COATING_BASE_USES = 10;
    public static double DAMAGE_MULTIPLIER = 0.25;
    public static double SPEED_BOOST = 2.0;
    public static int XP_COST = 1;
    public static double REPAIR_PERCENTAGE = 0.10;
    public static int MAX_WHETSTONE_USES = 3;
    public static int WHETSTONE_USE_TIME = 40;

    public static int EFFECT_DURATION_BASE = 100;       // 5 seconds (ticks)
    public static int EFFECT_DURATION_EXTENDED = 200;   // 10 seconds (ticks)
    public static int EFFECT_AMPLIFIER_BASE = 0;        // Level 1
    public static int EFFECT_AMPLIFIER_AMPLIFIED = 1;   // Level 2
    public static float FIRE_SECONDS_BASE = 4.0f;       // 4 seconds
    public static float FIRE_SECONDS_AMPLIFIED = 8.0f;  // 8 seconds
    public static float VAMPIRE_HEAL_BASE = 1.0f;       // half heart
    public static float VAMPIRE_HEAL_AMPLIFIED = 2.0f;  // full heart

    private static File getConfigFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("toolsmithsharper.properties").toFile();
    }

    public static void loadConfig() {
        try {
            File file = getConfigFile();
            if (file.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(file));
                MAX_SHARPER_BASE_USES = Integer.parseInt(props.getProperty("maxUses", "32"));
                MAX_COATING_BASE_USES = Integer.parseInt(props.getProperty("maxCoatingUses", "10"));
                MAX_WHETSTONE_USES = Integer.parseInt(props.getProperty("maxWhetstoneUses", "3"));
                WHETSTONE_USE_TIME = Integer.parseInt(props.getProperty("whetstoneUseTime", "40"));
                DAMAGE_MULTIPLIER = Double.parseDouble(props.getProperty("damageMultiplier", "0.25"));
                SPEED_BOOST = Double.parseDouble(props.getProperty("speedBoost", "2.0"));
                XP_COST = Integer.parseInt(props.getProperty("xpCost", "1"));
                REPAIR_PERCENTAGE = Double.parseDouble(props.getProperty("repairPercentage", "0.10"));

                EFFECT_DURATION_BASE = Integer.parseInt(props.getProperty("effectDurationBase", "100"));
                EFFECT_DURATION_EXTENDED = Integer.parseInt(props.getProperty("effectDurationExtended", "200"));
                EFFECT_AMPLIFIER_BASE = Integer.parseInt(props.getProperty("effectAmplifierBase", "0"));
                EFFECT_AMPLIFIER_AMPLIFIED = Integer.parseInt(props.getProperty("effectAmplifierAmplified", "1"));
                FIRE_SECONDS_BASE = Float.parseFloat(props.getProperty("fireSecondsBase", "4.0"));
                FIRE_SECONDS_AMPLIFIED = Float.parseFloat(props.getProperty("fireSecondsAmplified", "8.0"));
                VAMPIRE_HEAL_BASE = Float.parseFloat(props.getProperty("vampireHealBase", "1.0"));
                VAMPIRE_HEAL_AMPLIFIED = Float.parseFloat(props.getProperty("vampireHealAmplified", "2.0"));
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            LOGGER.error("Error loading Toolsmith Sharper config", e);
        }
    }

    public static void saveConfig() {
        try {
            Properties props = new Properties();
            props.setProperty("maxUses", String.valueOf(MAX_SHARPER_BASE_USES));
            props.setProperty("maxCoatingUses", String.valueOf(MAX_COATING_BASE_USES));
            props.setProperty("maxWhetstoneUses", String.valueOf(MAX_WHETSTONE_USES));
            props.setProperty("whetstoneUseTime", String.valueOf(WHETSTONE_USE_TIME));
            props.setProperty("damageMultiplier", String.valueOf(DAMAGE_MULTIPLIER));
            props.setProperty("speedBoost", String.valueOf(SPEED_BOOST));
            props.setProperty("xpCost", String.valueOf(XP_COST));
            props.setProperty("repairPercentage", String.valueOf(REPAIR_PERCENTAGE));

            props.setProperty("effectDurationBase", String.valueOf(EFFECT_DURATION_BASE));
            props.setProperty("effectDurationExtended", String.valueOf(EFFECT_DURATION_EXTENDED));
            props.setProperty("effectAmplifierBase", String.valueOf(EFFECT_AMPLIFIER_BASE));
            props.setProperty("effectAmplifierAmplified", String.valueOf(EFFECT_AMPLIFIER_AMPLIFIED));
            props.setProperty("fireSecondsBase", String.valueOf(FIRE_SECONDS_BASE));
            props.setProperty("fireSecondsAmplified", String.valueOf(FIRE_SECONDS_AMPLIFIED));
            props.setProperty("vampireHealBase", String.valueOf(VAMPIRE_HEAL_BASE));
            props.setProperty("vampireHealAmplified", String.valueOf(VAMPIRE_HEAL_AMPLIFIED));
            
            props.store(new FileOutputStream(getConfigFile()), "Configuration of Toolsmith Sharper");
        } catch (Exception e) {
            LOGGER.error("Error saving Toolsmith Sharper config", e);
        }
    }
}