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
   public static final EntityType<FoodGhostEntity> FOOD_GHOST = (EntityType<FoodGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "food_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, FoodGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<BlockGhostEntity> BLOCK_GHOST = (EntityType<BlockGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "block_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, BlockGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<TaitouGhostEntity> TAITOU_GHOST = (EntityType<TaitouGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "taitou_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TaitouGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<DitouGhostEntity> DITOU_GHOST = (EntityType<DitouGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ditou_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DitouGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<BoxGhostEntity> BOX_GHOST = (EntityType<BoxGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "box_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, BoxGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<VillagerGhostEntity> VILLAGER_GHOST = (EntityType<VillagerGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "villager_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VillagerGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<JumpGhostEntity> JUMP_GHOST = (EntityType<JumpGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "jump_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, JumpGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<UntouchableGhostEntity> UNTOUCHABLE_GHOST = (EntityType<UntouchableGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "untouchable_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, UntouchableGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<DeathSightGhostEntity> DEATH_SIGHT_GHOST = (EntityType<DeathSightGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "death_sight_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DeathSightGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<QiaomenGhostEntity> QIAOMEN_GHOST = (EntityType<QiaomenGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "qiaomen_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, QiaomenGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostMerchantEntity> GHOST_MERCHANT = (EntityType<GhostMerchantEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_merchant"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostMerchantEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<FogGhostEntity> FOG_GHOST = (EntityType<FogGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "fog_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, FogGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<LostGhostEntity> LOST_GHOST = (EntityType<LostGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "lost_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, LostGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<PlayerGhostEntity> PLAYER_GHOST = (EntityType<PlayerGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "player_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, PlayerGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostSlaveEntity> GHOST_SLAVE = (EntityType<GhostSlaveEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_slave"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostSlaveEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GiantShadowGhostEntity> GIANT_SHADOW_GHOST = (EntityType<GiantShadowGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "giant_shadow_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GiantShadowGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GanshiBrideGhostEntity> GANSHI_BRIDE_GHOST = (EntityType<GanshiBrideGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ganshi_bride_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GanshiBrideGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<CropGhostEntity> CROP_GHOST = (EntityType<CropGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "crop_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CropGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<StepGhostEntity> STEP_GHOST = (EntityType<StepGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "step_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, StepGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<TrashGhostEntity> TRASH_GHOST = (EntityType<TrashGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "trash_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, TrashGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<WaterGhostEntity> WATER_GHOST = (EntityType<WaterGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "water_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, WaterGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<BurnGhostEntity> BURN_GHOST = (EntityType<BurnGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "burn_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, BurnGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<CryingGhostEntity> CRYING_GHOST = (EntityType<CryingGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "crying_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CryingGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<SuonaGhostEntity> SUONA_GHOST = (EntityType<SuonaGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "suona_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SuonaGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<SilentGhostEntity> SILENT_GHOST = (EntityType<SilentGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "silent_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SilentGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostWindEntity> GHOST_WIND = (EntityType<GhostWindEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_wind"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostWindEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GoodsSellerGhostEntity> GOODS_SELLER_GHOST = (EntityType<GoodsSellerGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "goods_seller_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GoodsSellerGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostPressureEntity> GHOST_PRESSURE = (EntityType<GhostPressureEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_pressure"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostPressureEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GongGhostEntity> GONG_GHOST = (EntityType<GongGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "gong_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GongGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<WangXiaoMingEntity> WANG_XIAO_MING = (EntityType<WangXiaoMingEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "wangxiaoming"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WangXiaoMingEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<ChenDoctorEntity> CHEN_DOCTOR = (EntityType<ChenDoctorEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "chen_doctor"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ChenDoctorEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<CaoYanHuaEntity> CAO_YAN_HUA = (EntityType<CaoYanHuaEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "cao_yan_hua"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CaoYanHuaEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiuXiaoYuEntity> LIU_XIAO_YU = (EntityType<LiuXiaoYuEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "liu_xiao_yu"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, LiuXiaoYuEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<YangJianEntity> YANG_JIAN = (EntityType<YangJianEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "yangjian"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, YangJianEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiJunEntity> LI_JUN = (EntityType<LiJunEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "lijun"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, LiJunEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<YeZhenEntity> YE_ZHEN = (EntityType<YeZhenEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "yezhen"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, YeZhenEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<FengQuanEntity> FENG_QUAN = (EntityType<FengQuanEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "fengquan"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FengQuanEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<CaoYangEntity> CAO_YANG = (EntityType<CaoYangEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "caoyang"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CaoYangEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<FangShiMinEntity> FANG_SHI_MIN = (EntityType<FangShiMinEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "fangshimin"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FangShiMinEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC1Entity> NPC1 = (EntityType<NPC1Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc1"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC1Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC2Entity> NPC2 = (EntityType<NPC2Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc2"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC2Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC3Entity> NPC3 = (EntityType<NPC3Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc3"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC3Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC4Entity> NPC4 = (EntityType<NPC4Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc4"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC4Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC5Entity> NPC5 = (EntityType<NPC5Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc5"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC5Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<NPC6Entity> NPC6 = (EntityType<NPC6Entity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "npc6"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPC6Entity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<YanLiEntity> YAN_LI = (EntityType<YanLiEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "yanli"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, YanLiEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<LiLePingEntity> LI_LE_PING = (EntityType<LiLePingEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "lileping"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, LiLePingEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<ZhaoKaiMingEntity> ZHAO_KAI_MING = (EntityType<ZhaoKaiMingEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "zhaokaiming"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, ZhaoKaiMingEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<YangXiaoEntity> YANG_XIAO = (EntityType<YangXiaoEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "yangxiao"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, YangXiaoEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GoldenBulletEntity> GOLDEN_BULLET = (EntityType<GoldenBulletEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "golden_bullet"),
      FabricEntityTypeBuilder.create(SpawnGroup.MISC, GoldenBulletEntity::new)
         .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
         .trackRangeChunks(4)
         .trackedUpdateRate(10)
         .build()
   );
   public static final EntityType<FissuredSpearEntity> FISSURED_SPEAR = (EntityType<FissuredSpearEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "fissured_spear"),
      FabricEntityTypeBuilder.create(SpawnGroup.MISC, FissuredSpearEntity::new)
         .dimensions(EntityDimensions.fixed(0.5F, 1.5F))
         .trackRangeChunks(4)
         .trackedUpdateRate(10)
         .build()
   );
   public static final EntityType<PlagueGhostEntity> PLAGUE_GHOST = (EntityType<PlagueGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "plague_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, PlagueGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<MineralGhostEntity> MINERAL_GHOST = (EntityType<MineralGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "mineral_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MineralGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<ShadowGhostEntity> SHADOW_GHOST = (EntityType<ShadowGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "shadow_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ShadowGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostShadowHeadEntity> GHOST_SHADOW_HEAD = (EntityType<GhostShadowHeadEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_shadow_head"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostShadowHeadEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<DoorGhostEntity> DOOR_GHOST = (EntityType<DoorGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "door_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, DoorGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostSmokeEntity> GHOST_SMOKE = (EntityType<GhostSmokeEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_smoke"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostSmokeEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<SneakGhostEntity> SNEAK_GHOST = (EntityType<SneakGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "sneak_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, SneakGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<ClothesGhostEntity> CLOTHES_GHOST = (EntityType<ClothesGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "clothes_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ClothesGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<PuppetGhostEntity> PUPPET_GHOST = (EntityType<PuppetGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "puppet_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, PuppetGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostOfficerEntity> GHOST_OFFICER = (EntityType<GhostOfficerEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_officer"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostOfficerEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GhostChildEntity> GHOST_CHILD = (EntityType<GhostChildEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_child"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostChildEntity::new).dimensions(EntityDimensions.fixed(0.4F, 1.0F)).build()
   );
   public static final EntityType<GiantMaleCorpseGhostEntity> GIANT_MALE_CORPSE_GHOST = (EntityType<GiantMaleCorpseGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "giant_male_corpse_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GiantMaleCorpseGhostEntity::new).dimensions(EntityDimensions.fixed(0.8F, 2.5F)).build()
   );
   public static final EntityType<GhostDreamEntity> GHOST_DREAM = (EntityType<GhostDreamEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "ghost_dream"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GhostDreamEntity::new).dimensions(EntityDimensions.fixed(0.6F, 0.8F)).build()
   );
   public static final EntityType<XinKaiGhostEntity> XINKAI_GHOST = (EntityType<XinKaiGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "xinkai_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, XinKaiGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<YinQiEntity> YIN_QI = (EntityType<YinQiEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "yin_qi"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, YinQiEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<XianWangEntity> XIAN_WANG = (EntityType<XianWangEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "xian_wang"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, XianWangEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<GuiNiaoEntity> GUI_NIAO = (EntityType<GuiNiaoEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "gui_niao"),
      FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GuiNiaoEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
   public static final EntityType<LuoQianGhostEntity> LUO_QIAN_GHOST = (EntityType<LuoQianGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "luo_qian_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, LuoQianGhostEntity::new).dimensions(EntityDimensions.fixed(0.7F, 2.2F)).build()
   );
   public static final EntityType<GraveWarningEntity> GRAVE_WARNING = (EntityType<GraveWarningEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "grave_warning"),
      FabricEntityTypeBuilder.create(SpawnGroup.MISC, GraveWarningEntity::new).dimensions(EntityDimensions.fixed(1.0F, 0.1F)).build()
   );
   public static final EntityType<FloatingDirtEntity> FLOATING_DIRT = (EntityType<FloatingDirtEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "floating_dirt"),
      FabricEntityTypeBuilder.create(SpawnGroup.MISC, FloatingDirtEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.5F)).build()
   );
   public static final EntityType<GraveEarthGhostEntity> GRAVE_EARTH_GHOST = (EntityType<GraveEarthGhostEntity>)Registry.register(
      Registries.ENTITY_TYPE,
      new Identifier("smfs", "grave_earth_ghost"),
      FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, GraveEarthGhostEntity::new).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build()
   );
}
