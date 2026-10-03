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
   public void tick() {
      super.tick();
      if (!this.getWorld().isClient) {
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
         && !player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY);
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      if (this.attackCooldown <= 0) {
         int effect = this.random.nextInt(6) + 1;
         Text message = Text.empty();
         switch (effect) {
            case 1:
               message = Text.literal("你忘记了走路...");
               player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
               break;
            case 2:
               message = Text.literal("你忘记了你的眼睛...");
               player.addStatusEffect(new StatusEffectInstance(ModEffects.BLACK_GHOST_DOMAIN_TARGET, Integer.MAX_VALUE, 255, false, false));
               break;
            case 3:
               message = Text.literal("你忘记了自己...");
               EntityAttributeInstance sanityAttribute = player.getAttributeInstance(SpiritAttributes.SANITY);
               if (sanityAttribute != null) {
                  sanityAttribute.setBaseValue(0.0);
                  PlayerEvents.setSpiritAttribute(player, SpiritAttributes.SANITY, 0.0F);
               }

               if (player instanceof ServerPlayerEntity serverPlayer) {
                  InstantKillUtil.executePlayerSelfKill(serverPlayer, null, false, true);
               }
               break;
            case 4:
               message = Text.literal("你忘记了呼吸...");
               DamageSource damageSource = this.getWorld().getDamageSources().drown();
               RegistryEntry<DamageType> drownDamageType = (RegistryEntry<DamageType>)this.getWorld()
                  .getRegistryManager()
                  .get(RegistryKeys.DAMAGE_TYPE)
                  .getEntry(DamageTypes.DROWN)
                  .orElse(null);
               if (drownDamageType != null) {
                  PlayerEvents.handleSpiritDamage(player, 99999.0F, 99999.0F, damageSource);
               } else {
                  PlayerEvents.handleSpiritDamage(player, 99999.0F, 99999.0F, damageSource);
               }
               break;
            case 5:
               message = Text.literal("你忘记了战斗...");
               player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, Integer.MAX_VALUE, 255, false, false));
               break;
            case 6:
               message = Text.literal("你忘记了秩序...");
               this.shufflePlayerInventory(player);
         }

         player.sendMessage(message, true);
         this.attackCooldown = 800;
      }
   }

   private void shufflePlayerInventory(PlayerEntity player) {
      PlayerInventory inventory = player.getInventory();
      List<ItemStack> allStacks = new ArrayList<>();

      for (int i = 0; i < inventory.main.size(); i++) {
         ItemStack stack = (ItemStack)inventory.main.get(i);
         if (!stack.isEmpty()) {
            allStacks.add(stack.copy());
            inventory.main.set(i, ItemStack.EMPTY);
         }
      }

      for (int i = 0; i < inventory.armor.size(); i++) {
         ItemStack stack = (ItemStack)inventory.armor.get(i);
         if (!stack.isEmpty()) {
            allStacks.add(stack.copy());
            inventory.armor.set(i, ItemStack.EMPTY);
         }
      }

      ItemStack offhandStack = (ItemStack)inventory.offHand.get(0);
      if (!offhandStack.isEmpty()) {
         allStacks.add(offhandStack.copy());
         inventory.offHand.set(0, ItemStack.EMPTY);
      }

      Collections.shuffle(allStacks, this.random);
      int totalSlots = inventory.main.size() + inventory.armor.size() + 1;
      List<Integer> slotIndices = new ArrayList<>();

      for (int i = 0; i < totalSlots; i++) {
         slotIndices.add(i);
      }

      Collections.shuffle(slotIndices, this.random);

      for (int i = 0; i < allStacks.size() && i < slotIndices.size(); i++) {
         int targetSlot = slotIndices.get(i);
         if (targetSlot < inventory.main.size()) {
            inventory.main.set(targetSlot, allStacks.get(i));
         } else if (targetSlot < inventory.main.size() + inventory.armor.size()) {
            inventory.armor.set(targetSlot - inventory.main.size(), allStacks.get(i));
         } else {
            inventory.offHand.set(0, allStacks.get(i));
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
            this.setInvisible(false);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
               serverWorld.getPlayers().forEach(player -> {
                  if (player.squaredDistanceTo(this) <= 1024.0) {
                     player.sendMessage(Text.literal("§c你想起来什么！"), true);
                  }
               });
            }

            this.triggerAttackAfterInvisibility();
         }
      } else if (this.visibilityCycleTimer >= this.visibleDuration) {
         this.isInvisible = true;
         this.visibilityCycleTimer = 0;
         this.setInvisible(true);
         if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.getPlayers().forEach(player -> {
               if (player.squaredDistanceTo(this) <= 256.0) {
                  player.sendMessage(Text.literal("§7似乎忘记了什么..."), true);
               }
            });
         }
      }
   }

   private void triggerAttackAfterInvisibility() {
      if (!this.getWorld().isClient) {
         List<PlayerEntity> nearbyPlayers = this.getWorld()
            .getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(48.0), this::shouldAttackPlayer);
         if (!nearbyPlayers.isEmpty()) {
            PlayerEntity target = nearbyPlayers.get(this.random.nextInt(nearbyPlayers.size()));
            DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
            PlayerEvents.handleSpiritDamage(target, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
         }
      }
   }

   private void teleportToRandomLocation() {
      if (!this.getWorld().isClient) {
         double newX = this.getX() + (this.random.nextDouble() - 0.5) * 32.0;
         double newY = this.getY() + (this.random.nextDouble() - 0.5) * 8.0;
         double newZ = this.getZ() + (this.random.nextDouble() - 0.5) * 32.0;
         newY = Math.max(0.0, Math.min(256.0, newY));
         if (this.isSafeLocation(newX, newY, newZ)) {
            this.teleport(newX, newY, newZ);
         } else {
            for (int i = 0; i < 10; i++) {
               double tryX = this.getX() + (this.random.nextDouble() - 0.5) * 32.0;
               double tryY = this.getY() + (this.random.nextDouble() - 0.5) * 8.0;
               double tryZ = this.getZ() + (this.random.nextDouble() - 0.5) * 32.0;
               tryY = Math.max(0.0, Math.min(256.0, tryY));
               if (this.isSafeLocation(tryX, tryY, tryZ)) {
                  this.teleport(tryX, tryY, tryZ);
                  break;
               }
            }
         }
      }
   }

   private boolean isSafeLocation(double x, double y, double z) {
      BlockPos targetPos = new BlockPos((int)x, (int)y, (int)z);
      BlockPos belowPos = targetPos.down();
      BlockPos abovePos = targetPos.up();
      return this.getWorld().isAir(targetPos) && !this.getWorld().isAir(belowPos) && this.getWorld().isAir(abovePos);
   }
}
