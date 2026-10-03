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
   private Vec3d entityPosition = Vec3d.field_1353;

   public FootprintBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlockEntities.FOOTPRINT_BLOCK_ENTITY, pos, state);
   }

   public void setEntityInfo(LivingEntity entity) {
      this.entityUUID = entity.method_5667().toString();
      this.entityType = entity.method_5864().method_5882();
      this.entityPosition = entity.method_19538();
      this.method_5431();
      if (this.field_11863 != null && !this.field_11863.method_8608()) {
         this.field_11863.method_8413(this.field_11867, this.method_11010(), this.method_11010(), 3);
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

            for (Entity entity : world.method_8390(Entity.class, new Box(-3.0E7, -64.0, -3.0E7, 3.0E7, 320.0, 3.0E7), ex -> true)) {
               if (entity.method_5667().equals(uuid) && entity instanceof LivingEntity livingEntity && livingEntity.method_5805()) {
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

   public void method_11014(NbtCompound nbt) {
      super.method_11014(nbt);
      if (nbt.method_10545("EntityUUID")) {
         this.entityUUID = nbt.method_10558("EntityUUID");
      }

      if (nbt.method_10545("EntityType")) {
         this.entityType = nbt.method_10558("EntityType");
      }

      if (nbt.method_10545("EntityPosition")) {
         double x = nbt.method_10574("EntityPositionX");
         double y = nbt.method_10574("EntityPositionY");
         double z = nbt.method_10574("EntityPositionZ");
         this.entityPosition = new Vec3d(x, y, z);
      }
   }

   public void method_11007(NbtCompound nbt) {
      super.method_11007(nbt);
      if (this.entityUUID != null && !this.entityUUID.isEmpty()) {
         nbt.method_10582("EntityUUID", this.entityUUID);
      }

      if (this.entityType != null && !this.entityType.isEmpty()) {
         nbt.method_10582("EntityType", this.entityType);
      }

      if (this.entityPosition != null) {
         nbt.method_10549("EntityPositionX", this.entityPosition.field_1352);
         nbt.method_10549("EntityPositionY", this.entityPosition.field_1351);
         nbt.method_10549("EntityPositionZ", this.entityPosition.field_1350);
      }
   }

   @Nullable
   public Packet<ClientPlayPacketListener> method_38235() {
      return BlockEntityUpdateS2CPacket.method_38585(this);
   }

   public NbtCompound method_16887() {
      return this.method_38244();
   }
}
