package com.xie.smfs.event;

import com.xie.smfs.client.screen.BroadswordConfigScreen;
import com.xie.smfs.client.screen.ConfigScreen;
import com.xie.smfs.client.screen.CreditsScreen;
import com.xie.smfs.client.screen.PersonalConfigScreen;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.ModConfig;
import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.RustyOldBroadswordItem;
import com.xie.smfs.manager.GhostSkillSystem;
import com.xie.smfs.network.ClientModNetwork;
import com.xie.smfs.network.packets.common.c2s.SpearModeConfigC2SPacket;
import com.xie.smfs.network.packets.common.c2s.SwitchToSurvivalPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientGhostDomainToggleC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSwitchMainGhostC2SPacket;
import com.xie.smfs.network.packets.skills.c2s.ClientSwitchMainGhostToSlotC2SPacket;
import com.xie.smfs.util.ScreenOpener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyEventHandler {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs/KeyEventHandler");
   public static final KeyBinding TOGGLE_GHOST_DOMAIN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.toggle", Type.KEYSYM, 75, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_TELEPORT = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.teleport", Type.KEYSYM, 86, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_TELEPORT_ENTITY = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.teleport_entity", Type.KEYSYM, 71, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_PAUSE_TIME = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.pause_time", Type.KEYSYM, 74, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_REMOVE_BUFFS = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.remove_buffs", Type.KEYSYM, 78, "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch", Type.KEYSYM, 341, "category.smfs.general")
   );
   public static final KeyBinding SPECTATOR_RESPAWN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.spectator.respawn", Type.KEYSYM, 66, "category.smfs.general")
   );
   public static final KeyBinding OPEN_GHOST_CONTROL_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghost_control.open", Type.KEYSYM, 85, "category.smfs.general")
   );
   public static final KeyBinding OPEN_CREDITS_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.credits.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_CONFIG_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.config.open", Type.KEYSYM, 96, "category.smfs.general")
   );
   public static final KeyBinding ITEM_INTERACT = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.item.interact", Type.KEYSYM, 72, "category.smfs.general")
   );
   public static final KeyBinding OPEN_TAMING_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.taming.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_QUEST_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.quest.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_ROYAL_CURSE_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.royal_curse.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_GHOST_CHILD_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghost_child.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_INFO_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.info.open", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_0 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.0", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_1 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.1", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_2 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.2", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_3 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.3", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_4 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.4", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_5 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.5", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_6 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.6", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_7 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.7", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_8 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.8", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_9 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.9", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_LEVEL_UP = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.level_up", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_LEVEL_DOWN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.level_down", Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), "category.smfs.general")
   );
   private static boolean vKeyPressed = false;
   private static boolean gKeyPressed = false;
   private static boolean jKeyPressed = false;
   private static boolean nKeyPressed = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               while (TOGGLE_GHOST_DOMAIN.wasPressed()) {
                  if (client.player != null) {
                     ClientPlayNetworking.send(ClientGhostDomainToggleC2SPacket.ID, PacketByteBufs.empty());
                  }
               }

               while (GHOST_DOMAIN_LEVEL_UP.wasPressed()) {
                  if (client.player != null) {
                     ClientPlayNetworking.send(new Identifier("smfs", "ghost_domain_level_increase"), PacketByteBufs.empty());
                  }
               }

               while (GHOST_DOMAIN_LEVEL_DOWN.wasPressed()) {
                  if (client.player != null) {
                     ClientPlayNetworking.send(new Identifier("smfs", "ghost_domain_level_decrease"), PacketByteBufs.empty());
                  }
               }

               if (GHOST_DOMAIN_TELEPORT.isPressed()) {
                  if (!vKeyPressed && client.player != null) {
                     vKeyPressed = true;
                     GhostSkillSystem.handleVKeySkill(client.player);
                  }
               } else {
                  vKeyPressed = false;
               }

               if (GHOST_DOMAIN_TELEPORT_ENTITY.isPressed()) {
                  if (!gKeyPressed && client.player != null) {
                     gKeyPressed = true;
                     GhostSkillSystem.handleGKeySkill(client.player);
                  }
               } else {
                  gKeyPressed = false;
               }

               if (GHOST_DOMAIN_PAUSE_TIME.isPressed()) {
                  if (!jKeyPressed && client.player != null) {
                     jKeyPressed = true;
                     GhostSkillSystem.handleJKeySkill(client.player);
                  }
               } else {
                  jKeyPressed = false;
               }

               if (GHOST_DOMAIN_REMOVE_BUFFS.isPressed()) {
                  if (!nKeyPressed && client.player != null) {
                     nKeyPressed = true;
                     GhostSkillSystem.handleNKeySkill(client.player);
                  }
               } else {
                  nKeyPressed = false;
               }

               while (SWITCH_MAIN_GHOST.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下Ctrl键，切换主鬼", client.player.getName().getString());
                     ClientPlayNetworking.send(ClientSwitchMainGhostC2SPacket.ID, PacketByteBufs.empty());
                  }
               }

               while (SPECTATOR_RESPAWN.wasPressed()) {
                  if (client.player != null) {
                     if (!client.player.isSpectator()) {
                        LOGGER.debug("玩家 {} 按下B键，但当前不在旁观者模式，无法转世", client.player.getName().getString());
                        client.player.sendMessage(Text.literal("§c只能在亡魂状态使用转世功能"), true);
                        break;
                     }

                     if (client.world != null && client.world.getLevelProperties().isHardcore()) {
                        LOGGER.debug("玩家 {} 按下B键，但极限模式不支持转世功能", client.player.getName().getString());
                        client.player.sendMessage(Text.literal("§c极限模式不支持转世功能"), true);
                        break;
                     }

                     if (!ModConfig.getInstance().instantReincarnation) {
                        LOGGER.debug("玩家 {} 按下B键，但立即转世已禁用，无法转世", client.player.getName().getString());
                        client.player.sendMessage(Text.literal("§c立即转世功能已禁用，无法转世"), true);
                        break;
                     }

                     LOGGER.debug("玩家 {} 按下B键，请求从旁观者模式切换到生存模式", client.player.getName().getString());
                     LOGGER.debug(
                        "当前状态 - 游戏模式: {}, 生命值: {}, 死亡: {}",
                        client.player.isSpectator() ? "SPECTATOR" : "OTHER",
                        client.player.getHealth(),
                        client.player.isDead()
                     );
                     ClientModNetwork.sendToServer(new SwitchToSurvivalPacket());
                     LOGGER.debug("已发送 SwitchToSurvivalPacket 到服务端");
                  }
               }

               while (OPEN_GHOST_CONTROL_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下U键，打开灵异界面", client.player.getName().getString());
                     ScreenOpener.openGhostControlScreen(client.player);
                  }
               }

               while (OPEN_CREDITS_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下C键，打开制作团队界面", client.player.getName().getString());
                     client.setScreen(new CreditsScreen());
                  }
               }

               while (OPEN_CONFIG_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     if (!client.player.hasPermissionLevel(2)) {
                        LOGGER.debug("玩家 {} 按下反引号键，权限不足，打开个人设置界面", client.player.getName().getString());
                        client.setScreen(new PersonalConfigScreen(null));
                        break;
                     }

                     LOGGER.debug("玩家 {} 按下反引号键，打开设置界面", client.player.getName().getString());
                     client.setScreen(new ConfigScreen(client.currentScreen));
                  }
               }

               while (ITEM_INTERACT.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下H键，尝试物品交互", client.player.getName().getString());
                     handleItemInteract(client.player);
                  }
               }

               while (OPEN_TAMING_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下驾驭界面按键", client.player.getName().getString());
                     ScreenOpener.openTamingScreen(client.player);
                  }
               }

               while (OPEN_QUEST_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下任务界面按键", client.player.getName().getString());
                     ScreenOpener.openQuestScreen(client.player);
                  }
               }

               while (OPEN_ROYAL_CURSE_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下王家诅咒界面按键", client.player.getName().getString());
                     ClientPlayNetworking.send(new Identifier("smfs", "open_royal_curse_screen"), PacketByteBufs.empty());
                  }
               }

               while (OPEN_GHOST_CHILD_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下鬼童界面按键", client.player.getName().getString());
                     ClientPlayNetworking.send(new Identifier("smfs", "open_ghost_child_screen"), PacketByteBufs.empty());
                  }
               }

               while (OPEN_INFO_SCREEN.wasPressed()) {
                  if (client.player != null) {
                     LOGGER.debug("玩家 {} 按下信息界面按键", client.player.getName().getString());
                     ScreenOpener.openInfoScreen(client.player);
                  }
               }

               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_0, 0);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_1, 1);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_2, 2);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_3, 3);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_4, 4);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_5, 5);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_6, 6);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_7, 7);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_8, 8);
               handleSwitchMainGhostToSlot(client, SWITCH_MAIN_GHOST_TO_SLOT_9, 9);
            }
         );
   }

   private static void handleSwitchMainGhostToSlot(MinecraftClient client, KeyBinding keyBinding, int slotIndex) {
      while (keyBinding.wasPressed()) {
         if (client.player != null) {
            if (PlayerEvents.isGhostSlotOccupied(client.player, slotIndex)) {
               LOGGER.debug("玩家 {} 按下按键切换主鬼到槽位 {}", client.player.getName().getString(), slotIndex);
               PacketByteBuf buf = PacketByteBufs.create();
               buf.writeInt(slotIndex);
               ClientPlayNetworking.send(ClientSwitchMainGhostToSlotC2SPacket.ID, buf);
            } else {
               client.player.sendMessage(Text.literal("§c槽位 " + slotIndex + " 没有鬼魂"), true);
            }
         }
      }
   }

   private static void handleItemInteract(PlayerEntity player) {
      ItemStack stack = player.getMainHandStack();
      if (stack.isEmpty()) {
         if (player instanceof ClientPlayerEntity) {
            MinecraftClient.getInstance().setScreen(new PersonalConfigScreen(null));
         }
      } else if (stack.getItem() instanceof FissuredSpearPurpleItem) {
         SpearModeConfigC2SPacket.sendRequestToServer();
      } else if (stack.getItem() instanceof RustyOldBroadswordItem) {
         if (player instanceof ClientPlayerEntity) {
            MinecraftClient.getInstance().setScreen(new BroadswordConfigScreen(stack));
         }
      } else {
         player.sendMessage(Text.literal("§c不可交互！"), true);
      }
   }
}
