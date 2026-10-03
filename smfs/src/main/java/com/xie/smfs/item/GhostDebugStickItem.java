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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (!world.isClient) {
         if (this.findGhostTarget(user) instanceof GhostEntity ghost) {
            this.displayGhostStatusInfo(user, ghost);
            return TypedActionResult.success(itemStack);
         }

         user.sendMessage(Text.literal("§c未找到附近的鬼实体！"));
      }

      return TypedActionResult.pass(itemStack);
   }

   private LivingEntity findGhostTarget(PlayerEntity player) {
      return (LivingEntity)player.getWorld()
         .getEntitiesByClass(GhostEntity.class, player.getBoundingBox().expand(10.0), ghost -> ghost.isAlive() && player.canSee(ghost))
         .stream()
         .findFirst()
         .orElse(null);
   }

   private void displayGhostStatusInfo(PlayerEntity player, GhostEntity ghost) {
      player.sendMessage(Text.literal("§6=== 鬼生物调试信息 ==="));
      player.sendMessage(Text.literal("§a鬼名称: §f" + ghost.getName().getString()));
      player.sendMessage(Text.literal("§a实体类型: §f" + ghost.getType().toString()));
      player.sendMessage(Text.literal("§a位置: §f" + String.format("%.1f, %.1f, %.1f", ghost.getX(), ghost.getY(), ghost.getZ())));
      player.sendMessage(Text.literal("§6--- 生命状态 ---"));
      player.sendMessage(Text.literal("§a生命值: §f" + String.format("%.1f/%.1f", ghost.getHealth(), ghost.getMaxHealth())));
      player.sendMessage(Text.literal("§a是否存活: §f" + (ghost.isAlive() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§6--- API支持状态 ---"));
      player.sendMessage(Text.literal("§a自定义攻击逻辑: §f" + (ghost.getCustomAttackLogic() != null ? "§a已设置" : "§c未设置")));
      player.sendMessage(Text.literal("§a自定义目标选择器: §f" + (ghost.getCustomTargetSelector() != null ? "§a已设置" : "§c未设置")));
      player.sendMessage(Text.literal("§a状态监听器数量: §f" + (ghost.getStateListeners() != null ? ghost.getStateListeners().size() : 0)));
      player.sendMessage(Text.literal("§a自定义NBT序列化器: §f" + (ghost.getCustomNbtSerializers() != null ? ghost.getCustomNbtSerializers().size() : 0)));
      player.sendMessage(Text.literal("§a自定义NBT反序列化器: §f" + (ghost.getCustomNbtDeserializers() != null ? ghost.getCustomNbtDeserializers().size() : 0)));
      player.sendMessage(Text.literal("§6--- 灵异属性 ---"));
      player.sendMessage(Text.literal("§a灵异强度: §f" + ghost.getSpiritualStrength() + "/" + ghost.getMaxSpiritualStrength()));
      player.sendMessage(Text.literal("§a灵异伤害: §f" + ghost.getSpiritualDamage()));
      player.sendMessage(Text.literal("§a灵异抗性: §f" + ghost.getSpiritualResistance()));
      player.sendMessage(Text.literal("§a恢复因子: §f" + String.format("%.2f", ghost.getRecoveryFactor())));
      player.sendMessage(Text.literal("§6--- 怨气值系统 ---"));
      player.sendMessage(Text.literal("§a当前怨气值: §f" + ghost.getResentmentValue() + "/100"));
      player.sendMessage(Text.literal("§a怨气阈值: §f" + (ghost.isResentmentThresholdReached() ? "§c已达到" : "§a未达到")));
      player.sendMessage(Text.literal("§a怨气积累范围: §f" + String.format("%.1f格", ghost.getGhostDomainRadius() > 0.0F ? ghost.getGhostDomainRadius() : 48.0)));
      player.sendMessage(Text.literal("§6--- 鬼域状态 ---"));
      player.sendMessage(Text.literal("§a鬼域等级: §f" + ghost.getGhostDomainLevel()));
      player.sendMessage(Text.literal("§a鬼域半径: §f" + String.format("%.1f", ghost.getGhostDomainRadius())));
      player.sendMessage(Text.literal("§a拥有鬼域: §f" + (ghost.hasGhostDomain() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a鬼域启用: §f" + (ghost.isGhostDomainEnabled() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a恐怖等级: §f" + ghost.getTerrorLevel()));
      player.sendMessage(Text.literal("§6--- 控制状态 ---"));
      player.sendMessage(Text.literal("§a被压制: §f" + (ghost.isSuppressed() ? "§c是" : "§a否")));
      player.sendMessage(Text.literal("§a死机状态: §f" + (ghost.isDeadlocked() ? "§c是" : "§a否")));
      player.sendMessage(Text.literal("§a移动禁用: §f" + (ghost.isMovementDisabled() ? "§c是" : "§a否")));
      player.sendMessage(Text.literal("§a行走状态: §f" + (ghost.isWalking() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§6--- 可见性状态 ---"));
      player.sendMessage(Text.literal("§a基础可见: §f" + (ghost.isVisible() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a实际可见: §f" + (ghost.isVisible() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a可见计时: §f" + ghost.getVisibleTicks() + " ticks"));
      player.sendMessage(Text.literal("§a在关闭棺材中: §f" + (ghost.isInClosedCoffin() ? "§c是" : "§a否")));
      player.sendMessage(Text.literal("§6--- 攻击状态 ---"));
      player.sendMessage(Text.literal("§a攻击冷却: §f" + ghost.getAttackCooldown() + " ticks"));
      player.sendMessage(Text.literal("§a杀人规律启用: §f" + (ghost.isKillingRulesEnabled() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a拥有棺材钉: §f" + (ghost.hasCoffinNail() ? "§c是" : "§a否")));
      player.sendMessage(Text.literal("§6--- 鬼奴信息 ---"));
      player.sendMessage(Text.literal("§a当前鬼奴个数: §f" + ghost.getGhostSlaveCount()));
      player.sendMessage(Text.literal("§6--- 攻击目标信息 ---"));
      LivingEntity target = ghost.getTarget();
      if (target != null) {
         player.sendMessage(Text.literal("§a当前目标: §f" + target.getName().getString()));
         player.sendMessage(Text.literal("§a目标类型: §f" + target.getType().toString()));
         player.sendMessage(Text.literal("§a目标位置: §f" + String.format("%.1f, %.1f, %.1f", target.getX(), target.getY(), target.getZ())));
         player.sendMessage(Text.literal("§a目标距离: §f" + String.format("%.1f", ghost.distanceTo(target))));
         player.sendMessage(Text.literal("§a目标生命值: §f" + String.format("%.1f/%.1f", target.getHealth(), target.getMaxHealth())));
         LivingEntity attackTarget = ghost.getAttackTarget();
         if (attackTarget != null) {
            player.sendMessage(Text.literal("§a攻击目标: §f" + attackTarget.getName().getString()));
            player.sendMessage(Text.literal("§a攻击目标类型: §f" + attackTarget.getType().toString()));
            player.sendMessage(Text.literal("§a攻击目标距离: §f" + String.format("%.1f", ghost.distanceTo(attackTarget))));
         } else {
            player.sendMessage(Text.literal("§a攻击目标: §f无"));
         }
      } else {
         player.sendMessage(Text.literal("§a当前目标: §f无"));
         player.sendMessage(Text.literal("§a攻击目标: §f无"));
      }

      player.sendMessage(Text.literal("§6--- 动画状态信息 ---"));
      player.sendMessage(Text.literal("§a手部摆动状态: §f" + (ghost.handSwinging ? "§a正在摆动" : "§c静止")));
      player.sendMessage(Text.literal("§a手部摆动计时: §f" + ghost.handSwingTicks + " ticks"));
      player.sendMessage(Text.literal("§a行走状态: §f" + (ghost.isWalking() ? "§a正在行走" : "§c静止")));
      player.sendMessage(Text.literal("§a强制行走状态: §f" + (ghost.walkingStateManager != null && ghost.walkingStateManager.isForcedWalking() ? "§a是" : "§c否")));
      player.sendMessage(Text.literal("§a当前动画: §f" + this.getCurrentAnimationState(ghost)));
      player.sendMessage(Text.literal("§6=== 调试信息结束 ==="));
   }

   private String getCurrentAnimationState(GhostEntity ghost) {
      if (ghost.handSwinging) {
         return "§c攻击动画";
      } else if (ghost.isWalking()) {
         return "§a行走动画";
      } else {
         return ghost.getVelocity().horizontalLengthSquared() > 0.001 && ghost.isOnGround() ? "§b移动动画" : "§e空闲动画";
      }
   }
}
