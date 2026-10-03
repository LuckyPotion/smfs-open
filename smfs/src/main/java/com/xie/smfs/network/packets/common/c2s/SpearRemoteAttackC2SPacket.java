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
                  ItemStack stack = player.method_6047();
                  SpiritWeapon weapon = null;
                  if (stack.method_7909() instanceof FissuredSpearPurpleItem) {
                     String mode = FissuredSpearPurpleItem.getThrowMode(stack);
                     if (!mode.equals("wish")) {
                        return;
                     }

                     String preset = FissuredSpearPurpleItem.getWishPreset(stack);
                     if (!preset.equals("remote_attack")) {
                        return;
                     }

                     weapon = (SpiritWeapon)stack.method_7909();
                  } else {
                     if (!(stack.method_7909() instanceof RustyOldBroadswordItem)) {
                        return;
                     }

                     if (!RustyOldBroadswordItem.isRangedMode(stack)) {
                        return;
                     }

                     weapon = (SpiritWeapon)stack.method_7909();
                  }

                  if (!player.method_7357().method_7904(stack.method_7909())) {
                     if (player.method_37908().method_8469(targetEntityId) instanceof LivingEntity target && target != player && target.method_5805()) {
                        float weaponDamageBonus = weapon.getSpiritDamageBonus();
                        float damageMultiplier = weapon.getSpiritDamageMultiplier();
                        NbtCompound attackerData = PlayerEvents.getSpiritAttributes(player);
                        float playerSpiritDamage = attackerData.method_10545("spiritDamage") ? (float)attackerData.method_10574("spiritDamage") : 0.0F;
                        float tempSpiritDamage = attackerData.method_10545("tempSpiritDamage") ? (float)attackerData.method_10574("tempSpiritDamage") : 0.0F;
                        float tempSpiritDamageMultiplier = attackerData.method_10545("tempSpiritDamageMultiplier")
                           ? (float)attackerData.method_10574("tempSpiritDamageMultiplier")
                           : 1.0F;
                        float totalSpiritDamage = playerSpiritDamage * tempSpiritDamageMultiplier + tempSpiritDamage;
                        float spiritDamage = weaponDamageBonus + totalSpiritDamage * damageMultiplier;
                        target.method_5643(player.method_48923().method_48802(player), spiritDamage);
                        player.method_37908()
                           .method_43128(
                              null,
                              player.method_23317(),
                              player.method_23318(),
                              player.method_23321(),
                              ModSounds.SWING_ATTACK_SOUND,
                              SoundCategory.field_15251,
                              1.0F,
                              1.0F
                           );
                        player.method_7357().method_7906(stack.method_7909(), 10);
                        if (stack.method_7909() instanceof FissuredSpearPurpleItem && FissuredSpearPurpleItem.isShowWishText(stack)) {
                           player.method_7353(Text.method_43470("§6我说这一刀砍下必定命中眼前生物"), false);
                        }
                     }
                  }
               }
            );
         }
      );
   }
}
