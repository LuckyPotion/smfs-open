package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AllowDamage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class GhostPorcelainItem extends Item {
   private static final String IS_BOUND_KEY = "IsBound";
   private static final String BOUND_PLAYER_UUID_KEY = "BoundPlayerUUID";
   public static final int BASE_DURABILITY = 1000;
   private static boolean eventsRegistered = false;
   private static int tickCounter = 0;

   public GhostPorcelainItem(Settings settings) {
      super(settings.maxDamage(1000));
      if (!eventsRegistered) {
         this.registerEvents();
         eventsRegistered = true;
      }
   }

   private void registerEvents() {
      ServerLivingEntityEvents.ALLOW_DAMAGE.register((AllowDamage)(entity, source, amount) -> {
         if (entity instanceof PlayerEntity player && !entity.getWorld().isClient()) {
            boolean isGhostOrSkillDamage = this.isGhostDamage(source) || GhostSkillManager.isSkillDamage(source);
            if (isGhostOrSkillDamage && this.checkAndResistAttack(player, source, amount)) {
               return false;
            }

            if (source.isOf(RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier("wither"))) && player instanceof ServerPlayerEntity serverPlayer) {
               this.checkAndResistWitherEffect(serverPlayer);
            }
         }

         return true;
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         tickCounter++;
         if (tickCounter % 20 == 0) {
            for (ServerWorld world : server.getWorlds()) {
               this.checkBurningGhostPorcelainItems(world);
            }

            tickCounter = 0;
         }
      });
   }

   private void checkBurningGhostPorcelainItems(ServerWorld world) {
      List<ItemEntity> itemEntities = new ArrayList<>();

      for (PlayerEntity player : world.getPlayers()) {
         BlockPos playerPos = player.getBlockPos();
         Box searchBox = new Box(
            playerPos.getX() - 32, playerPos.getY() - 32, playerPos.getZ() - 32, playerPos.getX() + 32, playerPos.getY() + 32, playerPos.getZ() + 32
         );
         itemEntities.addAll(
            world.getEntitiesByClass(
               ItemEntity.class,
               searchBox,
               entity -> EntityPredicates.VALID_ENTITY.test(entity) && entity.getStack().isOf(ModItems.GHOST_PORCELAIN) && isBound(entity.getStack())
            )
         );
      }

      for (ItemEntity itemEntity : itemEntities) {
         ItemStack stack = itemEntity.getStack();
         if (this.isItemEntityOnFire(itemEntity)) {
            this.handleGhostPorcelainBurning(stack, world, itemEntity);
         }
      }
   }

   private boolean isItemEntityOnFire(ItemEntity itemEntity) {
      BlockPos pos = itemEntity.getBlockPos();
      World world = itemEntity.getWorld();
      BlockState blockState = world.getBlockState(pos);
      if (blockState.isOf(Blocks.FIRE) || blockState.isOf(Blocks.SOUL_FIRE)) {
         return true;
      }

      if (itemEntity.isInLava()) {
         return true;
      }

      if (itemEntity.isOnFire()) {
         return true;
      }

      BlockPos belowPos = pos.down();
      BlockState belowState = world.getBlockState(belowPos);
      return belowState.isOf(Blocks.FIRE)
         || belowState.isOf(Blocks.SOUL_FIRE)
         || belowState.isOf(Blocks.MAGMA_BLOCK)
         || belowState.isOf(Blocks.CAMPFIRE)
         || belowState.isOf(Blocks.SOUL_CAMPFIRE);
   }

   private void handleGhostPorcelainBurning(ItemStack stack, World world, ItemEntity itemEntity) {
      NbtCompound nbt = stack.getNbt();
      if (nbt != null && nbt.containsUuid("BoundPlayerUUID")) {
         UUID boundPlayerUuid = nbt.getUuid("BoundPlayerUUID");
         killBoundPlayer(boundPlayerUuid, world);
         itemEntity.setStack(ItemStack.EMPTY);
         itemEntity.discard();
         if (!world.isClient) {
            world.playSound(null, itemEntity.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }
   }

   private boolean isGhostDamage(DamageSource source) {
      return source.isOf(ModDamageSources.GHOST);
   }

   private boolean checkAndResistAttack(PlayerEntity player, DamageSource source, float amount) {
      for (ItemStack stack : player.getInventory().main) {
         if (stack.isOf(ModItems.GHOST_PORCELAIN) && isBound(stack) && tryResistAttack(player, amount, stack, source)) {
            return true;
         }
      }

      return this.checkOffHandAttack(player, source, amount);
   }

   private boolean checkOffHandAttack(PlayerEntity player, DamageSource source, float amount) {
      ItemStack offHandStack = player.getOffHandStack();
      return offHandStack.isOf(ModItems.GHOST_PORCELAIN) && isBound(offHandStack) ? tryResistAttack(player, amount, offHandStack, source) : false;
   }

   private static void damageItem(ItemStack stack, int amount, PlayerEntity player) {
      stack.damage(amount, player, p -> p.sendToolBreakStatus(Hand.MAIN_HAND));
      if (stack.getDamage() >= stack.getMaxDamage()) {
         NbtCompound nbt = stack.getNbt();
         if (nbt != null && nbt.containsUuid("BoundPlayerUUID")) {
            UUID boundPlayerUuid = nbt.getUuid("BoundPlayerUUID");
            killBoundPlayer(boundPlayerUuid, player.getWorld());
         }
      }
   }

   private void checkAndResistWitherEffect(ServerPlayerEntity player) {
      StatusEffectInstance witherEffect = player.getStatusEffect(StatusEffects.WITHER);
      if (witherEffect != null) {
         boolean mainInventoryResisted = false;

         for (ItemStack stack : player.getInventory().main) {
            if (this.removeWitherEffect(player, stack)) {
               mainInventoryResisted = true;
               break;
            }
         }

         if (mainInventoryResisted || this.removeWitherEffect(player, player.getOffHandStack())) {
            player.removeStatusEffect(StatusEffects.WITHER);
         }
      }
   }

   private boolean removeWitherEffect(ServerPlayerEntity player, ItemStack stack) {
      if (stack.isOf(ModItems.GHOST_PORCELAIN) && isBound(stack)) {
         damageItem(stack, 1, player);
         return true;
      } else {
         return false;
      }
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient) {
         if (isBound(stack)) {
            this.unbindPlayer(stack);
            user.sendMessage(Text.translatable("item.smfs.ghost_porcelain.unbound"), true);
            return TypedActionResult.success(stack);
         } else {
            return this.bindPlayerIfHealthAllows(user, stack);
         }
      } else {
         return TypedActionResult.pass(stack);
      }
   }

   private TypedActionResult<ItemStack> bindPlayerIfHealthAllows(PlayerEntity user, ItemStack stack) {
      if (user.getHealth() > 4.0F) {
         user.setHealth(user.getHealth() - 4.0F);
         this.bindPlayer(stack, user);
         user.sendMessage(Text.translatable("item.smfs.ghost_porcelain.bound"), true);
         return TypedActionResult.success(stack);
      } else {
         user.sendMessage(Text.translatable("item.smfs.ghost_porcelain.not_enough_health"), true);
         return TypedActionResult.pass(stack);
      }
   }

   public static boolean isBound(ItemStack stack) {
      NbtCompound nbt = stack.getNbt();
      return nbt != null && nbt.getBoolean("IsBound");
   }

   private void bindPlayer(ItemStack stack, PlayerEntity player) {
      this.unbindAllPlayersGhostPorcelain(player);
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.putBoolean("IsBound", true);
      nbt.putUuid("BoundPlayerUUID", player.getUuid());
   }

   private void unbindAllPlayersGhostPorcelain(PlayerEntity player) {
      for (int i = 0; i < player.getInventory().size(); i++) {
         ItemStack invStack = player.getInventory().getStack(i);
         if (invStack.isOf(ModItems.GHOST_PORCELAIN) && isBound(invStack)) {
            this.unbindPlayer(invStack);
         }
      }
   }

   private void unbindPlayer(ItemStack stack) {
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.remove("IsBound");
      nbt.remove("BoundPlayerUUID");
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_porcelain.fire_warning").formatted(Formatting.RED));
      if (isBound(stack)) {
         tooltip.add(Text.translatable("item.smfs.ghost_porcelain.bound_status"));
         NbtCompound nbt = stack.getNbt();
         if (nbt != null && nbt.containsUuid("BoundPlayerUUID")) {
            UUID uuid = nbt.getUuid("BoundPlayerUUID");
            tooltip.add(Text.translatable("item.smfs.ghost_porcelain.bound_to", new Object[]{uuid.toString().substring(0, 8)}));
         }
      } else {
         tooltip.add(Text.translatable("item.smfs.ghost_porcelain.unbound_status"));
      }

      tooltip.add(Text.translatable("item.smfs.ghost_porcelain.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_porcelain.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_porcelain.description.type"));
   }

   public static boolean tryResistAttack(PlayerEntity player, float damageAmount, ItemStack stack, DamageSource source) {
      if (stack.isOf(ModItems.GHOST_PORCELAIN) && isBound(stack)) {
         int durabilityToConsume = Math.max(1, (int)Math.ceil(damageAmount * 0.6));
         damageItem(stack, durabilityToConsume, player);
         return true;
      } else {
         return false;
      }
   }

   private static void killBoundPlayer(UUID boundPlayerUuid, World world) {
      if (boundPlayerUuid != null) {
         PlayerEntity player = world.getPlayerByUuid(boundPlayerUuid);
         if (player != null) {
            DamageSource damageSource = ModDamageSources.of(world, ModDamageSources.GHOST);
            PlayerEvents.handleSpiritDamage(player, 999.0F, 999.0F, damageSource);
            if (!world.isClient) {
               player.sendMessage(Text.translatable("item.smfs.ghost_porcelain.burned_death").formatted(Formatting.DARK_RED), false);
            }
         }
      }
   }
}
