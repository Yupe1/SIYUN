package com.yupe.siyun.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
    视图用于用户-商品查询
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("v_coupon")
public class OpCouponVO implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String couponSn;
    private String couponName;
    private Double amount;
    private String imgUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    //0无限制 1仅限实体商品 2仅限视频课程
    private Integer applyType;

    private Integer userId;
    private Integer goodsId;

}
