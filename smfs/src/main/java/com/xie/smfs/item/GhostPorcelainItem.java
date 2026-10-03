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
      super(settings.method_7895(1000));
      if (!eventsRegistered) {
         this.registerEvents();
         eventsRegistered = true;
      }
   }

   private void registerEvents() {
      ServerLivingEntityEvents.ALLOW_DAMAGE
         .register(
            (AllowDamage)(entity, source, amount) -> {
               if (entity instanceof PlayerEntity player && !entity.method_37908().method_8608()) {
                  boolean isGhostOrSkillDamage = this.isGhostDamage(source) || GhostSkillManager.isSkillDamage(source);
                  if (isGhostOrSkillDamage && this.checkAndResistAttack(player, source, amount)) {
                     return false;
                  }

                  if (source.method_49708(RegistryKey.method_29179(RegistryKeys.field_42534, new Identifier("wither")))
                     && player instanceof ServerPlayerEntity serverPlayer) {
                     this.checkAndResistWitherEffect(serverPlayer);
                  }
               }

               return true;
            }
         );
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         tickCounter++;
         if (tickCounter % 20 == 0) {
            for (ServerWorld world : server.method_3738()) {
               this.checkBurningGhostPorcelainItems(world);
            }

            tickCounter = 0;
         }
      });
   }

   private void checkBurningGhostPorcelainItems(ServerWorld world) {
      List<ItemEntity> itemEntities = new ArrayList<>();

      for (PlayerEntity player : world.method_18456()) {
         BlockPos playerPos = player.method_24515();
         Box searchBox = new Box(
            playerPos.method_10263() - 32,
            playerPos.method_10264() - 32,
            playerPos.method_10260() - 32,
            playerPos.method_10263() + 32,
            playerPos.method_10264() + 32,
            playerPos.method_10260() + 32
         );
         itemEntities.addAll(
            world.method_8390(
               ItemEntity.class,
               searchBox,
               entity -> EntityPredicates.field_6154.test(entity)
                  && entity.method_6983().method_31574(ModItems.GHOST_PORCELAIN)
                  && isBound(entity.method_6983())
            )
         );
      }

      for (ItemEntity itemEntity : itemEntities) {
         ItemStack stack = itemEntity.method_6983();
         if (this.isItemEntityOnFire(itemEntity)) {
            this.handleGhostPorcelainBurning(stack, world, itemEntity);
         }
      }
   }

   private boolean isItemEntityOnFire(ItemEntity itemEntity) {
      BlockPos pos = itemEntity.method_24515();
      World world = itemEntity.method_37908();
      BlockState blockState = world.method_8320(pos);
      if (blockState.method_27852(Blocks.field_10036) || blockState.method_27852(Blocks.field_22089)) {
         return true;
      }

      if (itemEntity.method_5771()) {
         return true;
      }

      if (itemEntity.method_5809()) {
         return true;
      }

      BlockPos belowPos = pos.method_10074();
      BlockState belowState = world.method_8320(belowPos);
      return belowState.method_27852(Blocks.field_10036)
         || belowState.method_27852(Blocks.field_22089)
         || belowState.method_27852(Blocks.field_10092)
         || belowState.method_27852(Blocks.field_17350)
         || belowState.method_27852(Blocks.field_23860);
   }

   private void handleGhostPorcelainBurning(ItemStack stack, World world, ItemEntity itemEntity) {
      NbtCompound nbt = stack.method_7969();
      if (nbt != null && nbt.method_25928("BoundPlayerUUID")) {
         UUID boundPlayerUuid = nbt.method_25926("BoundPlayerUUID");
         killBoundPlayer(boundPlayerUuid, world);
         itemEntity.method_6979(ItemStack.field_8037);
         itemEntity.method_31472();
         if (!world.field_9236) {
            world.method_8396(null, itemEntity.method_24515(), SoundEvents.field_15081, SoundCategory.field_15245, 1.0F, 1.0F);
         }
      }
   }

   private boolean isGhostDamage(DamageSource source) {
      return source.method_49708(ModDamageSources.GHOST);
   }

   private boolean checkAndResistAttack(PlayerEntity player, DamageSource source, float amount) {
      for (ItemStack stack : player.method_31548().field_7547) {
         if (stack.method_31574(ModItems.GHOST_PORCELAIN) && isBound(stack) && tryResistAttack(player, amount, stack, source)) {
            return true;
         }
      }

      return this.checkOffHandAttack(player, source, amount);
   }

   private boolean checkOffHandAttack(PlayerEntity player, DamageSource source, float amount) {
      ItemStack offHandStack = player.method_6079();
      return offHandStack.method_31574(ModItems.GHOST_PORCELAIN) && isBound(offHandStack) ? tryResistAttack(player, amount, offHandStack, source) : false;
   }

   private static void damageItem(ItemStack stack, int amount, PlayerEntity player) {
      stack.method_7956(amount, player, p -> p.method_20236(Hand.field_5808));
      if (stack.method_7919() >= stack.method_7936()) {
         NbtCompound nbt = stack.method_7969();
         if (nbt != null && nbt.method_25928("BoundPlayerUUID")) {
            UUID boundPlayerUuid = nbt.method_25926("BoundPlayerUUID");
            killBoundPlayer(boundPlayerUuid, player.method_37908());
         }
      }
   }

   private void checkAndResistWitherEffect(ServerPlayerEntity player) {
      StatusEffectInstance witherEffect = player.method_6112(StatusEffects.field_5920);
      if (witherEffect != null) {
         boolean mainInventoryResisted = false;

         for (ItemStack stack : player.method_31548().field_7547) {
            if (this.removeWitherEffect(player, stack)) {
               mainInventoryResisted = true;
               break;
            }
         }

         if (mainInventoryResisted || this.removeWitherEffect(player, player.method_6079())) {
            player.method_6016(StatusEffects.field_5920);
         }
      }
   }

   private boolean removeWitherEffect(ServerPlayerEntity player, ItemStack stack) {
      if (stack.method_31574(ModItems.GHOST_PORCELAIN) && isBound(stack)) {
         damageItem(stack, 1, player);
         return true;
      } else {
         return false;
      }
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236) {
         if (isBound(stack)) {
            this.unbindPlayer(stack);
            user.method_7353(Text.method_43471("item.smfs.ghost_porcelain.unbound"), true);
            return TypedActionResult.method_22427(stack);
         } else {
            return this.bindPlayerIfHealthAllows(user, stack);
         }
      } else {
         return TypedActionResult.method_22430(stack);
      }
   }

   private TypedActionResult<ItemStack> bindPlayerIfHealthAllows(PlayerEntity user, ItemStack stack) {
      if (user.method_6032() > 4.0F) {
         user.method_6033(user.method_6032() - 4.0F);
         this.bindPlayer(stack, user);
         user.method_7353(Text.method_43471("item.smfs.ghost_porcelain.bound"), true);
         return TypedActionResult.method_22427(stack);
      } else {
         user.method_7353(Text.method_43471("item.smfs.ghost_porcelain.not_enough_health"), true);
         return TypedActionResult.method_22430(stack);
      }
   }

   public static boolean isBound(ItemStack stack) {
      NbtCompound nbt = stack.method_7969();
      return nbt != null && nbt.method_10577("IsBound");
   }

   private void bindPlayer(ItemStack stack, PlayerEntity player) {
      this.unbindAllPlayersGhostPorcelain(player);
      NbtCompound nbt = stack.method_7948();
      nbt.method_10556("IsBound", true);
      nbt.method_25927("BoundPlayerUUID", player.method_5667());
   }

   private void unbindAllPlayersGhostPorcelain(PlayerEntity player) {
      for (int i = 0; i < player.method_31548().method_5439(); i++) {
         ItemStack invStack = player.method_31548().method_5438(i);
         if (invStack.method_31574(ModItems.GHOST_PORCELAIN) && isBound(invStack)) {
            this.unbindPlayer(invStack);
         }
      }
   }

   private void unbindPlayer(ItemStack stack) {
      NbtCompound nbt = stack.method_7948();
      nbt.method_10551("IsBound");
      nbt.method_10551("BoundPlayerUUID");
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.fire_warning").method_27692(Formatting.field_1061));
      if (isBound(stack)) {
         tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.bound_status"));
         NbtCompound nbt = stack.method_7969();
         if (nbt != null && nbt.method_25928("BoundPlayerUUID")) {
            UUID uuid = nbt.method_25926("BoundPlayerUUID");
            tooltip.add(Text.method_43469("item.smfs.ghost_porcelain.bound_to", new Object[]{uuid.toString().substring(0, 8)}));
         }
      } else {
         tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.unbound_status"));
      }

      tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_porcelain.description.type"));
   }

   public static boolean tryResistAttack(PlayerEntity player, float damageAmount, ItemStack stack, DamageSource source) {
      if (stack.method_31574(ModItems.GHOST_PORCELAIN) && isBound(stack)) {
         int durabilityToConsume = Math.max(1, (int)Math.ceil(damageAmount * 0.6));
         damageItem(stack, durabilityToConsume, player);
         return true;
      } else {
         return false;
      }
   }

   private static void killBoundPlayer(UUID boundPlayerUuid, World world) {
      if (boundPlayerUuid != null) {
         PlayerEntity player = world.method_18470(boundPlayerUuid);
         if (player != null) {
            DamageSource damageSource = ModDamageSources.of(world, ModDamageSources.GHOST);
            PlayerEvents.handleSpiritDamage(player, 999.0F, 999.0F, damageSource);
            if (!world.field_9236) {
               player.method_7353(Text.method_43471("item.smfs.ghost_porcelain.burned_death").method_27692(Formatting.field_1079), false);
            }
         }
      }
   }
}
