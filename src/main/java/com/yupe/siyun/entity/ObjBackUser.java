package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 后端用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("obj_back_user")
public class ObjBackUser implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 0女 1男
     */
    private Integer gender;

    /**
     * 头像路径 (★需要上传)
     */
    private String avataUrl;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话
     */
    private String tel;

    /**
     * 身份证号
     */
    private String chinaId;

    /**
     * 出生日期
     */
    private LocalDate birth;

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 注册日期
     */
    private LocalDate regieterDate;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 0正常 1禁言 2封停 3注销
     */
    private Integer status;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 薪资
     */
    private BigDecimal salary;

    /**
     * 备注
     */
    private String remark;
}

