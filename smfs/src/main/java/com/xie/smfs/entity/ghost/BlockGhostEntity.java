package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.item.RedGhostCandleItem;
import com.xie.smfs.util.PlayerBlockActionTracker;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class BlockGhostEntity extends GhostEntity {
   private static final Map<UUID, Boolean> playerInteractingWithBlocks = new HashMap<>();
   private static final int BLOCK_ATTACK_COOLDOWN = 20;
   private int blockAttackCooldown = 0;

   public BlockGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, true, 3, 64.0, 'B', 2000, 350, 80, 0.3F);
      this.ghostLevel = 2;
      this.setGhostDomainActualLevel(3);
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      if (this.isSuppressed() || this.isDeadlocked() || RedGhostCandleItem.isHoldingCandle(player)) {
         playerInteractingWithBlocks.put(player.getUuid(), false);
         return false;
      }

      if (this.blockAttackCooldown > 0) {
         return false;
      }

      boolean isInteractingWithBlocks = this.isPlayerInteractingWithBlocks(player);
      playerInteractingWithBlocks.put(player.getUuid(), isInteractingWithBlocks);
      return isInteractingWithBlocks;
   }

   private boolean isPlayerInteractingWithBlocks(PlayerEntity player) {
      return PlayerBlockActionTracker.hasRecentBlockAction(player.getUuid(), 20);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.blockAttackCooldown > 0) {
         this.blockAttackCooldown--;
      }

      if (this.isDeadlocked()) {
         this.setVisible(true);
         this.setInvisible(false);
         this.setVisibleTicks(20);
      } else if (this.getTarget() != null && this.getTarget().isAlive()) {
         this.setVisible(true);
         this.setInvisible(false);
         this.setVisibleTicks(20);
      }
   }

   @Override
   protected void executeAttack(PlayerEntity player) {
      this.blockAttackCooldown = 20;
      this.setVisible(true);
      this.setVisibleTicks(20);
      this.setInvisible(false);
      this.executeAttack(player, false);
   }

   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("BlockAttackCooldown", this.blockAttackCooldown);
   }

   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("BlockAttackCooldown")) {
         this.blockAttackCooldown = nbt.getInt("BlockAttackCooldown");
      }
   }
}
