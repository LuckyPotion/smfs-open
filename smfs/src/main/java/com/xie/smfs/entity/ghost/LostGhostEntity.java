package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.manager.GoldBlockProtectionManager;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.util.InstantKillUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LostGhostEntity extends GhostEntity {
   private final Random random = new Random();
   private int attackCooldown = 0;
   private int visibilityCycleTimer = 0;
   private boolean isInvisible = false;
   private int invisibleDuration = 100;
   private int visibleDuration = 200;

   public LostGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'A', 3500, 600, 100, 0.5F);
      this.ghostLevel = 1;
      this.setEnableChaseAfterRule(false);
      this.setKillingRulesEnabled(true);
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (!this.method_37908().field_9236) {
         if (this.attackCooldown > 0) {
            this.attackCooldown--;
         }

         this.handleVisibilityCycle();
      }
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return !this.isSuppressed()
         && !this.isDeadlocked()
         && !RedGhostCandleItem.isHoldingCandle(player)
         && !CoffinEffectManager.isPlayerInGoldCoffin(player)
         && !GoldBlockProtectionManager.isPlayerInGoldBlockShelter(player)
         && !player.method_6059(ModEffects.SPIRIT_IMMUNITY);
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (this.attackCooldown <= 0) {
         int effect = this.random.nextInt(6) + 1;
         Text message = Text.method_43473();
         switch (effect) {
            case 1:
               message = Text.method_43470("你忘记了走路...");
               player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, Integer.MAX_VALUE, 255, false, false));
               break;
            case 2:
               message = Text.method_43470("你忘记了你的眼睛...");
               player.method_6092(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, 255, false, false));
               break;
            case 3:
               message = Text.method_43470("你忘记了自己...");
               EntityAttributeInstance sanityAttribute = player.method_5996(SpiritAttributes.SANITY);
               if (sanityAttribute != null) {
                  sanityAttribute.method_6192(0.0);
                  PlayerEvents.setSpiritAttribute(player, SpiritAttributes.SANITY, 0.0F);
               }

               if (player instanceof ServerPlayerEntity serverPlayer) {
                  InstantKillUtil.executePlayerSelfKill(serverPlayer, null, false, true);
               }
               break;
            case 4:
               message = Text.method_43470("你忘记了呼吸...");
               DamageSource damageSource = this.method_37908().method_48963().method_48824();
               RegistryEntry<DamageType> drownDamageType = (RegistryEntry<DamageType>)this.method_37908()
                  .method_30349()
                  .method_30530(RegistryKeys.field_42534)
                  .method_40264(DamageTypes.field_42342)
                  .orElse(null);
               if (drownDamageType != null) {
                  PlayerEvents.handleSpiritDamage(player, 99999.0F, 99999.0F, damageSource);
               } else {
                  PlayerEvents.handleSpiritDamage(player, 99999.0F, 99999.0F, damageSource);
               }
               break;
            case 5:
               message = Text.method_43470("你忘记了战斗...");
               player.method_6092(new StatusEffectInstance(StatusEffects.field_5911, Integer.MAX_VALUE, 255, false, false));
               break;
            case 6:
               message = Text.method_43470("你忘记了秩序...");
               this.shufflePlayerInventory(player);
         }

         player.method_7353(message, true);
         this.attackCooldown = 800;
      }
   }

   private void shufflePlayerInventory(PlayerEntity player) {
      PlayerInventory inventory = player.method_31548();
      List<ItemStack> allStacks = new ArrayList<>();

      for (int i = 0; i < inventory.field_7547.size(); i++) {
         ItemStack stack = (ItemStack)inventory.field_7547.get(i);
         if (!stack.method_7960()) {
            allStacks.add(stack.method_7972());
            inventory.field_7547.set(i, ItemStack.field_8037);
         }
      }

      for (int i = 0; i < inventory.field_7548.size(); i++) {
         ItemStack stack = (ItemStack)inventory.field_7548.get(i);
         if (!stack.method_7960()) {
            allStacks.add(stack.method_7972());
            inventory.field_7548.set(i, ItemStack.field_8037);
         }
      }

      ItemStack offhandStack = (ItemStack)inventory.field_7544.get(0);
      if (!offhandStack.method_7960()) {
         allStacks.add(offhandStack.method_7972());
         inventory.field_7544.set(0, ItemStack.field_8037);
      }

      Collections.shuffle(allStacks, this.random);
      int totalSlots = inventory.field_7547.size() + inventory.field_7548.size() + 1;
      List<Integer> slotIndices = new ArrayList<>();

      for (int i = 0; i < totalSlots; i++) {
         slotIndices.add(i);
      }

      Collections.shuffle(slotIndices, this.random);

      for (int i = 0; i < allStacks.size() && i < slotIndices.size(); i++) {
         int targetSlot = slotIndices.get(i);
         if (targetSlot < inventory.field_7547.size()) {
            inventory.field_7547.set(targetSlot, allStacks.get(i));
         } else if (targetSlot < inventory.field_7547.size() + inventory.field_7548.size()) {
            inventory.field_7548.set(targetSlot - inventory.field_7547.size(), allStacks.get(i));
         } else {
            inventory.field_7544.set(0, allStacks.get(i));
         }
      }
   }

   private void handleVisibilityCycle() {
      this.visibilityCycleTimer++;
      if (this.isInvisible) {
         if (this.visibilityCycleTimer >= this.invisibleDuration) {
            this.isInvisible = false;
            this.visibilityCycleTimer = 0;
            this.teleportToRandomLocation();
            this.method_5648(false);
            if (this.method_37908() instanceof ServerWorld serverWorld) {
               serverWorld.method_18456().forEach(player -> {
                  if (player.method_5858(this) <= 1024.0) {
                     player.method_7353(Text.method_43470("§c你想起来什么！"), true);
                  }
               });
            }

            this.triggerAttackAfterInvisibility();
         }
      } else if (this.visibilityCycleTimer >= this.visibleDuration) {
         this.isInvisible = true;
         this.visibilityCycleTimer = 0;
         this.method_5648(true);
         if (this.method_37908() instanceof ServerWorld serverWorld) {
            serverWorld.method_18456().forEach(player -> {
               if (player.method_5858(this) <= 256.0) {
                  player.method_7353(Text.method_43470("§7似乎忘记了什么..."), true);
               }
            });
         }
      }
   }

   private void triggerAttackAfterInvisibility() {
      if (!this.method_37908().field_9236) {
         List<PlayerEntity> nearbyPlayers = this.method_37908().method_8390(PlayerEntity.class, this.method_5829().method_1014(48.0), this::shouldAttackPlayer);
         if (!nearbyPlayers.isEmpty()) {
            PlayerEntity target = nearbyPlayers.get(this.random.nextInt(nearbyPlayers.size()));
            DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
            PlayerEvents.handleSpiritDamage(target, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
         }
      }
   }

   private void teleportToRandomLocation() {
      if (!this.method_37908().field_9236) {
         double newX = this.method_23317() + (this.random.nextDouble() - 0.5) * 32.0;
         double newY = this.method_23318() + (this.random.nextDouble() - 0.5) * 8.0;
         double newZ = this.method_23321() + (this.random.nextDouble() - 0.5) * 32.0;
         newY = Math.max(0.0, Math.min(256.0, newY));
         if (this.isSafeLocation(newX, newY, newZ)) {
            this.method_20620(newX, newY, newZ);
         } else {
            for (int i = 0; i < 10; i++) {
               double tryX = this.method_23317() + (this.random.nextDouble() - 0.5) * 32.0;
               double tryY = this.method_23318() + (this.random.nextDouble() - 0.5) * 8.0;
               double tryZ = this.method_23321() + (this.random.nextDouble() - 0.5) * 32.0;
               tryY = Math.max(0.0, Math.min(256.0, tryY));
               if (this.isSafeLocation(tryX, tryY, tryZ)) {
                  this.method_20620(tryX, tryY, tryZ);
                  break;
               }
            }
         }
      }
   }

   private boolean isSafeLocation(double x, double y, double z) {
      BlockPos targetPos = new BlockPos((int)x, (int)y, (int)z);
      BlockPos belowPos = targetPos.method_10074();
      BlockPos abovePos = targetPos.method_10084();
      return this.method_37908().method_22347(targetPos) && !this.method_37908().method_22347(belowPos) && this.method_37908().method_22347(abovePos);
   }
}
