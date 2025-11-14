-- 创建学生信息表
CREATE TABLE students
(
    student_id      INT PRIMARY KEY AUTO_INCREMENT,
    student_no      VARCHAR(20) UNIQUE                    NOT NULL COMMENT '学号',
    name            VARCHAR(50)                           NOT NULL COMMENT '姓名',
    gender          ENUM ('男', '女', '其他') COMMENT '性别',
    birth_date      DATE COMMENT '出生日期',
    email           VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone           VARCHAR(20) COMMENT '电话',
    address         VARCHAR(200) COMMENT '地址',
    enrollment_date DATE                                  NOT NULL COMMENT '入学日期',
    major_id        INT                                   NOT NULL COMMENT '专业ID',
    class_id        INT                                   NOT NULL COMMENT '班级ID',
    status          ENUM ('在读', '休学', '毕业', '退学') NOT NULL DEFAULT '在读' COMMENT '状态',
    created_at      TIMESTAMP                                      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建专业表
CREATE TABLE majors
(
    major_id      INT PRIMARY KEY AUTO_INCREMENT,
    major_name    VARCHAR(50) UNIQUE NOT NULL COMMENT '专业名称',
    department_id INT                NOT NULL COMMENT '院系ID',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建院系表
CREATE TABLE departments
(
    department_id   INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(50) UNIQUE NOT NULL COMMENT '院系名称',
    dean            VARCHAR(50) COMMENT '院长',
    phone           VARCHAR(20) COMMENT '联系电话',
    email           VARCHAR(100) COMMENT '邮箱',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建班级表
CREATE TABLE classes
(
    class_id     INT PRIMARY KEY AUTO_INCREMENT,
    class_name   VARCHAR(50) NOT NULL COMMENT '班级名称',
    major_id     INT         NOT NULL COMMENT '专业ID',
    grade        INT         NOT NULL COMMENT '年级',
    head_teacher VARCHAR(50) COMMENT '班主任',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建课程表
CREATE TABLE courses
(
    course_id     INT PRIMARY KEY AUTO_INCREMENT,
    course_code   VARCHAR(20) UNIQUE                  NOT NULL COMMENT '课程代码',
    course_name   VARCHAR(100)                        NOT NULL COMMENT '课程名称',
    credit        TINYINT                             NOT NULL COMMENT '学分',
    course_type   ENUM ('必修课', '选修课', '公共课') NOT NULL COMMENT '课程类型',
    department_id INT                                 NOT NULL COMMENT '开课院系ID',
    teacher_id    INT                                 NOT NULL COMMENT '授课教师ID',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建教师表
CREATE TABLE teachers
(
    teacher_id    INT PRIMARY KEY AUTO_INCREMENT,
    teacher_no    VARCHAR(20) UNIQUE NOT NULL COMMENT '教师编号',
    name          VARCHAR(50)        NOT NULL COMMENT '姓名',
    gender        ENUM ('男', '女', '其他') COMMENT '性别',
    title         VARCHAR(50) COMMENT '职称',
    department_id INT                NOT NULL COMMENT '所属院系ID',
    email         VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone         VARCHAR(20) COMMENT '电话',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建考试安排表
CREATE TABLE exam_arrangements
(
    exam_id       INT PRIMARY KEY AUTO_INCREMENT,
    course_id     INT                                   NOT NULL COMMENT '课程ID',
    exam_date     DATE                                  NOT NULL COMMENT '考试日期',
    start_time    TIME                                  NOT NULL COMMENT '开始时间',
    end_time      TIME                                  NOT NULL COMMENT '结束时间',
    exam_room     VARCHAR(50)                           NOT NULL COMMENT '考场',
    invigilator   VARCHAR(100) COMMENT '监考老师',
    exam_type     ENUM ('期中', '期末', '补考', '重修') NOT NULL COMMENT '考试类型',
    academic_year VARCHAR(20)                           NOT NULL COMMENT '学年',
    semester      TINYINT                               NOT NULL COMMENT '学期',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建成绩表
CREATE TABLE scores
(
    score_id   INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT                                           NOT NULL COMMENT '学生ID',
    exam_id    INT                                           NOT NULL COMMENT '考试ID',
    score      DECIMAL(5, 2) COMMENT '成绩',
    score_type ENUM ('原始分', '平时分', '卷面分', '最终分') NOT NULL DEFAULT '最终分' COMMENT '成绩类型',
    remark     VARCHAR(200) COMMENT '备注',
    created_at TIMESTAMP                                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP                                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_student_exam (student_id, exam_id)
);

-- 创建索引
CREATE INDEX idx_students_student_no ON students (student_no);
CREATE INDEX idx_students_major_id ON students (major_id);
CREATE INDEX idx_students_class_id ON students (class_id);
CREATE INDEX idx_majors_department_id ON majors (department_id);
CREATE INDEX idx_classes_major_id ON classes (major_id);
CREATE INDEX idx_courses_department_id ON courses (department_id);
CREATE INDEX idx_courses_teacher_id ON courses (teacher_id);
CREATE INDEX idx_teachers_department_id ON teachers (department_id);
CREATE INDEX idx_exam_arrangements_course_id ON exam_arrangements (course_id);
CREATE INDEX idx_scores_student_id ON scores (student_id);
CREATE INDEX idx_scores_exam_id ON scores (exam_id);

-- 添加外键约束
ALTER TABLE students
    ADD CONSTRAINT fk_students_major_id FOREIGN KEY (major_id) REFERENCES majors (major_id);
ALTER TABLE students
    ADD CONSTRAINT fk_students_class_id FOREIGN KEY (class_id) REFERENCES classes (class_id);
ALTER TABLE majors
    ADD CONSTRAINT fk_majors_department_id FOREIGN KEY (department_id) REFERENCES departments (department_id);
ALTER TABLE classes
    ADD CONSTRAINT fk_classes_major_id FOREIGN KEY (major_id) REFERENCES majors (major_id);
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_department_id FOREIGN KEY (department_id) REFERENCES departments (department_id);
ALTER TABLE courses
    ADD CONSTRAINT fk_courses_teacher_id FOREIGN KEY (teacher_id) REFERENCES teachers (teacher_id);
ALTER TABLE teachers
    ADD CONSTRAINT fk_teachers_department_id FOREIGN KEY (department_id) REFERENCES departments (department_id);
ALTER TABLE exam_arrangements
    ADD CONSTRAINT fk_exam_arrangements_course_id FOREIGN KEY (course_id) REFERENCES courses (course_id);
ALTER TABLE scores
    ADD CONSTRAINT fk_scores_student_id FOREIGN KEY (student_id) REFERENCES students (student_id);
ALTER TABLE scores
    ADD CONSTRAINT fk_scores_exam_id FOREIGN KEY (exam_id) REFERENCES exam_arrangements (exam_id);

-- ============================================================
-- 样例数据插入
-- ============================================================

-- 1. 插入院系数据
INSERT INTO departments (department_name, dean, phone, email) VALUES
('计算机学院', '王教授', '010-88888001', 'cs@university.edu'),
('数学系', '李教授', '010-88888002', 'math@university.edu'),
('文学院', '张教授', '010-88888003', 'lit@university.edu'),
('物理学院', '刘教授', '010-88888004', 'physics@university.edu'),
('经济管理学院', '陈教授', '010-88888005', 'econ@university.edu');

-- 2. 插入专业数据
INSERT INTO majors (major_name, department_id) VALUES
('计算机科学与技术', 1),
('软件工程', 1),
('人工智能', 1),
('信息安全', 1),
('数学与应用数学', 2),
('统计学', 2),
('汉语言文学', 3),
('应用物理学', 4),
('金融学', 5),
('会计学', 5);

-- 3. 插入班级数据
INSERT INTO classes (class_name, major_id, grade, head_teacher) VALUES
('计科2101班', 1, 2021, '王老师'),
('计科2102班', 1, 2021, '李老师'),
('软工2101班', 2, 2021, '张老师'),
('AI2101班', 3, 2021, '刘老师'),
('信安2101班', 4, 2021, '陈老师'),
('数学2101班', 5, 2021, '赵老师'),
('计科2201班', 1, 2022, '孙老师'),
('软工2201班', 2, 2022, '周老师');

-- 4. 插入教师数据
INSERT INTO teachers (teacher_no, name, gender, title, department_id, email, phone) VALUES
('T001', '王建国', '男', '教授', 1, 'wangjianguo@university.edu', '13900001001'),
('T002', '李明华', '女', '副教授', 1, 'liminghua@university.edu', '13900001002'),
('T003', '张伟', '男', '讲师', 2, 'zhangwei@university.edu', '13900001003'),
('T004', '刘芳', '女', '教授', 2, 'liufang@university.edu', '13900001004'),
('T005', '陈静', '女', '副教授', 3, 'chenjing@university.edu', '13900001005'),
('T006', '赵强', '男', '讲师', 1, 'zhaoqiang@university.edu', '13900001006'),
('T007', '孙丽', '女', '教授', 4, 'sunli@university.edu', '13900001007'),
('T008', '周杰', '男', '副教授', 1, 'zhoujie@university.edu', '13900001008');

-- 5. 插入课程数据
INSERT INTO courses (course_code, course_name, credit, course_type, department_id, teacher_id) VALUES
('CS101', '高等数学', 4, '必修课', 2, 3),
('CS102', '大学语文', 2, '公共课', 3, 5),
('CS201', '数据结构', 4, '必修课', 1, 1),
('CS202', '算法设计与分析', 3, '必修课', 1, 2),
('CS203', '操作系统', 4, '必修课', 1, 1),
('CS204', '计算机网络', 3, '必修课', 1, 6),
('CS205', '数据库原理', 4, '必修课', 1, 8),
('CS301', '人工智能导论', 3, '选修课', 1, 2),
('CS302', '机器学习', 3, '选修课', 1, 8),
('MATH101', '线性代数', 3, '必修课', 2, 4),
('MATH201', '概率论与数理统计', 4, '必修课', 2, 4),
('PHY101', '大学物理', 4, '公共课', 4, 7);

-- 6. 插入学生数据（包含测试用户）
INSERT INTO students (student_no, name, gender, birth_date, email, phone, address, enrollment_date, major_id, class_id, status) VALUES
('2021001', '张铁牛', '男', '2003-05-15', 'zhangtieniu@stu.edu', '13800001001', '北京市海淀区', '2021-09-01', 1, 1, '在读'),
('2021002', '李小花', '女', '2003-08-20', 'lixiaohua@stu.edu', '13800001002', '上海市浦东新区', '2021-09-01', 2, 3, '在读'),
('2021003', '王大锤', '男', '2003-03-10', 'wangdachui@stu.edu', '13800001003', '广州市天河区', '2021-09-01', 1, 1, '在读'),
('2021004', '赵小美', '女', '2003-11-25', 'zhaoxiaomei@stu.edu', '13800001004', '深圳市南山区', '2021-09-01', 3, 4, '在读'),
('2021005', '孙大圣', '男', '2003-07-08', 'sundassheng@stu.edu', '13800001005', '杭州市西湖区', '2021-09-01', 1, 2, '在读'),
('2021006', '周小雨', '女', '2003-09-12', 'zhouxiaoyu@stu.edu', '13800001006', '成都市武侯区', '2021-09-01', 2, 3, '在读'),
('2021007', '吴大伟', '男', '2003-04-18', 'wudawei@stu.edu', '13800001007', '武汉市洪山区', '2021-09-01', 4, 5, '在读'),
('2021008', '郑小雪', '女', '2003-12-05', 'zhengxiaoxue@stu.edu', '13800001008', '西安市雁塔区', '2021-09-01', 1, 1, '在读'),
('2021009', '朱老七', '男', '2003-06-22', 'zhulaoqi@stu.edu', '13800001009', '南京市玄武区', '2021-09-01', 1, 2, '在读'),
('2021010', '林小美', '女', '2003-10-30', 'linxiaomei@stu.edu', '13800001010', '重庆市渝中区', '2021-09-01', 3, 4, '在读'),
('2022001', '陈大明', '男', '2004-02-14', 'chendaming@stu.edu', '13800001011', '天津市南开区', '2022-09-01', 1, 7, '在读'),
('2022002', '黄小丽', '女', '2004-05-28', 'huangxiaoli@stu.edu', '13800001012', '苏州市姑苏区', '2022-09-01', 2, 8, '在读');

-- 7. 插入考试安排数据
-- 2023-2024学年 上学期 期中考试
INSERT INTO exam_arrangements (course_id, exam_date, start_time, end_time, exam_room, invigilator, exam_type, academic_year, semester) VALUES
(1, '2023-11-15', '09:00:00', '11:00:00', 'A101', '王建国,张伟', '期中', '2023-2024', 1),
(2, '2023-11-16', '09:00:00', '11:00:00', 'B201', '陈静', '期中', '2023-2024', 1),
(3, '2023-11-17', '14:00:00', '16:00:00', 'A102', '王建国,李明华', '期中', '2023-2024', 1),
(4, '2023-11-18', '09:00:00', '11:00:00', 'A103', '李明华', '期中', '2023-2024', 1),
(10, '2023-11-19', '14:00:00', '16:00:00', 'B202', '刘芳', '期中', '2023-2024', 1);

-- 2023-2024学年 上学期 期末考试
INSERT INTO exam_arrangements (course_id, exam_date, start_time, end_time, exam_room, invigilator, exam_type, academic_year, semester) VALUES
(1, '2024-01-10', '09:00:00', '11:00:00', 'A101', '张伟,刘芳', '期末', '2023-2024', 1),
(2, '2024-01-11', '09:00:00', '11:00:00', 'B201', '陈静', '期末', '2023-2024', 1),
(3, '2024-01-12', '14:00:00', '16:00:00', 'A102', '王建国,李明华', '期末', '2023-2024', 1),
(4, '2024-01-13', '09:00:00', '11:00:00', 'A103', '李明华,赵强', '期末', '2023-2024', 1),
(10, '2024-01-14', '14:00:00', '16:00:00', 'B202', '刘芳', '期末', '2023-2024', 1);

-- 2023-2024学年 下学期 期中考试
INSERT INTO exam_arrangements (course_id, exam_date, start_time, end_time, exam_room, invigilator, exam_type, academic_year, semester) VALUES
(5, '2024-04-20', '09:00:00', '11:00:00', 'A101', '王建国', '期中', '2023-2024', 2),
(6, '2024-04-21', '14:00:00', '16:00:00', 'A102', '赵强', '期中', '2023-2024', 2),
(7, '2024-04-22', '09:00:00', '11:00:00', 'A103', '周杰', '期中', '2023-2024', 2),
(11, '2024-04-23', '14:00:00', '16:00:00', 'B202', '刘芳', '期中', '2023-2024', 2);

-- 2023-2024学年 下学期 期末考试
INSERT INTO exam_arrangements (course_id, exam_date, start_time, end_time, exam_room, invigilator, exam_type, academic_year, semester) VALUES
(5, '2024-06-25', '09:00:00', '11:00:00', 'A101', '王建国,赵强', '期末', '2023-2024', 2),
(6, '2024-06-26', '14:00:00', '16:00:00', 'A102', '赵强', '期末', '2023-2024', 2),
(7, '2024-06-27', '09:00:00', '11:00:00', 'A103', '周杰,李明华', '期末', '2023-2024', 2),
(11, '2024-06-28', '14:00:00', '16:00:00', 'B202', '刘芳', '期末', '2023-2024', 2);

-- 8. 插入成绩数据（包含所有测试学生的完整成绩）
-- 张铁牛(student_id=1)的成绩
INSERT INTO scores (student_id, exam_id, score, score_type, remark) VALUES
-- 2023-2024上学期
(1, 1, 85.00, '最终分', '期中表现良好'), (1, 2, 78.00, '最终分', NULL), (1, 3, 92.00, '最终分', '优秀'),
(1, 4, 88.00, '最终分', NULL), (1, 5, 90.00, '最终分', NULL),
(1, 6, 88.50, '最终分', '进步明显'), (1, 7, 80.00, '最终分', NULL), (1, 8, 95.00, '最终分', '优秀'),
(1, 9, 90.00, '最终分', NULL), (1, 10, 92.50, '最终分', NULL),
-- 2023-2024下学期
(1, 11, 87.00, '最终分', NULL), (1, 12, 83.00, '最终分', NULL), (1, 13, 91.00, '最终分', NULL),
(1, 14, 88.00, '最终分', NULL), (1, 15, 89.50, '最终分', NULL), (1, 16, 85.00, '最终分', NULL),
(1, 17, 93.00, '最终分', '优秀'), (1, 18, 90.00, '最终分', NULL);

-- 李小花(student_id=2)的成绩
INSERT INTO scores (student_id, exam_id, score, score_type, remark) VALUES
(2, 1, 92.00, '最终分', '优秀'), (2, 2, 88.00, '最终分', NULL), (2, 3, 95.00, '最终分', '优秀'),
(2, 4, 91.00, '最终分', NULL), (2, 5, 94.00, '最终分', '优秀'),
(2, 6, 93.50, '最终分', '优秀'), (2, 7, 90.00, '最终分', NULL), (2, 8, 96.00, '最终分', '优秀'),
(2, 9, 93.00, '最终分', NULL), (2, 10, 95.00, '最终分', '优秀'),
(2, 11, 92.00, '最终分', NULL), (2, 12, 89.00, '最终分', NULL), (2, 13, 94.00, '最终分', '优秀'),
(2, 14, 91.00, '最终分', NULL), (2, 15, 93.50, '最终分', '优秀'), (2, 16, 90.00, '最终分', NULL),
(2, 17, 95.00, '最终分', '优秀'), (2, 18, 92.50, '最终分', NULL);

-- 王大锤(student_id=3)的成绩
INSERT INTO scores (student_id, exam_id, score, score_type, remark) VALUES
(3, 1, 72.00, '最终分', '需要加强'), (3, 2, 75.00, '最终分', NULL), (3, 3, 80.00, '最终分', NULL),
(3, 4, 76.00, '最终分', NULL), (3, 5, 78.00, '最终分', NULL),
(3, 6, 74.50, '最终分', NULL), (3, 7, 77.00, '最终分', NULL), (3, 8, 82.00, '最终分', '有进步'),
(3, 9, 78.00, '最终分', NULL), (3, 10, 80.00, '最终分', NULL),
(3, 11, 81.00, '最终分', NULL), (3, 12, 79.00, '最终分', NULL), (3, 13, 83.00, '最终分', NULL),
(3, 14, 77.00, '最终分', NULL), (3, 15, 82.50, '最终分', NULL), (3, 16, 80.00, '最终分', NULL),
(3, 17, 84.00, '最终分', NULL), (3, 18, 79.00, '最终分', NULL);

-- 赵小美(student_id=4)的成绩
INSERT INTO scores (student_id, exam_id, score, score_type, remark) VALUES
(4, 1, 95.00, '最终分', '优秀'), (4, 2, 91.00, '最终分', NULL), (4, 3, 98.00, '最终分', '优秀'),
(4, 4, 94.00, '最终分', NULL), (4, 5, 96.00, '最终分', '优秀'),
(4, 6, 96.50, '最终分', '优秀'), (4, 7, 92.00, '最终分', NULL), (4, 8, 99.00, '最终分', '优秀'),
(4, 9, 95.00, '最终分', '优秀'), (4, 10, 97.00, '最终分', '优秀'),
(4, 11, 96.00, '最终分', '优秀'), (4, 12, 93.00, '最终分', NULL), (4, 13, 97.00, '最终分', '优秀'),
(4, 14, 94.00, '最终分', NULL), (4, 15, 97.50, '最终分', '优秀'), (4, 16, 94.00, '最终分', NULL),
(4, 17, 98.00, '最终分', '优秀'), (4, 18, 95.50, '最终分', '优秀');

-- 朱老七(student_id=9)的成绩
INSERT INTO scores (student_id, exam_id, score, score_type, remark) VALUES
(9, 1, 87.00, '最终分', NULL), (9, 2, 85.00, '最终分', NULL), (9, 3, 90.00, '最终分', NULL),
(9, 4, 88.00, '最终分', NULL), (9, 5, 89.00, '最终分', NULL),
(9, 6, 88.50, '最终分', NULL), (9, 7, 86.00, '最终分', NULL), (9, 8, 91.00, '最终分', '优秀'),
(9, 9, 89.00, '最终分', NULL), (9, 10, 90.00, '最终分', NULL),
(9, 11, 89.00, '最终分', NULL), (9, 12, 87.00, '最终分', NULL), (9, 13, 92.00, '最终分', '优秀'),
(9, 14, 88.00, '最终分', NULL), (9, 15, 90.50, '最终分', '优秀'), (9, 16, 88.00, '最终分', NULL),
(9, 17, 93.00, '最终分', '优秀'), (9, 18, 89.50, '最终分', NULL);

-- 其他学生成绩
INSERT INTO scores (student_id, exam_id, score, score_type) VALUES
(5, 1, 80.00, '最终分'), (5, 2, 82.00, '最终分'), (5, 3, 85.00, '最终分'), (5, 6, 82.50, '最终分'), (5, 8, 86.00, '最终分'),
(6, 1, 88.00, '最终分'), (6, 2, 90.00, '最终分'), (6, 3, 92.00, '最终分'), (6, 6, 89.50, '最终分'), (6, 8, 93.00, '最终分'),
(7, 1, 76.00, '最终分'), (7, 2, 79.00, '最终分'), (7, 3, 81.00, '最终分'), (7, 6, 78.50, '最终分'), (7, 8, 82.00, '最终分'),
(8, 1, 84.00, '最终分'), (8, 2, 86.00, '最终分'), (8, 3, 89.00, '最终分'), (8, 6, 85.50, '最终分'), (8, 8, 90.00, '最终分'),
(10, 1, 91.00, '最终分'), (10, 2, 89.00, '最终分'), (10, 3, 94.00, '最终分'), (10, 6, 92.50, '最终分'), (10, 8, 95.00, '最终分'),
(11, 1, 79.00, '最终分'), (11, 2, 81.00, '最终分'), (11, 3, 83.00, '最终分'), (11, 6, 80.50, '最终分'), (11, 8, 84.00, '最终分'),
(12, 1, 86.00, '最终分'), (12, 2, 88.00, '最终分'), (12, 3, 90.00, '最终分'), (12, 6, 87.50, '最终分'), (12, 8, 91.00, '最终分');