package com.xie.smfs.event;

import com.xie.smfs.Smfs;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.entity.ghost.VillagerGhostEntity;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.manager.GhostSkillManager;
import com.xie.smfs.util.WeaponOilHandler;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class EntityAttackListener {
   public static void register() {
      AttackEntityCallback.EVENT
         .register(
            (AttackEntityCallback)(player, world, hand, entity, hitResult) -> {
               if (!world.isClient && player instanceof ServerPlayerEntity) {
                  if (entity instanceof VillagerEntity) {
                     VillagerGhostEntity.recordVillagerAttack(player);
                  }

                  if (entity instanceof LivingEntity target
                     && SpiritWeapon.isSpiritWeapon(player.getMainHandStack())
                     && world.getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
                     SpiritWeapon weapon = (SpiritWeapon)player.getMainHandStack().getItem();
                     ModEvents.preSpiritWeaponAttackHealth.put(target.getUuid(), target.getHealth());
                     weapon.onSpiritWeaponAttack(player.getMainHandStack(), target, player);
                  }

                  if (entity instanceof LivingEntity target) {
                     handleCorpseOilSwordAttack((ServerPlayerEntity)player, target, world);
                  }

                  if (entity instanceof LivingEntity target && world.getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
                     GhostSkillManager.onPlayerAttack(player, target);
                  }
               }

               return ActionResult.PASS;
            }
         );
   }

   private static void handleCorpseOilSwordAttack(ServerPlayerEntity player, LivingEntity target, World world) {
      ItemStack mainHand = player.getMainHandStack();
      if (world.getRegistryKey() != Smfs.GHOST_DREAM_DIMENSION) {
         if (mainHand.getItem() instanceof SwordItem && WeaponOilHandler.getCorpseOilLayers(mainHand) > 0) {
            if (target instanceof GhostEntity) {
               return;
            }

            WeaponOilHandler.consumeCorpseOilLayer(mainHand);
         }
      }
   }
}
