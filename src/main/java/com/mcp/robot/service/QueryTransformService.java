package com.mcp.robot.service;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryTransformService {

    private final ChatModel chatModel;

    /**
     * 查询扩展
     */
    public String expandQuery(String originalQuery) {
        String prompt = String.format("""
                将以下用户查询扩展为更详细的描述，添加相关关键词。
                
                原始查询：%s
                
                要求：
                1. 保持原意
                2. 添加同义词、相关术语
                3. 明确查询意图
                
                只返回扩展后的查询：
                """, originalQuery);

        return chatModel.chat(prompt);
    }

    /**
     * 查询重写（面向 SQL）
     */
    public String rewriteForSql(String originalQuery) {
        String prompt = String.format("""
                将以下用户查询重写为适合数据库检索的专业描述。
                
                原始查询：%s
                
                要求：
                1. 识别需要查询的实体（表、字段）
                2. 明确查询条件和关联关系
                3. 使用数据库术语
                
                只返回重写后的查询：
                """, originalQuery);

        return chatModel.chat(prompt);
    }

    /**
     * 生成多个查询视角
     */
    public List<String> generateMultiQueries(String originalQuery) {
        String prompt = String.format("""
                将以下查询分解为 3 个不同的子查询，从不同角度理解问题。
                
                原始查询：%s
                
                返回格式（每行一个查询）：
                1. ...
                2. ...
                3. ...
                """, originalQuery);

        String response = chatModel.chat(prompt);

        // 解析返回的多个查询
        return Arrays.stream(response.split("\n"))
                .filter(line -> line.matches("^\\d+\\..*"))
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", ""))
                .toList();
    }

    /**
     * Step-back 查询
     */
    public String stepBackQuery(String originalQuery) {
        String prompt = String.format("""
                对于以下具体问题，生成一个更抽象、更通用的"后退"问题，
                用于先理解背景知识。
                
                原始问题：%s
                
                后退问题应该：
                1. 更抽象、更基础
                2. 有助于理解原问题的背景
                
                只返回后退问题：
                """, originalQuery);

        return chatModel.chat(prompt);
    }
}