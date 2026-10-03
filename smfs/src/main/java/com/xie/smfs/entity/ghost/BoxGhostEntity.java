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
      return LivingEntity.method_26827()
         .method_26868(EntityAttributes.field_23716, 100000.0)
         .method_26868(EntityAttributes.field_23719, 0.25)
         .method_26868(EntityAttributes.field_23721, 5.0);
   }

   public BoxGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 0, 32.0, 'C', 1000, 190, 45, 0.2F);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(100000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.25);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(5.0);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (RedGhostCandleItem.isHoldingCandle(player)) {
         return false;
      } else if (CoffinEffectManager.isPlayerInGoldCoffin(player)) {
         return false;
      } else if (player.method_6059(ModEffects.SPIRIT_IMMUNITY)) {
         return false;
      } else if (this.attackCooldown > 0) {
         return false;
      } else {
         return !this.isPlayerInRange(player) ? false : this.isPlayerInteractingWithChest(player);
      }
   }

   private boolean isPlayerInRange(PlayerEntity player) {
      return this.method_5858(player) <= 1024.0;
   }

   public static void registerChestInteractionListener() {
      UseBlockCallback.EVENT.register((UseBlockCallback)(player, world, hand, hitResult) -> {
         if (!world.method_8608() && world.method_8320(hitResult.method_17777()).method_26204() instanceof ChestBlock) {
            playerChestInteractionMap.put(player.method_5667(), System.currentTimeMillis());
         }

         return ActionResult.field_5811;
      });
   }

   private boolean isPlayerInteractingWithChest(PlayerEntity player) {
      Long lastInteractionTime = playerChestInteractionMap.get(player.method_5667());
      return lastInteractionTime == null ? false : System.currentTimeMillis() - lastInteractionTime < 2500L;
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.attackCooldown > 0) {
         this.attackCooldown--;
      }

      if (!this.method_37908().method_8608() && this.field_6012 % 10 == 0) {
         for (PlayerEntity player : this.method_37908().method_18456()) {
            if (this.shouldAttackPlayer(player) && this.method_6057(player)) {
               this.executeAttack(player);
               this.attackCooldown = 40;
               break;
            }
         }
      }
   }
}
