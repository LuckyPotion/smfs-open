package com.xie.smfs.api.impl;

import com.xie.smfs.Smfs;
import com.xie.smfs.api.GhostAPI;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.manager.GhostDomainManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GhostAPIImpl implements GhostAPI {
   @Override
   public <T extends GhostEntity> EntityType<T> registerCustomGhost(Identifier id, Class<T> ghostClass, EntityDimensions dimensions) {
      if (!GhostEntity.class.isAssignableFrom(ghostClass)) {
         throw new IllegalArgumentException("Custom ghost class must extend GhostEntity");
      } else {
         return (EntityType<T>)Registry.register(Registries.ENTITY_TYPE, id, FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, (type, world) -> {
            try {
               return ghostClass.getConstructor(EntityType.class, World.class).newInstance(type, world);
            } catch (Exception e) {
               throw new RuntimeException("Failed to create ghost entity", e);
            }
         }).dimensions(dimensions).build());
      }
   }

   @Override
   public Builder createCustomGhostAttributes(double maxHealth, double movementSpeed, double attackDamage, double followRange) {
      return GhostEntity.createGhostAttributes()
         .add(EntityAttributes.GENERIC_MAX_HEALTH, maxHealth)
         .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, movementSpeed)
         .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, attackDamage)
         .add(EntityAttributes.GENERIC_FOLLOW_RANGE, followRange);
   }

   @Override
   public Builder getDefaultGhostAttributes() {
      return GhostEntity.createGhostAttributes();
   }

   @Override
   public GhostEntity spawnGhost(
      EntityType<? extends GhostEntity> ghostType,
      World world,
      double x,
      double y,
      double z,
      boolean hasGhostDomain,
      int ghostDomainLevel,
      double ghostDomainRadius,
      char terrorLevel
   ) {
      try {
         GhostEntity ghost = (GhostEntity)ghostType.create(world);
         if (ghost != null) {
            ghost.refreshPositionAndAngles(x, y, z, world.random.nextFloat() * 360.0F, 0.0F);
            ghost.setGhostDomainEnabled(hasGhostDomain);
            ghost.setGhostDomainLevel(ghostDomainLevel);
            ghost.setGhostDomainRadius((float)ghostDomainRadius);
            ghost.setTerrorLevel(terrorLevel);
            world.spawnEntity(ghost);
            return ghost;
         }
      } catch (Exception e) {
         Smfs.LOGGER.error("Failed to spawn ghost: {}", e.getMessage(), e);
      }

      return null;
   }

   @Override
   public boolean isGhostEntity(Entity entity) {
      return entity instanceof GhostEntity;
   }

   @Override
   public int getSpiritualStrength(GhostEntity ghost) {
      return ghost.getSpiritualStrength();
   }

   @Override
   public void setSpiritualStrength(GhostEntity ghost, int strength) {
      ghost.setSpiritualStrength(strength);
   }

   @Override
   public int getSpiritualResistance(GhostEntity ghost) {
      return ghost.getSpiritualResistance();
   }

   @Override
   public void setSpiritualResistance(GhostEntity ghost, int resistance) {
      ghost.setSpiritualResistance(resistance);
   }

   @Override
   public int getSpiritualDamage(GhostEntity ghost) {
      return ghost.getSpiritualDamage();
   }

   @Override
   public void setSpiritualDamage(GhostEntity ghost, int damage) {
      ghost.setSpiritualDamage(damage);
   }

   @Override
   public float getRecoveryFactor(GhostEntity ghost) {
      return ghost.getRecoveryFactor();
   }

   @Override
   public void setRecoveryFactor(GhostEntity ghost, float factor) {
      ghost.setRecoveryFactor(factor);
   }

   @Override
   public float getGhostDomainRadius(GhostEntity ghost) {
      return ghost.getGhostDomainRadius();
   }

   @Override
   public void setGhostDomainRadius(GhostEntity ghost, float radius) {
      ghost.setGhostDomainRadius(radius);
   }

   @Override
   public void enableGhostDomain(GhostEntity ghost) {
      ghost.enableGhostDomain();
   }

   @Override
   public void disableGhostDomain(GhostEntity ghost) {
      ghost.disableGhostDomain();
   }

   @Override
   public void suppressGhost(GhostEntity ghost) {
      ghost.setSuppressed(true);
   }

   @Override
   public void unsuppressGhost(GhostEntity ghost) {
      ghost.setSuppressed(false);
   }

   @Override
   public boolean isGhostSuppressed(GhostEntity ghost) {
      return ghost.isSuppressed();
   }

   @Override
   public boolean isGhostDeadlocked(GhostEntity ghost) {
      return ghost.isDeadlocked();
   }

   @Override
   public void registerCustomGoal(GhostEntity ghost, int priority, Goal goal) {
      ghost.getGoalSelector().add(priority, goal);
   }

   @Override
   public void registerCustomTargetGoal(GhostEntity ghost, int priority, Goal targetGoal) {
      ghost.getTargetSelector().add(priority, targetGoal);
   }

   @Override
   public void clearCustomGoals(GhostEntity ghost) {
      ghost.getGoalSelector().clear(null);
      ghost.getTargetSelector().clear(null);
   }

   @Override
   public void setCustomAttackLogic(GhostEntity ghost, Consumer<PlayerEntity> attackLogic) {
      try {
         Field field = GhostEntity.class.getDeclaredField("customAttackLogic");
         field.setAccessible(true);
         field.set(ghost, attackLogic);
      } catch (Exception e) {
         Smfs.LOGGER.warn("Failed to set custom attack logic: {}", e.getMessage());
      }
   }

   @Override
   public void setCustomTargetSelector(GhostEntity ghost, Predicate<PlayerEntity> targetSelector) {
      try {
         Field field = GhostEntity.class.getDeclaredField("customTargetSelector");
         field.setAccessible(true);
         field.set(ghost, targetSelector);
      } catch (Exception e) {
         Smfs.LOGGER.warn("Failed to set custom target selector: {}", e.getMessage());
      }
   }

   @Override
   public void setMovementSpeed(GhostEntity ghost, double speed) {
      ghost.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
   }

   @Override
   public void setFollowRange(GhostEntity ghost, double range) {
      ghost.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(range);
   }

   @Override
   public void addStateChangeListener(GhostEntity ghost, BiConsumer<GhostEntity, String> listener) {
      try {
         Field field = GhostEntity.class.getDeclaredField("stateListeners");
         field.setAccessible(true);
         List<BiConsumer<GhostEntity, String>> listeners = (List<BiConsumer<GhostEntity, String>>)field.get(ghost);
         if (listeners == null) {
            listeners = new ArrayList<>();
            field.set(ghost, listeners);
         }

         listeners.add(listener);
      } catch (Exception e) {
         Smfs.LOGGER.warn("Failed to add state change listener: {}", e.getMessage());
      }
   }

   @Override
   public void removeStateChangeListener(GhostEntity ghost, BiConsumer<GhostEntity, String> listener) {
      try {
         Field field = GhostEntity.class.getDeclaredField("stateListeners");
         field.setAccessible(true);
         List<BiConsumer<GhostEntity, String>> listeners = (List<BiConsumer<GhostEntity, String>>)field.get(ghost);
         if (listeners != null) {
            listeners.remove(listener);
         }
      } catch (Exception e) {
         Smfs.LOGGER.warn("Failed to remove state change listener: {}", e.getMessage());
      }
   }

   @Override
   public void triggerCustomEvent(GhostEntity ghost, String eventName, Object data) {
      Smfs.LOGGER.debug("Custom event triggered: {} for ghost {}", eventName, ghost.getUuid());
   }

   @Override
   public void addCustomNbtSerializer(
      GhostEntity ghost, String key, Function<GhostEntity, NbtElement> serializer, BiConsumer<GhostEntity, NbtElement> deserializer
   ) {
      try {
         Field field = GhostEntity.class.getDeclaredField("customNbtSerializers");
         field.setAccessible(true);
         Map<String, Function<GhostEntity, NbtElement>> serializers = (Map<String, Function<GhostEntity, NbtElement>>)field.get(ghost);
         Map<String, BiConsumer<GhostEntity, NbtElement>> deserializers = (Map<String, BiConsumer<GhostEntity, NbtElement>>)field.get(ghost);
         if (serializers == null) {
            serializers = new HashMap<>();
            field.set(ghost, serializers);
         }

         if (deserializers == null) {
            deserializers = new HashMap<>();
            field.set(ghost, deserializers);
         }

         serializers.put(key, serializer);
         deserializers.put(key, deserializer);
      } catch (Exception e) {
         Smfs.LOGGER.warn("Failed to add custom NBT serializer: {}", e.getMessage());
      }
   }

   @Override
   public int getGhostLevel(GhostEntity ghost) {
      return ghost.ghostLevel;
   }

   @Override
   public void setGhostLevel(GhostEntity ghost, int level) {
      ghost.ghostLevel = Math.max(1, Math.min(5, level));
   }

   @Override
   public boolean hasGhostDomain(GhostEntity ghost) {
      return ghost.hasGhostDomain;
   }

   @Override
   public int getGhostDomainLevel(GhostEntity ghost) {
      return ghost.getGhostDomainLevel();
   }

   @Override
   public void setGhostDomainLevel(GhostEntity ghost, int level) {
      ghost.setGhostDomainLevel(Math.max(0, level));
   }

   @Override
   public char getTerrorLevel(GhostEntity ghost) {
      return ghost.getTerrorLevel();
   }

   @Override
   public void setTerrorLevel(GhostEntity ghost, char level) {
      char processedLevel = Character.toUpperCase(level);
      if (processedLevel >= 'A' && processedLevel <= 'S') {
         ghost.setTerrorLevel(processedLevel);
         ghost.setRecoveryFactor(ghost.getRecoveryFactorByTearorLevel(processedLevel));
      }
   }

   @Override
   public boolean isVisible(GhostEntity ghost) {
      return ghost.isVisible();
   }

   @Override
   public void setVisible(GhostEntity ghost, boolean visible) {
      ghost.setVisible(visible);
   }

   @Override
   public int getAttackCooldown(GhostEntity ghost) {
      return ghost.attackCooldown;
   }

   @Override
   public void setAttackCooldown(GhostEntity ghost, int cooldown) {
      ghost.attackCooldown = Math.max(0, cooldown);
   }

   @Override
   public int getPlayerGhostDomainLevel(PlayerEntity player) {
      return GhostDomainManager.getGhostDomainLevel(player);
   }

   @Override
   public int getGhostTypeLevel(PlayerEntity player, String ghostType) {
      if (ghostType != null && !ghostType.isEmpty()) {
         for (int i = 0; i < 10; i++) {
            String currentGhostType = PlayerEvents.getGhostTypeInSlot(player, i);
            if (ghostType.equals(currentGhostType)) {
               return PlayerEvents.getGhostSlotLevel(player, i);
            }
         }

         return 0;
      } else {
         return 0;
      }
   }
}
