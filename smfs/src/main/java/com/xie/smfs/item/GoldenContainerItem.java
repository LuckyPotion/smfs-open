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

   public ActionResult method_7847(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      if (user.method_37908().field_9236) {
         LOGGER.debug("客户端处理关押交互，直接返回成功");
         return ActionResult.field_5812;
      }

      LOGGER.debug("玩家 {} 尝试使用黄金容器关押实体: {}", user.method_5477().getString(), entity.method_5864().method_5882());
      if (entity instanceof GhostEntity ghost && !hasGhost(stack)) {
         LOGGER.debug("目标为鬼实体，容器为空，检查状态条件");
         if (ghost instanceof PlayerGhostEntity playerGhost && playerGhost.isServantMode() && playerGhost.getMasterUuid() != null) {
            LOGGER.debug("目标为有主人的玩家鬼，无法关押");
            user.method_7353(Text.method_43470("目标不支持关押！").method_27692(Formatting.field_1061), true);
            return ActionResult.field_5811;
         }

         if (ghost instanceof GhostChildEntity) {
            LOGGER.debug("目标为鬼童，无法关押");
            user.method_7353(Text.method_43470("目标不支持关押！").method_27692(Formatting.field_1061), true);
            return ActionResult.field_5811;
         }

         if (ghost instanceof GiantMaleCorpseGhostEntity) {
            LOGGER.debug("目标为高大男尸，无法关押");
            user.method_7353(Text.method_43470("无法关押目标！").method_27692(Formatting.field_1061), true);
            return ActionResult.field_5811;
         }

         if (ghost instanceof LuoQianGhostEntity) {
            LOGGER.debug("目标为罗千厉鬼，无法关押");
            user.method_7353(Text.method_43470("无法关押目标！").method_27692(Formatting.field_1061), true);
            return ActionResult.field_5811;
         }

         boolean isSuppressed = ghost.isSuppressed() && ghost.hasCoffinNail();
         boolean isDeadlocked = ghost.isDeadlocked();
         boolean hasOnlySuppressStatus = ghost.isSuppressed() && !ghost.hasCoffinNail() && !isDeadlocked;
         LOGGER.debug("鬼状态 - 被压制: {}, 死机: {}, 只有压制状态: {}", isSuppressed, isDeadlocked, hasOnlySuppressStatus);
         if (isSuppressed || isDeadlocked) {
            LOGGER.debug("满足关押条件，开始关押鬼: {}", ghost.method_5864().method_5882());
            if (ghost instanceof GhostOfficerEntity ghostOfficer) {
               LOGGER.debug("目标为鬼差，触发重启");
               user.method_7353(Text.method_43470("关押成功？").method_27692(Formatting.field_1061), true);
               ghostOfficer.restartGhost();
               return ActionResult.field_5811;
            }

            user.method_7353(Text.method_43470("关押成功！").method_27692(Formatting.field_1060), true);
            this.captureGhost(stack, ghost, user);
            return ActionResult.field_5812;
         }

         if (hasOnlySuppressStatus) {
            LOGGER.debug("鬼只有压制状态，无法关押");
            user.method_7353(Text.method_43470("目标剧烈挣扎！").method_27692(Formatting.field_1061), true);
         } else {
            LOGGER.debug("鬼不满足关押条件，被压制状态: {}, 棺材钉: {}, 死机状态: {}", ghost.isSuppressed(), ghost.hasCoffinNail(), ghost.isDeadlocked());
         }
      } else if (entity instanceof GhostEntity) {
         LOGGER.debug("目标为鬼实体，但容器已有关押的鬼");
      } else {
         LOGGER.debug("目标不是鬼实体，类型: {}", entity.method_5864().method_5882());
      }

      return ActionResult.field_5811;
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (world.field_9236) {
         LOGGER.debug("客户端处理黄金容器使用，直接返回成功");
         return TypedActionResult.method_22427(stack);
      } else {
         LOGGER.debug("玩家 {} 使用黄金容器，手: {}", user.method_5477().getString(), hand.name());
         if (hasGhost(stack)) {
            LOGGER.debug("黄金容器有关押的鬼，开始释放");
            this.releaseGhost(stack, user, world);
            return TypedActionResult.method_22427(stack);
         } else {
            LOGGER.debug("黄金容器为空，无鬼可释放");
            return TypedActionResult.method_22430(stack);
         }
      }
   }

   public static boolean hasGhost(ItemStack stack) {
      return stack.method_7948().method_10577("HasGhost");
   }

   @Nullable
   public static String getContainedGhostType(ItemStack stack) {
      if (!hasGhost(stack)) {
         return null;
      }

      NbtCompound ghostNbt = stack.method_7948().method_10562("ContainedGhost");
      return ghostNbt.method_33133() ? null : ghostNbt.method_10558("id");
   }

   public String method_7866(ItemStack stack) {
      return hasGhost(stack) ? "item.smfs.golden_container.full" : "item.smfs.golden_container.empty";
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      if (hasGhost(stack)) {
         tooltip.add(Text.method_43471("item.smfs.golden_container.full.description.source"));
         tooltip.add(Text.method_43471("item.smfs.golden_container.full.description.desc"));
         tooltip.add(Text.method_43471("item.smfs.golden_container.full.description.type"));
      } else {
         tooltip.add(Text.method_43471("item.smfs.golden_container.description.source"));
         tooltip.add(Text.method_43471("item.smfs.golden_container.description.desc"));
         tooltip.add(Text.method_43471("item.smfs.golden_container.description.type"));
      }
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (entity instanceof PlayerEntity player && !world.field_9236 && hasGhost(stack) && selected) {
         player.method_6092(new StatusEffectInstance(StatusEffects.field_5909, 0, 0, false, false));
      }
   }

   private void captureGhost(ItemStack stack, GhostEntity ghost, PlayerEntity player) {
      if (hasGhost(stack)) {
         LOGGER.warn("尝试关押鬼但容器已满，跳过处理");
      } else {
         LOGGER.debug("开始关押鬼: {}，玩家: {}", ghost.method_5864().method_5882(), player.method_5477().getString());
         World world = player.method_37908();
         NbtCompound ghostNbt = new NbtCompound();
         ghost.method_5647(ghostNbt);
         ItemStack coffinNail = ghost.getCoffinNail();
         if (coffinNail != null) {
            ghostNbt.method_10566("CoffinNail", coffinNail.method_7953(new NbtCompound()));
         }

         ghostNbt.method_10556("Deadlocked", ghost.isDeadlocked());
         LOGGER.debug("保存鬼NBT数据完成，鬼类型: {}", ghost.method_5864().method_5882());
         if (ghost instanceof PlayerGhostEntity playerGhost) {
            LOGGER.debug("检测到玩家鬼，开始保存玩家生前的驾驭信息和槽位信息");
            String playerUuid = playerGhost.getPlayerUuid();
            String playerName = playerGhost.getPlayerName();
            ghostNbt.method_10582("PlayerUuid", playerUuid != null ? playerUuid : "");
            ghostNbt.method_10582("PlayerName", playerName != null ? playerName : "王家厉鬼");
            NbtCompound playerGhostsNbt = new NbtCompound();
            List<String> playerGhosts = playerGhost.getPlayerGhosts();

            for (int i = 0; i < playerGhosts.size(); i++) {
               playerGhostsNbt.method_10582("Ghost" + i, playerGhosts.get(i));
            }

            ghostNbt.method_10566("PlayerGhosts", playerGhostsNbt);
            String ghostDomainColor = playerGhost.getGhostDomainColor();
            ghostNbt.method_10582("GhostDomainColor", ghostDomainColor != null ? ghostDomainColor : "none");
            NbtCompound killingRulesNbt = new NbtCompound();
            List<String> killingRules = playerGhost.getKillingRules();

            for (int i = 0; i < killingRules.size(); i++) {
               killingRulesNbt.method_10582("Rule" + i, killingRules.get(i));
            }

            ghostNbt.method_10566("KillingRules", killingRulesNbt);
            NbtCompound ghostSlotsData = playerGhost.getGhostSlotsData();
            if (!ghostSlotsData.method_33133()) {
               ghostNbt.method_10566("GhostSlotsData", ghostSlotsData.method_10553());
               LOGGER.debug("成功保存玩家 {} 生前的槽位信息", playerGhost.getPlayerName());
            }

            LOGGER.debug("玩家鬼 {} 的驾驭信息和槽位信息已保存到黄金容器", playerGhost.getPlayerName());
         }

         ghostNbt.method_10551("UUID");
         ghostNbt.method_10551("UUIDMost");
         ghostNbt.method_10551("UUIDLeast");
         ghostNbt.method_10582("id", EntityType.method_5890(ghost.method_5864()).toString());
         ghostNbt.method_10549("OriginalX", ghost.method_23317());
         ghostNbt.method_10549("OriginalY", ghost.method_23318());
         ghostNbt.method_10549("OriginalZ", ghost.method_23321());
         ghostNbt.method_10582("OriginalWorld", world.method_27983().method_29177().toString());
         LOGGER.debug(
            "保存鬼位置信息: ({}, {}, {})，世界: {}", ghost.method_23317(), ghost.method_23318(), ghost.method_23321(), world.method_27983().method_29177().toString()
         );
         stack.method_7948().method_10566("ContainedGhost", ghostNbt);
         stack.method_7948().method_10556("HasGhost", true);
         stack.method_7948().method_10556("IsHeavy", true);
         LOGGER.debug("成功将鬼数据保存到黄金容器NBT");
         GhostDeathHandler.markLegitimateRemoval(ghost);
         ghost.method_31472();
         LOGGER.debug("原鬼实体已移除");
         world.method_43128(
            null, player.method_23317(), player.method_23318(), player.method_23321(), SoundEvents.field_14823, SoundCategory.field_15248, 0.5F, 1.0F
         );
         LOGGER.debug("更新关押鬼任务进度，任务ID: main_capture_ghost，目标ID: capture_ghost");
         QuestManager.updateQuestProgress(player, "main_capture_ghost", "capture_ghost", 1);
         if (player instanceof ServerPlayerEntity serverPlayer) {
            AdvancementManager.checkAndUnlockGhostHunter(serverPlayer);
         }

         FactionManager.addReputation(player, 100);
         FactionManager.onPlayerTamedFirstGhost(player);
         LOGGER.debug("关押鬼任务完成，玩家: {}，鬼类型: {}", player.method_5477().getString(), ghost.method_5864().method_5882());
      }
   }

   private void releaseGhost(ItemStack stack, PlayerEntity player, World world) {
      LOGGER.debug("玩家 {} 开始释放黄金容器中的鬼", player.method_5477().getString());
      NbtCompound ghostNbt = stack.method_7948().method_10562("ContainedGhost");
      if (ghostNbt.method_33133()) {
         LOGGER.warn("黄金容器NBT数据为空，无法释放鬼");
      } else {
         LOGGER.debug("读取鬼NBT数据成功，鬼类型: {}", ghostNbt.method_10558("id"));
         Entity entity = EntityType.method_17842(ghostNbt, world, loadedEntity -> {
            loadedEntity.method_23327(player.method_23317(), player.method_23318(), player.method_23321());
            LOGGER.debug("设置鬼生成位置: ({}, {}, {})", player.method_23317(), player.method_23318(), player.method_23321());
            return loadedEntity;
         });
         if (entity != null) {
            LOGGER.debug("成功创建鬼实体，开始生成到世界");
            world.method_8649(entity);
            LOGGER.debug("鬼实体已生成到世界");
            if (entity instanceof GhostEntity releasedGhost) {
               LOGGER.debug("释放的实体为GhostEntity类型，开始恢复状态");
               releasedGhost.method_5651(ghostNbt);
               releasedGhost.method_23327(player.method_23317(), player.method_23318(), player.method_23321());
               releasedGhost.method_5875(false);
               releasedGhost.method_5684(false);
               releasedGhost.method_5834(false);
               releasedGhost.method_5648(false);
               LOGGER.debug("重置鬼实体状态完成");
               if (!world.field_9236) {
                  releasedGhost.method_5814(player.method_23317(), player.method_23318(), player.method_23321());
                  releasedGhost.method_5808(
                     player.method_23317(), player.method_23318(), player.method_23321(), releasedGhost.method_36454(), releasedGhost.method_36455()
                  );
                  LOGGER.debug("服务端位置刷新完成");
               }

               releasedGhost.disableGhostDomain();
               releasedGhost.disableKillingRules();
               LOGGER.debug("禁用鬼域和杀人规则");
               if (ghostNbt.method_10545("CoffinNail")) {
                  ItemStack coffinNail = ItemStack.method_7915(ghostNbt.method_10562("CoffinNail"));
                  releasedGhost.setCoffinNail(coffinNail);
                  if (!coffinNail.method_7960()) {
                     releasedGhost.setSuppressed(true);
                     LOGGER.debug("恢复棺材钉压制状态");
                  }
               }

               if (ghostNbt.method_10545("Deadlocked") && ghostNbt.method_10577("Deadlocked")) {
                  releasedGhost.setDeadlocked(true);
                  LOGGER.debug("保持死机状态");
               }

               releasedGhost.method_20620(player.method_23317(), player.method_23318(), player.method_23321());
               LOGGER.debug("强制更新实体追踪状态");
               if (releasedGhost instanceof PlayerGhostEntity playerGhost && ghostNbt.method_10545("PlayerUuid")) {
                  LOGGER.debug("检测到玩家鬼，开始恢复玩家生前的驾驭信息和槽位信息");
                  playerGhost.method_5749(ghostNbt);
                  LOGGER.debug("玩家鬼 {} 的驾驭信息和槽位信息已从黄金容器恢复", playerGhost.getPlayerName());
               }

               if (!world.field_9236) {
                  LOGGER.debug("鬼释放完成，位置: ({}, {}, {})", player.method_23317(), player.method_23318(), player.method_23321());
               }
            } else {
               LOGGER.warn("释放的实体不是GhostEntity类型，实际类型: {}", entity.method_5864().method_5882());
            }
         } else {
            LOGGER.error("创建鬼实体失败，NBT数据可能损坏");
         }

         stack.method_7948().method_10551("ContainedGhost");
         stack.method_7948().method_10556("HasGhost", false);
         stack.method_7948().method_10551("IsHeavy");
         LOGGER.debug("黄金容器已清空，恢复为空容器状态");
         world.method_43128(
            null, player.method_23317(), player.method_23318(), player.method_23321(), SoundEvents.field_14982, SoundCategory.field_15248, 0.5F, 1.0F
         );
         LOGGER.debug("鬼释放操作完成，玩家: {}", player.method_5477().getString());
      }
   }
}
