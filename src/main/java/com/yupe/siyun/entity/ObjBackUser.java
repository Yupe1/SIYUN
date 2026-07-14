package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
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


    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    private String name;
    //0女 1男
    private Integer gender;
    private String avataUrl;
    private String email;
    private String password;
    private String tel;
    private String chinaId;
    private LocalDate birth;
    private Integer deptId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDate regieterDate;
    private String registerIp;
    //0正常 1禁言 2封停 3注销
    private Integer status;
    private Integer level;
    private BigDecimal salary;
    private String remark;
}
