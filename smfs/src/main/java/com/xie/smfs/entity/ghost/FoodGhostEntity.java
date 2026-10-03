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
      return player.isUsingItem() && player.getActiveItem().isFood();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isHasGhostDomain() && this.isGhostDomainEnabled() && this.age % 20 == 0) {
         List<PlayerEntity> players = new ArrayList<>();
         MinecraftServer server = this.getWorld().getServer();
         if (server != null) {
            PlayerManager playerManager = server.getPlayerManager();

            for (PlayerEntity player : playerManager.getPlayerList()) {
               double distanceSquared = (player.getX() - this.getX()) * (player.getX() - this.getX())
                  + (player.getY() - this.getY()) * (player.getY() - this.getY())
                  + (player.getZ() - this.getZ()) * (player.getZ() - this.getZ());
               if (distanceSquared <= 4096.0 && player.isAlive()) {
                  players.add(player);
               }
            }
         }

         for (PlayerEntity player : players) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 0), this);
         }
      }
   }
}
