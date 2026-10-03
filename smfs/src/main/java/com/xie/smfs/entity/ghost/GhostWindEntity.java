package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.registry.ModEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GhostWindEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/GhostWindEntity");
   private static final boolean HAS_GHOST_DOMAIN = true;
   private static final int GHOST_DOMAIN_LEVEL = 3;
   private static final double GHOST_DOMAIN_RADIUS = 24.0;
   private static final char TERROR_LEVEL = 'B';
   private int windFieldTicks = 0;
   private boolean isWindFieldActive = false;
   private int windEffectApplyCount = 0;

   public GhostWindEntity(EntityType<GhostWindEntity> entityType, World world) {
      super(entityType, world, true, 3, 24.0, 'B', 1000, 580, 40, 0.2F);
      this.ghostLevel = 2;
      this.attackCooldown = 0;
      this.initWindAttributes();
   }

   private void initWindAttributes() {
      EntityAttributeInstance healthAttribute = this.method_5996(EntityAttributes.field_23716);
      if (healthAttribute != null) {
         healthAttribute.method_6192(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.method_5996(EntityAttributes.field_23719);
      if (speedAttribute != null) {
         speedAttribute.method_6192(0.25);
      }
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.method_6092(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
      this.applyWindFieldEffect(player);
   }

   private void applyWindFieldEffect(PlayerEntity player) {
      if (this.isInGhostDomain(player) && !player.method_6059(ModEffects.GHOST_WIND_EFFECT)) {
         player.method_6092(new StatusEffectInstance(ModEffects.GHOST_WIND_EFFECT, 600, 0));
         NbtCompound playerData = PlayerEvents.getCachedData(player);
         NbtCompound ghostWindData = new NbtCompound();
         ghostWindData.method_25927("ghost_wind_entity", this.method_5667());
         ghostWindData.method_10544("wind_start_time", this.method_37908().method_8510());
         ghostWindData.method_10548("wind_progress", 0.0F);
         ghostWindData.method_10544("last_update_time", this.method_37908().method_8510());
         playerData.method_10566("ghost_wind_effect", ghostWindData);
         LOGGER.info("玩家 {} 获得鬼风效果，来源鬼风鬼: {}，位置: {}", player.method_5477().getString(), this.method_5667(), player.method_24515());
         this.windEffectApplyCount++;
         if (this.windEffectApplyCount > 1) {
            this.executeSpiritAttack(player);
         }
      }
   }

   private void executeSpiritAttack(PlayerEntity player) {
      if (!this.isDeadlocked()) {
         if (this.attackCooldown <= 0) {
            this.attackCooldown = 60;
            DamageSource damageSource = ModDamageSources.ghost(this.method_37908());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            LOGGER.info("鬼风鬼 {} 对玩家 {} 执行灵异袭击，造成 {} 点灵异伤害", this.method_5667(), player.method_5477().getString(), this.getSpiritualDamage());
         }
      }
   }

   @Override
   public float getGhostDomainRadius() {
      return 24.0F;
   }
}
