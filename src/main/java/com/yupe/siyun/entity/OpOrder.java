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
 * 全套订单表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_order")
public class OpOrder implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 前台用户id
     */
    private Integer userId;

    /**
     * 用户收货地址id
     */
    private Integer addressId;

    /**
     * 用户编号
     */
    private String userSn;

    /**
     * 全站唯一订单编号
     */
    private String orderSn;

    /**
     * 关联购买商品/课程id
     */
    private Integer entityId;

    /**
     * 1视频/课程 2实体商品
     */
    private Integer entityType;

    /**
     * 0待付款 1已付款 2待发货 3已发货 4已签收 5退货申请 6退货中 7已退货 8取消交易
     */
    private Integer status;

    /**
     * 商品总数量
     */
    private Integer totalQuantity;

    /**
     * 商品总价
     */
    private BigDecimal priceTotal;

    /**
     * 实际订单应付金额
     */
    private BigDecimal pricePay;

    /**
     * 运费金额
     */
    private BigDecimal priceFreight;

    /**
     * 物流快递编号
     */
    private String deliverySn;

    /**
     * 1微信 2支付宝 3余额支付
     */
    private Integer payChannel;

    /**
     * 第三方支付流水号
     */
    private String paySn;

    /**
     * 完整收货地址文字
     */
    private String fullAddress;

    /**
     * 付款时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 1网页/H5 2小程序 3App
     */
    private Integer sourceType;

    /**
     * 0待评论 1已评论
     */
    private Integer commentStatus;

    /**
     * 用户下单备注
     */
    private String userRemark;

    /**
     * 下单时间
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

