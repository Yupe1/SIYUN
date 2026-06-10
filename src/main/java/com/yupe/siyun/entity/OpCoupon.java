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
 * 营销优惠券表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("op_coupon")
public class OpCoupon implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 优惠券编号
     */
    private String couponSn;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券面值金额
     */
    private BigDecimal amount;

    /**
     * 图片路径 (★需要上传)
     */
    private String imgUrl;

    /**
     * 生效开始时间
     */
    private LocalDateTime startTime;

    /**
     * 失效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 0下线 1上线
     */
    private Integer statusShelf;

    /**
     * 1无限制自领 2活动自动发放 3特定营销赠送
     */
    private Integer issueType;

    /**
     * 发行总数量
     */
    private Integer totalCount;

    /**
     * 已被使用数量
     */
    private Integer usedCount;

    /**
     * 0无限制 1仅限实体商品 2仅限视频课程
     */
    private Integer applyType;

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

