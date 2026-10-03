package com.xie.smfs.block.entity;

import com.xie.smfs.block.GhostCoffinBlock;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.registry.ModBlockEntities;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostCoffinBlockEntity extends BlockEntity implements GeoBlockEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private UUID storedGhostUuid;
   private boolean isGhostSuppressed = true;
   private GhostEntity cachedGhost;
   private static final Logger LOGGER = LoggerFactory.getLogger(GhostCoffinBlockEntity.class);
   private EntityType<?> markedGhostType = null;
   private int suppressionQuota = 0;

   public GhostCoffinBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.GHOST_COFFIN_BLOCK_ENTITY, pos, state);
   }

   private void cleanupInvalidReference() {
      LOGGER.warn("清理无效的鬼实体引用: {}", this.storedGhostUuid);
      this.storedGhostUuid = null;
      this.isGhostSuppressed = false;
      this.cachedGhost = null;
      this.markedGhostType = null;
      this.method_5431();
   }

   public void storeGhost(GhostEntity ghost) {
      if (ghost != null && !ghost.method_31481()) {
         LOGGER.debug("存储鬼实体: {}", ghost.method_5667());
         this.storedGhostUuid = ghost.method_5667();
         this.cachedGhost = ghost;
         this.isGhostSuppressed = true;
         this.markedGhostType = ghost.method_5864();
         ghost.setSuppressed(true);
         ghost.setMovementDisabled(true);
         BlockState state = this.method_11010();
         boolean isOpen = state.method_28498(GhostCoffinBlock.OPEN) && (Boolean)state.method_11654(GhostCoffinBlock.OPEN);
         ghost.setInClosedCoffin(!isOpen);
         ghost.setVisible(isOpen);
         ghost.method_20620(this.field_11867.method_10263() + 0.5, this.field_11867.method_10264() + 0.1, this.field_11867.method_10260() + 0.5);
         if (state.method_28498(GhostCoffinBlock.FACING)) {
            Direction facing = (Direction)state.method_11654(GhostCoffinBlock.FACING);
            float yaw = this.getYawFromDirection(facing);
            ghost.method_36456(yaw);
            ghost.method_5847(yaw);
            ghost.method_5636(yaw);
         }

         ghost.method_31472();
         LOGGER.debug("鬼实体已存储并discard: {}", this.storedGhostUuid);
         if (this.field_11863 != null && !this.field_11863.field_9236) {
            BlockState newState = (BlockState)((BlockState)state.method_11657(GhostCoffinBlock.OPEN, false)).method_11657(GhostCoffinBlock.OCCUPIED, true);
            this.field_11863.method_8652(this.field_11867, newState, 3);
            LOGGER.debug("棺材已自动关闭并设置为占用状态");
         }

         this.method_5431();
      } else {
         LOGGER.warn("尝试存储无效的鬼实体");
      }
   }

   public void updateGhostCoffinState() {
      this.getStoredGhost().ifPresent(ghost -> {
         BlockState state = this.method_11010();
         if (state.method_28498(GhostCoffinBlock.OPEN)) {
            boolean isOpen = (Boolean)state.method_11654(GhostCoffinBlock.OPEN);
            LOGGER.debug("更新鬼的状态: 棺材打开状态 = {}", isOpen);
            ghost.setInClosedCoffin(!isOpen);
            ghost.setVisible(isOpen);
            ghost.setSuppressed(this.isGhostSuppressed);
            ghost.setMovementDisabled(this.isGhostSuppressed);
         }
      });
   }

   public Optional<GhostEntity> getGhost() {
      if (this.storedGhostUuid == null) {
         return Optional.empty();
      }

      World world = this.method_10997();
      if (world == null) {
         return Optional.empty();
      }

      GhostEntity ghost = this.cachedGhost;
      if (ghost == null || ghost.method_31481()) {
         ghost = this.findGhostByUuid(world, this.storedGhostUuid);
      }

      if ((ghost == null || ghost.method_31481()) && this.markedGhostType != null) {
         try {
            Entity entity = this.markedGhostType.method_5883(world);
            if (entity instanceof GhostEntity) {
               ghost = (GhostEntity)entity;
               ghost.method_5971();
               ghost.method_5808(
                  this.field_11867.method_10263() + 0.5, this.field_11867.method_10264() + 0.5, this.field_11867.method_10260() + 0.5, 0.0F, 0.0F
               );
               ghost.setSuppressed(true);
               ghost.setMovementDisabled(true);
               ghost.setInClosedCoffin(true);
               ghost.setVisible(false);
               ghost.method_5826(this.storedGhostUuid);
               world.method_8649(ghost);
               this.cachedGhost = ghost;
               LOGGER.debug("从标记类型重新生成鬼实体: {}", this.storedGhostUuid);
            } else if (entity != null) {
               entity.method_31472();
            }
         } catch (Exception e) {
            LOGGER.error("重新生成鬼实体时出错", e);
         }
      }

      return Optional.ofNullable(ghost).filter(g -> !g.method_31481());
   }

   public Optional<GhostEntity> releaseGhost() {
      if (this.storedGhostUuid == null) {
         return Optional.empty();
      }

      World world = this.method_10997();
      if (world == null) {
         return Optional.empty();
      }

      GhostEntity ghost = this.cachedGhost;
      if (ghost == null || ghost.method_31481()) {
         ghost = this.findGhostByUuid(world, this.storedGhostUuid);
      }

      if ((ghost == null || ghost.method_31481()) && this.markedGhostType != null) {
         try {
            Entity entity = this.markedGhostType.method_5883(world);
            if (entity instanceof GhostEntity) {
               ghost = (GhostEntity)entity;
               ghost.method_5971();
               ghost.method_5826(this.storedGhostUuid);
               world.method_8649(ghost);
               LOGGER.debug("释放时从标记类型重新生成鬼实体: {}", this.storedGhostUuid);
            } else if (entity != null) {
               entity.method_31472();
            }
         } catch (Exception e) {
            LOGGER.error("释放鬼实体时重新生成出错", e);
         }
      }

      if (ghost != null && !ghost.method_31481()) {
         ghost.setSuppressed(false);
         ghost.setMovementDisabled(false);
         ghost.setInClosedCoffin(false);
         ghost.setVisible(true);
         if (this.method_11010().method_28498(GhostCoffinBlock.FACING)) {
            Vec3i facingVector = ((Direction)this.method_11010().method_11654(GhostCoffinBlock.FACING)).method_10163();
            Vec3d offset = new Vec3d(facingVector.method_10263() * 1.5, 0.5, facingVector.method_10260() * 1.5);
            Vec3d spawnPos = Vec3d.method_24953(this.field_11867).method_1019(offset);
            ghost.method_20620(spawnPos.field_1352, spawnPos.field_1351, spawnPos.field_1350);
         }

         this.storedGhostUuid = null;
         this.cachedGhost = null;
         this.isGhostSuppressed = false;
         this.markedGhostType = null;
         this.method_5431();
         return Optional.of(ghost);
      } else {
         this.cleanupInvalidReference();
         return Optional.empty();
      }
   }

   public void onBlockStateChanged() {
      this.updateGhostCoffinState();
   }

   private GhostEntity findGhostByUuid(World world, UUID uuid) {
      if (world != null && uuid != null) {
         for (GhostEntity ghost : world.method_8390(GhostEntity.class, new Box(this.field_11867).method_1014(16.0), e -> true)) {
            if (ghost.method_5667().equals(uuid) && !ghost.method_31481()) {
               return ghost;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public boolean hasStoredGhost() {
      return this.storedGhostUuid != null;
   }

   public void clearStoredGhost() {
      this.storedGhostUuid = null;
      this.cachedGhost = null;
      this.isGhostSuppressed = false;
      this.markedGhostType = null;
      this.method_5431();
   }

   public UUID getStoredGhostUuid() {
      return this.storedGhostUuid;
   }

   public void method_11007(NbtCompound nbt) {
      super.method_11007(nbt);
      if (this.storedGhostUuid != null) {
         nbt.method_25927("StoredGhostUuid", this.storedGhostUuid);
         nbt.method_10556("IsGhostSuppressed", this.isGhostSuppressed);
      }

      if (this.markedGhostType != null) {
         nbt.method_10582("MarkedGhostType", Registries.field_41177.method_10221(this.markedGhostType).toString());
      }

      nbt.method_10569("SuppressionQuota", this.suppressionQuota);
   }

   public void method_11014(NbtCompound nbt) {
      super.method_11014(nbt);
      if (nbt.method_10545("StoredGhostUuid")) {
         this.storedGhostUuid = nbt.method_25926("StoredGhostUuid");
         this.isGhostSuppressed = nbt.method_10577("IsGhostSuppressed");
         this.cachedGhost = null;
         LOGGER.debug("读取到存储的鬼实体UUID: {}", this.storedGhostUuid);
      } else {
         this.storedGhostUuid = null;
         this.cachedGhost = null;
         this.isGhostSuppressed = false;
      }

      if (nbt.method_10545("MarkedGhostType")) {
         String typeId = nbt.method_10558("MarkedGhostType");
         this.markedGhostType = this.matchGhostTypeByString(typeId);
         if (this.markedGhostType != null) {
            LOGGER.debug("通过字符串匹配标记鬼实体类型: {} -> {}", typeId, Registries.field_41177.method_10221(this.markedGhostType));
         } else {
            LOGGER.warn("无法通过字符串匹配找到对应的鬼实体类型: {}", typeId);
            this.markedGhostType = null;
         }
      } else {
         this.markedGhostType = null;
      }

      if (nbt.method_10545("SuppressionQuota")) {
         this.suppressionQuota = nbt.method_10550("SuppressionQuota");
      } else {
         this.suppressionQuota = 0;
      }
   }

   private EntityType<?> matchGhostTypeByString(String typeString) {
      if (typeString != null && !typeString.isEmpty()) {
         try {
            EntityType<?> directType = (EntityType<?>)Registries.field_41177.method_10223(new Identifier(typeString));
            if (directType != null && this.isGhostEntityType(directType)) {
               return directType;
            }
         } catch (Exception var7) {
         }

         String lowerTypeString = typeString.toLowerCase();
         if (lowerTypeString.contains("fog") || lowerTypeString.contains("雾") || lowerTypeString.contains("鬼雾")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "fog_ghost"));
         }

         if (lowerTypeString.contains("block") || lowerTypeString.contains("方块") || lowerTypeString.contains("方块鬼")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "block_ghost"));
         }

         if (lowerTypeString.contains("lost") || lowerTypeString.contains("遗忘") || lowerTypeString.contains("遗忘鬼")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "lost_ghost"));
         }

         if (lowerTypeString.contains("look_up") || lowerTypeString.contains("抬头") || lowerTypeString.contains("抬头鬼")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "look_up_ghost"));
         }

         if (lowerTypeString.contains("look_down") || lowerTypeString.contains("低头") || lowerTypeString.contains("低头鬼")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "look_down_ghost"));
         }

         if (lowerTypeString.contains("door") || lowerTypeString.contains("门") || lowerTypeString.contains("敲门鬼")) {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "door_ghost"));
         }

         if (!lowerTypeString.contains("player") && !lowerTypeString.contains("玩家") && !lowerTypeString.contains("玩家鬼")) {
            for (EntityType<?> entityType : Registries.field_41177) {
               if (this.isGhostEntityType(entityType)) {
                  String translationKey = entityType.method_5882().toLowerCase();
                  String entityId = Registries.field_41177.method_10221(entityType).toString().toLowerCase();
                  if (translationKey.contains(lowerTypeString) || entityId.contains(lowerTypeString)) {
                     return entityType;
                  }
               }
            }

            return null;
         } else {
            return (EntityType<?>)Registries.field_41177.method_10223(new Identifier("smfs", "player_ghost"));
         }
      } else {
         return null;
      }
   }

   private boolean isGhostEntityType(EntityType<?> entityType) {
      return AdvancementManager.isGhostEntityType(entityType);
   }

   private float getYawFromDirection(Direction direction) {
      return switch (direction) {
         case field_11043 -> 180.0F;
         case field_11035 -> 0.0F;
         case field_11039 -> 90.0F;
         case field_11034 -> -90.0F;
         default -> 0.0F;
      };
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "state_controller", 0, this::handleAnimation));
   }

   private PlayState handleAnimation(AnimationState<GhostCoffinBlockEntity> state) {
      BlockState blockState = this.method_11010();
      if (!blockState.method_28498(GhostCoffinBlock.OPEN)) {
         return PlayState.STOP;
      }

      boolean isOpen = (Boolean)blockState.method_11654(GhostCoffinBlock.OPEN);
      if (isOpen) {
         state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.ghost_coffin.open"));
      } else {
         state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.ghost_coffin.close"));
      }

      return PlayState.CONTINUE;
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public Optional<GhostEntity> getStoredGhost() {
      if (this.storedGhostUuid == null || this.field_11863 == null) {
         return Optional.empty();
      } else if (this.cachedGhost != null && !this.cachedGhost.method_31481()) {
         return Optional.of(this.cachedGhost);
      } else {
         GhostEntity ghost = this.findGhostByUuid(this.field_11863, this.storedGhostUuid);
         if (ghost != null) {
            this.cachedGhost = ghost;
            return Optional.of(ghost);
         } else {
            return Optional.empty();
         }
      }
   }

   public boolean isGhostSuppressed() {
      return this.isGhostSuppressed;
   }

   public void setGhostSuppressed(boolean suppressed) {
      this.isGhostSuppressed = suppressed;
      this.getStoredGhost().ifPresent(ghost -> ghost.setSuppressed(suppressed));
      this.method_5431();
   }

   public void markGhostType(EntityType<?> ghostType) {
      this.markedGhostType = ghostType;
      this.method_5431();
      LOGGER.debug("标记鬼实体类型: {}", Registries.field_41177.method_10221(ghostType));
   }

   public void clearMarkedGhostType() {
      this.markedGhostType = null;
      this.method_5431();
      LOGGER.debug("清除标记的鬼实体类型");
   }

   public EntityType<?> getMarkedGhostType() {
      return this.markedGhostType;
   }

   public boolean hasMarkedGhostType() {
      return this.markedGhostType != null;
   }

   public Optional<GhostEntity> spawnFromMarkedType() {
      if (this.markedGhostType != null && this.field_11863 != null) {
         try {
            Entity entity = this.markedGhostType.method_5883(this.field_11863);
            if (entity instanceof GhostEntity ghost) {
               ghost.method_5808(
                  this.field_11867.method_10263() + 0.5, this.field_11867.method_10264() + 0.5, this.field_11867.method_10260() + 0.5, 0.0F, 0.0F
               );
               ghost.method_5971();
               this.field_11863.method_8649(ghost);
               LOGGER.debug("从标记类型生成鬼实体: {}", Registries.field_41177.method_10221(this.markedGhostType));
               return Optional.of(ghost);
            }

            if (entity != null) {
               entity.method_31472();
            }
         } catch (Exception e) {
            LOGGER.error("从标记类型生成鬼实体时出错", e);
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public int getSuppressionQuota() {
      return this.suppressionQuota;
   }

   public void setSuppressionQuota(int suppressionQuota) {
      this.suppressionQuota = suppressionQuota;
      this.method_5431();
      LOGGER.debug("设置压制名额: {}", suppressionQuota);
   }
}
