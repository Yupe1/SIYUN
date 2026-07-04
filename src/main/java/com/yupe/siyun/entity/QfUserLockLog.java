package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 账号封停记录表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("qf_user_lock_log")
public class QfUserLockLog implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime lockTime;
    //封停时长(小时)
    private Integer lockDuration;
    private String lockReason;
    private LocalDateTime unlockTime;
    private String unlockReason;
    private Integer operatorBy;
    // 0封停中 1已解封
    private Integer unlockStatus;
}

