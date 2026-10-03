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
      if (world.isClient) {
         return ActionResult.PASS;
      } else {
         ItemStack stack = player.getStackInHand(hand);
         BlockPos blockPos = hitResult.getBlockPos();
         BlockState blockState = world.getBlockState(blockPos);
         if (!isGhostDoor(blockState)) {
            return ActionResult.PASS;
         } else if (!(stack.getItem() instanceof GoldenContainerItem)) {
            return ActionResult.PASS;
         } else if (!GoldenContainerItem.hasGhost(stack)) {
            player.sendMessage(Text.translatable("message.smfs.golden_container.portal.empty").formatted(Formatting.YELLOW), true);
            return ActionResult.FAIL;
         } else {
            String ghostType = GoldenContainerItem.getContainedGhostType(stack);
            if (ghostType == null) {
               LOGGER.warn("无法获取黄金容器中的鬼类型");
               player.sendMessage(Text.translatable("message.smfs.golden_container.portal.invalid").formatted(Formatting.RED), true);
               return ActionResult.FAIL;
            } else {
               return injectGhostIntoPortal(player, world, stack, ghostType, blockPos);
            }
         }
      }
   }

   private static boolean isGhostDoor(BlockState blockState) {
      return blockState.getBlock() instanceof NewGhostDoorBlock;
   }

   private static ActionResult injectGhostIntoPortal(PlayerEntity player, World world, ItemStack containerStack, String ghostType, BlockPos portalPos) {
      NbtCompound ghostNbt = containerStack.getOrCreateNbt().getCompound("ContainedGhost");
      if (ghostNbt.isEmpty()) {
         LOGGER.error("黄金容器中的鬼NBT数据为空");
         player.sendMessage(Text.translatable("message.smfs.golden_container.portal.nbt_error").formatted(Formatting.RED), true);
         return ActionResult.FAIL;
      }

      EntityType<?> ghostEntityType = getEntityTypeFromString(ghostType);
      if (ghostEntityType == null) {
         LOGGER.warn("无法将鬼类型字符串 {} 转换为EntityType", ghostType);
         player.sendMessage(Text.translatable("message.smfs.golden_container.portal.invalid_type").formatted(Formatting.RED), true);
         return ActionResult.FAIL;
      }

      if (world instanceof ServerWorld serverWorld) {
         GhostSpawnManager.lockGhostType(serverWorld, ghostEntityType);
         clearGhostsInOverworld(serverWorld, ghostEntityType);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementManager.onGhostTypeLocked(serverPlayer, ghostEntityType);
         }
      }

      containerStack.getOrCreateNbt().remove("ContainedGhost");
      containerStack.getOrCreateNbt().putBoolean("HasGhost", false);
      containerStack.getOrCreateNbt().remove("IsHeavy");
      LOGGER.debug("玩家 {} 成功将鬼 {} 注入鬼门", player.getGameProfile().getName(), ghostType);
      playPortalEffects(player, world, portalPos);
      player.sendMessage(Text.translatable("message.smfs.golden_container.portal.success").formatted(Formatting.GREEN), true);
      return ActionResult.SUCCESS;
   }

   private static void clearGhostsInOverworld(ServerWorld serverWorld, EntityType<?> ghostType) {
      ServerWorld overworld = serverWorld.getServer().getWorld(World.OVERWORLD);
      if (overworld == null) {
         LOGGER.warn("无法获取主世界，无法清除鬼实体");
      } else {
         List<Entity> entitiesToRemove = new ArrayList<>();

         for (Entity entity : overworld.iterateEntities()) {
            if (entity.getType() == ghostType) {
               entitiesToRemove.add(entity);
               LOGGER.debug("标记要清除的鬼实体: {} 位置: {},{},{}", Registries.ENTITY_TYPE.getId(ghostType), entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
            }
         }

         for (Entity entity : entitiesToRemove) {
            entity.discard();
            LOGGER.debug("已清除主世界中的鬼实体: {} 位置: {},{},{}", Registries.ENTITY_TYPE.getId(ghostType), entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
         }

         LOGGER.debug("成功清除主世界中所有 {} 类型的鬼实体，共清除 {} 个", Registries.ENTITY_TYPE.getId(ghostType), entitiesToRemove.size());
      }
   }

   private static void playPortalEffects(PlayerEntity player, World world, BlockPos portalPos) {
      world.playSound(null, portalPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
      if (world instanceof ServerWorld serverWorld) {
         serverWorld.spawnParticles(ParticleTypes.PORTAL, portalPos.getX() + 0.5, portalPos.getY() + 1.0, portalPos.getZ() + 0.5, 20, 1.0, 1.0, 1.0, 0.1);
         serverWorld.spawnParticles(ParticleTypes.SOUL, portalPos.getX() + 0.5, portalPos.getY() + 1.0, portalPos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.05);
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

         return (EntityType<?>)Registries.ENTITY_TYPE.get(entityId);
      } catch (Exception e) {
         LOGGER.warn("无法将鬼类型字符串 {} 转换为EntityType: {}", ghostType, e.getMessage());
         return null;
      }
   }
}
