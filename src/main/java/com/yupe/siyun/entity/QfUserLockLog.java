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

    /**
     * 用户主键/账号
     */
    private Integer userId;

    /**
     * 封停时间
     */
    private LocalDateTime lockTime;

    /**
     * 封停时长(分钟)
     */
    private Integer lockDuration;

    /**
     * 封停原因
     */
    private String lockReason;

    /**
     * 解封时间
     */
    private LocalDateTime unlockTime;

    /**
     * 解封原因
     */
    private String unlockReason;

    /**
     * 操作人(后台管理用户id)
     */
    private Integer operatorBy;

    /**
     * 0封停中 1已解封
     */
    private Integer unlockStatus;
}

