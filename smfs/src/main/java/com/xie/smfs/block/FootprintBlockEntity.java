package com.xie.smfs.block;

import com.xie.smfs.registry.ModBlockEntities;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FootprintBlockEntity extends BlockEntity {
   private static final String ENTITY_UUID_KEY = "EntityUUID";
   private static final String ENTITY_TYPE_KEY = "EntityType";
   private static final String ENTITY_POSITION_KEY = "EntityPosition";
   private String entityUUID = "";
   private String entityType = "";
   private Vec3d entityPosition = Vec3d.ZERO;

   public FootprintBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FOOTPRINT_BLOCK_ENTITY, pos, state);
   }

   public void setEntityInfo(LivingEntity entity) {
      this.entityUUID = entity.getUuid().toString();
      this.entityType = entity.getType().getTranslationKey();
      this.entityPosition = entity.getPos();
      this.markDirty();
      if (this.world != null && !this.world.isClient()) {
         this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
      }
   }

   public String getEntityUUID() {
      return this.entityUUID;
   }

   public String getEntityType() {
      return this.entityType;
   }

   public Vec3d getEntityPosition() {
      return this.entityPosition;
   }

   public boolean hasValidEntityInfo() {
      return this.entityUUID != null && !this.entityUUID.isEmpty() && this.entityType != null && !this.entityType.isEmpty();
   }

   @Nullable
   public LivingEntity getTargetEntity(World world) {
      if (this.hasValidEntityInfo() && world != null) {
         try {
            UUID uuid = UUID.fromString(this.entityUUID);

            for (Entity entity : world.getEntitiesByClass(Entity.class, new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7), ex -> true)) {
               if (entity.getUuid().equals(uuid) && entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                  return livingEntity;
               }
            }

            return null;
         } catch (IllegalArgumentException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("EntityUUID")) {
         this.entityUUID = nbt.getString("EntityUUID");
      }

      if (nbt.contains("EntityType")) {
         this.entityType = nbt.getString("EntityType");
      }

      if (nbt.contains("EntityPosition")) {
         double x = nbt.getDouble("EntityPositionX");
         double y = nbt.getDouble("EntityPositionY");
         double z = nbt.getDouble("EntityPositionZ");
         this.entityPosition = new Vec3d(x, y, z);
      }
   }

   public void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.entityUUID != null && !this.entityUUID.isEmpty()) {
         nbt.putString("EntityUUID", this.entityUUID);
      }

      if (this.entityType != null && !this.entityType.isEmpty()) {
         nbt.putString("EntityType", this.entityType);
      }

      if (this.entityPosition != null) {
         nbt.putDouble("EntityPositionX", this.entityPosition.x);
         nbt.putDouble("EntityPositionY", this.entityPosition.y);
         nbt.putDouble("EntityPositionZ", this.entityPosition.z);
      }
   }

   @Nullable
   public Packet<ClientPlayPacketListener> toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      return this.createNbt();
   }
}
