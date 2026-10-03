package com.xie.smfs.mixin.server;

import com.xie.smfs.api.IPlayerData;
import com.xie.smfs.common.events.PlayerEvents;
import com.xie.smfs.effect.SpiritAttributes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements IPlayerData {
   @Unique
   private static final Logger LOGGER = LoggerFactory.getLogger(PlayerEntityMixin.class);
   @Unique
   private int customProperty = 0;
   @Unique
   private NbtCompound spiritData = new NbtCompound();

   @Override
   public int getCustomProperty() {
      return this.customProperty;
   }

   @Override
   public void setCustomProperty(int value) {
      this.customProperty = value;
   }

   @Override
   public float getSpiritResistance() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SPIRIT_RESISTANCE);
   }

   @Override
   public void setSpiritResistance(float value) {
      PlayerEntity player = (PlayerEntity)this;
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.SPIRIT_RESISTANCE);
      if (instance != null) {
         instance.setBaseValue(value);
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putDouble("spiritResistance", value);
      PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), data);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   @Override
   public float getSpiritDamage() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SPIRIT_DAMAGE);
   }

   @Override
   public void setSpiritDamage(float value) {
      PlayerEntity player = (PlayerEntity)this;
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.SPIRIT_DAMAGE);
      if (instance != null) {
         instance.setBaseValue(value);
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putDouble("spiritDamage", value);
      PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), data);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   @Override
   public float getCurrentSpirit() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getCurrentSpirit(player);
   }

   @Override
   public void setCurrentSpirit(float value) {
      PlayerEntity player = (PlayerEntity)this;
      PlayerEvents.setCurrentSpirit(player, value);
   }

   @Override
   public float getMaxSpirit() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getMaxSpirit(player);
   }

   @Override
   public void setMaxSpirit(float value) {
      PlayerEntity player = (PlayerEntity)this;
      PlayerEvents.setMaxSpirit(player, value);
   }

   @Override
   public double getRevivalFactor() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getSpiritAttribute(player, SpiritAttributes.REVIVAL_FACTOR);
   }

   @Override
   public void setRevivalFactor(double value) {
      PlayerEntity player = (PlayerEntity)this;
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.REVIVAL_FACTOR);
      if (instance != null) {
         instance.setBaseValue((float)value);
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putDouble("revivalFactor", value);
      PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), data);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   @Override
   public float getSanity() {
      PlayerEntity player = (PlayerEntity)this;
      return PlayerEvents.getSpiritAttribute(player, SpiritAttributes.SANITY);
   }

   @Override
   public void setSanity(float value) {
      PlayerEntity player = (PlayerEntity)this;
      EntityAttributeInstance instance = player.getAttributeInstance(SpiritAttributes.SANITY);
      if (instance != null) {
         instance.setBaseValue(value);
      }

      NbtCompound data = PlayerEvents.getCachedData(player);
      data.putDouble("sanity", value);
      PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), data);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   @Override
   public NbtCompound getSpiritData() {
      return this.spiritData;
   }

   @Override
   public void setSpiritData(NbtCompound data) {
      this.spiritData = data.copy();
   }

   @Override
   public void copyFrom(IPlayerData other) {
      LOGGER.info("copyFrom复制的数据: {}", other.getSpiritData());
      this.setCustomProperty(other.getCustomProperty());
      this.setSpiritResistance(other.getSpiritResistance());
      this.setSpiritDamage(other.getSpiritDamage());
      this.setCurrentSpirit(other.getCurrentSpirit());
      this.setMaxSpirit(other.getMaxSpirit());
      this.setRevivalFactor(other.getRevivalFactor());
      this.setSanity(other.getSanity());
      this.setSpiritData(other.getSpiritData().copy());
   }

   @Override
   public NbtCompound getQuestData() {
      PlayerEntity player = (PlayerEntity)this;
      NbtCompound data = PlayerEvents.getCachedData(player);
      return data.contains("questData") ? data.getCompound("questData") : new NbtCompound();
   }

   @Override
   public void setQuestData(NbtCompound questData) {
      PlayerEntity player = (PlayerEntity)this;
      NbtCompound data = PlayerEvents.getCachedData(player);
      data.put("questData", questData.copy());
      PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), data);
      PlayerEvents.setSpiritAttributes(player, data);
   }

   @Override
   public List<NbtCompound> getActiveQuests() {
      PlayerEntity player = (PlayerEntity)this;
      NbtCompound questData = this.getQuestData();
      List<NbtCompound> activeQuests = new ArrayList<>();
      if (questData.contains("activeQuests")) {
         NbtList activeList = questData.getList("activeQuests", 10);

         for (int i = 0; i < activeList.size(); i++) {
            activeQuests.add(activeList.getCompound(i));
         }
      }

      return activeQuests;
   }

   @Override
   public boolean hasCompletedQuest(String questId) {
      PlayerEntity player = (PlayerEntity)this;
      NbtCompound questData = this.getQuestData();
      if (questData.contains("completedQuests")) {
         NbtList completedList = questData.getList("completedQuests", 8);

         for (int i = 0; i < completedList.size(); i++) {
            if (completedList.getString(i).equals(questId)) {
               return true;
            }
         }
      }

      return false;
   }

   @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
   private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
      PlayerEntity player = (PlayerEntity)this;
      NbtCompound spiritData = this.spiritData;

      for (String key : spiritData.getKeys()) {
         NbtElement element = spiritData.get(key);
         if (key.equals("GhostSlots")) {
            NbtCompound ghostSlots = (NbtCompound)element;
            int occupiedCount = 0;

            for (String slotKey : ghostSlots.getKeys()) {
               NbtCompound slot = ghostSlots.getCompound(slotKey);
               if (slot.getBoolean("occupied")) {
                  occupiedCount++;
               }
            }
         }
      }

      nbt.put("smfs_spirit_data", spiritData);
      nbt.putInt("customProperty", this.customProperty);
   }

   @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
   private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
      PlayerEntity player = (PlayerEntity)this;
      if (nbt.contains("smfs_spirit_data")) {
         this.spiritData = nbt.getCompound("smfs_spirit_data").copy();

         for (String key : this.spiritData.getKeys()) {
            NbtElement var6 = this.spiritData.get(key);
         }

         PlayerEvents.PLAYER_DATA_CACHE.put(player.getUuid(), this.spiritData.copy());
         PlayerEvents.syncAttributesFromNbt(player, this.spiritData);
         this.customProperty = nbt.getInt("customProperty");
      } else {
         PlayerEvents.initSpiritData(player);
         this.spiritData = PlayerEvents.getSpiritAttributes(player).copy();
         LOGGER.info("已为玩家初始化新的灵异数据");
         nbt.put("smfs_spirit_data", this.spiritData.copy());
      }
   }
}
