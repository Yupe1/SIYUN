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
 * 内容审核日志表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("qf_audit_log")
public class QfAuditLog implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联实体主键(视频ID/商品ID)
     */
    private Integer entityId;

    /**
     * 1视频 2商品
     */
    private Integer entityType;

    /**
     * 发起时间
     */
    private LocalDateTime applyTime;

    /**
     * 发起人标识
     */
    private Integer applicantId;

    /**
     * 执行人标识
     */
    private Integer auditorId;

    /**
     * 执行时间
     */
    private LocalDateTime auditTime;

    /**
     * 0拒绝 1通过
     */
    private Integer auditResult;

    /**
     * 反馈详情/需要改进的地方
     */
    private String feedbackDetail;

    /**
     * 备注
     */
    private String remark;
}

