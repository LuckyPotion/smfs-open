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
      NbtCompound nbt = stack.getNbt();
      return nbt != null && nbt.getBoolean("Shard");
   }

   public static void setShard(ItemStack stack, boolean shard) {
      stack.getOrCreateNbt().putBoolean("Shard", shard);
      if (shard) {
         stack.getOrCreateNbt().putInt("ShardUses", 2);
      }
   }

   public Text getName(ItemStack stack) {
      Text baseName = super.getName(stack);
      return (Text)(isShard(stack) ? Text.literal(baseName.getString() + "（碎片）") : baseName);
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.getStackInHand(hand);
      if (isShard(stack)) {
         return TypedActionResult.pass(user.getStackInHand(hand));
      }

      if (world.isClient) {
         return TypedActionResult.pass(user.getStackInHand(hand));
      }

      if (user instanceof ServerPlayerEntity serverPlayer) {
         LOGGER.debug("玩家 {} 是服务器端玩家，开始处理绑定逻辑", serverPlayer.getGameProfile().getName());

         try {
            TameableItemAPI api = TameableItemAPI.getInstance();
            if (api.hasTamedGhost(serverPlayer, this.getGhostType())) {
               LOGGER.debug("玩家 {} 已经驾驭同种类厉鬼，绑定失败", serverPlayer.getGameProfile().getName());
               user.sendMessage(Text.translatable("item.smfs.ghost_eye.same_type_exists"), true);
               return TypedActionResult.fail(stack);
            }
         } catch (Exception e) {
            LOGGER.warn("检查同种类驾驭状态失败: {}", e.getMessage());
         }

         int emptySlot = PlayerEvents.getUnlockedGhostSlot(serverPlayer);
         if (emptySlot != -1) {
            ItemStack ghostEyeCopy = stack.copy();
            ghostEyeCopy.setCount(1);

            try {
               PlayerEvents.setGhostSlotData(serverPlayer, emptySlot, ghostEyeCopy);
               stack.decrement(1);
               PlayerEvents.validateGhostSlots(serverPlayer);
               NbtCompound ghostData = new NbtCompound();
               ghostData.putString("Type", this.getGhostType());
               ghostData.putString("Name", this.getName().getString());
               PlayerEvents.showGhostAbilityPopup(serverPlayer, ghostData);
               GhostTamingScreenHandler.tryLockGhostAfterTaming(serverPlayer, this.getGhostType());
               return TypedActionResult.success(stack);
            } catch (Exception e) {
               LOGGER.error("绑定鬼眼到槽位 {} 失败: {}", emptySlot, e.getMessage(), e);
               user.sendMessage(Text.literal("绑定失败，请重试"), false);
               return TypedActionResult.fail(stack);
            }
         } else {
            this.sendSlotsFullMessage(user);
            return TypedActionResult.fail(stack);
         }
      } else {
         return TypedActionResult.pass(stack);
      }
   }

   protected void applySpiritAttributes(ServerPlayerEntity player) {
      NbtCompound spiritData = PlayerEvents.getSpiritAttributes(player);
      spiritData.putDouble("maxSpirit", spiritData.getDouble("maxSpirit") + this.maxSpiritBonus);
      spiritData.putDouble("spiritResistance", spiritData.getDouble("spiritResistance") + this.spiritResistanceBonus);
      spiritData.putDouble("spiritDamage", spiritData.getDouble("spiritDamage") + this.spiritDamageBonus);
      spiritData.putDouble("sanity", spiritData.getDouble("sanity") + this.sanityBonus);
      spiritData.putDouble("revivalFactor", spiritData.getDouble("revivalFactor") + this.revivalFactor);
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

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.source"));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.desc"));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.description.type"));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.max_spirit", new Object[]{this.maxSpiritBonus}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.spirit_resistance", new Object[]{this.spiritResistanceBonus}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.spirit_damage", new Object[]{this.spiritDamageBonus}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.sanity", new Object[]{this.sanityBonus}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.revival_factor", new Object[]{String.format("%.1f", this.revivalFactor * 100.0)}));
      tooltip.add(Text.translatable("item.smfs.ghost_eye.usage"));
   }

   protected abstract Text getBindSuccessMessage(int i);

   protected abstract void sendSlotsFullMessage(PlayerEntity playerEntity);

   public abstract String getGhostType();
}
