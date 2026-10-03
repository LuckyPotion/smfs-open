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
      long currentTime = world.getTime();
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

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
         if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.pass(stack);
         } else if (this.hasUsedToday(stack, world)) {
            serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_lot.already_used"), false);
            return TypedActionResult.pass(stack);
         } else {
            boolean isLifeLot = world.getRandom().nextBoolean();
            GhostLotFlipS2CPacket.send(serverPlayer, isLifeLot);
            world.playSound(
               null,
               serverPlayer.getX(),
               serverPlayer.getY(),
               serverPlayer.getZ(),
               SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
               SoundCategory.PLAYERS,
               1.0F,
               0.8F
            );
            serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_lot.drawing"), false);
            this.markUsedToday(stack, world);
            user.getItemCooldownManager().set(this, 20);
            ServerWorld serverWorld = (ServerWorld)world;
            long applyTime = serverWorld.getTime() + 40L;
            pendingResults.put(serverPlayer.getUuid(), new GhostLotItem.PendingResult(serverPlayer, isLifeLot, applyTime));
            return TypedActionResult.success(stack);
         }
      } else {
         return TypedActionResult.pass(stack);
      }
   }

   private static void applyLotResult(ServerPlayerEntity serverPlayer, boolean isLifeLot, World world) {
      if (serverPlayer.isAlive()) {
         if (isLifeLot) {
            serverPlayer.addStatusEffect(new StatusEffectInstance(ModEffects.SPIRIT_IMMUNITY, 3600, 0, false, true, true));
            serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_lot.life_sign"), false);
            world.playSound(
               null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 1.0F, 1.5F
            );
         } else {
            serverPlayer.damage(ModDamageSources.ghost(world), 5500.0F);
            serverPlayer.sendMessage(Text.translatable("item.smfs.ghost_lot.death_sign"), false);
            world.playSound(
               null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5F, 1.0F
            );
         }
      }
   }

   private boolean hasUsedToday(ItemStack stack, World world) {
      NbtCompound nbt = stack.getOrCreateNbt();
      if (nbt.contains("LastUsedDay")) {
         long lastUsedDay = nbt.getLong("LastUsedDay");
         long currentDay = world.getTimeOfDay() / 24000L;
         return lastUsedDay == currentDay;
      } else {
         return false;
      }
   }

   private void markUsedToday(ItemStack stack, World world) {
      NbtCompound nbt = stack.getOrCreateNbt();
      long currentDay = world.getTimeOfDay() / 24000L;
      nbt.putLong("LastUsedDay", currentDay);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_lot.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_lot.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_lot.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_lot.effect.life_sign"));
      tooltip.add(Text.translatable("item.smfs.ghost_lot.effect.death_sign"));
      tooltip.add(Text.translatable("item.smfs.ghost_lot.effect.daily"));
      if (world != null && this.hasUsedToday(stack, world)) {
         tooltip.add(Text.translatable("item.smfs.ghost_lot.used_today"));
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
