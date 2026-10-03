package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.effect.SpiritAttributes;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.master.YeZhenEntity;
import com.xie.smfs.event.ModEvents;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModItems;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScapegoatGhostItem extends BaseGhostEyeItem {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/ScapegoatGhostItem");

   public ScapegoatGhostItem(Settings settings) {
      super(settings.maxCount(1), 20, 15, 0, 0, 0.15);
   }

   @Override
   public String getGhostType() {
      return "scapegoat_ghost";
   }

   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.scapegoat_ghost");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.translatable("item.smfs.scapegoat_ghost.bind_success").append(Text.literal(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.sendMessage(Text.translatable("item.smfs.scapegoat_ghost.slots_full"), true);
   }

   @Override
   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return super.use(world, user, hand);
   }

   @Override
   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
   }

   public static boolean handleScapegoatPassiveSkill(PlayerEntity player, DamageSource source, float amount) {
      if (!hasScapegoatGhostEquipped(player)) {
         return false;
      }

      if (!player.hasStatusEffect(ModEffects.SILENCE) && !player.hasStatusEffect(ModEffects.DREAM)) {
         if (player.getHealth() - amount <= 0.0F && !player.getWorld().isClient()) {
            LivingEntity transferTarget = findTransferTargetInGhostDomain(player);
            if (transferTarget != null) {
               boolean isGhostMasterOrGhost = transferTarget instanceof GhostMasterEntity || transferTarget instanceof GhostEntity;
               if (hasScapegoatAbility(transferTarget) && transferTarget.getHealth() - amount <= 0.0F) {
                  LOGGER.debug("替死鬼被动技能触发：转移目标 {} 也有替死能力且处于致命状态，跳过转移避免递归", transferTarget.getName().getString());
                  if (player.getWorld().isClient()) {
                     player.sendMessage(
                        Text.translatable("item.smfs.scapegoat_ghost.recursion_prevented", new Object[]{transferTarget.getName().getString()}), true
                     );
                  }

                  return false;
               }

               if (isGhostMasterOrGhost) {
                  LOGGER.debug("替死鬼被动技能触发：转移目标为驭鬼者/鬼魂类 {}，直接返回true", transferTarget.getName().getString());
                  ModEvents.processingSpiritDamage.set(true);
                  DamageSource spiritDamageSource = ModDamageSources.ghost(player.getWorld());
                  boolean damageApplied = transferTarget.damage(spiritDamageSource, amount);
                  ModEvents.processingSpiritDamage.set(false);
                  if (damageApplied) {
                     LOGGER.debug("替死鬼被动技能：成功对驭鬼者/鬼魂类 {} 造成灵异伤害", transferTarget.getName().getString());
                  }

                  player.setHealth(player.getMaxHealth());
                  player.getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 0.0, 0.0, 0.0);
                  int scapegoatLevel = getScapegoatGhostLevel(player);
                  float sanityRecoveryPercent = getSanityRecoveryPercent(scapegoatLevel);
                  float spiritRecoveryPercent = getSpiritRecoveryPercent(scapegoatLevel);
                  float maxSanity = PlayerEvents.getMaxSanity(player);
                  float sanityRecovery = maxSanity * sanityRecoveryPercent;
                  PlayerEvents.setCurrentSanity(player, PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SANITY) + sanityRecovery);
                  float maxSpirit = PlayerEvents.getMaxSpirit(player);
                  float spiritRecovery = maxSpirit * spiritRecoveryPercent;
                  PlayerEvents.setCurrentSpirit(player, PlayerEvents.getCurrentSpirit(player) + spiritRecovery);
                  player.sendMessage(
                     Text.translatable(
                        "item.smfs.scapegoat_ghost.damage_transfer", new Object[]{String.format("%.1f", amount), transferTarget.getName().getString()}
                     ),
                     true
                  );
                  return true;
               }

               DamageSource spiritDamageSource = ModDamageSources.ghost(player.getWorld());
               boolean damageApplied = transferTarget.damage(spiritDamageSource, amount);
               if (damageApplied) {
                  player.setHealth(player.getMaxHealth());
                  player.getWorld().addParticle(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 0.0, 0.0, 0.0);
                  int scapegoatLevel = getScapegoatGhostLevel(player);
                  float sanityRecoveryPercent = getSanityRecoveryPercent(scapegoatLevel);
                  float spiritRecoveryPercent = getSpiritRecoveryPercent(scapegoatLevel);
                  float maxSanity = PlayerEvents.getMaxSanity(player);
                  float sanityRecovery = maxSanity * sanityRecoveryPercent;
                  PlayerEvents.setCurrentSanity(player, PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SANITY) + sanityRecovery);
                  float maxSpirit = PlayerEvents.getMaxSpirit(player);
                  float spiritRecovery = maxSpirit * spiritRecoveryPercent;
                  PlayerEvents.setCurrentSpirit(player, PlayerEvents.getCurrentSpirit(player) + spiritRecovery);
                  player.sendMessage(
                     Text.translatable(
                        "item.smfs.scapegoat_ghost.damage_transfer", new Object[]{String.format("%.1f", amount), transferTarget.getName().getString()}
                     ),
                     true
                  );
                  LOGGER.debug("替死鬼被动技能触发：伤害已转移到目标 {}，玩家生命值已恢复", transferTarget.getName().getString());
                  return true;
               }

               LOGGER.warn("替死鬼被动技能触发：伤害转移失败，目标 {} 免疫伤害", transferTarget.getName().getString());
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static LivingEntity findTransferTargetInGhostDomain(PlayerEntity player) {
      int radius = getGhostDomainRadius(player);
      List<LivingEntity> nearbyEntities = player.getWorld()
         .getEntitiesByClass(
            LivingEntity.class, player.getBoundingBox().expand(radius), entityx -> entityx != player && entityx.isAlive() && !(entityx instanceof GhostEntity)
         );
      if (!nearbyEntities.isEmpty()) {
         for (LivingEntity entity : nearbyEntities) {
            if (entity.hasStatusEffect(ModEffects.SCAPEGOAT_MARK)) {
               entity.removeStatusEffect(ModEffects.SCAPEGOAT_MARK);
               return entity;
            }
         }

         nearbyEntities.sort((entity1, entity2) -> {
            double distance1 = player.squaredDistanceTo(entity1);
            double distance2 = player.squaredDistanceTo(entity2);
            return Double.compare(distance1, distance2);
         });
         return nearbyEntities.get(0);
      } else {
         return null;
      }
   }

   private static int getGhostDomainRadius(PlayerEntity player) {
      int defaultRadius = 15;

      for (int i = 0; i < 10; i++) {
         ItemStack ghostItem = PlayerEvents.getGhostSlotItem(player, i);
         if (ghostItem.getItem() == ModItems.SCAPEGOAT_GHOST) {
            int level = PlayerEvents.getGhostSlotLevel(player, i);
            return defaultRadius + level * 2;
         }
      }

      return defaultRadius;
   }

   private static void spawnDamageTransferParticles(PlayerEntity player) {
      ServerWorld serverWorld = (ServerWorld)player.getWorld();
      Vec3d pos = player.getPos();

      for (int i = 0; i < 20; i++) {
         double offsetX = (player.getRandom().nextDouble() - 0.5) * 2.0;
         double offsetY = player.getRandom().nextDouble() * 2.0;
         double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2.0;
         serverWorld.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0.0, 0.0, 0.0, 0.1);
      }
   }

   public static boolean hasScapegoatGhostEquipped(PlayerEntity player) {
      return PlayerEvents.hasGhostType(player, "scapegoat_ghost");
   }

   private static boolean hasScapegoatAbility(LivingEntity entity) {
      return entity instanceof PlayerEntity ? hasScapegoatGhostEquipped((PlayerEntity)entity) : entity instanceof YeZhenEntity;
   }

   private static int getScapegoatGhostLevel(PlayerEntity player) {
      for (int i = 0; i < 10; i++) {
         NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
         if (spiritData.contains("GhostSlots")) {
            NbtCompound ghostSlots = spiritData.getCompound("GhostSlots");
            String slotKey = "Slot" + i;
            if (ghostSlots.contains(slotKey)) {
               NbtCompound slotData = ghostSlots.getCompound(slotKey);
               if (slotData.getBoolean("occupied")) {
                  ItemStack itemStack = ItemStack.fromNbt(slotData.getCompound("item"));
                  if (itemStack.getItem() instanceof ScapegoatGhostItem) {
                     return slotData.getInt("level");
                  }
               }
            }
         }
      }

      return 1;
   }

   private static float getSanityRecoveryPercent(int level) {
      return switch (level) {
         case 1 -> 0.2F;
         case 2 -> 0.35F;
         case 3 -> 0.45F;
         case 4 -> 0.6F;
         case 5 -> 0.8F;
         case 6 -> 1.0F;
         default -> 0.1F;
      };
   }

   private static float getSpiritRecoveryPercent(int level) {
      return switch (level) {
         case 1 -> 0.2F;
         case 2 -> 0.35F;
         case 3 -> 0.45F;
         case 4 -> 0.6F;
         case 5 -> 0.8F;
         case 6 -> 1.0F;
         default -> 0.1F;
      };
   }
}
