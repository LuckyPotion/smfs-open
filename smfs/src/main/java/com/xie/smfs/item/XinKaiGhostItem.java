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
      super(settings.method_7889(1), 1500, 60, 100, 0, 0.25);
   }

   @Override
   public Text method_7864(ItemStack stack) {
      return Text.method_43471("item.smfs.xinkai_ghost");
   }

   @Override
   protected Text getBindSuccessMessage(int slot) {
      return Text.method_43471("item.smfs.xinkai_ghost.bind_success").method_10852(Text.method_43470(" (槽位 " + (slot + 1) + ")"));
   }

   @Override
   protected void sendSlotsFullMessage(PlayerEntity player) {
      player.method_7353(Text.method_43471("item.smfs.xinkai_ghost.slots_full"), true);
   }

   @Override
   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity player, Hand hand) {
      return super.method_7836(world, player, hand);
   }

   @Override
   public String getGhostType() {
      return "xinkai_ghost";
   }
}
