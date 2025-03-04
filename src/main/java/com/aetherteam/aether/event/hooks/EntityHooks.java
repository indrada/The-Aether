package com.aetherteam.aether.event.hooks;

import com.aetherteam.aether.AetherTags;
import com.aetherteam.aether.attachment.AetherDataAttachments;
import com.aetherteam.aether.block.AetherBlocks;
import com.aetherteam.aether.client.AetherSoundEvents;
import com.aetherteam.aether.effect.AetherEffects;
import com.aetherteam.aether.entity.ai.goal.BeeGrowBerryBushGoal;
import com.aetherteam.aether.entity.ai.goal.FoxEatBerryBushGoal;
import com.aetherteam.aether.entity.monster.Swet;
import com.aetherteam.aether.entity.monster.dungeon.boss.Slider;
import com.aetherteam.aether.entity.passive.FlyingCow;
import com.aetherteam.aether.entity.passive.MountableAnimal;
import com.aetherteam.aether.entity.projectile.crystal.ThunderCrystal;
import com.aetherteam.aether.item.AetherItems;
import com.aetherteam.aether.item.accessories.AccessoryItem;
import com.aetherteam.aether.item.accessories.SlotIdentifierHolder;
import com.aetherteam.aether.item.accessories.cape.CapeItem;
import com.aetherteam.aether.item.accessories.gloves.GlovesItem;
import com.aetherteam.aether.item.accessories.miscellaneous.ShieldOfRepulsionItem;
import com.aetherteam.aether.item.accessories.pendant.PendantItem;
import com.aetherteam.aether.item.miscellaneous.bucket.SkyrootBucketItem;
import com.aetherteam.aether.mixin.AetherMixinHooks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EntityHooks {
    /**
     * Adds a new goal to an entity.
     *
     * @param entity The {@link Entity}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onEntityJoin(EntityJoinLevelEvent)
     */
    public static void addGoals(Entity entity) {
        if (entity.getClass() == Bee.class) {
            Bee bee = (Bee) entity;
            bee.getGoalSelector().addGoal(7, new BeeGrowBerryBushGoal(bee));
        } else if (entity.getClass() == Fox.class) {
            Fox fox = (Fox) entity;
            fox.goalSelector.addGoal(10, new FoxEatBerryBushGoal(fox, 1.2F, 12, 1));
        }
    }

    /**
     * Used to check whether an entity can spawn with accessories based on their {@link EntityType}.
     *
     * @param entity The {@link Entity} that is spawning.
     * @return Whether the entity can spawn in the world with accessories, as a {@link Boolean}.
     * @see com.aetherteam.aether.mixin.mixins.common.EventHooksMixin
     */
    public static boolean canMobSpawnWithAccessories(Entity entity) {
        EntityType<?> entityType = entity.getType();
        return entity instanceof Mob &&
                (entityType == EntityType.ZOMBIE || entityType == EntityType.ZOMBIE_VILLAGER || entityType == EntityType.HUSK || entityType == EntityType.SKELETON || entityType == EntityType.STRAY || entityType == EntityType.PIGLIN);
    }

    /**
     * Equips entities with accessories during spawning.
     *
     * @param entity The {@link Entity} to equip accessories to.
     * @see com.aetherteam.aether.mixin.mixins.common.EventHooksMixin
     */
    public static void spawnWithAccessories(Entity entity, DifficultyInstance difficulty) {
        if (entity instanceof Mob mob && mob.level() instanceof ServerLevel) {
            RandomSource random = mob.getRandom();
            EntityType<?> entityType = mob.getType();
            String[] allSlots = {"hands", "necklace", "aether_gloves", "aether_pendant"};
            String[] gloveSlots = {"hands", "aether_gloves"};
            if (entityType == EntityType.PIGLIN) {
                if (mob instanceof AbstractPiglin abstractPiglin && abstractPiglin.isAdult()) {
                    for (String identifier : allSlots) {
                        if (random.nextFloat() < 0.1F) {
                            equipAccessory(mob, identifier, ArmorMaterials.GOLD);
                        }
                    }
                }
            } else {
                boolean fullyArmored = true;
                for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                    if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR) {
                        ItemStack itemStack = mob.getItemBySlot(equipmentslot);
                        if (itemStack.isEmpty()) {
                            fullyArmored = false;
                            break;
                        }
                    }
                }
                if (fullyArmored && random.nextInt(4) == 1) {
                    if (mob.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem armorItem) {
                        if (armorItem.getMaterial() instanceof ArmorMaterials armorMaterials) {
                            for (String identifier : gloveSlots) {
                                equipAccessory(mob, identifier, armorMaterials);
                            }
                        }
                    }
                }
            }
            enchantAccessories(mob, difficulty, allSlots);
        }
    }

    /**
     * Equips an accessory to an empty slot for an entity on spawn.
     *
     * @param mob            The {@link Mob} to equip to.
     * @param identifier     The {@link String} identifier for the slot.
     * @param armorMaterials The {@link ArmorMaterials} to get an item from.
     * @see EntityHooks#spawnWithAccessories(Entity, DifficultyInstance)
     */
    private static void equipAccessory(Mob mob, String identifier, ArmorMaterials armorMaterials) {
        CuriosApi.getCuriosInventory(mob).ifPresent((handler) -> {
            boolean empty = true;
            for (SlotResult slotResult : handler.findCurios(identifier)) {
                if (!slotResult.stack().isEmpty()) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                Item item = getEquipmentForSlot(identifier, armorMaterials);
                if (item != null) {
                    handler.setEquippedCurio(identifier, 0, new ItemStack(item));
                }
            }
        });
    }

    /**
     * Gets an accessory item from a slot identifier and armor material.
     *
     * @param identifier     The {@link String} identifier for the slot.
     * @param armorMaterials The {@link ArmorMaterials} to get an item from.
     * @return The accessory {@link Item}.
     * @see EntityHooks#equipAccessory(Mob, String, ArmorMaterials)
     */
    @Nullable
    private static Item getEquipmentForSlot(String identifier, ArmorMaterials armorMaterials) {
        if (identifier.equals(GlovesItem.getIdentifierStatic())) {
            switch (armorMaterials) {
                case LEATHER -> {
                    return AetherItems.LEATHER_GLOVES.get();
                }
                case GOLD -> {
                    return AetherItems.GOLDEN_GLOVES.get();
                }
                case CHAIN -> {
                    return AetherItems.CHAINMAIL_GLOVES.get();
                }
                case IRON -> {
                    return AetherItems.IRON_GLOVES.get();
                }
                case DIAMOND -> {
                    return AetherItems.DIAMOND_GLOVES.get();
                }
            }
        } else if (identifier.equals(PendantItem.getIdentifierStatic())) {
            switch (armorMaterials) {
                case GOLD -> {
                    return AetherItems.GOLDEN_PENDANT.get();
                }
                case IRON -> {
                    return AetherItems.IRON_PENDANT.get();
                }
            }
        }
        return null;
    }

    /**
     * Randomly enchants accessories.
     *
     * @param mob          The {@link Mob} wearing the accessories.
     * @param difficulty   The {@link DifficultyInstance} of the level.
     * @param allowedSlots The list of {@link String} identifiers to enchant the accessories in.
     * @see EntityHooks#spawnWithAccessories(Entity, DifficultyInstance)
     */
    private static void enchantAccessories(Mob mob, DifficultyInstance difficulty, String[] allowedSlots) {
        RandomSource random = mob.getRandom();
        float chanceMultiplier = difficulty.getSpecialMultiplier();
        for (String identifier : allowedSlots) {
            CuriosApi.getCuriosInventory(mob).ifPresent((handler) -> handler.findCurio(identifier, 0).ifPresent((slotResult) -> {
                ItemStack itemStack = slotResult.stack();
                if (!itemStack.isEmpty() && random.nextFloat() < 0.5F * chanceMultiplier) {
                    handler.setEquippedCurio(identifier, 0, EnchantmentHelper.enchantItem(random, itemStack, (int) (5.0F + chanceMultiplier * (float) random.nextInt(18)), false));
                }
            }));
        }
    }

    /**
     * Prevents dismounting Aether mounts in the air, and Swets when consumed.
     *
     * @param rider       The {@link Entity} riding the mount.
     * @param mount       The mounted {@link Entity}.
     * @param dismounting Whether the rider is trying to dismount, as a {@link Boolean}.
     * @return Whether to prevent the rider from dismounting, as a {@link Boolean}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onMountEntity(EntityMountEvent)
     */
    public static boolean dismountPrevention(Entity rider, Entity mount, boolean dismounting) {
        if (dismounting && rider.isShiftKeyDown()) {
            return (mount instanceof MountableAnimal && !mount.onGround() && !mount.isInFluidType() && !mount.isPassenger()) || (mount instanceof Swet swet && !swet.isFriendly());
        }
        return false;
    }

    /**
     * Launches a mount when it interacts with a blue aercloud. This is handled as an event to get around a vanilla bug with it not working from the {@link com.aetherteam.aether.block.natural.BlueAercloudBlock} class.
     *
     * @param player The passenger {@link Player}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onRiderTick(TickEvent.PlayerTickEvent)
     */
    public static void launchMount(Player player) {
        Entity mount = player.getVehicle();
        if (player.isPassenger() && mount != null) {
            if (mount.level().getBlockStates(mount.getBoundingBox()).anyMatch((state) -> state.is(AetherBlocks.BLUE_AERCLOUD.get()))) {
                if (player.level().isClientSide()) {
                    mount.setDeltaMovement(mount.getDeltaMovement().x(), 2.0, mount.getDeltaMovement().z());
                }
            }
        }
    }

    /**
     * Handles milking cow entities with Skyroot Buckets.
     *
     * @param target The target {@link Entity} to milk.
     * @param player The {@link Player} milking the target.
     * @param hand   The {@link InteractionHand} with the bucket item.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onInteractWithEntity(PlayerInteractEvent.EntityInteractSpecific)
     */
    public static void skyrootBucketMilking(Entity target, Player player, InteractionHand hand) {
        if ((target instanceof Cow || target instanceof FlyingCow) && !((Animal) target).isBaby()) {
            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.is(AetherItems.SKYROOT_BUCKET.get())) {
                if (target instanceof FlyingCow) {
                    player.playSound(AetherSoundEvents.ENTITY_FLYING_COW_MILK.get(), 1.0F, 1.0F);
                } else {
                    player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                }
                ItemStack filledBucket = ItemUtils.createFilledResult(heldStack, player, AetherItems.SKYROOT_MILK_BUCKET.get().getDefaultInstance());
                player.swing(hand);
                player.setItemInHand(hand, filledBucket);
            }
        }
    }

    /**
     * Handles picking up aquatic entities with a Skyroot Bucket. This is done by checking for the result bucket that contains the entity and replacing it with a Skyroot equivalent.
     *
     * @param target The target {@link Entity}.
     * @param player The {@link Player}.
     * @param hand   The {@link InteractionHand} with the bucket item.
     * @return The {@link Optional} {@link InteractionResult} from this interaction.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onInteractWithEntity(PlayerInteractEvent.EntityInteractSpecific)
     */
    public static Optional<InteractionResult> pickupBucketable(Entity target, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        Optional<InteractionResult> interactionResult = Optional.empty();
        if (heldStack.is(AetherItems.SKYROOT_WATER_BUCKET.get())) { // Checks if the player is interacting with an entity with a Skyroot Water Bucket.
            if (target instanceof Bucketable bucketable && target instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                ItemStack bucketStack = bucketable.getBucketItemStack();
                bucketStack = SkyrootBucketItem.swapBucketType(bucketStack); // Swaps the bucket stack that contains an entity with a Skyroot equivalent.
                if (!bucketStack.isEmpty()) {
                    target.playSound(bucketable.getPickupSound(), 1.0F, 1.0F);
                    bucketable.saveToBucketTag(bucketStack);
                    ItemStack filledStack = ItemUtils.createFilledResult(heldStack, player, bucketStack, false);
                    player.setItemInHand(hand, filledStack);
                    Level level = livingEntity.level();
                    if (!level.isClientSide()) {
                        CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, bucketStack);
                    }
                    target.discard();
                    interactionResult = Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
                } else {
                    interactionResult = Optional.of(InteractionResult.FAIL);
                }
            }
        }
        return interactionResult;
    }

    /**
     * Handles the interaction for equipping and unequipping accessories to armor stands.
     *
     * @param target The target {@link Entity}.
     * @param player The {@link Player}.
     * @param stack  The held {@link ItemStack}.
     * @param pos    The right-click {@link Vec3} position.
     * @param hand   The {@link InteractionHand} with the item.
     * @return The {@link Optional} {@link InteractionResult} from this interaction.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onInteractWithEntity(PlayerInteractEvent.EntityInteractSpecific)
     */
    public static Optional<InteractionResult> interactWithArmorStand(Entity target, Player player, ItemStack stack, Vec3 pos, InteractionHand hand) {
        if (target instanceof ArmorStand armorStand) {
            if (armorStand.level().isClientSide()) {
                return Optional.of(InteractionResult.SUCCESS);
            }
            if (!stack.isEmpty()) { // Equip behavior.
                if (stack.is(AetherTags.Items.ACCESSORIES)) {
                    String identifier = "";
                    if (stack.getItem() instanceof SlotIdentifierHolder slotIdentifierHolder)
                        identifier = slotIdentifierHolder.getIdentifier();

                    Optional<ICuriosItemHandler> lazyHandler = CuriosApi.getCuriosInventory(armorStand);
                    if (lazyHandler.isPresent()) {
                        ICuriosItemHandler handler = lazyHandler.get();
                        Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler(identifier);
                        if (stacksHandler.isPresent()) {
                            IDynamicStackHandler stackHandler = stacksHandler.get().getCosmeticStacks();
                            if (0 < stackHandler.getSlots()) {
                                ItemStack itemStack = stackHandler.getStackInSlot(0);
                                if (stack.getItem() instanceof AccessoryItem accessoryItem) {
                                    SlotContext slotContext = new SlotContext(identifier, armorStand, 0, true, true);
                                    if (accessoryItem.canEquip(slotContext, stack)) {
                                        stackHandler.setStackInSlot(0, stack.copy());
                                        if (accessoryItem instanceof GlovesItem glovesItem) {
                                            armorStand.level().playSound(null, armorStand.blockPosition(), glovesItem.getEquipSound(slotContext, stack).soundEvent(), armorStand.getSoundSource(), 1, 1);
                                        } else if (accessoryItem instanceof PendantItem pendantItem) {
                                            armorStand.level().playSound(null, armorStand.blockPosition(), pendantItem.getEquipSound(slotContext, stack).soundEvent(), armorStand.getSoundSource(), 1, 1);
                                        } else {
                                            armorStand.level().playSound(null, armorStand.blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, armorStand.getSoundSource(), 1, 1);
                                        }
                                        if (identifier.equals("hands") || identifier.equals("aether_gloves")) {
                                            armorStand.setShowArms(true);
                                        }
                                        if (!player.isCreative()) {
                                            int count = stack.getCount();
                                            stack.shrink(count);
                                        }
                                        if (!itemStack.isEmpty()) {
                                            player.setItemInHand(hand, itemStack);
                                        }
                                        return Optional.of(InteractionResult.SUCCESS);
                                    }
                                }
                            }
                        }
                    }
                }
            } else { // Unequip behavior.
                String identifier = slotToUnequip(armorStand, pos);
                if (!identifier.isEmpty()) {
                    Optional<ICuriosItemHandler> lazyHandler = CuriosApi.getCuriosInventory(armorStand);
                    if (lazyHandler.isPresent()) {
                        ICuriosItemHandler handler = lazyHandler.get();
                        Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler(identifier);
                        if (stacksHandler.isPresent()) {
                            IDynamicStackHandler stackHandler = stacksHandler.get().getCosmeticStacks();
                            if (0 < stackHandler.getSlots()) {
                                ItemStack itemStack = stackHandler.getStackInSlot(0);
                                if (!itemStack.isEmpty()) {
                                    player.setItemInHand(hand, itemStack);
                                    stackHandler.setStackInSlot(0, ItemStack.EMPTY);
                                    return Optional.of(InteractionResult.SUCCESS);
                                }
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * What accessory slot of the armor stand to unequip from, based on where the armor stand is right-clicked.
     *
     * @param armorStand The {@link ArmorStand} to unequip from.
     * @param pos        The right-click {@link Vec3} position.
     * @return The {@link String} for the slot identifier.
     * @see EntityHooks#interactWithArmorStand(Entity, Player, ItemStack, Vec3, InteractionHand)
     */
    private static String slotToUnequip(ArmorStand armorStand, Vec3 pos) {
        boolean isSmall = armorStand.isSmall();
        Direction.Axis axis = armorStand.getDirection().getAxis();
        double x = isSmall ? pos.x * 2.0 : pos.x;
        double z = isSmall ? pos.z * 2.0 : pos.z;
        double front = axis == Direction.Axis.X ? z : x;
        double vertical = isSmall ? pos.y * 2.0 : pos.y;
        String glovesIdentifier = GlovesItem.getIdentifierStatic();
        String pendantIdentifier = PendantItem.getIdentifierStatic();
        String capeIdentifier = CapeItem.getIdentifierStatic();
        String shieldIdentifier = ShieldOfRepulsionItem.getIdentifierStatic();
        if (!getItemByIdentifier(armorStand, glovesIdentifier).isEmpty()
                && Math.abs(front) >= (isSmall ? 0.15 : 0.2)
                && vertical >= (isSmall ? 0.65 : 0.75)
                && vertical < 1.15) {
            return glovesIdentifier;
        } else if (!getItemByIdentifier(armorStand, pendantIdentifier).isEmpty()
                && vertical >= (isSmall ? 1.2 : 1.3)
                && vertical < 0.9 + (isSmall ? 0.8 : 0.6)) {
            return pendantIdentifier;
        } else if (!getItemByIdentifier(armorStand, capeIdentifier).isEmpty()
                && vertical >= (isSmall ? 1.0 : 1.1)
                && vertical < (isSmall ? 1.7 : 1.4)) {
            return capeIdentifier;
        } else if (!getItemByIdentifier(armorStand, shieldIdentifier).isEmpty()
                && vertical >= (isSmall ? 0.9 : 1.0)
                && vertical < (isSmall ? 1.5 : 1.2)) {
            return shieldIdentifier;
        }
        return "";
    }

    /**
     * Gets an accessory from an armor stand.
     *
     * @param armorStand The {@link ArmorStand} to get the accessory from.
     * @param identifier The {@link String} for the slot identifier.
     * @return The accessory {@link ItemStack} gotten from the entity.
     * @see EntityHooks#slotToUnequip(ArmorStand, Vec3)
     */
    private static ItemStack getItemByIdentifier(ArmorStand armorStand, String identifier) {
        Optional<ICuriosItemHandler> lazyHandler = CuriosApi.getCuriosInventory(armorStand);
        if (lazyHandler.isPresent()) {
            ICuriosItemHandler handler = lazyHandler.get();
            Optional<ICurioStacksHandler> stacksHandler = handler.getStacksHandler(identifier);
            if (stacksHandler.isPresent()) {
                IDynamicStackHandler stackHandler = stacksHandler.get().getCosmeticStacks();
                if (0 < stackHandler.getSlots()) {
                    return stackHandler.getStackInSlot(0);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Prevents an entity from being hooked with a Fishing Rod.
     *
     * @param projectileEntity The hook projectile {@link Entity}.
     * @param rayTraceResult   The {@link HitResult} of the projectile.
     * @return Whether to prevent the hook interaction, as a {@link Boolean}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onProjectileHitEntity(ProjectileImpactEvent)
     */
    public static boolean preventEntityHooked(Entity projectileEntity, HitResult rayTraceResult) {
        if (rayTraceResult instanceof EntityHitResult entityHitResult) {
            return entityHitResult.getEntity().getType().is(AetherTags.Entities.UNHOOKABLE) && projectileEntity instanceof FishingHook;
        }
        return false;
    }

    /**
     * Disallows blocking the Slider with a shield.
     *
     * @param source The {@link DamageSource} to block.
     * @return Whether to disallow blocking, as a {@link Boolean}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onShieldBlock(ShieldBlockEvent)
     */
    public static boolean preventSliderShieldBlock(DamageSource source) {
        return source.getEntity() instanceof Slider;
    }

    /**
     * Prevents lightning from damaging dungeon keys.
     *
     * @param entity The {@link Entity}.
     * @return Whether lightning hit a key item, as a {@link Boolean}.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onLightningStrike(EntityStruckByLightningEvent)
     */
    public static boolean lightningHitKeys(Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            return itemEntity.getItem().is(AetherTags.Items.DUNGEON_KEYS);
        } else {
            return false;
        }
    }

    /**
     * Prevents lightning summoned by Thunder Crystals from damaging items.
     *
     * @param entity    The {@link Entity} struck by the lightning bolt.
     * @param lightning The {@link LightningBolt} that struck the entity.
     * @return Whether the lightning was from a {@link ThunderCrystal} and hit an item, as a {@link Boolean}.
     */
    public static boolean thunderCrystalHitItems(Entity entity, LightningBolt lightning) {
        if (entity instanceof ItemEntity) {
            if (lightning.hasData(AetherDataAttachments.LIGHTNING_TRACKER)) {
                return lightning.getData(AetherDataAttachments.LIGHTNING_TRACKER).getOwner(lightning.level()) instanceof ThunderCrystal;
            }
        }
        return false;
    }

    /**
     * Tracks if items were dropped by a player's death.
     *
     * @param entity    The {@link LivingEntity} that dropped the items.
     * @param itemDrops The {@link Collection} of dropped {@link ItemEntity}s.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onPlayerDrops(LivingDropsEvent)
     */
    public static void trackDrops(LivingEntity entity, Collection<ItemEntity> itemDrops) {
        if (entity instanceof Player player) {
            itemDrops.forEach(itemEntity -> itemEntity.getData(AetherDataAttachments.DROPPED_ITEM).setOwner(player));
        }
    }

    /**
     * Damages certain accessory items dropped from entities if they're not guaranteed drops.
     *
     * @param entity      The {@link LivingEntity} dropping the accessories.
     * @param itemDrops   The {@link Collection} of {@link ItemEntity} drops.
     * @param recentlyHit Whether the entity was recently hit, as a {@link Boolean}.
     * @param looting     The {@link Integer} for the looting enchantment value.
     * @return The new {@link Collection} of {@link ItemEntity} drops.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onCurioDrops(CurioDropsEvent)
     */
    public static Collection<ItemEntity> handleEntityCurioDrops(LivingEntity entity, Collection<ItemEntity> itemDrops, boolean recentlyHit, int looting) {
        if (entity instanceof Mob mob && mob.hasData(AetherDataAttachments.MOB_ACCESSORY)) {
            String[] allSlots = {"hands", "necklace", "aether_gloves", "aether_pendant"};
            for (String identifier : allSlots) {
                List<ItemStack> itemStacks = itemDrops.stream().map(ItemEntity::getItem).filter((stack) -> AetherMixinHooks.getIdentifierForItem(mob, stack).equals(identifier)).toList();
                if (!itemStacks.isEmpty()) {
                    ItemStack itemStack = itemStacks.get(0);
                    float f = mob.getData(AetherDataAttachments.MOB_ACCESSORY).getEquipmentDropChance(identifier);
                    boolean flag = f > 1.0F;
                    if (!itemStack.isEmpty()) {
                        itemDrops.removeIf((itemEntity) -> ItemStack.isSameItemSameTags(itemEntity.getItem(), itemStack));
                    }
                    if (!itemStack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemStack) && recentlyHit && Math.max(mob.getRandom().nextFloat() - (float) looting * 0.01F, 0.0F) < f) {
                        if (!flag && itemStack.isDamageableItem()) {
                            itemStack.setDamageValue(itemStack.getMaxDamage() - mob.getRandom().nextInt(1 + mob.getRandom().nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
                        }
                        ItemEntity itemEntity = new ItemEntity(mob.level(), mob.getX(), mob.getY(), mob.getZ(), itemStack);
                        itemEntity.setDefaultPickUpDelay();
                        itemDrops.add(itemEntity);
                    }
                }
            }

        }
        return itemDrops;
    }

    /**
     * Increase the experience drops of an entity based on whether they're wearing accessories.
     *
     * @param entity     The {@link LivingEntity} dropping the experience.
     * @param experience The original {@link Integer} amount of experience.
     * @return The new {@link Integer} amount of experience.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onDropExperience(LivingExperienceDropEvent)
     */
    public static int modifyExperience(LivingEntity entity, int experience) {
        if (entity instanceof Mob mob && mob.hasData(AetherDataAttachments.MOB_ACCESSORY)) {
            Optional<ICuriosItemHandler> lazyHandler = CuriosApi.getCuriosInventory(mob);
            if (lazyHandler.isPresent()) {
                ICuriosItemHandler handler = lazyHandler.get();
                if (experience > 0) {
                    String[] allSlots = {"hands", "necklace", "aether_gloves", "aether_pendant"};
                    for (String identifier : allSlots) {
                        Optional<SlotResult> optionalSlotResult = handler.findCurio(identifier, 0);
                        if (optionalSlotResult.isPresent()) {
                            ItemStack stack = optionalSlotResult.get().stack();
                            if (!stack.isEmpty() && mob.getData(AetherDataAttachments.MOB_ACCESSORY).getEquipmentDropChance(identifier) <= 1.0F) {
                                experience += 1 + mob.getRandom().nextInt(3);
                            }
                        }
                    }
                }
            }
        }
        return experience;
    }

    /**
     * Prevents an entity from being inflicted with {@link AetherEffects#INEBRIATION} if it has {@link AetherEffects#REMEDY} applied.
     *
     * @param livingEntity    The {@link LivingEntity} that the effect is being applied to.
     * @param appliedInstance The {@link MobEffectInstance}.
     * @return Whether Inebriation application can be prevented.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onEffectApply(MobEffectEvent.Applicable)
     */
    public static boolean preventInebriation(LivingEntity livingEntity, MobEffectInstance appliedInstance) {
        return livingEntity.hasEffect(AetherEffects.REMEDY.get()) && appliedInstance.getEffect() == AetherEffects.INEBRIATION.get();
    }

    /**
     * Prevents Slime split behavior from carrying over to Swets.
     *
     * @param mob The splitting {@link Mob}.
     * @return Whether the {@link Mob} should split.
     * @see com.aetherteam.aether.event.listeners.EntityListener#onEntitySplit(MobSplitEvent)
     */
    public static boolean preventSplit(Mob mob) {
        return mob.getType().is(AetherTags.Entities.SWETS);
    }
}
