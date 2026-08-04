package com.yupe.siyun.controller.dto;

import lombok.Data;

@Data
public class CouponGoodsBindPayload {
    private Integer targetType;
    private Integer couponId;
    private Integer goodsId;
    private Integer quota;
}
