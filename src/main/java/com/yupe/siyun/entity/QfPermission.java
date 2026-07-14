package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统权限控制表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("qf_permission")
public class QfPermission implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    //权限名称
    private String name;
    //权限描述
    private String permDesc;
    //父级权限主键
    private Integer parentId;
    private String path;//前端路由路径
    private String perms;//权限标识符 后端路径 eg: hr:goods:delete
    //0停用 1正常
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    //更新人id
    private Integer updateBy;
}
