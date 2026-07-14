package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_goods")
public class JsGoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String cateName;
    //0普通 1新品 2推荐
    private Integer recommendStatus;
    //服务保证标签(退货无忧/快速退款/包邮等)
    private String serviceTags;
    private String goodsName;
    private String keywords;
    private String mainPicUrl;
    private BigDecimal priceOriginal;
    private BigDecimal pricePromotion;
    private String intro;
    private Integer salesVolume;
    private Integer createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
