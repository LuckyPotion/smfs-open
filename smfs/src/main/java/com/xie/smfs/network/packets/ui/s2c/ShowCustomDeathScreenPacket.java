package com.xie.smfs.network.packets.ui.s2c;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ShowCustomDeathScreenPacket {
   public static final Identifier ID = new Identifier("smfs", "show_custom_death_screen");
   private final Text deathMessage;
   private final boolean isSilentGhostReinvade;

   public ShowCustomDeathScreenPacket(Text deathMessage) {
      this.deathMessage = deathMessage;
      this.isSilentGhostReinvade = false;
   }

   public ShowCustomDeathScreenPacket(Text deathMessage, boolean isSilentGhostReinvade) {
      this.deathMessage = deathMessage;
      this.isSilentGhostReinvade = isSilentGhostReinvade;
   }

   public ShowCustomDeathScreenPacket(PacketByteBuf buf) {
      this.deathMessage = buf.readText();
      this.isSilentGhostReinvade = buf.readBoolean();
   }

   public void write(PacketByteBuf buf) {
      buf.writeText(this.deathMessage);
      buf.writeBoolean(this.isSilentGhostReinvade);
   }

   public Text getDeathMessage() {
      return this.deathMessage;
   }

   public boolean isSilentGhostReinvade() {
      return this.isSilentGhostReinvade;
   }
}
