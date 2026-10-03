package com.xie.smfs.event;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.Random;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndWorldTick;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GhostspawnsHandler {
   private static final Random RANDOM = new Random();
   private static final int KNOCK_CHANCE = 1;
   private static final int CURSE_DURATION = 7200;
   private static final int SEARCH_RADIUS = 5;
   private static final int KNOCK_DELAY = 10;

   public static void register() {
      ServerTickEvents.END_WORLD_TICK
         .register(
            (EndWorldTick)world -> {
               if (world.getTime() % 20L == 0L) {
                  for (PlayerEntity player : world.getPlayers()) {
                     int currentDay = (int)(world.getTime() / 24000L) + 1;
                     if (!world.isClient
                        && currentDay == 5
                        && isNightTime(world)
                        && !isFirstNightCurseTriggered(player)
                        && !player.hasStatusEffect(ModEffects.DEAFNESS)) {
                        player.addStatusEffect(new StatusEffectInstance(ModEffects.KNOCKING_CURSE, 7200, 0));
                        setFirstNightCurseTriggered(player, true);
                     }

                     if (!world.isClient && currentDay >= 5 && isNightTime(world) && isNearDoor(player)) {
                        triggerKnocking(player, world, player.getBlockPos());
                     }
                  }
               }
            }
         );
   }

   private static boolean isNightTime(World world) {
      long time = world.getTimeOfDay() % 24000L;
      return time >= 13000L && time <= 23000L;
   }

   private static boolean isNearDoor(PlayerEntity player) {
      BlockPos playerPos = player.getBlockPos();
      World world = player.getWorld();

      for (int x = -5; x <= 5; x++) {
         for (int y = -5; y <= 5; y++) {
            for (int z = -5; z <= 5; z++) {
               BlockPos checkPos = playerPos.add(x, y, z);
               BlockState state = world.getBlockState(checkPos);
               if (state.getBlock() instanceof DoorBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void triggerKnocking(PlayerEntity player, World world, BlockPos pos) {
      if (RANDOM.nextInt(100) < 1 && !player.hasStatusEffect(ModEffects.KNOCKING_CURSE) && !player.hasStatusEffect(ModEffects.DEAFNESS)) {
         world.playSound(null, pos, ModSounds.KNOCKING_SOUND, SoundCategory.AMBIENT, 0.5F, 1.0F);
         player.addStatusEffect(new StatusEffectInstance(ModEffects.KNOCKING_CURSE, 7200, 0));
         player.sendMessage(Text.translatable("event.smfs.knocking_curse").formatted(Formatting.GRAY), true);
      }
   }

   private static boolean isFirstNightCurseTriggered(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      return playerData.contains("first_night_curse_triggered") && playerData.getBoolean("first_night_curse_triggered");
   }

   private static void setFirstNightCurseTriggered(PlayerEntity player, boolean triggered) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      playerData.putBoolean("first_night_curse_triggered", triggered);
      PlayerEvents.saveDataToPlayer(player, playerData);
   }
}
