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

public class YinQiEntity extends GhostMasterEntity {
   public YinQiEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.ghostDomainLevel = 0;
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.method_5665(Text.method_43470("§6楪祈"));
      this.method_5880(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createYinQiAttributes() {
      return MobEntity.method_26828()
         .method_26868(EntityAttributes.field_23716, 850.0)
         .method_26868(EntityAttributes.field_23719, 0.3)
         .method_26868(EntityAttributes.field_23721, 18.0)
         .method_26868(EntityAttributes.field_23717, 16.0);
   }

   @Override
   protected void method_5959() {
      super.method_5959();
   }

   @Override
   public boolean method_5810() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "楪祈";
   }
}
