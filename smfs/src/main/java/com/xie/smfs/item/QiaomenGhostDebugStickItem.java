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
      super(settings.maxCount(1));
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      if (world.isClient) {
         return TypedActionResult.pass(user.getStackInHand(hand));
      }

      ItemStack stack = user.getStackInHand(hand);
      Box searchBox = new Box(user.getBlockPos()).expand(20.0);
      List<QiaomenGhostEntity> nearbyGhosts = world.getEntitiesByClass(QiaomenGhostEntity.class, searchBox, ghostx -> true);
      if (nearbyGhosts.isEmpty()) {
         user.sendMessage(Text.literal("§c附近没有找到敲门鬼实体"), false);
         return TypedActionResult.fail(stack);
      }

      for (int i = 0; i < nearbyGhosts.size(); i++) {
         QiaomenGhostEntity ghost = nearbyGhosts.get(i);
         String phaseInfo = this.getPhaseInfo(ghost);
         user.sendMessage(Text.literal("§6敲门鬼 #" + (i + 1) + ": " + phaseInfo), false);
         user.sendMessage(Text.literal("§7- 当前阶段: " + ghost.getCurrentPhase().name()), false);
         user.sendMessage(Text.literal("§7- 阶段计时器: " + ghost.getPhaseTimer() + " ticks"), false);
         user.sendMessage(Text.literal("§7- 行走状态: " + (ghost.isWalking() ? "§a是" : "§c否")), false);
         user.sendMessage(Text.literal("§7- 位置: " + ghost.getBlockPos().getX() + ", " + ghost.getBlockPos().getY() + ", " + ghost.getBlockPos().getZ()), false);
         if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.PREPARE_KNOCK) {
            user.sendMessage(Text.literal("§a准备敲门阶段 (剩余: " + (600 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.KNOCKING) {
            user.sendMessage(Text.literal("§e敲门阶段 (剩余: " + (20 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.PREPARE_ATTACK) {
            user.sendMessage(Text.literal("§c准备攻击阶段 (剩余: " + (600 - ghost.getPhaseTimer()) + " ticks)"), false);
         } else if (ghost.getCurrentPhase() == QiaomenGhostEntity.Phase.ATTACKING) {
            user.sendMessage(Text.literal("§4攻击阶段 (剩余: " + (20 - ghost.getPhaseTimer()) + " ticks)"), false);
         }

         user.sendMessage(Text.literal("§b- 连续空循环次数: " + ghost.getEmptyCycles()), false);
         user.sendMessage(Text.literal("§b- 连续失败攻击次数: " + ghost.getFailedAttackCycles()), false);
         user.sendMessage(Text.literal("§b- 当前伤害倍率: " + ghost.getAttackDamageMultiplier() + "x"), false);
         user.sendMessage(Text.literal("§b- 当前鬼奴个数: " + ghost.getGhostSlaveCount()), false);
         user.sendMessage(Text.literal(""), false);
      }

      return TypedActionResult.success(stack);
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

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add(Text.literal("§6敲门鬼测试棒"));
      tooltip.add(Text.literal("§7右键点击查看附近敲门鬼的阶段状态"));
      tooltip.add(Text.literal("§7检测范围: 20格"));
      tooltip.add(Text.literal(""));
      tooltip.add(Text.literal("§a准备敲门阶段 §7- 600 ticks"));
      tooltip.add(Text.literal("§e敲门阶段 §7- 20 ticks"));
      tooltip.add(Text.literal("§c准备攻击阶段 §7- 600 ticks"));
      tooltip.add(Text.literal("§4攻击阶段 §7- 20 ticks"));
   }

   public Text getName(ItemStack stack) {
      return Text.literal("敲门鬼测试棒");
   }

   public Text getName() {
      return Text.literal("敲门鬼测试棒");
   }
}
