package com.mcp.robot.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

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
}