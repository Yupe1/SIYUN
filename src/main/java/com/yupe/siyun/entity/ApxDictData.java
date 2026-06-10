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
 * 数据字典明细表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("apx_dict_data")
public class ApxDictData implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 排序序号
     */
    private Integer sortNum;

    /**
     * 字典标签(中文显示)
     */
    private String dictLabel;

    /**
     * 字典键值(实际传值)
     */
    private String dictValue;

    /**
     * 字典类型标识
     */
    private String dictType;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格回显样式
     */
    private String listClass;

    /**
     * 0否 1是
     */
    private Integer isDefault;

    /**
     * 0正常 1停用
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

    /**
     * 备注
     */
    private String remark;
}

