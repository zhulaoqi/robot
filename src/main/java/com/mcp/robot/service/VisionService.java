package com.mcp.robot.service;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 多模态服务（图片+文本）
 */
@AiService(
    wiringMode = EXPLICIT,
    chatModel = "openAiChatModel"
)
public interface VisionService {
    
    /**
     * 🖼️ 图片分析
     */
    @UserMessage("""
            请分析这张图片的内容：
            {{imageUrl}}
            
            {{question}}
            """)
    String analyzeImage(
        @V("imageUrl") String imageUrl,
        @V("question") String question
    );
}