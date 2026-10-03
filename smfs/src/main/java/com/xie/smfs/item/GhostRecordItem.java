package com.xie.smfs.item;

import com.xie.smfs.Smfs;
import com.xie.smfs.registry.ModEffects;
import com.xie.smfs.registry.ModSounds;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.Item.Settings;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GhostRecordItem extends MusicDiscItem {
   private static final int EFFECT_DURATION = 3600;
   private static final double EFFECT_RADIUS = 16.0;

   public GhostRecordItem(Settings settings) {
      super(15, ModSounds.KNOCKING_SOUND, settings, 60);
   }

   public ActionResult method_7884(ItemUsageContext context) {
      World world = context.method_8045();
      BlockPos pos = context.method_8037();
      PlayerEntity player = context.method_8036();
      Smfs.LOGGER.debug("鬼唱片使用开始 - 玩家: {}, 位置: {}, 世界: {}", player != null ? player.method_5477().getString() : "null", pos, world.field_9236 ? "客户端" : "服务端");
      ActionResult parentResult = super.method_7884(context);
      Smfs.LOGGER.debug("父类处理结果: {}", parentResult);
      if (world.method_8320(pos).method_27852(Blocks.field_10223)) {
         Smfs.LOGGER.debug("检测到唱片机方块");
         boolean hasRecord = (Boolean)world.method_8320(pos).method_11654(JukeboxBlock.field_11180);
         Smfs.LOGGER.debug("唱片机当前是否有唱片: {}", hasRecord);
         if (!hasRecord && (parentResult == ActionResult.field_5812 || parentResult == ActionResult.field_21466)) {
            Smfs.LOGGER.debug("检测到放入操作 - 准备添加特殊效果");
            if (!world.field_9236) {
               Smfs.LOGGER.debug("服务端 - 开始添加敲门诅咒效果");
               Smfs.LOGGER.debug("父类已处理唱片放入，开始添加特殊效果");
               this.applyEffectToNearbyPlayers(world, pos);
            } else {
               Smfs.LOGGER.debug("客户端 - 跳过效果添加");
            }
         } else if (hasRecord) {
            Smfs.LOGGER.debug("检测到取出操作 - 跳过效果添加");
         } else if (parentResult != ActionResult.field_5812 && parentResult != ActionResult.field_21466) {
            Smfs.LOGGER.debug("父类处理未成功 - 跳过效果添加");
         }
      } else {
         Smfs.LOGGER.debug("非唱片机方块 - 跳过特殊效果处理");
      }

      Smfs.LOGGER.debug("最终返回结果: {}", parentResult);
      return parentResult;
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_record.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_record.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_record.description.type"));
   }

   private void applyEffectToNearbyPlayers(World world, BlockPos pos) {
      Smfs.LOGGER.debug("开始搜索唱片机周围玩家 - 唱片机位置: {}", pos);
      Box box = new Box(
         pos.method_10263() - 16.0,
         pos.method_10264() - 16.0,
         pos.method_10260() - 16.0,
         pos.method_10263() + 16.0,
         pos.method_10264() + 16.0,
         pos.method_10260() + 16.0
      );
      List<PlayerEntity> players = world.method_18467(PlayerEntity.class, box);
      Smfs.LOGGER.debug("找到 {} 个附近玩家", players.size());

      for (PlayerEntity player : players) {
         Smfs.LOGGER.debug("给玩家 {} 添加敲门诅咒效果", player.method_5477().getString());
         player.method_6092(new StatusEffectInstance(ModEffects.KNOCKING_CURSE, 3600, 0));
         world.method_43128(
            null, player.method_23317(), player.method_23318(), player.method_23321(), ModSounds.KNOCKING_SOUND, SoundCategory.field_15256, 1.0F, 1.0F
         );
         Smfs.LOGGER.debug("已为玩家 {} 添加效果并播放音效", player.method_5477().getString());
      }

      Smfs.LOGGER.debug("敲门诅咒效果添加完成 - 总共影响 {} 个玩家", players.size());
   }
}
