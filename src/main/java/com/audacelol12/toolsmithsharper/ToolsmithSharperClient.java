package com.audacelol12.toolsmithsharper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;

public class ToolsmithSharperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.contains(ToolsmithSharper.SHARPER_USES)) {
                int uses = stack.getOrDefault(ToolsmithSharper.SHARPER_USES, 0);
                String coating = stack.getOrDefault(ToolsmithSharper.SHARPER_COATING, "none");
                String tier = stack.getOrDefault(ToolsmithSharper.SHARPER_COATING_TIER, "base");

                String suffix = "";
                if (tier.equals("amplified")) suffix = " II";
                else if (tier.equals("extended")) suffix = " (Extended)";

                if (uses > 0) {
                    lines.add(Text.literal(""));

                    switch (coating) {
                        case "fire" -> {
                            lines.add(Text.literal("§6§l♦ §cEffect: Fire Oil" + suffix));
                            lines.add(Text.literal("  §7Charges: §c" + uses));
                        }
                        case "poison" -> {
                            lines.add(Text.literal("§2§l♦ §2Effect: Poison Oil" + suffix));
                            lines.add(Text.literal("  §7Charges: §2" + uses));
                        }
                        case "vampire" -> {
                            lines.add(Text.literal("§4§l♦ §4Effect: Vampire Oil" + suffix));
                            lines.add(Text.literal("  §7Charges: §4" + uses));
                        }
                        case "frost" -> {
                            lines.add(Text.literal("§1§l♦ §9Effect: Frost Oil" + suffix));
                            lines.add(Text.literal("  §7Charges: §9" + uses));
                        }
                        case "luck" -> {
                            lines.add(Text.literal("§6§l♦ §eEffect: Fortune Oil" + suffix));
                            lines.add(Text.literal("  §7Charges: §e" + uses));
                        }
                        default -> {
                            lines.add(Text.literal("§8§l♦ §7Status: Honed Edge"));
                            lines.add(Text.literal("  §7Charges: §7" + uses));
                        }
                    }
                }
            }
        });
    }
}