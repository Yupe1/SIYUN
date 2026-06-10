package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券使用明细表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_coupon_user")
public class OpCouponUser implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 优惠券id
     */
    private Integer couponId;

    /**
     * 前台用户id
     */
    private Integer userId;

    /**
     * 获取时间
     */
    private LocalDateTime getTime;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 0未使用 1已使用 2已过期
     */
    private Integer status;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    /**
     * 关联订单id
     */
    private Integer orderId;
}

