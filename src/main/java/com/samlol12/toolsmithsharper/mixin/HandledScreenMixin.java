package com.samlol12.toolsmithsharper.mixin;

import com.samlol12.toolsmithsharper.util.ModUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void drawSharperSlotBorder(DrawContext context, Slot slot, CallbackInfo ci) {
        ItemStack stack = slot.getStack();
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

            int posX = slot.x;
            int posY = slot.y;

            context.fill(posX - 1, posY - 1, posX + 17, posY, borderColor);         // Top
            context.fill(posX - 1, posY + 16, posX + 17, posY + 17, borderColor);   // Down
            context.fill(posX - 1, posY, posX, posY + 16, borderColor);             // Left
            context.fill(posX + 16, posY, posX + 17, posY + 16, borderColor);       // Right
        }
    }
}