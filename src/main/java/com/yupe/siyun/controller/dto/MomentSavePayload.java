package com.yupe.siyun.controller.dto;

import lombok.Data;

@Data
public class MomentSavePayload {
    private String title;
    private String keywords;
    private String coverUrl;
    private String content;
}
