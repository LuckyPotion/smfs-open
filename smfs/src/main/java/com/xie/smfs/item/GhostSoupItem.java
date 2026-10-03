package com.xie.smfs.item;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.util.GhostUtils;
import com.xie.smfs.util.InstantKillUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostSoupItem extends Item {
   public GhostSoupItem(Settings settings) {
      super(settings);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.method_5998(hand);
      if (user.method_7332(true)) {
         user.method_6019(hand);
         return TypedActionResult.method_22428(itemStack);
      } else {
         return TypedActionResult.method_22431(itemStack);
      }
   }

   public ItemStack method_7861(ItemStack stack, World world, LivingEntity user) {
      if (user instanceof PlayerEntity player && !world.field_9236) {
         this.stripGhostFromBody(player);
      }

      return super.method_7861(stack, world, user);
   }

   private void stripGhostFromBody(PlayerEntity player) {
      if (player instanceof ServerPlayerEntity serverPlayer) {
         boolean hasBoundGhost = false;
         List<Integer> occupiedSlots = new ArrayList<>();

         for (int i = 0; i < 10; i++) {
            if (PlayerEvents.isGhostSlotOccupied(player, i)) {
               hasBoundGhost = true;
               occupiedSlots.add(i);
            }
         }

         if (hasBoundGhost && !occupiedSlots.isEmpty()) {
            Random random = new Random();
            int slotToStrip = occupiedSlots.get(random.nextInt(occupiedSlots.size()));
            String ghostType = PlayerEvents.getGhostTypeInSlot(player, slotToStrip);
            int strippedLevel = PlayerEvents.getGhostSlotLevel(player, slotToStrip);
            int strippedRevivalDegree = PlayerEvents.getGhostSlotRevivalDegree(player, slotToStrip);
            boolean isLastGhost = occupiedSlots.size() == 1;
            double revivalChance = 0.0;
            if (isLastGhost) {
               revivalChance = 1.0;
            } else if (strippedRevivalDegree >= 900) {
               revivalChance = 0.5;
            } else if (strippedRevivalDegree >= 800) {
               revivalChance = 0.2;
            } else if (strippedRevivalDegree >= 700) {
               revivalChance = 0.05;
            } else if (strippedRevivalDegree >= 600) {
               revivalChance = 0.01;
            }

            boolean willRevive = random.nextDouble() < revivalChance;
            PlayerEvents.clearGhostSlot(player, slotToStrip);
            GhostDomainManager.disableGhostDomain(player);
            if (willRevive) {
               player.method_7353(Text.method_43471("item.smfs.ghost_soup.effect.revival"), true);
               GhostEntity ghostEntity = GhostUtils.createGhostEntityByType(ghostType, player.method_37908());
               if (ghostEntity != null) {
                  ghostEntity.method_5808(
                     player.method_23317() + random.nextGaussian() * 2.0,
                     player.method_23318(),
                     player.method_23321() + random.nextGaussian() * 2.0,
                     random.nextFloat() * 360.0F,
                     0.0F
                  );
                  ghostEntity.method_5980(player);
                  player.method_37908().method_8649(ghostEntity);
               } else {
                  this.spawnCorrespondingTamedItem(ghostType, player, strippedLevel, strippedRevivalDegree);
               }

               if (isLastGhost) {
                  InstantKillUtil.executePlayerSelfKill(serverPlayer, ModDamageSources.ghost(player.method_37908()));
               }
            } else {
               this.spawnCorrespondingTamedItem(ghostType, player, strippedLevel, strippedRevivalDegree);
               this.verifyAndEnsureCleanup(player, slotToStrip);
               player.method_7353(Text.method_43469("item.smfs.ghost_soup.effect.success", new Object[]{slotToStrip + 1}), true);
            }

            player.method_6092(new StatusEffectInstance(StatusEffects.field_5903, 200, 1, false, true));
         } else {
            player.method_7353(Text.method_43471("item.smfs.ghost_soup.effect.no_ghost"), true);
         }
      }
   }

   private void spawnCorrespondingTamedItem(String ghostType, PlayerEntity player, int level, int revivalDegree) {
      ItemStack tamedItem = GhostUtils.createTamedItem(ghostType);
      if (!tamedItem.method_7960()) {
         NbtCompound nbt = tamedItem.method_7948();
         nbt.method_10569("StoredLevel", level);
         nbt.method_10569("StoredRevivalDegree", revivalDegree);
         if (!player.method_31548().method_7394(tamedItem)) {
            player.method_7328(tamedItem, false);
         }
      }
   }

   private void verifyAndEnsureCleanup(PlayerEntity player, int slotToStrip) {
      boolean slotStillOccupied = PlayerEvents.isGhostSlotOccupied(player, slotToStrip);
      boolean domainStillActive = GhostDomainManager.isGhostDomainActive(player);
      if (slotStillOccupied) {
         PlayerEvents.clearGhostSlot(player, slotToStrip);
      }

      if (domainStillActive) {
         GhostDomainManager.disableGhostDomain(player);
      }

      if (slotStillOccupied || domainStillActive) {
         Logger.getLogger("GhostSoup").info("鬼汤保险补救触发 - 槽位" + slotToStrip + (slotStillOccupied ? "已重新清除" : "") + (domainStillActive ? "鬼域已重新关闭" : ""));
      }
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_soup.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_soup.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_soup.description.side_effect"));
      tooltip.add(Text.method_43471("item.smfs.ghost_soup.description.side_effect_last"));
   }
}
