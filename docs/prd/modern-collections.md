# PRD：现代集合（SeqUtil + CollUtil 增补）

> 批次：P0 · 批 2　|　负责人：软件研发小组　|　状态：已评审，进入实现
> 版本目标：v1.1.0（与批 1 结构化并发合并发布）

## 1. 目标与范围

兑现 JDK 21「不可变 + Sequenced 集合」独占差异化：Guava / Hutool / Apache Commons 均无 Sequenced 集合工具。

**范围内**：
- 新增 `SeqUtil`（`com.sure.tool.collection`）：Sequenced 语义的首尾安全访问、倒序、不可变前置/追加、有序 Map 首尾键值；
- `CollUtil` 增补常用缺口：分片（chunk）、随机取样、不可变转换、频次统计。

**范围外（不做）**：
- 不自造不可变集合实现（JDK `List.copyOf` 等原生能力足够）；
- 不改动既有 API 签名（向后兼容）；
- 不做 Guava ImmutableCollection Builder（JDK 21 流式工厂已覆盖）。

## 2. 用户场景

1. **顺序敏感处理**：LinkedHashMap 按插入序取首/尾键值；列表倒序视图；
2. **不可变安全**：对外暴露只读数据（防御性拷贝），避免调用方修改内部状态；
3. **批量工具**：大数据分片处理、随机抽样、频次统计。

## 3. 架构评审结论（第 2 步）

| 项 | 结论 |
| --- | --- |
| 模块归属 | `sure-core` → `com.sure.tool.collection`（已 exports，无需改 module-info） |
| 新增类 | `SeqUtil`（`XxxUtil` 命名规范） |
| 依赖边界 | 仅 JDK `java.base`，零第三方运行期依赖 |
| API 形状 | `public final class` + 私有构造器 + 静态方法 + `@since 1.1.0` + 中文 Javadoc + Tab 缩进 |

### 3.1 API 签名（评审通过）

```java
public final class SeqUtil {

	/** 倒序返回副本（SequencedCollection 高效路径；空 → 空列表） */
	public static <T> List<T> reversed(Collection<T> collection);
	/** 安全取首元素（空 → null） */
	public static <T> T firstOrNull(Collection<T> collection);
	/** 安全取尾元素（空 → null） */
	public static <T> T lastOrNull(Collection<T> collection);
	/** 返回「首元素 + 原集合」的不可变新列表（原集合不变） */
	public static <T> List<T> withFirst(Collection<T> collection, T first);
	/** 返回「原集合 + 尾元素」的不可变新列表（原集合不变） */
	public static <T> List<T> withLast(Collection<T> collection, T last);
	/** 有序 Map 首键（空 → null） */
	public static <K, V> K firstKeyOrNull(Map<K, V> map);
	/** 有序 Map 尾键（空 → null） */
	public static <K, V> K lastKeyOrNull(Map<K, V> map);
	/** 有序 Map 首条目（空 → null） */
	public static <K, V> Map.Entry<K, V> firstEntryOrNull(Map<K, V> map);
	/** 有序 Map 尾条目（空 → null） */
	public static <K, V> Map.Entry<K, V> lastEntryOrNull(Map<K, V> map);
}
```

`CollUtil` 增补（全部 `@since 1.1.0`）：
```java
public static <T> List<List<T>> chunk(Collection<T> collection, int size);   // 分片
public static <T> T randomItem(Collection<T> collection);                    // 随机取一
public static <T> List<T> randomItems(Collection<T> collection, int count);  // 随机取 n（不重复）
public static <T> List<T> toImmutable(Collection<T> collection);             // 不可变副本
public static <T> int frequency(Collection<T> collection, T value);          // 频次
```

## 4. 验收标准（门禁）

1. `SeqUtil.reversed`：顺序正确、空集合返回空列表；
2. `SeqUtil.firstOrNull/lastOrNull`：非空正确、空返回 null；
3. `SeqUtil.withFirst/withLast`：返回不可变新列表（原集合不变；结果再 add 抛异常）；
4. `SeqUtil` Map 首尾键值：LinkedHashMap 顺序正确、空返回 null；
5. `CollUtil.chunk`：分片数量与尺寸正确、空/非法参数处理；
6. `CollUtil.randomItem/randomItems`：样本合法、数量正确、不重复；
7. `CollUtil.toImmutable`：结果不可变；`frequency` 计数正确；
8. 质检门禁：`mvn -B verify` 全绿（Checkstyle 0 / SpotBugs 0 / sure-core 覆盖率 ≥ 0.70 且不降）；
9. CHANGELOG + README 同步；commit + push。

## 5. 风险与边界

- `reversed` 返回不可变副本（非视图），语义为「快照倒序」，避免视图失效陷阱；
- `withFirst/withLast` 走不可变路径，避免破坏调用方持有的可变集合（原地 addFirst 不做，防止静默失败）；
- `randomItems` 超出集合大小时返回全量乱序（不抛）。
