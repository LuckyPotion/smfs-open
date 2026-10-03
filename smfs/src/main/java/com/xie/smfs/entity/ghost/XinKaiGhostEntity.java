package com.xie.smfs.entity.ghost;

import com.xie.smfs.entity.GhostEntity;
import java.util.Objects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class XinKaiGhostEntity extends GhostEntity {
   private static final double ATTACK_RANGE = 8.0;

   public XinKaiGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 32.0, 'C', 1500, 100, 60, 0.25F);
      this.ghostLevel = 0;
      this.attackCooldown = 30;
      this.initAttributes();
   }

   private void initAttributes() {
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23716)).method_6192(100000.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23719)).method_6192(0.25);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23721)).method_6192(8.0);
      Objects.requireNonNull(this.method_5996(EntityAttributes.field_23717)).method_6192(8.0);
   }

   @Override
   protected void method_5959() {
      this.field_6201.method_6277(1, new MeleeAttackGoal(this, 1.8, false));
      this.field_6185.method_6277(1, new ActiveTargetGoal(this, PlayerEntity.class, 10, true, false, entity -> {
         if (this.isSuppressed() || this.isDeadlocked()) {
            return false;
         } else {
            return this.attackCooldown > 0 ? false : this.method_5858(entity) <= 64.0;
         }
      }));
   }

   @Override
   public boolean shouldAttackPlayer(PlayerEntity player) {
      return this.attackCooldown > 0 ? false : this.method_5858(player) <= 64.0;
   }
}
