package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台部门表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("js_dept")
public class JsDept implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门描述
     */
    private String deptDesc;

    /**
     * 上级部门主键
     */
    private Integer parentId;

    /**
     * 0未启用 1启用
     */
    private Integer status;

    /**
     * 创建人id
     */
    private Integer createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

