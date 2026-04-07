package com.samlol12.toolsmithsharper.mixin;

import com.samlol12.toolsmithsharper.util.ModUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamagedBySharperOil(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        
        if (cir.getReturnValue() && source.getAttacker() instanceof PlayerEntity player && source.getSource() == player) {
            ItemStack stack = player.getMainHandStack();
            
            if (ModUtils.getUses(stack) > 0) {
                LivingEntity target = (LivingEntity) (Object) this;
                String coating = ModUtils.getCoating(stack);
                String tier = ModUtils.getTier(stack);

                ModUtils.applyCoatingHitEffects(player, target, coating, tier);

                ModUtils.decrementUses(stack, player, target.getWorld());
            }
        }
    }
}