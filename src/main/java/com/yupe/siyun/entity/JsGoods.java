package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品主表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("js_goods")
public class JsGoods implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer cateId;
    //0普通 1新品 2推荐
    private Integer recommendStatus;
    //服务保证标签(退货无忧/快速退款/包邮等)
    private String serviceTags;

    private String goodsName;
    private String keywords;
    private String mainPicUrl;
    private BigDecimal priceOriginal;
    private String intro;

    //0已删除 1下架 2上架
    private Integer status;

    private Integer createBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

