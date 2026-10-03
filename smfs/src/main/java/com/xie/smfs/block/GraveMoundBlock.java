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

   public void method_9615(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      super.method_9615(state, world, pos, oldState, notify);
      if (!world.method_8608()) {
         world.method_8396(null, pos, SoundEvents.field_14609, SoundCategory.field_15251, 1.0F, 0.8F);
         Box searchBox = new Box(pos).method_1014(1.0);

         for (LivingEntity entity : world.method_18467(LivingEntity.class, searchBox)) {
            if (!isImmuneToGraveMound(entity)) {
               entity.method_20620(pos.method_10263() + 0.5, pos.method_10264() + 0.1, pos.method_10260() + 0.5);
               entity.method_18800(0.0, 0.0, 0.0);
               entity.field_6037 = true;
               if (!entity.method_6059(ModEffects.SILENCE)) {
                  entity.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, false));
               }
            }
         }

         world.method_39279(pos, this, 1);
      }
   }

   public void method_9588(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      Box box = new Box(pos.method_10263(), pos.method_10264(), pos.method_10260(), pos.method_10263() + 1, pos.method_10264() + 1.5, pos.method_10260() + 1);

      for (LivingEntity entity : world.method_18467(LivingEntity.class, box)) {
         if (!isImmuneToGraveMound(entity)) {
            if (!entity.method_6059(ModEffects.SILENCE)) {
               entity.method_6092(new StatusEffectInstance(ModEffects.SILENCE, 40, 0, false, false, false));
            }

            double dx = pos.method_10263() + 0.5 - entity.method_23317();
            double dz = pos.method_10260() + 0.5 - entity.method_23321();
            if (Math.abs(dx) > 0.3 || Math.abs(dz) > 0.3) {
               entity.method_20620(pos.method_10263() + 0.5, entity.method_23318(), pos.method_10260() + 0.5);
               entity.method_18800(0.0, entity.method_18798().field_1351, 0.0);
               entity.field_6037 = true;
            }
         }
      }

      this.checkTimer++;
      if (this.checkTimer >= 20) {
         this.checkTimer = 0;
         this.checkBuriedEntityAndRestore(world, pos);
      }

      world.method_39279(pos, this, 1);
   }

   private void checkBuriedEntityAndRestore(ServerWorld world, BlockPos pos) {
      Box entityBox = new Box(pos.method_10069(-2, -2, -2), pos.method_10069(2, 2, 2));
      List<Entity> entities = world.method_8390(Entity.class, entityBox, e -> e instanceof MobEntity || e instanceof PlayerEntity);
      if (!entities.isEmpty()) {
         LuoQianGhostEntity luoQian = this.findNearbyLuoQian(world, pos);
         if (luoQian != null) {
            luoQian.onGraveRestore(pos);
         }
      }
   }

   private LuoQianGhostEntity findNearbyLuoQian(ServerWorld world, BlockPos pos) {
      Box searchBox = new Box(pos.method_10069(-50, -20, -50), pos.method_10069(50, 20, 50));
      List<LuoQianGhostEntity> luoQians = world.method_8390(LuoQianGhostEntity.class, searchBox, e -> true);
      LuoQianGhostEntity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (LuoQianGhostEntity luoQian : luoQians) {
         double distance = pos.method_19770(luoQian.method_19538());
         if (distance < nearestDistance) {
            nearestDistance = distance;
            nearest = luoQian;
         }
      }

      return nearest;
   }

   @Nullable
   public BlockEntity method_10123(BlockPos pos, BlockState state) {
      return new StaticAnimatable(ModBlockEntities.GRAVE_MOUND_BLOCK_ENTITY, pos, state);
   }

   public BlockRenderType method_9604(BlockState state) {
      return BlockRenderType.field_11456;
   }
}
