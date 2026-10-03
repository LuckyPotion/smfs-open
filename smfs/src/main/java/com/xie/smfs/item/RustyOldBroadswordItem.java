package com.xie.smfs.item;

import com.xie.smfs.client.renderer.item.RustyOldBroadswordRenderer;
import com.xie.smfs.entity.GhostEntity;
import com.xie.smfs.util.GhostUtils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.Item.Settings;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtils;

public class RustyOldBroadswordItem extends SwordItem implements SpiritWeapon, GeoItem {
   private static final Logger LOGGER = LoggerFactory.getLogger(RustyOldBroadswordItem.class);
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final String RANGED_MODE_KEY = "RangedMode";

   public RustyOldBroadswordItem(Settings settings) {
      super(ToolMaterials.field_8923, 8, -2.4F, settings);
   }

   public static boolean isRangedMode(ItemStack stack) {
      return !stack.method_7985() ? false : stack.method_7969().method_10577("RangedMode");
   }

   public static void setRangedMode(ItemStack stack, boolean ranged) {
      stack.method_7948().method_10556("RangedMode", ranged);
   }

   public TypedActionResult<ItemStack> method_7836(World world, PlayerEntity user, Hand hand) {
      ItemStack stack = user.method_5998(hand);
      if (isRangedMode(stack)) {
         setRangedMode(stack, false);
         if (!world.field_9236) {
            user.method_7353(Text.method_43470("§7大刀切换为劈砍模式"), true);
         }
      } else {
         setRangedMode(stack, true);
         if (!world.field_9236) {
            user.method_7353(Text.method_43470("§c大刀切换为媒介模式"), true);
         }
      }

      return TypedActionResult.method_29237(stack, world.method_8608());
   }

   public boolean method_7846() {
      return false;
   }

   @Override
   public float getSpiritDamageBonus() {
      return 180.0F;
   }

   @Override
   public float getSpiritDamageMultiplier() {
      return 1.3F;
   }

   @Override
   public void onSpiritWeaponAttack(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (!attacker.method_37908().method_8608()
         && attacker.method_5805()
         && target instanceof GhostEntity ghost
         && target.method_6051().method_43048(100) == 0) {
         String ghostType = GhostUtils.getGhostTypeFromEntity(ghost);
         if (ghostType == null) {
            LOGGER.warn("锈迹大刀攻击的厉鬼 {} 未找到对应的鬼类型映射", target.method_5864().method_5882());
            return;
         }

         Item ghostItem = GhostUtils.getGhostItemByType(ghostType);
         if (ghostItem == null) {
            LOGGER.warn("鬼类型 {} 未找到对应的驾驭物品映射", ghostType);
            return;
         }

         ItemStack shard = new ItemStack(ghostItem);
         BaseGhostEyeItem.setShard(shard, true);
         target.method_37908().method_8649(new ItemEntity(target.method_37908(), target.method_23317(), target.method_23318(), target.method_23321(), shard));
         LOGGER.info("锈迹大刀攻击 {} 触发碎片掉落: {}", ghostType, ghostItem.method_7876());
      }
   }

   public void method_7851(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
      super.method_7851(stack, world, tooltip, context);
      tooltip.add(Text.method_43471("item.smfs.rusty_old_broadsword.description.source"));
      tooltip.add(Text.method_43471("item.smfs.rusty_old_broadsword.description.desc"));
      tooltip.add(Text.method_43471("item.smfs.rusty_old_broadsword.description.type"));
      tooltip.add(Text.method_43471("item.smfs.rusty_old_broadsword.effect.shard_drop"));
      tooltip.add(Text.method_43471("item.smfs.rusty_old_broadsword.mode_switch"));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_bonus", new Object[]{this.getSpiritDamageBonus()}));
      tooltip.add(Text.method_43469("item.smfs.spirit_weapon.damage_multiplier", new Object[]{this.getSpiritDamageMultiplier() * 100.0F}));
   }

   @Override
   public void registerControllers(ControllerRegistrar registrar) {
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   @Override
   public double getTick(Object itemStack) {
      return RenderUtils.getCurrentTick();
   }

   @Override
   public void createRenderer(Consumer<Object> consumer) {
      consumer.accept(new RustyOldBroadswordRenderer());
   }

   @Override
   public Supplier<Object> getRenderProvider() {
      return GeoItem.makeRenderer(this);
   }
}
