package com.yupe.siyun.controller.dto;

import lombok.Data;

@Data
public class AuditPayload {
    private Integer auditResult;
    private Integer statusShelf;
    private String feedbackDetail;
    private String remark;
}
