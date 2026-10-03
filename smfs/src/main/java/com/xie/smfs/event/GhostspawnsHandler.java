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
               if (world.method_8510() % 20L == 0L) {
                  for (PlayerEntity player : world.method_18456()) {
                     int currentDay = (int)(world.method_8510() / 24000L) + 1;
                     if (!world.field_9236
                        && currentDay == 5
                        && isNightTime(world)
                        && !isFirstNightCurseTriggered(player)
                        && !player.method_6059(ModEffects.DEAFNESS)) {
                        player.method_6092(new StatusEffectInstance(ModEffects.KNOCKING_CURSE, 7200, 0));
                        setFirstNightCurseTriggered(player, true);
                     }

                     if (!world.field_9236 && currentDay >= 5 && isNightTime(world) && isNearDoor(player)) {
                        triggerKnocking(player, world, player.method_24515());
                     }
                  }
               }
            }
         );
   }

   private static boolean isNightTime(World world) {
      long time = world.method_8532() % 24000L;
      return time >= 13000L && time <= 23000L;
   }

   private static boolean isNearDoor(PlayerEntity player) {
      BlockPos playerPos = player.method_24515();
      World world = player.method_37908();

      for (int x = -5; x <= 5; x++) {
         for (int y = -5; y <= 5; y++) {
            for (int z = -5; z <= 5; z++) {
               BlockPos checkPos = playerPos.method_10069(x, y, z);
               BlockState state = world.method_8320(checkPos);
               if (state.method_26204() instanceof DoorBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static void triggerKnocking(PlayerEntity player, World world, BlockPos pos) {
      if (RANDOM.nextInt(100) < 1 && !player.method_6059(ModEffects.KNOCKING_CURSE) && !player.method_6059(ModEffects.DEAFNESS)) {
         world.method_8396(null, pos, ModSounds.KNOCKING_SOUND, SoundCategory.field_15256, 0.5F, 1.0F);
         player.method_6092(new StatusEffectInstance(ModEffects.KNOCKING_CURSE, 7200, 0));
         player.method_7353(Text.method_43471("event.smfs.knocking_curse").method_27692(Formatting.field_1080), true);
      }
   }

   private static boolean isFirstNightCurseTriggered(PlayerEntity player) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      return playerData.method_10545("first_night_curse_triggered") && playerData.method_10577("first_night_curse_triggered");
   }

   private static void setFirstNightCurseTriggered(PlayerEntity player, boolean triggered) {
      NbtCompound playerData = PlayerEvents.getCachedData(player);
      playerData.method_10556("first_night_curse_triggered", triggered);
      PlayerEvents.saveDataToPlayer(player, playerData);
   }
}
