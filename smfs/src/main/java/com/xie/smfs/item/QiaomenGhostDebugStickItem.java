package com.xie.smfs.item;

import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class QiaomenGhostDebugStickItem extends Item {
   public QiaomenGhostDebugStickItem(Settings settings) {
      super(settings.method_7889(1));
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      if (world.field_9236) {
         return TypedActionResult.method_22430(player.method_5998(hand));
      }

      ItemStack stack = player.method_5998(hand);
      Box searchBox = new Box(player.method_24515()).method_1014(20.0);
      List<QiaomenGhostEntity> nearbyGhosts = world.method_8390(QiaomenGhostEntity.class, searchBox, ghostx -> true);
      if (nearbyGhosts.isEmpty()) {
         player.method_7353(Text.method_43470("§c附近没有找到敲门鬼实体"), false);
         return TypedActionResult.method_22431(stack);
      }

      for (int i = 0; i < nearbyGhosts.size(); i++) {
         QiaomenGhostEntity ghost = nearbyGhosts.get(i);
         String phaseInfo = this.getPhaseInfo(ghost);
         player.method_7353(Text.method_43470("§6敲门鬼 #" + (i + 1) + ": " + phaseInfo), false);
         player.method_7353(Text.method_43470("§7- 当前阶段: " + ghost.getCurrentPhase().name()), false);
         player.method_7353(Text.method_43470("§7- 阶段计时器: " + ghost.getPhaseTimer() + " ticks"), false);
         player.method_7353(Text.method_43470("§7- 行走状态: " + (ghost.isWalking() ? "§a是" : "§c否")), false);
         player.method_7353(
            Text.method_43470(
               "§7- 位置: " + ghost.method_24515().method_10263() + ", " + ghost.method_24515().method_10264() + ", " + ghost.method_24515().method_10260()
            ),
            false
         );
         if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.PREPARE_KNOCK) {
            player.method_7353(Text.method_43470("§a准备敲门阶段 (剩余: " + (600 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.KNOCKING) {
            player.method_7353(Text.method_43470("§e敲门阶段 (剩余: " + (20 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.PREPARE_ATTACK) {
            player.method_7353(Text.method_43470("§c准备攻击阶段 (剩余: " + (600 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.ATTACKING) {
            player.method_7353(Text.method_43470("§4攻击阶段 (剩余: " + (20 - ghost.getPhaseTimer()) + " ticks)"), false);
         }

         player.method_7353(Text.method_43470("§b- 连续空循环次数: " + ghost.getEmptyCycles()), false);
         player.method_7353(Text.method_43470("§b- 连续失败攻击次数: " + ghost.getFailedAttackCycles()), false);
         player.method_7353(Text.method_43470("§b- 当前伤害倍率: " + ghost.getAttackDamageMultiplier() + "x"), false);
         player.method_7353(Text.method_43470("§b- 当前鬼奴个数: " + ghost.getGhostSlaveCount()), false);
         player.method_7353(Text.method_43470(""), false);
      }

      return TypedActionResult.method_22427(stack);
   }

   private String getPhaseInfo(QiaomenGhostEntity ghost) {
      QiaomenGhostEntity.Phase currentPhase = ghost.getCurrentPhase();
      int phaseTimer = ghost.getPhaseTimer();
      switch (currentPhase) {
         case PREPARE_KNOCK:
            return "§a准备敲门阶段 §7(已进行: " + phaseTimer + "/600 ticks)";
         case KNOCKING:
            return "§e敲门阶段 §7(已进行: " + phaseTimer + "/20 ticks)";
         case PREPARE_ATTACK:
            return "§c准备攻击阶段 §7(已进行: " + phaseTimer + "/600 ticks)";
         case ATTACKING:
            return "§4攻击阶段 §7(已进行: " + phaseTimer + "/20 ticks)";
         default:
            return "§7未知阶段";
      }
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.method_43470("§6敲门鬼测试棒"));
      tooltip.add(Text.method_43470("§7右键点击查看附近敲门鬼的阶段状态"));
      tooltip.add(Text.method_43470("§7检测范围: 20格"));
      tooltip.add(Text.method_43470(""));
      tooltip.add(Text.method_43470("§a准备敲门阶段 §7- 600 ticks"));
      tooltip.add(Text.method_43470("§e敲门阶段 §7- 20 ticks"));
      tooltip.add(Text.method_43470("§c准备攻击阶段 §7- 600 ticks"));
      tooltip.add(Text.method_43470("§4攻击阶段 §7- 20 ticks"));
   }

   public Text method_7864(ItemStack stack) {
      return Text.method_43470("敲门鬼测试棒");
   }

   public Text method_7848() {
      return Text.method_43470("敲门鬼测试棒");
   }
}
