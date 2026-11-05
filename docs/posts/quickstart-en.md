# suretool Quickstart (5 minutes)

Requires **JDK 25+**. `sure-core` has no runtime dependencies.

```xml
<dependency>
    <groupId>io.github.tasure</groupId>
    <artifactId>sure-core</artifactId>
    <version>1.19.1</version>
</dependency>
```

```java
import com.sure.tool.util.StrUtil;
import com.sure.tool.collection.CollUtil;
import java.util.List;

public class Quickstart {
    public static void main(String[] args) {
        System.out.println(StrUtil.trim("  Hello, suretool  "));      // Hello, suretool
        System.out.println(StrUtil.toCamelCase("user_name"));         // userName
        System.out.println(CollUtil.distinct(List.of(3, 1, 2, 1)));   // [3, 1, 2]
    }
}
```

Run with `mvn -q compile exec:java -Dexec.mainClass=Quickstart`.

Next: [module guide](../modules.md) · [migrating from Hutool](../migration-from-hutool.en.md)
