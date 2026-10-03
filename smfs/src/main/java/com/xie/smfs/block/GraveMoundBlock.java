package com.xie.smfs.block;

import com.xie.smfs.client.renderer.StaticAnimatable;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.entity.ghost.GraveEarthGhostEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.registry.ModBlockEntities;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GraveMoundBlock extends BlockWithEntity implements BlockEntityProvider {
   private static final int RESTORE_CHECK_INTERVAL = 20;
   private int checkTimer = 0;

   public GraveMoundBlock(Settings settings) {
      super(settings);
   }

   private static boolean isImmuneToGraveMound(LivingEntity entity) {
      return !(entity instanceof LuoQianGhostEntity) && !(entity instanceof GraveEarthGhostEntity)
         ? entity instanceof PlayerEntity player && PlayerEvents.hasGhostType(player, "grave_earth_ghost")
         : true;
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      super.onBlockAdded(state, world, pos, oldState, notify);
      if (!world.isClient()) {
         world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.HOSTILE, 1.0F, 0.8F);
         Box searchBox = new Box(pos).expand(1.0);

         for (LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, searchBox)) {
            if (!isImmuneToGraveMound(entity)) {
               entity.teleport(pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
               entity.setVelocity(0.0, 0.0, 0.0);
               entity.velocityModified = true;
               if (!entity.hasStatusEffect(ModEffects.SILENCE)) {
                  entity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, false));
               }
            }
         }

         world.scheduleBlockTick(pos, this, 1);
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1);

      for (LivingEntity entity : world.getNonSpectatingEntities(LivingEntity.class, box)) {
         if (!isImmuneToGraveMound(entity)) {
            if (!entity.hasStatusEffect(ModEffects.SILENCE)) {
               entity.addStatusEffect(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, false));
            }

            double dx = pos.getX() + 0.5 - entity.getX();
            double dz = pos.getZ() + 0.5 - entity.getZ();
            if (Math.abs(dx) > 0.3 || Math.abs(dz) > 0.3) {
               entity.teleport(pos.getX() + 0.5, entity.getY(), pos.getZ() + 0.5);
               entity.setVelocity(0.0, entity.getVelocity().y, 0.0);
               entity.velocityModified = true;
            }
         }
      }

      this.checkTimer++;
      if (this.checkTimer >= 20) {
         this.checkTimer = 0;
         this.checkBuriedEntityAndRestore(world, pos);
      }

      world.scheduleBlockTick(pos, this, 1);
   }

   private void checkBuriedEntityAndRestore(ServerWorld world, BlockPos pos) {
      Box entityBox = new Box(pos.add(-2, -2, -2), pos.add(2, 2, 2));
      List<Entity> entities = world.getEntitiesByClass(Entity.class, entityBox, e -> e instanceof MobEntity || e instanceof PlayerEntity);
      if (!entities.isEmpty()) {
         LuoQianGhostEntity luoQian = this.findNearbyLuoQian(world, pos);
         if (luoQian != null) {
            luoQian.onGraveRestore(pos);
         }
      }
   }

   private LuoQianGhostEntity findNearbyLuoQian(ServerWorld world, BlockPos pos) {
      Box searchBox = new Box(pos.add(-50, -20, -50), pos.add(50, 20, 50));
      List<LuoQianGhostEntity> luoQians = world.getEntitiesByClass(LuoQianGhostEntity.class, searchBox, e -> true);
      LuoQianGhostEntity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (LuoQianGhostEntity luoQian : luoQians) {
         double distance = pos.getSquaredDistance(luoQian.getPos());
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearest = luoQian;
         }
      }

      return nearest;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GRAVE_MOUND_BLOCK_ENTITY, pos, state);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }
}
