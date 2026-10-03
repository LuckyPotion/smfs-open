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
      EntityAttributeInstance healthAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
      if (healthAttribute != null) {
         healthAttribute.setBaseValue(100000.0);
      }

      EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
      if (speedAttribute != null) {
         speedAttribute.setBaseValue(0.25);
      }
   }

   @Override
   protected void applyDefaultEffects(PlayerEntity player) {
      player.addStatusEffect(new StatusEffectInstance(ModEffects.CYAN_GHOST_DOMAIN_TARGET, 200, this.getGhostDomainActualLevel() - 1, false, false));
      this.applyWindFieldEffect(player);
   }

   private void applyWindFieldEffect(PlayerEntity player) {
      if (this.isInGhostDomain(player) && !player.hasStatusEffect(ModEffects.GHOST_WIND_EFFECT)) {
         player.addStatusEffect(new StatusEffectInstance(ModEffects.GHOST_WIND_EFFECT, 600, 0));
         NbtCompound playerData = PlayerEvents.getCachedData(player);
         NbtCompound ghostWindData = new NbtCompound();
         ghostWindData.putUuid("ghost_wind_entity", this.getUuid());
         ghostWindData.putLong("wind_start_time", this.getWorld().getTime());
         ghostWindData.putFloat("wind_progress", 0.0F);
         ghostWindData.putLong("last_update_time", this.getWorld().getTime());
         playerData.put("ghost_wind_effect", ghostWindData);
         LOGGER.info("玩家 {} 获得鬼风效果，来源鬼风鬼: {}，位置: {}", player.getName().getString(), this.getUuid(), player.getBlockPos());
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
            DamageSource damageSource = ModDamageSources.ghost(this.getWorld());
            PlayerEvents.handleSpiritDamage(player, this.getSpiritualDamage(), this.getSpiritualDamage(), damageSource);
            LOGGER.info("鬼风鬼 {} 对玩家 {} 执行灵异袭击，造成 {} 点灵异伤害", this.getUuid(), player.getName().getString(), this.getSpiritualDamage());
         }
      }
   }

   @Override
   public float getGhostDomainRadius() {
      return 24.0F;
   }
}
