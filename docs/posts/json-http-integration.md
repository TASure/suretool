# JSON + HTTP 实战：接口对接不再手写样板

> 目标：用 sure-json + sure-http 完成一次完整的第三方接口对接（GET 查询 → JSON 解析 → POST 提交）。

## 1. JSON 基础

```java
import com.sure.tool.json.JSONUtil;
import com.sure.tool.json.JSONObject;
import com.sure.tool.json.JSONArray;

// 对象 → JSON 字符串
String json = JSONUtil.toJsonStr(user);          // {"name":"Tom","age":18}

// 字符串 → JSONObject / JSONArray
JSONObject obj = JSONUtil.parseObj("{\"name\":\"Tom\"}");
JSONArray arr = JSONUtil.parseArray("[1,2,3]");

// JSON → Bean
User user = JSONUtil.toBean(json, User.class);

// 美化输出（调试友好）
String pretty = JSONUtil.toJsonPrettyStr(user);
```

### JSONPath（筛选嵌套数据）

```java
import com.sure.tool.json.JsonPath;

// 从响应中直接取深层字段（JsonPath.eval 表达式 / JSONUtil.getByPath 字符串路径）
Object city = JsonPath.eval(json, "$.data.address.city");
Object city2 = JSONUtil.getByPath(json, "$.data.address.city");
```

## 2. HTTP 基础

```java
import com.sure.tool.http.HttpUtil;

// GET
String html = HttpUtil.get("https://api.example.com/users?page=1");

// GET + 参数 Map（自动拼 query）
Map<String, Object> params = Map.of("page", 1, "size", 20);
String result = HttpUtil.get("https://api.example.com/users", params);

// POST 表单
Map<String, Object> form = Map.of("username", "tom");
String resp = HttpUtil.post("https://api.example.com/login", form);

// POST JSON
String resp2 = HttpUtil.postJson("https://api.example.com/users", "{\"name\":\"Tom\"}");
```

## 3. 完整对接示例

```java
import com.sure.tool.http.HttpUtil;
import com.sure.tool.json.JSONUtil;
import com.sure.tool.json.JSONObject;

public class ApiClient {
    public static void main(String[] args) {
        // 1. 调用接口
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("q", "suretool");
        params.put("per_page", 5);
        String resp = HttpUtil.get("https://api.github.com/search/repositories", params);

        // 2. 解析响应
        JSONObject json = JSONUtil.parseObj(resp);
        var items = json.getJSONArray("items");

        // 3. 遍历取字段（JSONPath 风格：JSONObject.getNested）
        for (int i = 0; i < items.size(); i++) {
            JSONObject repo = items.getJSONObject(i);
            System.out.println(repo.getStr("full_name") + " ★" + repo.getInt("stargazers_count"));
        }
    }
}
```

## 4. 注意事项

- `HttpUtil` 默认 10s 连接/读取超时，可传 `timeoutMillis` 覆盖
- `postJson` 自动设置 `Content-Type: application/json`
- JSON 解析支持大数字（`P4JsonBigNumberTest`）、LocalDateTime（`P4JsonLocalDateTimeTest`）等场景
- 对不可信 JSON 请先 `JSONUtil.isJson(str)` 校验再解析

## 5. 更多

- JSONPath 流式解析与测试：见 [P5JsonPathTest](../../sure-json/src/test/java/com/sure/tool/json/P5JsonPathTest.java)
- HTTP Cookie/代理/multipart 高级能力：见 [HttpRequest](../../sure-http/src/main/java/com/sure/tool/http/HttpRequest.java)
