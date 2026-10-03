package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.World;

public class FoodGhostEntity extends GhostEntity {
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 64.0;
   private static final char TERROR_LEVEL = 'A';

   public FoodGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 64.0, 'A', 2200, 320, 110, 0.3F);
      this.ghostLevel = 3;
      this.setGhostDomainActualLevel(3);
      this.attackCooldown = 60;
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return player.method_6115() && player.method_6030().method_19267();
   }

   @Override
   public void method_5773() {
      super.method_5773();
      if (this.isHasGhostDomain() && this.isGhostDomainEnabled() && this.field_6012 % 20 == 0) {
         List<PlayerEntity> players = new ArrayList<>();
         MinecraftServer server = this.method_37908().method_8503();
         if (server != null) {
            PlayerManager playerManager = server.method_3760();

            for (PlayerEntity player : playerManager.method_14571()) {
               double distanceSquared = (player.method_23317() - this.method_23317()) * (player.method_23317() - this.method_23317())
                  + (player.method_23318() - this.method_23318()) * (player.method_23318() - this.method_23318())
                  + (player.method_23321() - this.method_23321()) * (player.method_23321() - this.method_23321());
               if (distanceSquared <= 4096.0 && player.method_5805()) {
                  players.add(player);
               }
            }
         }

         for (PlayerEntity player : players) {
            player.method_37222(new StatusEffectInstance(StatusEffects.field_5903, 100, 0), this);
         }
      }
   }
}
