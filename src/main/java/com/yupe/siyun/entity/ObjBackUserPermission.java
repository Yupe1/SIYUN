package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/*
    此表仅供查询
    此表仅供查询
    此表仅供查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_backuser_permission")
public class ObjBackUserPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id")
    private Integer userId;
    private String userName;
    private Integer userLevel;
    private String avataUrl;
    private Integer userStatus;//正常0
    private String dept;
    private String roleName;
    private String roleKey;
    private String permissionName;
    private String parentName;//父权限名
    private String path;
    private String perms;
}
