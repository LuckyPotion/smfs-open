package com.xie.smfs.item;

import com.xie.smfs.network.packets.common.c2s.SpearBindC2SPacket;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BoundSpearItem extends FissuredSpearPurpleItem {
   private static final String OWNER_UUID_KEY = "OwnerUUID";
   private static final String CORRECT_ANSWER_KEY = "CorrectAnswer";
   private static final String IS_BOUND_KEY = "IsBound";
   private static final String LAST_UI_TICK_KEY = "LastUITick";

   public BoundSpearItem(Settings settings) {
      super(settings);
   }

   public boolean isBound(ItemStack stack) {
      return stack.method_7985() && stack.method_7969().method_10577("IsBound");
   }

   public UUID getOwnerUUID(ItemStack stack) {
      if (!stack.method_7985()) {
         return null;
      }

      String uuidStr = stack.method_7969().method_10558("OwnerUUID");

      try {
         return UUID.fromString(uuidStr);
      } catch (Exception e) {
         return null;
      }
   }

   public String getCorrectAnswer(ItemStack stack) {
      return !stack.method_7985() ? "" : stack.method_7969().method_10558("CorrectAnswer");
   }

   public void bindToOwner(ItemStack stack, PlayerEntity owner, String answer) {
      NbtCompound nbt = stack.method_7948();
      nbt.method_10582("OwnerUUID", owner.method_5845());
      nbt.method_10582("CorrectAnswer", answer);
      nbt.method_10556("IsBound", true);
   }

   public boolean isOwner(ItemStack stack, PlayerEntity player) {
      UUID ownerUUID = this.getOwnerUUID(stack);
      return ownerUUID != null && ownerUUID.equals(player.method_5667());
   }

   public void method_7888(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.method_7888(stack, world, entity, slot, selected);
      if (!world.field_9236 && entity instanceof ServerPlayerEntity player) {
         if (selected) {
            long lastAttemptPlayerId = stack.method_7985() ? stack.method_7969().method_10537("LastAttemptPlayer") : 0L;
            long currentPlayerId = player.method_5628();
            if (this.isBound(stack) && !this.isOwner(stack, player) && lastAttemptPlayerId != currentPlayerId) {
               stack.method_7948().method_10551("LastUITick");
               stack.method_7948().method_10544("LastAttemptPlayer", currentPlayerId);
            }

            long currentTick = world.method_8510();
            long lastUITick = stack.method_7985() ? stack.method_7969().method_10537("LastUITick") : 0L;
            if (this.isBound(stack) && !this.isOwner(stack, player) && currentTick - lastUITick < 600L) {
               long lastFailedPlayerId = stack.method_7985() ? stack.method_7969().method_10537("LastFailedPlayer") : 0L;
               if (lastFailedPlayerId == currentPlayerId) {
                  long lastErosionTick = stack.method_7985() ? stack.method_7969().method_10537("LastErosionTick") : 0L;
                  if (currentTick - lastErosionTick >= 20L) {
                     player.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 19));
                     stack.method_7948().method_10544("LastErosionTick", currentTick);
                  }
               }
            } else {
               if (!this.isBound(stack)) {
                  SpearBindC2SPacket.sendToClient(player, true, "");
                  stack.method_7948().method_10544("LastUITick", currentTick);
               } else if (!this.isOwner(stack, player)) {
                  SpearBindC2SPacket.sendToClient(player, false, this.getCorrectAnswer(stack));
                  stack.method_7948().method_10544("LastUITick", currentTick);
                  stack.method_7948().method_10544("LastAttemptPlayer", currentPlayerId);
               }
            }
         }
      }
   }

   public void handleAnswer(String answer, ItemStack stack, PlayerEntity player) {
      if (!this.isBound(stack)) {
         this.bindToOwner(stack, player, answer);
      } else if (answer.equals(this.getCorrectAnswer(stack))) {
         stack.method_7948().method_10551("LastFailedPlayer");
      } else {
         player.method_5643(player.method_48923().method_48830(), 5000.0F);
         stack.method_7948().method_10544("LastFailedPlayer", player.method_5628());
      }
   }

   @Override
   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      if (this.isBound(stack)) {
         tooltip.add(Text.method_43470("已认主").method_27692(Formatting.field_1054));
      } else {
         tooltip.add(Text.method_43470("未认主").method_27692(Formatting.field_1080));
      }
   }
}
