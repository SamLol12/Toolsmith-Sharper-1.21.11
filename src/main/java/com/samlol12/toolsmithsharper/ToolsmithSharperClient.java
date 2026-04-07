package com.samlol12.toolsmithsharper;

import com.samlol12.toolsmithsharper.util.ModUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;

public class ToolsmithSharperClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            int uses = ModUtils.getUses(stack);
            
            if (uses > 0) {
                String coating = ModUtils.getCoating(stack);
                String tier = ModUtils.getTier(stack);

                Text suffix = Text.literal("");
                if (tier.equals("amplified")) {
                    suffix = Text.literal(" II");
                } else if (tier.equals("extended")) {
                    suffix = Text.translatable("tooltip.toolsmithsharper.tier.extended");
                }

                lines.add(Text.literal(""));

                switch (coating) {
                    case "fire" -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.effect.fire", suffix));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.fire", uses));
                    }
                    case "poison" -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.effect.poison", suffix));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.poison", uses));
                    }
                    case "vampire" -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.effect.vampire", suffix));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.vampire", uses));
                    }
                    case "frost" -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.effect.frost", suffix));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.frost", uses));
                    }
                    case "luck" -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.effect.luck", suffix));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.luck", uses));
                    }
                    default -> {
                        lines.add(Text.translatable("tooltip.toolsmithsharper.status.honed"));
                        lines.add(Text.translatable("tooltip.toolsmithsharper.charges.honed", uses));
                    }
                }
            }
        });
    }
}