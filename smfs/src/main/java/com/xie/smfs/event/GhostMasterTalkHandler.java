package com.xie.smfs.event;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.entity.other.CaoYanHuaEntity;
import com.xie.smfs.entity.other.ChenDoctorEntity;
import com.xie.smfs.entity.other.LiuXiaoYuEntity;
import com.xie.smfs.entity.other.WangXiaoMingEntity;
import com.xie.smfs.manager.GhostDreamManager;
import com.xie.smfs.network.packets.ui.s2c.OpenGhostHunterTalkScreenS2CPacket;
import com.xie.smfs.registry.ModItems;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class GhostMasterTalkHandler implements UseEntityCallback {
   public ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
      GhostMasterEntity ghostMaster = null;
      String[] customDialogues = null;
      if (entity instanceof GhostMasterEntity) {
         ghostMaster = (GhostMasterEntity)entity;
      } else if (entity instanceof WangXiaoMingEntity wangXiaoMing) {
         customDialogues = wangXiaoMing.getGreetingDialogues();
      } else if (entity instanceof ChenDoctorEntity chenDoctor) {
         customDialogues = chenDoctor.getGreetingDialogues();
      } else if (entity instanceof CaoYanHuaEntity caoYanHua) {
         customDialogues = caoYanHua.getGreetingDialogues();
      } else {
         if (!(entity instanceof LiuXiaoYuEntity liuXiaoYu)) {
            return ActionResult.field_5811;
         }

         customDialogues = liuXiaoYu.getGreetingDialogues();
      }

      if (world.method_8608()) {
         return ActionResult.field_5811;
      } else if (!player.method_5998(hand).method_7960()) {
         return ActionResult.field_5811;
      } else if (this.isHoldingCoffinNail(player)) {
         return ActionResult.field_5811;
      } else if (ghostMaster != null && ghostMaster.isSuppressed()) {
         return ActionResult.field_5811;
      } else if (!(player instanceof ServerPlayerEntity serverPlayer)) {
         return ActionResult.field_5811;
      } else {
         boolean var19 = false;
         boolean useStoryMode = ghostMaster != null
            && ghostMaster.isStoryMode()
            && GhostDreamManager.isInGhostDream(player)
            && GhostDreamManager.isNaturalTrigger(player);
         if (useStoryMode) {
            if (ghostMaster.hasCompletedStory(player)) {
               var19 = true;
            } else {
               ghostMaster.markStoryCompleted(player);
            }
         }

         String[] dialogues;
         if (customDialogues != null) {
            dialogues = customDialogues;
         } else if (ghostMaster != null) {
            dialogues = ghostMaster.getGreetingDialogues();
         } else {
            dialogues = new String[0];
         }

         List<String> dialogueList = dialogues != null ? Arrays.asList(dialogues) : List.of();
         List<String> storyDialogues = useStoryMode && ghostMaster != null ? ghostMaster.getStoryDialogues() : null;
         PacketByteBuf buf = PacketByteBufs.create();
         buf.writeInt(entity.method_5628());
         buf.writeInt(dialogueList.size());

         for (String dialogue : dialogueList) {
            buf.method_10814(dialogue);
         }

         if (storyDialogues != null) {
            buf.writeInt(storyDialogues.size());

            for (String dialogue : storyDialogues) {
               buf.method_10814(dialogue);
            }
         } else {
            buf.writeInt(0);
         }

         buf.writeBoolean(var19);
         ServerPlayNetworking.send(serverPlayer, OpenGhostHunterTalkScreenS2CPacket.ID, buf);
         return ActionResult.field_5812;
      }
   }

   private boolean isHoldingCoffinNail(PlayerEntity player) {
      ItemStack mainHandStack = player.method_6047();
      if (!mainHandStack.method_7960() && mainHandStack.method_7909() == ModItems.COFFIN_NAIL) {
         return true;
      }

      ItemStack offHandStack = player.method_6079();
      return !offHandStack.method_7960() && offHandStack.method_7909() == ModItems.COFFIN_NAIL;
   }
}
