package com.samlol12.toolsmithsharper.util;

import com.samlol12.toolsmithsharper.config.ModConfig;
import com.samlol12.toolsmithsharper.registry.ModComponents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.util.Hand;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;

public class ModUtils {

    // =========================================================================
	// TAGS
	// =========================================================================
    public static boolean isWeapon(ItemStack stack) { return stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.SPEARS); }
    public static boolean isTool(ItemStack stack) { return stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SHOVELS) || stack.isIn(ItemTags.HOES); }
    public static boolean isAxe(ItemStack stack) { return stack.isIn(ItemTags.AXES); }
    public static boolean isSharpenable(ItemStack stack) { return isWeapon(stack) || isTool(stack) || isAxe(stack); }
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

        int currentUses = target.getOrDefault(ModComponents.SHARPER_USES, 0);
        String currentCoating = target.getOrDefault(ModComponents.SHARPER_COATING, "none");
        String currentTier = target.getOrDefault(ModComponents.SHARPER_COATING_TIER, "base");

        // Already max sharpened
        if (currentUses >= usesToApply && currentCoating.equals(coating) && currentTier.equals(tier)) {
            if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.already_honed").formatted(Formatting.YELLOW), true);
            return false;
        }

        // XP Check
        if (player.experienceLevel < ModConfig.XP_COST && !player.getAbilities().creativeMode) {
            if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.need_xp", ModConfig.XP_COST).formatted(Formatting.RED), true);
            return false;
        }

        // Durability Check
        if (target.isDamageable()) {
            float durability = (float)(target.getMaxDamage() - target.getDamage()) / target.getMaxDamage();
            if (durability < 0.20f) {
                if (world.isClient()) player.sendMessage(Text.translatable("message.toolsmithsharper.too_damaged").formatted(Formatting.RED), true);
                return false;
            }
        }

        // Coating-specific checks
        if (coating.equals("luck")) {
            int currentEnchantLevel = isWeapon(target) || isAxe(target) || isTool(target)
                ? EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), target)
                : EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), target);
            
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
            String currentCoating = target.getOrDefault(ModComponents.SHARPER_COATING, "none");
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
				case "fire" ->
						((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
				case "frost" ->
						((ServerWorld) world).spawnParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
				case "luck" ->
						((ServerWorld) world).spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 15, 0.4, 0.4, 0.4, 0.1);
				default ->
						((ServerWorld) world).spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
			}
            
        }
        return ActionResult.SUCCESS;
    }

    // =========================================================================
    // COMBAT EFFECTS
    // =========================================================================
    public static void applySharperEffect(World world, ItemStack stack, String coating, String tier) {
        int usesToApply = coating.equals("none") ? (isTool(stack) ? ModConfig.MAX_SHARPER_BASE_USES * 2 : ModConfig.MAX_SHARPER_BASE_USES) 
                : (tier.equals("extended") ? ModConfig.MAX_COATING_BASE_USES * 2 : ModConfig.MAX_COATING_BASE_USES);
        stack.set(ModComponents.SHARPER_USES, usesToApply);

        if (coating.equals("none")) {
            stack.remove(ModComponents.SHARPER_COATING);
            stack.remove(ModComponents.SHARPER_COATING_TIER);
        } else {
            stack.set(ModComponents.SHARPER_COATING, coating);
            stack.set(ModComponents.SHARPER_COATING_TIER, tier);
        }

        updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, 0);
        updateEnchantmentLevel(world, stack, Enchantments.LOOTING, 0);

        if (coating.equals("luck")) {
            int level = tier.equals("amplified") ? 3 : 1;
            if (isWeapon(stack)) updateEnchantmentLevel(world, stack, Enchantments.LOOTING, level);
            else updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, level);
        }

        AttributeModifiersComponent.Builder attrBuilder = getModifiersBuilder(stack);
        if (coating.equals("none")) {
            if (isWeapon(stack) || isAxe(stack)) {
                attrBuilder.add(EntityAttributes.ATTACK_DAMAGE,
                        new EntityAttributeModifier(ModComponents.SHARPER_DAMAGE_ID, ModConfig.DAMAGE_MULTIPLIER, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                        AttributeModifierSlot.MAINHAND);
            }
            if (isTool(stack) || isAxe(stack)) {
                attrBuilder.add(EntityAttributes.MINING_EFFICIENCY,
                        new EntityAttributeModifier(ModComponents.SHARPER_SPEED_ID, ModConfig.SPEED_BOOST, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND);
            }
        }
        stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
    }

    public static void applyCoatingHitEffects(PlayerEntity attacker, LivingEntity target, String coating, String tier) {
        if (coating.equals("none")) return;

        // Dynamically calculate duration and amplifier based on tier
        int duration = tier.equals("extended") ? ModConfig.EFFECT_DURATION_EXTENDED : ModConfig.EFFECT_DURATION_BASE;
        int amplifier = tier.equals("amplified") ? ModConfig.EFFECT_AMPLIFIER_AMPLIFIED : ModConfig.EFFECT_AMPLIFIER_BASE;

        switch (coating) {
            case "fire" -> {
                float fireSeconds = tier.equals("amplified") ? ModConfig.FIRE_SECONDS_AMPLIFIED : ModConfig.FIRE_SECONDS_BASE;
                target.setOnFireFor(fireSeconds);
            }
            case "poison" -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration, amplifier));
            case "frost" -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, duration, amplifier));
            case "vampire" -> attacker.heal(tier.equals("amplified") ? ModConfig.VAMPIRE_HEAL_AMPLIFIED : ModConfig.VAMPIRE_HEAL_BASE);
            case "luck" -> attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, duration * 2, amplifier));
        }
    }

    public static void decrementUses(ItemStack stack, PlayerEntity player, World world) {
        if (stack.contains(ModComponents.SHARPER_USES)) {
			int current = stack.getOrDefault(ModComponents.SHARPER_USES, 0);
			String coating = stack.getOrDefault(ModComponents.SHARPER_COATING, "none");
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
				} else if (biomeEntry.value().isCold(player.getBlockPos(), world.getSeaLevel())) {
					if (world.random.nextFloat() < 0.5f) drainAmount = 0;
				}
			}
			else if ((coating.equals("luck") || coating.equals("vampire")) && world.isNight()) {
				if (world.random.nextFloat() < (coating.equals("luck") ? 0.3f : 0.5f)) drainAmount = 0;
			}

			if (drainAmount == 0) return;

			if (current <= drainAmount) {
				String currentCoating = stack.getOrDefault(ModComponents.SHARPER_COATING, "none");

				if (currentCoating.equals("luck")) {
					updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, 0);
					updateEnchantmentLevel(world, stack, Enchantments.LOOTING, 0);
				}

				stack.remove(ModComponents.SHARPER_USES);
				stack.remove(ModComponents.SHARPER_COATING);
				stack.remove(ModComponents.SHARPER_COATING_TIER);
				stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);

                // Feedback sounds and particles
				world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.6f, 1.8f);
                if (!world.isClient()) {
                    ((ServerWorld) world).spawnParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1, player.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
                }
			} else {
				stack.set(ModComponents.SHARPER_USES, current - drainAmount);
			}
		}
    }
    // =========================================================================

    // =========================================================================
    // HELPERS
    // =========================================================================
    public static AttributeModifiersComponent.Builder getModifiersBuilder(ItemStack stack) {
        AttributeModifiersComponent currentMods = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (currentMods == null) {
            currentMods = stack.getItem().getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
        }
        
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        for (AttributeModifiersComponent.Entry entry : currentMods.modifiers()) {
            if (!entry.modifier().id().equals(ModComponents.SHARPER_DAMAGE_ID) && !entry.modifier().id().equals(ModComponents.SHARPER_SPEED_ID)) {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        return builder;
    }

    private static void updateEnchantmentLevel(World world, ItemStack stack, RegistryKey<Enchantment> enchantmentKey, int level) {
        Registry<Enchantment> registry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent currentEnchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(currentEnchants);

        if (level <= 0) {
            builder.remove(e -> e.matchesKey(enchantmentKey));
        } else {
            builder.set(registry.getOrThrow(enchantmentKey), level);
        }

        stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }

    public static Hand getOppositeHand(Hand hand) {
        return (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }
}