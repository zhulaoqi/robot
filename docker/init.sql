-- ============================================
-- Langchain4j ChatBI 项目 - 数据库初始化脚本
-- ============================================

-- 创建对话记忆表
CREATE TABLE IF NOT EXISTS chat_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    memory_id VARCHAR(255) NOT NULL COMMENT '会话ID',
    message_type VARCHAR(50) NOT NULL COMMENT '消息类型：USER/AI/SYSTEM/TOOL',
    message_text TEXT NOT NULL COMMENT '消息内容（JSON）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_memory_id (memory_id),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记忆存储表';

-- 创建向量知识表
CREATE TABLE IF NOT EXISTS knowledge_embedding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    embedding_id VARCHAR(64) NOT NULL UNIQUE COMMENT '向量唯一ID',
    content TEXT NOT NULL COMMENT '原始文本',
    embedding_vector LONGTEXT NOT NULL COMMENT '向量数据（JSON数组，1536维）',
    metadata_json TEXT COMMENT '元数据（JSON）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库向量存储表';

-- ============================================
-- BI 演示数据 - 学生管理系统
-- ============================================

-- 创建院系表
CREATE TABLE IF NOT EXISTS departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(50) UNIQUE NOT NULL COMMENT '院系名称',
    dean VARCHAR(50) COMMENT '院长',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

-- 创建专业表
CREATE TABLE IF NOT EXISTS majors (
    major_id INT PRIMARY KEY AUTO_INCREMENT,
    major_name VARCHAR(50) UNIQUE NOT NULL COMMENT '专业名称',
    department_id INT NOT NULL COMMENT '院系ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 创建班级表
CREATE TABLE IF NOT EXISTS classes (
    class_id INT PRIMARY KEY AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    major_id INT NOT NULL COMMENT '专业ID',
    grade INT NOT NULL COMMENT '年级',
    head_teacher VARCHAR(50) COMMENT '班主任',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (major_id) REFERENCES majors(major_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 创建学生信息表
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(20) UNIQUE NOT NULL COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('男', '女', '其他') COMMENT '性别',
    birth_date DATE COMMENT '出生日期',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    address VARCHAR(200) COMMENT '地址',
    enrollment_date DATE NOT NULL COMMENT '入学日期',
    major_id INT NOT NULL COMMENT '专业ID',
    class_id INT NOT NULL COMMENT '班级ID',
    status ENUM('在读', '休学', '毕业', '退学') NOT NULL DEFAULT '在读' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (major_id) REFERENCES majors(major_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    INDEX idx_students_student_no (student_no),
    INDEX idx_students_major_id (major_id),
    INDEX idx_students_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 创建教师表
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_no VARCHAR(20) UNIQUE NOT NULL COMMENT '教师编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('男', '女', '其他') COMMENT '性别',
    title VARCHAR(50) COMMENT '职称',
    department_id INT NOT NULL COMMENT '所属院系ID',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES departments(department_id),
    INDEX idx_teachers_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 创建课程表
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程代码',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit TINYINT NOT NULL COMMENT '学分',
    course_type ENUM('必修课', '选修课', '公共课') NOT NULL COMMENT '课程类型',
    department_id INT NOT NULL COMMENT '开课院系ID',
    teacher_id INT NOT NULL COMMENT '授课教师ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES departments(department_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    INDEX idx_courses_department_id (department_id),
    INDEX idx_courses_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 创建考试安排表
CREATE TABLE IF NOT EXISTS exam_arrangements (
    exam_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL COMMENT '课程ID',
    exam_date DATE NOT NULL COMMENT '考试日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    exam_room VARCHAR(50) NOT NULL COMMENT '考场',
    invigilator VARCHAR(100) COMMENT '监考老师',
    exam_type ENUM('期中', '期末', '补考', '重修') NOT NULL COMMENT '考试类型',
    academic_year VARCHAR(20) NOT NULL COMMENT '学年',
    semester TINYINT NOT NULL COMMENT '学期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (course_id) REFERENCES courses(course_id),
    INDEX idx_exam_arrangements_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

-- 创建成绩表
CREATE TABLE IF NOT EXISTS scores (
    score_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL COMMENT '学生ID',
    exam_id INT NOT NULL COMMENT '考试ID',
    score DECIMAL(5,2) COMMENT '成绩',
    score_type ENUM('原始分', '平时分', '卷面分', '最终分') NOT NULL DEFAULT '最终分' COMMENT '成绩类型',
    remark VARCHAR(200) COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_student_exam (student_id, exam_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (exam_id) REFERENCES exam_arrangements(exam_id),
    INDEX idx_scores_student_id (student_id),
    INDEX idx_scores_exam_id (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- ============================================
-- 插入演示数据
-- ============================================

-- 插入院系数据
INSERT INTO departments (department_name, dean, phone, email) VALUES
('计算机学院', '张教授', '010-12345678', 'cs@example.com'),
('文学院', '李教授', '010-23456789', 'literature@example.com');

-- 插入专业数据
INSERT INTO majors (major_name, department_id) VALUES
('软件工程', 1),
('汉语言文学', 2);

-- 插入班级数据
INSERT INTO classes (class_name, major_id, grade, head_teacher) VALUES
('软工2021级1班', 1, 2021, '王老师'),
('中文2021级1班', 2, 2021, '赵老师');

-- 插入学生数据
INSERT INTO students (student_no, name, gender, birth_date, email, phone, enrollment_date, major_id, class_id, status) VALUES
('2021001', '张铁牛', '男', '2003-05-15', 'zhang@example.com', '13800138001', '2021-09-01', 1, 1, '在读'),
('2021002', '李小花', '女', '2003-08-20', 'li@example.com', '13800138002', '2021-09-01', 2, 2, '在读');

-- 插入教师数据
INSERT INTO teachers (teacher_no, name, gender, title, department_id, email, phone) VALUES
('T001', '陈教授', '男', '教授', 1, 'chen@example.com', '13900139001'),
('T002', '刘老师', '女', '讲师', 2, 'liu@example.com', '13900139002');

-- 插入课程数据
INSERT INTO courses (course_code, course_name, credit, course_type, department_id, teacher_id) VALUES
('CS101', '数据结构', 4, '必修课', 1, 1),
('CHI101', '大学语文', 3, '公共课', 2, 2);

-- 插入考试安排数据
INSERT INTO exam_arrangements (course_id, exam_date, start_time, end_time, exam_room, invigilator, exam_type, academic_year, semester) VALUES
(1, '2024-01-15', '09:00:00', '11:00:00', 'A101', '王老师', '期末', '2023-2024', 1),
(2, '2024-01-20', '14:00:00', '16:00:00', 'B201', '赵老师', '期末', '2023-2024', 1);

-- 插入成绩数据
INSERT INTO scores (student_id, exam_id, score, score_type) VALUES
(1, 1, 92.00, '最终分'),
(1, 2, 85.00, '最终分'),
(2, 2, 88.00, '最终分');

