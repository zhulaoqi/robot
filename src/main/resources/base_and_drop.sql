SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS exam_arrangements;
DROP TABLE IF EXISTS knowledge_embedding;
DROP TABLE IF EXISTS majors;
DROP TABLE IF EXISTS scores;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS teachers;

SET FOREIGN_KEY_CHECKS = 1;



-- 创建对话记忆表
CREATE TABLE IF NOT EXISTS chat_memory (
       id           BIGINT AUTO_INCREMENT PRIMARY KEY,
       memory_id    VARCHAR(255) NOT NULL COMMENT '会话ID',
       message_type VARCHAR(50)  NOT NULL COMMENT '消息类型：USER/AI/SYSTEM/TOOL',
       message_text TEXT         NOT NULL COMMENT '消息内容（JSON）',
       created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
       INDEX idx_memory_id (memory_id),
       INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记忆存储表';

-- 创建向量知识表
CREATE TABLE IF NOT EXISTS knowledge_embedding (
       id               BIGINT AUTO_INCREMENT PRIMARY KEY,
       embedding_id     VARCHAR(64) NOT NULL UNIQUE COMMENT '向量唯一ID',
       content          TEXT        NOT NULL COMMENT '原始文本',
       embedding_vector LONGTEXT    NOT NULL COMMENT '向量数据（JSON数组，1536维）',
       metadata_json    TEXT COMMENT '元数据（JSON）',
       created_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
       INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库向量存储表';
