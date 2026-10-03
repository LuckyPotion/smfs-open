package com.xie.smfs.entity.ghost;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.config.WorldConfig;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.network.packets.goodseller.s2c.GoodsSellerKillScreenS2CPacket;
import java.lang.reflect.Method;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoodsSellerGhostEntity extends GhostEntity {
   private static final Logger LOGGER = LoggerFactory.getLogger(GoodsSellerGhostEntity.class);

   public GoodsSellerGhostEntity(EntityType<? extends GhostEntity> entityType, World world) {
      super(entityType, world, false, 0, 24.0, 'B', 2500, 50, 40, 0.2F);
      this.ghostLevel = 1;
      this.attackCooldown = 80;
      this.setEnableChaseAfterRule(false);
      this.initSellerAttributes();
   }

   private void initSellerAttributes() {
      this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(100000.0);
      this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);
      this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(20.0);
      this.setHealth(40.0F);
   }

   @Override
   protected void executeAttack(PlayerEntity player, boolean isTimeoutAttack) {
      super.executeAttack(player, isTimeoutAttack);
      if (!this.getWorld().isClient && player.isAlive()) {
         if (!PlayerEvents.isGhostChildFused(player)) {
            WorldConfig config = WorldConfig.getInstance(this.getWorld());
            if (!"linear".equals(config.endingMode)) {
               GoodsSellerKillScreenS2CPacket.send((ServerPlayerEntity)player);
            }
         }
      }
   }

   public ActionResult interactMob(PlayerEntity player, Hand hand) {
      if (this.getWorld().isClient) {
         return ActionResult.SUCCESS;
      }

      if (player.isSneaking()) {
         return super.interactMob(player, hand);
      }

      this.injectQuestToManager(player);
      return ActionResult.SUCCESS;
   }

   private void injectQuestToManager(PlayerEntity player) {
      try {
         Class<?> questManagerClass = Class.forName("com.xie.smfs.manager.QuestManager");
         Method assignQuestMethod = questManagerClass.getMethod("assignQuest", PlayerEntity.class, String.class, String.class);
         int completions = this.getSellerQuestCompletions(player);
         Boolean result;
         if (completions == 0) {
            result = (Boolean)assignQuestMethod.invoke(null, player, "seller_basic_collection", null);
         } else {
            Method getRandomMethod = questManagerClass.getMethod("getRandomGhostTypeForSellerQuest");
            String ghostType = (String)getRandomMethod.invoke(null);
            result = (Boolean)assignQuestMethod.invoke(null, player, "seller_basic_collection", ghostType);
         }

         if (result != null && result) {
            player.sendMessage(Text.literal("§6你与卖货郎进行了一笔交易，打开任务界面查看详细！"), false);
            LOGGER.info("为玩家 {} 注入了限时黄金容器收集任务", player.getName().getString());
         }
      } catch (Exception e) {
         LOGGER.error("注入任务失败: {}", e.getMessage());
         player.sendMessage(Text.literal("§c任务注入失败：系统错误"), false);
      }
   }

   private int getSellerQuestCompletions(PlayerEntity player) {
      try {
         Class<?> questManagerClass = Class.forName("com.xie.smfs.manager.QuestManager");
         Method getQuestDataMethod = questManagerClass.getDeclaredMethod("getQuestData", PlayerEntity.class);
         getQuestDataMethod.setAccessible(true);
         NbtCompound questData = (NbtCompound)getQuestDataMethod.invoke(null, player);
         NbtList completedQuests = questData.getList("completedQuests", 10);
         int count = 0;

         for (int i = 0; i < completedQuests.size(); i++) {
            NbtCompound quest = completedQuests.getCompound(i);
            if ("seller_basic_collection".equals(quest.getString("id"))) {
               count++;
            }
         }

         return count;
      } catch (Exception e) {
         return 0;
      }
   }
}
