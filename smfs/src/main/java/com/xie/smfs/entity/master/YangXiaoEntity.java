package com.xie.smfs.entity.master;

import com.xie.smfs.entity.GhostMasterEntity;
import com.xie.smfs.faction.PlayerFaction;
import com.xie.smfs.manager.GhostDreamManager;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class YangXiaoEntity extends GhostMasterEntity {
   private static final List<String> STORY_DIALOGUES = Arrays.asList(
      "你不该出现在这里。", "这里是鬼梦的世界，所有的灵异手段在这里都会失效。", "看到你让我想起来一个人，现在看来他是对的，我终于理解了他的用意。", "接下来我会帮助你离开这里。把这个带在身上，只要存活到天亮，你就能够驾驭它了。"
   );

   public YangXiaoEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
      super(entityType, world);
      this.setGhostDomainLevel(0);
      this.setGhostDomainRadius(0.0);
      this.setSpiritualStrength(3000);
      this.setSpiritualDamage(50);
      this.setSpiritualResistance(100);
      this.setRecoveryFactor(0.05F);
      this.shouldFleeFromGhosts = false;
      this.shouldAttackPlayers = false;
      this.shouldProtectPlayers = true;
      this.shouldAttackGhostsNearPlayers = true;
      this.setCustomName(Text.literal("§5[鬼梦]杨孝"));
      this.setCustomNameVisible(true);
      this.faction = PlayerFaction.FOLK_GHOST_MASTER;
   }

   public static Builder createYangXiaoAttributes() {
      return GhostMasterEntity.createGhostMasterAttributes();
   }

   @Override
   protected void initGoals() {
      super.initGoals();
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected String getGhostMasterDisplayName() {
      return "杨孝";
   }

   @Override
   public List<String> getStoryDialogues() {
      return STORY_DIALOGUES;
   }

   @Override
   public boolean hasCompletedStory(PlayerEntity player) {
      return GhostDreamManager.hasTalkedToYangXiao(player);
   }

   @Override
   public void markStoryCompleted(PlayerEntity player) {
      GhostDreamManager.markTalkedToYangXiao(player);
   }

   @Override
   public String[] getGreetingDialogues() {
      return new String[0];
   }
}
