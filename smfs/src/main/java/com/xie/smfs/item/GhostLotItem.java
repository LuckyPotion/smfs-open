package com.xie.smfs.item;

import com.xie.smfs.damage.ModDamageSources;
import com.xie.smfs.network.packets.ui.s2c.GhostLotFlipS2CPacket;
import com.xie.smfs.registry.ModEffects;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostLotItem extends Item {
   private static final int COOLDOWN_TICKS = 20;
   private static final int IMMUNITY_DURATION = 3600;
   private static final int REVEAL_DELAY_TICKS = 40;
   private static final Map<UUID, GhostLotItem.PendingResult> pendingResults = new ConcurrentHashMap<>();

   public GhostLotItem(Settings settings) {
      super(settings);
   }

   public static void tickPendingResults(ServerWorld world) {
      long currentTime = world.method_8510();
      pendingResults.entrySet().removeIf(entry -> {
         GhostLotItem.PendingResult result = entry.getValue();
         if (currentTime >= result.applyTime) {
            applyLotResult(result.player, result.isLifeLot, world);
            return true;
         } else {
            return false;
         }
      });
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (!world.field_9236 && user instanceof ServerPlayerEntity serverPlayer) {
         if (user.method_7357().method_7904(this)) {
            return TypedActionResult.method_22430(stack);
         } else if (this.hasUsedToday(stack, world)) {
            serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_lot.already_used"), false);
            return TypedActionResult.method_22430(stack);
         } else {
            boolean isLifeLot = world.method_8409().method_43056();
            GhostLotFlipS2CPacket.send(serverPlayer, isLifeLot);
            world.method_43128(
               null,
               serverPlayer.method_23317(),
               serverPlayer.method_23318(),
               serverPlayer.method_23321(),
               SoundEvents.field_17484,
               SoundCategory.field_15248,
               1.0F,
               0.8F
            );
            serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_lot.drawing"), false);
            this.markUsedToday(stack, world);
            user.method_7357().method_7906(this, 20);
            ServerWorld serverWorld = (ServerWorld)world;
            long applyTime = serverWorld.method_8510() + 40L;
            pendingResults.put(serverPlayer.method_5667(), new GhostLotItem.PendingResult(serverPlayer, isLifeLot, applyTime));
            return TypedActionResult.method_22427(stack);
         }
      } else {
         return TypedActionResult.method_22430(stack);
      }
   }

   private static void applyLotResult(ServerPlayerEntity serverPlayer, boolean isLifeLot, World world) {
      if (serverPlayer.method_5805()) {
         if (isLifeLot) {
            serverPlayer.method_6092(new StatusEffectInstance(ModEffects.SPIRIT_IMMUNITY, 3600, 0, false, true, true));
            serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_lot.life_sign"), false);
            world.method_43128(
               null,
               serverPlayer.method_23317(),
               serverPlayer.method_23318(),
               serverPlayer.method_23321(),
               SoundEvents.field_15119,
               SoundCategory.field_15248,
               1.0F,
               1.5F
            );
         } else {
            serverPlayer.method_5643(ModDamageSources.ghost(world), 5500.0F);
            serverPlayer.method_7353(Text.method_43471("item.smfs.ghost_lot.death_sign"), false);
            world.method_43128(
               null,
               serverPlayer.method_23317(),
               serverPlayer.method_23318(),
               serverPlayer.method_23321(),
               SoundEvents.field_14792,
               SoundCategory.field_15248,
               0.5F,
               1.0F
            );
         }
      }
   }

   private boolean hasUsedToday(ItemStack stack, World world) {
      NbtCompound nbt = stack.method_7948();
      if (nbt.method_10545("LastUsedDay")) {
         long lastUsedDay = nbt.method_10537("LastUsedDay");
         long currentDay = world.method_8532() / 24000L;
         return lastUsedDay == currentDay;
      } else {
         return false;
      }
   }

   private void markUsedToday(ItemStack stack, World world) {
      NbtCompound nbt = stack.method_7948();
      long currentDay = world.method_8532() / 24000L;
      nbt.method_10544("LastUsedDay", currentDay);
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.description.type"));
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.effect.life_sign"));
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.effect.death_sign"));
      tooltip.add(Text.method_43471("item.smfs.ghost_lot.effect.daily"));
      if (world != null && this.hasUsedToday(stack, world)) {
         tooltip.add(Text.method_43471("item.smfs.ghost_lot.used_today"));
      }
   }

   private static class PendingResult {
      final ServerPlayerEntity player;
      final boolean isLifeLot;
      final long applyTime;

      PendingResult(ServerPlayerEntity player, boolean isLifeLot, long applyTime) {
         this.player = player;
         this.isLifeLot = isLifeLot;
         this.applyTime = applyTime;
      }
   }
}
