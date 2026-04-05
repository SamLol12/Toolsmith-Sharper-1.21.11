package com.audacelol12.toolsmithsharper.mixin;

import com.audacelol12.toolsmithsharper.ToolsmithSharper;
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
		if (stack.contains(ToolsmithSharper.SHARPER_USES)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getItemBarStep", at = @At("HEAD"), cancellable = true)
	private void getSharperBarStep(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.contains(ToolsmithSharper.SHARPER_USES)) {
			int uses = stack.getOrDefault(ToolsmithSharper.SHARPER_USES, 0);
			String coating = stack.getOrDefault(ToolsmithSharper.SHARPER_COATING, "none");
			String tier = stack.getOrDefault(ToolsmithSharper.SHARPER_COATING_TIER, "base");

			int max;
			if (coating.equals("none")) {
				max = ToolsmithSharper.isTool(stack) ? ToolsmithSharper.MAX_SHARPER_USES * 2 : ToolsmithSharper.MAX_SHARPER_USES;
			} else {
				max = tier.equals("extended") ? ToolsmithSharper.MAX_COATING_USES * 2 : ToolsmithSharper.MAX_COATING_USES;
			}

			float step = (float) uses / (float) max;
			cir.setReturnValue(Math.round(13.0f * step));
		}
	}

	@Inject(method = "getItemBarColor", at = @At("HEAD"), cancellable = true)
	private void getSharperBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (stack.contains(ToolsmithSharper.SHARPER_USES)) {
			String coating = stack.getOrDefault(ToolsmithSharper.SHARPER_COATING, "none");
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