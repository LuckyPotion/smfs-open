# 反编译工具链与复现步骤

本文件记录生成 [`smfs/`](smfs/) 与 [`smfs-mca-compatibility/`](smfs-mca-compatibility/) 中源码所用的完整工具链，任何人都可以据此从原始 jar 复现同样的结果。

## 环境

| 组件 | 版本 | 用途 |
|---|---|---|
| JDK | Oracle JDK 17.0.12 | 运行重映射器与反编译器 |
| [Vineflower](https://repo1.maven.org/maven2/org/vineflower/vineflower/) | 1.12.0 | Java 反编译器 |
| [TinyRemapper](https://maven.fabricmc.net/net/fabricmc/tiny-remapper/) | 0.10.4（fat jar） | 命名空间重映射 |
| [Yarn](https://maven.fabricmc.net/net/fabricmc/yarn/) | `1.20.1+build.10` (v2) | intermediary → named 映射表 |
| sponge-mixin | `0.16.3+mixin.0.8.7` | TinyRemapper Mixin 扩展的注解依赖 |
| slf4j-api | 2.0.9 | TinyRemapper 运行期依赖 |

### 反编译外部类路径

提供这些库能显著改善类型还原质量（本仓库实测：891 个类中 88 个文件的输出因此改善）：

| 库 | 版本 | 来源 |
|---|---|---|
| Fabric API | `0.92.2+1.20.1` | Modrinth / CurseForge |
| GeckoLib | `4.4.9`（fabric-1.20.1） | `https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/` |
| Fabric Loader | 0.17.2 | `https://maven.fabricmc.net/` |
| sponge-mixin | `0.16.3+mixin.0.8.7` | Maven Central |
| JetBrains Annotations | 24.1.0 | Maven Central |
| Gson | 2.10.1 | Maven Central |
| Commons Lang3 | 3.12.0 | Maven Central |
| JOML | 1.10.5 | Maven Central |

> GeckoLib 声明依赖 `>=4.2.4`；1.20.1 分支的最后一个版本是 4.4.9，故选用该版本。

## 步骤

### 1. 取映射表

```bash
curl -O https://maven.fabricmc.net/net/fabricmc/yarn/1.20.1+build.10/yarn-1.20.1+build.10-v2.jar
unzip yarn-1.20.1+build.10-v2.jar mappings/mappings.tiny
# 表头：tiny	2	0	intermediary	named
```

### 2. 重映射 jar（intermediary → named）

必须使用 **API 而非 CLI**，因为只有 API 能挂载 `MixinExtension`，从而把 `@Mixin(class_310.class)`、`@Inject(method = "...")`、`@At(target = "Lnet/minecraft/...")` 里的注解载荷一并重写。使用 CLI 会留下未重命名的注解引用。

核心代码（完整实现见 [`tools/Remap.java`](tools/Remap.java)）：

```java
TinyRemapper.Builder builder = TinyRemapper.newRemapper()
        .withMappings(TinyUtils.createTinyMappingProvider(mappings, "intermediary", "named"))
        .threads(Runtime.getRuntime().availableProcessors())
        .renameInvalidLocals(true)
        .rebuildSourceFilenames(true);

new MixinExtension(Collections.emptySet()).attach(builder);   // ← 关键

try (OutputConsumerPath consumer = new OutputConsumerPath.Builder(out).build()) {
    consumer.addNonClassFiles(in);                            // 资源原样带过
    remapper.readClassPath(libs);
    InputTag tag = remapper.createInputTag();
    remapper.readInputs(tag, in);
    remapper.apply(consumer, tag);
}
```

执行：

```bash
java -cp classes:tiny-remapper-fat.jar:sponge-mixin.jar:slf4j-api.jar \
     Remap smfs-1.5.0.jar smfs-named.jar mappings.tiny intermediary named
```

### 3. 反编译

```bash
java -jar vineflower.jar -dgs=1 --silent \
     -e=fabric-api-0.92.2+1.20.1.jar \
     -e=geckolib-fabric-1.20.1-4.4.9.jar \
     -e=fabric-loader-0.17.2.jar \
     -e=sponge-mixin-0.16.3+mixin.0.8.7.jar \
     -e=slf4j-api-2.0.9.jar \
     smfs-named.jar out-src/
```

- `-dgs=1`：反编译泛型签名，恢复 `List<ItemStack>` 这类类型参数
- `-e=<path>`：加入外部库类路径。**参数必须写成 `-e=path` 形式**，写成 `-e path` 或把多个路径用 `;` 拼成一个参数都会导致该路径被当作输入文件而忽略（会打印 `warn: missing '...', ignored`）

### 4. 编码修复

作者编译期把 UTF-8 源码当作 GBK 读取，使中文串变成乱码。还原公式：

```java
String original = new String(mojibake.getBytes(Charset.forName("GBK")), Charset.forName("GB18030"));
```

实现见 [`tools/CharsetFix.java`](tools/CharsetFix.java)。注意必须用 **GB18030** 而非 UTF-8 作最终解码：部分汉字（如「蟹」U+87F9）无 GBK 码位，若用 UTF-8 解码会得到 `U+FFFD`。

### 5. 校验

```bash
# 类名不应残留 intermediary 命名（期望：无输出）
grep -rEo '\bclass_[0-9]+' src/main/java/ | wc -l

# 成员名残留：本项目为 38 处 / 14 个文件，且集中在模组自有实体类上
grep -rEo '\b(method|field)_[0-9]+' src/main/java/ | wc -l

# 资源文件应与原始 jar 逐字节一致
# （lang/*.json、assets/**、data/** 全部比对 MD5）
```

> **注意：不要用 PowerShell 的 `Select-String -Path "...\**\*.java"` 做这项检查。**
> PowerShell 会把 `**` 当成单层通配符而不是递归通配符，路径匹配不到文件时**不会报错**，直接返回 0 个结果——本项目就因此一度得出了"零残留"的错误结论，而实际有 27,434 处。请使用 `grep -r`，或用 `Get-ChildItem -Recurse | Select-String`。

## 已知的工具坑

| 现象 | 原因 | 处理 |
|---|---|---|
| `warn: missing '...', ignored` | `-e` 参数写法错误，被当成输入文件 | 使用 `-e=<path>`，每个库一个 `-e` |
| 注解里的类名没被重命名 | 用的 TinyRemapper CLI，未挂 MixinExtension | 改用 API 并 `new MixinExtension(...).attach(builder)` |
| **类名都改了，但成员名一个都没改** | **没给 remapper 传 Minecraft 类路径**，解析不出成员属主 | **把 Minecraft 的 intermediary jar 作为 `-cp` 传入**（见下一节） |
| `--only=<包名>` 不输出任何文件 | 该参数匹配的是类名前缀，不是包路径 | 用 `com/xie/smfs/...` 形式的完整类名前缀，或直接整包反编译 |
| 校验命令返回 0，但实际有残留 | PowerShell `**` 不是递归通配符，静默匹配不到文件 | 用 `grep -r` 或 `Get-ChildItem -Recurse` |

## 关于 Minecraft 类路径（重要）

TinyRemapper 的**成员**映射依赖类型解析：它必须先确定某个方法/字段属于哪个类，才能查表改名。**类名映射不依赖解析**，所以缺类路径时会出现"类名全对、成员名全错"的现象——很容易误判为重映射成功。

本项目第一版正是如此，产出物含 **27,434 处**成员残留；补上类路径后降至 **38 处**。

Minecraft 的 intermediary jar 官方并不直接托管在 Fabric maven 上，需要自己生成：

```bash
# 1) 取 official -> intermediary 映射
curl -O https://maven.fabricmc.net/net/fabricmc/intermediary/1.20.1/intermediary-1.20.1-v2.jar
unzip -o intermediary-1.20.1-v2.jar mappings/mappings.tiny -d mappings-intermediary/
#    表头：tiny	2	0	official	intermediary

# 2) 取官方 client.jar（从 Mojang 的版本清单里拿 URL）
#    https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
#    -> 找到 1.20.1 的 version url -> downloads.client.url

# 3) 生成 intermediary 版 Minecraft
java -cp "$CP" Remap minecraft-1.20.1-client.jar mc-intermediary-1.20.1.jar \
     mappings-intermediary/mappings/mappings.tiny official intermediary

# 4) 重映射模组时传入该 jar
java -cp "$CP" Remap smfs-1.5.0.jar smfs-named.jar mappings/mappings.tiny intermediary named \
     -cp mc-intermediary-1.20.1.jar
```

### 剩余 38 处为何消不掉

集中在模组自有实体类上的调用，例如：

```java
yinQi.method_5808(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
```

`method_5808` 的真实属主是 `net/minecraft/entity/Entity`（`class_1297`，确实存在于类路径中），但调用点接收者是 `YinQiEntity`——模组自有类，重映射时不在类路径里，TinyRemapper 无法解析出它继承自 `Entity`，于是放弃这个调用点。同一行的 `getX()`、`getY()` 都能正常改名。这是重映射的固有限制，不影响阅读。
| 中文全部变成 `锟斤拷` / `����` | 终端或读取端编码与文件实际编码不符 | 文件是 UTF-8，用 UTF-8 读取；不要用控制台管道传递中文 |
| Windows PowerShell 5.1 执行含中文路径的脚本报语法错误 | 无 BOM 的 UTF-8 脚本被按 GBK 解析，中文路径字节里出现了 `"` | 给脚本加 UTF-8 BOM（`EF BB BF`） |
