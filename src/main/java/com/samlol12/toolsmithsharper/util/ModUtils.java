package com.samlol12.toolsmithsharper.util;

import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.registry.ModComponents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Map;
import java.util.UUID;

public class ModUtils {

    // =========================================================================
    // TAGS
    // =========================================================================
    public static boolean isWeapon(ItemStack stack) { return stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.AXES); }
    public static boolean isTool(ItemStack stack) { return stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SHOVELS) || stack.isIn(ItemTags.HOES); }
    public static boolean isAxe(ItemStack stack) { return stack.isIn(ItemTags.AXES); }
    public static boolean isSharpenable(ItemStack stack) { return isWeapon(stack) || isTool(stack) || isAxe(stack); }

    // =========================================================================
    // NBT HELPERS
    // =========================================================================
    public static int getUses(ItemStack stack) {
        return stack.hasNbt() ? stack.getNbt().getInt(ModComponents.SHARPER_USES) : 0;
    }

    public static String getCoating(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains(ModComponents.SHARPER_COATING) ? stack.getNbt().getString(ModComponents.SHARPER_COATING) : "none";
    }

    public static String getTier(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains(ModComponents.SHARPER_COATING_TIER) ? stack.getNbt().getString(ModComponents.SHARPER_COATING_TIER) : "base";
    }

    public static void setUses(ItemStack stack, int uses) {
        stack.getOrCreateNbt().putInt(ModComponents.SHARPER_USES, uses);
    }

    public static void setCoating(ItemStack stack, String coating, String tier) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(ModComponents.SHARPER_COATING, coating);
        nbt.putString(ModComponents.SHARPER_COATING_TIER, tier);
    }

    public static void removeSharperNBT(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound nbt = stack.getNbt();
            nbt.remove(ModComponents.SHARPER_USES);
            nbt.remove(ModComponents.SHARPER_COATING);
            nbt.remove(ModComponents.SHARPER_COATING_TIER);
        }
    }

    // =========================================================================

    public static boolean canSharpenPreview(PlayerEntity player, World world, ItemStack target, String coating, String tier) {
        if (target.isEmpty() || !isSharpenable(target)) return false;

        if (!coating.equals("none") && !coating.equals("luck")) {
            if (isTool(target) && !isAxe(target)) {
                if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.volatile_oil").formatted(Formatting.RED), true);
                return false;
            }
        }

        int usesToApply;
        if (coating.equals("none")) usesToApply = isTool(target) ? ModConfig.MAX_SHARPER_BASE_USES * 2 : ModConfig.MAX_SHARPER_BASE_USES;
        else usesToApply = tier.equals("extended") ? ModConfig.MAX_COATING_BASE_USES * 2 : ModConfig.MAX_COATING_BASE_USES;

        int currentUses = getUses(target);
        String currentCoating = getCoating(target);
        String currentTier = getTier(target);

        if (currentUses >= usesToApply && currentCoating.equals(coating) && currentTier.equals(tier)) {
            if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.already_honed").formatted(Formatting.YELLOW), true);
            return false;
        }

        if (player.experienceLevel < ModConfig.XP_COST && !player.getAbilities().creativeMode) {
            if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.need_xp", ModConfig.XP_COST).formatted(Formatting.RED), true);
            return false;
        }

        if (target.isDamageable()) {
            float durability = (float)(target.getMaxDamage() - target.getDamage()) / target.getMaxDamage();
            if (durability < 0.20f) {
                if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.too_damaged").formatted(Formatting.RED), true);
                return false;
            }
        }

        if (coating.equals("luck")) {
            int currentEnchantLevel = isWeapon(target) || isAxe(target) || isTool(target)
                ? EnchantmentHelper.getLevel(Enchantments.LOOTING, target)
                : EnchantmentHelper.getLevel(Enchantments.FORTUNE, target);

            int oilLevel = tier.equals("amplified") ? 3 : 1;
            if (currentEnchantLevel >= oilLevel) {
                if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.already_luck").formatted(Formatting.RED), true);
                return false;
            }
        }
        return true;
    }

    public static ActionResult trySharpen(PlayerEntity player, World world, ItemStack target, String coating, String tier) {
        if (!canSharpenPreview(player, world, target, coating, tier)) return ActionResult.FAIL;

        if (!world.isClient()) {
            String currentCoating = getCoating(target);
            applySharperEffect(world, target, coating, tier);

            if (target.isDamageable()) {
                int repairAmount = (int) (target.getMaxDamage() * ModConfig.REPAIR_PERCENTAGE);
                target.setDamage(Math.max(0, target.getDamage() - repairAmount));
            }

            if (!player.getAbilities().creativeMode) player.addExperienceLevels(-ModConfig.XP_COST);

            if (!coating.equals("none")) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.8f, 1.2f);
            }

            switch (coating) {
                case "none" -> {
                    if (!currentCoating.equals("none")) world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.PLAYERS, 1.0f, 1.5f);
                    else world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.PLAYERS, 1.0f, 1.5f);
                }
                case "fire" -> world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                case "poison" -> world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_SPIDER_AMBIENT, SoundCategory.PLAYERS, 1.0f, 1.5f);
                case "vampire" -> world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GHAST_SCREAM, SoundCategory.PLAYERS, 0.5f, 1.5f);
                case "frost" -> world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_POWDER_SNOW_BREAK, SoundCategory.PLAYERS, 0.5f, 2.0f);
                case "luck" -> world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.2f);
            }

            switch (coating) {
                case "fire" -> ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                case "frost" -> ((ServerWorld) world).spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                case "luck" -> ((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 15, 0.4, 0.4, 0.4, 0.1);
                default -> ((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
            }
        }
        return ActionResult.SUCCESS;
    }

    public static void applySharperEffect(World world, ItemStack stack, String coating, String tier) {
        int usesToApply = coating.equals("none") ? (isTool(stack) ? ModConfig.MAX_SHARPER_BASE_USES * 2 : ModConfig.MAX_SHARPER_BASE_USES)
                : (tier.equals("extended") ? ModConfig.MAX_COATING_BASE_USES * 2 : ModConfig.MAX_COATING_BASE_USES);

        setUses(stack, usesToApply);

        if (coating.equals("none")) {
            if (stack.hasNbt()) {
                stack.getNbt().remove(ModComponents.SHARPER_COATING);
                stack.getNbt().remove(ModComponents.SHARPER_COATING_TIER);
            }
        } else {
            setCoating(stack, coating, tier);
        }

        updateEnchantmentLevel(stack, Enchantments.FORTUNE, 0);
        updateEnchantmentLevel(stack, Enchantments.LOOTING, 0);

        if (coating.equals("luck")) {
            int level = tier.equals("amplified") ? 3 : 1;
            if (isWeapon(stack)) updateEnchantmentLevel(stack, Enchantments.LOOTING, level);
            else updateEnchantmentLevel(stack, Enchantments.FORTUNE, level);
        }

        clearSharperModifiers(stack);

        if (coating.equals("none")) {
            if (isWeapon(stack) || isAxe(stack)) {
                double currentWeaponDamage = getCurrentWeaponAttackDamage(stack);
                double sharpenBonus = currentWeaponDamage * ModConfig.DAMAGE_MULTIPLIER;

                stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(ModComponents.SHARPER_DAMAGE_ID, "Sharper Damage", sharpenBonus, EntityAttributeModifier.Operation.ADDITION),
                        EquipmentSlot.MAINHAND);
            }
            if (isTool(stack) || isAxe(stack)) {
                stack.getOrCreateNbt().putDouble("SharperMiningSpeed", ModConfig.SPEED_BOOST);
            }
        }
    }

    public static void applyCoatingHitEffects(PlayerEntity attacker, LivingEntity target, String coating, String tier) {
        if (coating.equals("none")) return;

        int duration = tier.equals("extended") ? ModConfig.EFFECT_DURATION_EXTENDED : ModConfig.EFFECT_DURATION_BASE;
        int amplifier = tier.equals("amplified") ? ModConfig.EFFECT_AMPLIFIER_AMPLIFIED : ModConfig.EFFECT_AMPLIFIER_BASE;

        switch (coating) {
            case "fire" -> {
                float fireSeconds = tier.equals("amplified") ? ModConfig.FIRE_SECONDS_AMPLIFIED : ModConfig.FIRE_SECONDS_BASE;
                target.setOnFireFor((int)fireSeconds);
            }
            case "poison" -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration, amplifier));
            case "frost" -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, amplifier));
            case "vampire" -> attacker.heal(tier.equals("amplified") ? ModConfig.VAMPIRE_HEAL_AMPLIFIED : ModConfig.VAMPIRE_HEAL_BASE);
            case "luck" -> attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, duration * 2, amplifier));
        }
    }

    public static void decrementUses(ItemStack stack, PlayerEntity player, World world) {
        int current = getUses(stack);
        if (current > 0) {
            String coating = getCoating(stack);
            int drainAmount = 1;

            if ((coating.equals("fire") || coating.equals("poison")) && player.isTouchingWaterOrRain()) {
                drainAmount = 2;
                net.minecraft.particle.ParticleEffect pType = coating.equals("fire") ? ParticleTypes.SMOKE : ParticleTypes.BUBBLE;
                ((ServerWorld)world).spawnParticles(pType, player.getX(), player.getY() + 1, player.getZ(), 3, 0.1, 0.1, 0.1, 0.02);
            }
            else if (coating.equals("poison")) {
                var biomeEntry = world.getBiome(player.getBlockPos());
                boolean isSwamp = biomeEntry.matchesKey(BiomeKeys.SWAMP) || biomeEntry.matchesKey(BiomeKeys.MANGROVE_SWAMP);
                boolean isJungle = biomeEntry.matchesKey(BiomeKeys.JUNGLE) || biomeEntry.matchesKey(BiomeKeys.SPARSE_JUNGLE) || biomeEntry.matchesKey(BiomeKeys.BAMBOO_JUNGLE);

                if (isSwamp || isJungle || world.isRaining()) {
                    if (world.random.nextFloat() < 0.5f) drainAmount = 0;
                }
            }
            else if (coating.equals("frost")) {
                var biomeEntry = world.getBiome(player.getBlockPos());
                boolean isHot = biomeEntry.value().getTemperature() > 1.0f;

                if (isHot || player.isInLava() || player.isOnFire()) {
                    drainAmount = 2;
                    if (!world.isClient()) {
                        ((ServerWorld)world).spawnParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1, player.getZ(), 3, 0.1, 0.1, 0.1, 0.02);
                    }
                } else if (biomeEntry.value().isCold(player.getBlockPos())) {
                    if (world.random.nextFloat() < 0.5f) drainAmount = 0;
                }
            }
            else if ((coating.equals("luck") || coating.equals("vampire")) && world.isNight()) {
                if (world.random.nextFloat() < (coating.equals("luck") ? 0.3f : 0.5f)) drainAmount = 0;
            }

            if (drainAmount == 0) return;

            if (current <= drainAmount) {
                if (coating.equals("luck")) {
                    updateEnchantmentLevel(stack, Enchantments.FORTUNE, 0);
                    updateEnchantmentLevel(stack, Enchantments.LOOTING, 0);
                }

                removeSharperNBT(stack);
                clearSharperModifiers(stack);

                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.6f, 1.8f);
                if (!world.isClient()) {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1, player.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
                }
            } else {
                setUses(stack, current - drainAmount);
            }
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================
    private static void clearSharperModifiers(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("AttributeModifiers", 9)) {
            NbtList list = stack.getNbt().getList("AttributeModifiers", 10);
            for (int i = list.size() - 1; i >= 0; i--) {
                NbtCompound mod = list.getCompound(i);
                if (mod.contains("UUID")) {
                    UUID uuid = mod.getUuid("UUID");
                    if (uuid.equals(ModComponents.SHARPER_DAMAGE_ID) || uuid.equals(ModComponents.SHARPER_SPEED_ID)) {
                        list.remove(i);
                    }
                }
            }
            if (list.isEmpty()) {
                stack.getNbt().remove("AttributeModifiers");
            }
        }
        if (stack.hasNbt()) {
            stack.getNbt().remove("SharperMiningSpeed");
        }
    }

    private static double getCurrentWeaponAttackDamage(ItemStack stack) {
        double totalAttackDamage = 0.0;
        var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
                .get(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        for (EntityAttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                totalAttackDamage += modifier.getValue();
            }
        }

        return Math.max(0.0, totalAttackDamage);
    }

    private static void updateEnchantmentLevel(ItemStack stack, Enchantment enchantment, int level) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        if (level <= 0) {
            enchantments.remove(enchantment);
        } else {
            enchantments.put(enchantment, level);
        }
        EnchantmentHelper.set(enchantments, stack);
    }

    public static Hand getOppositeHand(Hand hand) {
        return (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }
}