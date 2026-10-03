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
      new KeyBinding("key.smfs.ghostdomain.toggle", Type.field_1668, 75, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_TELEPORT = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.teleport", Type.field_1668, 86, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_TELEPORT_ENTITY = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.teleport_entity", Type.field_1668, 71, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_PAUSE_TIME = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.pause_time", Type.field_1668, 74, "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_REMOVE_BUFFS = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.remove_buffs", Type.field_1668, 78, "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch", Type.field_1668, 341, "category.smfs.general")
   );
   public static final KeyBinding SPECTATOR_RESPAWN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.spectator.respawn", Type.field_1668, 66, "category.smfs.general")
   );
   public static final KeyBinding OPEN_GHOST_CONTROL_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghost_control.open", Type.field_1668, 85, "category.smfs.general")
   );
   public static final KeyBinding OPEN_CREDITS_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.credits.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_CONFIG_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.config.open", Type.field_1668, 96, "category.smfs.general")
   );
   public static final KeyBinding ITEM_INTERACT = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.item.interact", Type.field_1668, 72, "category.smfs.general")
   );
   public static final KeyBinding OPEN_TAMING_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.taming.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_QUEST_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.quest.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_ROYAL_CURSE_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.royal_curse.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_GHOST_CHILD_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghost_child.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding OPEN_INFO_SCREEN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.info.open", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_0 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.0", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_1 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.1", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_2 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.2", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_3 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.3", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_4 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.4", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_5 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.5", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_6 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.6", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_7 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.7", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_8 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.8", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding SWITCH_MAIN_GHOST_TO_SLOT_9 = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.main_ghost.switch_slot.9", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_LEVEL_UP = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.level_up", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   public static final KeyBinding GHOST_DOMAIN_LEVEL_DOWN = KeyBindingHelper.registerKeyBinding(
      new KeyBinding("key.smfs.ghostdomain.level_down", Type.field_1668, InputUtil.field_16237.method_1444(), "category.smfs.general")
   );
   private static boolean vKeyPressed = false;
   private static boolean gKeyPressed = false;
   private static boolean jKeyPressed = false;
   private static boolean nKeyPressed = false;

   public static void register() {
      ClientTickEvents.END_CLIENT_TICK
         .register(
            (EndTick)client -> {
               while (TOGGLE_GHOST_DOMAIN.method_1436()) {
                  if (client.field_1724 != null) {
                     ClientPlayNetworking.send(ClientGhostDomainToggleC2SPacket.ID, PacketByteBufs.empty());
                  }
               }

               while (GHOST_DOMAIN_LEVEL_UP.method_1436()) {
                  if (client.field_1724 != null) {
                     ClientPlayNetworking.send(new Identifier("smfs", "ghost_domain_level_increase"), PacketByteBufs.empty());
                  }
               }

               while (GHOST_DOMAIN_LEVEL_DOWN.method_1436()) {
                  if (client.field_1724 != null) {
                     ClientPlayNetworking.send(new Identifier("smfs", "ghost_domain_level_decrease"), PacketByteBufs.empty());
                  }
               }

               if (GHOST_DOMAIN_TELEPORT.method_1434()) {
                  if (!vKeyPressed && client.field_1724 != null) {
                     vKeyPressed = true;
                     GhostSkillSystem.handleVKeySkill(client.field_1724);
                  }
               } else {
                  vKeyPressed = false;
               }

               if (GHOST_DOMAIN_TELEPORT_ENTITY.method_1434()) {
                  if (!gKeyPressed && client.field_1724 != null) {
                     gKeyPressed = true;
                     GhostSkillSystem.handleGKeySkill(client.field_1724);
                  }
               } else {
                  gKeyPressed = false;
               }

               if (GHOST_DOMAIN_PAUSE_TIME.method_1434()) {
                  if (!jKeyPressed && client.field_1724 != null) {
                     jKeyPressed = true;
                     GhostSkillSystem.handleJKeySkill(client.field_1724);
                  }
               } else {
                  jKeyPressed = false;
               }

               if (GHOST_DOMAIN_REMOVE_BUFFS.method_1434()) {
                  if (!nKeyPressed && client.field_1724 != null) {
                     nKeyPressed = true;
                     GhostSkillSystem.handleNKeySkill(client.field_1724);
                  }
               } else {
                  nKeyPressed = false;
               }

               while (SWITCH_MAIN_GHOST.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下Ctrl键，切换主鬼", client.field_1724.method_5477().getString());
                     ClientPlayNetworking.send(ClientSwitchMainGhostC2SPacket.ID, PacketByteBufs.empty());
                  }
               }

               while (SPECTATOR_RESPAWN.method_1436()) {
                  if (client.field_1724 != null) {
                     if (!client.field_1724.method_7325()) {
                        LOGGER.debug("玩家 {} 按下B键，但当前不在旁观者模式，无法转世", client.field_1724.method_5477().getString());
                        client.field_1724.method_7353(Text.method_43470("§c只能在亡魂状态使用转世功能"), true);
                        break;
                     }

                     if (client.field_1687 != null && client.field_1687.method_28104().method_152()) {
                        LOGGER.debug("玩家 {} 按下B键，但极限模式不支持转世功能", client.field_1724.method_5477().getString());
                        client.field_1724.method_7353(Text.method_43470("§c极限模式不支持转世功能"), true);
                        break;
                     }

                     if (!ModConfig.getInstance().instantReincarnation) {
                        LOGGER.debug("玩家 {} 按下B键，但立即转世已禁用，无法转世", client.field_1724.method_5477().getString());
                        client.field_1724.method_7353(Text.method_43470("§c立即转世功能已禁用，无法转世"), true);
                        break;
                     }

                     LOGGER.debug("玩家 {} 按下B键，请求从旁观者模式切换到生存模式", client.field_1724.method_5477().getString());
                     LOGGER.debug(
                        "当前状态 - 游戏模式: {}, 生命值: {}, 死亡: {}",
                        client.field_1724.method_7325() ? "SPECTATOR" : "OTHER",
                        client.field_1724.method_6032(),
                        client.field_1724.method_29504()
                     );
                     ClientModNetwork.sendToServer(new SwitchToSurvivalPacket());
                     LOGGER.debug("已发送 SwitchToSurvivalPacket 到服务端");
                  }
               }

               while (OPEN_GHOST_CONTROL_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下U键，打开灵异界面", client.field_1724.method_5477().getString());
                     ScreenOpener.openGhostControlScreen(client.field_1724);
                  }
               }

               while (OPEN_CREDITS_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下C键，打开制作团队界面", client.field_1724.method_5477().getString());
                     client.method_1507(new CreditsScreen());
                  }
               }

               while (OPEN_CONFIG_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     if (!client.field_1724.method_5687(2)) {
                        LOGGER.debug("玩家 {} 按下反引号键，权限不足，打开个人设置界面", client.field_1724.method_5477().getString());
                        client.method_1507(new PersonalConfigScreen(null));
                        break;
                     }

                     LOGGER.debug("玩家 {} 按下反引号键，打开设置界面", client.field_1724.method_5477().getString());
                     client.method_1507(new ConfigScreen(client.field_1755));
                  }
               }

               while (ITEM_INTERACT.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下H键，尝试物品交互", client.field_1724.method_5477().getString());
                     handleItemInteract(client.field_1724);
                  }
               }

               while (OPEN_TAMING_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下驾驭界面按键", client.field_1724.method_5477().getString());
                     ScreenOpener.openTamingScreen(client.field_1724);
                  }
               }

               while (OPEN_QUEST_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下任务界面按键", client.field_1724.method_5477().getString());
                     ScreenOpener.openQuestScreen(client.field_1724);
                  }
               }

               while (OPEN_ROYAL_CURSE_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下王家诅咒界面按键", client.field_1724.method_5477().getString());
                     ClientPlayNetworking.send(new Identifier("smfs", "open_royal_curse_screen"), PacketByteBufs.empty());
                  }
               }

               while (OPEN_GHOST_CHILD_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下鬼童界面按键", client.field_1724.method_5477().getString());
                     ClientPlayNetworking.send(new Identifier("smfs", "open_ghost_child_screen"), PacketByteBufs.empty());
                  }
               }

               while (OPEN_INFO_SCREEN.method_1436()) {
                  if (client.field_1724 != null) {
                     LOGGER.debug("玩家 {} 按下信息界面按键", client.field_1724.method_5477().getString());
                     ScreenOpener.openInfoScreen(client.field_1724);
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
      while (keyBinding.method_1436()) {
         if (client.field_1724 != null) {
            if (PlayerEvents.isGhostSlotOccupied(client.field_1724, slotIndex)) {
               LOGGER.debug("玩家 {} 按下按键切换主鬼到槽位 {}", client.field_1724.method_5477().getString(), slotIndex);
               PacketByteBuf buf = PacketByteBufs.create();
               buf.writeInt(slotIndex);
               ClientPlayNetworking.send(ClientSwitchMainGhostToSlotC2SPacket.ID, buf);
            } else {
               client.field_1724.method_7353(Text.method_43470("§c槽位 " + slotIndex + " 没有鬼魂"), true);
            }
         }
      }
   }

   private static void handleItemInteract(PlayerEntity player) {
      ItemStack stack = player.method_6047();
      if (stack.method_7960()) {
         if (player instanceof ClientPlayerEntity) {
            MinecraftClient.method_1551().method_1507(new PersonalConfigScreen(null));
         }
      } else if (stack.method_7909() instanceof FissuredSpearPurpleItem) {
         SpearModeConfigC2SPacket.sendRequestToServer();
      } else if (stack.method_7909() instanceof RustyOldBroadswordItem) {
         if (player instanceof ClientPlayerEntity) {
            MinecraftClient.method_1551().method_1507(new BroadswordConfigScreen(stack));
         }
      } else {
         player.method_7353(Text.method_43470("§c不可交互！"), true);
      }
   }
}
