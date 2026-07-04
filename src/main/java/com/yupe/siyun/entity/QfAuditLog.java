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
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer entityId;
    //1视频 2商品
    private Integer entityType;
    private LocalDateTime applyTime;
    //发起人标识
    private Integer applicantId;
    private Integer auditorId;
    private LocalDateTime auditTime;
    //0拒绝 1通过
    private Integer auditResult;
    //反馈详情/需要改进的地方
    private String feedbackDetail;
    private String remark;
}

