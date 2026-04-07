package com.samlol12.toolsmithsharper.mixin;

import com.samlol12.toolsmithsharper.util.ModUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderHotbarItem", at = @At("HEAD"))
    private void drawSharperHotbarBorder(DrawContext context, int x, int y, float tickDelta, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (stack != null && ModUtils.getUses(stack) > 0) {
            String coating = ModUtils.getCoating(stack);
            
            int color = switch (coating) {
                case "fire" -> 0xFF8C00;
                case "poison" -> 0x006400;
                case "vampire" -> 0x800000;
                case "frost" -> 0x4682B4;
                case "luck" -> 0xFFD700;
                default -> 0xAAAAAA;
            };

            int borderColor = color | 0xFF000000;

            context.fill(x - 1, y - 1, x + 17, y, borderColor);         // Top
            context.fill(x - 1, y + 16, x + 17, y + 17, borderColor);   // Bottom
            context.fill(x - 1, y, x, y + 16, borderColor);             // Left
            context.fill(x + 16, y, x + 17, y + 16, borderColor);       // Right
        }
    }
}