package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 全套订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_order")
public class OpOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private String orderSn;
    private Integer entityId;
    //1视频/课程 2实体商品
    private Integer entityType;
    //0待付款 1已付款 2待发货 3已发货 4已签收 5退货申请 6退货中 7已退货 8取消交易
    private Integer status;
    private Integer totalQuantity;
    private BigDecimal priceTotal;
    private BigDecimal pricePay;
    private BigDecimal priceFreight;//运费金额
    private String deliverySn;//物流快递编号
    //1微信 2支付宝 3余额支付
    private Integer payChannel;
    private String paySn;//第三方支付流水号
    private String fullAddress;
    private LocalDateTime payTime;
    //发货时间
    private LocalDateTime deliveryTime;
    //1网页/H5 2小程序 3App
    private Integer sourceType;
    //0待评论 1已评价
    private Integer commentStatus;
    private String userRemark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer updateBy;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

