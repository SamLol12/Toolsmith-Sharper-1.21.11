package com.samlol12.toolsmithsharper.mixin;

import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.util.ModUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "isItemBarVisible", at = @At("HEAD"), cancellable = true)
    private void showSharperBar(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ModUtils.getUses(stack) > 0) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
    private void getSharperBarStep(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int uses = ModUtils.getUses(stack);
        if (uses > 0) {
            String coating = ModUtils.getCoating(stack);
            String tier = ModUtils.getTier(stack);

            int max;
            if (coating.equals("none")) {
                max = ModUtils.isTool(stack) ? ModConfig.MAX_SHARPER_BASE_USES * 2 : ModConfig.MAX_SHARPER_BASE_USES;
            } else {
                max = tier.equals("extended") ? ModConfig.MAX_COATING_BASE_USES * 2 : ModConfig.MAX_COATING_BASE_USES;
            }

            float step = (float) uses / (float) max;
            cir.setReturnValue(Math.round(13.0f * step));
        }
    }

    @Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
    private void getSharperBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (ModUtils.getUses(stack) > 0) {
            String coating = ModUtils.getCoating(stack);
            int color = switch (coating) {
                case "fire" -> 0xFF8C00;    // Dark Orange
                case "poison" -> 0x006400;  // Dark Green
                case "vampire" -> 0x800000; // Maroon
                case "frost" -> 0x4682B4;   // Steel
                case "luck" -> 0xFFD700;    // Gold
                default -> 0xAAAAAA;        // Silver
            };
            cir.setReturnValue(color);
        }
    }
}