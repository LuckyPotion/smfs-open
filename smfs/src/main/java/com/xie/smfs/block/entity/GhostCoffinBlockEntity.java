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
      this.markDirty();
   }

   public void storeGhost(GhostEntity ghost) {
      if (ghost != null && !ghost.isRemoved()) {
         LOGGER.debug("存储鬼实体: {}", ghost.getUuid());
         this.storedGhostUuid = ghost.getUuid();
         this.cachedGhost = ghost;
         this.isGhostSuppressed = true;
         this.markedGhostType = ghost.getType();
         ghost.setSuppressed(true);
         ghost.setMovementDisabled(true);
         BlockState state = this.getCachedState();
         boolean isOpen = state.contains(GhostCoffinBlock.OPEN) && (Boolean)state.get(GhostCoffinBlock.OPEN);
         ghost.setInClosedCoffin(!isOpen);
         ghost.setVisible(isOpen);
         ghost.teleport(this.pos.getX() + 0.5, this.pos.getY() + 0.1, this.pos.getZ() + 0.5);
         if (state.contains(GhostCoffinBlock.FACING)) {
            Direction facing = (Direction)state.get(GhostCoffinBlock.FACING);
            float yaw = this.getYawFromDirection(facing);
            ghost.setYaw(yaw);
            ghost.setHeadYaw(yaw);
            ghost.setBodyYaw(yaw);
         }

         ghost.discard();
         LOGGER.debug("鬼实体已存储并discard: {}", this.storedGhostUuid);
         if (this.world != null && !this.world.isClient) {
            BlockState newState = (BlockState)((BlockState)state.with(GhostCoffinBlock.OPEN, false)).with(GhostCoffinBlock.OCCUPIED, true);
            this.world.setBlockState(this.pos, newState, 3);
            LOGGER.debug("棺材已自动关闭并设置为占用状态");
         }

         this.markDirty();
      } else {
         LOGGER.warn("尝试存储无效的鬼实体");
      }
   }

   public void updateGhostCoffinState() {
      this.getStoredGhost().ifPresent(ghost -> {
         BlockState state = this.getCachedState();
         if (state.contains(GhostCoffinBlock.OPEN)) {
            boolean isOpen = (Boolean)state.get(GhostCoffinBlock.OPEN);
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

      World world = this.getWorld();
      if (world == null) {
         return Optional.empty();
      }

      GhostEntity ghost = this.cachedGhost;
      if (ghost == null || ghost.isRemoved()) {
         ghost = this.findGhostByUuid(world, this.storedGhostUuid);
      }

      if ((ghost == null || ghost.isRemoved()) && this.markedGhostType != null) {
         try {
            Entity entity = this.markedGhostType.create(world);
            if (entity instanceof GhostEntity) {
               ghost = (GhostEntity)entity;
               ghost.setPersistent();
               ghost.refreshPositionAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 0.0F, 0.0F);
               ghost.setSuppressed(true);
               ghost.setMovementDisabled(true);
               ghost.setInClosedCoffin(true);
               ghost.setVisible(false);
               ghost.setUuid(this.storedGhostUuid);
               world.spawnEntity(ghost);
               this.cachedGhost = ghost;
               LOGGER.debug("从标记类型重新生成鬼实体: {}", this.storedGhostUuid);
            } else if (entity != null) {
               entity.discard();
            }
         } catch (Exception e) {
            LOGGER.error("重新生成鬼实体时出错", e);
         }
      }

      return Optional.ofNullable(ghost).filter(g -> !g.isRemoved());
   }

   public Optional<GhostEntity> releaseGhost() {
      if (this.storedGhostUuid == null) {
         return Optional.empty();
      }

      World world = this.getWorld();
      if (world == null) {
         return Optional.empty();
      }

      GhostEntity ghost = this.cachedGhost;
      if (ghost == null || ghost.isRemoved()) {
         ghost = this.findGhostByUuid(world, this.storedGhostUuid);
      }

      if ((ghost == null || ghost.isRemoved()) && this.markedGhostType != null) {
         try {
            Entity entity = this.markedGhostType.create(world);
            if (entity instanceof GhostEntity) {
               ghost = (GhostEntity)entity;
               ghost.setPersistent();
               ghost.setUuid(this.storedGhostUuid);
               world.spawnEntity(ghost);
               LOGGER.debug("释放时从标记类型重新生成鬼实体: {}", this.storedGhostUuid);
            } else if (entity != null) {
               entity.discard();
            }
         } catch (Exception e) {
            LOGGER.error("释放鬼实体时重新生成出错", e);
         }
      }

      if (ghost != null && !ghost.isRemoved()) {
         ghost.setSuppressed(false);
         ghost.setMovementDisabled(false);
         ghost.setInClosedCoffin(false);
         ghost.setVisible(true);
         if (this.getCachedState().contains(GhostCoffinBlock.FACING)) {
            Vec3i facingVector = ((Direction)this.getCachedState().get(GhostCoffinBlock.FACING)).getVector();
            Vec3d offset = new Vec3d(facingVector.getX() * 1.5, 0.5, facingVector.getZ() * 1.5);
            Vec3d spawnPos = Vec3d.ofCenter(this.pos).add(offset);
            ghost.teleport(spawnPos.x, spawnPos.y, spawnPos.z);
         }

         this.storedGhostUuid = null;
         this.cachedGhost = null;
         this.isGhostSuppressed = false;
         this.markedGhostType = null;
         this.markDirty();
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
         for (GhostEntity ghost : world.getEntitiesByClass(GhostEntity.class, new Box(this.pos).expand(16.0), e -> true)) {
            if (ghost.getUuid().equals(uuid) && !ghost.isRemoved()) {
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
      this.markDirty();
   }

   public UUID getStoredGhostUuid() {
      return this.storedGhostUuid;
   }

   public void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.storedGhostUuid != null) {
         nbt.putUuid("StoredGhostUuid", this.storedGhostUuid);
         nbt.putBoolean("IsGhostSuppressed", this.isGhostSuppressed);
      }

      if (this.markedGhostType != null) {
         nbt.putString("MarkedGhostType", Registries.ENTITY_TYPE.getId(this.markedGhostType).toString());
      }

      nbt.putInt("SuppressionQuota", this.suppressionQuota);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("StoredGhostUuid")) {
         this.storedGhostUuid = nbt.getUuid("StoredGhostUuid");
         this.isGhostSuppressed = nbt.getBoolean("IsGhostSuppressed");
         this.cachedGhost = null;
         LOGGER.debug("读取到存储的鬼实体UUID: {}", this.storedGhostUuid);
      } else {
         this.storedGhostUuid = null;
         this.cachedGhost = null;
         this.isGhostSuppressed = false;
      }

      if (nbt.contains("MarkedGhostType")) {
         String typeId = nbt.getString("MarkedGhostType");
         this.markedGhostType = this.matchGhostTypeByString(typeId);
         if (this.markedGhostType != null) {
            LOGGER.debug("通过字符串匹配标记鬼实体类型: {} -> {}", typeId, Registries.ENTITY_TYPE.getId(this.markedGhostType));
         } else {
            LOGGER.warn("无法通过字符串匹配找到对应的鬼实体类型: {}", typeId);
            this.markedGhostType = null;
         }
      } else {
         this.markedGhostType = null;
      }

      if (nbt.contains("SuppressionQuota")) {
         this.suppressionQuota = nbt.getInt("SuppressionQuota");
      } else {
         this.suppressionQuota = 0;
      }
   }

   private EntityType<?> matchGhostTypeByString(String typeString) {
      if (typeString != null && !typeString.isEmpty()) {
         try {
            EntityType<?> directType = (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier(typeString));
            if (directType != null && this.isGhostEntityType(directType)) {
               return directType;
            }
         } catch (Exception var7) {
         }

         String lowerTypeString = typeString.toLowerCase();
         if (lowerTypeString.contains("fog") || lowerTypeString.contains("雾") || lowerTypeString.contains("鬼雾")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "fog_ghost"));
         }

         if (lowerTypeString.contains("block") || lowerTypeString.contains("方块") || lowerTypeString.contains("方块鬼")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "block_ghost"));
         }

         if (lowerTypeString.contains("lost") || lowerTypeString.contains("遗忘") || lowerTypeString.contains("遗忘鬼")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "lost_ghost"));
         }

         if (lowerTypeString.contains("look_up") || lowerTypeString.contains("抬头") || lowerTypeString.contains("抬头鬼")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "look_up_ghost"));
         }

         if (lowerTypeString.contains("look_down") || lowerTypeString.contains("低头") || lowerTypeString.contains("低头鬼")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "look_down_ghost"));
         }

         if (lowerTypeString.contains("door") || lowerTypeString.contains("门") || lowerTypeString.contains("敲门鬼")) {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "door_ghost"));
         }

         if (!lowerTypeString.contains("player") && !lowerTypeString.contains("玩家") && !lowerTypeString.contains("玩家鬼")) {
            for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
               if (this.isGhostEntityType(entityType)) {
                  String translationKey = entityType.getTranslationKey().toLowerCase();
                  String entityId = Registries.ENTITY_TYPE.getId(entityType).toString().toLowerCase();
                  if (translationKey.contains(lowerTypeString) || entityId.contains(lowerTypeString)) {
                     return entityType;
                  }
               }
            }

            return null;
         } else {
            return (EntityType<?>)Registries.ENTITY_TYPE.get(new Identifier("smfs", "player_ghost"));
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
         case NORTH -> 180.0F;
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController<>(this, "state_controller", 0, this::handleAnimation));
   }

   private PlayState handleAnimation(AnimationState<GhostCoffinBlockEntity> state) {
      BlockState blockState = this.getCachedState();
      if (!blockState.contains(GhostCoffinBlock.OPEN)) {
         return PlayState.STOP;
      }

      boolean isOpen = (Boolean)blockState.get(GhostCoffinBlock.OPEN);
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
      if (this.storedGhostUuid == null || this.world == null) {
         return Optional.empty();
      } else if (this.cachedGhost != null && !this.cachedGhost.isRemoved()) {
         return Optional.of(this.cachedGhost);
      } else {
         GhostEntity ghost = this.findGhostByUuid(this.world, this.storedGhostUuid);
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
      this.markDirty();
   }

   public void markGhostType(EntityType<?> ghostType) {
      this.markedGhostType = ghostType;
      this.markDirty();
      LOGGER.debug("标记鬼实体类型: {}", Registries.ENTITY_TYPE.getId(ghostType));
   }

   public void clearMarkedGhostType() {
      this.markedGhostType = null;
      this.markDirty();
      LOGGER.debug("清除标记的鬼实体类型");
   }

   public EntityType<?> getMarkedGhostType() {
      return this.markedGhostType;
   }

   public boolean hasMarkedGhostType() {
      return this.markedGhostType != null;
   }

   public Optional<GhostEntity> spawnFromMarkedType() {
      if (this.markedGhostType != null && this.world != null) {
         try {
            Entity entity = this.markedGhostType.create(this.world);
            if (entity instanceof GhostEntity ghost) {
               ghost.refreshPositionAndAngles(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, 0.0F, 0.0F);
               ghost.setPersistent();
               this.world.spawnEntity(ghost);
               LOGGER.debug("从标记类型生成鬼实体: {}", Registries.ENTITY_TYPE.getId(this.markedGhostType));
               return Optional.of(ghost);
            }

            if (entity != null) {
               entity.discard();
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
      this.markDirty();
      LOGGER.debug("设置压制名额: {}", suppressionQuota);
   }
}
