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