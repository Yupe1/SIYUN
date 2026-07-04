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

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer couponId;
    private Integer userId;
    private LocalDateTime getTime;
    //0未使用 1已使用 2已过期
    private Integer status;
    private LocalDateTime useTime;
    private Integer orderId;
}

