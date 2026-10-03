package com.xie.smfs.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class XinKaiGhostItem extends BaseGhostEyeItem {
   public XinKaiGhostItem(Settings settings) {
      super(settings.maxCount(1), 1500, 60, 100, 0, 0.25);
   }

   @Override
   public Text getName(ItemStack stack) {
      return Text.translatable("item.smfs.xinkai_ghost");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.translatable("item.smfs.xinkai_ghost.bind_success").append(Text.literal(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.sendMessage(Text.translatable("item.smfs.xinkai_ghost.slots_full"), true);
   }

   @Override
   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      return super.use(world, user, hand);
   }

   @Override
   public String getGhostType() {
      return "xinkai_ghost";
   }
}
