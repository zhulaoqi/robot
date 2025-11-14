package com.mcp.robot.model;

import lombok.Data;

/**
 * 人员信息实体
 *
 * @author kinch.zhu
 * @create 2025-11-14 11:35
 */
@Data
public class Person {

    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别
     */
    private String gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 职业/职位
     */
    private String occupation;

    /**
     * 公司/学校
     */
    private String organization;
}
