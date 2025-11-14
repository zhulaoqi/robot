package com.mcp.robot.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * tools
 *
 * @author Kinch.zhu
 * @date 2025/5/15
 */
@Slf4j
@Component
public class SysTools {
    @Tool("根据用户的名称获取对应的code")
    public String getUserCodeByUsername(@P("用户名称") String username) {
        log.info("get user code by username:{}", username);
        if ("张铁牛".equals(username)) {
            return "003";
        }

        return "000";
    }
}