package com.mcp.robot.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 系统工具类
 * 提供 SQL 执行、用户查询等功能
 *
 * @author Kinch.zhu
 * @date 2025/5/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SysTools {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${external-api.amap.key}")
    private String amapApiKey;


    /**
     * 执行查询 SQL 并返回结果
     * AI 会根据用户问题生成 SQL，然后调用此工具执行
     */
    @Tool("""
            执行 SELECT 查询语句并返回结果。
            参数说明：
            - sql: 要执行的 SELECT 语句（必须是查询语句，不能是 UPDATE/DELETE/INSERT）
            返回：查询结果的 JSON 字符串
            """)
    public String executeQuery(@P("要执行的SQL查询语句") String sql) {
        log.info("🔧 Tool调用 - 执行SQL查询: {}", sql);

        try {
            // 安全检查：只允许 SELECT 语句
            String upperSql = sql.trim().toUpperCase();
            if (!upperSql.startsWith("SELECT")) {
                return "错误：只允许执行 SELECT 查询语句，不支持 UPDATE/DELETE/INSERT 等操作";
            }

            // 执行查询
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            if (results.isEmpty()) {
                return "查询成功，但没有找到符合条件的数据";
            }

            // 转换为易读的 JSON
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(results);

            log.info("✅ SQL执行成功，返回 {} 条记录", results.size());
            return String.format("查询成功，共 %d 条记录：\n%s", results.size(), jsonResult);

        } catch (Exception e) {
            log.error("❌ SQL执行失败: {}", sql, e);
            return "SQL执行失败: " + e.getMessage();
        }
    }

    /**
     * 根据用户名获取用户编码（示例工具）
     */
    @Tool("根据用户的名称获取对应的用户编码")
    public String getUserCodeByUsername(@P("用户名称") String username) {
        log.info("🔧 Tool调用 - 查询用户编码: {}", username);

        if ("朱老七".equals(username)) {
            return "003";
        } else if ("张铁牛".equals(username)) {
            return "001";
        } else if ("李明".equals(username)) {
            return "002";
        }

        return "000";
    }

    // ==================== 🆕 新增：外部 API 工具 ====================

    /**
     * 🌤️ 查询城市天气（使用高德天气 API）
     * 免费申请：https://lbs.amap.com/api/webservice/guide/api/weatherinfo
     */
    @Tool("""
            查询指定城市的实时天气信息。
            参数：
            - city: 城市名称（如：北京、上海、深圳）
            返回：天气状况、温度、湿度等信息
            """)
    public String getWeather(@P("城市名称") String city) {
        log.info("🔧 Tool调用 - 查询天气: {}", city);

        try {

            // 1. 先查询城市编码
            String geocodeUrl = String.format(
                    "https://restapi.amap.com/v3/geocode/geo?address=%s&key=%s",
                    city,
                    amapApiKey
            );

            Map<String, Object> geocodeResult = restTemplate.getForObject(geocodeUrl, Map.class);

            if (geocodeResult == null || !"1".equals(geocodeResult.get("status"))) {
                return "查询失败：无法找到城市 " + city;
            }

            // 获取城市 adcode
            List<Map<String, Object>> geocodes = (List<Map<String, Object>>) geocodeResult.get("geocodes");
            if (geocodes == null || geocodes.isEmpty()) {
                return "未找到城市：" + city;
            }

            String adcode = (String) geocodes.get(0).get("adcode");

            // 2. 查询天气
            String weatherUrl = String.format(
                    "https://restapi.amap.com/v3/weather/weatherInfo?city=%s&key=%s",
                    adcode,
                    amapApiKey
            );

            Map<String, Object> weatherResult = restTemplate.getForObject(weatherUrl, Map.class);

            if (weatherResult == null || !"1".equals(weatherResult.get("status"))) {
                return "天气查询失败";
            }

            List<Map<String, Object>> lives = (List<Map<String, Object>>) weatherResult.get("lives");
            if (lives == null || lives.isEmpty()) {
                return "暂无天气数据";
            }

            Map<String, Object> weather = lives.get(0);

            String result = String.format("""
                            🌤️ %s 实时天气：
                            - 天气：%s
                            - 温度：%s℃
                            - 风向：%s
                            - 风力：%s级
                            - 湿度：%s%%
                            - 更新时间：%s
                            """,
                    weather.get("city"),
                    weather.get("weather"),
                    weather.get("temperature"),
                    weather.get("winddirection"),
                    weather.get("windpower"),
                    weather.get("humidity"),
                    weather.get("reporttime")
            );

            log.info("✅ 天气查询成功: {}", city);
            return result;

        } catch (Exception e) {
            log.error("❌ 天气查询失败: {}", city, e);
            return "天气查询出错: " + e.getMessage();
        }
    }

    /**
     * 📍 地址解析（经纬度转地址）
     */
    @Tool("""
            将经纬度坐标转换为详细地址。
            参数：
            - longitude: 经度
            - latitude: 纬度
            返回：详细地址信息
            """)
    public String getAddressByLocation(
            @P("经度") double longitude,
            @P("纬度") double latitude) {
        log.info("🔧 Tool调用 - 地址解析: {},{}", longitude, latitude);

        try {

            String url = String.format(
                    "https://restapi.amap.com/v3/geocode/regeo?location=%f,%f&key=%s",
                    longitude, latitude, amapApiKey
            );

            Map<String, Object> result = restTemplate.getForObject(url, Map.class);

            if (result == null || !"1".equals(result.get("status"))) {
                return "地址解析失败";
            }

            Map<String, Object> regeocode = (Map<String, Object>) result.get("regeocode");
            String formattedAddress = (String) regeocode.get("formatted_address");

            log.info("✅ 地址解析成功: {}", formattedAddress);
            return "📍 该位置的地址是：" + formattedAddress;

        } catch (Exception e) {
            log.error("❌ 地址解析失败", e);
            return "地址解析出错: " + e.getMessage();
        }
    }

    /**
     * 🔍 搜索地点（POI 搜索）
     */
    @Tool("""
            搜索指定城市的地点（如餐厅、酒店、景点等）。
            参数：
            - keyword: 搜索关键词（如：火锅、咖啡厅）
            - city: 城市名称（如：北京、深圳）
            返回：地点列表
            """)
    public String searchPlace(
            @P("搜索关键词") String keyword,
            @P("城市名称") String city) {
        log.info("🔧 Tool调用 - 搜索地点: {} in {}", keyword, city);

        try {
            // 🆕 第一步：先获取城市的 adcode（城市编码）
            String geocodeUrl = String.format(
                    "https://restapi.amap.com/v3/geocode/geo?address=%s&key=%s",
                    city,
                    amapApiKey
            );

            Map<String, Object> geocodeResult = restTemplate.getForObject(geocodeUrl, Map.class);

            // 提取 adcode
            String cityCode = city;  // 默认使用城市名称
            if (geocodeResult != null && "1".equals(geocodeResult.get("status"))) {
                List<Map<String, Object>> geocodes = (List<Map<String, Object>>) geocodeResult.get("geocodes");
                if (geocodes != null && !geocodes.isEmpty()) {
                    cityCode = (String) geocodes.get(0).get("adcode");
                    log.info("📍 城市编码: {} -> {}", city, cityCode);
                }
            }

            // 🆕 第二步：使用 adcode 进行精确搜索
            String url = String.format(
                    "https://restapi.amap.com/v3/place/text?keywords=%s&city=%s&key=%s&citylimit=true",
                    //                                                                  ^^^^^^^^^^^^^^^^
                    //                                                            严格限制在该城市内搜索
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8),
                    cityCode,  // ✅ 使用城市编码
                    amapApiKey
            );

            log.info("🔍 搜索URL: {}", url);

            Map<String, Object> result = restTemplate.getForObject(url, Map.class);

            if (result == null || !"1".equals(result.get("status"))) {
                log.warn("⚠️ API返回状态异常: {}", result);
                return "搜索失败，可能是城市名称错误或API限制";
            }

            List<Map<String, Object>> pois = (List<Map<String, Object>>) result.get("pois");

            if (pois == null || pois.isEmpty()) {
                return String.format("未在 %s 找到与 '%s' 相关的地点", city, keyword);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("🔍 在 %s 找到 %d 个与 '%s' 相关的地点：\n\n",
                    city, Math.min(5, pois.size()), keyword));

            // 返回前5个结果
            for (int i = 0; i < Math.min(5, pois.size()); i++) {
                Map<String, Object> poi = pois.get(i);

                // 🆕 提取详细地址信息
                String name = (String) poi.get("name");
                String address = (String) poi.get("address");
                String provinceName = (String) poi.getOrDefault("pname", "");
                String cityName = (String) poi.getOrDefault("cityname", "");
                Object tel = poi.get("tel");

                // 拼接完整地址
                String fullAddress = provinceName + cityName + address;

                sb.append(String.format("%d. **%s**\n   📍 地址：%s\n   📞 电话：%s\n\n",
                        i + 1,
                        name,
                        fullAddress,
                        tel
                ));
            }

            log.info("✅ 搜索成功: {} 个结果", pois.size());
            return sb.toString();

        } catch (Exception e) {
            log.error("❌ 搜索失败", e);
            return "搜索出错: " + e.getMessage();
        }
    }

    /**
     * 🕐 获取当前时间
     */
    @Tool("获取当前的日期和时间")
    public String getCurrentTime() {
        log.info("🔧 Tool调用 - 获取当前时间");
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss E"));
        return "📅 当前时间：" + formatted;
    }

    /**
     * 🧮 计算器
     */
    @Tool("""
            执行数学计算。
            参数：
            - expression: 数学表达式（如：2+3*4）
            返回：计算结果
            """)
    public String calculate(@P("数学表达式") String expression) {
        log.info("🔧 Tool调用 - 计算: {}", expression);

        try {
            // 简单实现，实际可以用 JavaScript 引擎
            javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");
            Object result = engine.eval(expression);

            return String.format("🧮 %s = %s", expression, result);

        } catch (Exception e) {
            log.error("❌ 计算失败: {}", expression, e);
            return "计算出错: " + e.getMessage();
        }
    }
}