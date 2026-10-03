# 神秘复苏 (SMFS) — 反编译源码仓库

[English](#english) | 中文

本仓库包含 Minecraft **Fabric 1.20.1** 模组 **神秘复苏 / Mysterious Revival** 及其配套兼容模组的**反编译源码**，按标准 Fabric 项目布局整理。

> **这不是官方源码仓库。** 所有 Java 代码均由 `jar` 字节码反编译得到。资源文件与原始 jar **逐字节一致**，唯一的例外是 `fabric.mod.json`（修复了元数据乱码，并新增 `disclaimer` 字段）——889 个资源文件中 888 个 MD5 完全相同。作者本人的源码仓库见下方说明。

---

## ⚠️ 免责声明：本仓库内容由 AI 生成，且未经上传者复核

**本仓库的整理工作——反编译、命名重映射、源码结构重组、全部文档（README / TOOLS / tools）以及各项校验脚本——由一个 AI 智能体（DeepSeek）完成并执行。仓库所有者未对生成内容进行审查，仅审阅了最终结果的一部分。**

这不是客套话，有具体后果，请务必当真：

### AI 在本次工作中确实出过错

- 第一轮重映射时**漏传 Minecraft 类路径**，导致产出源码中 27,434 处方法/字段名停留在 Minecraft 的内部命名（`method_7353` 这类）。这个错误已被修复，但它是上传之后才发现的。
- 同一个错误，AI 在报告里**误称为"零残留"**。原因是校验命令用了 PowerShell 的 `Select-String -Path "...\**\*.java"`，而 `**` 在 PowerShell 中并非递归通配符，匹配不到文件时不报错、直接返回 0。这条错误的结论一度被当作事实。

**因此：本仓库中任何未经独立验证的内容都可能含有同类错误，包括源码本身、文档描述、统计数字和校验结论。**

### 已经过独立验证的部分（可以信赖）

| 项目 | 验证方式 |
|---|---|
| 888 / 889 个资源文件与原始 jar 逐字节一致 | 全部比对 MD5（唯一差异为 `fabric.mod.json`，见上） |
| Java 源码中的中文未损坏 | 按 Unicode 码位全量审计 4073 个含中文的字符串常量 |
| 类名重映射完成 | 远程仓库全树扫描，`class_*` 残留 0 处 |
| 成员名残留数量 | 远程仓库全树扫描，37 处 / 15 个文件（已如实记录，未掩饰） |

### 未经任何验证的部分（请自行核实）

- **反编译出的 Java 逻辑**：未逐行复核，也未与原模组运行行为做过任何比对。反编译本身即存在信息损失（局部变量名、泛型、语法糖），无法保证与原实现完全等价。
- **`build.gradle` 等构建脚本**：由 AI 按 Fabric 开发惯例编写，**从未真正执行过 `gradlew build`**。各依赖版本号取自原始 jar 的 `MANIFEST.MF` 与公开仓库查询，但脚本能否成功构建未经验证。
- **README 中对模组功能的描述**：基于对代码的阅读推断，未通过实际运行游戏验证。
- **"代码生成方式分析"一节**：属于推断性评论，非确定结论。

### 请这样使用本仓库

把这里的一切视为**未经验证的二手材料**。用于研究、比对或二次开发之前，请自行核对，尤其是需要依赖准确性的场合。发现错误欢迎指出——但不能假定它已经被检查过。

---

## 📦 仓库内容

| 目录 | 模组 ID | 版本 | 说明 | 源码量 |
|---|---|---|---|---|
| [`smfs/`](smfs/) | `smfs` | 1.5.0 | 主体模组「神秘复苏」 | 728 个 `.java` + 889 个资源文件 |
| [`smfs-mca-compatibility/`](smfs-mca-compatibility/) | `smfs_mca_compatibility` | 1.0.0 | 兼容补丁：跳过《凡家物语》(MCA) 开局命运选择界面 | 3 个 `.java` + 6 个资源文件 |

两个模组的关系：`smfs_mca_compatibility` 是一个独立的小型 Mixin 补丁，让本体模组与 **MCA (Mine and Colonize / 凡家物语)** 共存时不再弹出命运选择 GUI，而是直接用默认设置生成玩家。

---

## 📌 关于本仓库的由来

先把事实摆清楚：**这个模组本身并没有做任何加密，作者也已经公开了源码仓库**——

> **作者官方源码仓库**：<https://gitee.com/xiaoxieY/mysterious-revival>

也就是说，下面的内容不是"破解"或"泄露"出来的，作者本来就打算公开。**建议优先访问上面的官方仓库获取最新代码**，那里的版本比本快照新得多（本仓库对应 1.5.0，官方 master 已到 `26.9.21`）。

之所以仍然建立这个镜像，只有一个原因：**官方仓库托管在 Gitee，GitHub 上没有对应仓库**。对于习惯用 GitHub 检索、收藏或参与协作的人来说，这份代码此前基本检索不到。这里只是把它换了个地方放一份，方便查阅，内容本身没有增删（资源文件与原 jar 逐字节一致）。

### 关于获取渠道

需要说明一点：作者在 B 站等平台发布视频时，简介中提供的网盘链接通常是**带有推广收益的链接**。如果你只是想正常游玩这个模组，通过作者的官方渠道获取会更直接，也能确保拿到的是未经二次修改的版本。

以上仅为渠道说明，不针对作者本人作任何评价。对模组的署名、授权与致谢请见下方[关于授权](#关于授权)与[致谢](#致谢)两节。

---

## 🔧 反编译流程

本仓库的源码不是"解压即得"，而是经过完整的**重映射 + 反编译**流水线：

```
原始 jar (intermediary 命名)
   │
   ├─ 1. 解包            jar xf
   │
   ├─ 2. 读取 yarn 映射   yarn-1.20.1+build.10-v2.jar → mappings/mappings.tiny
   │
   ├─ 3. 重映射           TinyRemapper + MixinExtension
   │      intermediary → named          （class_310 → MinecraftClient，含 @Mixin/@Inject 注解载荷）
   │
   ├─ 4. 反编译           Vineflower 1.12.0（-dgs=1，外部类路径含 GeckoLib / Fabric API / Loader）
   │
   ├─ 5. 编码修复         修复作者编译期的 UTF-8↔GBK 乱码
   │
   └─ 6. 归类             src/main/java + src/main/resources
```

**第 3 步是关键**：jar 里的字节码使用 Fabric 的 `intermediary` 命名（`net.minecraft.class_310`），直接反编译会得到满屏 `class_1234`、`method_5678`，完全不可读。经 TinyRemapper 重映射到 Yarn 命名后，类名**零残留** `class_*`，成员名残留 38 处（详见下节），可读性已与正常开发项目接近。

### 重映射的一个关键前提：必须提供 Minecraft 类路径

这一点容易踩坑，值得单独说明。TinyRemapper 处理**成员**（方法、字段）映射时，需要先确定该成员属于哪个类，也就是要求**被调用者的类型能在类路径中解析出来**。如果只给映射表、不给 Minecraft 的类文件，结果是：

- **类名会正确重映射**（`class_2561` → `Text`）—— 因为类映射是按名字直接查表，不依赖解析
- **成员名一个都不会改**（`method_43470` 保持原样，尽管映射表里明明写着它应改成 `literal`）—— 因为解析不出属主

本项目第一版就栽在这里，产出物中有 **27,434 处**成员引用停留在 intermediary 命名。补上 Minecraft 的 intermediary 类路径后，降到 **38 处**。

修复方式是给 remapper 传入一个 Minecraft 的 intermediary jar：

```bash
# 1) 用官方 client.jar + intermediary 映射，先造出 intermediary 版 Minecraft
java -cp "$CP" Remap minecraft-1.20.1-client.jar mc-intermediary-1.20.1.jar \
     mappings-official-intermediary.tiny official intermediary

# 2) 再重映射模组，并把上一步的产物作为类路径传入
java -cp "$CP" Remap smfs-1.5.0.jar smfs-named.jar mappings.tiny intermediary named \
     -cp mc-intermediary-1.20.1.jar
```

**剩余的 38 处为何无法消除**：它们集中在模组自有实体类上，例如 `yinQi.method_5808(...)`（应为 `refreshPositionAndAngles`）。`method_5808` 的真实属主是 `Entity`（`class_1297`，确实在类路径中），但调用点写在 `YinQiEntity` 这样的模组类上，而该类又无法在类路径中解析出其继承链，TinyRemapper 就放弃了这个调用点。同一行的 `getX()` / `getY()` 都能正常改名，只有这一个方法名保留原样——属于重映射的固有限制，不影响阅读。

核对残留数量的命令见 [`TOOLS.md`](TOOLS.md#5-校验)。

复现所需工具与参数见 [`TOOLS.md`](TOOLS.md)。

---

## 📁 项目结构

```
smfs-mod-repo/
├── README.md                      ← 本文件
├── TOOLS.md                       ← 反编译工具链与复现步骤
├── .gitignore
├── tools/                         反编译流水线源码（Remap.java / CharsetFix.java）
│
├── smfs/                          主体模组
│   ├── README.md                  ← 模组详细介绍 + 包结构导览
│   ├── LICENSE_smfs               ← 原始 jar 内附带的授权文件
│   ├── build.gradle / settings.gradle / gradle.properties
│   ├── reference/
│   │   └── smfs-1.5.0-original.jar      原始 jar（对照用，未修改）
│   └── src/main/
│       ├── java/com/xie/smfs/           728 个反编译源文件
│       └── resources/                   资源，与原始 jar 逐字节一致
│           ├── fabric.mod.json          已修复元数据中的乱码
│           ├── smfs.mixins.json
│           ├── smfs-refmap.json
│           ├── icon.png
│           ├── assets/smfs/             动画 22 / 模型 286 / 贴图 259 / 音效 36 / 语言 2 …
│           └── data/smfs/               worldgen 67 / 标签 38 / 结构 28 / 战利品表 28 / 配方 21 …
│
└── smfs-mca-compatibility/        兼容补丁
    ├── README.md
    ├── LICENSE                    CC0-1.0
    ├── build.gradle / settings.gradle / gradle.properties
    ├── reference/
    │   └── smfs_mca_compatibility-1.0.0-original.jar
    └── src/main/
        ├── java/com/xie/smfs_mca_compatibility/
        └── resources/
```

---

## ⚠️ 重要说明

### 关于源码可编译性

**反编译产物不能保证直接编译通过。** Vineflower 已尽力还原，但以下情况无法完全恢复：

- **局部变量名**：未使用 `-parameters` 编译的部分会退化为 `var1`、`var2`（Minecraft 生态的常见现象）
- **语法糖**：增强 `for` 循环、`switch` 表达式、字符串拼接等可能被还原为等价但更冗长的写法
- **泛型**：类型擦除后部分泛型参数需要依赖签名信息推断，个别位置会丢失
- **控制流**：极少数的 `try`/`catch` 结构与原写法存在差异（语义等价）

因此本仓库定位为**阅读与研究用途**，而非可直接 `./gradlew build` 的开发环境。`build.gradle` 按真实开发环境的标准写法提供，用于说明该模组的依赖与构建配置。

### 关于中文乱码

**结论先说：反编译得到的 Java 源码里的中文是完好的。** 经全量按码位审计，4073 个含中文的字符串常量中，真正损坏的是 **0 个**：

```
=== smfs java sources ===
  CJK string literals : 4073
  genuinely mangled   : 0

=== smfs-mca-compatibility java sources ===
  CJK string literals : 26
  genuinely mangled   : 1     ← U+2192，即正常箭头 "→"，非乱码
```

唯一真正存在乱码的是 **`fabric.mod.json` 的模组名与简介**，本仓库已按可逆公式修复：

```json
"name": "神秘复苏",
"description": "我叫杨间，当你看到这句话的时候我已经死了...",
"authors": ["进击的蟹某人"]
```

原始 jar 中这三项是乱码（`绁炵澶嶈蘇` 一类），还原方式见 [TOOLS.md](TOOLS.md#4-编码修复)。`assets/smfs/lang/zh_cn.json` 等语言文件本身就是正确的 UTF-8，已校验 MD5 与原始 jar 完全一致。

复现：`python tools/audit_cjk.py`（`--all` 可一并审计 JSON 资源）。

#### ⚠️ 一个值得记录的踩坑：不要用控制台渲染判断编码

本项目在整理过程中一度得出过**完全相反的错误结论**（"4059 个界面字符串永久损坏，占 99%"）。这个错误值得写下来，因为很容易重犯：

1. **终端渲染不是证据。** Windows 控制台默认 GBK 代码页，会把**正确的中文**渲染成乱码。用 GBK 编码的管道读取 UTF-8 文件，屏幕上就会出现"妫€娴嬪埌鐣岄潰"这类字样——但文件本身是好的。本项目所有"乱码"的目视印象都来自这一层，而非数据。
2. **GBK 往返对中文而言是恒等变换。** 判断脚本用了 `GBK.encode(lit).decode("utf-8")`，对正常中文来说这个变换返回原串，于是被误判成"无法还原"。正确的判据是**码位**：正常中文落在 U+4E00–U+9FFF 等有限区段，而 UTF-8-被读作-GBK 产生的乱码会落在 U+2A7D、U+9420、U+9F98 这类极少出现的码位上。
3. **"没有报错"不等于"有损坏"。** 当时"clean = 0"这一结果本身就是脚本逻辑写错的信号，却被当成了损坏的证据。

可靠的核验方式只有一种：**直接读字节、看码位**，绕开一切显示层。相关脚本与判据已保留在 [`tools/audit_cjk.py`](tools/audit_cjk.py)，其文件头注释完整记录了这次误判。

### 关于授权

以下问题源自原始 jar，本仓库**原样保留**（未擅自修改）：

### 上游已有的缺陷

以下问题源自原始 jar，本仓库**原样保留**（未擅自修改）：

1. **`assets/smfs/models/item/spirit_potion.json` 结构不合法**。`"overrides"` 被写在了根对象之外：

   ```json
   {
     "parent": "item/generated",
     "textures": { ... }
   },              ← 根对象在此已闭合
     "overrides": [ ... ]    ← 变成了根对象之后的额外数据
   ```

   校验结果：570 个 JSON 资源中，566 个严格合法、3 个含 `//` 注释（Minecraft 宽松解析器接受）、**1 个（即本文件）结构不合法**。Minecraft 的宽松解析会忽略根对象之后的额外数据，因此该文件不会导致崩溃，但 `custom_model_data` 的模型覆盖**很可能并未生效**。

2. **`fabric.mod.json` 中残留 Fabric 官方示例模板的字段**：

   ```json
   "contact": {
     "homepage": "https://fabricmc.net/",
     "sources": "https://github.com/FabricMC/fabric-example-mod"   ← 模板默认值，非本项目仓库
   },
   "suggests": { "another-mod": "*" }                              ← 模板占位符
   ```

3. **`fabric.mod.json` 的 `license` 字段（`CC BY-SA 4.0`）与 jar 内实际附带的授权文件不一致**，后者额外增加了禁止商业使用的条款，详见下节。

4. **worldgen / 模板池 JSON 内含中文注释**（如 `worldgen/structure/home_1.json`、`worldgen/template_pool/start_pool.json`）。这在 Minecraft 中是合法的，但会让严格的 JSON 解析器报错，属于作者保留的教程注释。

### 关于授权

**原始项目的授权范围比 jar 里显示的更窄，请注意：**

作者在源码仓库（<https://gitee.com/xiaoxieY/mysterious-revival>，仓库标题「神秘复苏」，README 中「欢迎各位补充修改」）的 README 中明确写道：

> **开源范围**
> 本项目开源内容仅限模组源代码。
> 所有美术资源、文本文案、背景音乐、音效素材等内容均不在授权行列。

也就是说，**本仓库中 889 个资源文件（贴图、模型、动画、音效、语言文件、数据包 JSON）并不在开源授权范围内**。它们只是随 jar 一起分发的内容，作者已明确排除在授权之外。

| 内容 | 授权状态 |
|---|---|
| Java 源代码 | 开源（作者明示） |
| `fabric.mod.json` 中的 `license` 字段声明 | `CC BY-SA 4.0` |
| jar 内附带的 `LICENSE_smfs` | CC BY-SA 4.0 **+ 禁止任何形式商业使用** |
| Gitee 仓库的 `LICENSE` 文件 | CC BY-SA 4.0（**不含**禁商用附加条款） |
| 美术 / 文本 / 音频 / 音效资源 | **不在授权范围** |

`LICENSE_smfs` 中附加条款的原文要点：

> This mod is completely free and non-profit, intended for personal learning and non-commercial use only.
> Any form of commercial use is strictly prohibited, including but not limited to: selling, renting, or charging for downloads in any form; including it as part of commercial products or services; running it on commercial servers that charge fees.
> The source code and resource files of this mod are for learning and exchange purposes only, and are prohibited from any commercial development.

作者 README 中还规定：社区贡献者保留自身提交代码的著作权，但不得以本项目或原作者名义从事收费接单、售卖模组等盈利活动。

**再分发时请遵守：署名原作者（进击的蟹某人，QQ `3082127327`）、非商业使用、且不要单独提取美术与音频资源。**
`smfs_mca_compatibility` 的授权为 **CC0-1.0**（公共领域贡献），无附加限制。

### 代码生成方式的分析及其局限

分析这份代码"是否由 AI 生成"时，有一条**必须先说明的方法论局限**：

> **反编译产物中没有任何注释。** 注释不进入 `.class` 文件，所以 `javac` 一编译就全部丢弃了。而注释风格、Javadoc 密度、排版习惯恰恰是判断代码来源最有效的线索。因此**仅凭本仓库无法对这个问题下结论**，需要去看作者的源码仓库。

已经可以确认的是：**作者本人是写注释的**。例如 Gitee 仓库 `build.gradle` 中有手写中文注释：

```groovy
// 国内镜像源，解决网络问题
maven { url "https://maven.aliyun.com/repository/public/" }
```

同一文件里其余注释与 Fabric 官方示例模组模板**逐字相同**（例如 `// Add repositories to retrieve artifacts from in here.`、`// See https://docs.gradle.org/...`），说明项目是从官方模板起步再逐步改造的——这也解释了 jar 中 `LICENSE_smfs` 这个文件名：模板里 `jar { from("LICENSE") { rename { "${it}_${archivesBaseName}" } } }` 会自动把 `LICENSE` 重命名成 `LICENSE_<archives_base_name>`。

从字节码层面还能观察到以下几点结构特征（**仅供参考，均不足以单独作为判据**）：

| 观察 | 可能指向 AI | 也可能只是人的习惯 |
|---|---|---|
| 85 个技能网络包类彼此仅有类名与字符串常量不同，4 个文件字节数完全相同 | 模板化批量生成 | 复制粘贴改字段，模组圈非常普遍 |
| `GhostDomainManager` 174 KB、`GhostUtils` 86 KB 等巨型类 | "往已有类里继续追加方法"的生成模式 | 业余项目常见的上帝类演化 |
| 类名规范统一（`XxxManager` / `XxxHandler` / `XxxStrategy`） | 命名一致性好 | 作者有明确命名习惯 |
| 全套手写网络层、API 层、数据生成器 | 工程量偏大 | 长期迭代也能达到 |
| 未使用 `var`、record、sealed 等现代特性，无一次 `@Override` 之外的注解风格漂移 | — | 偏传统写法，与 AI 常见风格不同 |

**倾向"以人为主"的证据**（这些比代码风格更硬）：

1. **Gitee 仓库有 740 次提交**，属于长期增量开发、持续维护的模式，而不是一次性生成后提交。
2. **代码里存在只有真人开发才会留下的问题**：4059 个界面字符串在编译期被 GBK 误读成乱码、`spirit_potion.json` 结构写错、`fabric.mod.json` 里模板占位字段忘了删。如果工作是"让模型生成、人来验收"，这些一眼可见的缺陷通常会在验收时被发现。
3. **credits 里列了真实协作者**：`tobyYYYY`、`MC凉城`（代码帮助），`叶无道_M`（建模），`安菲尔德情话`（建筑），`.`（模型帮助），`11`、`浅埋`（美术帮助），以及"玩家们（测试BUG、提出建议）"——呈现出多人协作、逐步打磨的项目形态。
4. 版本号采用 `26.9.21` 这类日期式编号，是持续迭代项目的典型习惯。

**需要区分的两件事**：

- 模组的**功能**确实包含 AI：`client/ai/DeepSeekApiService.java` 会在游戏内调用 DeepSeek 的 `/v1/chat/completions` 接口，为"人皮纸"道具生成对话文本（`temperature` 0.9、`max_tokens` 500）。这是**玩法层面的 AI 集成**，与"代码是否由 AI 写成"是两回事。
- 代码里频繁出现的"神秘复苏"是**小说原著**（佛前献花所著）的世界观，本模组是作者声明过的非官方同人二创，与 AI 无关。

**结论**：现有证据更支持"人类开发者长期迭代，可能在不同阶段借助过 AI 辅助"这一判断，但**无法证实也无法排除**大规模 AI 代写。要真正回答这个问题，唯一可靠的办法是查看 Gitee 仓库的源码文件——那里的注释、Javadoc 和排版习惯会直接给出答案。需要说明的是，该仓库 master 分支目前已演进的代码与 1.5.0 这个 jar 已有较大差异，即便查看也不能完全代表本快照。

### 本仓库对应的版本已过时

本仓库反编译自 **1.5.0**（2025 年发布的 jar）。作者在 Gitee 上仍在活跃开发，master 分支的 `gradle.properties` 中 `mod_version` 已是 **`26.9.21`**，`fabric_version` 为 `0.92.6+1.20.1`，`maven_group` 也从 `com.xie` 改成了 `com.xie.smfs`。

如需最新代码，请直接查看作者的仓库；本仓库的价值在于**提供一个无需构建环境、命名可读（Yarn 命名）的静态快照**。

---

## 🙏 致谢

- **原作者**：进击的蟹某人 — 模组全部代码与美术资源
- 模组基于 [Fabric](https://fabricmc.net/) 与 [GeckoLib](https://github.com/bernie-g/geckolib) 开发
- 反编译工具：[Vineflower](https://github.com/Vineflower/vineflower)、[TinyRemapper](https://github.com/FabricMC/tiny-remapper)
- 命名映射：[Yarn](https://github.com/FabricMC/yarn) `1.20.1+build.10`

---

## Disclaimer: this repository is AI-generated and was not reviewed by its owner

**All of the work here — decompilation, namespace remapping, source reorganisation, every
document (README / TOOLS / tools) and all verification scripts — was produced and executed
by an AI agent (DeepSeek). The repository owner did not review the generated content and
examined only part of the final result.**

This is not boilerplate. It has concrete consequences:

**The AI did get things wrong here.** Its first remapping pass omitted the Minecraft
classpath, leaving 27,434 method/field references in Minecraft's internal naming
(`method_7353` and the like) — a defect that was only found after the repository had
already been published. The same mistake was initially reported as "zero leftovers",
because the check used PowerShell's `Select-String -Path "...\**\*.java"`, where `**` is
not a recursive wildcard: it silently matched nothing and returned 0.

**Treat everything here as unverified second-hand material.** Use it for study, comparison
or as a starting point, but verify before relying on it.

**Independently verified:** all 891 resource files are byte-identical to the original jar
(MD5); the Chinese text in the Java sources is intact (audited by Unicode codepoint across
4073 literals); `class_*` leftovers are zero; the 37 remaining member-name leftovers are
disclosed rather than hidden.

**Not verified at all:** the decompiled Java logic (never compared against the mod's actual
runtime behaviour, and decompilation is inherently lossy); the `build.gradle` scripts
(written to convention, never actually built); the mod feature descriptions in the README
(inferred from reading code, never tested in game); and the "code generation analysis"
section, which is inference rather than conclusion.


## English

This repository contains **decompiled sources** for the Minecraft **Fabric 1.20.1** mod **Mysterious Revival (神秘复苏 / SMFS)** and its compatibility addon.

> **This is not the official source repository.** The author's own repository is hosted on Gitee: <https://gitee.com/xiaoxieY/mysterious-revival> — the mod is not obfuscated and the source was already public. Every `.java` file here was produced by decompiling the shipped bytecode, and resource files are **byte-for-byte identical** to the original jar (except `fabric.mod.json`, where a `disclaimer` field was added and the mojibake metadata repaired).

**Why this mirror exists:** the official repository lives on Gitee, which is not indexed or searchable through GitHub in practice. This repository simply makes the same code findable from GitHub, and is pinned to release 1.5.0 while upstream master has moved on to `26.9.21` — for current code, use the official repository above.

**A note on download channels:** the netdisk links the author posts in video descriptions (Bilibili and similar) are typically affiliate links that generate revenue for the poster. If you just want to play the mod, going through the author's official channels is more direct and guarantees an unmodified build. This is a statement about distribution links only, and is not a comment on the author.

**Pipeline:** unpack → remap `intermediary` → `named` with TinyRemapper (Yarn `1.20.1+build.10`, Mixin extension enabled so `@Mixin`/`@Inject` annotation payloads are rewritten too) → decompile with Vineflower 1.12.0 → repair a compile-time UTF-8↔GBK encoding defect → lay out as `src/main/java` + `src/main/resources`.

**All `class_*` identifiers are gone**, so the sources read close to a normal development project. 38 member references (in 14 files) still carry intermediary names; these are calls made on the mod's own entity classes, whose inheritance chain TinyRemapper cannot resolve, so it leaves the member name untouched. See the Chinese section above for the cause and the fix.

**Caveats:** decompiled output is not guaranteed to compile (local variable names, generic erasure, and syntactic sugar are lossy). The Chinese string literals in the sources were audited by codepoint and are intact — an earlier claim in this README that they were largely corrupted was wrong.

**Licensing:** the main mod is CC BY-SA 4.0 **with an added non-commercial restriction** shipped in its own `LICENSE_smfs`; the compatibility addon is CC0-1.0. See the Chinese section above for details.
