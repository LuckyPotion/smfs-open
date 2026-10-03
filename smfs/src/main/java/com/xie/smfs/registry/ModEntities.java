package com.xie.smfs.registry;

import com.xie.smfs.entity.ghost.BlockGhostEntity;
import com.xie.smfs.entity.ghost.BoxGhostEntity;
import com.xie.smfs.entity.ghost.BurnGhostEntity;
import com.xie.smfs.entity.ghost.ClothesGhostEntity;
import com.xie.smfs.entity.ghost.CropGhostEntity;
import com.xie.smfs.entity.ghost.CryingGhostEntity;
import com.xie.smfs.entity.ghost.DeathSightGhostEntity;
import com.xie.smfs.entity.ghost.DitouGhostEntity;
import com.xie.smfs.entity.ghost.DoorGhostEntity;
import com.xie.smfs.entity.ghost.FogGhostEntity;
import com.xie.smfs.entity.ghost.FoodGhostEntity;
import com.xie.smfs.entity.ghost.GanshiBrideGhostEntity;
import com.xie.smfs.entity.ghost.GhostChildEntity;
import com.xie.smfs.entity.ghost.GhostDreamEntity;
import com.xie.smfs.entity.ghost.GhostMerchantEntity;
import com.xie.smfs.entity.ghost.GhostOfficerEntity;
import com.xie.smfs.entity.ghost.GhostPressureEntity;
import com.xie.smfs.entity.ghost.GhostShadowHeadEntity;
import com.xie.smfs.entity.ghost.GhostSmokeEntity;
import com.xie.smfs.entity.ghost.GhostWindEntity;
import com.xie.smfs.entity.ghost.GiantMaleCorpseGhostEntity;
import com.xie.smfs.entity.ghost.GiantShadowGhostEntity;
import com.xie.smfs.entity.ghost.GongGhostEntity;
import com.xie.smfs.entity.ghost.GoodsSellerGhostEntity;
import com.xie.smfs.entity.ghost.GraveEarthGhostEntity;
import com.xie.smfs.entity.ghost.JumpGhostEntity;
import com.xie.smfs.entity.ghost.LostGhostEntity;
import com.xie.smfs.entity.ghost.LuoQianGhostEntity;
import com.xie.smfs.entity.ghost.MineralGhostEntity;
import com.xie.smfs.entity.ghost.PlagueGhostEntity;
import com.xie.smfs.entity.ghost.PuppetGhostEntity;
import com.xie.smfs.entity.ghost.QiaomenGhostEntity;
import com.xie.smfs.entity.ghost.ShadowGhostEntity;
import com.xie.smfs.entity.ghost.SilentGhostEntity;
import com.xie.smfs.entity.ghost.SneakGhostEntity;
import com.xie.smfs.entity.ghost.StepGhostEntity;
import com.xie.smfs.entity.ghost.SuonaGhostEntity;
import com.xie.smfs.entity.ghost.TaitouGhostEntity;
import com.xie.smfs.entity.ghost.TrashGhostEntity;
import com.xie.smfs.entity.ghost.UntouchableGhostEntity;
import com.xie.smfs.entity.ghost.VillagerGhostEntity;
import com.xie.smfs.entity.ghost.WaterGhostEntity;
import com.xie.smfs.entity.ghost.XinKaiGhostEntity;
import com.xie.smfs.entity.master.CaoYangEntity;
import com.xie.smfs.entity.master.FangShiMinEntity;
import com.xie.smfs.entity.master.FengQuanEntity;
import com.xie.smfs.entity.master.GuiNiaoEntity;
import com.xie.smfs.entity.master.LiJunEntity;
import com.xie.smfs.entity.master.LiLePingEntity;
import com.xie.smfs.entity.master.NPC1Entity;
import com.xie.smfs.entity.master.NPC2Entity;
import com.xie.smfs.entity.master.NPC3Entity;
import com.xie.smfs.entity.master.NPC4Entity;
import com.xie.smfs.entity.master.NPC5Entity;
import com.xie.smfs.entity.master.NPC6Entity;
import com.xie.smfs.entity.master.XianWangEntity;
import com.xie.smfs.entity.master.YanLiEntity;
import com.xie.smfs.entity.master.YangJianEntity;
import com.xie.smfs.entity.master.YangXiaoEntity;
import com.xie.smfs.entity.master.YeZhenEntity;
import com.xie.smfs.entity.master.YinQiEntity;
import com.xie.smfs.entity.master.ZhaoKaiMingEntity;
import com.xie.smfs.entity.other.CaoYanHuaEntity;
import com.xie.smfs.entity.other.ChenDoctorEntity;
import com.xie.smfs.entity.other.FissuredSpearEntity;
import com.xie.smfs.entity.other.FloatingDirtEntity;
import com.xie.smfs.entity.other.GhostSlaveEntity;
import com.xie.smfs.entity.other.GoldenBulletEntity;
import com.xie.smfs.entity.other.GraveWarningEntity;
import com.xie.smfs.entity.other.LiuXiaoYuEntity;
import com.xie.smfs.entity.other.PlayerGhostEntity;
import com.xie.smfs.entity.other.WangXiaoMingEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
   public static final EntityType<FoodGhostEntity> FOOD_GHOST = (EntityType<FoodGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "food_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, FoodGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<BlockGhostEntity> BLOCK_GHOST = (EntityType<BlockGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "block_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, BlockGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<TaitouGhostEntity> TAITOU_GHOST = (EntityType<TaitouGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "taitou_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, TaitouGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<DitouGhostEntity> DITOU_GHOST = (EntityType<DitouGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ditou_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, DitouGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<BoxGhostEntity> BOX_GHOST = (EntityType<BoxGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "box_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, BoxGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<VillagerGhostEntity> VILLAGER_GHOST = (EntityType<VillagerGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "villager_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, VillagerGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<JumpGhostEntity> JUMP_GHOST = (EntityType<JumpGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "jump_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, JumpGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<UntouchableGhostEntity> UNTOUCHABLE_GHOST = (EntityType<UntouchableGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "untouchable_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, UntouchableGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<DeathSightGhostEntity> DEATH_SIGHT_GHOST = (EntityType<DeathSightGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "death_sight_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, DeathSightGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<QiaomenGhostEntity> QIAOMEN_GHOST = (EntityType<QiaomenGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "qiaomen_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, QiaomenGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostMerchantEntity> GHOST_MERCHANT = (EntityType<GhostMerchantEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_merchant"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostMerchantEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<FogGhostEntity> FOG_GHOST = (EntityType<FogGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "fog_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, FogGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<LostGhostEntity> LOST_GHOST = (EntityType<LostGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "lost_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, LostGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<PlayerGhostEntity> PLAYER_GHOST = (EntityType<PlayerGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "player_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, PlayerGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostSlaveEntity> GHOST_SLAVE = (EntityType<GhostSlaveEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_slave"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostSlaveEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GiantShadowGhostEntity> GIANT_SHADOW_GHOST = (EntityType<GiantShadowGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "giant_shadow_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GiantShadowGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GanshiBrideGhostEntity> GANSHI_BRIDE_GHOST = (EntityType<GanshiBrideGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ganshi_bride_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GanshiBrideGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<CropGhostEntity> CROP_GHOST = (EntityType<CropGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "crop_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, CropGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<StepGhostEntity> STEP_GHOST = (EntityType<StepGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "step_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, StepGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<TrashGhostEntity> TRASH_GHOST = (EntityType<TrashGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "trash_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, TrashGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<WaterGhostEntity> WATER_GHOST = (EntityType<WaterGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "water_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, WaterGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<BurnGhostEntity> BURN_GHOST = (EntityType<BurnGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "burn_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, BurnGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<CryingGhostEntity> CRYING_GHOST = (EntityType<CryingGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "crying_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, CryingGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<SuonaGhostEntity> SUONA_GHOST = (EntityType<SuonaGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "suona_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, SuonaGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<SilentGhostEntity> SILENT_GHOST = (EntityType<SilentGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "silent_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, SilentGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostWindEntity> GHOST_WIND = (EntityType<GhostWindEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_wind"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostWindEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GoodsSellerGhostEntity> GOODS_SELLER_GHOST = (EntityType<GoodsSellerGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "goods_seller_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GoodsSellerGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostPressureEntity> GHOST_PRESSURE = (EntityType<GhostPressureEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_pressure"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostPressureEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GongGhostEntity> GONG_GHOST = (EntityType<GongGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "gong_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GongGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<WangXiaoMingEntity> WANG_XIAO_MING = (EntityType<WangXiaoMingEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "wangxiaoming"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, WangXiaoMingEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<ChenDoctorEntity> CHEN_DOCTOR = (EntityType<ChenDoctorEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "chen_doctor"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, ChenDoctorEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<CaoYanHuaEntity> CAO_YAN_HUA = (EntityType<CaoYanHuaEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "cao_yan_hua"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, CaoYanHuaEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiuXiaoYuEntity> LIU_XIAO_YU = (EntityType<LiuXiaoYuEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "liu_xiao_yu"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, LiuXiaoYuEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<YangJianEntity> YANG_JIAN = (EntityType<YangJianEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "yangjian"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, YangJianEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiJunEntity> LI_JUN = (EntityType<LiJunEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "lijun"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, LiJunEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<YeZhenEntity> YE_ZHEN = (EntityType<YeZhenEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "yezhen"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, YeZhenEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<FengQuanEntity> FENG_QUAN = (EntityType<FengQuanEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "fengquan"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, FengQuanEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<CaoYangEntity> CAO_YANG = (EntityType<CaoYangEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "caoyang"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, CaoYangEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<FangShiMinEntity> FANG_SHI_MIN = (EntityType<FangShiMinEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "fangshimin"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, FangShiMinEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC1Entity> NPC1 = (EntityType<NPC1Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc1"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC1Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC2Entity> NPC2 = (EntityType<NPC2Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc2"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC2Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC3Entity> NPC3 = (EntityType<NPC3Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc3"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC3Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC4Entity> NPC4 = (EntityType<NPC4Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc4"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC4Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC5Entity> NPC5 = (EntityType<NPC5Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc5"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC5Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC6Entity> NPC6 = (EntityType<NPC6Entity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "npc6"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, NPC6Entity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<YanLiEntity> YAN_LI = (EntityType<YanLiEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "yanli"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, YanLiEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiLePingEntity> LI_LE_PING = (EntityType<LiLePingEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "lileping"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, LiLePingEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<ZhaoKaiMingEntity> ZHAO_KAI_MING = (EntityType<ZhaoKaiMingEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "zhaokaiming"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, ZhaoKaiMingEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<YangXiaoEntity> YANG_XIAO = (EntityType<YangXiaoEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "yangxiao"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, YangXiaoEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GoldenBulletEntity> GOLDEN_BULLET = (EntityType<GoldenBulletEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "golden_bullet"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_17715, GoldenBulletEntity::new)
         .dimensions(EntityDimensions.method_18385(0.25F, 0.25F))
         .trackRangeChunks(4)
         .trackedUpdateRate(10)
         .build()
   );
   public static final EntityType<FissuredSpearEntity> FISSURED_SPEAR = (EntityType<FissuredSpearEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "fissured_spear"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_17715, FissuredSpearEntity::new)
         .dimensions(EntityDimensions.method_18385(0.5F, 1.5F))
         .trackRangeChunks(4)
         .trackedUpdateRate(10)
         .build()
   );
   public static final EntityType<PlagueGhostEntity> PLAGUE_GHOST = (EntityType<PlagueGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "plague_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, PlagueGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<MineralGhostEntity> MINERAL_GHOST = (EntityType<MineralGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "mineral_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, MineralGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<ShadowGhostEntity> SHADOW_GHOST = (EntityType<ShadowGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "shadow_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, ShadowGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostShadowHeadEntity> GHOST_SHADOW_HEAD = (EntityType<GhostShadowHeadEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_shadow_head"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostShadowHeadEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<DoorGhostEntity> DOOR_GHOST = (EntityType<DoorGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "door_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, DoorGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostSmokeEntity> GHOST_SMOKE = (EntityType<GhostSmokeEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_smoke"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostSmokeEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<SneakGhostEntity> SNEAK_GHOST = (EntityType<SneakGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "sneak_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, SneakGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<ClothesGhostEntity> CLOTHES_GHOST = (EntityType<ClothesGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "clothes_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, ClothesGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<PuppetGhostEntity> PUPPET_GHOST = (EntityType<PuppetGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "puppet_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, PuppetGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostOfficerEntity> GHOST_OFFICER = (EntityType<GhostOfficerEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_officer"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostOfficerEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostChildEntity> GHOST_CHILD = (EntityType<GhostChildEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_child"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostChildEntity::new).dimensions(EntityDimensions.method_18385(0.4F, 1.0F)).build()
   );
   public static final EntityType<GiantMaleCorpseGhostEntity> GIANT_MALE_CORPSE_GHOST = (EntityType<GiantMaleCorpseGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "giant_male_corpse_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GiantMaleCorpseGhostEntity::new).dimensions(EntityDimensions.method_18385(0.8F, 2.5F)).build()
   );
   public static final EntityType<GhostDreamEntity> GHOST_DREAM = (EntityType<GhostDreamEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "ghost_dream"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GhostDreamEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 0.8F)).build()
   );
   public static final EntityType<XinKaiGhostEntity> XINKAI_GHOST = (EntityType<XinKaiGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "xinkai_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, XinKaiGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<YinQiEntity> YIN_QI = (EntityType<YinQiEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "yin_qi"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, YinQiEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<XianWangEntity> XIAN_WANG = (EntityType<XianWangEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "xian_wang"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, XianWangEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<GuiNiaoEntity> GUI_NIAO = (EntityType<GuiNiaoEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "gui_niao"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6294, GuiNiaoEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
   public static final EntityType<LuoQianGhostEntity> LUO_QIAN_GHOST = (EntityType<LuoQianGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "luo_qian_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, LuoQianGhostEntity::new).dimensions(EntityDimensions.method_18385(0.7F, 2.2F)).build()
   );
   public static final EntityType<GraveWarningEntity> GRAVE_WARNING = (EntityType<GraveWarningEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "grave_warning"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_17715, GraveWarningEntity::new).dimensions(EntityDimensions.method_18385(1.0F, 0.1F)).build()
   );
   public static final EntityType<FloatingDirtEntity> FLOATING_DIRT = (EntityType<FloatingDirtEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "floating_dirt"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_17715, FloatingDirtEntity::new).dimensions(EntityDimensions.method_18385(0.5F, 0.5F)).build()
   );
   public static final EntityType<GraveEarthGhostEntity> GRAVE_EARTH_GHOST = (EntityType<GraveEarthGhostEntity>)Registry.method_10230(
      Registries.field_41177,
      new Identifier("smfs", "grave_earth_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.field_6302, GraveEarthGhostEntity::new).dimensions(EntityDimensions.method_18385(0.6F, 1.95F)).build()
   );
}
