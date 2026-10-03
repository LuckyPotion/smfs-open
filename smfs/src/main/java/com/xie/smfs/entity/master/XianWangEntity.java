package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class XianWangEntity extends GhostMasterEntity {
   public XianWangEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.ghostDomainLevel = 0;
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.setCustomName(Text.literal("§6贤王"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createXianWangAttributes() {
      return MobEntity.createMobAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, 850.0)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 25.0)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
   }

   @Override
   protected void initGoals() {
      super.initGoals();
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "贤王";
   }
}
