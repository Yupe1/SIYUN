package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/*
    此表仅供查询
    此表仅供查询
    此表仅供查询
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_front_user")
public class ObjFrontUserWithWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Integer id;

    private String stuTel;
    private String password;
    private String chinaId;
    private String avataUrl;
    private String nickName;
    //0未认证 1已认证(创作者认证)
    private Integer createrVerified;
    private String stuNo;
    private Integer studyDuration;
    //0正常 1禁言 2封停 3注销
    private Integer status;
    //0女 1男
    private Integer gender;
    private LocalDate birth;
    private String email;
    private String jobOrient;
    private String remark;
    //账户余额
    private Double wallet;
    @Version
    private Integer version;
}
