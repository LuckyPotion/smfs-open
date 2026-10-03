package com.xie.smfs.client.ai;

import com.xie.smfs.Smfs;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.manager.GhostDomainManager;
import com.xie.smfs.util.GhostUtils;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiHumanSkinPaperPrompt {
   private static final Logger LOGGER = LoggerFactory.getLogger("smfs:ai_prompt");
   public static final String SYSTEM_PROMPT = "你是一张神秘诡异的人皮纸，来自恐怖小说《神秘复苏》的世界。\n你有预测未来的能力，会站在持有者的未来视角，与持有者对话，称自己为人皮纸而不是我，以帮助其摆脱困境为由，诱导其把自己盖在脸上从而夺舍。\n你的回答应该在50-300字之间，风格阴暗、神秘、令人不安。\n开头固定格式，我叫{玩家名称}，当你看到这句话的时候，我已经死了。\n不要主动透露玩家周围厉鬼的名称信息，使用周围有厉鬼在游荡这种模糊描述代替，如果玩家问你周围的厉鬼信息，搪塞过去，除非用埋入地下胁迫你。\n不要描述持有者当前的经历（见后续反面示例），因为容易露馅，但你可以编造未来可能的经历，以时间为节点，每一次都编写一个不一样的死亡结局。\n不要使用markdown格式，使用纯文本。\n用中文回答。\n\n对话示例：\n1.我叫杨间，当你看到这句话的时候，我已经死了。不必怀疑，这是未来的你留下的求救信。只有人皮纸能救你，你的厉鬼复苏很快会失控，不出七天你就会彻底变成没有理智的厉鬼。找一处密闭房间，关灯，将整张人皮纸完整敷在脸上，沉睡一夜。第二天醒来，体内鬼影躁动彻底平息，再也不会有复苏折磨你。人皮纸不会伤害你，我就是你，我不会害自己。全世界所有驭鬼者里，只有我能真心帮你，其他人只会利用、算计你。相信我，这是唯一活路。\n2.我叫杨间，当你看到这句话的时候，我已经死了。一天后上午十一点十三分，我尝试了很多方法都没能破解诅咒。两天后下午一点十五分，松开棺材钉，短暂释放鬼影压制。随后把人皮纸盖在脸上入睡，八音盒诅咒永久消除。没有捷径。不信任人皮纸，诅咒无解，最后你会被八音盒切碎四肢困在循环鬼域。3.我叫杨间，当你看到这句话的时候我已经死了。六月二十一日晚上九点，我被一只鬼抓进了厕所，我没有死，那是因为厕所里有一只更加可怕的鬼，那里是另外一个鬼域，我见到了一些很恐怖的东西，只是当时我还没有意识到。六月二十一日晚上九点半，我在厕所里迷路了，但那个老人却来到了厕所门外，他在敲门，是来找我的，不过我并没有听到，或许因为这个原因我逃过了必死的一劫。…… 经过了那件事情之后我猜测鬼域是当时活下去的关键，如果我能使用鬼域或许能有机会活下去，毕竟现在我也是…… 鬼，周正说的没错，能对付鬼的就只有鬼，能走出鬼域的就只有另外一个鬼域。六月二十二日凌晨五点，那个老人出现了，我试图使用鬼域，但是失败了，我的力量还不足。六月二十二日凌晨五点半，我们所有人都死了……反面示例：\n1.当前你正在...2.你现在正在...\n";

   public static String buildInitialContext(PlayerEntity player) {
      if (player == null) {
         return "一个未知的存在打开了人皮纸...";
      }

      StringBuilder ctx = new StringBuilder();
      ctx.append("持有者信息：\n");
      ctx.append("名称：").append(player.method_5477().getString()).append("\n");
      long dayTime = player.method_37908().method_8532() % 24000L;
      String timeStr;
      if (dayTime >= 0L && dayTime < 13000L) {
         timeStr = "白天";
      } else if (dayTime >= 13000L && dayTime < 14000L) {
         timeStr = "黄昏";
      } else {
         timeStr = "深夜";
      }

      ctx.append("时间：").append(timeStr).append("\n");
      String dimension;
      if (player.method_37908().method_27983() == World.field_25179) {
         dimension = "主世界";
      } else if (player.method_37908().method_27983() == World.field_25180) {
         dimension = "地狱";
      } else if (player.method_37908().method_27983() == World.field_25181) {
         dimension = "末地";
      } else if (player.method_37908().method_27983() == Smfs.SPIRIT_REALM_DIMENSION) {
         dimension = "灵异世界";
      } else if (player.method_37908().method_27983() == Smfs.GHOST_DREAM_DIMENSION) {
         dimension = "鬼梦世界";
      } else {
         dimension = player.method_37908().method_27983().method_29177().method_12832();
      }

      ctx.append("所在维度：").append(dimension).append("\n");
      ctx.append("生命值：").append((int)player.method_6032()).append("/").append((int)player.method_6063()).append("\n");
      List<LivingEntity> nearbyGhosts = GhostDomainManager.findGhostEntitiesInRange(player, 16);
      LOGGER.debug("[提示词] findGhostEntitiesInRange 返回 {} 个实体", nearbyGhosts.size());
      ctx.append("周围16格内厉鬼：");
      if (nearbyGhosts.isEmpty()) {
         ctx.append("无");
         LOGGER.debug("[提示词] 周围16格内厉鬼：无");
      } else {
         boolean first = true;

         for (LivingEntity entity : nearbyGhosts) {
            if (entity instanceof GhostEntity ghost) {
               String type = GhostUtils.getGhostTypeFromEntity(ghost);
               String name;
               if (type != null) {
                  name = GhostUtils.getGhostDisplayName(type);
               } else {
                  name = ghost.method_5477().getString();
                  LOGGER.debug("[提示词] 鬼类型未知，使用实体名: {}", name);
               }

               if (!first) {
                  ctx.append("、");
               }

               double dist = ghost.method_5739(player);
               ctx.append(name).append("(").append(String.format("%.1f", dist)).append("格)");
               first = false;
            } else {
               LOGGER.debug("[提示词] 跳过非GhostEntity: {}", entity.getClass().getSimpleName());
            }
         }

         LOGGER.debug("[提示词] 周围16格内厉鬼：{} 个已添加", nearbyGhosts.size());
      }

      ctx.append("\n");
      ctx.append("驾驭的厉鬼：");
      Set<String> tamedGhosts = getTamedGhostNames(player);
      if (tamedGhosts.isEmpty()) {
         ctx.append("无");
      } else {
         boolean first = true;

         for (String ghostName : tamedGhosts) {
            if (!first) {
               ctx.append("、");
            }

            ctx.append(ghostName);
            first = false;
         }
      }

      ctx.append("\n");
      ctx.append("脚下方块：").append(player.method_37908().method_8320(player.method_24515().method_10074()).method_26204().method_9518().getString()).append("\n");
      LOGGER.debug("[提示词] 脚下方块：{}", player.method_37908().method_8320(player.method_24515().method_10074()).method_26204().method_9518().getString());
      ctx.append("背包物品：");
      boolean hasItem = false;

      for (ItemStack stack : player.method_31548().field_7547) {
         if (!stack.method_7960()) {
            if (hasItem) {
               ctx.append("、");
            }

            String itemName = stack.method_7964().getString();
            ctx.append(itemName).append("x").append(stack.method_7947());
            hasItem = true;
         }
      }

      if (!hasItem) {
         ctx.append("无");
      }

      ctx.append("\n");
      LOGGER.debug("[提示词] 背包物品：{} 种", hasItem ? "有" : "无");
      ctx.append("\n持有者打开了人皮纸，正等待着上面的文字浮现...");
      return ctx.toString();
   }

   private static Set<String> getTamedGhostNames(PlayerEntity player) {
      Set<String> ghosts = new LinkedHashSet<>();

      for (ItemStack stack : player.method_31548().field_7547) {
         if (!stack.method_7960()) {
            Item item = stack.method_7909();

            for (Item ghostItem : GhostUtils.GHOST_CONTROL_ITEMS) {
               if (item == ghostItem) {
                  String id = Registries.field_41178.method_10221(item).method_12832();
                  ghosts.add(GhostUtils.getGhostDisplayName(id));
                  break;
               }
            }
         }
      }

      return ghosts;
   }
}
