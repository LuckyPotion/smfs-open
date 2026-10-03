package com.xie.smfs.event;

import com.xie.smfs.block.NewGhostDoorBlock;
import com.xie.smfs.item.GoldenContainerItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.GhostSpawnManager;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoldenContainerPortalHandler implements UseBlockCallback {
   private static final Logger LOGGER = LoggerFactory.getLogger(GoldenContainerPortalHandler.class);

   public ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
      return onUseOnBlock(player, world, hand, hitResult);
   }

   public static ActionResult onUseOnBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
      if (world.field_9236) {
         return ActionResult.field_5811;
      } else {
         ItemStack stack = player.method_5998(hand);
         BlockPos blockPos = hitResult.method_17777();
         BlockState blockState = world.method_8320(blockPos);
         if (!isGhostDoor(blockState)) {
            return ActionResult.field_5811;
         } else if (!(stack.method_7909() instanceof GoldenContainerItem)) {
            return ActionResult.field_5811;
         } else if (!GoldenContainerItem.hasGhost(stack)) {
            player.method_7353(Text.method_43471("message.smfs.golden_container.portal.empty").method_27692(Formatting.field_1054), true);
            return ActionResult.field_5814;
         } else {
            String ghostType = GoldenContainerItem.getContainedGhostType(stack);
            if (ghostType == null) {
               LOGGER.warn("无法获取黄金容器中的鬼类型");
               player.method_7353(Text.method_43471("message.smfs.golden_container.portal.invalid").method_27692(Formatting.field_1061), true);
               return ActionResult.field_5814;
            } else {
               return injectGhostIntoPortal(player, world, stack, ghostType, blockPos);
            }
         }
      }
   }

   private static boolean isGhostDoor(BlockState blockState) {
      return blockState.method_26204() instanceof NewGhostDoorBlock;
   }

   private static ActionResult injectGhostIntoPortal(PlayerEntity player, World world, ItemStack containerStack, String ghostType, BlockPos portalPos) {
      NbtCompound ghostNbt = containerStack.method_7948().method_10562("ContainedGhost");
      if (ghostNbt.method_33133()) {
         LOGGER.error("黄金容器中的鬼NBT数据为空");
         player.method_7353(Text.method_43471("message.smfs.golden_container.portal.nbt_error").method_27692(Formatting.field_1061), true);
         return ActionResult.field_5814;
      }

      EntityType<?> ghostEntityType = getEntityTypeFromString(ghostType);
      if (ghostEntityType == null) {
         LOGGER.warn("无法将鬼类型字符串 {} 转换为EntityType", ghostType);
         player.method_7353(Text.method_43471("message.smfs.golden_container.portal.invalid_type").method_27692(Formatting.field_1061), true);
         return ActionResult.field_5814;
      }

      if (world instanceof ServerWorld serverWorld) {
         GhostSpawnManager.lockGhostType(serverWorld, ghostEntityType);
         clearGhostsInOverworld(serverWorld, ghostEntityType);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementManager.onGhostTypeLocked(serverPlayer, ghostEntityType);
         }
      }

      containerStack.method_7948().method_10551("ContainedGhost");
      containerStack.method_7948().method_10556("HasGhost", false);
      containerStack.method_7948().method_10551("IsHeavy");
      LOGGER.debug("玩家 {} 成功将鬼 {} 注入鬼门", player.method_7334().getName(), ghostType);
      playPortalEffects(player, world, portalPos);
      player.method_7353(Text.method_43471("message.smfs.golden_container.portal.success").method_27692(Formatting.field_1060), true);
      return ActionResult.field_5812;
   }

   private static void clearGhostsInOverworld(ServerWorld serverWorld, EntityType<?> ghostType) {
      ServerWorld overworld = serverWorld.method_8503().method_3847(World.field_25179);
      if (overworld == null) {
         LOGGER.warn("无法获取主世界，无法清除鬼实体");
      } else {
         List<Entity> entitiesToRemove = new ArrayList<>();

         for (Entity entity : overworld.method_27909()) {
            if (entity.method_5864() == ghostType) {
               entitiesToRemove.add(entity);
               LOGGER.debug(
                  "标记要清除的鬼实体: {} 位置: {},{},{}",
                  Registries.field_41177.method_10221(ghostType),
                  entity.method_31477(),
                  entity.method_31478(),
                  entity.method_31479()
               );
            }
         }

         for (Entity entity : entitiesToRemove) {
            entity.method_31472();
            LOGGER.debug(
               "已清除主世界中的鬼实体: {} 位置: {},{},{}",
               Registries.field_41177.method_10221(ghostType),
               entity.method_31477(),
               entity.method_31478(),
               entity.method_31479()
            );
         }

         LOGGER.debug("成功清除主世界中所有 {} 类型的鬼实体，共清除 {} 个", Registries.field_41177.method_10221(ghostType), entitiesToRemove.size());
      }
   }

   private static void playPortalEffects(PlayerEntity player, World world, BlockPos portalPos) {
      world.method_8396(null, portalPos, SoundEvents.field_14931, SoundCategory.field_15245, 1.0F, 1.0F);
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.method_14199(
            ParticleTypes.field_11214, portalPos.method_10263() + 0.5, portalPos.method_10264() + 1.0, portalPos.method_10260() + 0.5, 20, 1.0, 1.0, 1.0, 0.1
         );
         serverWorld.method_14199(
            ParticleTypes.field_23114, portalPos.method_10263() + 0.5, portalPos.method_10264() + 1.0, portalPos.method_10260() + 0.5, 10, 0.5, 0.5, 0.5, 0.05
         );
      }
   }

   private static EntityType<?> getEntityTypeFromString(String ghostType) {
      try {
         Identifier entityId;
         if (ghostType.contains(":")) {
            entityId = new Identifier(ghostType);
         } else {
            entityId = new Identifier("smfs", ghostType);
         }

         return (EntityType<?>)Registries.field_41177.method_10223(entityId);
      } catch (Exception e) {
         LOGGER.warn("无法将鬼类型字符串 {} 转换为EntityType: {}", ghostType, e.getMessage());
         return null;
      }
   }
}
