package com.xie.smfs.api;

import com.xie.smfs.api.common.GhostAPIHolder;
import com.xie.smfs.entity.GhostEntity;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface GhostAPI {
   static void register(GhostAPI instance) {
      GhostAPIHolder.INSTANCE = instance;
   }

   static GhostAPI getInstance() {
      if (GhostAPIHolder.INSTANCE == null) {
         throw new IllegalStateException("GhostAPI not registered yet. Please call GhostAPI.register() first.");
      } else {
         return GhostAPIHolder.INSTANCE;
      }
   }

   static boolean isRegistered() {
      return GhostAPIHolder.INSTANCE != null;
   }

   <T extends GhostEntity> EntityType<T> registerCustomGhost(Identifier identifier, Class<T> class_, EntityDimensions entityDimensions);

   Builder createCustomGhostAttributes(double d, double e, double f, double g);

   Builder getDefaultGhostAttributes();

   GhostEntity spawnGhost(EntityType<? extends GhostEntity> entityType, World world, double d, double e, double f, boolean bl, int i, double g, char c);

   boolean isGhostEntity(Entity entity);

   int getSpiritualStrength(GhostEntity ghostEntity);

   void setSpiritualStrength(GhostEntity ghostEntity, int i);

   int getSpiritualResistance(GhostEntity ghostEntity);

   void setSpiritualResistance(GhostEntity ghostEntity, int i);

   int getSpiritualDamage(GhostEntity ghostEntity);

   void setSpiritualDamage(GhostEntity ghostEntity, int i);

   float getRecoveryFactor(GhostEntity ghostEntity);

   void setRecoveryFactor(GhostEntity ghostEntity, float f);

   float getGhostDomainRadius(GhostEntity ghostEntity);

   void setGhostDomainRadius(GhostEntity ghostEntity, float f);

   void enableGhostDomain(GhostEntity ghostEntity);

   void disableGhostDomain(GhostEntity ghostEntity);

   void suppressGhost(GhostEntity ghostEntity);

   void unsuppressGhost(GhostEntity ghostEntity);

   boolean isGhostSuppressed(GhostEntity ghostEntity);

   boolean isGhostDeadlocked(GhostEntity ghostEntity);

   void registerCustomGoal(GhostEntity ghostEntity, int i, Goal goal);

   void registerCustomTargetGoal(GhostEntity ghostEntity, int i, Goal goal);

   void clearCustomGoals(GhostEntity ghostEntity);

   void setCustomAttackLogic(GhostEntity ghostEntity, Consumer<PlayerEntity> consumer);

   void setCustomTargetSelector(GhostEntity ghostEntity, Predicate<PlayerEntity> predicate);

   void setMovementSpeed(GhostEntity ghostEntity, double d);

   void setFollowRange(GhostEntity ghostEntity, double d);

   void addStateChangeListener(GhostEntity ghostEntity, BiConsumer<GhostEntity, String> biConsumer);

   void removeStateChangeListener(GhostEntity ghostEntity, BiConsumer<GhostEntity, String> biConsumer);

   void triggerCustomEvent(GhostEntity ghostEntity, String string, Object object);

   void addCustomNbtSerializer(
      GhostEntity ghostEntity, String string, Function<GhostEntity, NbtElement> function, BiConsumer<GhostEntity, NbtElement> biConsumer
   );

   int getGhostLevel(GhostEntity ghostEntity);

   void setGhostLevel(GhostEntity ghostEntity, int i);

   boolean hasGhostDomain(GhostEntity ghostEntity);

   int getGhostDomainLevel(GhostEntity ghostEntity);

   void setGhostDomainLevel(GhostEntity ghostEntity, int i);

   char getTerrorLevel(GhostEntity ghostEntity);

   void setTerrorLevel(GhostEntity ghostEntity, char c);

   boolean isVisible(GhostEntity ghostEntity);

   void setVisible(GhostEntity ghostEntity, boolean bl);

   int getAttackCooldown(GhostEntity ghostEntity);

   void setAttackCooldown(GhostEntity ghostEntity, int i);

   int getPlayerGhostDomainLevel(PlayerEntity playerEntity);

   int getGhostTypeLevel(PlayerEntity playerEntity, String string);
}
