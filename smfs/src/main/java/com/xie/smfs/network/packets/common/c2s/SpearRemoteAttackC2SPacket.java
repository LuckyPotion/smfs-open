package com.xie.smfs.network.packets.common.c2s;

import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.item.FissuredSpearPurpleItem;
import com.xie.smfs.item.RustyOldBroadswordItem;
import com.xie.smfs.item.SpiritWeapon;
import com.xie.smfs.registry.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpearRemoteAttackC2SPacket {
   private static final Identifier ID = new Identifier("smfs", "spear_remote_attack");

   public static void sendToServer(int targetEntityId) {
      PacketByteBuf buf = PacketByteBufs.create();
      buf.writeInt(targetEntityId);
      ClientPlayNetworking.send(ID, buf);
   }

   public static void registerServer() {
      ServerPlayNetworking.registerGlobalReceiver(
         ID,
         (server, player, handler, buf, responseSender) -> {
            int targetEntityId = buf.readInt();
            server.execute(
               () -> {
                  ItemStack stack = player.getMainHandStack();
                  SpiritWeapon weapon = null;
                  if (stack.getItem() instanceof FissuredSpearPurpleItem) {
                     String mode = FissuredSpearPurpleItem.getThrowMode(stack);
                     if (!mode.equals("wish")) {
                        return;
                     }

                     String preset = FissuredSpearPurpleItem.getWishPreset(stack);
                     if (!preset.equals("remote_attack")) {
                        return;
                     }

                     weapon = (SpiritWeapon)stack.getItem();
                  } else {
                     if (!(stack.getItem() instanceof RustyOldBroadswordItem)) {
                        return;
                     }

                     if (!RustyOldBroadswordItem.isRangedMode(stack)) {
                        return;
                     }

                     weapon = (SpiritWeapon)stack.getItem();
                  }

                  if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                     if (player.getWorld().getEntityById(targetEntityId) instanceof LivingEntity target && target != player && target.isAlive()) {
                        float weaponDamageBonus = weapon.getSpiritDamageBonus();
                        float damageMultiplier = weapon.getSpiritDamageMultiplier();
                        NbtCompound attackerData = PlayerEvents.getSpiritAttributes(player);
                        float playerSpiritDamage = attackerData.contains("spiritDamage") ? (float)attackerData.getDouble("spiritDamage") : 0.0F;
                        float tempSpiritDamage = attackerData.contains("tempSpiritDamage") ? (float)attackerData.getDouble("tempSpiritDamage") : 0.0F;
                        float tempSpiritDamageMultiplier = attackerData.contains("tempSpiritDamageMultiplier")
                           ? (float)attackerData.getDouble("tempSpiritDamageMultiplier")
                           : 1.0F;
                        float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
                        float spiritDamage = weaponDamageBonus + totalSpiritDamage * damageMultiplier;
                        target.damage(player.getDamageSources().playerAttack(player), spiritDamage);
                        player.getWorld()
                           .playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.SWING_ATTACK_SOUND, SoundCategory.HOSTILE, 1.0F, 1.0F);
                        player.getItemCooldownManager().set(stack.getItem(), 10);
                        if (stack.getItem() instanceof FissuredSpearPurpleItem && FissuredSpearPurpleItem.isShowWishText(stack)) {
                           player.sendMessage(Text.literal("§6我说这一刀砍下必定命中眼前生物"), false);
                        }
                     }
                  }
               }
            );
         }
      );
   }
}
