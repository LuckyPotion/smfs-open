package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.manager.CoffinEffectManager;
import com.xie.smfs.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class BoxGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 0;
   private static final double GHOST_DOMAIN_RADIUS = 32.0;
   private static final char TERROR_LEVEL = 'C';
   private int attackCooldown = 0;
   private static final Map<UUID, Long> playerChestInteractionMap = new HashMap<>();
   private static final int INTERACTION_TIMEOUT = 50;

   public static Builder createLivingAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 100000.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
   }

   public BoxGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1000, 190, 45, 0.2F);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(100000.0);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.25);
      Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(5.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.hasStatusEffect(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else if (this.attackCooldown > 0) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : this.isPlayerInteractingWithChest(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.squaredDistanceTo(player) <= 1024.0;
   }

   public static void registerChestInteractionListener() {
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hitResult) -> {
         if (!world.isClient() && world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof ChestBlock) {
            playerChestInteractionMap.put(player.getUuid(), System.currentTimeMillis());
         }

         return ActionResult.PASS;
      });
   }

   private boolean isPlayerInteractingWithChest(PlayerEntity player) {
      Long lastInteractionTime = playerChestInteractionMap.get(player.getUuid());
      return lastInteractionTime == null ? false : System.currentTimeMillis() - lastInteractionTime < 2500L;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.attackCooldown > 0) {
         this.attackCooldown--;
      }

      if (!this.getWorld().isClient() && this.age % 10 == 0) {
         for (PlayerEntity player : this.getWorld().getPlayers()) {
            if (this.shouldAttackPlayer(player) && this.canSee(player)) {
               this.executeAttack(player);
               this.attackCooldown = 40;
               break;
            }
         }
      }
   }
}
