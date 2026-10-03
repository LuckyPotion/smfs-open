package com.xie.smfs.item;

import com.xie.smfs.api.TameableItemAPI;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.event.screen.GhostTamingScreenHandler;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseGhostEyeItem extends Item {
   private static final Logger LOGGER = LoggerFactory.getLogger(BaseGhostEyeItem.class);
   protected final int maxSpiritBonus;
   protected final int spiritResistanceBonus;
   protected final int spiritDamageBonus;
   protected final int sanityBonus;
   protected final double revivalFactor;

   public BaseGhostEyeItem(Settings settings, int maxSpiritBonus, int spiritResistanceBonus, int spiritDamageBonus, int sanityBonus, double revivalFactor) {
      super(settings);
      this.maxSpiritBonus = maxSpiritBonus;
      this.spiritResistanceBonus = spiritResistanceBonus;
      this.spiritDamageBonus = spiritDamageBonus;
      this.sanityBonus = sanityBonus;
      this.revivalFactor = revivalFactor;
   }

   public static boolean isShard(ItemStack stack) {
      NbtCompound nbt = stack.method_7969();
      return nbt != null && nbt.method_10577("Shard");
   }

   public static void setShard(ItemStack stack, boolean shard) {
      stack.method_7948().method_10556("Shard", shard);
      if (shard) {
         stack.method_7948().method_10569("ShardUses", 2);
      }
   }

   public Text method_7864(ItemStack stack) {
      Text baseName = super.method_7864(stack);
      return (Text)(isShard(stack) ? Text.method_43470(baseName.getString() + "（碎片）") : baseName);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      ItemStack stack = player.method_5998(hand);
      if (isShard(stack)) {
         return TypedActionResult.method_22430(player.method_5998(hand));
      }

      if (world.field_9236) {
         return TypedActionResult.method_22430(player.method_5998(hand));
      }

      if (player instanceof ServerPlayerEntity serverPlayer) {
         LOGGER.debug("玩家 {} 是服务器端玩家，开始处理绑定逻辑", serverPlayer.method_7334().getName());

         try {
            TameableItemAPI api = TameableItemAPI.getInstance();
            if (api.hasTamedGhost(serverPlayer, this.getGhostType())) {
               LOGGER.debug("玩家 {} 已经驾驭同种类厉鬼，绑定失败", serverPlayer.method_7334().getName());
               player.method_7353(Text.method_43471("item.smfs.ghost_eye.same_type_exists"), true);
               return TypedActionResult.method_22431(stack);
            }
         } catch (Exception e) {
            LOGGER.warn("检查同种类驾驭状态失败: {}", e.getMessage());
         }

         int emptySlot = PlayerEvents.getUnlockedGhostSlot(serverPlayer);
         if (emptySlot != -1) {
            ItemStack ghostEyeCopy = stack.method_7972();
            ghostEyeCopy.method_7939(1);

            try {
               PlayerEvents.setGhostSlotData(serverPlayer, emptySlot, ghostEyeCopy);
               stack.method_7934(1);
               PlayerEvents.validateGhostSlots(serverPlayer);
               NbtCompound ghostData = new NbtCompound();
               ghostData.method_10582("Type", this.getGhostType());
               ghostData.method_10582("Name", this.method_7848().getString());
               PlayerEvents.showGhostAbilityPopup(serverPlayer, ghostData);
               GhostTamingScreenHandler.tryLockGhostAfterTaming(serverPlayer, this.getGhostType());
               return TypedActionResult.method_22427(stack);
            } catch (Exception e) {
               LOGGER.error("绑定鬼眼到槽位 {} 失败: {}", emptySlot, e.getMessage(), e);
               player.method_7353(Text.method_43470("绑定失败，请重试"), false);
               return TypedActionResult.method_22431(stack);
            }
         } else {
            this.sendSlotsFullMessage(player);
            return TypedActionResult.method_22431(stack);
         }
      } else {
         return TypedActionResult.method_22430(stack);
      }
   }

   protected void applySpiritAttributes(ServerPlayerEntity player) {
      NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
      spiritData.method_10549("maxSpirit", spiritData.method_10574("maxSpirit") + this.maxSpiritBonus);
      spiritData.method_10549("spiritResistance", spiritData.method_10574("spiritResistance") + this.spiritResistanceBonus);
      spiritData.method_10549("spiritDamage", spiritData.method_10574("spiritDamage") + this.spiritDamageBonus);
      spiritData.method_10549("sanity", spiritData.method_10574("sanity") + this.sanityBonus);
      spiritData.method_10549("revivalFactor", spiritData.method_10574("revivalFactor") + this.revivalFactor);
      PlayerEvents.setSpiritAttributes(player, spiritData);
   }

   public int getMaxSpiritBonus() {
      return this.maxSpiritBonus;
   }

   public int getSpiritResistanceBonus() {
      return this.spiritResistanceBonus;
   }

   public int getSpiritDamageBonus() {
      return this.spiritDamageBonus;
   }

   public int getSanityBonus() {
      return this.sanityBonus;
   }

   public double getRevivalFactor() {
      return this.revivalFactor;
   }

   public void method_7851(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.description.source"));
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.description.type"));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.max_spirit", new Object[]{this.maxSpiritBonus}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.spirit_resistance", new Object[]{this.spiritResistanceBonus}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.spirit_damage", new Object[]{this.spiritDamageBonus}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.sanity", new Object[]{this.sanityBonus}));
      tooltip.add(Text.method_43469("item.smfs.ghost_eye.revival_factor", new Object[]{String.format("%.1f", this.revivalFactor * 100.0)}));
      tooltip.add(Text.method_43471("item.smfs.ghost_eye.usage"));
   }

   protected abstract Text getBindSuccessMessage(int i);

   protected abstract void sendSlotsFullMessage(PlayerEntity playerEntity);

   public abstract String getGhostType();
}
