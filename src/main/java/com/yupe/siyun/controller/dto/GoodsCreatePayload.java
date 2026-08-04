package com.yupe.siyun.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsCreatePayload {
    private Integer cateId;
    private String goodsName;
    private String keywords;
    private List<String> imageUrls;
    private BigDecimal priceOriginal;
    private String intro;
    private List<String> serviceTags;
}
