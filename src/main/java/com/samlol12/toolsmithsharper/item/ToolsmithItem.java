package com.samlol12.toolsmithsharper.item;

import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.util.ModUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

public class ToolsmithItem extends Item {
    public final int useTime;
    public final String coating;
    public final boolean consume;

    public ToolsmithItem(Settings settings, int useTime, String coating, boolean consume) {
        super(settings);
        this.useTime = useTime;
        this.coating = coating;
        this.consume = consume;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!this.coating.equals("none")) return TypedActionResult.pass(stack);

        Hand otherHand = ModUtils.getOppositeHand(hand);
        ItemStack target = user.getStackInHand(otherHand);
        String tier = ModUtils.getTier(stack);

        if (ModUtils.canSharpenPreview(user, world, target, this.coating, tier)) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return TypedActionResult.fail(stack);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BRUSH;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return this.coating.equals("none") ? ModConfig.WHETSTONE_USE_TIME : useTime;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity player) {
            Hand hand = player.getStackInHand(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
            Hand otherHand = ModUtils.getOppositeHand(hand);
            ItemStack target = player.getStackInHand(otherHand);
            String tier = ModUtils.getTier(stack);

            if (ModUtils.trySharpen(player, world, target, coating, tier) == ActionResult.SUCCESS) {
                player.swingHand(hand);
                if (consume) {
                    stack.decrement(1);
                } else {
                    if (!world.isClient()) {
                        stack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                    }
                }
                player.getItemCooldownManager().set(this, 20);
            }
        }
        return stack;
    }
}