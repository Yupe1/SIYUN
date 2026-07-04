package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资格职位申请表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("qf_position_apply")
public class QfPositionApply implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime applyTime;
    private Integer userId;
    private String targetPosition;
    private String applyReason;
    private String chinaId;
    private String tel;
    private String email;
    private String fileUrl;//作品
    //执行人(人事id)
    private Integer handlerId;
    private LocalDateTime handleTime;
    //0申请中 1审核中 2已通过 3已驳回
    private Integer status;
    private String handleRemark;
}

