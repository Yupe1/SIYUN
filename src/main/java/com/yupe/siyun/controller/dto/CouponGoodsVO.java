package com.yupe.siyun.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponGoodsVO {
    private Integer id;
    private Integer goodsId;
    private String itemName;
    private Integer couponId;
    private String couponSn;
    private String couponName;
    private Double amount;
    private Integer applyType;
    private Integer quota;
    private Integer statusShelf;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
