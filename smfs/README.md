# 神秘复苏 / Mysterious Revival (SMFS)

Minecraft **Fabric 1.20.1** 模组「神秘复苏」的**反编译源码**。

> ⚠️ **非官方源码。** 全部 Java 代码由 `smfs-1.5.0.jar` 的字节码反编译得到，作者从未发布过源代码。资源文件与原始 jar 逐字节一致。反编译流程见[仓库根目录 README](../README.md) 与 [TOOLS.md](../TOOLS.md)。

## 元数据

| 项 | 值 |
|---|---|
| 模组 ID | `smfs` |
| 名称 | 神秘复苏 |
| 简介 | 我叫杨间，当你看到这句话的时候我已经死了... |
| 版本 | 1.5.0 |
| 作者 | 进击的蟹某人 |
| 许可证 | CC BY-SA 4.0 **+ 附加条款：禁止商业使用**（见 [`LICENSE-smfs`](LICENSE_smfs)） |
| 环境 | 客户端 + 服务端（`"environment": "*"`） |
| Minecraft | `~1.20.1` |
| Java | `>=17` |
| 前置 | `fabricloader >=0.14.21`、`fabric-api`、`geckolib >=4.2.4` |

## 构建环境

原始 jar 的 `META-INF/MANIFEST.MF` 记录了作者使用的工具链，本仓库的构建脚本与之对齐：

| 组件 | 版本 |
|---|---|
| Fabric Loom | 1.10.5 |
| Gradle | 8.14.2 |
| Fabric Loader | 0.17.2 |
| Mixin | 0.16.3+mixin.0.8.7 |
| Yarn 映射 | `1.20.1+build.10` |
| 映射命名空间 | `intermediary`（发布态） |

## 代码规模

| 类别 | 数量 |
|---|---|
| Java 源文件 | 728（对应 890 个 class） |
| 资源文件 | 889 |
| 语言条目 | 1353（`zh_cn`）/ 1353（`en_us`） |
| Mixin | 26（服务端 14 + 客户端 12） |

## 包结构导览

```
com/xie/smfs/
├── Smfs.java                    主入口（ModInitializer）
├── SmfsDataGenerator.java       数据生成器入口
│
├── item/            (155)  物品：鬼物、法器、手记、人皮纸等
├── network/         (142)  网络包，按 c2s / s2c 与业务分组
│   ├── core/               包注册与编解码核心
│   ├── packets/skills/     技能同步（c2s 85 / s2c 6）
│   ├── packets/ui/         界面数据同步
│   ├── packets/quests/     任务系统
│   ├── packets/goodseller/ 鬼商交易
│   ├── packets/ghostchild/ 鬼婴
│   ├── packets/config/     配置同步
│   └── packets/common/     通用包
├── client/          (108)  客户端逻辑
│   ├── renderer/    (53)   实体/方块/物品渲染器（GeckoLib）
│   ├── screen/      (48)   界面
│   ├── model/              模型（armor / block / item）
│   ├── ai/                 人皮纸等 AI 文本提示
│   ├── preset/             预设
│   └── sound/ data/ util/ net/ render/
├── entity/           (75)  实体
│   ├── ghost/       (51)   厉鬼（哭鬼、影鬼、锣鬼、索命鬼…）
│   ├── master/      (19)   驭鬼者 / 主控实体
│   └── other/       (16)   其他（棺材、脚印等）
├── effect/           (40)  状态效果（迷失、许愿诅咒…）
├── block/            (38)  方块（棺材、鬼镜、鬼烛、鬼门…）
│   └── entity/      (20)   方块实体
├── manager/          (31)  系统管理器（ advancement、任务、棺材效果…）
├── event/            (29)  事件与界面事件
├── mixin/            (26)  Mixin（server/ 14、client/ 12、client.sound/ 1）
├── command/          (21)  指令
├── api/              (19)  对外 API（api/common、api/impl）
├── registry/          (9)  注册表
│   ModBlocks / ModItems / ModEntities / ModEffects / ModFluids /
│   ModSounds / ModBlockEntities / ModScreenHandlers / ModStructureType
├── util/              (7)  工具类
├── data/              (5)  数据组件
├── config/            (5)  配置
├── worldgen/          (4)  世界生成
├── recipe/            (3)  配方
├── fluid/             (3)  流体
├── structure/         (2)  结构
├── faction/           (2)  阵营
├── damage/            (1)  伤害类型
└── common/            (1)  通用事件
```

## 资源结构

