package com.xie.smfs.item;

import com.xie.smfs.entity.GhostEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class GhostDebugStickItem extends Item {
   public GhostDebugStickItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (!world.field_9236) {
         if (this.findGhostTarget(user) instanceof GhostEntity ghost) {
            this.displayGhostStatusInfo(user, ghost);
            return TypedActionResult.method_22427(itemStack);
         }

         user.method_43496(Text.method_43470("§c未找到附近的鬼实体！"));
      }

      return TypedActionResult.method_22430(itemStack);
   }

   private LivingEntity findGhostTarget(PlayerEntity player) {
      return (LivingEntity)player.method_37908()
         .method_8390(GhostEntity.class, player.method_5829().method_1014(10.0), ghost -> ghost.method_5805() && player.method_6057(ghost))
         .stream()
         .findFirst()
         .orElse(null);
   }

   private void displayGhostStatusInfo(PlayerEntity player, GhostEntity ghost) {
      player.method_43496(Text.method_43470("§6=== 鬼生物调试信息 ==="));
      player.method_43496(Text.method_43470("§a鬼名称: §f" + ghost.method_5477().getString()));
      player.method_43496(Text.method_43470("§a实体类型: §f" + ghost.method_5864().toString()));
      player.method_43496(Text.method_43470("§a位置: §f" + String.format("%.1f, %.1f, %.1f", ghost.method_23317(), ghost.method_23318(), ghost.method_23321())));
      player.method_43496(Text.method_43470("§6--- 生命状态 ---"));
      player.method_43496(Text.method_43470("§a生命值: §f" + String.format("%.1f/%.1f", ghost.method_6032(), ghost.method_6063())));
      player.method_43496(Text.method_43470("§a是否存活: §f" + (ghost.method_5805() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§6--- API支持状态 ---"));
      player.method_43496(Text.method_43470("§a自定义攻击逻辑: §f" + (ghost.getCustomAttackLogic() != null ? "§a已设置" : "§c未设置")));
      player.method_43496(Text.method_43470("§a自定义目标选择器: §f" + (ghost.getCustomTargetSelector() != null ? "§a已设置" : "§c未设置")));
      player.method_43496(Text.method_43470("§a状态监听器数量: §f" + (ghost.getStateListeners() != null ? ghost.getStateListeners().size() : 0)));
      player.method_43496(Text.method_43470("§a自定义NBT序列化器: §f" + (ghost.getCustomNbtSerializers() != null ? ghost.getCustomNbtSerializers().size() : 0)));
      player.method_43496(Text.method_43470("§a自定义NBT反序列化器: §f" + (ghost.getCustomNbtDeserializers() != null ? ghost.getCustomNbtDeserializers().size() : 0)));
      player.method_43496(Text.method_43470("§6--- 灵异属性 ---"));
      player.method_43496(Text.method_43470("§a灵异强度: §f" + ghost.getSpiritualStrength() + "/" + ghost.getMaxSpiritualStrength()));
      player.method_43496(Text.method_43470("§a灵异伤害: §f" + ghost.getSpiritualDamage()));
      player.method_43496(Text.method_43470("§a灵异抗性: §f" + ghost.getSpiritualResistance()));
      player.method_43496(Text.method_43470("§a恢复因子: §f" + String.format("%.2f", ghost.getRecoveryFactor())));
      player.method_43496(Text.method_43470("§6--- 怨气值系统 ---"));
      player.method_43496(Text.method_43470("§a当前怨气值: §f" + ghost.getResentmentValue() + "/100"));
      player.method_43496(Text.method_43470("§a怨气阈值: §f" + (ghost.isResentmentThresholdReached() ? "§c已达到" : "§a未达到")));
      player.method_43496(Text.method_43470("§a怨气积累范围: §f" + String.format("%.1f格", ghost.getGhostDomainRadius() > 0.0F ? ghost.getGhostDomainRadius() : 48.0)));
      player.method_43496(Text.method_43470("§6--- 鬼域状态 ---"));
      player.method_43496(Text.method_43470("§a鬼域等级: §f" + ghost.getGhostDomainLevel()));
      player.method_43496(Text.method_43470("§a鬼域半径: §f" + String.format("%.1f", ghost.getGhostDomainRadius())));
      player.method_43496(Text.method_43470("§a拥有鬼域: §f" + (ghost.hasGhostDomain() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§a鬼域启用: §f" + (ghost.isGhostDomainEnabled() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§a恐怖等级: §f" + ghost.getTerrorLevel()));
      player.method_43496(Text.method_43470("§6--- 控制状态 ---"));
      player.method_43496(Text.method_43470("§a被压制: §f" + (ghost.isSuppressed() ? "§c是" : "§a否")));
      player.method_43496(Text.method_43470("§a死机状态: §f" + (ghost.isDeadlocked() ? "§c是" : "§a否")));
      player.method_43496(Text.method_43470("§a移动禁用: §f" + (ghost.isMovementDisabled() ? "§c是" : "§a否")));
      player.method_43496(Text.method_43470("§a行走状态: §f" + (ghost.isWalking() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§6--- 可见性状态 ---"));
      player.method_43496(Text.method_43470("§a基础可见: §f" + (ghost.isVisible() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§a实际可见: §f" + (ghost.isVisible() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§a可见计时: §f" + ghost.getVisibleTicks() + " ticks"));
      player.method_43496(Text.method_43470("§a在关闭棺材中: §f" + (ghost.isInClosedCoffin() ? "§c是" : "§a否")));
      player.method_43496(Text.method_43470("§6--- 攻击状态 ---"));
      player.method_43496(Text.method_43470("§a攻击冷却: §f" + ghost.getAttackCooldown() + " ticks"));
      player.method_43496(Text.method_43470("§a杀人规律启用: §f" + (ghost.isKillingRulesEnabled() ? "§a是" : "§c否")));
      player.method_43496(Text.method_43470("§a拥有棺材钉: §f" + (ghost.hasCoffinNail() ? "§c是" : "§a否")));
      player.method_43496(Text.method_43470("§6--- 鬼奴信息 ---"));
      player.method_43496(Text.method_43470("§a当前鬼奴个数: §f" + ghost.getGhostSlaveCount()));
      player.method_43496(Text.method_43470("§6--- 攻击目标信息 ---"));
      LivingEntity target = ghost.method_5968();
      if (target != null) {
         player.method_43496(Text.method_43470("§a当前目标: §f" + target.method_5477().getString()));
         player.method_43496(Text.method_43470("§a目标类型: §f" + target.method_5864().toString()));
         player.method_43496(
            Text.method_43470("§a目标位置: §f" + String.format("%.1f, %.1f, %.1f", target.method_23317(), target.method_23318(), target.method_23321()))
         );
         player.method_43496(Text.method_43470("§a目标距离: §f" + String.format("%.1f", ghost.method_5739(target))));
         player.method_43496(Text.method_43470("§a目标生命值: §f" + String.format("%.1f/%.1f", target.method_6032(), target.method_6063())));
         LivingEntity attackTarget = ghost.getAttackTarget();
         if (attackTarget != null) {
            player.method_43496(Text.method_43470("§a攻击目标: §f" + attackTarget.method_5477().getString()));
            player.method_43496(Text.method_43470("§a攻击目标类型: §f" + attackTarget.method_5864().toString()));
            player.method_43496(Text.method_43470("§a攻击目标距离: §f" + String.format("%.1f", ghost.method_5739(attackTarget))));
         } else {
            player.method_43496(Text.method_43470("§a攻击目标: §f无"));
         }
      } else {
         player.method_43496(Text.method_43470("§a当前目标: §f无"));
         player.method_43496(Text.method_43470("§a攻击目标: §f无"));
      }

      player.method_43496(Text.method_43470("§6--- 动画状态信息 ---"));
      player.method_43496(Text.method_43470("§a手部摆动状态: §f" + (ghost.field_6252 ? "§a正在摆动" : "§c静止")));
      player.method_43496(Text.method_43470("§a手部摆动计时: §f" + ghost.field_6279 + " ticks"));
      player.method_43496(Text.method_43470("§a行走状态: §f" + (ghost.isWalking() ? "§a正在行走" : "§c静止")));
      player.method_43496(
         Text.method_43470("§a强制行走状态: §f" + (ghost.walkingStateManager != null && ghost.walkingStateManager.isForcedWalking() ? "§a是" : "§c否"))
      );
      player.method_43496(Text.method_43470("§a当前动画: §f" + this.getCurrentAnimationState(ghost)));
      player.method_43496(Text.method_43470("§6=== 调试信息结束 ==="));
   }

   private String getCurrentAnimationState(GhostEntity ghost) {
      if (ghost.field_6252) {
         return "§c攻击动画";
      } else if (ghost.isWalking()) {
         return "§a行走动画";
      } else {
         return ghost.method_18798().method_37268() > 0.001 && ghost.method_24828() ? "§b移动动画" : "§e空闲动画";
      }
   }
}
