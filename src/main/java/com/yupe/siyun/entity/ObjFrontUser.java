package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 前端用户表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("obj_front_user")
public class ObjFrontUser implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学号/手机号
     */
    private String stuTel;

    /**
     * 密码
     */
    private String password;

    /**
     * 身份证号
     */
    private String chinaId;

    /**
     * 头像路径 (★需要上传)
     */
    private String avataUrl;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 0未认证 1已认证(创作者认证)
     */
    private Integer createrVerified;

    /**
     * 学号
     */
    private String stuNo;

    /**
     * 学习时长(分钟)
     */
    private Integer studyDuration;

    /**
     * 0正常 1禁言 2封停 3注销
     */
    private Integer status;

    /**
     * 0女 1男
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birth;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 职业意向
     */
    private String jobOrient;

    /**
     * 备注
     */
    private String remark;

    /**
     * 账户余额
     */
    private Double wallet;

    //修改密码用
    @TableField(exist = false)
    private String newPassword;
}

