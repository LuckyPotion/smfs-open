package com.xie.smfs.item;

import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.entity.ghost.GiantMaleCorpseGhostEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.event.GhostDeathHandler;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.manager.QuestManager;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoldenContainerItem extends Item {
   public static final String GHOST_NBT_KEY = "ContainedGhost";
   public static final String HAS_GHOST_NBT_KEY = "HasGhost";
   private static final Logger LOGGER = LoggerFactory.getLogger(GoldenContainerItem.class);

   public GoldenContainerItem(Settings settings) {
      super(settings);
   }

   public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      if (user.getWorld().isClient) {
         LOGGER.debug("客户端处理关押交互，直接返回成功");
         return ActionResult.SUCCESS;
      }

      LOGGER.debug("玩家 {} 尝试使用黄金容器关押实体: {}", user.getName().getString(), entity.getType().getTranslationKey());
      if (entity instanceof GhostEntity ghost && !hasGhost(stack)) {
         LOGGER.debug("目标为鬼实体，容器为空，检查状态条件");
         if (ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
            LOGGER.debug("目标为有主人的玩家鬼，无法关押");
            user.sendMessage(Text.literal("目标不支持关押！").formatted(Formatting.RED), true);
            return ActionResult.PASS;
         }

         if (ghost instanceof GhostChildEntity) {
            LOGGER.debug("目标为鬼童，无法关押");
            user.sendMessage(Text.literal("目标不支持关押！").formatted(Formatting.RED), true);
            return ActionResult.PASS;
         }

         if (ghost instanceof GiantMaleCorpseGhostEntity) {
            LOGGER.debug("目标为高大男尸，无法关押");
            user.sendMessage(Text.literal("无法关押目标！").formatted(Formatting.RED), true);
            return ActionResult.PASS;
         }

         if (ghost instanceof LuoQianGhostEntity) {
            LOGGER.debug("目标为罗千厉鬼，无法关押");
            user.sendMessage(Text.literal("无法关押目标！").formatted(Formatting.RED), true);
            return ActionResult.PASS;
         }

         boolean isSuppressed = ghost.isSuppressed() && ghost.hasCoffinNail();
         boolean isDeadlocked = ghost.isDeadlocked();
         boolean hasOnlySuppressStatus = ghost.isSuppressed() && !ghost.hasCoffinNail() && !isDeadlocked;
         LOGGER.debug("鬼状态 - 被压制: {}, 死机: {}, 只有压制状态: {}", isSuppressed, isDeadlocked, hasOnlySuppressStatus);
         if (isSuppressed || isDeadlocked) {
            LOGGER.debug("满足关押条件，开始关押鬼: {}", ghost.getType().getTranslationKey());
            if (ghost instanceof GhostOfficerEntity ghostOfficer) {
               LOGGER.debug("目标为鬼差，触发重启");
               user.sendMessage(Text.literal("关押成功？").formatted(Formatting.RED), true);
               ghostOfficer.restartGhost();
               return ActionResult.PASS;
            }

            user.sendMessage(Text.literal("关押成功！").formatted(Formatting.GREEN), true);
            this.captureGhost(stack, ghost, user);
            return ActionResult.SUCCESS;
         }

         if (hasOnlySuppressStatus) {
            LOGGER.debug("鬼只有压制状态，无法关押");
            user.sendMessage(Text.literal("目标剧烈挣扎！").formatted(Formatting.RED), true);
         } else {
            LOGGER.debug("鬼不满足关押条件，被压制状态: {}, 棺材钉: {}, 死机状态: {}", ghost.isSuppressed(), ghost.hasCoffinNail(), ghost.isDeadlocked());
         }
      } else if (entity instanceof GhostEntity) {
         LOGGER.debug("目标为鬼实体，但容器已有关押的鬼");
      } else {
         LOGGER.debug("目标不是鬼实体，类型: {}", entity.getType().getTranslationKey());
      }

      return ActionResult.PASS;
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (world.isClient) {
         LOGGER.debug("客户端处理黄金容器使用，直接返回成功");
         return TypedActionResult.success(stack);
      } else {
         LOGGER.debug("玩家 {} 使用黄金容器，手: {}", user.getName().getString(), hand.name());
         if (hasGhost(stack)) {
            LOGGER.debug("黄金容器有关押的鬼，开始释放");
            this.releaseGhost(stack, user, world);
            return TypedActionResult.success(stack);
         } else {
            LOGGER.debug("黄金容器为空，无鬼可释放");
            return TypedActionResult.pass(stack);
         }
      }
   }

   public static boolean hasGhost(ItemStack stack) {
      return stack.getOrCreateNbt().getBoolean("HasGhost");
   }

   @Nullable
   public static String getContainedGhostType(ItemStack stack) {
      if (!hasGhost(stack)) {
         return null;
      }

      NbtCompound ghostNbt = stack.getOrCreateNbt().getCompound("ContainedGhost");
      return ghostNbt.isEmpty() ? null : ghostNbt.getString("id");
   }

   public String getTranslationKey(ItemStack stack) {
      return hasGhost(stack) ? "item.smfs.golden_container.full" : "item.smfs.golden_container.empty";
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      if (hasGhost(stack)) {
         tooltip.add(Text.translatable("item.smfs.golden_container.full.description.source"));
         tooltip.add(Text.translatable("item.smfs.golden_container.full.description.desc"));
         tooltip.add(Text.translatable("item.smfs.golden_container.full.description.type"));
      } else {
         tooltip.add(Text.translatable("item.smfs.golden_container.description.source"));
         tooltip.add(Text.translatable("item.smfs.golden_container.description.desc"));
         tooltip.add(Text.translatable("item.smfs.golden_container.description.type"));
      }
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (entity instanceof PlayerEntity player && !world.isClient && hasGhost(stack) && selected) {
         player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 0, 0, false, false));
      }
   }

   private void captureGhost(ItemStack stack, GhostEntity ghost, PlayerEntity player) {
      if (hasGhost(stack)) {
         LOGGER.warn("尝试关押鬼但容器已满，跳过处理");
      } else {
         LOGGER.debug("开始关押鬼: {}，玩家: {}", ghost.getType().getTranslationKey(), player.getName().getString());
         World world = player.getWorld();
         NbtCompound ghostNbt = new NbtCompound();
         ghost.writeNbt(ghostNbt);
         ItemStack coffinNail = ghost.getCoffinNail();
         if (coffinNail != null) {
            ghostNbt.put("CoffinNail", coffinNail.writeNbt(new NbtCompound()));
         }

         ghostNbt.putBoolean("Deadlocked", ghost.isDeadlocked());
         LOGGER.debug("保存鬼NBT数据完成，鬼类型: {}", ghost.getType().getTranslationKey());
         if (ghost instanceof PlayerGhostEntity playerGhost) {
            LOGGER.debug("检测到玩家鬼，开始保存玩家生前的驾驭信息和槽位信息");
            String playerUuid = playerGhost.getPlayerUuid();
            String playerName = playerGhost.getPlayerName();
            ghostNbt.putString("PlayerUuid", playerUuid != null ? playerUuid : "");
            ghostNbt.putString("PlayerName", playerName != null ? playerName : "王家厉鬼");
            NbtCompound playerGhostsNbt = new NbtCompound();
            List<String> playerGhosts = playerGhost.getPlayerGhosts();

            for (int i = 0; i < playerGhosts.size(); i++) {
               playerGhostsNbt.putString("Ghost" + i, playerGhosts.get(i));
            }

            ghostNbt.put("PlayerGhosts", playerGhostsNbt);
            String ghostDomainColor = playerGhost.getGhostDomainColor();
            ghostNbt.putString("GhostDomainColor", ghostDomainColor != null ? ghostDomainColor : "none");
            NbtCompound killingRulesNbt = new NbtCompound();
            List<String> killingRules = playerGhost.getKillingRules();

            for (int i = 0; i < killingRules.size(); i++) {
               killingRulesNbt.putString("Rule" + i, killingRules.get(i));
            }

            ghostNbt.put("KillingRules", killingRulesNbt);
            NbtCompound ghostSlotsData = playerGhost.getGhostSlotsData();
            if (!ghostSlotsData.isEmpty()) {
               ghostNbt.put("GhostSlotsData", ghostSlotsData.copy());
               LOGGER.debug("成功保存玩家 {} 生前的槽位信息", playerGhost.getPlayerName());
            }

            LOGGER.debug("玩家鬼 {} 的驾驭信息和槽位信息已保存到黄金容器", playerGhost.getPlayerName());
         }

         ghostNbt.remove("UUID");
         ghostNbt.remove("UUIDMost");
         ghostNbt.remove("UUIDLeast");
         ghostNbt.putString("id", EntityType.getId(ghost.getType()).toString());
         ghostNbt.putDouble("OriginalX", ghost.getX());
         ghostNbt.putDouble("OriginalY", ghost.getY());
         ghostNbt.putDouble("OriginalZ", ghost.getZ());
         ghostNbt.putString("OriginalWorld", world.getRegistryKey().getValue().toString());
         LOGGER.debug("保存鬼位置信息: ({}, {}, {})，世界: {}", ghost.getX(), ghost.getY(), ghost.getZ(), world.getRegistryKey().getValue().toString());
         stack.getOrCreateNbt().put("ContainedGhost", ghostNbt);
         stack.getOrCreateNbt().putBoolean("HasGhost", true);
         stack.getOrCreateNbt().putBoolean("IsHeavy", true);
         LOGGER.debug("成功将鬼数据保存到黄金容器NBT");
         GhostDeathHandler.markLegitimateRemoval(ghost);
         ghost.discard();
         LOGGER.debug("原鬼实体已移除");
         world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.PLAYERS, 0.5F, 1.0F);
         LOGGER.debug("更新关押鬼任务进度，任务ID: main_capture_ghost，目标ID: capture_ghost");
         QuestManager.updateQuestProgress(player, "main_capture_ghost", "capture_ghost", 1);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementManager.checkAndUnlockGhostHunter(serverPlayer);
         }

         FactionManager.addReputation(player, 100);
         FactionManager.onPlayerTamedFirstGhost(player);
         LOGGER.debug("关押鬼任务完成，玩家: {}，鬼类型: {}", player.getName().getString(), ghost.getType().getTranslationKey());
      }
   }

   private void releaseGhost(ItemStack stack, PlayerEntity player, World world) {
      LOGGER.debug("玩家 {} 开始释放黄金容器中的鬼", player.getName().getString());
      NbtCompound ghostNbt = stack.getOrCreateNbt().getCompound("ContainedGhost");
      if (ghostNbt.isEmpty()) {
         LOGGER.warn("黄金容器NBT数据为空，无法释放鬼");
      } else {
         LOGGER.debug("读取鬼NBT数据成功，鬼类型: {}", ghostNbt.getString("id"));
         Entity entity = EntityType.loadEntityWithPassengers(ghostNbt, world, loadedEntity -> {
            loadedEntity.setPos(player.getX(), player.getY(), player.getZ());
            LOGGER.debug("设置鬼生成位置: ({}, {}, {})", player.getX(), player.getY(), player.getZ());
            return loadedEntity;
         });
         if (entity != null) {
            LOGGER.debug("成功创建鬼实体，开始生成到世界");
            world.spawnEntity(entity);
            LOGGER.debug("鬼实体已生成到世界");
            if (entity instanceof GhostEntity releasedGhost) {
               LOGGER.debug("释放的实体为GhostEntity类型，开始恢复状态");
               releasedGhost.readNbt(ghostNbt);
               releasedGhost.setPos(player.getX(), player.getY(), player.getZ());
               releasedGhost.setNoGravity(false);
               releasedGhost.setInvulnerable(false);
               releasedGhost.setGlowing(false);
               releasedGhost.setInvisible(false);
               LOGGER.debug("重置鬼实体状态完成");
               if (!world.isClient) {
                  releasedGhost.setPosition(player.getX(), player.getY(), player.getZ());
                  releasedGhost.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), releasedGhost.getYaw(), releasedGhost.getPitch());
                  LOGGER.debug("服务端位置刷新完成");
               }

               releasedGhost.disableGhostDomain();
               releasedGhost.disableKillingRules();
               LOGGER.debug("禁用鬼域和杀人规则");
               if (ghostNbt.contains("CoffinNail")) {
                  ItemStack coffinNail = ItemStack.fromNbt(ghostNbt.getCompound("CoffinNail"));
                  releasedGhost.setCoffinNail(coffinNail);
                  if (!coffinNail.isEmpty()) {
                     releasedGhost.setSuppressed(true);
                     LOGGER.debug("恢复棺材钉压制状态");
                  }
               }

               if (ghostNbt.contains("Deadlocked") && ghostNbt.getBoolean("Deadlocked")) {
                  releasedGhost.setDeadlocked(true);
                  LOGGER.debug("保持死机状态");
               }

               releasedGhost.teleport(player.getX(), player.getY(), player.getZ());
               LOGGER.debug("强制更新实体追踪状态");
               if (releasedGhost instanceof PlayerGhostEntity playerGhost && ghostNbt.contains("PlayerUuid")) {
                  LOGGER.debug("检测到玩家鬼，开始恢复玩家生前的驾驭信息和槽位信息");
                  playerGhost.readCustomDataFromNbt(ghostNbt);
                  LOGGER.debug("玩家鬼 {} 的驾驭信息和槽位信息已从黄金容器恢复", playerGhost.getPlayerName());
               }

               if (!world.isClient) {
                  LOGGER.debug("鬼释放完成，位置: ({}, {}, {})", player.getX(), player.getY(), player.getZ());
               }
            } else {
               LOGGER.warn("释放的实体不是GhostEntity类型，实际类型: {}", entity.getType().getTranslationKey());
            }
         } else {
            LOGGER.error("创建鬼实体失败，NBT数据可能损坏");
         }

         stack.getOrCreateNbt().remove("ContainedGhost");
         stack.getOrCreateNbt().putBoolean("HasGhost", false);
         stack.getOrCreateNbt().remove("IsHeavy");
         LOGGER.debug("黄金容器已清空，恢复为空容器状态");
         world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.PLAYERS, 0.5F, 1.0F);
         LOGGER.debug("鬼释放操作完成，玩家: {}", player.getName().getString());
      }
   }
}
