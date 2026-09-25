# StrUtil / CollUtil 日常场景速查（Cookbook）

> 面向日常开发的字符串与集合处理场景集合。每个场景给"需求 → 调用 → 结果"。

## 字符串篇

### 判空与空白

| 场景 | 调用 | 说明 |
| --- | --- | --- |
| 是否为 null 或空串 | `StrUtil.isEmpty(s)` | `""` 与 `null` 均判空 |
| 是否为空白 | `StrUtil.isBlank(s)` | null/空/全空白均判 blank |
| 多个参数任一空白 | `StrUtil.hasBlank(a, b, c)` | 参数组快速检查 |
| 清理后转 null | `StrUtil.trimToNull(s)` | 空结果返回 null |

### 裁剪与截取

```java
StrUtil.trim("  hi  ");            // "hi"
StrUtil.sub("hello", 1, 3);        // "el"
StrUtil.removePrefix("abc.txt", "abc");  // ".txt"
StrUtil.removeSuffix("abc.txt", ".txt"); // "abc"
```

### 拼接与格式化

```java
StrUtil.join(",", "a", "b", "c");  // "a,b,c"
StrUtil.join("-", list);           // 集合转分隔串
StrUtil.format("你好，{}！今天{}", "世界", "周三"); // "你好，世界！今天周三"
StrUtil.repeat("ab", 3);           // "ababab"
```

### 命名转换

```java
StrUtil.toCamelCase("user_name");     // "userName"
StrUtil.toUnderlineCase("userName");  // "user_name"
StrUtil.upperFirst("hello");          // "Hello"
```

### 填充

```java
StrUtil.padPre("5", 3, '0');      // "005"
StrUtil.padAfter("5", 3, '0');    // "500"
```

## 集合篇

### 创建

```java
CollUtil.newArrayList(1, 2, 3);          // List
CollUtil.newHashSet("a", "b");           // HashSet
CollUtil.newLinkedHashSet("a", "b");     // 保序 Set
```

### 判空与取值

```java
CollUtil.isEmpty(list);           // null 与空集合均判空
CollUtil.get(list, 2);            // 越界返回 null（安全）
CollUtil.getFirst(list);          // 首元素，空集合返回 null
CollUtil.getLast(list);           // 尾元素
```

### 集合运算

```java
CollUtil.union(a, b);             // 并集
CollUtil.intersection(a, b);      // 交集
CollUtil.disjunction(a, b);       // 对称差
CollUtil.distinct(list);          // 去重（保序）
CollUtil.filter(list, v -> v > 0); // 过滤
CollUtil.map(list, v -> v * 2);    // 映射
```

### 分组与排序

```java
// 按性别分组
Map<String, List<User>> byGender = CollUtil.groupByKey(users, User::getGender);

CollUtil.sort(list, Comparator.reverseOrder());  // 排序
```

### 转字符串

```java
CollUtil.join(list, ",");         // "1,2,3"
```

## 数组篇（ArrayUtil 补充）

```java
ArrayUtil.isEmpty(arr);           // 判空
ArrayUtil.contains(arr, value);   // 包含
ArrayUtil.reverse(arr);           // 就地反转
ArrayUtil.distinct(arr);          // 去重
ArrayUtil.join(arr, ",");         // 转串
ArrayUtil.min(ints);              // 最小值（int/long/double/泛型均有）
```

## 提示

- 以上 API 全部为静态方法，线程安全，可放心在服务中直接调用。
- 生成式测试保障：StrUtil/ArrayUtil/CollUtil 均有 jqwik 属性测试（数千次随机输入验证不变量），行为可预期。