```
assets/smfs/
├── animations/     22   GeckoLib 动画（各类厉鬼、棺材）
├── geo/            47   GeckoLib 几何模型
├── models/        286   原版风格模型与物品模型
├── textures/      259   贴图
├── blockstates/    26   方块状态
├── sounds/         36   音效
├── lang/            2   zh_cn / en_us
└── tutorials/       2   教程与事件剧情文本

data/smfs/
├── worldgen/       67   生物群系、地物、噪声设置、结构、模板池、世界预设
├── tags/           38   标签
├── structures/     28   结构定义
├── loot_tables/    28   战利品表（blocks / chests / entities / quests）
├── recipes/        21   配方
├── advancements/    8   进度
├── damage_type/     4   伤害类型
├── dimension/       3   维度
├── dimension_type/  3   维度类型
└── quests/          1   任务
```

## Mixin 清单

`smfs.mixins.json` 共注册 **26** 个：服务端（`mixins`）13 个，客户端（`client`）13 个。

<details>
<summary>服务端 (13)</summary>

| 类 | 目标 |
|---|---|
| `server.PlayerEntityMixin` | `PlayerEntity` |
| `server.ItemStackMixin` | `ItemStack` |
| `server.UpdateStructureBlockC2SPacketMixin` | `UpdateStructureBlockC2SPacket` |
| `server.StructureBlockBlockEntityMixin` | `StructureBlockBlockEntity` |
| `server.LivingEntityHealMixin` | `LivingEntity`（`heal`） |
| `server.LivingEntityInstantKillMixin` | `LivingEntity` |
| `server.LivingEntitySetHealthMixin` | `LivingEntity`（`setHealth`） |
| `server.VillagerEntityMixin` | `VillagerEntity` |
| `server.ChunkGeneratorMixin` | `ChunkGenerator` |
| `server.ChunkStatusMixin` | `ChunkStatus` |
| `server.JigsawStructureMixin` | `JigsawStructure` |
| `server.StructureTemplateMixin` | `StructureTemplate` |
| `server.ChunkRegionAccessor` | `ChunkRegion`（`@Accessor`，非注入型） |

</details>

<details>
<summary>客户端 (13)</summary>

| 类 | 目标 |
|---|---|
| `client.LivingEntityMixin` | `LivingEntity` |
| `client.InventoryScreenMixin` | `InventoryScreen` |
| `client.DeathScreenMixin` | `DeathScreen` |
| `client.CreateWorldScreenMixin` | `CreateWorldScreen`（`init`） |
| `client.sound.SoundSystemMixin` | `SoundSystem` |
| `client.PlayerEntityRendererMixin` | `PlayerEntityRenderer` |
| `client.WorldMixin` | `World`（`getRainGradient`） |
| `client.IntegratedServerLoaderMixin` | `IntegratedServerLoader` |
| `client.FirstPersonRendererMixin` | `FirstPersonRenderer` |
| `client.CameraMixin` | `Camera` |
| `client.InGameHudMixin` | `InGameHud`（`renderHotbar` / `renderStatusBars` / `renderExperienceBar`） |
| `client.ClientPlayerEntityMixin` | `ClientPlayerEntity`（`doAttack`） |
| `client.MinecraftClientMixin` | `MinecraftClient` |

</details>

## 阅读源码的注意事项

1. **局部变量名多数保留**。该 jar 由 Fabric Loom 构建，未做混淆，反编译后局部变量名基本可读。
2. **网络包**：`network/packets/skills/c2s` 下有 85 个类，通常每个技能一个包，适合按需查阅。
3. **`@Inject` 的方法名是 Yarn 名**（如 `readCustomDataFromNbt`、`renderStatusBars`），因为重映射时已通过 MixinExtension 同步改写注解载荷；编译时 refmap 会重新生成，指向 `intermediary` 名。
4. **`smfs-refmap.json`** 是从发布 jar 直接复制的产物，其中的映射对应 `intermediary` 命名。若在开发环境编译，Loom 会重新生成该文件。
5. **中文日志字符串**已从作者编译期产生的乱码还原为可读文本，还原方式见 [TOOLS.md](../TOOLS.md#4-编码修复)。

## 授权

```
CC BY-SA 4.0 + 附加条款（禁止商业使用）
```

完整条款见 [`LICENSE_smfs`](LICENSE_smfs)。注意 `fabric.mod.json` 中的 `license` 字段只写了 `CC BY-SA 4.0`，而 jar 内附带的授权文件额外增加了**禁止任何形式商业使用**的限制，两者以附带文件为准。

作者联系方式：QQ `3082127327`
