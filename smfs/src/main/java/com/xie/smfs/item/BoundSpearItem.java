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
      return stack.hasNbt() && stack.getNbt().getBoolean("IsBound");
   }

   public UUID getOwnerUUID(ItemStack stack) {
      if (!stack.hasNbt()) {
         return null;
      }

      String uuidStr = stack.getNbt().getString("OwnerUUID");

      try {
         return UUID.fromString(uuidStr);
      } catch (Exception e) {
         return null;
      }
   }

   public String getCorrectAnswer(ItemStack stack) {
      return !stack.hasNbt() ? "" : stack.getNbt().getString("CorrectAnswer");
   }

   public void bindToOwner(ItemStack stack, PlayerEntity owner, String answer) {
      NbtCompound nbt = stack.getOrCreateNbt();
      nbt.putString("OwnerUUID", owner.getUuidAsString());
      nbt.putString("CorrectAnswer", answer);
      nbt.putBoolean("IsBound", true);
   }

   public boolean isOwner(ItemStack stack, PlayerEntity player) {
      UUID ownerUUID = this.getOwnerUUID(stack);
      return ownerUUID != null && ownerUUID.equals(player.getUuid());
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      super.inventoryTick(stack, world, entity, slot, selected);
      if (!world.isClient && entity instanceof ServerPlayerEntity player) {
         if (selected) {
            long lastAttemptPlayerId = stack.hasNbt() ? stack.getNbt().getLong("LastAttemptPlayer") : 0L;
            long currentPlayerId = player.getId();
            if (this.isBound(stack) && !this.isOwner(stack, player) && lastAttemptPlayerId != currentPlayerId) {
               stack.getOrCreateNbt().remove("LastUITick");
               stack.getOrCreateNbt().putLong("LastAttemptPlayer", currentPlayerId);
            }

            long currentTick = world.getTime();
            long lastUITick = stack.hasNbt() ? stack.getNbt().getLong("LastUITick") : 0L;
            if (this.isBound(stack) && !this.isOwner(stack, player) && currentTick - lastUITick < 600L) {
               long lastFailedPlayerId = stack.hasNbt() ? stack.getNbt().getLong("LastFailedPlayer") : 0L;
               if (lastFailedPlayerId == currentPlayerId) {
                  long lastErosionTick = stack.hasNbt() ? stack.getNbt().getLong("LastErosionTick") : 0L;
                  if (currentTick - lastErosionTick >= 20L) {
                     player.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_EROSION, 60, 19));
                     stack.getOrCreateNbt().putLong("LastErosionTick", currentTick);
                  }
               }
            } else {
               if (!this.isBound(stack)) {
                  SpearBindC2SPacket.sendToClient(player, true, "");
                  stack.getOrCreateNbt().putLong("LastUITick", currentTick);
               } else if (!this.isOwner(stack, player)) {
                  SpearBindC2SPacket.sendToClient(player, false, this.getCorrectAnswer(stack));
                  stack.getOrCreateNbt().putLong("LastUITick", currentTick);
                  stack.getOrCreateNbt().putLong("LastAttemptPlayer", currentPlayerId);
               }
            }
         }
      }
   }

   public void handleAnswer(String answer, ItemStack stack, PlayerEntity player) {
      if (!this.isBound(stack)) {
         this.bindToOwner(stack, player, answer);
      } else if (answer.equals(this.getCorrectAnswer(stack))) {
         stack.getOrCreateNbt().remove("LastFailedPlayer");
      } else {
         player.damage(player.getDamageSources().generic(), 5000.0F);
         stack.getOrCreateNbt().putLong("LastFailedPlayer", player.getId());
      }
   }

   @Override
   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      if (this.isBound(stack)) {
         tooltip.add(Text.literal("已认主").formatted(Formatting.YELLOW));
      } else {
         tooltip.add(Text.literal("未认主").formatted(Formatting.GRAY));
      }
   }
}
