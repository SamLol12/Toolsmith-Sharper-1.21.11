package com.samlol12.toolsmithsharper;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.item.consume.UseAction;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.world.biome.BiomeKeys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class ToolsmithSharper implements ModInitializer {

	public static final String MOD_ID = "toolsmithsharper";

	// ==========================================
	// CONFIGURATION
	// ==========================================
	public static int MAX_SHARPER_USES = 32;
	public static int MAX_COATING_USES = 10;
	public static double DAMAGE_MULTIPLIER = 0.25;
	public static double SPEED_BOOST = 2.0;
	public static int XP_COST = 1;
	public static double REPAIR_PERCENTAGE = 0.10;

	// ==========================================
	// ITEMS CLASS
	// ==========================================
	public static class ToolsmithItem extends Item {
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
		public ActionResult use(World world, PlayerEntity user, Hand hand) {
			if (!this.coating.equals("none")) {
				return ActionResult.PASS;
			}

			ItemStack stack = user.getStackInHand(hand);
			Hand otherHand = (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
			ItemStack target = user.getStackInHand(otherHand);
			String tier = stack.getOrDefault(SHARPER_COATING_TIER, "base");

			if (ToolsmithSharper.canSharpenPreview(user, world, target, this.coating, tier)) {
				user.setCurrentHand(hand);
				return ActionResult.CONSUME;
			}
			return ActionResult.FAIL;
		}

		@Override
		public UseAction getUseAction(ItemStack stack) {
			return UseAction.BRUSH;
		}

		@Override
		public int getMaxUseTime(ItemStack stack, LivingEntity user) {
			return useTime;
		}

		@Override
		public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
			if (user instanceof PlayerEntity player) {
				Hand hand = player.getStackInHand(Hand.MAIN_HAND) == stack ? Hand.MAIN_HAND : Hand.OFF_HAND;
				Hand otherHand = (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack target = player.getStackInHand(otherHand);
				String tier = stack.getOrDefault(SHARPER_COATING_TIER, "base");

				if (ToolsmithSharper.trySharpen(player, world, target, coating, tier) == ActionResult.SUCCESS) {
					player.swingHand(hand);

					if (consume) {
						stack.decrement(1);
					} else {
						if (!world.isClient()) {
							stack.damage(1, (ServerWorld) world, player instanceof ServerPlayerEntity ? (ServerPlayerEntity) player : null,
									item -> player.sendEquipmentBreakStatus(item, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
						}
					}
					player.getItemCooldownManager().set(stack, 20);
				}
			}
			return stack;
		}
	}

	// ==========================================
	// ITEMS & COMPONENTS
	// ==========================================
	public static final RegistryKey<Item> WHETSTONE_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "whetstone"));
	public static final Item WHETSTONE = Registry.register(Registries.ITEM, WHETSTONE_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(WHETSTONE_KEY).maxDamage(3), 30, "none", false));

	public static final RegistryKey<Item> FIRE_OIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "fire_oil"));
	public static final Item FIRE_OIL = Registry.register(Registries.ITEM, FIRE_OIL_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(FIRE_OIL_KEY), 12, "fire", true));

	public static final RegistryKey<Item> POISON_OIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "poison_oil"));
	public static final Item POISON_OIL = Registry.register(Registries.ITEM, POISON_OIL_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(POISON_OIL_KEY), 12, "poison", true));

	public static final RegistryKey<Item> VAMPIRE_OIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "vampire_oil"));
	public static final Item VAMPIRE_OIL = Registry.register(Registries.ITEM, VAMPIRE_OIL_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(VAMPIRE_OIL_KEY), 12, "vampire", true));

	public static final RegistryKey<Item> FROST_OIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "frost_oil"));
	public static final Item FROST_OIL = Registry.register(Registries.ITEM, FROST_OIL_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(FROST_OIL_KEY), 12, "frost", true));

	public static final RegistryKey<Item> LUCK_OIL_KEY = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "luck_oil"));
	public static final Item LUCK_OIL = Registry.register(Registries.ITEM, LUCK_OIL_KEY.getValue(),
			new ToolsmithItem(new Item.Settings().registryKey(LUCK_OIL_KEY), 12, "luck", true));

	public static final ComponentType<Integer> SHARPER_USES = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "sharper_uses"), ComponentType.<Integer>builder().codec(Codec.INT).build());
	public static final ComponentType<String> SHARPER_COATING = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "sharper_coating"), ComponentType.<String>builder().codec(Codec.STRING).build());

	public static final ComponentType<String> SHARPER_COATING_TIER = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "sharper_coating_tier"), ComponentType.<String>builder().codec(Codec.STRING).build());

	public static final Identifier SHARPER_DAMAGE_ID = Identifier.of(MOD_ID, "sharper_damage");
	public static final Identifier SHARPER_SPEED_ID = Identifier.of(MOD_ID, "sharper_speed");

	@Override
	public void onInitialize() {
		loadConfig();
		registerCommands();

		// Grindstone
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (!player.isSneaking() || hand != Hand.MAIN_HAND) return ActionResult.PASS;
			BlockState state = world.getBlockState(hitResult.getBlockPos());
			if (state.isOf(Blocks.GRINDSTONE)) {
				ItemStack offStack = player.getOffHandStack();
				String coating = "none";
				boolean isValidItem = false;

				if (offStack.isOf(Items.FLINT)) { isValidItem = true; coating = "none"; }
				else if (offStack.isOf(FIRE_OIL)) { isValidItem = true; coating = "fire"; }
				else if (offStack.isOf(POISON_OIL)) { isValidItem = true; coating = "poison"; }
				else if (offStack.isOf(VAMPIRE_OIL)) { isValidItem = true; coating = "vampire"; }
				else if (offStack.isOf(FROST_OIL)) { isValidItem = true; coating = "frost"; }
				else if (offStack.isOf(LUCK_OIL)) { isValidItem = true; coating = "luck"; }

				if (!isValidItem) {
					if (world.isClient()) player.sendMessage(Text.literal("§cYou need Flint or a Coating Oil in your offhand!"), true);
					return ActionResult.FAIL;
				}

				String tier = offStack.getOrDefault(SHARPER_COATING_TIER, "base");

				ActionResult result = trySharpen(player, world, player.getMainHandStack(), coating, tier);
				if (result == ActionResult.SUCCESS && !world.isClient()) {
					offStack.decrement(1);
					player.getItemCooldownManager().set(player.getMainHandStack(), 60);
				}
				return result;
			}
			return ActionResult.PASS;
		});

		// Whetstone
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stackInHand = player.getStackInHand(hand);
			if (stackInHand.getItem() instanceof ToolsmithItem toolsmithItem) {
				Hand otherHand = (hand == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
				ItemStack targetStack = player.getStackInHand(otherHand);
				String tier = stackInHand.getOrDefault(SHARPER_COATING_TIER, "base");

				if (canSharpenPreview(player, world, targetStack, toolsmithItem.coating, tier)) {
					player.setCurrentHand(hand);
					return ActionResult.SUCCESS;
				}
				return ActionResult.FAIL;
			}
			return ActionResult.PASS;
		});

		// Callback
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient() && hand == Hand.MAIN_HAND) {
				ItemStack stack = player.getStackInHand(hand);
				if (stack.contains(SHARPER_USES) && entity instanceof LivingEntity livingTarget) {
					String coating = stack.getOrDefault(SHARPER_COATING, "none");
					String tier = stack.getOrDefault(SHARPER_COATING_TIER, "base");
					switch (coating) {
						case "fire" -> livingTarget.setOnFireFor(tier.equals("amplified") ? 8 : 4);
						case "poison" -> livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, tier.equals("amplified") ? 1 : 0));
						case "vampire" -> player.heal(tier.equals("amplified") ? 2.0f : 1.0f);
						case "frost" -> livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, tier.equals("amplified") ? 1 : 0));
						case "luck" -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 200, tier.equals("amplified") ? 1 : 0));
					}
				}
				decrementUses(stack, player, world);
			}
			return ActionResult.PASS;
		});

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!world.isClient()) decrementUses(player.getMainHandStack(), player, world);
		});
	}

	public static boolean canSharpenPreview(PlayerEntity player, World world, ItemStack target, String coating, String tier) {
		if (target.isEmpty() || !isSharpenable(target)) return false;

		if (!coating.equals("none") && !coating.equals("luck")) {
			if (isTool(target) && !isAxe(target)) {
				if (world.isClient()) player.sendMessage(Text.literal("§cThis oil is too volatile for gathering tools! Use it on weapons."), true);
				return false;
			}
		}

		int usesToApply;
		if (coating.equals("none")) usesToApply = isTool(target) ? MAX_SHARPER_USES * 2 : MAX_SHARPER_USES;
		else usesToApply = tier.equals("extended") ? MAX_COATING_USES * 2 : MAX_COATING_USES;

		int currentUses = target.getOrDefault(SHARPER_USES, 0);
		String currentCoating = target.getOrDefault(SHARPER_COATING, "none");
		String currentTier = target.getOrDefault(SHARPER_COATING_TIER, "base");

		// Already max sharpened
		if (currentUses >= usesToApply && currentCoating.equals(coating) && currentTier.equals(tier)) {
			if (world.isClient()) player.sendMessage(Text.literal("§eThis item is already perfectly honed with this oil!"), true);
			return false;
		}

		// XP Check
		if (player.experienceLevel < XP_COST && !player.getAbilities().creativeMode) {
			if (world.isClient()) player.sendMessage(Text.literal("§cYou need " + XP_COST + " XP level!"), true);
			return false;
		}

		// Durability Check
		if (target.isDamageable()) {
			float durability = (float)(target.getMaxDamage() - target.getDamage()) / target.getMaxDamage();
			if (durability < 0.20f) {
				if (world.isClient()) player.sendMessage(Text.literal("§cTool too damaged! (Min 20%)"), true);
				return false;
			}
		}
		return true;
	}

	public static ActionResult trySharpen(PlayerEntity player, World world, ItemStack target, String coating, String tier) {
		if (!canSharpenPreview(player, world, target, coating, tier)) return ActionResult.FAIL;

		if (!world.isClient()) {
			String currentCoating = target.getOrDefault(SHARPER_COATING, "none");
			applySharperEffect(world, target, coating, tier);
			if (target.isDamageable()) {
				int repairAmount = (int) (target.getMaxDamage() * REPAIR_PERCENTAGE);
				target.setDamage(Math.max(0, target.getDamage() - repairAmount));
			}

			if (!player.getAbilities().creativeMode) player.addExperienceLevels(-XP_COST);

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
	// TAGS
	// =========================================================================
	public static boolean isWeapon(ItemStack stack) { return stack.isIn(ItemTags.SWORDS) || stack.isIn(ItemTags.SPEARS); }
	public static boolean isTool(ItemStack stack) { return stack.isIn(ItemTags.PICKAXES) || stack.isIn(ItemTags.SHOVELS) || stack.isIn(ItemTags.HOES); }
	public static boolean isAxe(ItemStack stack) { return stack.isIn(ItemTags.AXES); }
	public static boolean isSharpenable(ItemStack stack) { return isWeapon(stack) || isTool(stack) || isAxe(stack); }
	// =========================================================================

	public static void applySharperEffect(World world, ItemStack stack, String coating, String tier) {
		int usesToApply = coating.equals("none") ? (isTool(stack) ? MAX_SHARPER_USES * 2 : MAX_SHARPER_USES) : (tier.equals("extended") ? MAX_COATING_USES * 2 : MAX_COATING_USES);
		stack.set(SHARPER_USES, usesToApply);

		if (coating.equals("none")) {
			stack.remove(SHARPER_COATING);
			stack.remove(SHARPER_COATING_TIER);
		} else {
			stack.set(SHARPER_COATING, coating);
			stack.set(SHARPER_COATING_TIER, tier);
		}

		updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, 0);
		updateEnchantmentLevel(world, stack, Enchantments.LOOTING, 0);

		if (coating.equals("luck")) {
			int level = tier.equals("amplified") ? 3 : 1;
			if (isWeapon(stack)) updateEnchantmentLevel(world, stack, Enchantments.LOOTING, level);
			else updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, level);
		}

		AttributeModifiersComponent.Builder attrBuilder = getModifiersBuilder(stack);
		if (!coating.equals("luck")) {
			if (isWeapon(stack) || isAxe(stack)) {
				attrBuilder.add(EntityAttributes.ATTACK_DAMAGE,
						new EntityAttributeModifier(SHARPER_DAMAGE_ID, DAMAGE_MULTIPLIER, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						AttributeModifierSlot.MAINHAND);
			}
			if (isTool(stack) || isAxe(stack)) {
				attrBuilder.add(EntityAttributes.MINING_EFFICIENCY,
						new EntityAttributeModifier(SHARPER_SPEED_ID, SPEED_BOOST, EntityAttributeModifier.Operation.ADD_VALUE),
						AttributeModifierSlot.MAINHAND);
			}
		}
		stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, attrBuilder.build());
	}

	public static void decrementUses(ItemStack stack, PlayerEntity player, World world) {
		if (stack.contains(SHARPER_USES)) {
			int current = stack.getOrDefault(SHARPER_USES, 0);
			String coating = stack.getOrDefault(SHARPER_COATING, "none");
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
				String currentCoating = stack.getOrDefault(SHARPER_COATING, "none");

				if (currentCoating.equals("luck")) {
					updateEnchantmentLevel(world, stack, Enchantments.FORTUNE, 0);
					updateEnchantmentLevel(world, stack, Enchantments.LOOTING, 0);
				}

				stack.remove(SHARPER_USES);
				stack.remove(SHARPER_COATING);
				stack.remove(SHARPER_COATING_TIER);

				AttributeModifiersComponent.Builder builder = getModifiersBuilder(stack);
				stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());

				world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.6f, 1.8f);
			} else {
				stack.set(SHARPER_USES, current - drainAmount);
			}
		}
	}

	public static AttributeModifiersComponent.Builder getModifiersBuilder(ItemStack stack) {
		AttributeModifiersComponent currentMods = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
		AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
		for (AttributeModifiersComponent.Entry entry : currentMods.modifiers()) {
			if (!entry.modifier().id().equals(SHARPER_DAMAGE_ID) && !entry.modifier().id().equals(SHARPER_SPEED_ID)) {
				builder.add(entry.attribute(), entry.modifier(), entry.slot());
			}
		}
		return builder;
	}

	private static void updateEnchantmentLevel(World world, ItemStack stack, RegistryKey<Enchantment> enchantmentKey, int level) {
		Registry<Enchantment> registry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

		ItemEnchantmentsComponent currentEnchants = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
		ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(currentEnchants);

		builder.set(registry.getOrThrow(enchantmentKey), level);

		stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
	}

	// =========================================================================
	// COMMANDS
	// =========================================================================

	private void registerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("toolsmithsharper")
					.then(CommandManager.literal("setUses").then(CommandManager.argument("value", IntegerArgumentType.integer(1)).executes(context -> { MAX_SHARPER_USES = IntegerArgumentType.getInteger(context, "value"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] Max Honed Uses set : §b" + MAX_SHARPER_USES), false); return 1; })))
					.then(CommandManager.literal("setCoatingUses").then(CommandManager.argument("value", IntegerArgumentType.integer(1)).executes(context -> { MAX_COATING_USES = IntegerArgumentType.getInteger(context, "value"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] Max Coating Uses set : §b" + MAX_COATING_USES), false); return 1; })))
					.then(CommandManager.literal("setDamage").then(CommandManager.argument("value (%)", DoubleArgumentType.doubleArg(0.0)).executes(context -> { DAMAGE_MULTIPLIER = DoubleArgumentType.getDouble(context, "value (%)"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] Weapon Damage Multiplier set : §b" + DAMAGE_MULTIPLIER), false); return 1; })))
					.then(CommandManager.literal("setSpeed").then(CommandManager.argument("value", DoubleArgumentType.doubleArg(0.0)).executes(context -> { SPEED_BOOST = DoubleArgumentType.getDouble(context, "value"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] Tool Speed Boost set : §b" + SPEED_BOOST), false); return 1; })))
					.then(CommandManager.literal("setCost").then(CommandManager.argument("value", IntegerArgumentType.integer(1)).executes(context -> { XP_COST = IntegerArgumentType.getInteger(context, "value"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] XP Cost set : §b" + XP_COST), false); return 1; })))
					.then(CommandManager.literal("setRepair").then(CommandManager.argument("value (%)", DoubleArgumentType.doubleArg(0.0)).executes(context -> { REPAIR_PERCENTAGE = DoubleArgumentType.getDouble(context, "value (%)"); saveConfig(); context.getSource().sendFeedback(() -> Text.literal("§a[Toolsmith Sharper] Repair Percentage set : §b" + (REPAIR_PERCENTAGE * 100) + "%"), false); return 1; })))
			);
		});
	}

	// =========================================================================
	// SAVE & FILE READING
	// =========================================================================

	private static File getConfigFile() { return FabricLoader.getInstance().getConfigDir().resolve("toolsmithsharper.properties").toFile(); }

	public static void loadConfig() {
		try {
			File file = getConfigFile();
			if (file.exists()) {
				Properties props = new Properties();
				props.load(new FileInputStream(file));
				MAX_SHARPER_USES = Integer.parseInt(props.getProperty("maxUses", "32"));
				MAX_COATING_USES = Integer.parseInt(props.getProperty("maxCoatingUses", "10"));
				DAMAGE_MULTIPLIER = Double.parseDouble(props.getProperty("damageMultiplier", "0.25"));
				SPEED_BOOST = Double.parseDouble(props.getProperty("speedBoost", "2.0"));
				XP_COST = Integer.parseInt(props.getProperty("xpCost", "1"));
				REPAIR_PERCENTAGE = Double.parseDouble(props.getProperty("repairPercentage", "0.10"));
			} else saveConfig();
		} catch (Exception e) { System.out.println("Error loading Toolsmith Sharper config"); }
	}

	public static void saveConfig() {
		try {
			Properties props = new Properties();
			props.setProperty("maxUses", String.valueOf(MAX_SHARPER_USES));
			props.setProperty("maxCoatingUses", String.valueOf(MAX_COATING_USES));
			props.setProperty("damageMultiplier", String.valueOf(DAMAGE_MULTIPLIER));
			props.setProperty("speedBoost", String.valueOf(SPEED_BOOST));
			props.setProperty("xpCost", String.valueOf(XP_COST));
			props.setProperty("repairPercentage", String.valueOf(REPAIR_PERCENTAGE));
			props.store(new FileOutputStream(getConfigFile()), "Configuration of Toolsmith Sharper");
		} catch (Exception e) { System.out.println("Error saving Toolsmith Sharper config"); }
	}
}