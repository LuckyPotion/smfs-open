# 反编译流水线工具

本目录是[仓库根目录 TOOLS.md](../TOOLS.md) 所述流程的可执行实现，用于从原始 jar 复现 `src/main/java` 中的源码。

## 文件

| 文件 | 说明 |
|---|---|
| `Remap.java` | 基于 TinyRemapper API 的重映射工具，挂载 Mixin 扩展，把 jar 在命名空间之间转换 |
| `CharsetFix.java` | 修复 UTF-8↔GBK 误读造成的中文乱码，可逆还原字符串 |
| `audit_cjk.py` | 按**码位**审计源码中的中文是否损坏，并复现本项目犯过的一次编码误判 |

## 编译

需要 JDK 17 及以上。依赖 jar 均从公开 Maven 仓库获取：

```bash
# TinyRemapper（fat jar，已内置 ASM 与 mapping-io）
curl -O https://maven.fabricmc.net/net/fabricmc/tiny-remapper/0.10.4/tiny-remapper-0.10.4-fat.jar
# Mixin 扩展需要的注解库
curl -O https://repo1.maven.org/maven2/net/fabricmc/sponge-mixin/0.16.3+mixin.0.8.7/sponge-mixin-0.16.3+mixin.0.8.7.jar
# TinyRemapper 运行期依赖
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

CP="tiny-remapper-0.10.4-fat.jar:sponge-mixin-0.16.3+mixin.0.8.7.jar:slf4j-api-2.0.9.jar"
javac -encoding UTF-8 -cp "$CP" -d classes Remap.java CharsetFix.java
```

## Remap.java

```
用法：
  Remap <in.jar> <out.jar> <mappings.tiny> <fromNs> <toNs> [--reverse] [-cp <path>]...

示例（intermediary → named）：
  java -cp "classes:$CP" Remap \
       smfs-1.5.0.jar smfs-named.jar mappings/mappings.tiny intermediary named

反向转换（named → intermediary，即 Loom 发布时的方向）：
  java -cp "classes:$CP" Remap \
       smfs-named.jar smfs-intermediary.jar mappings/mappings.tiny intermediary named --reverse

附加类路径（提供类型信息，提升重映射与反编译质量）：
  java -cp "classes:$CP" Remap in.jar out.jar mappings.tiny intermediary named \
       -cp geckolib-fabric-1.20.1-4.4.9.jar -cp fabric-api-0.92.2+1.20.1.jar
```

**为什么不直接用 CLI？** TinyRemapper 自带的 `net.fabricmc.tinyremapper.Main` 不加载 `MixinExtension`，因此 `@Mixin(class_310.class)`、`@At(target = "Lnet/minecraft/class_1234;method_5678(...)")` 这类**注解载荷不会被重写**，重映射后的代码注解仍然指向旧命名空间。只有通过 API 显式挂载扩展才能正确处理：

```java
new MixinExtension(Collections.emptySet()).attach(builder);
```

本工具还做了两件 CLI 不会做的事：

1. `consumer.addNonClassFiles(in)` —— 把 `fabric.mod.json`、`assets/`、`data/` 等资源原样复制到输出 jar
2. `rebuildSourceFilenames(true)` + `renameInvalidLocals(true)` —— 重建 `SourceFile` 属性，让局部变量名尽量可读

## CharsetFix.java

```
用法：
  CharsetFix <文件或目录> [...]        就地修复，结果写回 UTF-8
  CharsetFix --check <文件或目录> [...]  只报告不写入

示例：
  java -cp classes CharsetFix --check ../smfs/src/main/java
  java -cp classes CharsetFix ../smfs/src/main/java
```

处理 `.java`、`.json`、`.mcmeta` 三类文件。

**原理**：作者编译源码时，UTF-8 字节被以 GBK 代码页读取，于是每个汉字变成两个（或更多）乱码字符。该变换可逆：

```java
String original = new String(mojibake.getBytes(Charset.forName("GBK")), Charset.forName("GB18030"));
```

**为什么最终解码必须用 GB18030**：部分汉字没有 GBK 码位。例如「蟹」（U+87F9）在 GBK 中不存在，用 GBK 编码时会被替换为 `?`，再以 UTF-8 解码就得到 `U+FFFD`，字符永久丢失。GB18030 是 GBK 的超集，能正确还原这类字符。

**安全设计**：工具只会重写"确实发生变化"的文件。转换结果与原文本相同时直接跳过（有效 UTF-8 中文经 `GBK 编码 → GB18030 解码` 后是恒等变换，所以正常的中文文件不会被误改）。同时逐文件校验结果中不含 `U+FFFD`，否则放弃修改。

## 实测效果

在本仓库的目标 jar 上：

| 项目 | 结果 |
|---|---|
| 重映射后 `class_*` / `method_*` / `field_*` 残留 | **0** |
| 编码修复命中文件 | `fabric.mod.json` ×2（模组名/简介）、`tutorials.json`、`event_stories.json`、`home_1.json`、`homes.json` |
| Java 源码中的中文损坏数 | **0**（4073 个含中文的字符串常量，按码位审计） |
| 资源文件与原始 jar 差异 | **0**（891 个文件 MD5 全部一致） |
| 外部类路径带来的改善 | 728 个源文件中 88 个输出质量提升 |

## 关于编码的教训

`audit_cjk.py` 的头注释完整记录了一次真实的误判，值得单列：

判断中文是否乱码时，**不能用终端渲染，也不能用 GBK 往返变换**。

- Windows 控制台默认 GBK，会把正确的中文渲染成"妫€娴嬪埌鐣岄潰"这类字样。屏幕上看到的乱码可能完全来自显示层。
- `GBK.encode(s).decode("utf-8")` 对正常中文是恒等变换，把它当成"还原"测试会导致**大量假阳性**。本项目一度据此得出"4059 个字符串永久损坏（99%）"的结论，而实际是 0 个。
- 唯一可靠的判据是**码位分布**：正常中文落在 `U+4E00–U+9FFF` 等有限区段；UTF-8 被读作 GBK 产生的乱码会落在 `U+2A7D`、`U+9420`、`U+9F98`、`U+5A9B` 这类极少出现的码位上。

`CharsetFix` 的设计也受此影响：它只在转换结果**确实与原串不同**时才写回，并且拒绝任何含 `U+FFFD` 的结果——正常中文因此永远不会被它改动（实测：全量运行后 728 个 Java 文件与原始反编译输出逐字节一致）。
