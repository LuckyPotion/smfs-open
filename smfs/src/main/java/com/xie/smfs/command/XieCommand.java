package com.xie.smfs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.xie.smfs.Smfs;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.data.GhostChildData;
import com.xie.smfs.data.PlayerGhostChildManager;
import com.xie.smfs.data.PlayerRoyalCurseManager;
import com.xie.smfs.faction.FactionManager;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.item.BaseGhostEyeItem;
import com.xie.smfs.manager.AdvancementManager;
import com.xie.smfs.network.packets.ui.s2c.ResetPlayNoticeS2CPacket;
import com.xie.smfs.util.GhostUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class XieCommand {
   public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                   "xie"
                                                )
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                                           "gui"
                                                                        )
                                                                        .then(CommandManager.literal("ghost").executes(OpenGhostScreenCommand::execute)))
                                                                     .then(CommandManager.literal("taming").executes(OpenTamingScreenCommand::execute)))
                                                                  .then(CommandManager.literal("credits").executes(OpenCreditsCommand::execute)))
                                                               .then(
                                                                  CommandManager.literal("ghostchild")
                                                                     .executes(OpenGhostChildCultivationScreenCommand::execute)
                                                               ))
                                                            .then(CommandManager.literal("royalcurse").executes(OpenRoyalCurseScreenCommand::execute)))
                                                         .then(CommandManager.literal("feed").executes(OpenGhostChildFeedScreenCommand::execute)))
                                                      .then(CommandManager.literal("quest").executes(OpenQuestScreenCommand::execute))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                                  "servant"
                                                               )
                                                               .requires(source -> source.hasPermissionLevel(0)))
                                                            .then(
                                                               CommandManager.literal("summon")
                                                                  .then(CommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
                                                                     int index = IntegerArgumentType.getInteger(context, "index");
                                                                     PlayerRoyalCurseManager.summonServant(
                                                                        ((ServerCommandSource)context.getSource()).getPlayer(), index
                                                                     );
                                                                     return 1;
                                                                  }))
                                                            ))
                                                         .then(
                                                            CommandManager.literal("recall")
                                                               .then(CommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
                                                                  int index = IntegerArgumentType.getInteger(context, "index");
                                                                  PlayerRoyalCurseManager.recallServant(
                                                                     ((ServerCommandSource)context.getSource()).getPlayer(), index
                                                                  );
                                                                  return 1;
                                                               }))
                                                         ))
                                                      .then(CommandManager.literal("summon_all").executes(context -> {
                                                         PlayerRoyalCurseManager.summonAllServants(((ServerCommandSource)context.getSource()).getPlayer());
                                                         return 1;
                                                      })))
                                                   .then(CommandManager.literal("recall_all").executes(context -> {
                                                      PlayerRoyalCurseManager.recallAllServants(((ServerCommandSource)context.getSource()).getPlayer());
                                                      return 1;
                                                   }))
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("playerghost")
                                                         .requires(source -> source.hasPermissionLevel(2)))
                                                      .then(
                                                         CommandManager.literal("info")
                                                            .then(
                                                               CommandManager.argument("ghost", EntityArgumentType.entity())
                                                                  .executes(PlayerGhostCommand::getGhostInfo)
                                                            )
                                                      ))
                                                   .then(
                                                      CommandManager.literal("retrieve")
                                                         .then(
                                                            CommandManager.argument("ghost", EntityArgumentType.entity())
                                                               .executes(PlayerGhostCommand::retrieveCoffinNail)
                                                         )
                                                   ))
                                                .then(
                                                   CommandManager.literal("suppress")
                                                      .then(
                                                         CommandManager.argument("ghost", EntityArgumentType.entity())
                                                            .executes(PlayerGhostCommand::suppressGhost)
                                                      )
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                               "quest"
                                                            )
                                                            .then(CommandManager.literal("list").executes(QuestCommand::listQuests)))
                                                         .then(
                                                            CommandManager.literal("start")
                                                               .then(
                                                                  CommandManager.argument("questId", StringArgumentType.string())
                                                                     .executes(QuestCommand::startQuest)
                                                               )
                                                         ))
                                                      .then(
                                                         CommandManager.literal("abandon")
                                                            .then(
                                                               CommandManager.argument("questId", StringArgumentType.string())
                                                                  .executes(QuestCommand::abandonQuest)
                                                            )
                                                      ))
                                                   .then(
                                                      CommandManager.literal("info")
                                                         .then(
                                                            CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::questInfo)
                                                         )
                                                   ))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("complete")
                                                            .requires(source -> source.hasPermissionLevel(2)))
                                                         .executes(QuestCommand::completeAllActiveQuests))
                                                      .then(
                                                         CommandManager.argument("questId", StringArgumentType.string()).executes(QuestCommand::completeQuest)
                                                      )
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)CommandManager.literal("reset").requires(source -> source.hasPermissionLevel(2)))
                                                   .executes(QuestCommand::resetAllQuests)
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("book")
                                                .requires(source -> source.hasPermissionLevel(2)))
                                             .then(
                                                CommandManager.literal("give")
                                                   .then(
                                                      CommandManager.argument("player", EntityArgumentType.player())
                                                         .then(
                                                            CommandManager.argument("bookId", StringArgumentType.string())
                                                               .executes(
                                                                  context -> BookCommand.giveBook(
                                                                     context,
                                                                     EntityArgumentType.getPlayer(context, "player"),
                                                                     StringArgumentType.getString(context, "bookId")
                                                                  )
                                                               )
                                                         )
                                                   )
                                             ))
                                          .then(CommandManager.literal("list").executes(BookCommand::listAvailableBooks))
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("timer")
                                             .requires(source -> source.hasPermissionLevel(2)))
                                          .then(
                                             ((LiteralArgumentBuilder)CommandManager.literal("ghost_timer")
                                                   .executes(ctx -> GhostTimerCommand.execute(ctx, "minecraft:overworld")))
                                                .then(
                                                   CommandManager.argument("dimension", StringArgumentType.greedyString())
                                                      .executes(ctx -> GhostTimerCommand.execute(ctx, StringArgumentType.getString(ctx, "dimension")))
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)CommandManager.literal("complete_timers").executes(CompleteGhostTimersCommand::execute))
                                             .then(
                                                CommandManager.argument("dimension", StringArgumentType.greedyString())
                                                   .executes(ctx -> CompleteGhostTimersCommand.execute(ctx, StringArgumentType.getString(ctx, "dimension")))
                                             )
                                       )
                                 ))
                              .then(CommandManager.literal("locked_ghosts").executes(LockedGhostTypesCommand::execute)))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                "unlock"
                                             )
                                             .requires(source -> source.hasPermissionLevel(2)))
                                          .then(CommandManager.literal("aberration").executes(UnlockCommand::executeAberration)))
                                       .then(CommandManager.literal("slots").executes(UnlockCommand::executeSlots)))
                                    .then(CommandManager.literal("fusion").executes(UnlockCommand::executeFusion)))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("advancement")
                                                .then(CommandManager.literal("test").executes(AdvancementTestCommand::executeTest)))
                                             .then(CommandManager.literal("check").executes(AdvancementTestCommand::executeCheck)))
                                          .then(CommandManager.literal("info").executes(AdvancementTestCommand::executeInfo)))
                                       .then(
                                          ((LiteralArgumentBuilder)CommandManager.literal("force").requires(source -> source.hasPermissionLevel(2)))
                                             .executes(AdvancementTestCommand::executeForce)
                                       )
                                 )
                           ))
                        .then(
                           CommandManager.literal("ghost_swap")
                              .then(
                                 ((RequiredArgumentBuilder)CommandManager.argument("ghostType", IdentifierArgumentType.identifier())
                                       .executes(XieCommand::executeGhostSwap))
                                    .then(CommandManager.argument("slotIndex", IntegerArgumentType.integer(0, 9)).executes(XieCommand::executeGhostSwap))
                              )
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("faction")
                                       .requires(source -> source.hasPermissionLevel(2)))
                                    .then(
                                       CommandManager.literal("add_reputation")
                                          .then(CommandManager.argument("amount", IntegerArgumentType.integer(1)).executes(context -> {
                                             ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                                             if (player == null) {
                                                ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                                                return 0;
                                             } else {
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                FactionManager.addReputation(player, amount);
                                                int newRep = FactionManager.getReputation(player);
                                                player.sendMessage(Text.literal("§a增加了 " + amount + " 点声望，当前声望：" + newRep), false);
                                                return 1;
                                             }
                                          }))
                                    ))
                                 .then(
                                    CommandManager.literal("set_reputation")
                                       .then(CommandManager.argument("value", IntegerArgumentType.integer(0)).executes(context -> {
                                          ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                                          if (player == null) {
                                             ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                                             return 0;
                                          } else {
                                             int value = IntegerArgumentType.getInteger(context, "value");
                                             FactionManager.setReputation(player, value);
                                             player.sendMessage(Text.literal("§a声望已设置为 " + value), false);
                                             return 1;
                                          }
                                       }))
                                 ))
                              .then(
                                 CommandManager.literal("set_codename").then(CommandManager.argument("name", StringArgumentType.string()).executes(context -> {
                                    ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                                    if (player == null) {
                                       ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                                       return 0;
                                    } else {
                                       String name = StringArgumentType.getString(context, "name");
                                       if (name.length() > 16) {
                                          player.sendMessage(Text.literal("§c代号长度不能超过16个字符！"), false);
                                          return 0;
                                       } else {
                                          FactionManager.setCodename(player, name);
                                          player.sendMessage(Text.literal("§a代号已设置为：" + name), false);
                                          return 1;
                                       }
                                    }
                                 }))
                              ))
                           .then(
                              CommandManager.literal("info")
                                 .executes(
                                    context -> {
                                       ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                                       if (player == null) {
                                          ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                                          return 0;
                                       } else {
                                          PlayerFaction faction = FactionManager.getFaction(player);
                                          int rep = FactionManager.getReputation(player);
                                          int salary = FactionManager.getGoldSalary(player);
                                          String codename = FactionManager.getCodename(player);
                                          String displayCodename = codename != null && !codename.isEmpty() ? codename : "暂无";
                                          player.sendMessage(
                                             Text.literal(
                                                "§e阵营: " + faction.getDisplayName() + " | 声望: " + rep + " | 薪资: " + salary + " | 代号: " + displayCodename
                                             ),
                                             false
                                          );
                                          return 1;
                                       }
                                    }
                                 )
                           )
                     ))
                  .then(CommandManager.literal("tutorial").then(CommandManager.literal("reset").executes(context -> {
                     ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                     if (player != null) {
                        ResetPlayNoticeS2CPacket.send(player);
                        player.sendMessage(Text.literal("教程已重置"), false);
                     }

                     return 1;
                  }))))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ghostitem").requires(source -> source.hasPermissionLevel(2)))
                        .then(
                           CommandManager.literal("set")
                              .then(
                                 CommandManager.argument("revivalDegree", IntegerArgumentType.integer(0, 1000))
                                    .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 10)).executes(context -> {
                                       ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                                       int revivalDegree = IntegerArgumentType.getInteger(context, "revivalDegree");
                                       int level = IntegerArgumentType.getInteger(context, "level");
                                       return GhostItemCommand.executeSet(context, player, revivalDegree, level);
                                    }))
                              )
                        ))
                     .then(CommandManager.literal("get").executes(context -> {
                        ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                        return GhostItemCommand.executeGet(context, player);
                     }))
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("shard")
                           .requires(source -> source.hasPermissionLevel(2)))
                        .executes(context -> {
                           ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                           if (player == null) {
                              ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                              return 0;
                           } else {
                              ItemStack stack = player.getMainHandStack();
                              if (!(stack.getItem() instanceof BaseGhostEyeItem)) {
                                 ((ServerCommandSource)context.getSource()).sendError(Text.literal("手上拿的不是驾驭物品"));
                                 return 0;
                              } else {
                                 boolean current = BaseGhostEyeItem.isShard(stack);
                                 BaseGhostEyeItem.setShard(stack, !current);
                                 String state = !current ? "§a碎片模式" : "§c正常模式";
                                 ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("已切换为" + state), true);
                                 return 1;
                              }
                           }
                        }))
                     .then(CommandManager.literal("set").then(CommandManager.argument("state", BoolArgumentType.bool()).executes(context -> {
                        ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                        if (player == null) {
                           ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                           return 0;
                        } else {
                           boolean state = BoolArgumentType.getBool(context, "state");
                           ItemStack stack = player.getMainHandStack();
                           if (!(stack.getItem() instanceof BaseGhostEyeItem)) {
                              ((ServerCommandSource)context.getSource()).sendError(Text.literal("手上拿的不是驾驭物品"));
                              return 0;
                           } else {
                              BaseGhostEyeItem.setShard(stack, state);
                              String stateText = state ? "§a碎片模式" : "§c正常模式";
                              ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("已设置为" + stateText), true);
                              return 1;
                           }
                        }
                     }))))
                  .then(CommandManager.literal("get").executes(context -> {
                     ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
                     if (player == null) {
                        ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
                        return 0;
                     } else {
                        ItemStack stack = player.getMainHandStack();
                        if (stack.getItem() instanceof BaseGhostEyeItem item) {
                           boolean isShard = BaseGhostEyeItem.isShard(stack);
                           String stateText = isShard ? "§a是碎片" : "§c不是碎片";
                           ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.literal("类型：" + item.getGhostType() + "，状态：" + stateText), false);
                           return 1;
                        } else {
                           ((ServerCommandSource)context.getSource()).sendError(Text.literal("手上拿的不是驾驭物品"));
                           return 0;
                        }
                     }
                  }))
            )
      );
   }

   public static int executeGhostSwap(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).getPlayer();
      if (player == null) {
         ((ServerCommandSource)context.getSource()).sendError(Text.literal("该指令只能由玩家执行"));
         return 0;
      }

      try {
         Identifier ghostTypeId = IdentifierArgumentType.getIdentifier(context, "ghostType");
         String ghostType = ghostTypeId.toString();
         String simpleGhostType = ghostType;
         if (ghostType.contains(":")) {
            simpleGhostType = ghostType.substring(ghostType.indexOf(":") + 1);
         }

         GhostChildData data = PlayerGhostChildManager.getGhostChildData(player);
         if (data != null && data.getFedGhostTypes().contains(simpleGhostType)) {
            ItemStack newGhostItem = GhostUtils.createTamedItem(simpleGhostType);
            if (newGhostItem.isEmpty()) {
               player.sendMessage(Text.literal("§c无效的鬼类型"), true);
               return 0;
            }

            if (AdvancementManager.hasAdvancement(player, "smfs:become_god")) {
               NbtCompound nbt = newGhostItem.getOrCreateNbt();
               nbt.putInt("StoredLevel", 10);
               nbt.putInt("StoredRevivalDegree", 0);
            }

            boolean hasSlotIndex = false;

            int slotIndex;
            try {
               slotIndex = IntegerArgumentType.getInteger(context, "slotIndex");
               hasSlotIndex = true;
            } catch (Exception e) {
               slotIndex = PlayerEvents.getUnlockedGhostSlot(player);
               if (slotIndex == -1) {
                  player.sendMessage(Text.literal("§c没有可用的驾驭槽位"), true);
                  return 0;
               }
            }

            if (hasSlotIndex) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(player);
               String slotKey = "Slot" + slotIndex;
               if (!ghostSlots.contains(slotKey) || !ghostSlots.getCompound(slotKey).getBoolean("unlocked")) {
                  player.sendMessage(Text.literal("§c该槽位未解锁！"), true);
                  return 0;
               }
            }

            if (hasSlotIndex) {
               ItemStack oldGhostItem = PlayerEvents.getGhostSlotItem(player, slotIndex);
               if (!oldGhostItem.isEmpty() && !oldGhostItem.isOf(Items.AIR) && oldGhostItem.getItem() instanceof BaseGhostEyeItem oldItem) {
                  String oldGhostType = oldItem.getGhostType();
                  if (oldGhostType != null && !oldGhostType.isEmpty() && !oldGhostType.startsWith("minecraft:")) {
                     data.getFedGhostTypes().add(oldGhostType);
                  }
               }

               PlayerEvents.setGhostSlotData(player, slotIndex, newGhostItem);
               PlayerEvents.validateGhostSlots(player);
               data.getFedGhostTypes().remove(simpleGhostType);
               PlayerGhostChildManager.saveGhostChildData(player, data);
               String ghostName = GhostUtils.getGhostDisplayName(simpleGhostType);
               player.sendMessage(Text.literal("§a成功将 " + ghostName + " 放入槽位 " + slotIndex + "！"), true);
            } else {
               PlayerEvents.setGhostSlotData(player, slotIndex, newGhostItem);
               PlayerEvents.validateGhostSlots(player);
               data.getFedGhostTypes().remove(simpleGhostType);
               PlayerGhostChildManager.saveGhostChildData(player, data);
               String ghostName = GhostUtils.getGhostDisplayName(simpleGhostType);
               player.sendMessage(Text.literal("§a成功将 " + ghostName + " 加入驾驭槽位 " + slotIndex + "！"), true);
            }

            return 1;
         } else {
            player.sendMessage(Text.literal("§c没有找到该鬼类型"), true);
            return 0;
         }
      } catch (Exception e) {
         Smfs.LOGGER.error("交换鬼失败", e);
         player.sendMessage(Text.literal("§c交换失败: " + e.getMessage()), true);
         return 0;
      }
   }
}
