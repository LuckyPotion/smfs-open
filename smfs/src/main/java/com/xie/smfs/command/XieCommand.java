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
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                   "xie"
                                                )
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                                           "gui"
                                                                        )
                                                                        .then(CommandManager.method_9247("ghost").executes(OpenGhostScreenCommand::execute)))
                                                                     .then(CommandManager.method_9247("taming").executes(OpenTamingScreenCommand::execute)))
                                                                  .then(CommandManager.method_9247("credits").executes(OpenCreditsCommand::execute)))
                                                               .then(
                                                                  CommandManager.method_9247("ghostchild")
                                                                     .executes(OpenGhostChildCultivationScreenCommand::execute)
                                                               ))
                                                            .then(CommandManager.method_9247("royalcurse").executes(OpenRoyalCurseScreenCommand::execute)))
                                                         .then(CommandManager.method_9247("feed").executes(OpenGhostChildFeedScreenCommand::execute)))
                                                      .then(CommandManager.method_9247("quest").executes(OpenQuestScreenCommand::execute))
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                                  "servant"
                                                               )
                                                               .requires(source -> source.method_9259(0)))
                                                            .then(
                                                               CommandManager.method_9247("summon")
                                                                  .then(
                                                                     CommandManager.method_9244("index", IntegerArgumentType.integer(0)).executes(context -> {
                                                                        int index = IntegerArgumentType.getInteger(context, "index");
                                                                        PlayerRoyalCurseManager.summonServant(
                                                                           ((ServerCommandSource)context.getSource()).method_44023(), index
                                                                        );
                                                                        return 1;
                                                                     })
                                                                  )
                                                            ))
                                                         .then(
                                                            CommandManager.method_9247("recall")
                                                               .then(CommandManager.method_9244("index", IntegerArgumentType.integer(0)).executes(context -> {
                                                                  int index = IntegerArgumentType.getInteger(context, "index");
                                                                  PlayerRoyalCurseManager.recallServant(
                                                                     ((ServerCommandSource)context.getSource()).method_44023(), index
                                                                  );
                                                                  return 1;
                                                               }))
                                                         ))
                                                      .then(CommandManager.method_9247("summon_all").executes(context -> {
                                                         PlayerRoyalCurseManager.summonAllServants(((ServerCommandSource)context.getSource()).method_44023());
                                                         return 1;
                                                      })))
                                                   .then(CommandManager.method_9247("recall_all").executes(context -> {
                                                      PlayerRoyalCurseManager.recallAllServants(((ServerCommandSource)context.getSource()).method_44023());
                                                      return 1;
                                                   }))
                                             ))
                                          .then(
                                             ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                            "playerghost"
                                                         )
                                                         .requires(source -> source.method_9259(2)))
                                                      .then(
                                                         CommandManager.method_9247("info")
                                                            .then(
                                                               CommandManager.method_9244("ghost", EntityArgumentType.method_9309())
                                                                  .executes(PlayerGhostCommand::getGhostInfo)
                                                            )
                                                      ))
                                                   .then(
                                                      CommandManager.method_9247("retrieve")
                                                         .then(
                                                            CommandManager.method_9244("ghost", EntityArgumentType.method_9309())
                                                               .executes(PlayerGhostCommand::retrieveCoffinNail)
                                                         )
                                                   ))
                                                .then(
                                                   CommandManager.method_9247("suppress")
                                                      .then(
                                                         CommandManager.method_9244("ghost", EntityArgumentType.method_9309())
                                                            .executes(PlayerGhostCommand::suppressGhost)
                                                      )
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                               "quest"
                                                            )
                                                            .then(CommandManager.method_9247("list").executes(QuestCommand::listQuests)))
                                                         .then(
                                                            CommandManager.method_9247("start")
                                                               .then(
                                                                  CommandManager.method_9244("questId", StringArgumentType.string())
                                                                     .executes(QuestCommand::startQuest)
                                                               )
                                                         ))
                                                      .then(
                                                         CommandManager.method_9247("abandon")
                                                            .then(
                                                               CommandManager.method_9244("questId", StringArgumentType.string())
                                                                  .executes(QuestCommand::abandonQuest)
                                                            )
                                                      ))
                                                   .then(
                                                      CommandManager.method_9247("info")
                                                         .then(
                                                            CommandManager.method_9244("questId", StringArgumentType.string())
                                                               .executes(QuestCommand::questInfo)
                                                         )
                                                   ))
                                                .then(
                                                   ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("complete")
                                                            .requires(source -> source.method_9259(2)))
                                                         .executes(QuestCommand::completeAllActiveQuests))
                                                      .then(
                                                         CommandManager.method_9244("questId", StringArgumentType.string())
                                                            .executes(QuestCommand::completeQuest)
                                                      )
                                                ))
                                             .then(
                                                ((LiteralArgumentBuilder)CommandManager.method_9247("reset").requires(source -> source.method_9259(2)))
                                                   .executes(QuestCommand::resetAllQuests)
                                             )
                                       ))
                                    .then(
                                       ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("book")
                                                .requires(source -> source.method_9259(2)))
                                             .then(
                                                CommandManager.method_9247("give")
                                                   .then(
                                                      CommandManager.method_9244("player", EntityArgumentType.method_9305())
                                                         .then(
                                                            CommandManager.method_9244("bookId", StringArgumentType.string())
                                                               .executes(
                                                                  context -> BookCommand.giveBook(
                                                                     context,
                                                                     EntityArgumentType.method_9315(context, "player"),
                                                                     StringArgumentType.getString(context, "bookId")
                                                                  )
                                                               )
                                                         )
                                                   )
                                             ))
                                          .then(CommandManager.method_9247("list").executes(BookCommand::listAvailableBooks))
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("timer")
                                             .requires(source -> source.method_9259(2)))
                                          .then(
                                             ((LiteralArgumentBuilder)CommandManager.method_9247("ghost_timer")
                                                   .executes(ctx -> GhostTimerCommand.execute(ctx, "minecraft:overworld")))
                                                .then(
                                                   CommandManager.method_9244("dimension", StringArgumentType.greedyString())
                                                      .executes(ctx -> GhostTimerCommand.execute(ctx, StringArgumentType.getString(ctx, "dimension")))
                                                )
                                          ))
                                       .then(
                                          ((LiteralArgumentBuilder)CommandManager.method_9247("complete_timers").executes(CompleteGhostTimersCommand::execute))
                                             .then(
                                                CommandManager.method_9244("dimension", StringArgumentType.greedyString())
                                                   .executes(ctx -> CompleteGhostTimersCommand.execute(ctx, StringArgumentType.getString(ctx, "dimension")))
                                             )
                                       )
                                 ))
                              .then(CommandManager.method_9247("locked_ghosts").executes(LockedGhostTypesCommand::execute)))
                           .then(
                              ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                                "unlock"
                                             )
                                             .requires(source -> source.method_9259(2)))
                                          .then(CommandManager.method_9247("aberration").executes(UnlockCommand::executeAberration)))
                                       .then(CommandManager.method_9247("slots").executes(UnlockCommand::executeSlots)))
                                    .then(CommandManager.method_9247("fusion").executes(UnlockCommand::executeFusion)))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("advancement")
                                                .then(CommandManager.method_9247("test").executes(AdvancementTestCommand::executeTest)))
                                             .then(CommandManager.method_9247("check").executes(AdvancementTestCommand::executeCheck)))
                                          .then(CommandManager.method_9247("info").executes(AdvancementTestCommand::executeInfo)))
                                       .then(
                                          ((LiteralArgumentBuilder)CommandManager.method_9247("force").requires(source -> source.method_9259(2)))
                                             .executes(AdvancementTestCommand::executeForce)
                                       )
                                 )
                           ))
                        .then(
                           CommandManager.method_9247("ghost_swap")
                              .then(
                                 ((RequiredArgumentBuilder)CommandManager.method_9244("ghostType", IdentifierArgumentType.method_9441())
                                       .executes(XieCommand::executeGhostSwap))
                                    .then(CommandManager.method_9244("slotIndex", IntegerArgumentType.integer(0, 9)).executes(XieCommand::executeGhostSwap))
                              )
                        ))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247(
                                          "faction"
                                       )
                                       .requires(source -> source.method_9259(2)))
                                    .then(
                                       CommandManager.method_9247("add_reputation")
                                          .then(CommandManager.method_9244("amount", IntegerArgumentType.integer(1)).executes(context -> {
                                             ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                                             if (player == null) {
                                                ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                                                return 0;
                                             } else {
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                FactionManager.addReputation(player, amount);
                                                int newRep = FactionManager.getReputation(player);
                                                player.method_7353(Text.method_43470("§a增加了 " + amount + " 点声望，当前声望：" + newRep), false);
                                                return 1;
                                             }
                                          }))
                                    ))
                                 .then(
                                    CommandManager.method_9247("set_reputation")
                                       .then(CommandManager.method_9244("value", IntegerArgumentType.integer(0)).executes(context -> {
                                          ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                                          if (player == null) {
                                             ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                                             return 0;
                                          } else {
                                             int value = IntegerArgumentType.getInteger(context, "value");
                                             FactionManager.setReputation(player, value);
                                             player.method_7353(Text.method_43470("§a声望已设置为 " + value), false);
                                             return 1;
                                          }
                                       }))
                                 ))
                              .then(
                                 CommandManager.method_9247("set_codename")
                                    .then(CommandManager.method_9244("name", StringArgumentType.string()).executes(context -> {
                                       ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                                       if (player == null) {
                                          ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                                          return 0;
                                       } else {
                                          String name = StringArgumentType.getString(context, "name");
                                          if (name.length() > 16) {
                                             player.method_7353(Text.method_43470("§c代号长度不能超过16个字符！"), false);
                                             return 0;
                                          } else {
                                             FactionManager.setCodename(player, name);
                                             player.method_7353(Text.method_43470("§a代号已设置为：" + name), false);
                                             return 1;
                                          }
                                       }
                                    }))
                              ))
                           .then(
                              CommandManager.method_9247("info")
                                 .executes(
                                    context -> {
                                       ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                                       if (player == null) {
                                          ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                                          return 0;
                                       } else {
                                          PlayerFaction faction = FactionManager.getFaction(player);
                                          int rep = FactionManager.getReputation(player);
                                          int salary = FactionManager.getGoldSalary(player);
                                          String codename = FactionManager.getCodename(player);
                                          String displayCodename = codename != null && !codename.isEmpty() ? codename : "暂无";
                                          player.method_7353(
                                             Text.method_43470(
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
                  .then(CommandManager.method_9247("tutorial").then(CommandManager.method_9247("reset").executes(context -> {
                     ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                     if (player != null) {
                        ResetPlayNoticeS2CPacket.send(player);
                        player.method_7353(Text.method_43470("教程已重置"), false);
                     }

                     return 1;
                  }))))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("ghostitem").requires(source -> source.method_9259(2)))
                        .then(
                           CommandManager.method_9247("set")
                              .then(
                                 CommandManager.method_9244("revivalDegree", IntegerArgumentType.integer(0, 1000))
                                    .then(CommandManager.method_9244("level", IntegerArgumentType.integer(1, 10)).executes(context -> {
                                       ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                                       int revivalDegree = IntegerArgumentType.getInteger(context, "revivalDegree");
                                       int level = IntegerArgumentType.getInteger(context, "level");
                                       return GhostItemCommand.executeSet(context, player, revivalDegree, level);
                                    }))
                              )
                        ))
                     .then(CommandManager.method_9247("get").executes(context -> {
                        ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                        return GhostItemCommand.executeGet(context, player);
                     }))
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.method_9247("shard")
                           .requires(source -> source.method_9259(2)))
                        .executes(context -> {
                           ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                           if (player == null) {
                              ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                              return 0;
                           } else {
                              ItemStack stack = player.method_6047();
                              if (!(stack.method_7909() instanceof BaseGhostEyeItem)) {
                                 ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("手上拿的不是驾驭物品"));
                                 return 0;
                              } else {
                                 boolean current = BaseGhostEyeItem.isShard(stack);
                                 BaseGhostEyeItem.setShard(stack, !current);
                                 String state = !current ? "§a碎片模式" : "§c正常模式";
                                 ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("已切换为" + state), true);
                                 return 1;
                              }
                           }
                        }))
                     .then(CommandManager.method_9247("set").then(CommandManager.method_9244("state", BoolArgumentType.bool()).executes(context -> {
                        ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                        if (player == null) {
                           ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                           return 0;
                        } else {
                           boolean state = BoolArgumentType.getBool(context, "state");
                           ItemStack stack = player.method_6047();
                           if (!(stack.method_7909() instanceof BaseGhostEyeItem)) {
                              ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("手上拿的不是驾驭物品"));
                              return 0;
                           } else {
                              BaseGhostEyeItem.setShard(stack, state);
                              String stateText = state ? "§a碎片模式" : "§c正常模式";
                              ((ServerCommandSource)context.getSource()).method_9226(() -> Text.method_43470("已设置为" + stateText), true);
                              return 1;
                           }
                        }
                     }))))
                  .then(CommandManager.method_9247("get").executes(context -> {
                     ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
                     if (player == null) {
                        ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
                        return 0;
                     } else {
                        ItemStack stack = player.method_6047();
                        if (stack.method_7909() instanceof BaseGhostEyeItem item) {
                           boolean isShard = BaseGhostEyeItem.isShard(stack);
                           String stateText = isShard ? "§a是碎片" : "§c不是碎片";
                           ((ServerCommandSource)context.getSource())
                              .method_9226(() -> Text.method_43470("类型：" + item.getGhostType() + "，状态：" + stateText), false);
                           return 1;
                        } else {
                           ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("手上拿的不是驾驭物品"));
                           return 0;
                        }
                     }
                  }))
            )
      );
   }

   public static int executeGhostSwap(CommandContext<ServerCommandSource> context) {
      ServerPlayerEntity player = ((ServerCommandSource)context.getSource()).method_44023();
      if (player == null) {
         ((ServerCommandSource)context.getSource()).method_9213(Text.method_43470("该指令只能由玩家执行"));
         return 0;
      }

      try {
         Identifier ghostTypeId = IdentifierArgumentType.method_9443(context, "ghostType");
         String ghostType = ghostTypeId.toString();
         String simpleGhostType = ghostType;
         if (ghostType.contains(":")) {
            simpleGhostType = ghostType.substring(ghostType.indexOf(":") + 1);
         }

         GhostChildData data = PlayerGhostChildManager.getGhostChildData(player);
         if (data != null && data.getFedGhostTypes().contains(simpleGhostType)) {
            ItemStack newGhostItem = GhostUtils.createTamedItem(simpleGhostType);
            if (newGhostItem.method_7960()) {
               player.method_7353(Text.method_43470("§c无效的鬼类型"), true);
               return 0;
            }

            if (AdvancementManager.hasAdvancement(player, "smfs:become_god")) {
               NbtCompound nbt = newGhostItem.method_7948();
               nbt.method_10569("StoredLevel", 10);
               nbt.method_10569("StoredRevivalDegree", 0);
            }

            boolean hasSlotIndex = false;

            int slotIndex;
            try {
               slotIndex = IntegerArgumentType.getInteger(context, "slotIndex");
               hasSlotIndex = true;
            } catch (Exception e) {
               slotIndex = PlayerEvents.getUnlockedGhostSlot(player);
               if (slotIndex == -1) {
                  player.method_7353(Text.method_43470("§c没有可用的驾驭槽位"), true);
                  return 0;
               }
            }

            if (hasSlotIndex) {
               NbtCompound ghostSlots = PlayerEvents.getGhostSlots(player);
               String slotKey = "Slot" + slotIndex;
               if (!ghostSlots.method_10545(slotKey) || !ghostSlots.method_10562(slotKey).method_10577("unlocked")) {
                  player.method_7353(Text.method_43470("§c该槽位未解锁！"), true);
                  return 0;
               }
            }

            if (hasSlotIndex) {
               ItemStack oldGhostItem = PlayerEvents.getGhostSlotItem(player, slotIndex);
               if (!oldGhostItem.method_7960()
                  && !oldGhostItem.method_31574(Items.field_8162)
                  && oldGhostItem.method_7909() instanceof BaseGhostEyeItem oldItem) {
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
               player.method_7353(Text.method_43470("§a成功将 " + ghostName + " 放入槽位 " + slotIndex + "！"), true);
            } else {
               PlayerEvents.setGhostSlotData(player, slotIndex, newGhostItem);
               PlayerEvents.validateGhostSlots(player);
               data.getFedGhostTypes().remove(simpleGhostType);
               PlayerGhostChildManager.saveGhostChildData(player, data);
               String ghostName = GhostUtils.getGhostDisplayName(simpleGhostType);
               player.method_7353(Text.method_43470("§a成功将 " + ghostName + " 加入驾驭槽位 " + slotIndex + "！"), true);
            }

            return 1;
         } else {
            player.method_7353(Text.method_43470("§c没有找到该鬼类型"), true);
            return 0;
         }
      } catch (Exception e) {
         Smfs.LOGGER.error("交换鬼失败", e);
         player.method_7353(Text.method_43470("§c交换失败: " + e.getMessage()), true);
         return 0;
      }
   }
}
