package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 商品编号
     */
    private String goodsSn;

    /**
     * 所属商品分类id
     */
    private Integer cateId;

    /**
     * 0普通 1新品 2推荐
     */
    private Integer recommendStatus;

    /**
     * 0不可用优惠券 1可用优惠券
     */
    private Integer couponAllowed;

    /**
     * 服务保证标签(退货无忧/快速退款/包邮等)
     */
    private String serviceTags;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 搜索关键字
     */
    private String keywords;

    /**
     * 商品主图路径 (★需要上传)
     */
    private String mainPicUrl;

    /**
     * 商品现价
     */
    private BigDecimal pricePromotion;

    /**
     * 商品原价
     */
    private BigDecimal priceOriginal;

    /**
     * 商品描述简介
     */
    private String intro;

    /**
     * 点击总量
     */
    private Integer countView;

    /**
     * 0已删除 1下架 2上架
     */
    private Integer status;

    /**
     * 创建人id
     */
    private Integer createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Integer updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

